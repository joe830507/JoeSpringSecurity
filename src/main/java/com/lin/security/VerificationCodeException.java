package com.lin.security;

import javax.security.sasl.AuthenticationException;

@SuppressWarnings("serial")
public class VerificationCodeException extends AuthenticationException {

	public VerificationCodeException() {
		super("Verification Code error!");
	}
}
