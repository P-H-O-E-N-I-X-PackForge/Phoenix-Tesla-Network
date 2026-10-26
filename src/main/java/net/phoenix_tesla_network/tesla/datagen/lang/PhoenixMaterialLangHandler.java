package net.phoenix_tesla_network.tesla.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class PhoenixMaterialLangHandler {

    public static void init(RegistrateLangProvider provider) {

    }

    private static String formatName(String name) {
        String[] parts = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private static void addMaterialLang(RegistrateLangProvider provider, String id, String name) {
        provider.add("material.phoenix_tesla_network." + id, name);
    }
}
