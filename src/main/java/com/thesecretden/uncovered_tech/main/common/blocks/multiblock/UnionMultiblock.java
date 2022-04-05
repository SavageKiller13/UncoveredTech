package com.thesecretden.uncovered_tech.main.common.blocks.multiblock;

import com.thesecretden.uncovered_tech.main.api.multiblock.MultiblockHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UnionMultiblock implements MultiblockHandler.IMultiblock {
    private final ResourceLocation name;
    private final List<TransformedMultiblock> parts;
    private final Supplier<Component> displayName;

    public UnionMultiblock(ResourceLocation name, List<TransformedMultiblock> parts) {
        this.name = name;
        this.parts = parts;
        this.displayName = () -> parts.stream().map(TransformedMultiblock::multiblock).map(MultiblockHandler.IMultiblock::getDisplayName).map(Component::copy).reduce((c1, c2) -> c1.append(", ").append(c2)).orElse(TextComponent.EMPTY.copy());
    }

    @Override
    public ResourceLocation getUniqueName() {
        return name;
    }

    @Override
    public boolean isBlockTrigger(BlockState state, Direction dir, @Nullable Level level) {
        return false;
    }

    @Override
    public boolean createStructure(Level level, BlockPos pos, Direction dir, Player player) {
        return false;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getStructure(@Nullable Level level) {
        Vec3i min = getMin(level);
        List<StructureTemplate.StructureBlockInfo> infoReturn = new ArrayList<>();
        for (TransformedMultiblock part : parts)
            for (StructureTemplate.StructureBlockInfo info : part.multiblock().getStructure(level))
                infoReturn.add(new StructureTemplate.StructureBlockInfo(part.toUnionCoords(info.pos).subtract(min), info.state, info.nbt));
        return infoReturn;
    }

    @Override
    public Vec3i getSize(@Nullable Level level) {
        Vec3i max = Vec3i.ZERO;
        for (TransformedMultiblock part : parts)
            max = max(max, part.toUnionCoords(part.multiblock.getSize(level)));
        Vec3i min = getMin(level);
        return new Vec3i(max.getX() - min.getX(), max.getY() - min.getY(), max.getZ() - min.getZ());
    }

    private Vec3i getMin(@Nullable Level level) {
        Vec3i min = Vec3i.ZERO;
        for (TransformedMultiblock part : parts) {
            final Vec3i size = part.multiblock.getSize(level);
            for (int factorX = 0; factorX < 2; ++factorX)
                for (int factorY = 0; factorY < 2; ++factorY)
                    for (int factorZ = 0; factorZ < 2; ++factorZ)
                        min = min(min, part.toUnionCoords(new Vec3i(size.getX() * factorX, size.getY() * factorY, size.getZ() * factorZ)));
        }
        return min;
    }

    private Vec3i min(Vec3i a, Vec3i b) {
        return new Vec3i(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
    }

    private Vec3i max(Vec3i a, Vec3i b) {
        return new Vec3i(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    @Override
    public void disassemble(Level level, BlockPos pos, boolean mirrored, Direction dir) {}

    @Override
    public BlockPos getTriggerOffset() {
        return BlockPos.ZERO;
    }

    @Override
    public Component getDisplayName() {
        return displayName.get();
    }

    public record TransformedMultiblock(MultiblockHandler.IMultiblock multiblock, Vec3i offset, Rotation rotation) {
        public BlockPos toUnionCoords(Vec3i inMultiblockCoords) {
            return StructureTemplate.calculateRelativePosition(new StructurePlaceSettings().setRotation(rotation), new BlockPos(inMultiblockCoords)).offset(offset);
        }
    }
}
