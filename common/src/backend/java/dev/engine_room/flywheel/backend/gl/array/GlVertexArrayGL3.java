package dev.engine_room.flywheel.backend.gl.array;

import java.util.Arrays;
import java.util.BitSet;

import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.Checks;

import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlStateManager;

import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.buffer.GlBufferType;
import net.minecraft.util.Util;

public abstract class GlVertexArrayGL3 extends GlVertexArray {
	private final BitSet attributeDirty = new BitSet(MAX_ATTRIBS);
	private final int[] attributeOffsets = new int[MAX_ATTRIBS];
	private final VertexFormatElement[] vertexFormatElements = new VertexFormatElement[MAX_ATTRIBS];
	private final int[] attributeBindings = Util.make(new int[MAX_ATTRIBS], a -> Arrays.fill(a, -1));
	private final int[] bindingBuffers = new int[MAX_ATTRIB_BINDINGS];
	private final long[] bindingOffsets = new long[MAX_ATTRIB_BINDINGS];
	private final int[] bindingStrides = new int[MAX_ATTRIB_BINDINGS];
	private final int[] bindingDivisors = new int[MAX_ATTRIB_BINDINGS];
	private int requestedElementBuffer = 0;
	private int boundElementBuffer = 0;

	public GlVertexArrayGL3() {
		handle(GlStateManager._glGenVertexArrays());
	}

	@Override
	public void bindForDraw() {
		super.bindForDraw();

		maybeUpdateAttributes();

		maybeUpdateEBOBinding();
	}

	@Override
	public void bindVertexBuffer(int bindingIndex, int vbo, long offset, int stride) {
		if (bindingBuffers[bindingIndex] != vbo || bindingOffsets[bindingIndex] != offset || bindingStrides[bindingIndex] != stride) {
			bindingBuffers[bindingIndex] = vbo;
			bindingOffsets[bindingIndex] = offset;
			bindingStrides[bindingIndex] = stride;

			for (int attribIndex = 0; attribIndex < attributeBindings.length; attribIndex++) {
				if (attributeBindings[attribIndex] == bindingIndex) {
					attributeDirty.set(attribIndex);
				}
			}
		}
	}

	@Override
	public void setBindingDivisor(int bindingIndex, int divisor) {
		if (bindingDivisors[bindingIndex] != divisor) {
			bindingDivisors[bindingIndex] = divisor;
		}
	}

	@Override
	public void bindAttributes(final int bindingIndex, final int startAttribIndex, VertexFormat vertexFormat) {
		int attribIndex = startAttribIndex;

		for (VertexFormatElement element : vertexFormat.getElements()) {
			attributeBindings[attribIndex] = bindingIndex;
			vertexFormatElements[attribIndex] = element;
			attributeOffsets[attribIndex] = element.offset();

			attributeDirty.set(attribIndex);

			attribIndex++;
		}
	}

	@Override
	public void setElementBuffer(int ebo) {
		requestedElementBuffer = ebo;
	}

	private void maybeUpdateEBOBinding() {
		if (requestedElementBuffer != boundElementBuffer) {
			GlBufferType.ELEMENT_ARRAY_BUFFER.bind(requestedElementBuffer);
			boundElementBuffer = requestedElementBuffer;
		}
	}

	private void maybeUpdateAttributes() {
		for (int attribIndex = attributeDirty.nextSetBit(0); attribIndex < MAX_ATTRIB_BINDINGS && attribIndex >= 0; attribIndex = attributeDirty.nextSetBit(attribIndex + 1)) {
			updateAttribute(attribIndex);
		}
		attributeDirty.clear();
	}

	private void updateAttribute(int attribIndex) {
		int bindingIndex = attributeBindings[attribIndex];
		var element = vertexFormatElements[attribIndex];

		if (bindingIndex == -1 || element == null) {
			return;
		}

		GlBufferType.ARRAY_BUFFER.bind(bindingBuffers[bindingIndex]);
		GlStateManager._enableVertexAttribArray(attribIndex);

		long offset = bindingOffsets[bindingIndex] + attributeOffsets[attribIndex];
		int stride = bindingStrides[bindingIndex];

		int glExternalId = GlConst.toGlExternalId(element.format());
		int glType = GlConst.toGlType(element.format());
		boolean isIntegerFormat = GlConst.isGlFormatInteger(glExternalId);
		boolean isNormalizedFormat = GlConst.isFormatNormalized(element.format());
		int channelCount = GlConst.glFormatChannelCount(glExternalId);

		if (isIntegerFormat) {
			GlStateManager._vertexAttribIPointer(attribIndex, channelCount, glType, stride, offset);
		} else {
			GlStateManager._vertexAttribPointer(attribIndex, channelCount, glType, isNormalizedFormat, stride, offset);
		}

		int divisor = bindingDivisors[bindingIndex];
		if (divisor != 0) {
			setDivisor(attribIndex, divisor);
		}
	}

	protected abstract void setDivisor(int attribIndex, int divisor);

	public static class Core33 extends GlVertexArrayGL3 {
		public static final boolean SUPPORTED = isSupported();

		@Override
		protected void setDivisor(int attribIndex, int divisor) {
			GL33C.glVertexAttribDivisor(attribIndex, divisor);
		}

		private static boolean isSupported() {
			return Checks.checkFunctions(GlCompat.CAPABILITIES.glVertexAttribDivisor);
		}
	}

	public static class ARB extends GlVertexArrayGL3 {
		public static final boolean SUPPORTED = isSupported();

		@Override
		protected void setDivisor(int attribIndex, int divisor) {
			ARBInstancedArrays.glVertexAttribDivisorARB(attribIndex, divisor);
		}

		private static boolean isSupported() {
			return Checks.checkFunctions(GlCompat.CAPABILITIES.glVertexAttribDivisorARB);
		}
	}

	public static class Core extends GlVertexArrayGL3 {
		@Override
		protected void setDivisor(int attribIndex, int divisor) {
			throw new UnsupportedOperationException("Instanced arrays are not supported");
		}

		@Override
		public void setBindingDivisor(int bindingIndex, int divisor) {
			throw new UnsupportedOperationException("Instanced arrays are not supported");
		}
	}
}
