package com.jozufozu.flywheel.backend.engine.indirect;

import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

public class IndirectModel {
	public final IndirectInstancer<?> instancer;
	public final int index;
	private final Vector4fc boundingSphere;

	private int baseInstance = -1;
	private boolean needsFullWrite = true;

	public IndirectModel(IndirectInstancer<?> instancer, int index, Vector4fc boundingSphere) {
		this.instancer = instancer;
		this.index = index;
		this.boundingSphere = boundingSphere;
	}

	public int baseInstance() {
		return baseInstance;
	}

	public void prepare(int baseInstance) {
		instancer.update();
		if (baseInstance == this.baseInstance) {
			needsFullWrite = false;
			return;
		}
		this.baseInstance = baseInstance;
		needsFullWrite = true;
	}

	public void writeObjects(StagingBuffer stagingBuffer, long start, int dstVbo) {
		if (needsFullWrite) {
			instancer.writeAll(stagingBuffer, start, dstVbo);
		} else {
			instancer.writeChanged(stagingBuffer, start, dstVbo);
		}
	}

	public void write(long ptr) {
		MemoryUtil.memPutInt(ptr, 0); // instanceCount - to be incremented by the cull shader
		MemoryUtil.memPutInt(ptr + 4, baseInstance); // baseInstance
		boundingSphere.getToAddress(ptr + 8); // boundingSphere
	}
}
