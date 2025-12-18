package com.breakinblocks.horsepowered.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-side extension registration.
 * This class is only loaded on the client side to avoid loading client-only classes on the server.
 */
public class ClientExtensions {

    /**
     * Registers client-side mod extensions.
     * Called from the main mod class only when running on client.
     *
     * @param container The mod container
     */
    public static void register(ModContainer container) {
        // Register config screen factory for mod menu integration
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
