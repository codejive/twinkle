package org.codejive.twinkle.core.text;

public class StyledBufferTimings {
    private static int iterations = 1_000_000;

    public static void main(String[] args) {
        System.out.println("Basics:");
        String simple = "0123456789";
        titer(
                "Simple string length",
                10 * iterations,
                () -> {
                    int len = simple.length();
                    int total = 0;
                    for (int i = 0; i < len; i++) {
                        total += simple.charAt(i);
                    }
                });
        titer(
                "Simple string codePointCount",
                10 * iterations,
                () -> {
                    int len = simple.codePointCount(0, simple.length());
                    int total = 0;
                    for (int i = 0; i < len; ) {
                        int cp = simple.codePointAt(i);
                        total += cp;
                        i += Character.charCount(cp);
                    }
                });
        titer(
                "Simple string codepoints.length",
                10 * iterations,
                () -> {
                    int total = 0;
                    for (int i : simple.codePoints().toArray()) {
                        total += i;
                    }
                });

        System.out.println("Timing simple strings:");
        timeSimpleString(StyledBuffer.of(1000));

        System.out.println("Timing strings with surrogates:");
        timeStringWithSurrogates(StyledBuffer.of(1000));
    }

    private static void timeSimpleString(StyledBuffer buffer) {
        titer(
                buffer.getClass().getSimpleName(),
                () -> {
                    for (int i = 0; i < 500; i += 10) {
                        buffer.putStringAt(i, 0, "0123456789");
                    }
                    for (int i = 500; i < 1000; i += 10) {
                        buffer.putStringAt(i, 0, "0123456789");
                    }
                });
    }

    private static void timeStringWithSurrogates(StyledBuffer buffer) {
        titer(
                buffer.getClass().getSimpleName(),
                () -> {
                    for (int i = 0; i < 500; i += 10) {
                        buffer.putStringAt(i, 0, "0123456789");
                    }
                    for (int i = 500; i < 1000; i += 10) {
                        buffer.putStringAt(i, 0, "01234\uD83D\uDE8056789");
                    }
                });
    }

    private static void titer(String msg, Runnable func) {
        time(msg, () -> iterate(iterations, func));
    }

    private static void titer(String msg, int iterations, Runnable func) {
        time(msg, () -> iterate(iterations, func));
    }

    private static void iterate(int iterations, Runnable func) {
        for (int iter = 0; iter < iterations; iter++) {
            func.run();
        }
    }

    private static void time(String msg, Runnable func) {
        long startTime = System.nanoTime();
        func.run();
        System.out.println(msg + " " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
    }
}
