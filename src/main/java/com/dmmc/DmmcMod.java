package com.dmmc;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DmmcMod.MOD_ID)
public class DmmcMod {

    public static final String MOD_ID = "dmmc";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public DmmcMod(IEventBus modBus) {
        AddonDataComponents.DATA_COMPONENTS.register(modBus);
        AddonItems.ITEMS.register(modBus);
    }
}
