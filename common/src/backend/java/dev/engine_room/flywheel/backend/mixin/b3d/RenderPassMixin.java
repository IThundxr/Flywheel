package dev.engine_room.flywheel.backend.mixin.b3d;

import org.spongepowered.asm.mixin.Mixin;

import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.backend.api.RenderPassBackend;

import dev.engine_room.flywheel.backend.extension.b3d.FlwRenderPassExtension;

// This just so RenderPass(Backend) extends our interface
@Mixin({RenderPass.class, RenderPassBackend.class})
public interface RenderPassMixin extends FlwRenderPassExtension {
}
