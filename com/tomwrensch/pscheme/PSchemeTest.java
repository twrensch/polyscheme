package com.tomwrensch.pscheme;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Test;

/**
 * Basic unit tests to ensure the interpreter is working as expected.
 *
 * TODO: extend tests to include built in functions and polymorphic features.
 */
public class PSchemeTest {

    /** Basic test to make sure the parser and interpreter aren't hosed. */
    @Test
	public void test_basic() {
		PScheme pscheme = new PScheme();
		Object result = pscheme.load(new StringReader("(set! x 10) (+ x 1)"));
		assertTrue(result instanceof Number);
		assertEquals(11, ((Number) result).intValue());
	}

	@Test
	public void test_fn() {
		PScheme pscheme = new PScheme();
		String code = "(fn (x) (+ x 1))";
		Object result = pscheme.load(new StringReader(code));
		assertTrue(result instanceof SchemeProcedure);
		SchemeProcedure p = (SchemeProcedure) result;
		assertTrue(p.params instanceof Pair);
		assertTrue(p.params != Pair.EMPTY);
		assertTrue(U.cdr(p.params) == Pair.EMPTY);
		assertTrue(U.car(p.params) instanceof Symbol);
		assertEquals("x", U.symbol(U.car(p.params)).toString());

		assertEquals(Pair.EMPTY, U.cdr(p.body));
		assertTrue(U.car(p.body) instanceof Pair);
		assertEquals("+", U.symbol(U.car(U.car(p.body))).toString());

		pscheme.set("test", result);
		code = "(test 10)";
		result = pscheme.load(new StringReader(code));
		assertTrue(result instanceof Number);
		assertEquals(11, ((Number) result).intValue());
	}

	@Test
	public void test_fn2() {
		PScheme pscheme = new PScheme();
		String code = "(fn (x y) (cons x (cons y ())))";
		Object result = pscheme.load(new StringReader(code));
		assertTrue(result instanceof SchemeProcedure);
		SchemeProcedure p = (SchemeProcedure) result;
		assertTrue(p.params instanceof Pair);
		assertTrue(U.first(p.params) instanceof Symbol);
		assertTrue(U.second(p.params) instanceof Symbol);
		assertEquals(U.cddr(p.params), Pair.EMPTY);

		result = p.apply(U.list(1, 2), pscheme.global);
		assertTrue(U.first(result) instanceof Number);
		assertEquals(1, ((Number) U.first(result)).intValue());
		assertTrue(U.second(result) instanceof Number);
		assertEquals(2, ((Number) U.second(result)).intValue());
		assertEquals(Pair.EMPTY, U.cddr(result));
	}
}
