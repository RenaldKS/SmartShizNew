/*  Copyright (C) 2020-2024 José Rebelo, Petr Vaněk

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
package nodomain.freeyourgadget.gadgetbridge.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityPoint;
import nodomain.freeyourgadget.gadgetbridge.model.GPSCoordinate;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.FitFile;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.FitImporter;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.RecordData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.fit.messages.FitRecord;
import nodomain.freeyourgadget.gadgetbridge.util.gpx.GpxParseException;
import nodomain.freeyourgadget.gadgetbridge.util.gpx.GpxParser;
import nodomain.freeyourgadget.gadgetbridge.util.gpx.model.GpxFile;

import static android.graphics.Bitmap.createBitmap;


public class ActivitySummariesGpsFragment extends AbstractGBFragment {
    private static final Logger LOG = LoggerFactory.getLogger(ActivitySummariesGpsFragment.class);
    private ImageView gpsView;
    private int CANVAS_SIZE = 360;
    private File inputFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gps, container, false);
        gpsView = rootView.findViewById(R.id.activitygpsview);
        if (inputFile != null) {
            processInBackgroundThread();
        }
        return rootView;
    }

    public void set_data(File inputFile) {
        this.inputFile = inputFile;
        if (gpsView != null) { //first fragment inflate is AFTER this is called
            processInBackgroundThread();
        }
    }

    private void processInBackgroundThread() {
        final Canvas canvas = createCanvas(gpsView);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<GPSCoordinate> points = new ArrayList<>();
                if (inputFile.getName().endsWith(".gpx")) {
                    try (FileInputStream inputStream = new FileInputStream(inputFile)) {
                        final GpxParser gpxParser = new GpxParser(inputStream);
                        points.addAll(gpxParser.getGpxFile().getPoints());
                    } catch (final IOException e) {
                        LOG.error("Failed to open {}", inputFile, e);
                        return;
                    } catch (final GpxParseException e) {
                        LOG.error("Failed to parse gpx file", e);
                        return;
                    }
                } else if (inputFile.getName().endsWith(".fit")) {
                    try {
                        FitFile fitFile = FitFile.parseIncoming(inputFile);
                        for (final RecordData record : fitFile.getRecords()) {
                            if (record instanceof FitRecord) {
                                final ActivityPoint activityPoint = ((FitRecord) record).toActivityPoint();
                                if (activityPoint.getLocation() != null) {
                                    points.add(activityPoint.getLocation());
                                }
                            }
                        }
                    } catch (final IOException e) {
                        LOG.error("Failed to open {}", inputFile, e);
                        return;
                    } catch (final Exception e) {
                        LOG.error("Failed to parse fit file", e);
                        return;
                    }
                } else {
                    LOG.warn("Unknown file type {}", inputFile.getName());
                    return;
                }

                if (!points.isEmpty()) {
                    drawTrack(canvas, points);
                }
            }
        }).start();
    }

    private void drawTrack(Canvas canvas, List<? extends GPSCoordinate> trackPoints) {
        double maxLat = (Collections.max(trackPoints, new GPSCoordinate.compareLatitude())).getLatitude();
        double minLat = (Collections.min(trackPoints, new GPSCoordinate.compareLatitude())).getLatitude();
        double maxLon = (Collections.max(trackPoints, new GPSCoordinate.compareLongitude())).getLongitude();
        double minLon = (Collections.min(trackPoints, new GPSCoordinate.compareLongitude())).getLongitude();
        double maxAlt = (Collections.max(trackPoints, new GPSCoordinate.compareElevation())).getAltitude();
        double minAlt = (Collections.min(trackPoints, new GPSCoordinate.compareElevation())).getAltitude();
        float scale_factor_w = (float) ((maxLon - minLon) / (maxLat - minLat));
        float scale_factor_h = (float) ((maxLat - minLat) / (maxLon - minLon));

        if (scale_factor_h > scale_factor_w) { //scaling to draw proportionally
            scale_factor_h = 1;
        } else {
            scale_factor_w = 1;
        }


        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(1);
        paint.setColor(getResources().getColor(R.color.chart_activity_light));

        for (GPSCoordinate p : trackPoints) {
            float lat = (float) ((p.getLatitude() - minLat) / (maxLat - minLat));
            float lon = (float) ((p.getLongitude() - minLon) / (maxLon - minLon));
            float alt = (float) ((p.getAltitude() - minAlt) / (maxAlt - minAlt));
            paint.setStrokeWidth(1 + alt); //make thicker with higher altitude, we could do more here
            canvas.drawPoint(CANVAS_SIZE * lon * scale_factor_w, CANVAS_SIZE * lat * scale_factor_h, paint);
        }
    }


    private Canvas createCanvas(ImageView imageView) {
        Bitmap bitmap = createBitmap(CANVAS_SIZE, CANVAS_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(GBApplication.getWindowBackgroundColor(getActivity()));
        //frame around, but it doesn't look so nice
        /*
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.chart_activity_light));
        canvas.drawRect(0,0,360,360,paint);
         */
        imageView.setImageBitmap(bitmap);
        imageView.setScaleY(-1f); //flip the canvas

        return canvas;
    }

    @Nullable
    @Override
    protected CharSequence getTitle() {
        return null;
    }

}

