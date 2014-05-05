package com.tomwrensch.pscheme;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Tokenize and parse pscheme.
 *
 * S -> ( S* )
 * S -> [ S* ]
 * S -> ' S
 * S -> ; c* \n
 * S -> " C* "
 * C -> \ c
 * C -> c
 *
 * TODO: add support for reader macros
 */
public class Parser extends U {

	public static final char NULL = (char) -1;

	Reader reader;
	char pushback = NULL;

	public Parser(Reader reader) {
		this.reader = reader;
	}

	public Parser(String source) {
		this(new StringReader(source));
	}

	public Object read() {
		char ch = nextCharSkippingStuff();
		if (ch == NULL) return EOF;
		else if (ch == '(') return readList();
		else if (ch == '[') return readVector();
		else if (ch == '\'') return readQuote();
		else if (ch == '"') return readString();
		else return readAtom(ch);
	}

	private Object readList() {
		Pair head = Pair.EMPTY;
		Pair tail = null;
		char ch = nextCharSkippingStuff();
		if (ch == ')') return Pair.EMPTY;
		pushChar(ch);
		Object item = read();
		head = tail = new Pair(item, Pair.EMPTY);
		while (true) {
			ch = nextCharSkippingStuff();
			if (ch == ')') return head;
			pushChar(ch);
			item = read();
			if (item == EOF) {
				readerError("Unexpected end of list");
			} else if (U.isSymbol(item, ".")) {
				item = read();
				if (item == EOF) readerError("Error in dotted pair");
				tail.setRest(item);
				ch = nextCharSkippingStuff();
				if (ch != ')') readerError("Missing ')' after dotted pair");
				return head;
			} else {
				Pair cons = new Pair(item, Pair.EMPTY);
				tail.setRest(cons);
				tail = cons;
			}
		}
	}

	private Object readVector() {
		ArrayList<Object> items = new ArrayList<Object>();
		while (true) {
			char ch = nextCharSkippingStuff();
			if (ch == ']') return U.toVector(items);
			pushChar(ch);
			Object item = read();
			if (item == EOF)
				readerError("Unexpected end of vector");
			items.add(item);
		}
	}

	private Object readQuote() {
		Object obj = read();
		if (obj == EOF) {
			readerError("unrealized quote at EOF");
		}
		return U.list(Symbol.QUOTE, obj);
	}

	private Object readString() {
		StringBuilder buf = new StringBuilder();
		while (true) {
			char ch = nextChar();
			if (ch == NULL) {
				readerError("Unterminated string");
			} else if (ch == '"') {
				return buf.toString();
			} else if (ch == '\\') {
				ch = nextChar();
				if (ch == NULL)
					readerError("Unresolved backslash at EOF");
				else if (ch == 'n')
					buf.append('\n');
				else if (ch == 't')
					buf.append('\t');
				else buf.append(ch);
			} else buf.append(ch);
		}
	}

	private Object readAtom(char first) {
		StringBuilder buf = new StringBuilder();
		buf.append(first);
		while (true) {
			char ch = nextChar();
			if (ch == NULL) break;
			else if (isWhitespace(ch)) break;
			else if (isSingle(ch)) {
				pushChar(ch);
				break;
			}
			else buf.append(ch);
		}
		return interpretAtom(buf.toString());
	}

	/** Interpret a java string as a non-string pscheme atom. */
	private Object interpretAtom(String atom) {
		if (atom.equals("#t")) return U.TRUE;
		if (atom.equals("#f")) return U.FALSE;
		if (atom.equals("#n")) return null;
		// Maybe a number?
		Object result = interpretNumber(atom);
		if (result != null) return result;
		// Nothing left but a symbol
		return Symbol.intern(atom);
	}

	private Number interpretNumber(String atom) {
		// TODO: This is a hack, make it better
		try {
			return Long.parseLong(atom);
		} catch (NumberFormatException e1) {
			try {
				return Double.parseDouble(atom);
			} catch (NumberFormatException e2) { }
		}
		return null;
	}

	private boolean isSingle(char ch) {
		return ch==')' || ch=='(' || ch=='\'' || ch=='[' || ch==']';
	}

	private boolean isWhitespace(int ci) {
		return Character.isWhitespace(ci);
	}

	/** Get next character after skipping whitespace and comments */
	private char nextCharSkippingStuff() {
		boolean inComment = false;
		while (true) {
			char ch = nextChar();
			if (ch == NULL) {
				return ch;
			} else if (inComment) {
				if (ch == '\n') inComment = false;
			} else if (Character.isWhitespace(ch)) {
				continue;
			} else if (ch == ';') {
				inComment = true;
			} else {
				return ch;
			}
		}
	}

	/** Get the next character from the input source */
	private char nextChar() {
		if (pushback == NULL)
			try {
				return (char) reader.read();
			} catch (IOException e) {
				readerError("error reading input");
			}
		char ch = pushback;
		pushback = NULL;
		return ch;
	}

	/** Push a character back so it's read as the next character */
	private void pushChar(char ch) {
		pushback = ch;
	}

	/** Signals a error */
	private void readerError(String msg) {
		U.syntax("syntax: " + msg);
	}
}
