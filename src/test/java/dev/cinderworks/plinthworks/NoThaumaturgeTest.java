package dev.cinderworks.plinthworks;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// plinthworks has to load and run without thaumaturge, and only the compat source set may know it exists.
// checked on the compiled main classes: nothing in them names a thaumaturge type, and each one links its fields,
// methods and constructors in a class loader that refuses thaumaturge's packages
class NoThaumaturgeTest
{
	private static final String PACKAGE = "com.leclowndu93150.";

	@Test
	void noCoreClassNamesThaumaturge() {
		byte[] needle = PACKAGE.replace('.', '/').getBytes(StandardCharsets.UTF_8);
		List<String> offenders = new ArrayList<>();
		for (Path file : classFiles()) {
			if (indexOf(read(file), needle) >= 0) {
				offenders.add(classesDir().relativize(file).toString());
			}
		}
		assertEquals(List.of(), offenders, "main classes that reference thaumaturge");
	}

	@Test
	void theCompatIsNotPartOfTheCore() {
		assertFalse(Files.exists(classesDir().resolve("dev/cinderworks/plinthworks/compat/thaumaturge")),
				"the thaumaturge compat belongs in its own source set");
	}

	@Test
	void everyCoreClassLinksWithoutThaumaturge() throws Exception {
		Blind loader = new Blind(getClass().getClassLoader());
		int linked = 0;
		for (Path file : classFiles()) {
			String name = classesDir().relativize(file).toString().replace('/', '.').replaceAll("\\.class$", "");
			Class<?> type = Class.forName(name, false, loader);
			assertSame(loader, type.getClassLoader(), name);
			type.getDeclaredFields();
			type.getDeclaredMethods();
			type.getDeclaredConstructors();
			linked++;
		}
		assertTrue(linked > 100, "only found " + linked + " classes");
		assertTrue(loader.refused.isEmpty(), "asked for " + loader.refused);
	}

	// loads plinthworks from the compiled classes itself, so nothing slips in through the test class path
	private static class Blind extends ClassLoader
	{
		final List<String> refused = new ArrayList<>();

		Blind(ClassLoader parent) {
			super(parent);
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			if (name.startsWith(PACKAGE)) {
				refused.add(name);
				throw new ClassNotFoundException(name);
			}
			if (!name.startsWith("dev.cinderworks.plinthworks.")) {
				return super.loadClass(name, resolve);
			}
			synchronized (getClassLoadingLock(name)) {
				Class<?> loaded = findLoadedClass(name);
				if (loaded == null) {
					Path file = classesDir().resolve(name.replace('.', '/') + ".class");
					if (!Files.exists(file)) {
						throw new ClassNotFoundException(name);
					}
					byte[] bytes = read(file);
					loaded = defineClass(name, bytes, 0, bytes.length);
				}
				return loaded;
			}
		}
	}

	private static List<Path> classFiles() {
		try (Stream<Path> files = Files.walk(classesDir())) {
			return files.filter(p -> p.toString().endsWith(".class")).sorted().toList();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static Path classesDir() {
		Path dir = Path.of("").toAbsolutePath();
		while (dir != null && !Files.exists(dir.resolve("build.gradle"))) {
			dir = dir.getParent();
		}
		assertNotNull(dir, "project root not found");
		return dir.resolve("build/classes/java/main");
	}

	private static byte[] read(Path file) {
		try {
			return Files.readAllBytes(file);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static int indexOf(byte[] haystack, byte[] needle) {
		outer:
		for (int i = 0; i <= haystack.length - needle.length; i++) {
			for (int j = 0; j < needle.length; j++) {
				if (haystack[i + j] != needle[j]) {
					continue outer;
				}
			}
			return i;
		}
		return -1;
	}
}
