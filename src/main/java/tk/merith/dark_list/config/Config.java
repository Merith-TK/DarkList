package tk.merith.dark_list.config;

import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.values.ValueMap;

import java.util.HashMap;

public class Config extends WrappedConfig {

	@Comment("Enable debug mode. Used for development (default: false)")
	public final boolean debug = false;

	@Comment("Force Client to have DarkList mod installed")
	public final boolean enforceDarkList = false;
	@Comment("The modlist to send to the server. (format: modid:version,modid:version,modid:version")
	public final String modList = "example_mod:v1.0.0";
}
