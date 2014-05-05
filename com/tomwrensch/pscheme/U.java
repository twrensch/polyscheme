package com.tomwrensch.pscheme;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

/**
 * Handy utilities used by many of the implementation classes.
 */
public class U {

	public static Boolean TRUE = Boolean.TRUE;
	public static Boolean FALSE = Boolean.FALSE;
	public static Pair NIL = Pair.EMPTY;
	public static Object EOF = new Object();


	public static <T> T error(String message) {
		throw new PSchemeException(message);
	}

	public static <T> T syntax(String message) {
		throw new PSchemeException(message, true);
	}

	public static void error(String message, Throwable e) {
		throw new RuntimeException(message, e);
	}

	public static Pair list(Object... objects) {
		Pair pair = Pair.EMPTY;
		for (int i=objects.length - 1; i >= 0; i--) {
			pair = new Pair(objects[i], pair);
		}
		return pair;
	}

	public static Pair cons(Object car, Object cdr) {
		return new Pair(car,cdr);
	}

	public static Pair pair(Object obj) {
		return obj instanceof Pair ? (Pair) obj :
			(Pair) error("Expected pair, got " + obj);
	}

	public static Symbol symbol(Object obj) {
		return obj instanceof Symbol ? (Symbol) obj :
			(Symbol) error("Expected symbol, got " + obj);
	}

	public static Number number(Object obj) {
		return obj instanceof Number ? (Number) obj :
			(Number) error("Expected number, got " + obj);
	}

	public static boolean isLogicalFalse(Object obj) {
		return obj == Boolean.FALSE || obj == Pair.EMPTY;
	}

	public static boolean isLogicalTrue(Object obj) {
		return obj != Boolean.FALSE && obj != Pair.EMPTY;
	}

	public static Object car(Object pair) { return pair(pair).getFirst(); }
	public static Object cdr(Object pair) { return pair(pair).getRest(); }
	public static Object cddr(Object pair) { return cdr(cdr(pair)); }
	public static Object first(Object pair) { return pair(pair).getFirst(); }
	public static Object second(Object pair) { return car(cdr(pair)); }
	public static Object third(Object pair) { return car(cddr(pair)); }
	public static Object forth(Object pair) { return car(cddr(cdr(pair))); }

	public static Pair toList(Collection<?> col) {
		Pair head = Pair.EMPTY;
		Pair tail = null;
		for (Object item: col) {
			if (tail == null) {
				tail = new Pair(item, Pair.EMPTY);
				head = tail;
			} else {
				Pair cons = new Pair(item, Pair.EMPTY);
				tail.setRest(cons);
				tail = cons;
			}
		}
		return head;
	}

	public static int length(Object obj) {
		if (obj instanceof Pair) {
			int n = 0;
			while (obj instanceof Pair && obj != Pair.EMPTY) {
				n++;
				obj = ((Pair) obj).getRest();
			}
			return n;
		} else if (obj instanceof Object[]) {
			return ((Object[]) obj).length;
		} else if (obj instanceof Collection<?>) {
			return ((Collection<?>) obj).size();
		} else if (obj instanceof String) {
			return ((String) obj).length();
		} else {
			return 0;
		}
	}

	public static Object[] toVector(Object obj) {
		if (obj instanceof Object[]) {
			return (Object[]) obj;
		} else if (obj instanceof Collection<?>)
			return ((Collection<?>) obj).toArray();
		else if (obj instanceof Pair) {
			Object[] vect = new Object[length(obj)];
			int i = 0;
			while (obj instanceof Pair && obj != NIL) {
				vect[i++] = ((Pair) obj).getFirst();
				obj = ((Pair) obj).getRest();
			}
			return vect;
		} else {
			Object[] vect = new Object[1];
			vect[0] = obj;
			return vect;
		}
	}

	public static boolean isSymbol(Object obj, String value) {
		return obj instanceof Symbol
				&& ((Symbol) obj).toString().equals(value);
	}

	public static Object ensureName(Object proc, Object name) {
		if (proc instanceof Procedure && name instanceof Symbol) {
			Procedure p = (Procedure) proc;
			if (p.getName() == Symbol.UNKNOWN)
				p.setName(symbol(name));
		}
		return proc;
	}

	public static String stringify(Object obj) {
		StringWriter writer = new StringWriter();
		try {
			stringifyTo(obj, writer);
		} catch (IOException e) {
			error("Unexpected IO error");
		}
		return writer.toString();
	}

	public static void write(Writer writer, Object obj) {
		try {
			writer.append(stringify(obj));
		} catch (IOException e) {
			// Ignore it for now, maybe change to a runtime error?
		}
	}

	public static void stringifyTo(Object obj, Writer out) throws IOException {
		boolean first = true;
		if (obj instanceof Pair) {
			out.write('(');
			while (obj instanceof Pair && obj != Pair.EMPTY) {
				if (first) first = false; else out.write(' ');
				Pair pair = (Pair) obj;
				stringifyTo(pair.getFirst(), out);
				obj = pair.getRest();
			}
			if (obj != Pair.EMPTY) {
				out.write(". ");
				stringifyTo(obj, out);
			}
			out.write(')');
		} else if (obj instanceof Object[]) {
			out.write('[');
			for (Object item: (Object[]) obj) {
				if (first) first = false; else out.write(' ');
				stringifyTo(item, out);
			}
			out.write(']');
		} else if (obj instanceof Boolean) {
			out.write(Boolean.TRUE.equals(obj) ? "#t" : "#f");
		} else if (obj == null) {
			out.write("#n");
		} else {
			out.write(obj == null ? "#n" : obj.toString());
		}
	}

	@SuppressWarnings("unchecked")
	public static Iterator<Object> iter(Object obj) {
		if (obj instanceof Object[])
			return new ArrayIterator((Object[]) obj);
		if (obj instanceof Iterable)
			return ((Iterable<Object>) obj).iterator();
		throw new UnsupportedOperationException(
				"Can't iterate on: " + stringify(obj));
	}

	private static class ArrayIterator implements Iterator<Object> {
		Object[] array;
		int pos = 0;
		ArrayIterator(Object[] array) { this.array = array; }
		public boolean hasNext() { return pos < array.length; }
		public Object next() { return array[pos++]; }
		public void remove() { }
	}
}
