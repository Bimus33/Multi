package ru.bim;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.bim.hud.ActivePotion;
import ru.bim.hud.WaterMark;
import ru.bim.visual.TargetESP;

@Mod("multi")
public class Multi {

    private static final Logger LOGGER = LogManager.getLogger();

    public Multi() {
        // Регистрируем обработчик клиентской настройки
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new TargetESP());
        MinecraftForge.EVENT_BUS.register(new HitParticleHandler());
        MinecraftForge.EVENT_BUS.register(new WaterMark());
        MinecraftForge.EVENT_BUS.register(new ActivePotion());

        LOGGER.info("All modules registered successfully");
    }
}