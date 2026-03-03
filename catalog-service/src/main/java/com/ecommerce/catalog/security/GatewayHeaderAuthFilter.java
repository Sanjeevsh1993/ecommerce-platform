package com.ecommerce.catalog.security;

// BC References: BC-040, BC-034, BC-046

import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.util.CorrelationIdUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(1)
public class GatewayHeaderAuthFilter extends OncePerRequestFilter {
    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String cid = CorrelationIdUtils.resolveOrGenerate(request.getHeader(AppConstants.CORRELATION_ID_HEADER));
        CorrelationIdUtils.setMdc(cid);
        response.setHeader(AppConstants.CORRELATION_ID_HEADER, cid);
        try {
            String email = request.getHeader("X-User-Email");
            String rolesHeader = request.getHeader("X-User-Roles");
            if (email != null && !email.isBlank() && rolesHeader != null && !rolesHeader.isBlank()) {
                List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                        .map(String::trim).filter(r -> !r.isBlank())
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(email, null, authorities));
            }
            chain.doFilter(request, response);
        } finally {
            CorrelationIdUtils.clearMdc();
            SecurityContextHolder.clearContext();
        }
    }
}
