package com.anme.GRC_PV.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    JWTGenerator tokenGenerator;
    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Gérer la requête préflight CORS OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return; // on ne traite pas plus cette requête OPTIONS
        }

        // Ton code existant pour récupérer et valider le token
        System.out.println("=====================================");
        System.out.println("Processing request for URL: " + request.getRequestURL());
        System.out.println("Request method: " + request.getMethod());
        System.out.println("Authorization header: " + request.getHeader("Authorization"));

        String token = getJWTFromRequest(request);
        System.out.println("Extracted token: " + (token != null ? "present" : "null"));

        if (StringUtils.hasText(token)) {
            try {
                System.out.println("Validating token...");
                boolean isValid = tokenGenerator.validateToken(token);
                System.out.println("Token validation result: " + isValid);

                String username = tokenGenerator.getUsernameFromJWT(token);
                System.out.println("Username extracted from token: " + username);

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                System.out.println("Loaded user details. Authorities: " + userDetails.getAuthorities());

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null,
                        userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                System.out.println("Authentication token set in SecurityContext");

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("Current Authentication: "
                        + (auth != null ? auth.getName() + " - " + auth.getAuthorities() : "null"));

            } catch (Exception e) {
                System.out.println("Error processing token: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No token found in request");
        }

        System.out.println("Proceeding with filter chain");
        System.out.println("=====================================");

        filterChain.doFilter(request, response);
    }

    String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

}
