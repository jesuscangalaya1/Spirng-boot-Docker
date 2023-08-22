package com.idat.pe.persistence.repositories;

import com.idat.pe.persistence.entities.FlightEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CountryRepository extends JpaRepository<FlightEntity, Long> {

    @Query(value = "SELECT * FROM vuelo v " +
                   "JOIN itinerario i ON v.itinerario_id = i.id " +
                   "WHERE v.deleted = false " +
                   "AND STR_TO_DATE(i.fecha_ida, '%Y-%m-%d') " +
                   "BETWEEN STR_TO_DATE(:minDate, '%Y-%m-%d') " +
                   "AND STR_TO_DATE(:maxDate, '%Y-%m-%d') " +
                   "ORDER BY STR_TO_DATE(i.fecha_ida, '%Y-%m-%d') ASC",
            nativeQuery = true)
    Page<FlightEntity> findFlightsByDepartureDateRange(
            Pageable pageable,
            @Param("minDate") String minDate,
            @Param("maxDate") String maxDate);

}