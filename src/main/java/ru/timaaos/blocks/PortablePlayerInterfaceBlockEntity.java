package ru.timaaos.blocks;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PortablePlayerInterfaceBlockEntity extends BlockEntity implements SidedStorageBlockEntity {

    public PortablePlayerInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction face) {
        if (this.world == null || this.world.isClient) {
            return Storage.empty();
        }

        // Get the direction the block is "pointing"
        BlockState state = this.getCachedState();
        Direction pointing = state.contains(PortablePlayerInterfaceBlock.FACING)
                ? state.get(PortablePlayerInterfaceBlock.FACING)
                : Direction.UP;

        // Check for player in the 1x1 area in front of the interface
        Box detectionBox = new Box(this.pos).offset(pointing.getOffsetX(), pointing.getOffsetY(), pointing.getOffsetZ());

        List<PlayerEntity> players = this.world.getEntitiesByClass(PlayerEntity.class, detectionBox, player -> !player.isSpectator());

        if (!players.isEmpty()) {
            return PlayerInventoryStorage.of(players.get(0));
        }

        return Storage.empty();
    }
}