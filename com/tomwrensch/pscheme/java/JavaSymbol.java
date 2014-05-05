package com.tomwrensch.pscheme.java;

import com.tomwrensch.pscheme.Symbol;

/** A symbol that can directly name a Java class, method or field. */
public class JavaSymbol extends Symbol {

	public JavaSymbol() { super(); }

	@Override
	public boolean isJavaSymbol() {
		return true;
	}
}
