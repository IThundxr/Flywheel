package dev.engine_room.flywheel.lib.model.part;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.math.DataPacker;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.model.part.ModelPartConverter.TextureMapper;
import dev.engine_room.flywheel.lib.vertex.PosTexNormalVertexView;

class VertexWriter implements VertexConsumer {
	private static final int STRIDE = (int) PosTexNormalVertexView.STRIDE;

	private MemoryBlock data;

	@Nullable
	private TextureMapper textureMapper;
	private final Vector2f uvVec = new Vector2f();

	private int vertexCount;
	private boolean filledPosition;
	private boolean filledTexture;
	private boolean filledNormal;

	public VertexWriter() {
		data = MemoryBlock.malloc(128 * STRIDE);
	}

	public void setTextureMapper(@Nullable TextureMapper mapper) {
		textureMapper = mapper;
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		if (!filledPosition) {
			long ptr = vertexPtr();
			MemoryUtil.memPutFloat(ptr, (float) x);
			MemoryUtil.memPutFloat(ptr + 4, (float) y);
			MemoryUtil.memPutFloat(ptr + 8, (float) z);
			filledPosition = true;
		}
		return this;
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		// ignore color
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		if (!filledTexture) {
			if (textureMapper != null) {
				uvVec.set(u, v);
				textureMapper.map(uvVec);
				u = uvVec.x;
				v = uvVec.y;
			}

			long ptr = vertexPtr();
			MemoryUtil.memPutFloat(ptr + 12, u);
			MemoryUtil.memPutFloat(ptr + 16, v);
			filledTexture = true;
		}
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		// ignore overlay
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		// ignore light
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		if (!filledNormal) {
			long ptr = vertexPtr();
			MemoryUtil.memPutByte(ptr + 20, DataPacker.packNormI8(x));
			MemoryUtil.memPutByte(ptr + 21, DataPacker.packNormI8(y));
			MemoryUtil.memPutByte(ptr + 22, DataPacker.packNormI8(z));
			filledNormal = true;
		}
		return this;
	}

	@Override
	public void endVertex() {
		if (!filledPosition || !filledTexture || !filledNormal) {
			throw new IllegalStateException("Not filled all elements of the vertex");
		}

		filledPosition = false;
		filledTexture = false;
		filledNormal = false;
		vertexCount++;

		long byteSize = (vertexCount + 1) * STRIDE;
		long capacity = data.size();
		if (byteSize > capacity) {
			data = data.realloc(capacity * 2);
		}
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
	}

	@Override
	public void unsetDefaultColor() {
	}

	private long vertexPtr() {
		return data.ptr() + vertexCount * STRIDE;
	}

	public MemoryBlock copyDataAndReset() {
		MemoryBlock dataCopy = MemoryBlock.mallocTracked(vertexCount * STRIDE);
		data.copyTo(dataCopy);

		vertexCount = 0;
		filledPosition = false;
		filledTexture = false;
		filledNormal = false;
		textureMapper = null;

		return dataCopy;
	}
}
