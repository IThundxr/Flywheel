package com.jozufozu.flywheel.mixin.light;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.lib.util.RenderWork;
import com.jozufozu.flywheel.light.LightUpdater;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;

@Mixin(ClientPacketListener.class)
public class NetworkLightUpdateMixin {
	@Inject(at = @At("TAIL"), method = "handleLightUpdatePacket")
	private void flywheel$onLightPacket(ClientboundLightUpdatePacket packet, CallbackInfo ci) {
		RenderWork.enqueue(() -> {
			ClientLevel level = Minecraft.getInstance().level;

			if (level == null) return;

			int chunkX = packet.getX();
			int chunkZ = packet.getZ();

			LightUpdater.get(level)
					.onLightPacket(chunkX, chunkZ);
		});
	}
}
