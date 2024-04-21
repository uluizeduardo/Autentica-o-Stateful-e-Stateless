package br.com.microservice.statelessauthapi.core.service;

import br.com.microservice.statelessauthapi.core.model.User;
import br.com.microservice.statelessauthapi.infra.exception.AuthenticationException;
import br.com.microservice.statelessauthapi.infra.exception.ValidationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String EMPTY_SPACE = " ";
    private static final Integer TOKEN_INDEX = 1;
    private static final Integer ONE_DAY_IN_HOURS = 24;
    @Value("${app.token.secret-key}")
    public String secretkey;
    public String createToken(User user){
        var data = new HashMap<String, String>();
        data.put("id", user.getId().toString());
        data.put("username", user.getUsername());
        return Jwts
                .builder()
                .setClaims(data)
                .setExpiration(generateExpiresAt())
                .signWith(generateSign())
                .compact();
    }

    private Date generateExpiresAt(){
        return Date.from(
                LocalDateTime.now()
                    .plusHours(ONE_DAY_IN_HOURS)
                    .atZone(ZoneId.systemDefault()).toInstant()
        );
    }

    /**
     * Esse método pega todos os bites
     * do segredo (secretkey) cria um
     * algoritimo em bites com base no hash (hmacShaKeyFor)
     */
    private SecretKey generateSign(){
        return Keys.hmacShaKeyFor(secretkey.getBytes());
    }

    public void validateAccessToken(String token){
        var accessToken = extractToken(token);
        try {
            Jwts
                .parserBuilder()
                .setSigningKey(generateSign())
                .build()
                .parseClaimsJwt(accessToken)
                .getBody();

        } catch (Exception ex){
            throw new AuthenticationException("Invalid token " + ex.getMessage());
        }
    }

    private String extractToken(String token){
        if(ObjectUtils.isEmpty(token)){
            throw  new ValidationException("The accessToken was not informed.");
        }
        if(token.contains(EMPTY_SPACE)){
            return token.split(EMPTY_SPACE)[TOKEN_INDEX];
        }
        return token;
    }
}
