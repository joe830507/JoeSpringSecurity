package com.lin.security;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@SuppressWarnings("finally")
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		if (request.getContentType().equals(MediaType.APPLICATION_JSON_UTF8_VALUE)
				|| request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
			ObjectMapper mapper = new ObjectMapper();
			UsernamePasswordAuthenticationToken token = null;
			try (InputStream is = request.getInputStream()) {
				User user = mapper.readValue(is, User.class);
				token = new UsernamePasswordAuthenticationToken(user.getUsername(),
						user.getPassword());
			} catch (IOException e) {
				log.error("authenticated error:{}", e);
				token = new UsernamePasswordAuthenticationToken("", "");
			} finally {
				setDetails(request, token);
				return this.getAuthenticationManager().authenticate(token);
			}
		} else {
			return super.attemptAuthentication(request, response);
		}
	}
}