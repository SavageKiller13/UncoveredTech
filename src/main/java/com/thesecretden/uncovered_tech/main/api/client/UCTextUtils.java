package com.thesecretden.uncovered_tech.main.api.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class UCTextUtils {
    public static MutableComponent applyFormat(Component component, ChatFormatting... colour) {
        Style style = component.getStyle();
        for (ChatFormatting format : colour)
            style = style.applyFormat(format);
        return component.copy().setStyle(style);
    }
}
