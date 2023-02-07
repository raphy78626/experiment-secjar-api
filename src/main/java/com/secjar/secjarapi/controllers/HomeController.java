package com.secjar.secjarapi.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home(Principal principal) {
        return "Hello, " + principal.getName();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/secure")
    public String secure() {
        return "secure";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/secure2")
    public String secure2() {
        return "secure2";
    }
}
