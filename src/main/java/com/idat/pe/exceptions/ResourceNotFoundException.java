package com.idat.pe.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResourceNotFoundException extends RuntimeException  {

    private String message;


    public ResourceNotFoundException(String message) {
        super();
        this.message = message;
    }


}
