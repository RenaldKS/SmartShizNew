/*  Copyright (C) 2023-2024 José Rebelo, Yoran Vulker

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.activity;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TimeZone;

import nodomain.freeyourgadget.gadgetbridge.BuildConfig;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.XiaomiPreferences;
import nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.XiaomiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.services.XiaomiHealthService;
import nodomain.freeyourgadget.gadgetbridge.util.CheckSums;
import nodomain.freeyourgadget.gadgetbridge.util.FileUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class XiaomiActivityFileFetcher {
    private static final Logger LOG = LoggerFactory.getLogger(XiaomiActivityFileFetcher.class);

    private final XiaomiHealthService mHealthService;

    private final Queue<XiaomiActivityFileId> mFetchQueue = new PriorityQueue<>();
    private ByteArrayOutputStream mBuffer = new ByteArrayOutputStream();
    private boolean isFetching = false;

    public XiaomiActivityFileFetcher(final XiaomiHealthService healthService) {
        this.mHealthService = healthService;
    }

    public void addChunk(final byte[] chunk) {
        final int total = BLETypeConversions.toUint16(chunk, 0);
        final int num = BLETypeConversions.toUint16(chunk, 2);

        LOG.debug("Got activity chunk {}/{}", num, total);

        mBuffer.write(chunk, 4, chunk.length - 4);

        if (num == total) {
            final byte[] data = mBuffer.toByteArray();
            mBuffer = new ByteArrayOutputStream();

            if (data.length < 13) {
                LOG.warn("Activity data length of {} is too short", data.length);
                // FIXME this may mess up the order.. maybe we should just abort
                triggerNextFetch();
                return;
            }

            final int arrCrc32 = CheckSums.getCRC32(data, 0, data.length - 4);
            final int expectedCrc32 = BLETypeConversions.toUint32(data, data.length - 4);

            if (arrCrc32 != expectedCrc32) {
                LOG.warn(
                        "Invalid activity data checksum: got {}, expected {}",
                        String.format("%08X", arrCrc32),
                        String.format("%08X", expectedCrc32)
                );
                // FIXME this may mess up the order.. maybe we should just abort
                triggerNextFetch();
                return;
            }

            if (data[7] != 0) {
                LOG.warn(
                        "Unexpected activity payload byte {} at position 7 - parsing might fail",
                        String.format("0x%02X", data[7])
                );
            }

            final byte[] fileIdBytes = Arrays.copyOfRange(data, 0, 7);
            final byte[] activityData = Arrays.copyOfRange(data, 8, data.length - 4);
            final XiaomiActivityFileId fileId = XiaomiActivityFileId.from(fileIdBytes);

            if (BuildConfig.DEBUG) {
                // FIXME comment this out
                dumpBytesToExternalStorage(fileId, data);
            }

            if (!XiaomiPreferences.keepActivityDataOnDevice(mHealthService.getSupport().getDevice())) {
                LOG.debug("Acking recorded data {}", fileId);
                // TODO is this too early?
                mHealthService.ackRecordedData(fileId);
            }

            final XiaomiActivityParser activityParser = XiaomiActivityParser.create(fileId);
            if (activityParser == null) {
                LOG.warn("Failed to find parser for {}", fileId);
                triggerNextFetch();
                return;
            }

            try {
                if (activityParser.parse(mHealthService.getSupport(), fileId, activityData)) {
                    LOG.info("Successfully parsed {}", fileId);
                } else {
                    LOG.warn("Failed to parse {}", fileId);
                }
            } catch (final Exception ex) {
                LOG.error("Exception while parsing " + fileId, ex);
            }

            triggerNextFetch();
        }
    }

    public void fetch(final List<XiaomiActivityFileId> fileIds) {
        mFetchQueue.addAll(fileIds);
        if (!isFetching) {
            // Currently not fetching anything, fetch the next
            isFetching = true;
            final XiaomiSupport support = mHealthService.getSupport();
            final Context context = support.getContext();
            GB.updateTransferNotification(context.getString(R.string.busy_task_fetch_activity_data), "", true, 0, context);
            support.getDevice().setBusyTask(context.getString(R.string.busy_task_fetch_activity_data));
            support.getDevice().sendDeviceUpdateIntent(support.getContext());
            triggerNextFetch();
        }
    }

    private void triggerNextFetch() {
        final XiaomiActivityFileId fileId = mFetchQueue.poll();

        if (fileId == null) {
            LOG.debug("Nothing more to fetch");
            isFetching = false;
            GB.signalActivityDataFinish();
            mHealthService.getSupport().getDevice().unsetBusyTask();
            GB.updateTransferNotification(null, "", false, 100, mHealthService.getSupport().getContext());
            mHealthService.getSupport().getDevice().sendDeviceUpdateIntent(mHealthService.getSupport().getContext());
            return;
        }

        LOG.debug("Triggering next fetch for: {}", fileId);

        mHealthService.requestRecordedData(fileId);
    }

    protected void dumpBytesToExternalStorage(final XiaomiActivityFileId fileId, final byte[] bytes) {
        try {
            final File externalFilesDir = FileUtils.getExternalFilesDir();
            final File targetDir = new File(externalFilesDir, "rawFetchOperations");
            targetDir.mkdirs();

            final File outputFile = new File(targetDir, fileId.getFilename());

            final OutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(bytes);
            outputStream.close();
        } catch (final Exception e) {
            LOG.error("Failed to dump bytes to storage", e);
        }
    }
}
