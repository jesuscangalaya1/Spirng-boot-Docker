package com.idat.pe.service.impl;

import com.idat.pe.dto.request.PurchaseRequest;
import com.idat.pe.dto.response.FlightResponse;
import com.idat.pe.dto.response.PurchaseResponse;
import com.idat.pe.exceptions.ResourceNotFoundException;
import com.idat.pe.persistence.entities.FlightEntity;
import com.idat.pe.persistence.entities.ItineraryEntity;
import com.idat.pe.persistence.entities.PurchaseEntity;
import com.idat.pe.persistence.repositories.FlightRepository;
import com.idat.pe.persistence.repositories.PurchaseRepository;
import com.idat.pe.security.entity.Usuario;
import com.idat.pe.security.repository.UsuarioRepository;
import com.idat.pe.security.service.UsuarioService;
import com.idat.pe.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final FlightRepository flightRepository;

    @Override
    @CacheEvict(value = "compras", allEntries = true)
    @Transactional
    public void deletePurchase(Long id) {
        PurchaseEntity purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La compra no existe. ID: " + id));

        purchaseRepository.delete(purchase);
    }



    @Transactional
    @CacheEvict(value = "compras", allEntries = true)
    @Override
    public void purchaseFlight(PurchaseRequest purchaseRequest) {
        // Crear una instancia de PurchaseEntity utilizando los datos de PurchaseRequest
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setAmount(purchaseRequest.getAmount());
        purchase.setPrice(purchaseRequest.getPrice());
        // Calcular el total multiplicando la cantidad por el precio
        double total = purchaseRequest.getAmount() * purchaseRequest.getPrice();
        purchase.setTotal(total);
        purchase.setPurchaseDate(LocalDate.now());

        // Obtener el nombre de usuario del usuario autenticado desde el contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String nombreUsuario = authentication.getName();

        // Obtener el objeto Usuario utilizando el nombre de usuario
        Optional<Usuario> optionalUsuario = usuarioService.getByNombreUsuario(nombreUsuario);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            // Establecer el usuario asociado a la compra utilizando el objeto Usuario completo
            purchase.setUsuario(usuario);
            // Obtener el producto asociado a la compra utilizando el ID proporcionado
            FlightResponse optionalProduct = flightRepository.getByIdFlight(purchaseRequest.getFlightId());
            if (optionalProduct != null) {
                FlightEntity product = mapFlightResponseToEntity(optionalProduct);
                //Agregar el producto a la lista de productos asociados a la compra
                purchase.getFlights().add(product);
                // Guardar la compra en la base de datos
                purchaseRepository.save(purchase);
            } else {
                throw new ResourceNotFoundException("El vuelo no existe");
            }
        }
    }

    @Cacheable(value = "compras")
    @Transactional(readOnly = true)
    @Override
    public List<PurchaseResponse> getCustomerPurchases() {
        // Obtener el nombre de usuario del usuario autenticado desde el contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String nombreUsuario = authentication.getName();

        // Obtener las compras asociadas al nombre de usuario desde el repositorio
        List<PurchaseEntity> purchases = purchaseRepository.findByUsuario_NombreUsuario(nombreUsuario);

        if (purchases.isEmpty()) {
            throw new ResourceNotFoundException("El usuario no tiene compras registradas");
        }

        return purchases.stream()
                .map(this::mapPurchaseToResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public ResponseEntity<Resource> exportInvoice(Long id) {
        Optional<PurchaseEntity> optionalCompra = purchaseRepository.findById(id);
        if (optionalCompra.isPresent()) {
            try {
                PurchaseEntity compra = optionalCompra.get();
                ItineraryEntity itinerario = compra.getFlights().get(0).getItinerary(); // Obtener el itinerario desde el primer vuelo de la compra

                // Cargar el archivo JRXML y compilarlo en un objeto JasperReport
                InputStream reportStream = getClass().getResourceAsStream("/ReporteBoleto_4.jrxml");
                JasperReport report = JasperCompileManager.compileReport(reportStream);

                // Cargar la imagen del logo
                InputStream imgLogoStream = getClass().getResourceAsStream("/images/logo.png");

                // Crear los parámetros del informe
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("nombre", compra.getUsuario().getNombreUsuario());
                parameters.put("fecha_Salida", itinerario.getArrivalDate());
                parameters.put("horaSalida", itinerario.getHour());
                parameters.put("aeropuerto", itinerario.getOrigin().getAirport());
                parameters.put("duracion", compra.getFlights().get(0).getDuration());
                parameters.put("origen", itinerario.getOrigin().getCity() + ", " + itinerario.getOrigin().getCountry());
                parameters.put("destino", itinerario.getLocation().getCity() + ", " + itinerario.getLocation().getCountry());
                parameters.put("logoEmpresa", imgLogoStream);
                parameters.put("imagenAlternativa", imgLogoStream);

                // Crear la lista de objetos para la tabla de datos
                List<Map<String, Object>> purchaseDataList = new ArrayList<>();
                Map<String, Object> purchaseData = new HashMap<>();
                purchaseData.put("nombre", compra.getUsuario().getNombreUsuario());
                purchaseData.put("fecha_salida", compra.getFlights().get(0).getItinerary().getArrivalDate());
                purchaseData.put("hora_salida", compra.getFlights().get(0).getDepartureTime());
                purchaseData.put("aeropuerto", compra.getFlights().get(0).getItinerary().getOrigin().getAirport());
                purchaseData.put("duracion", compra.getFlights().get(0).getDuration());
                purchaseData.put("origen", compra.getFlights().get(0).getItinerary().getOrigin().getCity() + ", " + compra.getFlights().get(0).getItinerary().getOrigin().getCountry());
                purchaseData.put("destino", compra.getFlights().get(0).getItinerary().getLocation().getCity() + ", " + compra.getFlights().get(0).getItinerary().getLocation().getCountry());
                // Asigna los demás valores a purchaseData según las columnas del informe
                purchaseDataList.add(purchaseData);

                // Agregar la lista de datos a los parámetros del informe
                parameters.put("ds", new JRBeanArrayDataSource(purchaseDataList.toArray()));

                // Llenar el informe con los datos y los parámetros
                JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());

                // Exportar el informe a PDF
                byte[] reportBytes = JasperExportManager.exportReportToPdf(jasperPrint);

                // Generar el nombre de archivo único basado en el ID de la compra y la fecha actual
                String currentDate = new SimpleDateFormat("ddMMyyyy").format(new Date());
                String fileName = "InvoicePDF_" + compra.getId() + "_generateDate_" + currentDate + ".pdf";

                // Configurar los encabezados de respuesta
                ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                        .filename(fileName)
                        .build();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentDisposition(contentDisposition);

                log.info("Informe de factura exportado exitosamente. Compra ID: {}", compra.getId());

                // Devolver la respuesta con el archivo PDF generado
                return ResponseEntity.ok()
                        .contentLength(reportBytes.length)
                        .contentType(MediaType.APPLICATION_PDF)
                        .headers(headers)
                        .body(new ByteArrayResource(reportBytes));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error al exportar el informe de factura. Compra ID: {}", id, e);
            }
        } else {
            log.warn("No se encontró la compra con ID: {}", id);
            return ResponseEntity.noContent().build(); // No se encontró la compra
        }
        return null;
    }





























    private PurchaseResponse mapPurchaseToResponse(PurchaseEntity purchaseEntity) {
        purchaseEntity.setPurchaseDate(LocalDate.now()); // Establecer la fecha de compra como la fecha actual

        List<FlightResponse> flightResponses = purchaseEntity.getFlights().stream()
                .map(this::mapFlightToResponse)
                .collect(Collectors.toList());

        PurchaseResponse purchaseResponse = new PurchaseResponse();
        purchaseResponse.setId(purchaseEntity.getId());
        purchaseResponse.setAmount(purchaseEntity.getAmount());
        purchaseResponse.setPrice(purchaseEntity.getPrice());
        purchaseResponse.setTotal(purchaseEntity.getTotal());
        purchaseResponse.setFlights(flightResponses);

        return purchaseResponse;
    }

    private FlightResponse mapFlightToResponse(FlightEntity flightEntity) {
        FlightResponse flightResponse = new FlightResponse();
        flightResponse.setId(flightEntity.getId());
        flightResponse.setCapacity(flightEntity.getCapacity());
        flightResponse.setDuration(flightEntity.getDuration());
        flightResponse.setPrice(flightEntity.getPrice());
        flightResponse.setImage(flightEntity.getImage());
        flightResponse.setDepartureTime(flightEntity.getDepartureTime());
        // Otros atributos del vuelo que desees incluir en la respuesta

        return flightResponse;
    }







    private FlightEntity mapFlightResponseToEntity(FlightResponse flightResponse) {
        FlightEntity flightEntity = new FlightEntity();
        flightEntity.setId(flightResponse.getId());
        flightEntity.setCapacity(flightResponse.getCapacity());
        flightEntity.setDuration(flightResponse.getDuration());
        flightEntity.setPrice(flightResponse.getPrice());
        flightEntity.setImage(flightResponse.getImage());
        flightEntity.setDepartureTime(flightResponse.getDepartureTime());
        // Mapear otros atributos según corresponda

        return flightEntity;
    }
}

