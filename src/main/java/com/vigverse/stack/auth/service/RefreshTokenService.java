package com.vigverse.stack.auth.service;


import com.vigverse.stack.auth.entities.RefreshToken;
import com.vigverse.stack.auth.entities.User;
import com.vigverse.stack.auth.repositories.RefreshTokenRepository;
import com.vigverse.stack.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Ref;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    public RefreshToken createRefreshToken(String email){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("user not found with given email "+email));
        RefreshToken refreshToken = user.getRefreshToken();
        if(refreshToken==null){
            long refreshTokenValidity= 5 * 60 * 60 *10000;

            refreshToken=RefreshToken.builder().refreshToken(UUID.randomUUID().toString())
                    .expirationTime(Instant.now().plusMillis(refreshTokenValidity)).
                    user(user).build();
            refreshTokenRepository.save(refreshToken);
        }
        return  refreshToken;
    }

    public RefreshToken verifyRefreshToken(String refreshToken) {
        RefreshToken refToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found!"));

        if (refToken.getExpirationTime().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refToken);
            throw new RuntimeException("Refresh Token expired");
        }

        return refToken;
    }
}
