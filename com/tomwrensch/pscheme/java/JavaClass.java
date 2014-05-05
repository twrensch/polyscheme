package com.tomwrensch.pscheme.java;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.tomwrensch.pscheme.Environment;
import com.tomwrensch.pscheme.Pair;
import com.tomwrensch.pscheme.Procedure;
import com.tomwrensch.pscheme.Symbol;
import com.tomwrensch.pscheme.Type;
import com.tomwrensch.pscheme.U;

/**
 * Represents a java class.
 *
 * This implements the procedure interface so it can act as a
 * constructor for instances of the class.
 */
public class JavaClass extends Type implements Procedure {

	Class<Object> klass;
	Map<String,Object> cache;
	Map<String,Method[]> methods = null;

	@SuppressWarnings("unchecked")
	public JavaClass(Class<?> klass) {
		this.klass = (Class<Object>) klass;
	}

	public void cacheOn() {
		if (cache == null) cache = new HashMap<String, Object>();
	}

	public void cacheOff() {
		cache = null;
	}

	@Override
	public Object get(Symbol key) {
		if (key == null) return null;
		String k = key.toString();
		if (k.isEmpty()) return null;
		String name = Java.normalize(k);
		return k.endsWith("$")
				? new JavaField(this, name, key)
				: new JavaMethod(this, name, key);
	}

	@Override
	public void put(Symbol key, Object value) {
		U.error("Can't set varaibles in Java types");
	}

	@Override
	public Object apply(Object args, Environment env) {
		try {
			Constructor<?> con = getConstructor(args);
			if (con == null) return null;
			Object[] argArray = U.toVector(args);
			Class<?>[] paramArray = con.getParameterTypes();
			for (int i=0; i<argArray.length; i++)
				argArray[i] = Java.convert(argArray[i], paramArray[i]);
			return con.newInstance(argArray);
		} catch (Exception e) {
			return U.syntax("Error in Java constructor ["
					+ e.getMessage() + "]");
		}
	}

	@Override
	public Symbol getName() {
		String name = klass.getCanonicalName();
		return name == null ? Symbol.UNKNOWN : Symbol.intern(name);
	}

	@Override
	public void setName(Symbol name) {
		// Can't do this, ignore
	}

	/** Get the classes from the list of arguments */
	public static Class<?>[] classesOf(Object list) {
		Class<?>[] classes = new Class<?>[U.length(list)];
		int i=0;
		Object curr = list;
		while (curr instanceof Pair && curr != Pair.EMPTY) {
			Object obj = ((Pair) curr).getFirst();
			classes[i++] = obj == null ? null : obj.getClass();
			curr = ((Pair) curr).getRest();
		}
		return classes;
	}

	public Method[] methodsFor(String name) {
		if (methods == null) {
			methods = new HashMap<String,Method[]>();
			for (Method m: klass.getMethods()) {
				Method[] marr = methods.get(m.getName());
				if (marr == null) marr = new Method[0];
				marr = Arrays.copyOf(marr, marr.length + 1);
				marr[marr.length - 1] = m;
				methods.put(m.getName(), marr);
			}
		}
		return methods.get(name);
	}

	/** Scores how closely the parameter types match the argument types. */
	private int matchScore(Class<?>[] params, Class<?>[] args) {
		if (params.length == 0 && args.length == 0)
			return 1;
		if (params.length != args.length)
			return 0;
		int score = 0;
		for (int i=0; i<params.length; i++) {
			if (params[i].equals(args[i]))
				score = score + 2;
			else if (Java.isConvertableFrom(params[i], args[i]))
				score = score + 1;
			else return 0;
		}
		return score;
	}

	/** Compare parameter to argument types looking for an exact match */
	private boolean isExactMatch(Class<?>[] params, Class<?>[] args) {
		if (params.length == 0 && args.length == 0)
			return true;
		if (params.length != args.length)
			return false;
		for (int i=0; i<params.length; i++)
			if (!params[i].equals(args[i]))
				return false;
		return true;
	}

	public Method getMethod(String name, Object args) {
		Method[] marr = methodsFor(name);
		if (marr == null || marr.length == 0)
			return null;
		Class<?>[] argTypes = classesOf(U.cdr(args));
		// Check for an exact match
		for (Method m: marr)
			if (isExactMatch(m.getParameterTypes(), argTypes))
				return m;
		// Look for close match. TODO: make this smarter
		for (Method m: marr)
			if (matchScore(m.getParameterTypes(), argTypes) > 0)
				return m;
		return null;
	}

	public Constructor<?> getConstructor(Object args) {
		Constructor<?>[] carr = klass.getConstructors();
		if (carr == null || carr.length == 0)
			return null;
		Class<?>[] argTypes = classesOf(args);
		// Check for an exact match
		for (Constructor<?> c: carr)
			if (isExactMatch(c.getParameterTypes(), argTypes))
				return c;
		for (Constructor<?> c: carr)
			if (matchScore(c.getParameterTypes(), argTypes) > 0)
				return c;
		return null;
	}

	public Field getField(String name) throws NoSuchFieldException {
		return klass.getDeclaredField(name);
	}

	public String toString() {
		return getName().toString();
	}
}
