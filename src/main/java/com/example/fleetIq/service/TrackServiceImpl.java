package com.example.fleetIq.service;

import com.example.fleetIq.model.Device;
import com.example.fleetIq.model.Track;
import com.example.fleetIq.repository.DeviceRepository;
import com.example.fleetIq.repository.TrackRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Service
public class TrackServiceImpl implements TrackService {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    private String getAccessToken() throws Exception {
        long time = Instant.now().getEpochSecond();
        String md5Password = calculateMD5("expert2023"); // Asegúrate de que esta contraseña sea correcta
        String signatureInput = md5Password + time;
        String signature = calculateMD5(signatureInput);

        URL url = new URL("https://api.protrack365.com/api/authorization?time=" + time + "&account=expertsac&signature=" + signature);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            JSONObject json = new JSONObject(response.toString());
            if (json.getInt("code") == 0) {
                return json.getJSONObject("record").getString("access_token");
            } else {
                throw new Exception("Error de API al obtener token: " + json.getString("message"));
            }
        } else {
            throw new Exception("Error HTTP al obtener token: " + conn.getResponseCode());
        }
    }

    private String calculateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Fallo en el cálculo de MD5", e);
        }
    }

    @Override
    public void fetchAndSaveTracks(Long beginTime, Long endTime) throws Exception {
        String accessToken = getAccessToken();
        List<Device> devices = deviceRepository.findAll();
        if (devices.isEmpty()) {
            throw new Exception("No se encontraron dispositivos registrados.");
        }

        // Recopilar todos los IMEIs en una lista
        StringBuilder imeis = new StringBuilder();
        for (Device device : devices) {
            if (imeis.length() > 0) imeis.append(",");
            imeis.append(device.getImei());
        }
        System.out.println("🚪Se termina proceso automatico de extraccion GPS Tracks desde api protrack365");

        fetchAndSaveForImeis(imeis.toString(), beginTime, endTime, accessToken);
    }


    @Scheduled(fixedRate = 60000) // Ejecuta cada 1 minuto (60,000 milisegundos)
    public void scheduleFetchAndSaveTracks() throws Exception {
        Long beginTime = Instant.now().getEpochSecond() - 24 * 60 * 60; // Últimas 24 horas
        Long endTime = Instant.now().getEpochSecond(); // Hasta ahora

        // Obtener la fecha y hora actual
        LocalDateTime fechaHoraActual = LocalDateTime.now();

        // Formatear la fecha y hora
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
        String fechaHoraFormateada = fechaHoraActual.format(formato);


        System.out.println("✅ Se inicia proceso automatico de extraccion GPS Tracks desde api protrack365 ::: " + fechaHoraFormateada);
        fetchAndSaveTracks(beginTime, endTime);
    }

    @Override
    public List<Track> getTracksByImei(String imei, Long beginTime, Long endTime) {
        if (beginTime == null) {
            beginTime = 0L; // Desde el inicio si no se especifica
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis() / 1000; // Hasta ahora
        }
        return trackRepository.findByImeiAndTimeBetween(imei, beginTime, endTime);
    }

    @Override
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    private void fetchAndSaveForImeis(String imeis, Long beginTime, Long endTime, String accessToken) throws Exception {
        int page = 1;
        int pagesize = 500; // Máximo permitido por la API
        boolean hasMore = true;

        while (hasMore) {
            URL url = new URL("https://api.protrack365.com/api/track?access_token=" + accessToken + "&imeis=" + imeis + "&begin_time=" + beginTime + "&end_time=" + endTime + "&page=" + page + "&pagesize=" + pagesize);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                JSONObject json = new JSONObject(response.toString());
                if (json.getInt("code") == 0) {
                    JSONArray records = json.getJSONArray("record");
                    if (records.length() < pagesize) {
                        hasMore = false;
                    }
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject record = records.getJSONObject(i);
                        Track track = new Track();
                        track.setImei(record.getString("imei"));
                        track.setGpstime(record.getLong("gpstime"));
                        track.setHearttime(record.optLong("hearttime", 0L));
                        track.setSystemtime(record.optLong("systemtime", 0L));
                        track.setServertime(record.optLong("servertime", 0L));
                        track.setLatitude(record.getDouble("latitude"));
                        track.setLongitude(record.getDouble("longitude"));
                        track.setSpeed(record.optDouble("speed", 0.0));
                        track.setCourse(record.optDouble("course", 0.0));
                        track.setAcctime(record.optLong("acctime", 0L));
                        track.setAccstatus(record.optBoolean("accstatus", false));
                        track.setDoorstatus(record.optInt("doorstatus", 0));
                        track.setChargestatus(record.optInt("chargestatus", 0));
                        track.setOilpowerstatus(record.optInt("oilpowerstatus", 0));
                        track.setDefencestatus(record.optInt("defencestatus", 0));
                        track.setDatastatus(record.optInt("datastatus", 0));
                        track.setBattery(record.optDouble("battery", 0.0));
                        track.setMileage(record.optLong("mileage", 0L));
                        track.setTodaymileage(record.optLong("todaymileage", 0L));
                        track.setExternalpower(record.optString("externalpower", ""));
                        track.setFuel(record.optString("fuel", ""));
                        track.setFueltime(record.optLong("fueltime", 0L));
                        track.setTemperature(record.optString("temperature", "[]")); // Almacenamos como texto
                        track.setTemperaturetime(record.optLong("temperaturetime", 0L));
                        // Guardar solo si no existe
                        if (trackRepository.findByImeiAndTimeBetween(track.getImei(), track.getGpstime(), track.getGpstime()).isEmpty()) {
                            trackRepository.save(track);
                        }
                    }
                    page++;
                } else {
                    throw new Exception("Error de API: " + json.getString("message"));
                }
            } else {
                throw new Exception("Error HTTP: " + conn.getResponseCode());
            }
        }
    }
}