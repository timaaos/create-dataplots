package ru.timaaos.blocks;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.apache.logging.log4j.core.util.Patterns;
import ru.timaaos.CreateDataAndPlots;
import net.minecraft.nbt.NbtCompound;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;

public class DashboardBlockEntity extends SmartBlockEntity {
    private int[] barHeights = new int[]{5, 8, 2, 9};  // Example data for the bar plot
    private int[][] barColors = new int[][]{{60,60,255},{255,60,60},{60,255,60},{60,60,60}};
    private String[] barNames = new String[]{"block.minecraft.grass_block", "block.minecraft.gold_ore", "item.minecraft.diamond", "item.minecraft.iron_ingot"};
    public boolean isController;
    public int xSize;
    public ScrollValueBehaviour plotHeight;

    public DashboardBlockEntity(BlockPos pos, BlockState state) {
        super(CreateDataAndPlots.DASHBOARD_BLOCK_ENTITY, pos, state);
        isController = false;
        xSize = 1;
    }
    public int[] getBarHeights() {
        return barHeights;
    }
    public int[][] getBarColors() {return barColors; }

    public ItemStack getItemForLabel(int index){
        if(index >= barNames.length){
            return new ItemStack(Registries.ITEM.get(new Identifier("minecraft:air")), 1);
        }
        String[] p = barNames[index].split(Pattern.quote("."));
        String id = p[1] + ":" + p[2];
        Item item = Registries.ITEM.get(new Identifier(id));
        ItemStack stack = new ItemStack(item, 1);
        return stack;
    }

    public void setBarHeights(int[] bh) {
        barHeights = bh;
        /*barColors = new int[barHeights.length][];
        Random random = new Random();
        for (int i = 0; i < barHeights.length; i++) {
            barColors[i] = new int[]{random.nextInt(255),random.nextInt(255),random.nextInt(255)};
        }*/
    }
    public void setBarColors(int[][] bc){
        barColors = bc;
    }

    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        plotHeight =
                new ScrollValueBehaviour(Lang.translateDirect("dashboard.plot_height"), this, new ValueBoxTransform.Sided() {
                    @Override
                    protected Vec3d getSouthLocation() {
                        return direction == Direction.UP ? Vec3d.ZERO : VecHelper.voxelSpace(8, 6, 15.5);
                    }
                })
                        .between(1, 8)
                        .withFormatter(i -> i == 0 ? "*" : String.valueOf(i));
        behaviours.add(plotHeight);
    }

    public void setBarNames(String[] bn){
        barNames = bn;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    protected void read(NbtCompound tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        this.barHeights = tag.getIntArray("BarHeights");
        int[][] r = new int[tag.getInt("BarCount")][];
        for (int i = 0; i < tag.getInt("BarCount"); i++) {
            r[i] = tag.getIntArray("BarColors"+Integer.toString(i));
        }
        barColors = r;
        this.barNames = tag.getString("BarNames").split(",");
    }

    @Override
    protected void write(NbtCompound tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putIntArray("BarHeights", barHeights);
        tag.putInt("BarCount", barHeights.length);
        if(this.barColors.length > 0){
            for (int i = 0; i < this.barHeights.length; i++) {
                tag.putIntArray("BarColors"+Integer.toString(i), barColors[i]);
            }
        }
        tag.putString("BarNames", String.join(",", this.barNames));
    }


}
