package dev.engine_room.flywheel.backend.engine.uniform;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public interface FlwUniform extends AutoCloseable {
	String getUniformName();

	GpuBuffer getBuffer();

	@Override
	void close();

	static void putInFluidAndBlock(Std140Builder builder, Level level, BlockPos blockPos, Vec3 pos) {
		FluidState fluidState = level.getFluidState(blockPos);
		BlockState blockState = level.getBlockState(blockPos);
		float height = fluidState.getHeight(level, blockPos);

		if (fluidState.isEmpty()) {
			builder.putInt(0);
		} else if (pos.y < blockPos.getY() + height) {
			// TODO: handle custom fluids via defines
			if (fluidState.is(FluidTags.WATER)) {
				builder.putInt(1);
			} else if (fluidState.is(FluidTags.LAVA)) {
				builder.putInt(2);
			} else {
				builder.putInt(-1);
			}
		}

		if (blockState.isAir()) {
			builder.putInt(0);
		} else {
			// TODO: handle custom blocks via defines
			if (blockState.is(Blocks.POWDER_SNOW)) {
				builder.putInt(0);
			} else {
				builder.putInt(-1);
			}
		}
	}
}
