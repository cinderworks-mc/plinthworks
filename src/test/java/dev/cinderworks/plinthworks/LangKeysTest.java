package dev.cinderworks.plinthworks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// reads the sources as text: every key the code names must be in en_us.json and every key in
// en_us.json must be named by the code, built from a prefix the code names, or come from a registry
class LangKeysTest
{
	private static final Pattern LITERAL = Pattern.compile("\"([A-Za-z0-9_.]+)\"");
	private static final Pattern KEY_SHAPE = Pattern.compile("(gui|message|container|plinthworks|itemGroup)\\.[A-Za-z0-9_.]*");
	// item, block and advancement names come from the registries and the advancement provider
	private static final List<String> REGISTRY_PREFIXES = List.of("item.plinthworks.", "block.plinthworks.", "advancements.plinthworks.");

	// translated but not shown anywhere yet, the tip variants carry the wording the screen uses
	private static final Set<String> KEPT_UNUSED = Set.of("gui.plinthworks.mode.tip", "gui.plinthworks.network_mode",
			"gui.plinthworks.redstone.always", "gui.plinthworks.redstone.pause", "gui.plinthworks.redstone.run");

	@Test
	void everyKeyTheCodeNamesExistsAndNoneAreLeftOver() throws IOException {
		Set<String> lang = lang();
		Set<String> literals = new TreeSet<>();
		Set<String> prefixes = new TreeSet<>();
		try (Stream<Path> files = Files.walk(root().resolve("src/main/java"))) {
			for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
				if (file.toString().contains("/data/")) {
					continue;
				}
				Matcher m = LITERAL.matcher(Files.readString(file));
				while (m.find()) {
					String value = m.group(1);
					// MODID + ".etching." style fragments
					if (value.startsWith(".")) {
						if (!value.endsWith(".") || value.chars().noneMatch(Character::isLetter)) {
							continue;
						}
						value = "plinthworks" + value;
					}
					if (!KEY_SHAPE.matcher(value).matches()) {
						continue;
					}
					(value.endsWith(".") ? prefixes : literals).add(value);
				}
			}
		}
		List<String> missing = literals.stream().filter(k -> !lang.contains(k)).toList();
		assertTrue(missing.isEmpty(), "named in code but not in en_us.json: " + missing);

		List<String> deadPrefixes = prefixes.stream().filter(p -> lang.stream().noneMatch(k -> k.startsWith(p))).toList();
		assertTrue(deadPrefixes.isEmpty(), "prefix with no key under it: " + deadPrefixes);

		List<String> unused = lang.stream()
				.filter(k -> !literals.contains(k) && !KEPT_UNUSED.contains(k))
				.filter(k -> prefixes.stream().noneMatch(k::startsWith))
				.filter(k -> REGISTRY_PREFIXES.stream().noneMatch(k::startsWith))
				.toList();
		assertTrue(unused.isEmpty(), "in en_us.json but never named by the code: " + unused);
	}

	// the test working directory is a run folder, so climb to the project
	private static Path root() {
		Path dir = Path.of("").toAbsolutePath();
		while (dir != null && !Files.exists(dir.resolve("build.gradle"))) {
			dir = dir.getParent();
		}
		assertNotNull(dir, "project root not found");
		return dir;
	}

	private static Set<String> lang() throws IOException {
		Path path = root().resolve("src/generated/resources/assets/plinthworks/lang/en_us.json");
		JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
		return new TreeSet<>(json.keySet());
	}
}
