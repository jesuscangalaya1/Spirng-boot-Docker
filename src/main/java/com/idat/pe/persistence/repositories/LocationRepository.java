package com.idat.pe.persistence.repositories;

import com.idat.pe.persistence.entities.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
}
