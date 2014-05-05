package com.tomwrensch.pscheme.java;

import java.lang.reflect.Method;

import com.tomwrensch.pscheme.Environment;
import com.tomwrensch.pscheme.Procedure;
import com.tomwrensch.pscheme.Symbol;
import com.tomwrensch.pscheme.U;

/** Represents a Java method. */
public class JavaMethod implements Procedure {

	JavaClass jclass;
	String jname;
	Symbol name;

	public JavaMethod(JavaClass jclass, String jname, Symbol name) {
		this.jclass = jclass;
		this.jname = jname;
		this.name = name;
	}

	@Override
	public Object apply(Object args, Environment env) {
		Method method;
		try {
			method = jclass.getMethod(jname, args);
			if (method == null)
				throw new RuntimeException("No method found: " + jname);
			Object[] argArray = U.toVector(U.cdr(args));
			Class<?>[] paramArray = method.getParameterTypes();
			for (int i=0; i<argArray.length; i++)
				argArray[i] = Java.convert(argArray[i], paramArray[i]);
			return method.invoke(U.car(args), argArray);
		} catch (Exception e) {
			return U.error("Unable to call method " + jname
				+ " [" + e.getClass().getName() + " " + e.getMessage() + "]");
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
