package org.codejive.twinkle.tui.styles;

import static org.codejive.twinkle.tui.styles.Type.integer;
import static org.codejive.twinkle.tui.styles.Type.length;
import static org.codejive.twinkle.tui.styles.Type.number;
import static org.codejive.twinkle.tui.styles.Type.percentage;
import static org.codejive.twinkle.tui.styles.Type.string;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Value {

    Type type();

    static Int integer(int v) {
        return new Int(v);
    }

    static Num number(float v) {
        return new Num(v);
    }

    static Pct percentage(float v) {
        return new Pct(v);
    }

    static Len length(float v, Unit unit) {
        return new Len(v, unit);
    }

    static Str string(String v) {
        return new Str(v);
    }

    static Lit literal(String v) {
        return new Lit(v);
    }

    @SuppressWarnings("unchecked")
    default <T extends Value> T as(Class<T> type) {
        if ((type == Int.class && this instanceof Int)
                || (type == Num.class && this instanceof Num)
                || (type == Pct.class && this instanceof Pct)
                || (type == Len.class && this instanceof Len)
                || (type == Str.class && this instanceof Str)
                || (type == Lit.class && this instanceof Lit)) {
            return (T) this;
        } else {
            return null;
        }
    }

    static Optional<? extends Value> parse(String v) {
        if (v == null) {
            return Optional.empty();
        }
        Optional<? extends Value> result = Int.parse(v);
        if (!result.isPresent()) {
            result = Num.parse(v);
        }
        if (!result.isPresent()) {
            result = Pct.parse(v);
        }
        if (!result.isPresent()) {
            result = Col.parse(v);
        }
        if (!result.isPresent()) {
            result = Len.parse(v);
        }
        if (!result.isPresent()) {
            result = Lit.parse(v);
        }
        if (!result.isPresent()) {
            result = Str.parse(v);
        }
        return result;
    }

    class Int implements Value {
        private final Integer value;

        private static final Pattern intp = Pattern.compile("[-+]?(0|[1-9][0-9]*)");

        private Int(Integer value) {
            this.value = value;
        }

        public Integer get() {
            return value;
        }

        @Override
        public Type type() {
            return integer;
        }

        public static Optional<? extends Int> parse(String v) {
            if (intp.matcher(v).matches()) {
                return Optional.of(new Int(Integer.valueOf(v)));
            }
            return Optional.empty();
        }
    }

    class Num implements Value {
        private final Float value;

        private static final Pattern nump =
                Pattern.compile("[-+]?(0|[1-9][0-9]*)(\\.[0-9]+([eE][-+]?(0|[1-9][0-9]*))?)?");

        private Num(Float value) {
            this.value = value;
        }

        public Float get() {
            return value;
        }

        @Override
        public Type type() {
            return number;
        }

        public static Optional<? extends Num> parse(String v) {
            if (nump.matcher(v).matches()) {
                return Optional.of(new Num(Float.valueOf(v)));
            }
            return Optional.empty();
        }
    }

    class Pct extends Num {
        private Pct(Float value) {
            super(value);
        }

        @Override
        public Type type() {
            return percentage;
        }

        public static Optional<Pct> parse(String v) {
            if (v.endsWith("%")) {
                return Num.parse(v.substring(0, v.length() - 1).trim()).map(n -> new Pct(n.get()));
            }
            return Optional.empty();
        }
    }

    interface Dim extends Value {
        Float get();

        Unit unit();
    }

    class Len extends Num implements Dim {
        private final Unit unit;

        private static final Pattern lenp = Pattern.compile("[^a-z]+([a-z]+)");

        private Len(Float value, Unit unit) {
            super(value);
            this.unit = unit;
        }

        @Override
        public Unit unit() {
            return unit;
        }

        @Override
        public Type type() {
            return length;
        }

        public Len convert(Unit unit) {
            if (this.unit == unit) {
                return this;
            } else {
                // TODO Implement!
                throw new RuntimeException("NYI");
            }
        }

        public static Optional<Len> parse(String v) {
            Matcher m = lenp.matcher(v);
            if (m.matches()) {
                Unit u = Unit.valueOf(m.group(1));
                return Num.parse(v.substring(0, v.length() - u.name().length()).trim())
                        .map(n -> new Len(n.get(), u));
            }
            return Optional.empty();
        }
    }

    class Str implements Value {
        private final String value;

        private Str(String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }

        @Override
        public Type type() {
            return string;
        }

        public static Optional<Str> parse(String v) {
            return v != null ? Optional.of(new Str(v)) : Optional.empty();
        }
    }

    class Lit implements Value {
        private final Type type;

        private Lit(String value) {
            assert value != null;
            type = Type.valueOf(value.toUpperCase());
        }

        public String get() {
            return type.literal;
        }

        @Override
        public Type type() {
            return type;
        }

        public static Optional<Lit> parse(String v) {
            try {
                return Optional.of(new Lit(v));
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
        }
    }

    class Col extends Int {
        private static final Pattern colp =
                Pattern.compile("#([0-9a-fA-F]|[0-9a-fA-F]{2}|[0-9a-fA-F]{6})");

        private Col(Integer value) {
            super(value);
        }

        @Override
        public Type type() {
            return percentage;
        }

        public static Optional<Col> parse(String v) {
            if (colp.matcher(v).matches()) {
                return Int.parse(v.substring(1).trim()).map(n -> new Col(n.get()));
            }
            return Optional.empty();
        }
    }
}
