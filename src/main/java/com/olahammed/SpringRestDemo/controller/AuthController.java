package com.olahammed.SpringRestDemo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class AuthController {
    
    @PostMapping("/token")
    public String to(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    
}
