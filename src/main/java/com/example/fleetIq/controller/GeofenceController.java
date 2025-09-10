package com.example.fleetIq.controller;

import com.example.fleetIq.model.Geofence;
import com.example.fleetIq.service.GeofenceService;
import com.example.fleetIq.repository.GeofenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GeofenceController {

    @Autowired
    private GeofenceService geofenceService;

    @Autowired
    private GeofenceRepository geofenceRepository;

    @PostMapping("/geofences")
    public ResponseEntity<String> createGeofence(@RequestBody Geofence geofence) {
        try {
            geofenceService.createGeofence(geofence);
            return ResponseEntity.ok("Geofence created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating geofence: " + e.getMessage());
        }
    }

    @GetMapping("/geofences")
    public List<Geofence> getGeofences() {
        return geofenceRepository.findAll();
    }
}