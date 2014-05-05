package com.tomwrensch.pscheme;

import com.tomwrensch.pscheme.java.Java;

/**
 * Abstract superclass for scheme and Java types.
 *
 * Types act like an environment that is checked just before the
 * global enviroinment when looking up procedures. This provides a
 * way to have different implementations of the same procedure name
 * for different types of objects, in other words this is the key to
 * adding polymorphism.
 */
public abstract class Type {

	public static Type typeOf(Object obj) {
		Type result = SchemeType.typeOf(obj);
		if (result == null)
			result = Java.typeOf(obj);
		return result;
	}

	public abstract Object get(Symbol key);
	public abstract void put(Symbol key, Object value);

	public String toString() {return "<type: na?>"; }
}
