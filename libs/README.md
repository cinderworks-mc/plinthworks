# libs

local jars the build can use but the repo never holds. everything here except this file is gitignored.

## thaumaturge

the essentia compat (`src/thaumaturge`) compiles against thaumaturge's real classes. thaumaturge has no 1.21.1 maven and its licence doesn't let us host or commit the jar, so you bring your own copy:

1. get `thaumaturge-1.21.1-NeoForge-1.0.3.jar` from wherever you got thaumaturge.
2. drop it in this folder, or point gradle at it: `./gradlew build -Pthaumaturge_jar=/path/to/thaumaturge-1.21.1-NeoForge-1.0.3.jar`

with the jar the compat is compiled and goes into the plinthworks jar. without it the build prints a line saying so and you get plinthworks without essentia support, which is fine for working on anything else.

the jar is only used to compile. none of thaumaturge ends up in `build/libs`, and `.github/scripts/jar_guard.py build/libs/*.jar` checks that.
