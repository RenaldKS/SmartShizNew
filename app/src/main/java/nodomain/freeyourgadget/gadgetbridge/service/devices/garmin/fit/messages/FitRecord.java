package nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.messages;

import androidx.annotation.Nullable;

import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.model.ActivityPoint;
import nodomain.freeyourgadget.gadgetbridge.model.GPSCoordinate;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.GarminUtils;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.RecordData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.RecordDefinition;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.RecordHeader;

//
// WARNING: This class was auto-generated, please avoid modifying it directly.
// See nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.codegen.FitCodeGen
//
public class FitRecord extends RecordData {
    public FitRecord(final RecordDefinition recordDefinition, final RecordHeader recordHeader) {
        super(recordDefinition, recordHeader);

        final int globalNumber = recordDefinition.getGlobalFITMessage().getNumber();
        if (globalNumber != 20) {
            throw new IllegalArgumentException("FitRecord expects global messages of " + 20 + ", got " + globalNumber);
        }
    }

    @Nullable
    public Long getLatitude() {
        return (Long) getFieldByNumber(0);
    }

    @Nullable
    public Long getLongitude() {
        return (Long) getFieldByNumber(1);
    }

    @Nullable
    public Integer getHeartRate() {
        return (Integer) getFieldByNumber(3);
    }

    @Nullable
    public Long getDistance() {
        return (Long) getFieldByNumber(5);
    }

    @Nullable
    public Long getEnhancedSpeed() {
        return (Long) getFieldByNumber(73);
    }

    @Nullable
    public Long getEnhancedAltitude() {
        return (Long) getFieldByNumber(78);
    }

    @Nullable
    public Integer getWristHeartRate() {
        return (Integer) getFieldByNumber(136);
    }

    @Nullable
    public Integer getBodyBattery() {
        return (Integer) getFieldByNumber(143);
    }

    @Nullable
    public Long getTimestamp() {
        return (Long) getFieldByNumber(253);
    }

    // manual changes below

    public ActivityPoint toActivityPoint() {
        final ActivityPoint activityPoint = new ActivityPoint();
        activityPoint.setTime(new Date(getComputedTimestamp()));
        if (getLatitude() != null && getLongitude() != null) {
            activityPoint.setLocation(new GPSCoordinate(
                    GarminUtils.semicirclesToDegrees(getLongitude().longValue()),
                    GarminUtils.semicirclesToDegrees(getLatitude().longValue()),
                    getEnhancedAltitude() != null ? getEnhancedAltitude() / 10d : GPSCoordinate.UNKNOWN_ALTITUDE
            ));
        }
        if (getHeartRate() != null) {
            activityPoint.setHeartRate(getHeartRate());
        }
        if (getEnhancedSpeed() != null) {
            activityPoint.setSpeed(getEnhancedSpeed());
        }
        return activityPoint;
    }
}
