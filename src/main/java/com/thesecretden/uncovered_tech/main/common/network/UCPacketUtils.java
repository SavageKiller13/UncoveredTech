package com.thesecretden.uncovered_tech.main.common.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class UCPacketUtils {
    public static <T> List<T> readList(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> readElement) {
        int numElements = buf.readVarInt();
        List<T> result = new ArrayList<>(numElements);
        for (int i = 0; i < numElements; i++)
            result.add(readElement.apply(buf));
        return result;
    }

    public static <T> void writeListReverse(FriendlyByteBuf buf, List<T> toWrite, BiConsumer<FriendlyByteBuf, T> writeElement) {
        writeList(buf, toWrite, (t, buffer) -> writeElement.accept(buffer, t));
    }

    public static <T> void writeList(FriendlyByteBuf buf, List<T> toWrite, BiConsumer<T, FriendlyByteBuf> writeElement) {
        buf.writeVarInt(toWrite.size());
        for (T element : toWrite)
            writeElement.accept(element, buf);
    }
}
