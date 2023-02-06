package com.secjar.secjarapi.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {
    private final String verificationCode;

    public CustomWebAuthenticationDetails(HttpServletRequest request, String mfaToken) {
        super(request);
        verificationCode = mfaToken;
    }

    public String getToken() {
        return verificationCode;
    }
}
