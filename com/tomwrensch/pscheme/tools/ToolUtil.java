package com.tomwrensch.pscheme.tools;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.tomwrensch.pscheme.PScheme;

/** Utilities useful in creating developer tools. */
public class ToolUtil {

	public static Object openInteractor(PScheme pscheme) {
		// Not yet implemented
		return null;
	}

	public static Object openWorkspace(PScheme pscheme) {
		Workspace w = new Workspace(pscheme);
		w.open(null);
		return w;
	}

	public static File getSaveFile(Frame frame) {
		FileDialog dialog =
				new FileDialog(frame, "Save File", FileDialog.SAVE);
		dialog.setModal(true);
		dialog.setVisible(true);
		return dialog.getFile() == null
		    ? null : new File(dialog.getDirectory(), dialog.getFile());
	}

	public static File getLoadFile(Frame frame) {
		FileDialog dialog =
				new FileDialog(frame, "Load File", FileDialog.LOAD);
		dialog.setModal(true);
		dialog.setVisible(true);
		return dialog.getFile() == null
				? null : new File(dialog.getDirectory(), dialog.getFile());
	}

	public static boolean textCopy(Reader source, Writer sink, boolean close) {
		int ch;
		try {
			while ( (ch = source.read()) != -1)
				sink.write(ch);
			if (close) {
				source.close();
				sink.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (close) {
				try { sink.close(); } catch (IOException e) {}
				try { source.close(); } catch (IOException e) {}
			}
		}
	}

	public static boolean saveText(File file, String text) {
		if (file != null) {
			try {
				return ToolUtil.textCopy(
						new StringReader(text), new FileWriter(file), true);
			} catch (IOException e) {
				return false;
			}
		} else {
			return false;
		}
	}

	public static String getText(File file) {
		if (file == null) return "";
		if (!file.exists()) return "";
		try {
			StringWriter buf = new StringWriter();
			ToolUtil.textCopy(new FileReader(file), buf, true);
			return buf.toString();
		} catch (IOException e) {
			return null;
		}
	}

	public static JScrollPane makeVScroller(Component c) {
		return new JScrollPane(c,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}

	/**
	 * Pick a code chunk out of the source string from near the text position.
	 */
	public static String pickCodeChunk(String source, int position) {
		// Just pick a line for now. Make it smarter later
		int s = position - 1, e = position;
		while ( s >= 0 && source.charAt(s) != '\n') s--;
		while ( e < source.length() && source.charAt(e) != '\n') e++;
		return source.substring(s + 1, e);
	}

	public static void main(String[] args) {
		String source = "abc\ndef\ngef\n";
		for (int i=0; i<source.length(); i++) {
			System.out.println(i+" |"+pickCodeChunk(source,i)+"|");
		}
	}
}
