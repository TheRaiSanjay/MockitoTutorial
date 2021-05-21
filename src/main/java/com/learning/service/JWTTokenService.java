package com.learning.batch.service;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenService {
	private Integer tokenExpiryInMins;
	private byte[] tokenSecret;

	public static final String TOKEN_PREFIX = "Bearer";
	public static final String HEADER_STRING = "Authorization";

	private Set<String> VALID_TOKENS = new HashSet<>();

	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		String token = createToken(claims, username, tokenExpiryInMins);
		// Add token to Set
		VALID_TOKENS.add(token);
		return token;
	}

	public boolean validateToken(String token, String username) {
		final String usernameInToken = extractUsername(token);
		return (usernameInToken.equals(username) && !isTokenExpired(token) && isTokenValid(token));
	}

	private boolean isTokenExpired(String token) {
		boolean expired = extractExpiration(token).before(new Date());
		if (expired) {
			// Remove token from set when expired
			VALID_TOKENS.remove(token);
		}
		return expired;
	}

	private boolean isTokenValid(String token) {
		return (VALID_TOKENS.contains(token));
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public Claims extractAllClaims(String token) {

		return Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody();
	}

	private String createToken(Map<String, Object> claims, String subject, int expirationInMins) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * expirationInMins))
				.signWith(SignatureAlgorithm.HS256, tokenSecret).compact();
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public void logOut(String token) {
		VALID_TOKENS.remove(token);
	}

}
