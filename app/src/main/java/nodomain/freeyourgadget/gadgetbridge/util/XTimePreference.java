/*  Copyright (C) 2019-2024 Andreas Shimokawa

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
package nodomain.freeyourgadget.gadgetbridge.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import java.util.Locale;

public class XTimePreference extends DialogPreference {
    protected int hour = 0;
    protected int minute = 0;

    protected Format format = Format.AUTO;

    public XTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time;

        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        } else {
            if (defaultValue != null) {
                time = defaultValue.toString();
            } else {
                time = "00:00";
            }
        }

        String[] pieces = time.split(":");

        hour = Integer.parseInt(pieces[0]);
        minute = Integer.parseInt(pieces[1]);

        updateSummary();
    }

    public String getPrefValue() {
        return String.format(Locale.ROOT, "%02d:%02d", hour, minute);
    }

    public void setValue(final int hour, final int minute) {
        this.hour = hour;
        this.minute = minute;

        persistStringValue(getPrefValue());
    }

    void updateSummary() {
        if (is24HourFormat())
            setSummary(getTime24h());
        else
            setSummary(getTime12h());
    }

    String getTime24h() {
        return String.format("%02d", hour) + ":" + String.format("%02d", minute);
    }

    private String getTime12h() {
        String suffix = hour < 12 ? " AM" : " PM";
        int h = hour > 12 ? hour - 12 : hour;

        return h + ":" + String.format("%02d", minute) + suffix;
    }

    public void setFormat(final Format format) {
        this.format = format;
    }

    public Format getFormat() {
        return format;
    }

    void persistStringValue(String value) {
        persistString(value);
    }

    public boolean is24HourFormat() {
        switch (format) {
            case FORMAT_24H:
                return true;
            case FORMAT_12H:
                return false;
            case AUTO:
            default:
                return DateFormat.is24HourFormat(getContext());
        }
    }

    public enum Format {
        AUTO,
        FORMAT_24H,
        FORMAT_12H,
        ;
    }
}
