package be.woubuc.wurmunlimited.wurmmapgen;

public class Logger {
	
	/**
	 * Logs an info message to stdout
	 * @param  message    The message
	 * @param  isVerbose  True if this message should only be logged in verbose logging
	 */
	public static void info(String message, boolean isVerbose) {
		if (!isVerbose || WurmMapGen.verbose) {
			System.out.println(message);
		}
	}
	
	/**
	 * @see Logger#info
	 */
	public static void info(String message) {
		info(message, true);
	}
	
	/**
	 * Logs a warning message to stdout
	 * @param  message  The message
	 */
	public static void warn(String message) {
		info(" WARN " + message, false);
	}
	
	/**
	 * Logs an error message to stderr
	 * @param  message  The message
	 */
	public static void error(String message) {
		System.err.println("ERROR " + message);
	}
	
	/**
	 * Logs a title to stdout (in verbose mode, a title is preceded by an empty line)
	 * @param  title  The title
	 */
	public static void title(String title) {
		info("");
		info(title);
	}
	
	/**
	 * Logs an indented details message to stdout
	 * @param  message  The message
	 */
	public static void details(String message) {
		info("      " + message);
	}
	
	
	/**
	 * Logs a details message with OK label, or logs the given message when verbose mode is disabled
	 * @param  message    The message
	 * @param  isVerbose  True if this should only be logged in verbose logging
	 */
	public static void ok(String message, boolean isVerbose) {
		if (WurmMapGen.verbose) {
			info("   OK " + message);
		} else if (!isVerbose) {
			info(message, false);
		}
	}
	
	/**
	 * @see Logger#ok
	 */
	public static void ok(String message) {
		ok(message, false);
	}
	
	/**
	 * Logs a details message with a custom label
	 * @param  label      The label
	 * @param  message    The message
	 * @param  isVerbose  True if this should only be logged in verbose logging
	 */
	public static void custom(String label, String message, boolean isVerbose) {
		StringBuilder messageBuilder = new StringBuilder();
		if (label.length() < 6) {
			for (int i = 0; i < 6 - label.length(); i++) {
				messageBuilder.append(" ");
			}
		}
		messageBuilder.append(label).append(" ").append(message);
		info(messageBuilder.toString(), isVerbose);
	}
	
	/**
	 * @see Logger#custom
	 */
	public static void custom(String label, String message) {
		custom(label, message, true);
	}
}
