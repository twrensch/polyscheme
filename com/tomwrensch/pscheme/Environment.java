package com.tomwrensch.pscheme;

import java.util.HashMap;
import java.util.Map;

/**
 * First pass on an environment.
 *
 * TODO: Change over to detecting if a key exists rather than using null.
 *
 * @author twrensch
 */
public class Environment extends U {

	public static Environment env(Environment parent) {
		return new Environment(parent);
	}

	Environment parent;
	Map<Symbol,Object> table;

	private Environment(Environment parent) {
		this.parent = parent;
		table = (parent == null)
		    ? new HashMap<Symbol,Object>() : new LinearMap();
	}

	public Object get(Symbol key) {
		Object item = table.get(key);
		if (item == null && parent != null) {
			return parent.get(key);
		} else {
			return item;
		}
	}

	public Object get(Symbol key, Object within) {
		if (parent == null) {
			if (within instanceof Environment) {
				Object item = ((Environment) within).get(key);
				return item == null ? table.get(key) : item;
			}
			Type type = Type.typeOf(within);
			if (type != null) {
				Object item = type.get(key);
				return item == null ? table.get(key) : item;
			} else {
				return table.get(key);
			}
		} else {
			Object item = table.get(key);
			return item == null ? parent.get(key,within) : item;
		}
	}

	public void put(Symbol key, Object value) {
		table.put(key,value);
	}

	public Environment rootEnvironment() {
		Environment env = this;
		while (env.parent != null) env = env.parent;
		return env;
	}

	public void bindArgs(Object params, Object values) {
		while (params instanceof Pair && params != Pair.EMPTY) {
			put(symbol(car(params)), car(values));
			params = cdr(params);
			values = cdr(values);
		}
		if (params instanceof Symbol)
			put(symbol(params), values);
	}
}
