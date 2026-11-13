package net.phoenix_tesla_network.tesla.datagen;

import net.phoenix_tesla_network.tesla.datagen.lang.PhoenixLangHandler;
import net.phoenix_tesla_network.tesla.datagen.lang.PhoenixMachineLangHandler;
import net.phoenix_tesla_network.tesla.datagen.lang.PhoenixMaterialLangHandler;
import net.phoenix_tesla_network.tesla.datagen.lang.PhoenixUiLangHandler;

import com.tterrag.registrate.providers.ProviderType;

import static net.phoenix_tesla_network.tesla.common.registry.PhoenixRegistration.REGISTRATE;

public class PhoenixTeslaDatagen {

    public static void init() {
        REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixLangHandler::init);
        REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixMachineLangHandler::init);
        REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixMaterialLangHandler::init);
        REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixUiLangHandler::init);
    }
}
