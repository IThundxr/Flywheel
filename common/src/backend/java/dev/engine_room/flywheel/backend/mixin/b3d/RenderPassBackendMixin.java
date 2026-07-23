package dev.engine_room.flywheel.backend.mixin.b3d;

import org.spongepowered.asm.mixin.Mixin;

import com.mojang.blaze3d.systems.RenderPassBackend;

import dev.engine_room.flywheel.backend.extension.b3d.FlwRenderPassExtension;

// This just so RenderPassBackend extends our interface
@Mixin(RenderPassBackend.class)
public interface RenderPassBackendMixin extends FlwRenderPassExtension {
}
