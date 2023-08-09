package edu.idat.pe.project.controller;

import edu.idat.pe.project.dto.request.PurchaseRequest;
import edu.idat.pe.project.dto.response.PurchaseResponse;
import edu.idat.pe.project.dto.response.RestResponse;
import edu.idat.pe.project.security.dto.Mensaje;
import edu.idat.pe.project.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static edu.idat.pe.project.utils.constants.AppConstants.SUCCESS;

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

    @GetMapping("/list-deleted")
    public ResponseEntity<List<PurchaseResponse>> listPurchaseDeleted() {
        return new ResponseEntity<>(purchaseService.getDeletedPurchases(), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public RestResponse<String> restorePurchase(@PathVariable Long id) {
        this.purchaseService.restorePurchase(id);
        return new RestResponse<>(SUCCESS,
                String.valueOf(HttpStatus.OK),
                "COMPRA ID: " + id + " SUCCESSFULLY READED",
                "COMPRA RESTAURADO");
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