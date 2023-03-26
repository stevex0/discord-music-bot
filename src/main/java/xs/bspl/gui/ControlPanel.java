package xs.bspl.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.List;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import xs.bspl.Bot;
import xs.bspl.util.Formatter;

/** The ControlPanel class creates a graphical user interface to control and log the bot's actions. */
public final class ControlPanel {
	private boolean running;
	private final JFrame frame;
	private final JTextArea text_field;
	private final TrayIcon tray_icon;
	private Thread thread;

	public ControlPanel() {

		if (GraphicsEnvironment.isHeadless()) {
			this.frame		= null;
			this.text_field	= null;
			tray_icon		= null;
			return;
		}

		// Frame
		this.frame = new JFrame("bspl - control panel");
		this.frame.getContentPane().setPreferredSize(new Dimension(800, 500));
		this.frame.setMinimumSize(new Dimension(400, 250));
		this.frame.setResizable(true);
		this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.frame.setVisible(true);
		this.frame.setLayout(new BorderLayout());
		this.frame.setFocusable(true);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				final String[] options = { "Minimize window", "Shutdown bot", "Go back" };
				final int response = JOptionPane.showOptionDialog(
					null, 
					"What do you want to do?", 
					"You're trying to close the window!", 
					JOptionPane.YES_NO_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE, 
					null, 
					options, 
					null
				);
					
				switch (response) {
					// Minimize window
					case 0: hideToTray(); break;
					// Shutdown the bot
					case 1: Bot.shutdown(); break;
				}
			}
		});

		// Text Area
		this.text_field = new JTextArea();
		this.text_field.setLayout(null);
		this.text_field.setEditable(false);
		this.text_field.setBackground(Color.WHITE);
		this.text_field.setVisible(true);
		this.text_field.setFont(new Font("Yu Gothic UI", Font.BOLD, 12));
		this.text_field.setTabSize(4);
		this.text_field.setFocusable(true);
		this.text_field.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				final int keycode = e.getExtendedKeyCode();
				if (keycode == KeyEvent.VK_INSERT) {
					hideToTray();
				}
				if (keycode == KeyEvent.VK_ESCAPE || keycode == KeyEvent.VK_BACK_SPACE) {
					final String[] options = { "Yes", "No" };
					final int response = JOptionPane.showOptionDialog(
						null,
						"Are you sure you want to clear the log?",
						"Clear log?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						null
					);

					if (response == 0) { clear(); }
				}
			}
		});

		// Scroll Pane
		final JScrollPane scroll_pane = new JScrollPane(this.text_field);
		scroll_pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll_pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll_pane.setPreferredSize(new Dimension(600, 400));
		scroll_pane.setFocusable(false);

		// System tray
		if (SystemTray.isSupported()) {
			final SystemTray tray = SystemTray.getSystemTray();
			final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
			final PopupMenu menu = new PopupMenu();

			tray_icon = new TrayIcon(image, "discord bot", menu);

			tray_icon.addActionListener(a -> {
				this.frame.setVisible(true);
				tray.remove(tray_icon);
			});
		} else {
			tray_icon = null;
		}

		this.frame.add(scroll_pane, BorderLayout.CENTER);
		
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		
		running = true;
		update();
	}

	/** Hides the frame and creates a system tray item */
	public void hideToTray() {
		if (SystemTray.isSupported()) {
			final SystemTray tray = SystemTray.getSystemTray();
			try {
				tray.add(tray_icon);
				this.frame.setVisible(false);
			} catch (Exception e) {
				logError(e);
			}
		}
	}

	/** Disposes and stops the frame from running */
	public void kill() {
		running = false;
		this.frame.dispose();
		if (thread != null) { thread.interrupt(); }
	}
	
	/** Updates the title of the frame */
	private void update() {
		if (thread != null) { return; }
		thread = new Thread(() -> {
			final Runtime rt = Runtime.getRuntime();
			while (running) {
				final long total = rt.totalMemory();
				final long free = rt.freeMemory();
				final long used = total - free;
				
				final double used_MB = (double) used / (1024L * 1024L);
				final double total_MB = (double) rt.maxMemory() / (1024L * 1024L);

				final String usage = String.format("%.2fMB / %.2fMB (%.2f%%)", used_MB, total_MB, (used_MB / total_MB) * 100f);

				this.frame.setTitle(String.format("bpsl - control panel | %s", usage));

				if (tray_icon != null) {
					tray_icon.setToolTip("bspl | " + usage);
				}
				try {
					Thread.sleep(2500L);
				} catch (Exception e) {}
			}
		});
		thread.start();
	}

	/** Clears the contents of the text field. */
	public void clear() {
		this.text_field.selectAll();
		this.text_field.replaceSelection("");
	}

	/** Logs the given message to the text field. 
	 *	If the this.frame is {@code null}, the message is printed to the console instead.
	 *	@param message The message to be logged. */
	public void log(String message) {
		if (this.frame == null) { System.out.println(message); return; }
		this.text_field.append(message);
		this.text_field.append("\n");
		this.text_field.setCaretPosition(this.text_field.getDocument().getLength()); 
	}

	/** Logs the exception stack trace to the text field.
	 *	@param e the exception object whose stack trace is to be logged. */
	public void logError(Exception e) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		log(sw.toString());
	}

	/** Logs the formatted message to the text field.
	 *	@param message The format string.
	 *	@param args The arguments to be formatted and inserted into the message. */
	public void logf(String message, Object... args) {
		log(String.format(message, args));
	}

	/** Creates a new Log instance with the specified title.
	 *	@param title The title of the new Log instance
	 *	@return A new Log instance with the specified title */
	public Log makeLog(String title) {
		return new Log(title);
	}

	/** This class represents a log message with a given title and a list of fields to log. */
	public static final class Log {
		private final String title;
		private final List<Field> fields;

		/** Constructor for a log object 
		 *	@param title The title of the log */
		private Log(String title) {
			this.title	= title;
			this.fields	= new ArrayList<>();
		}

		/** Adds a new field to the log with a given key and value.
		 *	@param key The key of the field
		 *	@param value The value of the field
		 *	@return The updated Log object */
		public Log addField(String key, String value) {
			this.fields.add(new Field(key, value));
			return this;
		}

		/** Adds a new field to the log with a given key and value.
		 *	@param key The key of the field
		 *	@param value The integer value of the field
		 *	@return The updated Log object */
		public Log addField(String key, int value) {
			this.addField(key, "" + value);
			return this;
		}

		/** Adds a new field to the log with a given key and value.
		 *	@param key The key of the field
		 *	@param value The long value of the field
		 *	@return The updated Log object */
		public Log addField(String key, long value) {
			this.addField(key, "" + value);
			return this;
		}

		/** Adds a new field to the log with a given key and value.
		 *	@param key The key of the field
		 *	@param value The float value of the field
		 *	@return The updated Log object */
		public Log addField(String key, float value) {
			this.addField(key, String.format("%.2f", value));
			return this;
		}

		/** Adds a new field to the log with a given key and value.
		 *	@param key The key of the field
		 *	@param value The double value of the field
		 *	@return The updated Log object */
		public Log addField(String key, double value) {
			this.addField(key, String.format("%.2f", value));
			return this;
		}

		/** Logs the current state of the Log object.
		 *	This is a terminal action. */
		public void logIt() {
			Bot.getControlPanel().logf(
				"[%s]: <%s>%s",
				Formatter.getTimestamp(),
				this.title,
				this.getTree()
			);
		}

		/** Generates a tree string representation of the fields added to the log.
		 *	@return The tree string representation of the fields added to the log */
		private String getTree() {
			if (fields.isEmpty()) { return ""; }
			final StringBuilder tree = new StringBuilder();
			for (int i = 0; i < fields.size() - 1; ++i) {
				final Field field = fields.get(i);
				tree.append("\n\t")
					.append("┣ ")
					.append(field.key)
					.append('=')
					.append(field.value);
			}
			final Field last_field = fields.get(fields.size() - 1);
			tree.append("\n\t")
				.append("┗ ")
				.append(last_field.key)
				.append('=')
				.append(last_field.value);
			return tree.toString();
		}

		/** A class representing a key-value field in a log. */
		private static final class Field {

			public final String key;
			public final String value;

			/** Constructor for a field 
			 *	@param key The key of the field
			 *	@param value The value of the field */
			private Field(String key, String value) {
				this.key	= key;
				this.value	= value;
			}

		}
		
	}

}
