package com.idat.pe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryResponse {

    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String departureDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String arrivalDate;

    private String hour;

    private OriginResponse origin;
    private LocationResponse location;

}
