package com.idat.pe.emailpassword.controller;

import com.idat.pe.emailpassword.dto.ChangePasswordDTO;
import com.idat.pe.emailpassword.dto.EmailValuesDTO;
import com.idat.pe.emailpassword.service.EmailService;
import com.idat.pe.security.dto.Mensaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/email-password")
@CrossOrigin
public class EmailController {

    @Autowired
    EmailService emailService;


    @PostMapping("/send-email")
    public ResponseEntity<Mensaje> sendEmailTemplate(@RequestBody EmailValuesDTO dto) {
        return ResponseEntity.ok(emailService.sendEmailTemplate(dto));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Mensaje> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        return ResponseEntity.ok(emailService.changePassword(dto));
    }

}


