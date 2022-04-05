package com.thesecretden.uncovered_tech.main.common.blocks;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MultiblockTEType<T extends BlockEntity & UCBlockInterfaces.IGeneralMultiblock> implements BiFunction<BlockPos, BlockState, T> {
    private final RegistryObject<BlockEntityType<T>> master;
    private final RegistryObject<BlockEntityType<T>> dummy;
    private final Predicate<BlockState> isMaster;

    public MultiblockTEType(String name, DeferredRegister<BlockEntityType<?>> register, TEWithTypeConstructor<T> make, Supplier<? extends Block> block, Predicate<BlockState> isMaster) {
        this.isMaster = isMaster;
        this.master = register.register(name + "_master", makeType(make, block));
        this.dummy = register.register(name + "_dummy", makeType(make, block));
    }

    @Nullable
    @Override
    public T apply(BlockPos pos, BlockState state) {
        if (isMaster.test(state))
            return master.get().create(pos, state);
        else
            return dummy.get().create(pos, state);
    }

    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeType(TEWithTypeConstructor<T> create, Supplier<? extends Block> valid) {
        return () -> {
            Mutable<BlockEntityType<T>> typeMutable = new MutableObject<>();
            BlockEntityType<T> type = new BlockEntityType<>(
                    (pos, state) -> create.create(typeMutable.getValue(), pos, state), ImmutableSet.of(valid.get()), null
            );
            typeMutable.setValue(type);
            return type;
        };
    }

    public BlockEntityType<T> master() {
        return master.get();
    }

    public BlockEntityType<T> dummy() {
        return dummy.get();
    }

    public RegistryObject<BlockEntityType<T>> dummyHolder() {
        return dummy;
    }

    public RegistryObject<BlockEntityType<T>> masterHolder() {
        return master;
    }

    public interface TEWithTypeConstructor<T extends BlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }
}
