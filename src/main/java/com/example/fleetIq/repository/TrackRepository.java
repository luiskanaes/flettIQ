package com.example.fleetIq.repository;

import com.example.fleetIq.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    @Query("SELECT t FROM Track t WHERE t.imei = :imei AND t.gpstime BETWEEN :beginTime AND :endTime ORDER BY t.gpstime ASC")
    List<Track> findByImeiAndTimeBetween(String imei, Long beginTime, Long endTime);
}