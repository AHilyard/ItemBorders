package com.anthonyhilyard.iceberg.util;

import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;

public class GuiHelper {
    public static void drawGradientRect(Matrix4f matrix, int z, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        float fromAlpha = (float) (colorFrom >> 24 & 255) / 255.0F;
        float fromRed = (float) (colorFrom >> 16 & 255) / 255.0F;
        float fromGreen = (float) (colorFrom >> 8 & 255) / 255.0F;
        float fromBlue = (float) (colorFrom & 255) / 255.0F;
        float toAlpha = (float) (colorTo >> 24 & 255) / 255.0F;
        float toRed = (float) (colorTo >> 16 & 255) / 255.0F;
        float toGreen = (float) (colorTo >> 8 & 255) / 255.0F;
        float toBlue = (float) (colorTo & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex(matrix, (float)x2, (float)y1, (float)z).setColor(fromRed, fromGreen, fromBlue, fromAlpha);
        bufferbuilder.addVertex(matrix, (float)x1, (float)y1, (float)z).setColor(fromRed, fromGreen, fromBlue, fromAlpha);
        bufferbuilder.addVertex(matrix, (float)x1, (float)y2, (float)z).setColor(toRed, toGreen, toBlue, toAlpha);
        bufferbuilder.addVertex(matrix, (float)x2, (float)y2, (float)z).setColor(toRed, toGreen, toBlue, toAlpha);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        
        RenderSystem.disableBlend();
    }
}