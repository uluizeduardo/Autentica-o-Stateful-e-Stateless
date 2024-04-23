package br.com.microservice.statefulauthapi.core.service;

import br.com.microservice.statefulauthapi.core.dto.TokenData;
import br.com.microservice.statefulauthapi.infra.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TokenService {

    private static final String EMPTY_SPACE = " ";
    private static final Integer TOKEN_INDEX = 1;
    private static final Long ONE_DAY_IN_SECONDS = 86400L;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Método para criar o token.
     * O token que estamos utilizando é um token opaco, ou seja, não guardamos dados do usuário no token, ele é apenas
     * uma string com caracters especiais.
     * @param usermane
     * @return
     */
    public String createToken(String usermane){
        var accessToken = UUID.randomUUID().toString();
        var data = new TokenData(usermane);
        var jsonData = getJsonData(data);
        redisTemplate.opsForValue().set(accessToken, jsonData); //salvar no redis
        redisTemplate.expireAt(accessToken, Instant.now().plusSeconds(ONE_DAY_IN_SECONDS)); // informa o tempo de expiração dos dados
        return accessToken;
    }

    /**
     * Método que converte os dados de um objeto em uma string
     * @param payload
     * @return
     */
    private String getJsonData(Object payload){
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "";
        }
    }

    @SneakyThrows
    public TokenData getTokenData(String token) {
        var accessToken = extractToken(token);
        var jsonString = getRedisTokenValue(accessToken);
        try {
            return objectMapper.readValue(jsonString, TokenData.class);
        } catch (Exception ex) {
            throw new AuthenticationException("Error extracting the authenticated user: " + ex.getMessage());
        }
    }

    private String getRedisTokenValue(String token){
        return redisTemplate.opsForValue().get(token);
    }

    public boolean validateAccessToken(String token) {
        var accessToken = extractToken(token);
        var data = getRedisTokenValue(accessToken);
        return !ObjectUtils.isEmpty(data);
    }

    private void deleteRedisToken(String token){
        var accessToken = extractToken(token);
        redisTemplate.delete(accessToken);
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
