package net.spaceeye.someperipherals

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat

import net.minecraft.client.renderer.RenderStateShard.*
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShaderInstance

object SomePeripheralsRenderTypes {
    var projectorPixelShader: ShaderInstance? = null;
    val PROJECTOR_PIXEL_SHADER = ShaderStateShard { projectorPixelShader };

    val PROJECTOR_PIXEL = RenderType.create(
        "projector_pixel",
        DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
        VertexFormat.Mode.QUADS,
        256,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(PROJECTOR_PIXEL_SHADER)
            .setTextureState(NO_TEXTURE)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setLightmapState(LIGHTMAP)
            .createCompositeState(false)
    );

    fun register() { // Load static vars

    }
}