package com.lin.security;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class VerificationCodeFilter extends OncePerRequestFilter {

	private AuthenticationFailureHandler failureHandler = new AuthenticationFailureHandler() {
		@Override
		public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException exception) throws IOException, ServletException {
		}
	};

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (!"/login".equals(request.getRequestURI())) {
			filterChain.doFilter(request, response);
		} else {
			try {
				verifyCode(request);
				filterChain.doFilter(request, response);
			} catch (AuthenticationException e) {
				failureHandler.onAuthenticationFailure(request, response, e);
			}
		}
	}

	private void verifyCode(HttpServletRequest request) throws VerificationCodeException {
		ObjectMapper mapper = new ObjectMapper();
		try (InputStream is = request.getInputStream()) {
			User user = mapper.readValue(is, User.class);
			String captcha = (String) request.getSession().getAttribute("captcha");
			if (!StringUtils.isEmpty(captcha))
				request.getSession().removeAttribute("captcha");
			if (StringUtils.isEmpty(user.getCaptcha()) || StringUtils.isEmpty(captcha)
					|| !user.getCaptcha().equals(captcha))
				throw new VerificationCodeException();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
