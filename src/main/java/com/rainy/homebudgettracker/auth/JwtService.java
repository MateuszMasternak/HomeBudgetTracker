package com.rainy.homebudgettracker.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final RSAKeyProvider rsaKeyProvider;

    public Map<String, Claim> getClaims(String token) {
        Algorithm algorithm = Algorithm.RSA256(rsaKeyProvider);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT.getClaims();
    }

    public String getClaim(String token, String claim) {
        return getClaims(token).get(claim).asString();
    }
}
