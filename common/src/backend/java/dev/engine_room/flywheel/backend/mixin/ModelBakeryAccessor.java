package dev.engine_room.flywheel.backend.mixin;

import net.minecraft.client.resources.model.ModelBakery;

import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {
	@Accessor("BREAKING_LOCATIONS")
	static List<Identifier> flywheel$getBREAKING_LOCATIONS() {
		throw new AssertionError();
	}
}
