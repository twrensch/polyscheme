package com.tomwrensch.pscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A map that uses linear search to efficiently represent
 * small tables.
 */
public class LinearMap implements Map<Symbol, Object> {

	int size = 0;
	Object[] contents = new Object[8];

	@Override
	public void clear() {
		for (int i=0; i<size; i++)
			contents[i] = null;
		size = 0;
	}

	@Override
	public boolean containsKey(Object key) {
		for (int i=0; i<size; i=i+2)
			if (key == contents[i])
				return true;
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		for (int i=1; i<size; i=i+2)
			if (value == contents[i]
					|| value != null && value.equals(contents[i]))
				return true;
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<Symbol, Object>> entrySet() {
		// Probably don't need
		return null;
	}

	@Override
	public Object get(Object key) {
		for (int i=0; i<size; i=i+1)
			if (key == contents[i])
				return contents[i+1];
		return null;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Set<Symbol> keySet() {
		// Probably don't need
		HashSet<Symbol> set = new HashSet<Symbol>();
		for (int i=0; i<size; i=i+2)
			if (contents[i] instanceof Symbol)
				set.add((Symbol) contents[i]);
		return set;
	}

	@Override
	public synchronized Object put(Symbol key, Object value) {
		for (int i=0; i<size; i=i+2)
			if (key == contents[i]) {
				contents[i+1] = value;
				return value;
			}
		if (size >= contents.length)
			grow((size / 2 + 1 + size / 4) * 2); 
		contents[size] = key;
		contents[size + 1] = value;
		size = size + 2;
		return value;
	}

	private void grow(int max) {
		Evaluator.instGrow++;
		contents = Arrays.copyOf(contents, max);
	}

	@Override
	public void putAll(Map<? extends Symbol, ? extends Object> m) {
		for (Symbol key : m.keySet())
			put(key, m.get(key));
	}

	@Override
	public Object remove(Object key) {
		// NO-OP for now
		return null;
	}

	@Override
	public int size() {
		return size / 2;
	}

	@Override
	public Collection<Object> values() {
		ArrayList<Object> items = new ArrayList<Object>();
		for (int i=1; i<size; i=i+2)
			items.add(contents[i]);
		return items;
	}
}
