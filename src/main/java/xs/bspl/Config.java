package xs.bspl;

import io.github.cdimascio.dotenv.Dotenv;

/** A utility class for retrieving configuration values from environment variables or a .env file.
 *	This class uses the Dotenv library to load environment variables from a .env file if it is available. */
public class Config {

	private static Dotenv dot_env;

	private Config() {} // Private constructor to prevent instantiation of the class

	 /** Retrieves the value of the configuration variable with the specified key.
     *	@param key The key of the configuration variable
     *	@return The value of the configuration variable, or {@code null} if it is not defined */
	public static String get(String key) {
		final String var = System.getenv(key);
		if (var != null) return var;

		if (dot_env == null) {
			dot_env = Dotenv.load();
		}
		return dot_env.get(key);
	}
	
}