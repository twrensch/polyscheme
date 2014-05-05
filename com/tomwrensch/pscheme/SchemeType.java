package com.tomwrensch.pscheme;

import java.util.HashMap;

/**
 * A built-in scheme type.
 *
 * Types act like environments that are checked just before checking
 * the global environment when looking up procedures to execute. Thus
 * the types provide a place for type-specific function to be inserted.
 * This is keyed to the second item (first argument) in the s-expression,
 * this it does not apply to procedures that take no argument.
 */
public class SchemeType extends Type {

	public static final SchemeType PAIR      = new SchemeType("PAIR");
	public static final SchemeType NUMBER    = new SchemeType("NUMBER");
	public static final SchemeType STRING    = new SchemeType("STRING");
	public static final SchemeType TYPE      = new SchemeType("TYPE");
	public static final SchemeType SYMBOL    = new SchemeType("SYMBOL");
	public static final SchemeType PROCEDURE = new SchemeType("PROCEDURE");
	public static final SchemeType ARRAY     = new SchemeType("ARRAY");
	public static final SchemeType NULL      = new SchemeType("NULL");
	public static final SchemeType BOOLEAN   = new SchemeType("BOOLEAN");

	public static SchemeType typeOf(Object object) {
		if (object == null) return NULL;
		else if (object instanceof Pair) return PAIR;
		else if (object instanceof Number) return NUMBER;
		else if (object instanceof String) return STRING;
		else if (object instanceof Boolean) return BOOLEAN;
		else if (object instanceof SchemeType) return TYPE;
		else if (object instanceof Symbol) return SYMBOL;
		else if (object instanceof Procedure) return PROCEDURE;
		else if (object instanceof Object[]) return ARRAY;
		else return null;
	}

	public static void install(Environment env) {
		NULL.insert(env);
		PAIR.insert(env);
		NUMBER.insert(env);
		STRING.insert(env);
		BOOLEAN.insert(env);
		TYPE.insert(env);
		SYMBOL.insert(env);
		PROCEDURE.insert(env);
		ARRAY.insert(env);
	}

	public SchemeType(String name) {
		this.name = name;
	}

	String name;
	HashMap<Symbol,Object> table = new HashMap<Symbol,Object>();

	public Object get(Symbol key) { return table.get(key); }
	public void put(Symbol key, Object value) { table.put(key, value); }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	private void insert(Environment env) {
		env.put(Symbol.intern(name), this);
	}

	public String toString() { return "<type " + name + ">"; }
}
