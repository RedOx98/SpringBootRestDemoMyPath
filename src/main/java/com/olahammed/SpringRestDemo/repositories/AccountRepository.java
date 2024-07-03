package com.olahammed.SpringRestDemo.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.olahammed.SpringRestDemo.models.Account;

public interface AccountRepository extends JpaRepository<Account, Long > {

    Optional<Account> findByEmail(String email);
    
}
