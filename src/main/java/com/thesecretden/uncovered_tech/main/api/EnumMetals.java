package com.thesecretden.uncovered_tech.main.api;

import java.util.Locale;

public enum EnumMetals {

    COPPER(Type.VANILLA_NO_NUGGET, 0.3f),
    ALUMINIUM(0.2f),
    LEAD(0.5f),
    SILVER(0.7f),
    NICKEL(0.6f),
    IRON(Type.VANILLA, 0.7F),
    GOLD(Type.VANILLA, 1.0f),
    INVAR(Type.UC_ALLOY, Float.NaN);

    private final Type type;
    public final float smeltingXP;

    EnumMetals(Type t, float xp) {
        this.type = t;
        this.smeltingXP = xp;
    }

    EnumMetals(float xp) {
        smeltingXP = xp;
        this.type = Type.UC_PURE;
    }

    public boolean isVanillaMetal() {
        return type == Type.VANILLA || type == Type.VANILLA_NO_NUGGET;
    }

    public boolean isAlloy() {
        return type == Type.UC_ALLOY;
    }

    public boolean shouldAddOre() {
        return !isVanillaMetal() && !isAlloy();
    }

    public boolean shouldAddNugget() {
        return !isVanillaMetal() || type == Type.VANILLA_NO_NUGGET;
    }

    public String tagName() {
        return name().toLowerCase(Locale.US);
    }

    private enum Type {
        VANILLA,
        VANILLA_NO_NUGGET,
        UC_PURE,
        UC_ALLOY
    }
}
