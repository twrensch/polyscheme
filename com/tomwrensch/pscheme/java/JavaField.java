package com.tomwrensch.pscheme.java;

import java.lang.reflect.Field;

import com.tomwrensch.pscheme.Environment;
import com.tomwrensch.pscheme.Pair;
import com.tomwrensch.pscheme.Procedure;
import com.tomwrensch.pscheme.Symbol;
import com.tomwrensch.pscheme.U;

/**
 * Represents a java member or static field.
 *
 * Implements procedure so it can act as a getter or setter.
 */
public class JavaField implements Procedure {

	JavaClass jclass;
	String jname;
	Symbol name;

	public JavaField(JavaClass jclass, String jname, Symbol name) {
		this.jclass = jclass;
		this.jname = jname;
		this.name = name;
	}

	@Override
	public Object apply(Object args, Environment env) {
		Field field;
		try {
			field = jclass.getField(jname);
		} catch (Exception e) {
			return U.syntax("Unable to resolve " + jname +
					" [" + e.getMessage() + "]");
		}
		try {
			if (args == Pair.EMPTY || ! (args instanceof Pair))
				return U.syntax("Missing object for field access");
			Object first = U.car(args);
			if (U.cdr(args) == Pair.EMPTY) 
				return field.get(first);
			Object value = Java.convert(U.second(args), field.getType());
			field.set(first, value);
			return value;
		} catch (IllegalAccessException e) {
			return U.error("Can't access field " + jname);
		}
	}

	@Override
	public Symbol getName() {
		return name;
	}

	@Override
	public void setName(Symbol name) {
		this.name = name;
	}
}
