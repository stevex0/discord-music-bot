package xs.bspl.util;

import java.awt.Color;

/** A utility class for generating color values */
public class ColorScheme {

	private ColorScheme() {} // Private constructor to prevent instantiation of the class


	/** Returns an integer value representing the color red used to indicate errors.
     *	@return The integer value representing the error color. */
	public static int error() {
		return 0xe42a1a;
	}

	/** Returns a randomly generated color value.
     *	@return A integer value representing a randomly generated color. */
	public static int color() {
		final int random = (int) (Math.random() * 361);
		return Color.HSBtoRGB(random / 360f, 48 / 100f, 94 / 100f);
	}
	
}
