polyscheme
==========

A Scheme-inspired Lisp interpreter with polymorphic types running in a JVM.

This is the initial working commit of version 0.1. It includes a working interpreter, the ability to execute a REPL in a command line environment, and a simple GUI workspace for developing and executing lisp code. It does *not* include proper documentation or the ability to add new types from Lisp code.

See the BuiltIn.java and pscheme.scm files for an idea of what's supported.

## A Brief Overview

### `def` and `fn` rather than `define` and `lambda`

Up to a point syntactic conciseness and short names for common functions makes programming easier. Polyscheme currently supports the use of `fn` to mean what is usually meant by `lambda` and `def` for what is usually meant by `define`. Future versions will include support for both forms, but which will be the official form is undecided.

    (def pi 3.14159)
    (def (timestwo n) (* n 2))
    (def timestwo (fn (n) (* n 2))
    (map (fn (x) (+ x 1)) '(2 3 4))

### Types

There are nine built in types in the current version of polyscheme:

- PAIR type for all lists and Lisp pairs
- NUMBER type for all numbers
- STRING type for strings
- TYPE type for the type objects
- SYMBOL type for all symbols
- PROCEDURE type for all scheme procedures and java functions
- ARRAY type for arrays of objects (a Vector in standard Schemes)
- NULL type for the null value (#n or '())
- BOOLEAN type for true (#t) or false (#f) values.

Types can be used to associate procedure implementations with a type of object by defining a method with `defm`. The type acts as an environment that is used just before the global environment when looking up procedures to execute. The type used in a multi-argument call is the type of the first argument. For example, in

    (+ "one" x)
    
The type of `"one"` (which is STRING) is used to look up the procedure associated with `+`.

Currently there's no support in polyscheme for creating new types directly in Lisp code, though it can be done in Java. This is the number one *to do* item.

### Creating type-specific procedures with `defm`

An implementation can be associated with a type using `defm` rather than `def`. For example, the following defines `+` and `*` operations for boolean values:

    (defm BOOLEAN (+ a b) (or a b))
    (defm BOOLEAN (* a b) (and a b))

These definitions will not interfere with the `+` and `*` procedures used for numbers or the `+` function used to concatenate strings as they will only be used when the first argument is of type BOOLEAN.

### Indexing and the `acc` procedure

The procedure `acc` acts as both a `get` function if it has two arguments and a `set` function if it has three. The built-in version of the function works with integer-indexed objects like arrays and strings as well as symbol-index objects like environments, types, and Java maps. There are also `get` and `put` functions that are simple wrappers around the `acc` function.

    ;; Gets the function used for string concatination
    (acc STRING '+)

    ;; Sets the string length function for symbols
    (acc SYMBOL 'len (fn (sym) (len (.toString sym)))

    ;; Get and set values from an array (zero-base)
    (def arr ["one" "second" "three"])
    (acc arr 1)  ;; will return "second"
    (acc arr 1 "two")
    (acc arr 1)  ;; will return "two"

### Accessing Java

The Java access system is based on ideas from Silk, which was the predecessor to JScheme, which inspired some of Clojure...anyway, this may be familiar.

You can reference a java class using it's path. Thus `java.util.Vector` refers to the Vector class object. You can call a constructor by using the class as a procedure.

    (def v1 (java.lang.Vector))
    (def v2 (java.lang.Vector 1000))

The above creates two vectors, `v1` is a Java Vector created with the zero-argument constructor and `v2` is created with the constructor that allows an initial capacity to be specified.

Java member or static functions can be called using a '.' before the function name and the object (or class for static function) as the first argument.

    (def v1 (java.lang.Vector))
    (.size v1)   ;; returns 0
    (.add v1 "hello")
    (.size v1)   ;; returns 1

Likewise static and member variables can be accessed by preceding the name with a '.' and following it with a '$'. Note that you can only access fields that are visible (e.g. public fields).

    (.PI$ java.lang.Math)

If you have class `myapp.Point` with a public `x` and `y` fields you can get or set the field values like this:

    (def p1 (myapp.Point 10 20))
    (.x$ p1)  ;; should return 10
    (.x$ p1 15)
    (.x$ p1)  ;; should return 15

## TODO

The following are priorities on the todo list for polyscheme:

1. The ability to add new types in Lisp code. The exact form of this is still undecided.
2. Make it easier to add new types in Java code, include an example (probably a hash table).
3. Allow for `lambda` and `define` as well as `fn` and `def`.
4. Decent reference documentation.
5. Examples demonstrating interesting capabilities and uses.

## History

2014 May 05 - Version 0.1: Initial (mostly) working version.
