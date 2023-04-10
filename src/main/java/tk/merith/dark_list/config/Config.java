package tk.merith.dark_list.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class Config extends MidnightConfig {
		@Entry(name = "Debug") public static boolean debug = false;
		@Entry(name = "Enforce DarkList") public static boolean enforceDarkList = true;
		@Entry(name = "BanList") public static String modList = "examplemod:1.0.0,examplemod2:1.0.0";
}
