package com.thesecretden.uncovered_tech.main.api.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class UCItemUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static CompoundTag parseNBTFromJson(JsonElement jsonElement) throws CommandSyntaxException {
        if (jsonElement.isJsonObject())
            return TagParser.parseTag(GSON.toJson(jsonElement));
        else
            return TagParser.parseTag(jsonElement.getAsString());
    }

    public static HumanoidArm getLivingHand(LivingEntity entity, InteractionHand hand) {
        HumanoidArm handside = entity.getMainArm();
        if (hand != InteractionHand.MAIN_HAND)
            handside = handside == HumanoidArm.LEFT ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        return handside;
    }

    public static void removeTag(ItemStack stack, String key) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.remove(key);
            if (tag.isEmpty())
                stack.setTag(null);
        }
    }

    public static boolean hasTag(ItemStack stack, String key, int type) {
        return stack.hasTag() && stack.getOrCreateTag().contains(key, type);
    }
}
