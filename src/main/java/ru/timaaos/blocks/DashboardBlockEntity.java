package ru.timaaos.blocks;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import ru.timaaos.CreateDataAndPlots;
import net.minecraft.nbt.NbtCompound;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

public class DashboardBlockEntity extends SmartBlockEntity {
    private int[] barHeights = new int[]{5, 8, 2, 9};  // Example data for the bar plot
    private int[][] barColors = new int[][]{{60,60,255},{255,60,60},{60,255,60},{60,60,60}};
    private String[] barNames = new String[]{"block.minecraft.grass_block", "block.minecraft.gold_ore", "item.minecraft.diamond", "item.minecraft.iron_ingot"};
    public boolean isController;
    public int plotWidth = 1;
    public int plotHeight = 1;

    public DashboardBlockEntity(BlockPos pos, BlockState state) {
        super(CreateDataAndPlots.DASHBOARD_BLOCK_ENTITY, pos, state);
        isController = false;
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

    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

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
        plotWidth = tag.getInt("PlotWidth");
        plotHeight = tag.getInt("PlotHeight");
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
        tag.putInt("PlotWidth", plotWidth);
        tag.putInt("PlotHeight", plotHeight);
        tag.putString("BarNames", String.join(",", this.barNames));
    }

}
