package com.idat.pe.controller;

import com.idat.pe.dto.request.BraintreeRequest;
import com.idat.pe.dto.response.ClientTokenDto;
import com.idat.pe.service.impl.BraintreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/braintree")
@CrossOrigin("*")
public class BraintreeController {

    private final BraintreeService paymentService;

    @GetMapping("/token")
    public ResponseEntity<ClientTokenDto> getToken() {
        return ResponseEntity.ok(paymentService.getToken());
    }

    @PostMapping("/checkout")
    public ResponseEntity<Result<Transaction>> checkout(@RequestBody BraintreeRequest dto) {
        return ResponseEntity.ok(paymentService.checkout(dto));
    }

}

