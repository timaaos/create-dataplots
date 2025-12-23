package ru.timaaos.ui;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;

import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import ru.timaaos.blocks.DashboardBlockEntity;
import ru.timaaos.network.DashboardPackets;

public class DashboardScreen extends AbstractSimiScreen {

    private enum Tab {
        DISPLAY, CHART, STYLES
    }

    private final DashboardBlockEntity be;
    private Tab currentTab = Tab.DISPLAY;

    private ScrollInput width;
    private ScrollInput height;
    private Label widthLabel;
    private Label heightLabel;
    // Pick an existing Create texture for now
    private final AllGuiTextures background = AllGuiTextures.STATION;

    public DashboardScreen(DashboardBlockEntity be) {
        super(Text.literal("Dashboard"));
        this.be = be;
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight());
        super.init();

        clearChildren();

        int x = guiLeft;
        int y = guiTop;

        // --- Tabs (placeholder icons) ---
        addDrawableChild(new IconButton(x + 8, y + 6, AllGuiTextures.BUTTON)
                .withCallback(() -> switchTab(Tab.DISPLAY)));

        addDrawableChild(new IconButton(x + 28, y + 6, AllGuiTextures.BUTTON)
                .withCallback(() -> switchTab(Tab.CHART)));

        addDrawableChild(new IconButton(x + 48, y + 6, AllGuiTextures.BUTTON)
                .withCallback(() -> switchTab(Tab.STYLES)));

            widthLabel = new Label(x + 25, y + 45, Text.literal("")).withShadow();
            heightLabel = new Label(x + 25, y + 75, Text.literal("")).withShadow();

            width = new ScrollInput(x + 20, y + 40, 120, 18)
                    .withRange(1, 8)
                    .titled(Text.literal("Width"))
                    .setState(be.plotWidth).writingTo(widthLabel);

            height = new ScrollInput(x + 20, y + 70, 120, 18)
                    .withRange(1, 8)
                    .titled(Text.literal("Height"))
                    .setState(be.plotHeight).writingTo(heightLabel);
            addDrawableChild(widthLabel);
            addDrawableChild(heightLabel);
            addDrawableChild(width);
            addDrawableChild(height);

        addDrawableChild(
                new IconButton(
                        guiLeft + background.getWidth() - 33,
                        guiTop + background.getHeight() - 24,
                        AllIcons.I_CONFIRM
                ).withCallback(this::confirm)
        );
    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
        init();
    }
    private void confirm() {
        if (width == null || height == null) {
            close();
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(be.getPos());
        // Use +1 because SelectionScrollInput is 0-indexed (0-7), but you want (1-8)
        buf.writeInt(width.getState() + 1);
        buf.writeInt(height.getState() + 1);

        ClientPlayNetworking.send(DashboardPackets.CONFIG, buf);
        close();
    }
    @Override
    public void close() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(be.getPos());
        buf.writeInt(width.getState());
        buf.writeInt(height.getState());

        ClientPlayNetworking.send(DashboardPackets.CONFIG, buf);
        super.close();
    }

    private void send() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(be.getPos());
        buf.writeInt(width.getState());
        buf.writeInt(height.getState());

        ClientPlayNetworking.send(DashboardPackets.CONFIG, buf);
    }

    @Override
    protected void renderWindow(
            DrawContext context,
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        background.render(context, guiLeft, guiTop);

        context.drawText(
                textRenderer,
                Text.literal("Display Settings"),
                guiLeft + 20,
                guiTop + 25,
                0x404040,
                false
        );
    }
}
