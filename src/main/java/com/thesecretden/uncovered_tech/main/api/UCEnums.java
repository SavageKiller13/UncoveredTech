package com.thesecretden.uncovered_tech.main.api;

import com.thesecretden.uncovered_tech.main.UncoveredTech;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public class UCEnums {
    public enum IOSideConfig implements StringRepresentable {
        NONE("none"),
        INPUT("in"),
        OUTPUT("out");

        public static final IOSideConfig[] VALUES = values();

        final String texture;

        IOSideConfig(String texture) {
            this.texture = texture;
        }

        @Override
        public String getSerializedName() {
            return this.toString().toLowerCase(Locale.ROOT);
        }

        public String getTextureName() {
            return texture;
        }

        public Component getTextComponent() {
            return new TranslatableComponent(UncoveredTech.DESC_INFO + "blockSide.io." + getSerializedName());
        }

        public static IOSideConfig next(IOSideConfig current) {
            return current == INPUT ? OUTPUT : current == OUTPUT ? NONE : INPUT;
        }
    }
}
