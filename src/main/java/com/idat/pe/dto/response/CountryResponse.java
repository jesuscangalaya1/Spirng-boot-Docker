package com.idat.pe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CountryResponse {
    private Long id;
    private String city;
    private String country;
    private String airport;


}
