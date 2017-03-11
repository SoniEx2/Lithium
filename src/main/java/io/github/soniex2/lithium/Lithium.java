package io.github.soniex2.lithium;

import io.github.soniex2.lithium.api.CapabilityLithium;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

/**
 * @author soniex2
 */
@Mod(modid = "lithium", name = "Lithium API", version = "1.1.0")
public class Lithium {
	public static Logger logger;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		CapabilityLithium.register();
		MinecraftForge.EVENT_BUS.register(new LithiumEvents());
	}
}
