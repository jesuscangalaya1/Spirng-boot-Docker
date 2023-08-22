package com.idat.pe.controller;

import com.idat.pe.dto.response.PageableResponse;
import com.idat.pe.dto.response.RestResponse;
import com.idat.pe.dto.response.UserResponse;
import com.idat.pe.service.UserService;
import com.idat.pe.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.idat.pe.utils.constants.AppConstants.SUCCESS;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UsuarioController {

    private final UserService userService;

    @GetMapping(value = "/pageable-users", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<PageableResponse<UserResponse>> pageableProducts(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.NUMERO_DE_PAGINA_POR_DEFECTO, required = false) int numeroDePagina,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.MEDIDA_DE_PAGINA_POR_DEFECTO, required = false) int medidaDePagina,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.ORDENAR_POR_DEFECTO, required = false) String ordenarPor,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.ORDENAR_DIRECCION_POR_DEFECTO, required = false) String sortDir) {

        return new  RestResponse<>(SUCCESS,
                String.valueOf(HttpStatus.OK),
                "USERS SUCCESSFULLY READED",
                userService.pageableUsers(numeroDePagina, medidaDePagina, ordenarPor, sortDir));

    }

    @DeleteMapping("/{id}")
    public RestResponse<String> deleteFlight(@PathVariable Integer id) {
        userService.deleteUser(id);
        return new RestResponse<>(SUCCESS,
                String.valueOf(HttpStatus.OK),
                "USER ID: " + id + " SUCCESSFULLY DELETED",
                "null"); // Data null.
    }

}

