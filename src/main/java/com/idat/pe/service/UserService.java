package com.idat.pe.service;


import com.idat.pe.dto.response.PageableResponse;
import com.idat.pe.dto.response.UserResponse;

public interface UserService {

    PageableResponse<UserResponse> pageableUsers(int numeroDePagina, int medidaDePagina,
                                                 String ordenarPor, String sortDir);

    void deleteUser(Integer id);
}
