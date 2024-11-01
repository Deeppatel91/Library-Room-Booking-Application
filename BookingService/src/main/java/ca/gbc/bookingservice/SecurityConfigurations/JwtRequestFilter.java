
package ca.gbc.bookingservice.SecurityConfigurations;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Component that filters every request once per request to check for JWT in the Authorization header.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // Inject the JwtTokenProvider to extract details from the token.

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization"); // Fetch the Authorization header from the request.

        String jwt = null;
        String userId = null;

        // Check if the Authorization header is present and begins with "Bearer ".
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extract the JWT token by stripping "Bearer " prefix.
        }

        if (jwt != null) {
            try {
                userId = jwtTokenProvider.extractUserId(jwt); // Extract userId from JWT using JwtTokenProvider.
            } catch (Exception e) {
                logger.error("Error extracting userId from JWT: " + e.getMessage(), e); // Log error if userId extraction fails.
            }
        }

        // Authenticate only if userId is extracted and no authentication is currently set.
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, null, null);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // Set details about the authentication request.
            SecurityContextHolder.getContext().setAuthentication(authToken); // Set the Authentication in the context to this token.
        }
        chain.doFilter(request, response); // Continue the filter chain with the request and response.
    }
}
