package br.rafaeros.smp.core.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import br.rafaeros.smp.modules.user.model.User;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            Long companyId = (user.getSector() != null) ? user.getSector().getCompany().getId() : null;
            Long sectorId = (user.getSector() != null) ? user.getSector().getId() : null;

            var builder = JWT.create()
                    .withIssuer("smp-api")
                    .withSubject(user.getUsername())
                    .withExpiresAt(generateExpirationDate());

            if (companyId != null) builder = builder.withClaim("companyId", companyId);
            if (sectorId != null) builder = builder.withClaim("sectorId", sectorId);

            return builder.sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar JWT Token", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("smp-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(8).toInstant(ZoneOffset.of("-03:00"));
    }
}
