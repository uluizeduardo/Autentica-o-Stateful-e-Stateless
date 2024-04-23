package br.com.microservice.statefulauthapi.core.service;

import br.com.microservice.statefulauthapi.core.dto.AuthRequest;
import br.com.microservice.statefulauthapi.core.dto.AuthUserResponse;
import br.com.microservice.statefulauthapi.core.dto.TokenDTO;
import br.com.microservice.statefulauthapi.core.model.User;
import br.com.microservice.statefulauthapi.core.repository.UserRepository;
import br.com.microservice.statefulauthapi.infra.exception.AuthenticationException;
import br.com.microservice.statefulauthapi.infra.exception.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public TokenDTO login(AuthRequest authRequest){
        var user = findByUsername(authRequest.username());
        var accessToken = tokenService.createToken(user.getUsername());
        validatePassword(authRequest.password(), user.getPassword());
        return new TokenDTO(accessToken);
    }

    private User findByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found!"));
    }

    private void validatePassword(String rawPassword, String encodePassword) {

        if(ObjectUtils.isEmpty(rawPassword)){
            throw new ValidationException("The password must be informed!");
        }
        if(!passwordEncoder.matches(rawPassword, encodePassword)){
            throw  new ValidationException("The password is incorrect!");
        }
    }

    public AuthUserResponse getAuthenticatedUser(String accessToken) {
        var tokenData = tokenService.getTokenData(accessToken);
        var user = findByUsername(tokenData.username());
        return new AuthUserResponse(user.getId(), user.getUsername());
    }

    public void logout(String accessToken) {
        tokenService.deleteRedisToken(accessToken);
    }
    public TokenDTO validateToken(String accessToken){
        validateExistingToken(accessToken);
        var valid = tokenService.validateAccessToken(accessToken);
        if(valid) {
            return new TokenDTO(accessToken);
        }
        throw new AuthenticationException("Invalid token!");
    }

    private void validateExistingToken(String accessToken) {
        if(ObjectUtils.isEmpty(accessToken)){
            throw new ValidationException("The access token must be informed!");
        }
    }
}
