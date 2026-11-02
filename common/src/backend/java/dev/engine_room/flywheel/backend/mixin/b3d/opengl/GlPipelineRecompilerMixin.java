package dev.engine_room.flywheel.backend.mixin.b3d.opengl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.backend.opengl.GlPipelineRecompiler;

@Mixin(GlPipelineRecompiler.class)
public class GlPipelineRecompilerMixin {
	@Unique
	private static final String CONSERVATIVE_DEPTH_EXT = "GL_ARB_conservative_depth";

	// Workaround for SPIRV-Cross emitting layout(depth_*) without the extension
	// Fixed in SPIRV-Cross in a159fc7a046569fe7b77325e24c6990a870df252
	// but seems like lwjgl hasn't updated to include that commit yet
	@ModifyExpressionValue(method = "decompileShader", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryUtil;memASCII(J)Ljava/lang/String;"))
	private static String flywheel$fixSpvcConservativeDepth(String original) {
		if (!original.contains("layout(depth_") || original.contains(CONSERVATIVE_DEPTH_EXT)) {
			return original;
		}

		int endOfVersionLine = original.indexOf('\n');
		if (endOfVersionLine < 0) {
			return original;
		}

		return original.substring(0, endOfVersionLine + 1) +
				"#extension " + CONSERVATIVE_DEPTH_EXT + " : enable\n" +
				original.substring(endOfVersionLine + 1);
	}
}
