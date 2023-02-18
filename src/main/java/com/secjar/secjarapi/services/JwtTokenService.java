package com.secjar.secjarapi.services;

import com.secjar.secjarapi.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final DiskInfoService diskInfoService;

    public JwtTokenService(JwtEncoder encoder, DiskInfoService diskInfoService) {
        this.jwtEncoder = encoder;
        this.diskInfoService = diskInfoService;
    }

    public String generateToken(User user) {

        List<SimpleGrantedAuthority> userAuthorities = user.getRoles()
                .stream()
                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().name())).toList();

        String scope = userAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(diskInfoService.getMaxUserSessionTime(), ChronoUnit.MILLIS))
                .subject(user.getName())
                .claim("scope", scope)
                .claim("userUuid", user.getUuid())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
