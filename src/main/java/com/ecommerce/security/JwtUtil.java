package com.ecommerce.security;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ecommerce.config.JwtProperties;
import com.ecommerce.entity.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	 private final JwtProperties props;
	 private final Key secretKey;
	 
	 
	   public JwtUtil(JwtProperties props) {
	        this.props = props;
	        this.secretKey = Keys.hmacShaKeyFor(
	                props.getSecret().getBytes()
	        );
	    }

    // CHỈ tạo ACCESS TOKEN
    public String generateAccessToken(String email, Set<Role> roles) {	  
    	List<String> roleNames  = roles.stream()
                .map(Role::getName)
                .toList();

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roleNames ) 
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + props.getAccessExpiration()))
                .signWith(secretKey)
                .compact();
    }

    // Lấy toàn bộ claims
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Lấy email từ token
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }
    
    public List<String> extractRoles(String token) {
        return extractClaims(token).get("roles", List.class);
    }

    public boolean isTokenValid(String token) {
        try {
            return extractClaims(token)
                    .getExpiration()
                    .after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}