/*  Copyright (C) 2021-2024 Andreas Shimokawa, Daniel Dakhno, José Rebelo,
    Petr Vaněk

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
package nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgts2;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.InstallHandler;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.amazfitgts2.AmazfitGTS2MiniSupport;

public class AmazfitGTS2MiniCoordinator extends AmazfitGTS2Coordinator {
    private static final Logger LOG = LoggerFactory.getLogger(AmazfitGTS2MiniCoordinator.class);

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("Amazfit GTS2 mini", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public InstallHandler findInstallHandler(Uri uri, Context context) {
        AmazfitGTS2MiniFWInstallHandler handler = new AmazfitGTS2MiniFWInstallHandler(uri, context);
        return handler.isValid() ? handler : null;
    }

    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return new int[]{
                R.xml.devicesettings_amazfitgts2mini,
                R.xml.devicesettings_vibrationpatterns,
                R.xml.devicesettings_wearlocation,
                R.xml.devicesettings_heartrate_sleep,
                R.xml.devicesettings_goal_notification,
                R.xml.devicesettings_timeformat,
                R.xml.devicesettings_liftwrist_display,
                R.xml.devicesettings_inactivity_dnd,
                R.xml.devicesettings_disconnectnotification,
                R.xml.devicesettings_sync_calendar,
                R.xml.devicesettings_reserve_reminders_calendar,
                R.xml.devicesettings_expose_hr_thirdparty,
                R.xml.devicesettings_bt_connected_advertisement,
                R.xml.devicesettings_device_actions,
                R.xml.devicesettings_phone_silent_mode,
                R.xml.devicesettings_high_mtu,
                R.xml.devicesettings_overwrite_settings_on_connection,
                R.xml.devicesettings_huami2021_fetch_operation_time_unit,
                R.xml.devicesettings_transliteration
        };
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_amazfit_gts2_mini;
    }


    @Override
    public int getDefaultIconResource() {
        return R.drawable.ic_device_amazfit_bip;
    }

    @Override
    public int getDisabledIconResource() {
        return R.drawable.ic_device_amazfit_bip_disabled;
    }

    @NonNull
    @Override
    public Class<? extends DeviceSupport> getDeviceSupportClass() {
        return AmazfitGTS2MiniSupport.class;
    }
}
