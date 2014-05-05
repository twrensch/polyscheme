package com.tomwrensch.pscheme;

import java.util.Collection;
import java.util.HashMap;

import com.tomwrensch.pscheme.java.Java;

/**
 * Lisp symbols and the symbol table used for interning.
 */
public class Symbol {
	private static HashMap<String,Symbol> hashtable =
			new HashMap<String,Symbol>();

	public static Symbol intern(String value) {
		Symbol sym = hashtable.get(value);
		if (sym == null) {
			sym = Java.isJavaForm(value) ? Java.newJavaSymbol() : new Symbol();
			sym.value = value;
			hashtable.put(value, sym);
		}
		return sym;
	}

	public static boolean has(String value) {
		return hashtable.containsKey(value);
	}

	public static Collection<Symbol> allSymbol() {
		return hashtable.values();
	}

	public static final Symbol QUOTE = intern("quote");
	public static final Symbol FN = intern("fn");
	public static final Symbol DEF = intern("def");
	public static final Symbol IF = intern("if");
	public static final Symbol OR = intern("or");
	public static final Symbol AND = intern("and");
	public static final Symbol UNKNOWN = intern("*unknown*");
	public static final Symbol ACC = intern("acc"); // Access, get & set together
	public static final Symbol NIL = intern("nil");

	private String value;

	protected Symbol() {}

	public String toString() {
		return value;
	}

	public boolean isKeyword() {
		return value.charAt(0) == ':' && value.length() > 1;
	}

	public boolean isJavaSymbol() {
		return false;
	}

	/** Symbol starts with '.' followed by non-dot char? (A java member) */
	public boolean dotStart() {
		return value.length() > 1 && value.charAt(0) == '.';
	}
}
