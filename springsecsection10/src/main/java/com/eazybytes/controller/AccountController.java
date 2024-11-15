package com.eazybytes.controller;

import com.eazybytes.model.Accounts;
import com.eazybytes.repository.AccountsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountsRepository accountsRepository;

    @GetMapping("/myAccount/{mail}")
    public Accounts getAccountDetails(@PathVariable String mail) {
        Accounts accounts = accountsRepository.findByMail(mail);
        if (accounts != null) {
            return accounts;
        } else {
            return null;
        }
    }

}
