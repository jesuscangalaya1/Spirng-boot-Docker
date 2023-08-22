package com.idat.pe.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InternalServerErrorException extends RuntimeException {

    private String code;
    private String message;

    public InternalServerErrorException(String message) {
        this.message = message;
    }
}
