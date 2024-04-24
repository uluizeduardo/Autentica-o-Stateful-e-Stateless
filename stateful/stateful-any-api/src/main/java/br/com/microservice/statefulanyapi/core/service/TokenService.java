package br.com.microservice.statefulanyapi.core.service;

import br.com.microservice.statefulanyapi.core.client.TokenClient;
import br.com.microservice.statefulanyapi.core.dto.AuthUserResponse;
import br.com.microservice.statefulanyapi.infra.exception.AuthenticationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TokenService {

    private final TokenClient tokenClient;

    public void validateToken(String accessToken) {
        try {
            log.info("Sending request for token validation: {} ", accessToken);
            var response = tokenClient.validateToken(accessToken);
            log.info("Token is valid: {} ", response.accessToken());
        } catch (Exception ex) {
            throw new AuthenticationException("Auth error: " + ex.getMessage());
        }
    }

    public AuthUserResponse getAuthenticatedUser(String accessToken) {
        try {
            log.info("Sending request for auth user: {}", accessToken);
            var response = tokenClient.getAuthenticatedUser(accessToken);
            log.info("Auth user found: {} and token {}", response.toString(), accessToken);
            return response;
        } catch (Exception ex) {
            throw new AuthenticationException("Auth to get authentication user: " + ex.getMessage());
        }
    }
}
