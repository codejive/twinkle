
# Twinkle

Twinkle is a Java library for creating advanced text-based user interfaces.

This is a very early proof of concept, nothing to see here (yet)

## Building

To build the project, run the following command:

```bash
./mvnw clean package
```

## Running

To see a couple of very early demos, run the following commands:

```bash
java -cp twinkle-chart/target/twinkle-chart-1.0-SNAPSHOT.jar:twinkle-core/target/twinkle-core-1.0-SNAPSHOT.jar:twinkle-ansi/target/twinkle-ansi-1.0-SNAPSHOT.jar:twinkle-chart/target/test-classes examples.BarDemo
java -cp twinkle-chart/target/twinkle-chart-1.0-SNAPSHOT.jar:twinkle-core/target/twinkle-core-1.0-SNAPSHOT.jar:twinkle-ansi/target/twinkle-ansi-1.0-SNAPSHOT.jar:twinkle-chart/target/test-classes examples.MathPlotDemo
java -cp twinkle-chart/target/twinkle-chart-1.0-SNAPSHOT.jar:twinkle-core/target/twinkle-core-1.0-SNAPSHOT.jar:twinkle-ansi/target/twinkle-ansi-1.0-SNAPSHOT.jar:twinkle-chart/target/test-classes examples.MathPlotColorDemo
java -cp twinkle-chart/target/twinkle-chart-1.0-SNAPSHOT.jar:twinkle-core/target/twinkle-core-1.0-SNAPSHOT.jar:twinkle-ansi/target/twinkle-ansi-1.0-SNAPSHOT.jar:twinkle-chart/target/test-classes examples.MathPlotFourDemo
```

An easier way to run the demos is using [JBang](https://www.jbang.dev/):

```bash
./mvnw install -DskipTests
jbang run BarDemo
jbang run MathPlotDemo
jbang run MathPlotColorDemo
jbang run MathPlotFourDemo
```

These demos only show Twinkle's Ansi output capabilities. There is no interactivity being shown.
