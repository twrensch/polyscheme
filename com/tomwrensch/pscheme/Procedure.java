package com.tomwrensch.pscheme;

/**
 * Interface for any objects that act like procedures.
 * Note that in some cases the getName() can return a default
 * value as in the case of an anonymous procedure.
 */
public interface Procedure {
	public abstract Object apply(Object args, Environment env);
	public abstract Symbol getName();
	public abstract void setName(Symbol name);
}
