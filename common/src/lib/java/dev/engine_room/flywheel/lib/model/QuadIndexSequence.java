package dev.engine_room.flywheel.lib.model;

import dev.engine_room.flywheel.api.model.IndexSequence;

import java.nio.ByteBuffer;

public final class QuadIndexSequence implements IndexSequence {
	public static final QuadIndexSequence INSTANCE = new QuadIndexSequence();

	private QuadIndexSequence() {
	}

	@Override
	public void fill(ByteBuffer buffer, int count) {
		int numVertices = 4 * (count / 6);
		int baseVertex = 0;
		while (baseVertex < numVertices) {
			// triangle a
			buffer.putInt(baseVertex);
			buffer.putInt(baseVertex + 1);
			buffer.putInt(baseVertex + 2);
			// triangle b
			buffer.putInt(baseVertex);
			buffer.putInt(baseVertex + 2);
			buffer.putInt(baseVertex + 3);

			baseVertex += 4;
		}
	}
}
