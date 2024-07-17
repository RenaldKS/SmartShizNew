package nodomain.freeyourgadget.gadgetbridge.devices.garmin.watches.vivoactive;

import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.GarminCoordinator;

public class GarminVivoActive4Coordinator extends GarminCoordinator {
    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^vívoactive 4$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_garmin_vivoactive_4;
    }
}
