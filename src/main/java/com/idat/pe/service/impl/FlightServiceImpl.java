package com.idat.pe.service.impl;

import com.idat.pe.dto.response.FlightResponse;
import com.idat.pe.dto.response.PageableResponse;
import com.idat.pe.exceptions.BusinessException;
import com.idat.pe.persistence.repositories.FlightRepository;
import com.idat.pe.reports.exports.ResourceExport;
import com.idat.pe.service.FlightService;
import com.idat.pe.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.idat.pe.utils.ExportDataUtils.*;
import static com.idat.pe.utils.constants.AppConstants.*;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final CloudinaryService cloudinaryService;
    private final ResourceExport resourceExport;


    @Transactional
    @CacheEvict(value = "vuelo", allEntries = true)
    @Override
    public void deleteFlight(Long id) {
        flightRepository.deleteByIdFlight(id);
    }

    @Transactional
    @CacheEvict(value = "vuelo", allEntries = true)
    @Override
    public FlightResponse createFlight(int capacity, String duration, Double price,
                                       MultipartFile image, String departureTime, Long itineraryId) throws IOException {

        Map uploadResult = cloudinaryService.upload(image);
        String imageUrl = (String) uploadResult.get("url");


        // Llamamos al método createflight del repositorio para guardar el vuelo en la base de datos
        FlightResponse response = flightRepository.createflight(capacity, duration, price, imageUrl, departureTime, itineraryId);

        // Construir la instancia de FlightResponse con los detalles del vuelo
        // Utilizamos la respuesta del repositorio en lugar de la instancia response creada previamente
        FlightResponse flightResponse = new FlightResponse();
        flightResponse.setId(response.getId());
        flightResponse.setCapacity(response.getCapacity());
        flightResponse.setDuration(response.getDuration());
        flightResponse.setPrice(response.getPrice());
        flightResponse.setImage(imageUrl);
        // Establecer otros detalles del vuelo según sea necesario

        return flightResponse;

    }


    @Transactional
    @CacheEvict(value = "vuelo", allEntries = true)
    @Override
    public FlightResponse updateFlight(Long id, int capacity, String duration, Double price,
                                       MultipartFile image, String departureTime, Long itineraryId) {
        try {
            if (image.isEmpty()) {
                throw new BusinessException("Failed to store", HttpStatus.NOT_FOUND, "");
            }

            Map uploadResult = cloudinaryService.upload(image);
            String imageUrl = (String) uploadResult.get("url");


            // Llamamos al método updateFlight del repositorio para actualizar el vuelo en la base de datos
            FlightResponse response = flightRepository.updateFlight(id, capacity, duration, price, imageUrl, departureTime, itineraryId);

            // Construir la instancia de FlightResponse con los detalles del vuelo
            // Utilizamos la respuesta del repositorio en lugar de la instancia response creada previamente
            FlightResponse flightResponse = new FlightResponse();
            flightResponse.setId(response.getId());
            flightResponse.setCapacity(response.getCapacity());
            flightResponse.setDuration(response.getDuration());
            flightResponse.setPrice(response.getPrice());
            flightResponse.setImage(imageUrl);
            // Establecer otros detalles del vuelo según sea necesario

            return flightResponse;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }


    @Cacheable(value = "vuelo")
    @Transactional(readOnly = true)
    @Override
    public PageableResponse<FlightResponse> listFlights(Integer pageNumber, Integer pageSize, Integer priceMin,
                                                        Integer priceMax, String departureDate) {
        // Validar pageNumber y pageSize utilizando PaginationUtils
        PaginationUtils.validatePaginationParameters(pageNumber, pageSize);

        // Obtener el total de elementos
        long totalElements = flightRepository.getCountOfFlights(priceMin, priceMax);

        // Calcular información de paginación
        int offset = PaginationUtils.calculateOffset(pageNumber, pageSize);

        // Obtener los productos paginados
        List<FlightResponse> flights = flightRepository.listFlights(offset, pageSize, priceMin, priceMax, departureDate);

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

    @Cacheable(value = "vuelo")
    @Transactional(readOnly = true)
    @Override
    public FlightResponse getByIdFligth(Long id) {
        return flightRepository.getByIdFlight(id);
    }


    //Implementation Patron de diseño(SOLID)
/*   Principio de responsabilidad única (SRP): El método exportDataExcel se encarga únicamente de orquestar la exportación de los datos,
     mientras que la lógica relacionada con la validación de formato,
     creación de columnas y valores, se ha separado en métodos auxiliares con responsabilidades más específicas.
    */
    @Transactional
    @Override
    public File exportDataExcel(List<FlightResponse> flightResponses, String formato) throws Exception {
        validateFormato(formato);

        List<String> sheets = Collections.singletonList(SHEET_FLIGHT);

        Map<String, List<String>> colsBySheet = createColumnsBySheetMap();
        Map<String, List<Map<String, String>>> valuesBySheet = createValuesBySheetMap(flightResponses);

        String reportName = REPORT_NAME_FLIGHT_PAGINABLE;
        if (formato.equals(FORMATO_EXCEL_ABREVIATURA)) {
            return resourceExport.generateExcel(sheets, colsBySheet, valuesBySheet, reportName);
        } else {
            return resourceExport.generatePdf(sheets, colsBySheet, valuesBySheet, reportName);
        }
    }

}
