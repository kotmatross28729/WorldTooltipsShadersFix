package ninja.genuine.tooltips;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLModDisabledEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property.Type;
import ninja.genuine.tooltips.client.RenderEvent;
import ninja.genuine.tooltips.system.Tooltip;

@Mod(modid = WorldTooltips.MODID, name = WorldTooltips.NAME, version = WorldTooltips.VERSION, canBeDeactivated = true, useMetadata = true, guiFactory = "ninja.genuine.tooltips.client.TooltipsGuiFactory")
public class WorldTooltips {

	@Instance(WorldTooltips.MODID)
	public static WorldTooltips instance;
	public static Configuration config;
	public static final String MODID = "world-tooltips";
	public static final String NAME = "World-Tooltips";
	public static final String VERSION = "1.2.3-86" + " kotmatross edition";
	public static final String DESC = "Choose a color in hexidecimal (ie: 0xAB12cd or #AB12cd) \nYou can look up your favorite colors online.";
	public static final String GUIID = "worldtooltipsgui";
	public static int colorBackground, overrideOutlineColor;
	public static float alpha, maxDistance; // maxDistance - 2F = 1 block
	public static boolean hideModName, overrideOutline;
	private static boolean enabled = false;
	public static int ticksDelay;
	public static boolean ticksDelayReset = true;
    public static boolean enableMaxDistanceMethod = false;
    public static boolean isBetterTooltipsLoaded;

    public boolean client = FMLLaunchHandler.side().isClient();
	public RenderEvent events;

	public WorldTooltips() {
		instance = this;
	}

	@EventHandler
	public void pre(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile(), VERSION);
		enabled = config.get("Appearance", "Enable Mod", true, "Enable rendering the tooltips.").getBoolean();
		syncConfig();

        isBetterTooltipsLoaded = (Loader.isModLoaded("BetterTooltipBox")  );
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
        if(client) {
            events = new RenderEvent();
            Tooltip.init();
            if (enabled)
                enable();
            FMLCommonHandler.instance().bus().register(this);
        }
	}

	@EventHandler
	public void post(FMLPostInitializationEvent event) {
        if(client) {
            events.post();
        }
	}

	public void enable() {
        if(client) {
            MinecraftForge.EVENT_BUS.register(events);
            enabled = true;
        }
	}

	@EventHandler
	public void disable(FMLModDisabledEvent event) {
        if(client) {
            MinecraftForge.EVENT_BUS.unregister(events);
            enabled = false;
        }
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.modID.equals(MODID)) {
			if (event.configID.equals(GUIID)) {
				boolean tmp = enabled;
				enabled = config.get("Appearance", "Enable Mod", true, "Enable rendering the tooltips.").getBoolean();
				if (tmp != enabled) {
					if (enabled)
						enable();
					else
						disable(null);
				}
				syncConfig();
				events.syncColors();
			}
		}
	}

	private void syncConfig() {
		hideModName = config.getBoolean("Hide Mod Name", "Appearance", false, "Hide mod names on tooltips.");
		maxDistance = config.getFloat("Maximum Draw Distance", "Appearance", 10.0F, 2.0F, 64.0F, "Set the maximum distance that tooltips should be displayed from. Requires \"Enable maxDistance Method\" ");
		ticksDelay  = config.getInt("Ticks Delay Before Render", "Appearance", 10, 0, 2147483647, "Delay (in ticks) before rendering the tooltip.");
		ticksDelayReset = config.getBoolean("Ticks Delay Reset", "Appearance", true, "Whether to reset the tick counter if the player isn't looking at the item. If true, then wait for the delay every time.");
		enableMaxDistanceMethod  = config.getBoolean("Enable maxDistance (Maximum Draw Distance) Method", "Appearance", false, "If enabled, the render method will switch to the old getMouseOver() system, this will allow the Maximum Draw Distance config option to be used, but this means tooltips will be rendered through walls.");
        overrideOutline = config.getBoolean("Override Outline", "Appearance", false, "If enabled, outline color will be manually set instead of default behavior.");
		alpha = config.getFloat("Transparency", "Appearance", 0.8F, 0.0F, 1.0F, "Set the opacity for the tooltips; 0 being completely invisible and 1 being completely opaque.");
		try {
			colorBackground = Integer.decode(config.get("Appearance", "Background Color", "0x100010", DESC, Type.COLOR).getString());
		} catch (NumberFormatException e) {
			colorBackground = 0x100010;
		}
		try {
			overrideOutlineColor = Integer.decode(config.get("Appearance", "Outline Color", "0x5000FF", DESC, Type.COLOR).getString());
		} catch (NumberFormatException e) {
			overrideOutlineColor = 0x5000FF;
		}
		config.save();
	}
}
