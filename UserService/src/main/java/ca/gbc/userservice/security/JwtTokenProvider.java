package ca.gbc.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")  // Load the secret key from application.properties
    private String secretKey;

    private static final long EXPIRATION_TIME = 86400000; // 1 day in milliseconds

    // Generate JWT token
    public String generateToken(String email, String role) {
        Map<String, Object> claims = Map.of("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName()), SignatureAlgorithm.HS512)
                .compact();
    }


    // Resolve JWT token from the HTTP request header
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Extract token after "Bearer "
        }
        return null;
    }

    // Extract email (subject) from the token
    public String getUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract role from the token
    public String getRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Utility method to extract claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes())  // Use byte array for secret key
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes()) // Use byte array of the secret key
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;  // Invalid or expired token
        }
    }

}
