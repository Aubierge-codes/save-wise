package com.savewise.auth;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests from the session cookie.
 *
 * <p>This deliberately replaces the resource-server bearer-token filter: that DSL exempts
 * token-bearing requests from CSRF checks, which is only safe for tokens sent in a header.
 * Because this token rides in a cookie the browser attaches automatically, CSRF protection
 * must stay on for every state-changing request.
 */
public class SessionCookieAuthenticationFilter extends OncePerRequestFilter {

    private static final List<SimpleGrantedAuthority> USER_AUTHORITIES = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    private final JwtService jwtService;
    private final String cookieName;

    public SessionCookieAuthenticationFilter(JwtService jwtService, String cookieName) {
        this.jwtService = jwtService;
        this.cookieName = cookieName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = readCookie(request);
        if (token != null) {
            jwtService.verify(token).ifPresent(user -> {
                var authentication = UsernamePasswordAuthenticationToken.authenticated(user, null, USER_AUTHORITIES);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            });
        }
        chain.doFilter(request, response);
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
