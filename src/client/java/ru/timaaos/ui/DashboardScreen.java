package ru.timaaos.ui;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;

import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import net.minecraft.util.Identifier;
import ru.timaaos.CreateDataAndPlots;
import ru.timaaos.blocks.DashboardBlockEntity;
import ru.timaaos.network.DashboardPackets;

import java.util.ArrayList;
import java.util.List;

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
    private Label whLabel;
    private List<CustomButton> tabButtons = new ArrayList<>();
    private static final Identifier DISPLAY_BACKGROUND = new Identifier("dataplots", "textures/gui/display_tab.png");
    private static final Identifier PLOT_BACKGROUND = new Identifier("dataplots", "textures/gui/plot_tab.png");
    private static final Identifier STYLE_BACKGROUND = new Identifier("dataplots", "textures/gui/style_tab.png");
    private static final Identifier TAB_BUTTON_ID = new Identifier("dataplots", "textures/gui/tab_button.png");
    private static final Identifier TAB_BUTTON_ACTIVE_ID = new Identifier("dataplots", "textures/gui/tab_button_active.png");
    private static final Identifier TAB_BUTTON_HOVER_ID = new Identifier("dataplots", "textures/gui/tab_button_hover.png");
    private static final ScreenElement TAB_BUTTON = new ScreenElement() {
        @Override
        public void render(DrawContext drawContext, int x, int y) {
            drawContext.drawTexture(TAB_BUTTON_ID, x,y,0,0,48,16,48,16);
        }
    };
    private static final ScreenElement TAB_BUTTON_HOVER = new ScreenElement() {
        @Override
        public void render(DrawContext drawContext, int x, int y) {
            drawContext.drawTexture(TAB_BUTTON_HOVER_ID, x,y,0,0,48,16,48,16);
        }
    };
    private static final ScreenElement TAB_BUTTON_ACTIVE = new ScreenElement() {
        @Override
        public void render(DrawContext drawContext, int x, int y) {
            drawContext.drawTexture(TAB_BUTTON_ACTIVE_ID, x,y,0,0,48,16,48,16);
        }
    };

    private final int bgWidth = 192;
    private final int bgHeight = 200;

    public DashboardScreen(DashboardBlockEntity be) {
        super(Text.translatable("dataplots.gui.dashboard.title"));
        this.be = be;
    }

    @Override
    protected void init() {
        setWindowSize(bgWidth, bgHeight);
        super.init();

        clearChildren();

        int x = guiLeft;
        int y = guiTop;

        // --- Tabs (placeholder icons) ---
        tabButtons.add(new CustomButton(x + 3, y + 16, 48, 16, TAB_BUTTON_ACTIVE, TAB_BUTTON_HOVER, TAB_BUTTON)
                .withCallback(() -> switchTab(Tab.DISPLAY)));

        tabButtons.add(new CustomButton(x + 52, y + 16, 48, 16, TAB_BUTTON_ACTIVE, TAB_BUTTON_HOVER, TAB_BUTTON)
                .withCallback(() -> switchTab(Tab.CHART)));

        tabButtons.add(new CustomButton(x + 101, y + 16, 48, 16, TAB_BUTTON_ACTIVE, TAB_BUTTON_HOVER, TAB_BUTTON)
                .withCallback(() -> switchTab(Tab.STYLES)));

        for(CustomButton cb : tabButtons){
            addRenderableWidgets(cb);
        }


            widthLabel = new Label(x + 130, y + 48, Text.literal("")).colored(0x444444);
            heightLabel = new Label(x + 153, y + 48, Text.literal("")).colored(0x444444);
            whLabel = new Label(x + 25, y + 48, Text.literal("")).withShadow();

            width = new ScrollInput(x + 128, y + 47, 16, 10)
                    .withRange(1, 17)
                    .titled(Text.translatable("dataplots.dashboard.plot_width"))
                    .setState(be.plotWidth).writingTo(widthLabel);

            height = new ScrollInput(x + 151, y + 46, 16, 10)
                    .withRange(1, 17)
                    .titled(Text.translatable("dataplots.dashboard.plot_height"))
                    .setState(be.plotHeight).writingTo(heightLabel);
            addDrawableChild(width);
            addDrawableChild(height);

        addDrawableChild(
                new IconButton(
                        guiLeft + bgWidth - 25,
                        guiTop + bgHeight - 25,
                        AllIcons.I_CONFIRM
                ).withCallback(this::close)
        );


    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
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
    public void drawCenteredText(DrawContext context, Text text, int x, int y, int width, int color) {
        int textWidth = textRenderer.getWidth(text);
        context.drawText(textRenderer, text, x + (width - textWidth) / 2, y, color, false);
    }

    @Override
    protected void renderWindow(
            DrawContext context,
            int mouseX,
            int mouseY,
            float partialTicks
    ) {

        Identifier bg = DISPLAY_BACKGROUND;
        if(currentTab == Tab.CHART) bg = PLOT_BACKGROUND;
        if(currentTab == Tab.STYLES) bg = STYLE_BACKGROUND;

        context.drawTexture(bg, guiLeft, guiTop, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);
        //background.render(context, guiLeft, guiTop);

        drawCenteredText(context, Text.translatable("dataplots.gui.dashboard.title"), guiLeft, guiTop+4, 192, 0x592424);

        int i = 0;
        for(CustomButton cb : tabButtons){
            cb.doRenderG(context,mouseX,mouseY,partialTicks);
            cb.tabSelect = currentTab == Tab.values()[i];
            cb.active = !(currentTab == Tab.values()[i]);
            i+=1;
        }
        if(currentTab == Tab.DISPLAY){
            width.visible = true;
            width.active = true;
            height.visible = true;
            height.active = true;
            heightLabel.render(context,mouseX,mouseY,partialTicks);
            widthLabel.render(context,mouseX,mouseY,partialTicks);
            context.drawText(
                    textRenderer,
                    Text.translatable("dataplots.gui.dashboard.figure_size"),
                    guiLeft + 25,
                    guiTop + 48,
                    0xFFFFFF,
                    true
            );
        }else{
            width.visible = false;
            width.active = false;
            height.visible = false;
            height.active = false;
        }
        drawCenteredText(context, Text.translatable("dataplots.gui.dashboard.tab.display"), guiLeft+3,guiTop+20,48,0xE4B763);
        drawCenteredText(context, Text.translatable("dataplots.gui.dashboard.tab.plot"), guiLeft+52,guiTop+20,48,0xE4B763);
        drawCenteredText(context, Text.translatable("dataplots.gui.dashboard.tab.style"), guiLeft+101,guiTop+20,48,0xE4B763);
    }
}
