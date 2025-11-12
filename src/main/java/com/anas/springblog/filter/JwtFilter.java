package com.anas.springblog.filter;

import com.anas.springblog.authentecation.model.CustomUserDetails;
import com.anas.springblog.authentecation.service.CustomUserDetailsService;
import com.anas.springblog.utility.IJwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final IJwtUtil jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        final String token = authHeader.substring(7);
        final String username;
        try {
            username = jwtService.extractUsername(token);
        }catch (JwtException ex){
            filterChain.doFilter(request,response);
            return;
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            CustomUserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(userDetails.getUsername(),token)){
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        filterChain.doFilter(request,response);
    }
}
