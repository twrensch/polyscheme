package com.tomwrensch.pscheme;

import java.util.Iterator;

/** A lisp pair. */
public class Pair implements Iterable<Object> {

	public static Pair EMPTY = new Pair(null,null);
	static {
		EMPTY.setFirst(EMPTY);
		EMPTY.setRest(EMPTY);
	}

	Object first, rest;

	public Pair(Object first, Object rest) {
		this.first = first;
		this.rest = rest;
	}

	public Object getFirst() { return first; }
	public Object getRest() { return rest; }
	public void setFirst(Object first) { this.first = first; }
	public void setRest(Object rest) { this.rest = rest; }

	@Override
	public Iterator<Object> iterator() {
		return new PairIterator(this);
	}

	public class PairIterator implements Iterator<Object> {
		private Pair pair;
		public PairIterator(Pair pair) { this.pair = pair; }
		@Override
		public boolean hasNext() {
			return pair != EMPTY;
		}
		@Override
		public Object next() {
			Object result = pair.first;
			pair = pair.rest instanceof Pair ? (Pair) pair.rest : EMPTY;
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public String toString() { return U.stringify(this); }
}
