package dev.engine_room.flywheel.backend.compile.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 26.3: the Vulkan backend compiles GLSL to SPIR-V, which requires explicit
// layout(location = N) on every user input/output. Flywheel's assembled sources
// declare plain in/out globals, so we post-process both stages together and
// assign consistent locations: vertex attributes and fragment outputs by order
// of appearance, varyings by sorted name so both stages agree.
public final class FlwSpirvLocations {
	private static final Pattern DECL = Pattern.compile("^\\s*(flat\\s+|noperspective\\s+|smooth\\s+)?(in|out)\\s+(\\w+)\\s+(\\w+)(\\[(\\d+)])?\\s*;\\s*$");

	private FlwSpirvLocations() {
	}

	public record Processed(String vertex, String fragment) {
	}

	// Vulkan GLSL renames these builtins; glslang rejects the GL names.
	private static String renameBuiltins(String src) {
		return src.replaceAll("\\bgl_VertexID\\b", "gl_VertexIndex")
				.replaceAll("\\bgl_InstanceID\\b", "gl_InstanceIndex");
	}

	public static Processed process(String vertexSrc, String fragmentSrc) {
		vertexSrc = renameBuiltins(vertexSrc);
		if (fragmentSrc != null) {
			fragmentSrc = renameBuiltins(fragmentSrc);
		}
		Map<String, Integer> varyingSizes = new TreeMap<>();
		collectVaryings(vertexSrc, "out", varyingSizes);
		if (fragmentSrc != null) {
			collectVaryings(fragmentSrc, "in", varyingSizes);
		}
		Map<String, Integer> varyingLocations = new LinkedHashMap<>();
		int next = 0;
		for (Map.Entry<String, Integer> e : varyingSizes.entrySet()) {
			varyingLocations.put(e.getKey(), next);
			next += e.getValue();
		}

		String vertex = annotate(vertexSrc, "in", "out", varyingLocations);
		String fragment = fragmentSrc == null ? null : annotate(fragmentSrc, "out", "in", varyingLocations);
		return new Processed(vertex, fragment);
	}

	private static void collectVaryings(String src, String direction, Map<String, Integer> out) {
		int depth = 0;
		for (String line : src.split("\n", -1)) {
			if (depth == 0) {
				Matcher m = DECL.matcher(line);
				if (m.matches() && m.group(2).equals(direction)) {
					int size = m.group(6) == null ? 1 : Integer.parseInt(m.group(6));
					out.put(m.group(4), size);
				}
			}
			depth += count(line, '{') - count(line, '}');
		}
	}

	// sequentialDir: declarations numbered by order of appearance (vertex "in"
	// attributes / fragment "out" targets). varyingDir: looked up in the shared map.
	private static String annotate(String src, String sequentialDir, String varyingDir, Map<String, Integer> varyingLocations) {
		List<String> lines = new ArrayList<>();
		int depth = 0;
		int nextSequential = 0;
		for (String line : src.split("\n", -1)) {
			String result = line;
			if (depth == 0 && !line.contains("layout(")) {
				Matcher m = DECL.matcher(line);
				if (m.matches()) {
					String dir = m.group(2);
					String name = m.group(4);
					int size = m.group(6) == null ? 1 : Integer.parseInt(m.group(6));
					if (dir.equals(sequentialDir)) {
						result = "layout(location = " + nextSequential + ") " + line.stripLeading();
						nextSequential += size;
					} else if (dir.equals(varyingDir) && varyingLocations.containsKey(name)) {
						result = "layout(location = " + varyingLocations.get(name) + ") " + line.stripLeading();
					}
				}
			}
			depth += count(line, '{') - count(line, '}');
			lines.add(result);
		}
		return String.join("\n", lines);
	}

	private static int count(String s, char c) {
		int n = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c) {
				n++;
			}
		}
		return n;
	}
}
