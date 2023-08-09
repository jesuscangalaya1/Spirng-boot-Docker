package edu.idat.pe.project.service;

import edu.idat.pe.project.dto.request.PurchaseRequest;
import edu.idat.pe.project.dto.response.FlightResponse;
import edu.idat.pe.project.dto.response.PurchaseResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PurchaseService {

    void designBoleto(FlightResponse flightResponse);

    List<PurchaseResponse> getDeletedPurchases();

    void restorePurchase(Long id);

    void deletePurchase(Long id);

    void purchaseFlight(PurchaseRequest purchaseRequest);

    List<PurchaseResponse> getCustomerPurchases();

    ResponseEntity<Resource> exportInvoice(Long id);

}
