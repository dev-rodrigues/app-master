package com.httpsantos.cursomc.services.exceptions;

public class DataIntegrityViolationsException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public DataIntegrityViolationsException(String msg) {
		super(msg);
	}
	
	public DataIntegrityViolationsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
