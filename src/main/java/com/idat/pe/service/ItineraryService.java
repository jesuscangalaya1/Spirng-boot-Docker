package com.idat.pe.service;

import com.idat.pe.dto.response.ItineraryResponse;
import com.idat.pe.dto.response.PageableResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItineraryService {

    void save(MultipartFile file);

    PageableResponse<ItineraryResponse> pageableItineraries(int numeroDePagina, int medidaDePagina,
                                                            String ordenarPor, String sortDir);


    List<ItineraryResponse> listItineraries();
}