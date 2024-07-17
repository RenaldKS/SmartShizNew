/*  Copyright (C) 2023-2024 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.devices.xiaomi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.XiaomiActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.XiaomiActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.XiaomiSleepStageSample;
import nodomain.freeyourgadget.gadgetbridge.entities.XiaomiSleepTimeSample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
import nodomain.freeyourgadget.gadgetbridge.util.RangeMap;

public class XiaomiSampleProvider extends AbstractSampleProvider<XiaomiActivitySample> {
    private static final Logger LOG = LoggerFactory.getLogger(XiaomiSampleProvider.class);

    public XiaomiSampleProvider(final GBDevice device, final DaoSession session) {
        super(device, session);
    }

    @Override
    public AbstractDao<XiaomiActivitySample, ?> getSampleDao() {
        return getSession().getXiaomiActivitySampleDao();
    }

    @Nullable
    @Override
    protected Property getRawKindSampleProperty() {
        return XiaomiActivitySampleDao.Properties.RawKind;
    }

    @NonNull
    @Override
    protected Property getTimestampSampleProperty() {
        return XiaomiActivitySampleDao.Properties.Timestamp;
    }

    @NonNull
    @Override
    protected Property getDeviceIdentifierSampleProperty() {
        return XiaomiActivitySampleDao.Properties.DeviceId;
    }

    @Override
    public int normalizeType(final int rawType) {
        // TODO
        return rawType;
    }

    @Override
    public int toRawActivityKind(final int activityKind) {
        // TODO
        return activityKind;
    }

    @Override
    public float normalizeIntensity(final int rawIntensity) {
        return rawIntensity / 100f;
    }

    @Override
    public XiaomiActivitySample createActivitySample() {
        return new XiaomiActivitySample();
    }

    @Override
    protected List<XiaomiActivitySample> getGBActivitySamples(final int timestamp_from, final int timestamp_to, final int activityType) {
        final List<XiaomiActivitySample> samples = super.getGBActivitySamples(timestamp_from, timestamp_to, activityType);

        overlaySleep(samples, timestamp_from, timestamp_to);

        return samples;
    }

    /**
     * See {@link nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.activity.impl.SleepDetailsParser}
     */
    private static int getActivityKindForSample(final XiaomiSleepStageSample sample) {
        switch (sample.getStage()) {
            case 2:
                return ActivityKind.TYPE_DEEP_SLEEP;
            case 3:
                return ActivityKind.TYPE_LIGHT_SLEEP;
            case 4:
                return ActivityKind.TYPE_REM_SLEEP;
            default: // default to awake
                return ActivityKind.TYPE_UNKNOWN;
        }
    }

    /**
     * Overlay sleep states on activity samples, since they are stored on a separate table.
     *
     * @implNote In order to determine whether a sleep session was ongoing at the start of the
     * given range and what the detected sleep stage was at that time, the last sleep stage and
     * sleep time sample before the given range will be queried and included in the results if
     * found.
     */
    public void overlaySleep(final List<XiaomiActivitySample> samples, final int timestamp_from, final int timestamp_to) {
        final RangeMap<Long, Integer> stagesMap = new RangeMap<>();

        final XiaomiSleepStageSampleProvider sleepStagesSampleProvider = new XiaomiSleepStageSampleProvider(getDevice(), getSession());

        // Retrieve the last stage before this time range, as the user could have been asleep during
        // the range transition
        final XiaomiSleepStageSample lastSleepStageBeforeRange = sleepStagesSampleProvider.getLastSampleBefore(timestamp_from * 1000L);

        if (lastSleepStageBeforeRange != null) {
            LOG.debug("Last sleep stage before range: ts={}, stage={}", lastSleepStageBeforeRange.getTimestamp(), lastSleepStageBeforeRange.getStage());
            stagesMap.put(lastSleepStageBeforeRange.getTimestamp(), getActivityKindForSample(lastSleepStageBeforeRange));
        }

        // Retrieve all sleep stage samples during the range
        final List<XiaomiSleepStageSample> sleepStagesInRange = sleepStagesSampleProvider.getAllSamples(
                timestamp_from * 1000L,
                timestamp_to * 1000L
        );

        if (!sleepStagesInRange.isEmpty()) {
            // We got actual sleep stages
            LOG.debug("Found {} sleep stage samples between {} and {}", sleepStagesInRange.size(), timestamp_from, timestamp_to);

            for (final XiaomiSleepStageSample stageSample : sleepStagesInRange) {
                stagesMap.put(stageSample.getTimestamp(), getActivityKindForSample(stageSample));
            }
        }

        final XiaomiSleepTimeSampleProvider sleepTimeSampleProvider = new XiaomiSleepTimeSampleProvider(getDevice(), getSession());

        // Find last sleep sample before the requested range, as the recorded wake up time may be
        // in the current range
        final XiaomiSleepTimeSample lastSleepTimesBeforeRange = sleepTimeSampleProvider.getLastSampleBefore(timestamp_from * 1000L);

        if (lastSleepTimesBeforeRange != null) {
            stagesMap.put(lastSleepTimesBeforeRange.getWakeupTime(), ActivityKind.TYPE_UNKNOWN);
            stagesMap.put(lastSleepTimesBeforeRange.getTimestamp(), ActivityKind.TYPE_LIGHT_SLEEP);
        }

        // Find all wake up and sleep samples in the current time range
        final List<XiaomiSleepTimeSample> sleepTimesInRange = sleepTimeSampleProvider.getAllSamples(
                timestamp_from * 1000L,
                timestamp_to * 1000L
        );

        if (!sleepTimesInRange.isEmpty()) {
            LOG.debug("Found {} sleep samples between {} and {}", sleepTimesInRange.size(), timestamp_from, timestamp_to);
            for (final XiaomiSleepTimeSample stageSample : sleepTimesInRange) {
                if (sleepStagesInRange.isEmpty()) {
                    // Only overlay them as light sleep if we don't have actual sleep stages
                    stagesMap.put(stageSample.getTimestamp(), ActivityKind.TYPE_LIGHT_SLEEP);
                }

                // We need to set the wakeup times, because some bands don't report them in the stage samples (see #3502)
                stagesMap.put(stageSample.getWakeupTime(), ActivityKind.TYPE_UNKNOWN);
            }
        }

        if (!stagesMap.isEmpty()) {
            LOG.debug("Found {} sleep samples between {} and {}", stagesMap.size(), timestamp_from, timestamp_to);

            for (final XiaomiActivitySample sample : samples) {
                final long ts = sample.getTimestamp() * 1000L;
                final Integer sleepType = stagesMap.get(ts);
                if (sleepType != null && !sleepType.equals(ActivityKind.TYPE_UNKNOWN)) {
                    sample.setRawKind(sleepType);

                    switch (sleepType) {
                        case ActivityKind.TYPE_DEEP_SLEEP:
                            sample.setRawIntensity(20);
                            break;
                        case ActivityKind.TYPE_LIGHT_SLEEP:
                            sample.setRawIntensity(30);
                            break;
                        case ActivityKind.TYPE_REM_SLEEP:
                            sample.setRawIntensity(40);
                            break;
                    }
                }
            }
        }
    }
}
