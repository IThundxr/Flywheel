package com.jozufozu.flywheel.backend;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FlwBackendXplatImpl implements FlwBackendXplat {
	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getLightEmission(level, pos);
	}
}