package com.idat.pe.service.impl;

import com.idat.pe.dto.response.*;
import com.idat.pe.exceptions.BusinessException;
import com.idat.pe.persistence.entities.FlightEntity;
import com.idat.pe.persistence.entities.ItineraryEntity;
import com.idat.pe.persistence.entities.LocationEntity;
import com.idat.pe.persistence.entities.OriginEntity;
import com.idat.pe.persistence.repositories.CountryRepository;
import com.idat.pe.persistence.repositories.CountrysRepository;
import com.idat.pe.service.CountrysService;
import com.idat.pe.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CountrysServiceImpl implements CountrysService {

    private final CountrysRepository countrysRepository;
    private final CountryRepository repository;


    @Cacheable(value = {"itinerario", "vuelo", "origen", "destino"})
    @Transactional(readOnly = true)
    @Override
    public PageableResponse<FlightResponse> getFlights2(Integer pageNumber, Integer pageSize, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<FlightEntity> flightEntities = repository.findFlightsByDepartureDateRange(pageable, startDate, endDate);

        List<FlightResponse> flightResponses = new ArrayList<>();

        for (FlightEntity flightEntity : flightEntities) {
            FlightResponse flightResponse = mapEntityToResponse(flightEntity);

            flightResponses.add(flightResponse);
        }

        if (flightResponses.isEmpty()) {
            throw new BusinessException("P-204", HttpStatus.NO_CONTENT, "Lista Vaciá de vuelos");
        }

        return PageableResponse.<FlightResponse>builder()
                .content(flightResponses)
                .pageNumber(flightEntities.getNumber())
                .pageSize(flightEntities.getSize())
                .totalPages(flightEntities.getTotalPages())
                .totalElements(flightEntities.getTotalElements())
                .last(flightEntities.isLast())
                .build();
    }

    @Cacheable(value = {"itinerario", "vuelo", "origen", "destino"})
    @Transactional(readOnly = true)
    @Override
    public PageableResponse<FlightResponse> getFlights(Integer pageNumber, Integer pageSize, String origen, String destino, String fechaIda, String fechaVuelta) {
        PaginationUtils.validatePaginationParameters(pageNumber, pageSize);

        // Obtener el total de elementos
        long totalElements = countrysRepository.getCountOfFlights(origen,  destino,  fechaIda,  fechaVuelta);

        // Calcular información de paginación
        int offset = PaginationUtils.calculateOffset(pageNumber, pageSize);

        // Obtener los productos paginados
        List<FlightResponse> flights = countrysRepository.findByOriginAndLocation(offset, pageSize, origen,  destino,  fechaIda,  fechaVuelta);

        // Validar si la lista de productos está vacía
        PaginationUtils.validatePageContent(flights);

        // Validar si el número de página está dentro del rango válido
        int totalPages = PaginationUtils.calculateTotalPages(totalElements, pageSize);
        PaginationUtils.validatePageNumber(pageNumber, totalPages);

        // Validar el tamaño de página máximo
        int maxPageSize = 100; // Establecer el tamaño de página máximo permitido
        PaginationUtils.validatePageSize(pageSize, maxPageSize);

        // Configurar los resultados en la respuesta
        return PageableResponse.<FlightResponse>builder()
                .content(flights)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .last(pageNumber == totalPages)
                .build();

    }


    @Cacheable(value = "origen")
    @Transactional(readOnly = true)
    @Override
    public List<CountryResponse> getAllOrigins() {
        return countrysRepository.getAllOrigins();
    }

    @Cacheable(value = "destino")
    @Transactional(readOnly = true)
    @Override
    public List<CountryResponse> getAllLocations() {
        return countrysRepository.getAllLocations();
    }

    @Cacheable(value ={"origen", "destino"})
    @Transactional(readOnly = true)
    @Override
    public List<CountryResponse> searchOriginsAndDestinations(String searchTerm) {
        return countrysRepository.searchOriginsAndDestinations(searchTerm);
    }



















    private FlightResponse mapEntityToResponse(FlightEntity flightEntity) {
        FlightResponse flightResponse = new FlightResponse();
        flightResponse.setId(flightEntity.getId());
        flightResponse.setCapacity(flightEntity.getCapacity());
        flightResponse.setDuration(flightEntity.getDuration());
        flightResponse.setPrice(flightEntity.getPrice());
        flightResponse.setImage(flightEntity.getImage());
        flightResponse.setDepartureTime(flightEntity.getDepartureTime());

        ItineraryEntity itineraryEntity = flightEntity.getItinerary();
        ItineraryResponse itineraryResponse = new ItineraryResponse();
        itineraryResponse.setId(itineraryEntity.getId());
        itineraryResponse.setDepartureDate(itineraryEntity.getDepartureDate());
        itineraryResponse.setArrivalDate(itineraryEntity.getArrivalDate());
        itineraryResponse.setHour(itineraryEntity.getHour());

        OriginEntity originEntity = itineraryEntity.getOrigin();
        OriginResponse originResponse = new OriginResponse();
        originResponse.setId(originEntity.getId());
        originResponse.setCity(originEntity.getCity());
        originResponse.setCountry(originEntity.getCountry());
        originResponse.setAirport(originEntity.getAirport());

        LocationEntity locationEntity = itineraryEntity.getLocation();
        LocationResponse locationResponse = new LocationResponse();
        locationResponse.setId(locationEntity.getId());
        locationResponse.setCity(locationEntity.getCity());
        locationResponse.setCountry(locationEntity.getCountry());
        locationResponse.setAirport(locationEntity.getAirport());

        itineraryResponse.setOrigin(originResponse);
        itineraryResponse.setLocation(locationResponse);
        flightResponse.setItinerary(itineraryResponse);

        return flightResponse;
    }







}
