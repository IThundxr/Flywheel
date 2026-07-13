package dev.engine_room.flywheel.impl.mixin;

import java.util.Iterator;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.impl.FlwImplXplat;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {
	@Shadow
	private @Nullable ClientLevel level;

	@Inject(method = "allChanged", at = @At("RETURN"))
	private void flywheel$reload(CallbackInfo ci) {
		if (level != null) {
			FlwImplXplat.INSTANCE.dispatchReloadLevelRendererEvent(level);
		}
	}

	@ModifyExpressionValue(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;"))
	private Iterator<Entity> flywheel$decideNotToRenderEntity(Iterator<Entity> original) {
		return Iterators.filter(original, entity -> !(VisualizationManager.supportsVisualization(entity.level()) &&
				VisualizationHelper.skipVanillaRender(entity)));
	}
}
