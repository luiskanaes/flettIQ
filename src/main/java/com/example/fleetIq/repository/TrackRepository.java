package com.example.fleetIq.repository;

import com.example.fleetIq.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {

    // ⭐ MÉTODO PRINCIPAL: Obtener el track más reciente por cada IMEI
    @Query("SELECT t FROM Track t WHERE t.id IN " +
            "(SELECT MAX(t2.id) FROM Track t2 GROUP BY t2.imei)")
    List<Track> findLatestTracksByImei();

    // Alternativa usando gpstime si prefieres por timestamp en lugar de ID
    @Query("SELECT t FROM Track t WHERE t.gpstime IN " +
            "(SELECT MAX(t2.gpstime) FROM Track t2 GROUP BY t2.imei)")
    List<Track> findLatestTracksByImeiAndTime();

    // Método para obtener tracks activos de los últimos X minutos
    @Query("SELECT t FROM Track t WHERE t.gpstime > :timestamp")
    List<Track> findActiveTracksSince(@Param("timestamp") Long timestamp);

    // Método para obtener el track más reciente de un IMEI específico
    @Query("SELECT t FROM Track t WHERE t.imei = :imei ORDER BY t.id DESC LIMIT 1")
    Track findLatestTrackByImei(@Param("imei") String imei);

    // Método para obtener tracks por IMEI específico
    List<Track> findByImei(String imei);

    // Método para obtener tracks ordenados por fecha descendente
    @Query("SELECT t FROM Track t ORDER BY t.gpstime DESC")
    List<Track> findAllOrderByGpstimeDesc();

    @Query("SELECT t FROM Track t WHERE t.imei = :imei AND t.gpstime BETWEEN :beginTime AND :endTime ORDER BY t.gpstime ASC")
    List<Track> findByImeiAndTimeBetween(String imei, Long beginTime, Long endTime);

    @Query("SELECT t FROM Track t WHERE t.gpstime >= ?1 AND t.id IN (SELECT MAX(t2.id) FROM Track t2 GROUP BY t2.imei)")
    List<Track> findLatestTracksByImeiWithinLastMinutes(Long timestamp);
}