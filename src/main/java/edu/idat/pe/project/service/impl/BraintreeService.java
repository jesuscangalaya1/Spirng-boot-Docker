package edu.idat.pe.project.service.impl;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.ClientTokenRequest;
import com.braintreegateway.Environment;
import edu.idat.pe.project.config.BraintreeConfig;
import edu.idat.pe.project.dto.request.BraintreeRequest;
import edu.idat.pe.project.dto.response.ClientTokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.braintreegateway.*;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BraintreeService {

    private final BraintreeConfig config;

    public BraintreeGateway getGateway() {
        return new BraintreeGateway(
                Environment.SANDBOX,
                config.getMerchantId(),
                config.getPublicKey(),
                config.getPrivateKey()
        );
    }

    public ClientTokenDto getToken() {
        ClientTokenRequest request = new ClientTokenRequest();
        String token = getGateway().clientToken().generate(request);
        return new ClientTokenDto(token);
    }

    public Result<Transaction> checkout(BraintreeRequest dto) {
        TransactionRequest request = new TransactionRequest()
                .amount(dto.getAmount())
                .paymentMethodNonce(dto.getNonce())
                .options()
                .submitForSettlement(true)
                .done();
        return getGateway().transaction().sale(request);
    }

}
