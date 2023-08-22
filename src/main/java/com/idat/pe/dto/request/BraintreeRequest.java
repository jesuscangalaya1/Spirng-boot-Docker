package com.idat.pe.dto.request;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class BraintreeRequest {

    private String nonce;
    private BigDecimal amount;




}
