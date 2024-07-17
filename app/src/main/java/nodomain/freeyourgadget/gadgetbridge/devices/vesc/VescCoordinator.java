/*  Copyright (C) 2021-2024 Damien Gaignon, Daniel Dakhno, José Rebelo,
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
package nodomain.freeyourgadget.gadgetbridge.devices.vesc;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractBLEDeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.InstallHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.vesc.VescDeviceSupport;

public class VescCoordinator extends AbstractBLEDeviceCoordinator {
    public final static String UUID_SERVICE_SERIAL_HM10 = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public final static String UUID_CHARACTERISTIC_SERIAL_TX_HM10 = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static String UUID_CHARACTERISTIC_SERIAL_RX_HM10 = "0000ffe1-0000-1000-8000-00805f9b34fb";

    public final static String UUID_SERVICE_SERIAL_NRF = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public final static String UUID_CHARACTERISTIC_SERIAL_TX_NRF = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public final static String UUID_CHARACTERISTIC_SERIAL_RX_NRF = "0000ffe1-0000-1000-8000-00805f9b34fb";


    @Override
    protected void deleteDevice(@NonNull GBDevice gbDevice, @NonNull Device device, @NonNull DaoSession session) throws GBException {

    }

    @Override
    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return new int[]{
            R.xml.devicesettings_vesc
        };
    }

    @NonNull
    @Override
    public Class<? extends DeviceSupport> getDeviceSupportClass() {
        return VescDeviceSupport.class;
    }

    @Override
    public boolean supports(GBDeviceCandidate candidate) {
        if(!candidate.getName().toLowerCase().contains("vesc")){
            return false;
        }
        ParcelUuid[] uuids = candidate.getServiceUuids();
        for(ParcelUuid uuid : uuids){
            if(uuid.getUuid().toString().equals(UUID_SERVICE_SERIAL_NRF)){
                return true;
            }else if(uuid.getUuid().toString().equals(UUID_SERVICE_SERIAL_HM10)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<? extends Activity> getPairingActivity() {
        return null;
    }

    @Override
    public boolean supportsActivityDataFetching() {
        return true;
    }

    @Override
    public int getBondingStyle() {
        return BONDING_STYLE_NONE;
    }

    @Override
    public boolean supportsActivityTracking() {
        return false;
    }

    @Override
    public SampleProvider<? extends ActivitySample> getSampleProvider(GBDevice device, DaoSession session) {
        return null;
    }

    @Override
    public InstallHandler findInstallHandler(Uri uri, Context context) {
        return null;
    }

    @Override
    public boolean supportsScreenshots(final GBDevice device) {
        return false;
    }

    @Override
    public int getAlarmSlotCount(GBDevice device) {
        return 0;
    }

    @Override
    public boolean supportsHeartRateMeasurement(GBDevice device) {
        return false;
    }

    @Override
    public String getManufacturer() {
        return "Benjamin Vedder";
    }

    @Override
    public boolean supportsAppsManagement(final GBDevice device) {
        return true;
    }

    @Override
    public Class<? extends Activity> getAppsManagementActivity() {
        return VescControlActivity.class;
    }

    @Override
    public boolean supportsCalendarEvents() {
        return false;
    }

    @Override
    public boolean supportsRealtimeData() {
        return false;
    }

    @Override
    public boolean supportsWeather() {
        return false;
    }

    @Override
    public boolean supportsFindDevice() {
        return false;
    }


    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_vesc;
    }


    @Override
    public int getDefaultIconResource() {
        return R.drawable.ic_device_vesc;
    }

    @Override
    public int getDisabledIconResource() {
        return R.drawable.ic_device_vesc_disabled;
    }
}
