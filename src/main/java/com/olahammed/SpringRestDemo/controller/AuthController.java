package com.olahammed.SpringRestDemo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.olahammed.SpringRestDemo.models.Account;
import com.olahammed.SpringRestDemo.payload.auth.AccountDTO;
import com.olahammed.SpringRestDemo.payload.auth.AccountViewDTO;
import com.olahammed.SpringRestDemo.payload.auth.AuthoritiesDTO;
import com.olahammed.SpringRestDemo.payload.auth.PasswordDTO;
import com.olahammed.SpringRestDemo.payload.auth.ProfileDTO;
import com.olahammed.SpringRestDemo.payload.auth.TokenDTO;
import com.olahammed.SpringRestDemo.payload.auth.UserLoginDTO;
import com.olahammed.SpringRestDemo.services.AccountService;
import com.olahammed.SpringRestDemo.services.TokenService;
import com.olahammed.SpringRestDemo.util.constants.AccountError;
import com.olahammed.SpringRestDemo.util.constants.AccountSuccess;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@Tag(name = "Auth Controller", description = "Controller for Account management")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AccountService accountService;

    
    
    @PostMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TokenDTO> token(@Valid @RequestBody UserLoginDTO userLogin) throws AuthenticationException {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword()));
            return ResponseEntity.ok(new TokenDTO(tokenService.generateToken(authentication)));
        } catch (Exception e) {
           log.debug(AccountError.TOKEN_GENERATION_ERROR.toString() + ": "+e.getMessage());
           return new ResponseEntity<>(new TokenDTO(null), HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping(value = "/users/add", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please enter a valid email and Password length between 6 to 20 characters")
    @ApiResponse(responseCode = "201", description = "Account added")
    @ApiResponse(responseCode = "200", description = "success")
    @Operation(summary = "Add a new user")
    public ResponseEntity<String> addUser(@Valid @RequestBody AccountDTO accountDTO) throws Exception {
        try {
            Account account = new Account();
            account.setEmail(accountDTO.getEmail());
            account.setPassword(accountDTO.getPassword());
            // account.setRole("ROLE_USER");
            accountService.save(account);
            return ResponseEntity.ok(AccountSuccess.ACCOUNT_ADDED.toString());
        } catch (Exception e) {
            log.debug(AccountError.ADD_ACCOUNT_ERROR.toString()+ ": "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping(value = "/users", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of users")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "List user API")
    @SecurityRequirement(name = "olahammed-demo-api")
    public List<AccountViewDTO> users() {
        List<AccountViewDTO> accounts = new ArrayList<>();

        for (Account account: accountService.findAll()){
            accounts.add(new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities()));
        }
        return accounts;
    }

    @GetMapping(value = "/profile", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of users")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "List user API")
    @SecurityRequirement(name = "olahammed-demo-api")
    public ResponseEntity<ProfileDTO> profile(Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            ProfileDTO profileDTO = new ProfileDTO(account.getId(),account.getEmail(),account.getAuthorities());
            return ResponseEntity.ok(profileDTO);
        }
        return new ResponseEntity<ProfileDTO>(new ProfileDTO(), HttpStatus.BAD_REQUEST);
    }

    @PutMapping(value = "/profile/update-password", produces = "application/json", consumes = "application/json")
    @ApiResponse(responseCode = "200", description = "List of users")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "Update profile")
    @SecurityRequirement(name = "olahammed-demo-api")
    public AccountViewDTO updatePassword(@Valid @RequestBody PasswordDTO passwordDTO, Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            account.setPassword(passwordDTO.getPassword());
            accountService.save(account);
            AccountViewDTO accountViewDTO = new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities());
            return accountViewDTO;            
    }

    @PutMapping(value = "/users/{userId}/update-authorities", produces = "application/json", consumes = "application/json")
    @ApiResponse(responseCode = "200", description = "Updated authorities")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @ApiResponse(responseCode = "400", description = "Invalid user")
    @Operation(summary = "Update authorities")
    @SecurityRequirement(name = "olahammed-demo-api")
    public ResponseEntity<AccountViewDTO> updateAuthorities(@Valid @RequestBody AuthoritiesDTO authoritiesDTO, @PathVariable Long userId) {
        Optional<Account> optionalAccount = accountService.findById(userId);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            account.setAuthorities(authoritiesDTO.getAuthorities());
            accountService.save(account);
            AccountViewDTO accountViewDTO = new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities());
            
            return ResponseEntity.ok(accountViewDTO);
        }
        return new ResponseEntity<AccountViewDTO>(new AccountViewDTO(), HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(value = "/profile/delete")
    @ApiResponse(responseCode = "200", description = "List of users")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "Delete profile")
    @SecurityRequirement(name = "olahammed-demo-api")
    public ResponseEntity<String> deleteUser(Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        if (optionalAccount.isPresent()) {
            accountService.deleteById(optionalAccount.get().getId());
            return ResponseEntity.ok("User deleted!");
        }
            
        return new ResponseEntity<String>("Bad request", HttpStatus.BAD_REQUEST);
    }
    
    
}
