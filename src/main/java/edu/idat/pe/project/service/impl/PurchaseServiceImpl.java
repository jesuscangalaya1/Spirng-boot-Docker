package edu.idat.pe.project.service.impl;

import edu.idat.pe.project.dto.request.PurchaseRequest;
import edu.idat.pe.project.dto.response.FlightResponse;
import edu.idat.pe.project.dto.response.PurchaseResponse;
import edu.idat.pe.project.exceptions.ResourceNotFoundException;
import edu.idat.pe.project.persistence.entities.FlightEntity;
import edu.idat.pe.project.persistence.entities.ItineraryEntity;
import edu.idat.pe.project.persistence.entities.PurchaseEntity;
import edu.idat.pe.project.persistence.repositories.FlightRepository;
import edu.idat.pe.project.persistence.repositories.PurchaseRepository;
import edu.idat.pe.project.security.entity.Usuario;
import edu.idat.pe.project.security.repository.UsuarioRepository;
import edu.idat.pe.project.security.service.UsuarioService;
import edu.idat.pe.project.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
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
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
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
    @Transactional
    public List<PurchaseResponse> getDeletedPurchases() {
        // Obtener el nombre de usuario del usuario autenticado desde el contexto de seguridad
 /*       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String nombreUsuario = authentication.getName();

        // Obtener las compras eliminadas asociadas al nombre de usuario desde el repositorio
        List<PurchaseEntity> purchases = purchaseRepository.findByUsuario_NombreUsuarioAndDeleted(nombreUsuario, true);
*/
        List<Object[]> results = purchaseRepository.findDeletedPurchases();

        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No hay compras eliminadas");
        }

        List<PurchaseResponse> purchaseResponses = new ArrayList<>();
        for (Object[] result : results) {
            PurchaseResponse purchaseResponse = new PurchaseResponse();
            purchaseResponse.setId((Long) result[0]);
            purchaseResponse.setAmount((int) result[1]);
            purchaseResponse.setPrice((Double) result[2]);
            purchaseResponse.setPurchaseDate((LocalDate) result[3]);
            purchaseResponse.setTotal((Double) result[4]);

            FlightResponse flightResponse = new FlightResponse();
            flightResponse.setId((Long) result[0]);
            flightResponse.setCapacity((int) result[5]);
            flightResponse.setDepartureTime((String) result[6]);
            flightResponse.setDuration((String) result[7]);
            flightResponse.setImage((String) result[8]);
            flightResponse.setPrice((Double) result[9]);
            // Otros atributos del vuelo que desees incluir en la respuesta

            purchaseResponse.setFlights(Collections.singletonList(flightResponse));
            purchaseResponses.add(purchaseResponse);
        }

        return purchaseResponses;
    }


    @Override
    @CacheEvict(value = "compras", allEntries = true)
    @Transactional
    public void restorePurchase(Long id) {
        purchaseRepository.restorePurchaseById(id);

    }

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

                // Agregar el producto a la lista de productos asociados a la compra
                purchase.getFlights().add(product);

                // Guardar la compra en la base de datos
                purchaseRepository.save(purchase);
            } else {
                throw new ResourceNotFoundException("El vuelo no existe");
            }
        }
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

        // Mapear las compras a PurchaseResponse
        List<PurchaseResponse> purchaseResponses = new ArrayList<>();
        for (PurchaseEntity purchaseEntity : purchases) {
            PurchaseResponse purchaseResponse = new PurchaseResponse();
            purchaseResponse.setId(purchaseEntity.getId());
            purchaseResponse.setAmount(purchaseEntity.getAmount());
            purchaseResponse.setPrice(purchaseEntity.getPrice());
            purchaseResponse.setTotal(purchaseEntity.getTotal());
            purchaseEntity.setPurchaseDate(LocalDate.now()); // Establecer la fecha de compra como la fecha actual


            List<FlightResponse> flightResponses = new ArrayList<>();
            for (FlightEntity flightEntity : purchaseEntity.getFlights()) {
                FlightResponse flightResponse = new FlightResponse();
                flightResponse.setId(flightEntity.getId());
                flightResponse.setCapacity(flightEntity.getCapacity());
                flightResponse.setDuration(flightEntity.getDuration());
                flightResponse.setPrice(flightEntity.getPrice());
                flightResponse.setImage(flightEntity.getImage());
                flightResponse.setDepartureTime(flightEntity.getDepartureTime());
                // Otros atributos del vuelo que desees incluir en la respuesta

                flightResponses.add(flightResponse);
            }

            purchaseResponse.setFlights(flightResponses);
            purchaseResponses.add(purchaseResponse);
        }
        return purchaseResponses;
    }

    @Transactional
    @Override
    public ResponseEntity<Resource> exportInvoice(Long id) {
        Optional<PurchaseEntity> optionalCompra = purchaseRepository.findById(id);
        if (optionalCompra.isPresent()) {
            try {
                PurchaseEntity compra = optionalCompra.get();
                ItineraryEntity itinerario = compra.getFlights().get(0).getItinerary(); // Obtener el itinerario desde el primer vuelo de la compra

                File reportFile = ResourceUtils.getFile("classpath:images/ReporteBoleto.jasper");
                final File imgLogo = ResourceUtils.getFile("classpath:images/logo.png");

                JasperReport report = (JasperReport) JRLoader.loadObject(reportFile);

                // Crear los parámetros del informe
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("nombre", compra.getUsuario().getNombreUsuario());
                parameters.put("fecha_Salida", itinerario.getArrivalDate());
                parameters.put("horaSalida", itinerario.getHour());
                parameters.put("aeropuerto", itinerario.getOrigin().getAirport());
                parameters.put("duracion", compra.getFlights().get(0).getDuration());
                parameters.put("origen", itinerario.getOrigin().getCity() + ", " + itinerario.getOrigin().getCountry());
                parameters.put("destino", itinerario.getLocation().getCity() + ", " + itinerario.getLocation().getCountry());
                parameters.put("logoEmpresa", new FileInputStream(imgLogo));
                parameters.put("imagenAlternativa", new FileInputStream(imgLogo));

                // Crear la lista de objetos para la tabla de datos
                List<Map<String, Object>> purchaseDataList = new ArrayList<>();
                Map<String, Object> purchaseData = new HashMap<>();
                purchaseData.put("nombre", compra.getUsuario().getNombreUsuario());
                purchaseData.put("fecha_salida", compra.getFlights().get(0).getItinerary().getArrivalDate());
                purchaseData.put("hora_salida", compra.getFlights().get(0).getItinerary().getHour());
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


}
