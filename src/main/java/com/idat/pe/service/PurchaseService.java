package com.idat.pe.service;

import com.idat.pe.dto.request.PurchaseRequest;
import com.idat.pe.dto.response.FlightResponse;
import com.idat.pe.dto.response.PurchaseResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PurchaseService {


    void deletePurchase(Long id);

    void purchaseFlight(PurchaseRequest purchaseRequest);

    List<PurchaseResponse> getCustomerPurchases();

    ResponseEntity<Resource> exportInvoice(Long id);

}
