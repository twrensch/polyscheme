package com.tomwrensch.pscheme;

/**
 * Exception for PScheme runtime errors.
 */
public class PSchemeException extends RuntimeException {
	private static final long serialVersionUID = 1;

	boolean evalError;

	public PSchemeException(String msg) { super(msg); }
	public PSchemeException(String msg, boolean evalError) {
		super(msg);
		this.evalError = evalError;
	}

	public PSchemeException(String msg, Throwable t) { super(msg, t); }

	public boolean isEvalError() { return evalError; }
}
