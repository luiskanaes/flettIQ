package com.example.fleetIq.service;

import com.example.fleetIq.model.Alarm;
import com.example.fleetIq.model.Geofence;
import com.example.fleetIq.model.Track;
import com.example.fleetIq.repository.AlarmRepository;
import com.example.fleetIq.repository.GeofenceRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private GeofenceRepository geofenceRepository;

    @Override
    public void checkAndLogAlarm(Track track) throws Exception {
        if (track.getImei() == null || track.getLatitude() == null || track.getLongitude() == null) {
            throw new IllegalArgumentException("Track must have IMEI, latitude, and longitude");
        }

        List<Geofence> geofences = geofenceRepository.findAll(); // Fetch all geofences; optimize with IMEI filter if needed
        for (Geofence geofence : geofences) {
            if (!geofence.getImei().equals(track.getImei())) continue; // Only check geofences for this IMEI

            JSONArray pointsArray = new JSONArray(geofence.getPoints());
            double[] x = new double[pointsArray.length()];
            double[] y = new double[pointsArray.length()];
            for (int i = 0; i < pointsArray.length(); i++) {
                JSONArray point = pointsArray.getJSONArray(i);
                y[i] = point.getDouble(0); // lat as y
                x[i] = point.getDouble(1); // lon as x
            }

            if (isPointInPolygon(track.getLongitude(), track.getLatitude(), x, y)) {
                // Log alarm
                Alarm alarm = new Alarm();
                alarm.setImei(track.getImei());
                alarm.setGeofenceId(geofence.getId());
                //alarm.setTrackTime(track.getTime());
                alarm.setEntryTime(System.currentTimeMillis() / 1000L);
                alarmRepository.save(alarm);
                System.out.println("Alarm logged for IMEI " + track.getImei() + " entering geofence " + geofence.getId());
            }
        }
    }

    @Override
    public List<Alarm> getAlarms() {
        return alarmRepository.findAll();
    }

    // Ray-casting algorithm for point in polygon
    private boolean isPointInPolygon(double pointX, double pointY, double[] polygonX, double[] polygonY) {
        int polygonVertices = polygonX.length;
        boolean isIn = false;
        for (int i = 0, j = polygonVertices - 1; i < polygonVertices; j = i++) {
            if ((polygonY[i] > pointY) != (polygonY[j] > pointY) &&
                    (pointX < (polygonX[j] - polygonX[i]) * (pointY - polygonY[i]) / (polygonY[j] - polygonY[i]) + polygonX[i])) {
                isIn = !isIn;
            }
        }
        return isIn;
    }
}