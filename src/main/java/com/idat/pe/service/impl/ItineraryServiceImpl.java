package com.idat.pe.service.impl;

import com.idat.pe.dto.request.ItineraryRequest;
import com.idat.pe.dto.response.ItineraryResponse;
import com.idat.pe.dto.response.LocationResponse;
import com.idat.pe.dto.response.OriginResponse;
import com.idat.pe.dto.response.PageableResponse;
import com.idat.pe.exceptions.BusinessException;
import com.idat.pe.persistence.entities.ItineraryEntity;
import com.idat.pe.persistence.entities.LocationEntity;
import com.idat.pe.persistence.entities.OriginEntity;
import com.idat.pe.persistence.repositories.ItineraryRepository;
import com.idat.pe.persistence.repositories.LocationRepository;
import com.idat.pe.persistence.repositories.OriginRepository;
import com.idat.pe.reports.imports.HelperImportExcel;
import com.idat.pe.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItineraryServiceImpl implements ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final LocationRepository locationRepository;
    private final OriginRepository originRepository;

    @Transactional
    @CacheEvict(value = "itinerario", allEntries = true)
    @Override
    public void save(MultipartFile file) {

        try {
            List<ItineraryRequest> itineraries = HelperImportExcel.convertExcelToListOfItinerary(file.getInputStream());

            List<ItineraryEntity> itineraryEntities = new ArrayList<>();

            for (ItineraryRequest itineraryRequest : itineraries) {
                ItineraryEntity itineraryEntity = new ItineraryEntity();
                itineraryEntity.setDepartureDate(itineraryRequest.getDepartureDate());
                itineraryEntity.setArrivalDate(itineraryRequest.getArrivalDate());
                itineraryEntity.setHour(itineraryRequest.getHour());

                // Obtener el objeto LocationEntity correspondiente al locationId
                Optional<LocationEntity> locationEntityOptional = locationRepository.findById(itineraryRequest.getLocationId());
                if (locationEntityOptional.isEmpty()) {
                    // Manejar la situación si no se encuentra la ubicación por el ID
                    throw new IllegalArgumentException("LocationEntity not found for id: " + itineraryRequest.getLocationId());
                }
                itineraryEntity.setLocation(locationEntityOptional.get());

                // Obtener el objeto OriginEntity correspondiente al originId
                Optional<OriginEntity> originEntityOptional = originRepository.findById(itineraryRequest.getOriginId());
                if (originEntityOptional.isEmpty()) {
                    // Manejar la situación si no se encuentra el origen por el ID
                    throw new IllegalArgumentException("OriginEntity not found for id: " + itineraryRequest.getOriginId());
                }
                itineraryEntity.setOrigin(originEntityOptional.get());

                itineraryEntities.add(itineraryEntity);
            }

            itineraryRepository.saveAll(itineraryEntities);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "itinerario")
    @Override
    public PageableResponse<ItineraryResponse> pageableItineraries(int numeroDePagina, int medidaDePagina, String ordenarPor, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(ordenarPor).ascending()
                : Sort.by(ordenarPor).descending();
        Pageable pageable = PageRequest.of(numeroDePagina, medidaDePagina, sort);

        Page<ItineraryEntity> products = itineraryRepository.findAllByDeletedFalse(pageable);

        List<ItineraryEntity> listProducts = products.getContent();
        List<ItineraryResponse> contenido = listProducts.stream()
                .map(itinerary -> {
                    ItineraryResponse response = new ItineraryResponse();
                    response.setId(itinerary.getId());
                    response.setDepartureDate(itinerary.getDepartureDate().toString());
                    response.setArrivalDate(itinerary.getArrivalDate().toString());
                    response.setHour(itinerary.getHour());
                    response.setOrigin(mapOrigin(itinerary.getOrigin())); // Mapea el origen a OriginResponse
                    response.setLocation(mapLocation(itinerary.getLocation())); // Mapea la ubicación a LocationResponse
                    return response;
                })
                .toList();

        if (contenido.isEmpty()) {
            throw new BusinessException("P-204", HttpStatus.NO_CONTENT, "Lista Vaciá de Productos");
        }
        return PageableResponse.<ItineraryResponse>builder()
                .content(contenido)
                .pageNumber(products.getNumber())
                .pageSize(products.getSize())
                .totalPages(products.getTotalPages())
                .totalElements(products.getTotalElements())
                .last(products.isLast())
                .build();
    }




















    @Override
    public List<ItineraryResponse> listItineraries() {
        //List<ItineraryEntity> itineraries = itineraryRepository.findAllByDeletedFalse(); // Ejemplo: obtén los itinerarios desde el repositorio
/*
        List<ItineraryResponse> itineraryResponses = itineraries.stream()
                .map(itinerary -> {
                    ItineraryResponse response = new ItineraryResponse();
                    response.setId(itinerary.getId());
                    response.setDepartureDate(itinerary.getDepartureDate().toString());
                    response.setArrivalDate(itinerary.getArrivalDate().toString());
                    response.setHour(itinerary.getHour());
                    response.setOrigin(mapOrigin(itinerary.getOrigin())); // Mapea el origen a OriginResponse
                    response.setLocation(mapLocation(itinerary.getLocation())); // Mapea la ubicación a LocationResponse
                    return response;
                })
                .collect(Collectors.toList());*/

        return null;
    }

    private OriginResponse mapOrigin(OriginEntity origin) {
        OriginResponse originResponse = new OriginResponse();
        originResponse.setId(origin.getId());
        originResponse.setCity(origin.getCity());
        originResponse.setCountry(origin.getCountry());
        originResponse.setAirport(origin.getAirport());
        return originResponse;
    }

    private LocationResponse mapLocation(LocationEntity location) {
        LocationResponse locationResponse = new LocationResponse();
        locationResponse.setId(location.getId());
        locationResponse.setCity(location.getCity());
        locationResponse.setCountry(location.getCountry());
        locationResponse.setAirport(location.getAirport());
        return locationResponse;
    }






}

