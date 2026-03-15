package org.codejive.twinkle.text;

import static org.codejive.twinkle.ansi.Constants.*;

import java.io.IOException;
import java.util.Map;

public class Hyperlink {
    public final String url;
    public final String id;

    public static final Hyperlink END = new Hyperlink(null, null);

    public static final Hyperlink of(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return new Hyperlink(url, null);
    }

    public static final Hyperlink of(String url, String id) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return new Hyperlink(url, id);
    }

    public static boolean isHyperlinkSequence(String ansiSequence) {
        return (ansiSequence.startsWith(OSC + HYPERLINK) && ansiSequence.endsWith(OSC_END))
                || (ansiSequence.startsWith(OSC + HYPERLINK) && ansiSequence.endsWith(OSC_END_ALT));
    }

    public static Hyperlink parse(String sequence) {
        if (sequence.startsWith(OSC + HYPERLINK) && sequence.endsWith(OSC_END)) {
            // OSC 8 ; params ; URI BEL
            String params = sequence.substring((OSC + HYPERLINK).length(), sequence.length() - 1);
            return parseHyperlink(params);
        } else if (sequence.startsWith(OSC + HYPERLINK) && sequence.endsWith(OSC_END_ALT)) {
            // OSC 8 ; params ; URI ST
            String params = sequence.substring((OSC + HYPERLINK).length(), sequence.length() - 2);
            return parseHyperlink(params);
        } else if (sequence.equals(OSC + HYPERLINK + ";" + OSC_END)) {
            // This is the sequence to end a hyperlink
            return END;
        }
        return null;
    }

    private Hyperlink(String url, String id) {
        this.url = url;
        this.id = id;
    }

    protected static Hyperlink parseHyperlink(String link) {
        if (";".equals(link)) {
            // This is the sequence to end a hyperlink
            return END;
        }
        String[] parts = link.split(";", 2);
        if (parts.length == 2) {
            String uri = parts[1];
            Map<String, String> params = parseParams(parts[0]);
            String id = params.get("id");
            return Hyperlink.of(uri, id);
        }
        // Not a valid hyperlink sequence
        return null;
    }

    protected static Map<String, String> parseParams(String string) {
        // Parse the params in the first part of the hyperlink sequence, which are in the format
        // key=value:key=value:...
        String[] parts = string.split(":");
        Map<String, String> params = new java.util.HashMap<>();
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0].toLowerCase(), keyValue[1]);
            }
        }
        return params;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Hyperlink hyperlink = (Hyperlink) obj;
        if (this == END || hyperlink == END) {
            // If either hyperlink is the END sentinel, they are only equal if they are the same
            // instance
            return true;
        }
        if (this.url == null || hyperlink.url == null) {
            // If either URL is null when we get here, then they are not considered equal
            return false;
        }
        return java.util.Objects.equals(url, hyperlink.url)
                && java.util.Objects.equals(id, hyperlink.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(url, id);
    }

    public String toAnsi() {
        StringBuilder sb = new StringBuilder();
        toAnsi(sb);
        return sb.toString();
    }

    public void toAnsi(Appendable appendable) {
        if (this == END) {
            append(appendable, OSC)
                    .append(appendable, HYPERLINK)
                    .append(appendable, ";")
                    .append(appendable, OSC_END);
            return;
        }
        append(appendable, OSC).append(appendable, HYPERLINK);
        if (id != null) {
            append(appendable, "id=").append(appendable, id);
        }
        append(appendable, ";");
        append(appendable, url);
        append(appendable, OSC_END);
    }

    private Hyperlink append(Appendable appendable, String str) {
        try {
            appendable.append(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}
