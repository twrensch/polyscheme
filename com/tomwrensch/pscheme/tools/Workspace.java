package com.tomwrensch.pscheme.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import com.tomwrensch.pscheme.PScheme;
import com.tomwrensch.pscheme.PSchemeException;
import com.tomwrensch.pscheme.U;

public class Workspace implements ActionListener {

	static final String FILE_SAVE = "Save";
	static final String FILE_NEW = "New";
	static final String FILE_OPEN = "Open...";
	static final String FILE_CLOSE = "Close";
	static final String FILE_SAVEAS = "Save as...";
	static final String FILE_REPL = "REPL...";
	static final String WORKSPACE_DOIT = "Do it";
	static final String WORKSPACE_PRINTIT = "Print it";
	static final String WORKSPACE_EVALALL = "Evaluate";

	static final String WORKSPACE_VAR = "*W*";

	public Workspace(PScheme pscheme) {
		this.pscheme = pscheme;
	}

	public Workspace() {
		this(new PScheme());
	}

	PScheme pscheme;
	JFrame frame;
	public JTextArea textArea;
	public File file;
	Color color = new Color(200,200,200);

	public void open(File file) {
		this.file = file;
		frame = makeFrame();
		textArea = makeTextArea();
		textArea.setText(ToolUtil.getText(file));
		frame.setMenuBar(makeMenuBar());
		frame.add(ToolUtil.makeVScroller(textArea));
		frame.pack();
		frame.setSize(400, 300);
		frame.setVisible(true);
	}

	MenuBar makeMenuBar() {
		MenuBar bar = new MenuBar();
		bar.add(makeFileMenu());
		bar.add(makeWorkspaceMenu());
		return bar;
	}

	Menu makeWorkspaceMenu() {
		Menu menu = new Menu();
		menu.setLabel("Workspace");
		menu.add(menuItem(WORKSPACE_DOIT, KeyEvent.VK_D, false));
		menu.add(menuItem(WORKSPACE_PRINTIT, KeyEvent.VK_P, false));
		menu.add(menuItem(WORKSPACE_EVALALL, KeyEvent.VK_E, false));
		return menu;
	}

	Menu makeFileMenu() {
		Menu menu = new Menu();
		menu.setLabel("File");
		menu.add(menuItem(FILE_NEW, KeyEvent.VK_N, false));
		menu.add(menuItem(FILE_OPEN, KeyEvent.VK_O, false));
		menu.add(menuItem(FILE_CLOSE, KeyEvent.VK_W, false));
		menu.add(menuItem(FILE_SAVE, KeyEvent.VK_S, false));
		menu.add(menuItem(FILE_SAVEAS, KeyEvent.VK_S, true));
		menu.add(menuItem("-", 0, false));
		menu.add(menuItem(FILE_REPL, KeyEvent.VK_I, false));
		return menu;
	}

	MenuItem menuItem(String label, int keycode, boolean shift) {
		MenuItem item = keycode == 0
				? new MenuItem(label)
				: new MenuItem(label, new MenuShortcut(keycode, shift));
		// item.setActionCommand(label);
		item.addActionListener(this);
		return item;
	}

	JFrame makeFrame() {
		JFrame frame = new JFrame(titleLabel());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return new JFrame(titleLabel());
	}

	JTextArea makeTextArea() {
		JTextArea area = new JTextArea();
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setTabSize(2);
		area.setEditable(true);
		area.setBackground(color);
		area.setMinimumSize(new Dimension(200,100));
		return area;
	}

	public String titleLabel() {
		return file == null ? "Workspace" : "Workspace: " + file.getName();
	}

	public boolean newFile() {
		File temp = ToolUtil.getSaveFile(frame);
		if (temp != null) {
			file = temp;
			frame.setTitle(titleLabel());
			return true;
		}
		return false;
	}

	// File stuff
	public boolean save(String text) {
		if (file == null) newFile();
		if (file != null)
			return ToolUtil.saveText(file, text);
		else
			return false;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String cmd = evt.getActionCommand();
		if (FILE_NEW.equals(cmd)) {
			new Workspace().open(null);
		} else if (FILE_OPEN.equals(cmd)) {
			File file = ToolUtil.getLoadFile(frame);
			if (file != null) {
				new Workspace(pscheme).open(file);
			}
		} else if (FILE_CLOSE.equals(cmd)) {
			frame.setVisible(false);
		} else if (FILE_SAVE.equals(cmd)) {
			save(textArea.getText());
		} else if (FILE_SAVEAS.equals(cmd)) {
			if (newFile())
				save(textArea.getText());
		} else if (FILE_REPL.equals(cmd)) {
			ToolUtil.openInteractor(pscheme);
		} else if (WORKSPACE_DOIT.equals(cmd)) {
			String code = getDoitString();
			String result = eval(code);
			if (result.startsWith("ERROR:"))
				insertAndSelect(result);
			System.out.println("|"+code+"|");
		} else if (WORKSPACE_PRINTIT.equals(cmd)) {
			// TODO: adjust the range so printit is at end of code chunk
			String code = getDoitString();
			insertAndSelect(eval(code));
		} else if (WORKSPACE_EVALALL.equals(cmd)) {
			String code = textArea.getText();
			Object result = eval(code);
			pscheme.set("$0", result);
		} else {
			System.out.println("Unexpected command: " + cmd);
		}
	}

	public String eval(String source) {
		if (source == null)
			return null;
		Object oldW = null;
		try { oldW = pscheme.get(WORKSPACE_VAR); }
		catch (PSchemeException e) {}
		pscheme.set(WORKSPACE_VAR, this);
		try {
			Object result = pscheme.eval(source);
			pscheme.set("$0", result);
			return U.stringify(result);
		} catch (Exception e) {
			return "ERROR: " + e.getMessage();
		} finally {
			pscheme.set(WORKSPACE_VAR, oldW);
		}
	}

	public String getDoitString() {
		int start = textArea.getSelectionStart();
		int end = textArea.getSelectionEnd();
		return  (start == end)
			? ToolUtil.pickCodeChunk(textArea.getText(), start)
			: textArea.getSelectedText();
	}

	public void insertAndSelect(String text) {
		int pos = textArea.getSelectionEnd();
		textArea.insert(text, pos);
		textArea.setCaretPosition(pos);
		textArea.moveCaretPosition(pos + text.length());
	}

	public static void main(String[] args) {
		ToolUtil.openWorkspace(new PScheme());
	}
}
