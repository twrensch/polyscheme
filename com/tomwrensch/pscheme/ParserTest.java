package com.tomwrensch.pscheme;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for the parser.
 */
public class ParserTest extends U {

	@Test
	public void testAtoms() {
		Parser parser = new Parser("symbol :keyword 1235.34");
		Object obj = parser.read();
		assertEquals(Symbol.class, obj.getClass());
		assertEquals(obj.toString(), "symbol");

		obj = parser.read();
		assertTrue(obj instanceof Symbol);
		assertEquals(obj.toString(), ":keyword");
		assertTrue( ((Symbol) obj).isKeyword() );

		obj = parser.read();
		assertTrue(obj instanceof Number);
		assertEquals(1235.34, ((Number) obj).doubleValue(), 0);

		assertEquals(EOF, parser.read());
	}

	@Test
	public void testList() {
		Parser parser = new Parser("() (one 1) (2 . 3)");

		Object obj = parser.read();
		assertEquals(Pair.EMPTY, obj);

		obj = parser.read();
		assertTrue(obj instanceof Pair);
		assertTrue(car(obj) instanceof Symbol);
		assertTrue(cdr(obj) instanceof Pair);
		assertTrue(second(obj) instanceof Number);
		assertEquals(Pair.EMPTY, cdr(cdr(obj)));

		obj = parser.read();
		assertTrue(obj instanceof Pair);
		assertTrue(car(obj) instanceof Number);
		assertTrue(cdr(obj) instanceof Number);

		assertEquals(EOF, parser.read());
	}

	@Test
	public void testVector() {
		Parser parser = new Parser(" [] [one 1] [(1) (2)] ");

		Object obj = parser.read();
		assertTrue(obj instanceof Object[]);
		Object[] vect = (Object[]) obj;
		assertEquals(0, vect.length);

		obj = parser.read();
		assertTrue(obj instanceof Object[]);
		vect = (Object[]) obj;
		assertEquals(2, vect.length);
		isSymbol(vect[0], "one");
		assertTrue(vect[1] instanceof Number);

		obj = parser.read();
		assertTrue(obj instanceof Object[]);
		vect = (Object[]) obj;
		assertEquals(2, vect.length);
		assertTrue(vect[0] instanceof Pair);
		assertTrue(vect[1] instanceof Pair);

		assertEquals(EOF, parser.read());
	}
}
