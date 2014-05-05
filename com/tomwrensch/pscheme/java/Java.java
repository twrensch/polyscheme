package com.tomwrensch.pscheme.java;

import java.util.HashMap;
import java.util.Map;

import com.tomwrensch.pscheme.Environment;
import com.tomwrensch.pscheme.Pair;
import com.tomwrensch.pscheme.Symbol;

/**
 * Utility methods for interacting with Java.
 */
public class Java {

	static Map<Class<?>, JavaClass> classCache =
			new HashMap<Class<?>,JavaClass>();

	/** See if the symbol string directly references a Java object. */
	public static boolean isJavaForm(String ss) {
		return ss != null && ss.length() > 1 && ss.contains(".");
	}

	public static Object evalJavaSymbol(Symbol sym, Environment env,
			boolean useType, Object within) {
		String name = sym.toString();
		boolean isField = name.endsWith("$");
		if (!isField && !name.startsWith(".")) { // It's a class
			try {
				return javaClass(Class.forName(name));
			} catch (Exception e) {
				return null;
			}
		}
		if (useType && within != null) {
			return (within instanceof JavaClass 
					? (JavaClass) within
					: typeOf(within))
				.get(sym);
		}
		return null;
	}

	/** Normalize a string to be interpreted as a Java symbol. */
	public static String normalize(String ss) {
		if (ss == null || ss.length() < 2) return ss;
		boolean stripStart = ss.charAt(0) == '.';
		boolean stripEnd = ss.endsWith("$") || ss.endsWith(".");
		if (!stripStart && !stripEnd)
			return ss;
		else if (stripEnd)
			return ss.substring(stripStart ? 1 : 0, ss.length() - 1);
		else
			return ss.substring(1);
	}

	public static Symbol newJavaSymbol() {
		return new JavaSymbol();
	}

	public static JavaClass javaClass(Class<?> klass) {
		JavaClass jc = classCache.get(klass);
		if (jc == null) {
			jc = new JavaClass(klass);
			classCache.put(klass, jc);
		}
		return jc;
	}

	public static JavaClass typeOf(Object obj) {
		return javaClass(obj.getClass());
	}

	/** Converts an object to match a class type if possible */
	public static Object convert(Object obj, Class<?> type) {
		if (obj == null)
			return obj;
		else if (Object.class.equals(type))
			return obj;
		else if (type.isAssignableFrom(obj.getClass()))
			return obj;
		else if (obj instanceof Number || obj instanceof Character) {
			Number num = obj instanceof Character
					? (int) ((Character) obj).charValue()
					: (Number) obj;
			if (type.isPrimitive()) {
				if (Integer.TYPE.equals(type)) return num.intValue();
				if (Character.TYPE.equals(type)) return (char) num.intValue();
				if (Double.TYPE.equals(type)) return num.doubleValue();
				if (Long.TYPE.equals(type)) return num.longValue();
				if (Float.TYPE.equals(type)) return num.shortValue();
				if (Short.TYPE.equals(type)) return num.shortValue();
				if (Byte.TYPE.equals(type)) return num.byteValue();
			} else {
				if (Integer.class.equals(type)) return num.intValue();
				if (Character.class.equals(type)) return (char) num.intValue();
				if (Double.class.equals(type)) return num.doubleValue();
				if (Long.class.equals(type)) return num.longValue();
				if (Float.class.equals(type)) return num.shortValue();
				if (Short.class.equals(type)) return num.shortValue();
				if (Byte.class.equals(type)) return num.byteValue();
			}
			return obj;
		} else if (Boolean.TYPE.equals(type) || Boolean.class.equals(type)) {
			return obj != Boolean.FALSE && obj != Pair.EMPTY;
		} else {
			return obj;
		}
	}

	/** Can an object of type at be used as or converted into an object of type pt? */
	public static boolean isConvertableFrom(Class<?> pt, Class<?> at) {
		if (Object.class.equals(pt)) return true;
		if (at == null) return !pt.isPrimitive();
		if (pt.isAssignableFrom(at)) return true;
		if (!pt.isPrimitive()) {
			if (Integer.class.equals(pt)
					|| Double.class.equals(pt)
					|| Float.class.equals(pt)
					|| Long.class.equals(pt)
					|| Character.class.equals(pt)
					|| Short.class.equals(pt)
					|| Byte.class.equals(pt))
				return Long.class.equals(at)
						|| Double.class.equals(at)
						|| Character.class.equals(at);
			return false;
		}
		if (Boolean.TYPE.equals(pt))
			return Boolean.class.equals(at);
		if (Integer.TYPE.equals(pt)
				|| Double.TYPE.equals(pt)
				|| Float.TYPE.equals(pt)
				|| Long.TYPE.equals(pt)
				|| Character.TYPE.equals(pt)
				|| Short.TYPE.equals(pt)
				|| Byte.TYPE.equals(pt))
			return Long.class.equals(at)
					|| Integer.class.equals(at)
					|| Double.class.equals(at)
					|| Character.class.equals(at);
		return false;
	}
}
