package com.idat.pe.controller;

import com.idat.pe.dto.response.CountryResponse;
import com.idat.pe.dto.response.FlightResponse;
import com.idat.pe.dto.response.PageableResponse;
import com.idat.pe.dto.response.RestResponse;
import com.idat.pe.service.CountrysService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.idat.pe.utils.constants.AppConstants.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/countrys")
@CrossOrigin("*")
public class CountryController {

    private final CountrysService countrysService;

    @GetMapping(value = "/params2", produces = MediaType.APPLICATION_JSON_VALUE)

    public RestResponse<PageableResponse<FlightResponse>> getFlightss(
            @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam("fechaIda") String fechaIda,
            @RequestParam("fechaVuelta") String fechaVuelta) {
        return new RestResponse<>(SUCCESS,
                String.valueOf(HttpStatus.OK),
                "FLIGHTS SUCCESSFULLY READED",
                countrysService.getFlights2(pageNumber, pageSize, fechaIda, fechaVuelta));
    }

    @GetMapping(value = "/params", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<PageableResponse<FlightResponse>> getFlights(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam("origen") String origen,
            @RequestParam("destino") String destino,
            @RequestParam("fechaIda") String fechaIda,
            @RequestParam("fechaVuelta") String fechaVuelta) {
        return new RestResponse<>(SUCCESS,
                String.valueOf(HttpStatus.OK),
                "FLIGHTS SUCCESSFULLY READED",
                countrysService.getFlights(pageNumber, pageSize, origen, destino, fechaIda, fechaVuelta));
    }

    @GetMapping(value = "/locations", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<List<CountryResponse>> getAllLocations() {

        return new RestResponse<>(SUCCESS,
                String.valueOf(HttpStatus.OK),
                "LOCATIONS SUCCESSFULLY READED",
                countrysService.getAllLocations());
    }

    @GetMapping(value = "/origins", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<List<CountryResponse>> getAllOrigins() {
        return new RestResponse<>(SUCCESS,
                String.valueOf(HttpStatus.OK),
                "ORIGINS SUCCESSFULLY READED",
                countrysService.getAllOrigins());
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<List<CountryResponse>> searchOriginsAndDestinations(@RequestParam("term") String searchTerm) {
        return new RestResponse<>(SUCCESS,
                String.valueOf(HttpStatus.OK),
                "SEARCH SUCCESSFULLY READED",
                countrysService.searchOriginsAndDestinations(searchTerm));
    }

}

