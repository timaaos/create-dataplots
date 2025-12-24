package ru.timaaos.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class CustomButton extends AbstractSimiWidget {

    protected ScreenElement background;
    protected ScreenElement backgroundActive;
    protected ScreenElement backgroundHover;

    public boolean tabSelect = false;
    public CustomButton(int x, int y, ScreenElement background) {
        this(x, y, 16, 16, background, background, background);
    }

    public CustomButton(int x, int y, int w, int h, ScreenElement backgroundActive, ScreenElement background, ScreenElement backgroundHover) {
        super(x, y, w, h);
        this.backgroundActive = backgroundActive;
        this.background = background;
        this.backgroundHover = backgroundHover;
    }
    @Override
    public void doRender(DrawContext context, int mouseX, int mouseY, float partialTicks) {
    }

    public void doRenderG(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        if(tabSelect){
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            backgroundActive.render(context, getX(), getY());
        }else{
            boolean isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if(isHovered){
                background.render(context, getX(), getY());
            }else{
                backgroundHover.render(context, getX(), getY());
            }
        }
    }
}