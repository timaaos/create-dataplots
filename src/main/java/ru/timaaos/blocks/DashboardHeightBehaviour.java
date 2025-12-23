package ru.timaaos.blocks;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.*;
import net.createmod.catnip.math.VecHelper;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DashboardHeightBehaviour extends BlockEntityBehaviour implements ValueSettingsBehaviour {

    public static final BehaviourType<DashboardHeightBehaviour> TYPE = new BehaviourType<>();

    ValueBoxTransform slotPositioning;
    Vec3d textShift;

    int min = 1;
    protected int max = 1;
    public int value;
    public Text label;
    Consumer<Integer> callback;
    Consumer<Integer> clientCallback;
    Function<Integer, String> formatter;
    private Supplier<Boolean> isActive;
    boolean needsWrench = true;

    public DashboardHeightBehaviour(Text label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(be);
        this.setLabel(label);
        slotPositioning = slot;
        callback = i -> {
        };
        clientCallback = i -> {
        };
        formatter = i -> Integer.toString(i);
        value = 1;
        isActive = () -> true;
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(NbtCompound nbt, boolean clientPacket) {
        nbt.putInt("PlotHeight", value);
        super.write(nbt, clientPacket);
    }

    @Override
    public void read(NbtCompound nbt, boolean clientPacket) {
        value = nbt.getInt("PlotHeight");
        super.read(nbt, clientPacket);
    }

    public DashboardHeightBehaviour withClientCallback(Consumer<Integer> valueCallback) {
        clientCallback = valueCallback;
        return this;
    }

    public DashboardHeightBehaviour withCallback(Consumer<Integer> valueCallback) {
        callback = valueCallback;
        return this;
    }

    public DashboardHeightBehaviour between(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public DashboardHeightBehaviour requiresWrench() {
        this.needsWrench = true;
        return this;
    }

    public DashboardHeightBehaviour withFormatter(Function<Integer, String> formatter) {
        this.formatter = formatter;
        return this;
    }

    public DashboardHeightBehaviour onlyActiveWhen(Supplier<Boolean> condition) {
        isActive = condition;
        return this;
    }

    public void setValue(int value) {
        value = MathHelper.clamp(value, min, max);
        if (value == this.value)
            return;
        this.value = value;
        callback.accept(value);
        blockEntity.markDirty();
        blockEntity.sendData();
    }

    public int getValue() {
        return value;
    }

    public String formatValue() {
        return formatter.apply(value);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean isActive() {
        return isActive.get();
    }

    @Override
    public boolean testHit(Vec3d hit) {
        BlockState state = blockEntity.getCachedState();
        BlockPos pos = blockEntity.getPos();
        // Use blockEntity.getWorld() to get the level
        Vec3d localHit = hit.subtract(Vec3d.of(pos));

        // Add the missing arguments: level and pos
        return slotPositioning.testHit(blockEntity.getWorld(), pos, state, localHit);
    }

    public void setLabel(Text label) {
        this.label = label;
    }

    public static class StepContext {
        public int currentValue;
        public boolean forward;
        public boolean shift;
        public boolean control;
    }

    @Override
    public ValueBoxTransform getSlotPositioning() {
        return slotPositioning;
    }

    @Override
    public ValueSettingsBoard createBoard(PlayerEntity player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(label, max, 1, ImmutableList.of(Text.literal("Height")),
                new ValueSettingsFormatter(ValueSettings::format));
    }

    @Override
    public void setValueSettings(PlayerEntity player, ValueSettings valueSetting, boolean ctrlDown) {
        if (valueSetting.row() != 0) return; // Add this line!
        if (valueSetting.equals(getValueSettings()))
            return;
        setValue(valueSetting.value());
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, value);
    }

    @Override
    public boolean onlyVisibleWithWrench() {
        return needsWrench;
    }

    @Override
    public void onShortInteract(PlayerEntity player, Hand hand, Direction side, BlockHitResult hitResult) {
        if (player instanceof FakePlayer)
            blockEntity.getCachedState()
                    .onUse(getWorld(), player, hand,
                            new BlockHitResult(VecHelper.getCenterOf(getPos()), side, getPos(), true));
    }

}
