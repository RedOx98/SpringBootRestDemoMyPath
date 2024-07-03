package com.olahammed.SpringRestDemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.olahammed.SpringRestDemo.models.Account;
import com.olahammed.SpringRestDemo.services.AccountService;

@Component
public class SeedData implements CommandLineRunner{

    @Autowired
    private AccountService accountService;

    @Override
    public void run(String... args) throws Exception {
        Account account01 = new Account();
        Account account02 = new Account();

        account01.setEmail("olaskeet@gmail.com");
        account01.setPassword("password");
        account01.setRole("ROLE_USER");
        accountService.save(account01);

        account02.setEmail("olahammed@ecobank.com");
        account02.setPassword("pass987");
        account02.setRole("ROLE_ADMIN");
        accountService.save(account02);
        
    }
    
}
