package dev.engine_room.flywheel.backend.gl.array;

import java.util.Arrays;
import java.util.BitSet;

import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.Checks;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import dev.engine_room.flywheel.backend.gl.GlCompat;
import net.minecraft.util.Util;

public class GlVertexArrayDSA extends GlVertexArray {
	public static final boolean SUPPORTED = isSupported();
	private final BitSet attributeEnabled = new BitSet(MAX_ATTRIBS);
	private final VertexFormatElement[] vertexFormatElements = new VertexFormatElement[MAX_ATTRIBS];
	private final int[] attributeBindings = Util.make(new int[MAX_ATTRIBS], a -> Arrays.fill(a, -1));
	private final int[] bindingBuffers = new int[MAX_ATTRIB_BINDINGS];
	private final long[] bindingOffsets = new long[MAX_ATTRIB_BINDINGS];
	private final int[] bindingStrides = new int[MAX_ATTRIB_BINDINGS];
	private final int[] bindingDivisors = new int[MAX_ATTRIB_BINDINGS];

	private int elementBufferBinding = 0;

	public GlVertexArrayDSA() {
		handle(GL45C.glCreateVertexArrays());
	}

	@Override
	public void bindVertexBuffer(final int bindingIndex, final int vbo, final long offset, final int stride) {
		if (bindingBuffers[bindingIndex] != vbo || bindingOffsets[bindingIndex] != offset || bindingStrides[bindingIndex] != stride) {
			GL45C.glVertexArrayVertexBuffer(handle(), bindingIndex, vbo, offset, stride);
			bindingBuffers[bindingIndex] = vbo;
			bindingOffsets[bindingIndex] = offset;
			bindingStrides[bindingIndex] = stride;
		}
	}

	@Override
	public void setBindingDivisor(final int bindingIndex, final int divisor) {
		if (bindingDivisors[bindingIndex] != divisor) {
			GL45C.glVertexArrayBindingDivisor(handle(), bindingIndex, divisor);
			bindingDivisors[bindingIndex] = divisor;
		}
	}

	@Override
	public void bindAttributes(final int bindingIndex, final int startAttribIndex, VertexFormat vertexFormat) {
		final int handle = handle();
		int attribIndex = startAttribIndex;

		for (VertexFormatElement element : vertexFormat.getElements()) {
			if (!attributeEnabled.get(attribIndex)) {
				GL45C.glEnableVertexArrayAttrib(handle, attribIndex);
				attributeEnabled.set(attribIndex);
			}

			if (!element.equals(vertexFormatElements[attribIndex])) {
				int glExternalId = GlConst.toGlExternalId(element.format());
				int glType = GlConst.toGlType(element.format());
				boolean isIntegerFormat = GlConst.isGlFormatInteger(glExternalId);
				boolean isNormalizedFormat = GlConst.isFormatNormalized(element.format());
				int channelCount = GlConst.glFormatChannelCount(glExternalId);
				if (isIntegerFormat) {
					GL45C.glVertexArrayAttribIFormat(handle, attribIndex, channelCount, glType, element.offset());
				} else {
					GL45C.glVertexArrayAttribFormat(handle, attribIndex, channelCount, glType, isNormalizedFormat, element.offset());
				}
				vertexFormatElements[attribIndex] = element;
			}

			if (attributeBindings[attribIndex] != bindingIndex) {
				GL45C.glVertexArrayAttribBinding(handle, attribIndex, bindingIndex);
				attributeBindings[attribIndex] = bindingIndex;
			}

			attribIndex++;
		}
	}

	@Override
	public void setElementBuffer(int ebo) {
		if (elementBufferBinding != ebo) {
			GL45C.glVertexArrayElementBuffer(handle(), ebo);
			elementBufferBinding = ebo;
		}
	}

	private static boolean isSupported() {
		var c = GlCompat.CAPABILITIES;
		return GlCompat.ALLOW_DSA && Checks.checkFunctions(c.glCreateVertexArrays, c.glVertexArrayElementBuffer, c.glVertexArrayVertexBuffer, c.glVertexArrayBindingDivisor, c.glVertexArrayAttribBinding, c.glEnableVertexArrayAttrib, c.glVertexArrayAttribFormat, c.glVertexArrayAttribIFormat);
	}
}
