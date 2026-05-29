package dev.engine_room.flywheel.backend.compile;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import dev.engine_room.flywheel.backend.FlwRenderPipelines;
import dev.engine_room.flywheel.backend.b3d.DeviceFeatureCompat;
import dev.engine_room.flywheel.backend.compile.core.CompilationHarness;
import dev.engine_room.flywheel.backend.compile.core.Compile;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import dev.engine_room.flywheel.backend.glsl.GlslVersion;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import net.minecraft.resources.Identifier;

public class OitPrograms {
	private static final Identifier FULLSCREEN = IdentifierUtil.id("internal/fullscreen.vert");
	static final Identifier OIT_COMPOSITE = IdentifierUtil.id("internal/oit_composite.frag");
	static final Identifier OIT_DEPTH = IdentifierUtil.id("internal/oit_depth.frag");

	private static final Compile<Identifier> COMPILE = new Compile<>();

	private final CompilationHarness<Identifier> harness;

	public OitPrograms(CompilationHarness<Identifier> harness) {
		this.harness = harness;
	}

	public static OitPrograms createFullscreenCompiler(ShaderSources sources) {
		var harness = COMPILE.program()
				.link(COMPILE.shader(DeviceFeatureCompat.MAX_GLSL_VERSION, ShaderType.VERTEX)
						.nameMapper($ -> "fullscreen/fullscreen")
						.withResource(FULLSCREEN))
				.link(COMPILE.shader(DeviceFeatureCompat.MAX_GLSL_VERSION, ShaderType.FRAGMENT)
						.nameMapper(id -> "fullscreen/" + IdentifierUtil.toDebugFileNameNoExtension(id))
						.onCompile((id, compilation) -> {
							if (DeviceFeatureCompat.MAX_GLSL_VERSION.compareTo(GlslVersion.V400) < 0) {
								// Need to define FMA for the wavelet calculations
								compilation.define("fma(a, b, c) ((a) * (b) + (c))");
							}
						})
						.withResource(s -> s))
				.harness("fullscreen", sources);
		return new OitPrograms(harness);
	}

	@Deprecated(forRemoval = true)
	public GlProgram getOitCompositeProgram() {
		return harness.get(OitPrograms.OIT_COMPOSITE);
	}

	@Deprecated(forRemoval = true)
	public GlProgram getOitDepthProgram() {
		return harness.get(OitPrograms.OIT_DEPTH);
	}

	public RenderPipeline getOitCompositePipeline() {
		return harness.getPipeline(FlwRenderPipelines.OIT_COMPOSITE, OitPrograms.OIT_COMPOSITE);
	}

	public RenderPipeline getOitDepthPipeline() {
		return harness.getPipeline(FlwRenderPipelines.OIT_DEPTH_FROM_TRANSMITTANCE, OitPrograms.OIT_DEPTH);
	}

	public void delete() {
		harness.delete();
	}
}
