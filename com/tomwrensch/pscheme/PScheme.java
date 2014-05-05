package com.tomwrensch.pscheme;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * Class to represent a standard PScheme interpreter.
 *
 * This class can be used directly to manage a PScheme interpreter or it can
 * be used as a starting point for creating a custom variation.
 */
public class PScheme extends U {

	public PScheme() {
		global = Environment.env(null);
		BuiltIn.install(global);
		SchemeType.install(global);
		global.put(Symbol.intern("*G*"), global);
		global.put(Symbol.intern("*PSCHEME*"), this);
		loadInits();
	}

	/**
     * Location of scheme source with 'primitive' code. This code is expected
     * as part of the jar or otherwise in the classpath.
     */
	private static final String PRIM_SOURCE = "com/tomwrensch/pscheme/pscheme.scm";

    /**
     * Name of a source file to try to load after the interpreter is set up.
     * If included the file should be in the default directly, Intended for user
     * startup code.
     */
	public static final  String  INIT_SOURCE = "init.scm";

	Environment global;

	public Object evalProc(Procedure proc) {
		return proc.apply(Pair.EMPTY, global);
	}

	private void loadInits() {
		InputStream stream =
			PScheme.class.getClassLoader().getResourceAsStream(PRIM_SOURCE);
		if (stream != null)
			load(new InputStreamReader(stream));
		try { stream.close(); } catch (Exception e) {}
		File init = new File(INIT_SOURCE);
		if (init.exists())
			load(init);
	}

	public Object load(Reader reader) {
		return staticLoad(reader, global);
	}

	public static Object staticLoad(Reader reader, Environment env) {
		Parser parser = new Parser(reader);
		Object result = null;
		for (Object sexpr = parser.read(); sexpr != EOF; sexpr = parser.read())
			result = Evaluator.eval(sexpr, env, false, null);
		return result;
	}

	public Object load(File file) {
		try {
			Reader reader = new FileReader(file);
			Object result = load(reader);
			reader.close();
			return result;
		} catch (IOException e) {
			error("Unable to load file " + file, e);
			return null;
		}
	}

	public Object get(String name) {
		return Symbol.has(name) ? global.get(Symbol.intern(name)) : null;
	}

	public Object set(String name, Object value) {
		global.put(Symbol.intern(name), value);
		return value;
	}

	/** Evaluate source and return the result of the last statement. */
	public Object eval(String source) throws PSchemeException {
		Parser parser = new Parser(new StringReader(source));
		Object result = null;
		while (true) {
			Object input = parser.read();
			if (input == Parser.EOF) break;
			result = Evaluator.eval(input, global, false, null);
		}
		return result;
	}

	public boolean repl(Reader reader, Writer writer) {
		set("*exit*", false);
		String prompt = "> ";
		Parser parser = new Parser(reader);
		try {
			while (Boolean.FALSE.equals(this.get("*exit*"))) {
				writer.write(prompt);
				writer.flush();
				Object input = parser.read();
				try {
				Object result = Evaluator.eval(input, global, false, null);
				set("$", result);
				U.write(writer, result);
				} catch (PSchemeException e) {
					writer.write("ERROR: " + e.getMessage() + '\n');
				}  catch (StackOverflowError e) {
					writer.write("ERROR: stack overflow\n");
				}
				writer.write('\n');
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public Environment getGlobal() {
		return global;
	}

	public static void main(String[] args) {
		PScheme pscheme = new PScheme();
		Reader reader = new InputStreamReader(System.in);
		Writer writer = new OutputStreamWriter(System.out);
		pscheme.repl(reader, writer);
	}
}
