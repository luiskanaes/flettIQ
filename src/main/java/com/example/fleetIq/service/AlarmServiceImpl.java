package com.example.fleetIq.service;

import com.example.fleetIq.model.Alarm;
import com.example.fleetIq.model.Device;
import com.example.fleetIq.model.Geofence;
import com.example.fleetIq.model.Track;
import com.example.fleetIq.repository.AlarmRepository;
import com.example.fleetIq.repository.DeviceRepository;
import com.example.fleetIq.repository.GeofenceRepository;
import com.example.fleetIq.repository.TrackRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private GeofenceRepository geofenceRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    // Método que se ejecuta automáticamente cada 3 segundos
    @Scheduled(fixedRate = 3000) // 3000 milisegundos = 3 segundos
    public void checkAlarmsAutomatically() {
        try {
            long fiveMinutesAgo = System.currentTimeMillis() / 1000L - 300; // 5 minutos = 300 segundos
            List<Track> latestTracks = trackRepository.findLatestTracksByImeiWithinLastMinutes(fiveMinutesAgo);

            for (Track track : latestTracks) {
                checkAndLogAlarm(track);
            }
        } catch (Exception e) {
            System.err.println("Error durante la verificación automática de alarmas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void checkAndLogAlarm(Track track) throws Exception {
        if (track.getImei() == null || track.getLatitude() == null || track.getLongitude() == null) {
            throw new IllegalArgumentException("Track must have IMEI, latitude, and longitude");
        }

        List<Geofence> geofences = geofenceRepository.findAll();
        for (Geofence geofence : geofences) {

            JSONArray pointsArray = new JSONArray(geofence.getPoints());
            double[] x = new double[pointsArray.length()];
            double[] y = new double[pointsArray.length()];
            for (int i = 0; i < pointsArray.length(); i++) {
                JSONArray point = pointsArray.getJSONArray(i);
                y[i] = point.getDouble(0); // lat as y
                x[i] = point.getDouble(1); // lon as x
            }

            // AQUÍ ESTÁ EL CAMBIO PRINCIPAL: Evaluar tanto entrada como salida
            boolean isCurrentlyInside = isPointInPolygon(track.getLongitude(), track.getLatitude(), x, y);
            boolean hasActiveEntry = alarmRepository.existsByImeiAndGeofenceIdAndExitTimeIsNull(track.getImei(), geofence.getId());

            if (isCurrentlyInside && !hasActiveEntry) {
                // ENTRADA: Está dentro y no tiene entrada activa
                System.out.println("✅ Está dentro y no tiene entrada activa " + track.getImei() + " entered geofence " + geofence.getId());
                Alarm alarm = new Alarm();
                alarm.setImei(track.getImei());
                alarm.setGeofenceId(geofence.getId());
                alarm.setTrackTime(track.getGpstime());
                alarm.setAlarmType("ENTRY");
                alarm.setLatitude(track.getLatitude());
                alarm.setLongitude(track.getLongitude());
                alarm.setDeviceName(deviceRepository.findById(Long.valueOf(track.getImei())).map(Device::getDeviceName).orElse("Unknown"));
                alarm.setPlateNumber(deviceRepository.findById(Long.valueOf(track.getImei())).map(Device::getPlateNumber).orElse("Unknown"));
                alarm.setEntryTime(System.currentTimeMillis() / 1000L);
                alarm.setExitTime(null);
                alarmRepository.save(alarm);
                System.out.println("✅ ENTRY: IMEI " + track.getImei() + " entered geofence " + geofence.getId());

            } else if (!isCurrentlyInside && hasActiveEntry) {
                // SALIDA: No está dentro pero tiene entrada activa
                Alarm activeAlarm = alarmRepository.findByImeiAndGeofenceIdAndExitTimeIsNull(track.getImei(), geofence.getId());

                if (activeAlarm != null) {
                    // Cerrar la alarma de entrada existente
                    System.out.println("✅ Cerrar la alarma de entrada existente " + track.getImei() + " entered geofence " + geofence.getId());

                    activeAlarm.setExitTime(System.currentTimeMillis() / 1000L);
                    activeAlarm.setAlarmType("ENTRY");
                    alarmRepository.save(activeAlarm);

                    // Crear un nuevo registro para la salida
                    Alarm exitAlarm = new Alarm();
                    exitAlarm.setImei(track.getImei());
                    exitAlarm.setGeofenceId(geofence.getId());
                    exitAlarm.setTrackTime(track.getGpstime());
                    exitAlarm.setAlarmType("EXIT");
                    exitAlarm.setDeviceName(deviceRepository.findById(Long.valueOf(track.getImei())).map(Device::getDeviceName).orElse("Unknown"));
                    exitAlarm.setPlateNumber(deviceRepository.findById(Long.valueOf(track.getImei())).map(Device::getPlateNumber).orElse("Unknown"));
                    exitAlarm.setLatitude(track.getLatitude());
                    exitAlarm.setLongitude(track.getLongitude());
                    exitAlarm.setEntryTime(activeAlarm.getEntryTime());
                    exitAlarm.setExitTime(System.currentTimeMillis() / 1000L);
                    alarmRepository.save(exitAlarm);

                    long duration = exitAlarm.getExitTime() - exitAlarm.getEntryTime();
                    System.out.println("🚪 EXIT: IMEI " + track.getImei() + " exited geofence " + geofence.getId() + " (Duration: " + duration + " seconds)");
                }
            }
            // No imprimimos mensajes para casos donde no hay cambio de estado
        }
    }

    @Override
    public List<Alarm> getAlarms() {
        return alarmRepository.findAll();
    }

    // Método adicional para obtener alarmas activas
    public List<Alarm> getActiveAlarms() {
        return alarmRepository.findByExitTimeIsNull();
    }

    // Método para forzar salida de todas las alarmas activas de un IMEI (útil para testing)
    public void forceExitAllActive(String imei) {
        List<Alarm> activeAlarms = alarmRepository.findByImeiAndExitTimeIsNull(imei);
        long currentTime = System.currentTimeMillis() / 1000L;

        for (Alarm alarm : activeAlarms) {
            alarm.setExitTime(currentTime);
            alarm.setAlarmType("ENTRY_EXIT");
            alarmRepository.save(alarm);
            System.out.println("🔧 Forced exit for alarm ID: " + alarm.getId());
        }
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

    public List<Alarm> findAlarmsByFilters(String imei, Long startTimestamp, Long endTimestamp) {
        if (imei != null && !imei.isEmpty() && startTimestamp != null && endTimestamp != null) {
            return alarmRepository.findByImeiAndTrackTimeBetween(imei, startTimestamp, endTimestamp);
        } else if (imei != null && !imei.isEmpty()) {
            return alarmRepository.findByImei(imei);
        } else if (startTimestamp != null && endTimestamp != null) {
            return alarmRepository.findByTrackTimeBetween(startTimestamp, endTimestamp);
        } else {
            return alarmRepository.findAll();
        }
    }
}