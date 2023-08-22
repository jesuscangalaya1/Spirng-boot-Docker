package com.idat.pe.controller;

import com.idat.pe.dto.request.PurchaseRequest;
import com.idat.pe.dto.response.PurchaseResponse;
import com.idat.pe.dto.response.RestResponse;
import com.idat.pe.security.dto.Mensaje;
import com.idat.pe.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.idat.pe.utils.constants.AppConstants.SUCCESS;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases")
@CrossOrigin("*")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    public ResponseEntity<List<PurchaseResponse>> listCustomerPruchases() {
        return new ResponseEntity<>(purchaseService.getCustomerPurchases(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Mensaje> purchaseFlight(@RequestBody PurchaseRequest purchaseRequest) {
        purchaseService.purchaseFlight(purchaseRequest);
        return new ResponseEntity<>(new Mensaje("PURCHASE SUCCESSFULLY"), HttpStatus.CREATED);
    }

    @GetMapping("/exportInvoice/{id}")
    public ResponseEntity<Resource> exportInvoice(@PathVariable Long id) {
        return this.purchaseService.exportInvoice(id);
    }

    @DeleteMapping("/{id}")
    public RestResponse<String> deletePurchase(@PathVariable Long id) {
        this.purchaseService.deletePurchase(id);
        return new RestResponse<>(SUCCESS,
                String.valueOf(HttpStatus.OK),
                "COMPRA ID: " + id + " SUCCESSFULLY DELETED",
                "null"); // Data null.
    }

}