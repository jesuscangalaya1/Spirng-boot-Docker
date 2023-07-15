package edu.idat.pe.project.service;

import edu.idat.pe.project.dto.response.CountryResponse;
import edu.idat.pe.project.dto.response.FlightResponse;
import edu.idat.pe.project.dto.response.PageableResponse;
import edu.idat.pe.project.persistence.entities.FlightEntity;

import java.util.List;

public interface CountrysService {

    PageableResponse<FlightResponse> getFlights2(Integer pageNumber, Integer pageSize,String startDate, String endDate);

    PageableResponse<FlightResponse> getFlights(Integer pageNumber, Integer pageSize, String origen,
                                                String destino, String fechaIda, String fechaVuelta);

    List<CountryResponse> getAllOrigins();

    List<CountryResponse> getAllLocations();

    List<CountryResponse> searchOriginsAndDestinations(String searchTerm);


}
