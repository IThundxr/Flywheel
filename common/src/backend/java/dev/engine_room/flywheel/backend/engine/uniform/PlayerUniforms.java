package dev.engine_room.flywheel.backend.engine.uniform;

import java.nio.ByteBuffer;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.api.backend.RenderContext;
import dev.engine_room.flywheel.backend.FlwBackendXplat;
import dev.engine_room.flywheel.backend.mixin.AbstractClientPlayerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

public final class PlayerUniforms implements FlwUniform {
	private static final int UBO_SIZE = new Std140SizeCalculator()
			.putVec3()  // eyePos
			.putVec4()  // teamColor
			.putVec2()  // eyeBrightness
			.putFloat() // heldLight
			.putInt()   // playerEyeInFluid
			.putInt()   // playerEyeInBlock
			.putInt()   // playerCrouching
			.putInt()   // playerSleeping
			.putInt()   // playerSwimming
			.putInt()   // playerFallFlying
			.putInt()   // shiftKeyDown
			.putInt()   // gameMode
			.get();

	public static final PlayerUniforms INSTANCE = new PlayerUniforms();

	private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocateDirect(UBO_SIZE);

	private final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
			() -> "Flw Player UBO",
			GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM,
			UBO_SIZE
	);

	private PlayerUniforms() {
	}

	@Override
	public String getUniformName() {
		return "_FlwPlayerUniforms";
	}

	@Override
	public GpuBuffer getBuffer() {
		return buffer;
	}

	@Override
	public void close() {
		buffer.close();
	}

	public void update(RenderContext context) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), EMPTY_BUFFER);
			return;
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			Std140Builder builder = Std140Builder.onStack(stack, UBO_SIZE);

			PlayerInfo info = ((AbstractClientPlayerAccessor) player).flywheel$getPlayerInfo();

			Vec3 eyePos = player.getEyePosition(context.partialTick());
			builder.putVec3((float) eyePos.x, (float) eyePos.y, (float) eyePos.z);

			putTeamColor(builder, info == null ? null : info.getTeam());

			putEyeBrightness(builder, player);

			putHeldLight(builder, player);
			putEyeIn(builder, player);

			builder.putInt(player.isCrouching() ? 1 : 0);
			builder.putInt(player.isSleeping() ? 1 : 0);
			builder.putInt(player.isSwimming() ? 1 : 0);
			builder.putInt(player.isFallFlying() ? 1 : 0);

			builder.putInt(player.isShiftKeyDown() ? 1 : 0);

			builder.putInt(info == null ? 0 : info.getGameMode().getId());

			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), builder.get());
		}
	}

	private static void putTeamColor(Std140Builder builder, @Nullable PlayerTeam team) {
		if (team != null) {
			team.getColor().ifPresentOrElse(teamColor -> {
				int color = teamColor.rgb();
				int red = ARGB.red(color);
				int green = ARGB.green(color);
				int blue = ARGB.blue(color);
				builder.putVec4(red / 255f, green / 255f, blue / 255f, 1f);
			}, () -> builder.putVec4(1f, 1f, 1f, 1f));
		} else {
			builder.putVec4(1f, 1f, 1f, 0f);
		}
	}

	private static void putEyeBrightness(Std140Builder builder, LocalPlayer player) {
		Level level = player.level();
		int blockBrightness = level.getBrightness(LightLayer.BLOCK, player.blockPosition());
		int skyBrightness = level.getBrightness(LightLayer.SKY, player.blockPosition());
		int maxBrightness = LightEngine.MAX_LEVEL;

		builder.putVec2(
			(float) blockBrightness / (float) maxBrightness,
			(float) skyBrightness / (float) maxBrightness
		);
	}

	private static void putHeldLight(Std140Builder builder, LocalPlayer player) {
		int heldLight = 0;

		for (InteractionHand hand : InteractionHand.values()) {
			Item handItem = player.getItemInHand(hand).getItem();
			if (handItem instanceof BlockItem blockItem) {
				Block block = blockItem.getBlock();
				int blockLight = FlwBackendXplat.INSTANCE
						.getLightEmission(block.defaultBlockState(), player.level(), player.blockPosition());
				if (heldLight < blockLight) {
					heldLight = blockLight;
				}
			}
		}

		builder.putFloat((float) heldLight / 15);
	}

	private static void putEyeIn(Std140Builder builder, LocalPlayer player) {
		Level level = player.level();
		Vec3 eyePos = player.getEyePosition();
		BlockPos blockPos = BlockPos.containing(eyePos);
		FlwUniform.putInFluidAndBlock(builder, level, blockPos, eyePos);
	}
}
