package com.example.fleetIq.service;

import com.example.fleetIq.model.Device;
import com.example.fleetIq.model.Track;

import java.util.List;

public interface TrackService {
    void fetchAndSaveTracks(Long beginTime, Long endTime) throws Exception;
    List<Track> getTracksByImei(String imei, Long beginTime, Long endTime);
    List<Device> getAllDevices();
}