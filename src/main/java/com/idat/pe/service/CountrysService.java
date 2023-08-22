package com.idat.pe.service;

import com.idat.pe.dto.response.CountryResponse;
import com.idat.pe.dto.response.FlightResponse;
import com.idat.pe.dto.response.PageableResponse;

import java.util.List;

public interface CountrysService {

    PageableResponse<FlightResponse> getFlights2(Integer pageNumber, Integer pageSize, String startDate, String endDate);

    PageableResponse<FlightResponse> getFlights(Integer pageNumber, Integer pageSize, String origen,
                                                String destino, String fechaIda, String fechaVuelta);

    List<CountryResponse> getAllOrigins();

    List<CountryResponse> getAllLocations();

    List<CountryResponse> searchOriginsAndDestinations(String searchTerm);


}
