/*  Copyright (C) 2019-2024 José Rebelo, Petr Vaněk

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
package nodomain.freeyourgadget.gadgetbridge.activities.charts;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.AboutUserPreferencesActivity;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractPreferenceFragment;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractSettingsActivityV2;
import nodomain.freeyourgadget.gadgetbridge.util.GBPrefs;

public class ChartsPreferencesActivity extends AbstractSettingsActivityV2 {
    @Override
    protected String fragmentTag() {
        return ChartsPreferencesFragment.FRAGMENT_TAG;
    }

    @Override
    protected PreferenceFragmentCompat newFragment() {
        return new ChartsPreferencesFragment();
    }

    public static class ChartsPreferencesFragment extends AbstractPreferenceFragment {
        static final String FRAGMENT_TAG = "CHARTS_PREFERENCES_FRAGMENT";

        @Override
        public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
            setPreferencesFromResource(R.xml.charts_preferences, rootKey);

            setInputTypeFor(GBPrefs.CHART_MAX_HEART_RATE, InputType.TYPE_CLASS_NUMBER);
            setInputTypeFor(GBPrefs.CHART_MIN_HEART_RATE, InputType.TYPE_CLASS_NUMBER);
            setInputTypeFor("chart_sleep_lines_limit", InputType.TYPE_CLASS_NUMBER);
            setInputTypeFor("chart_list_min_session_length", InputType.TYPE_CLASS_NUMBER);
            setInputTypeFor("chart_list_max_idle_phase_length", InputType.TYPE_CLASS_NUMBER);
            setInputTypeFor("chart_list_min_steps_per_minute", InputType.TYPE_CLASS_NUMBER);
            setInputTypeFor("chart_list_min_steps_per_minute_for_run", InputType.TYPE_CLASS_NUMBER);

            final Preference aboutUserPref = findPreference("pref_category_activity_personal");
            if (aboutUserPref != null) {
                aboutUserPref.setOnPreferenceClickListener(preference -> {
                    final Intent enableIntent = new Intent(getActivity(), AboutUserPreferencesActivity.class);
                    startActivity(enableIntent);
                    return true;
                });
            }
        }
    }
}
