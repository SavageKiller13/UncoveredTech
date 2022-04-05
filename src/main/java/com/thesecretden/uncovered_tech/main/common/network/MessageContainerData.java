package com.thesecretden.uncovered_tech.main.common.network;

import com.mojang.datafixers.util.Pair;
import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.common.gui.UCBaseContainer;
import com.thesecretden.uncovered_tech.main.common.gui.sync.GenericDataSerializers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class MessageContainerData implements IMessage {
    private final List<Pair<Integer, GenericDataSerializers.DataPair<?>>> synced;

    public MessageContainerData(List<Pair<Integer, GenericDataSerializers.DataPair<?>>> synced) {
        this.synced = synced;
    }

    public MessageContainerData(FriendlyByteBuf buf) {
        this(UCPacketUtils.readList(buf, pb -> Pair.of(pb.readVarInt(), GenericDataSerializers.read(pb))));
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        UCPacketUtils.writeList(buf, synced, (pair, b) -> {
            b.writeVarInt(pair.getFirst());
            pair.getSecond().write(b);
        });
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> ctx) {
        AbstractContainerMenu currentContainer = UncoveredTech.proxy.getClientPlayer().containerMenu;
        if (!(currentContainer instanceof UCBaseContainer<?> ucContainer))
            return;
        ucContainer.receiveSync(synced);
    }
}
