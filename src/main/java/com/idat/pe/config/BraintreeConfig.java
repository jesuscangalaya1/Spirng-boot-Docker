package com.idat.pe.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class BraintreeConfig {

    @Value("${braintree.merchant-id}")
    private String merchantId;

    @Value("${braintree.public-key}")
    private String publicKey;

    @Value("${braintree.private-key}")
    private String privateKey;



}
