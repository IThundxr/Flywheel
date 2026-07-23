package dev.engine_room.flywheel.backend.mixin.b3d;

import org.spongepowered.asm.mixin.Mixin;

import com.mojang.renderpearl.api.commands.RenderPass;

import dev.engine_room.flywheel.backend.extension.b3d.FlwRenderPassExtension;

// classtweaker interface injection is compile-time only; this makes the
// RenderPass interface actually extend our extension at runtime.
@Mixin(RenderPass.class)
public interface RenderPassInterfaceMixin extends FlwRenderPassExtension {
}
