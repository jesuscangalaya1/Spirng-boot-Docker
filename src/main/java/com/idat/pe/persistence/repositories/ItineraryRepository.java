package com.idat.pe.persistence.repositories;

import com.idat.pe.persistence.entities.ItineraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


public interface ItineraryRepository extends JpaRepository<ItineraryEntity, Long> {
    Page<ItineraryEntity> findAllByDeletedFalse(Pageable pageable);

}
