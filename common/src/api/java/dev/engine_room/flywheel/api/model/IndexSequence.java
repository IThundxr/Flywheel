package dev.engine_room.flywheel.api.model;

import java.nio.ByteBuffer;

/**
 * Represents a sequence of unsigned integer vertex indices.
 */
public interface IndexSequence {
	/**
	 * Populate the given memory region with indices.
	 * <p>
	 * Do not write outside the range {@code [buffer.position(), buffer.position() + count * 4]}.
	 */
	void fill(ByteBuffer buffer, int count);
}
