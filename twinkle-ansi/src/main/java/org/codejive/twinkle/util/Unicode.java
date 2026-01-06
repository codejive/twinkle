package org.codejive.twinkle.util;

public class Unicode {
    public static int visibleWidth(CharSequence text) {
        int width = 0;
        SequenceIterator si = SequenceIterator.of(text);
        while (si.hasNext()) {
            si.next();
            width += si.width();
        }
        return width;
    }

    public static boolean isWide(int cp) {
        // Modern Pictographs and Enclosed Ideographs
        if (isEmoji(cp)) {
            return true;
        }

        // Hangul (Leading Jamo and full Syllables)
        if (isHangulWide(cp)) {
            return true;
        }

        // East Asian Wide (W) and Fullwidth (F)
        if ((cp >= 0x2E80 && cp <= 0xA4CF && cp != 0x303F)
                || // Hangul Syllables
                (cp >= 0xF900 && cp <= 0xFAFF)
                || // CJK Compatibility Ideographs
                (cp >= 0xFE10 && cp <= 0xFE19)
                || // Vertical forms
                (cp >= 0xFE30 && cp <= 0xFE6F)
                || // CJK Compatibility Forms
                (cp >= 0xFF00 && cp <= 0xFF60)
                || // Fullwidth Forms
                (cp >= 0xFFE0 && cp <= 0xFFE6)) {
            return true;
        }

        // Plane 2 and 3 (SIP/TIP) are almost entirely CJK Ideographs (Wide)
        if (cp >= 0x20000 && cp <= 0x3FFFD) {
            return true;
        }

        return false;
    }

    public static boolean isEmoji(int cp) {
        return (cp >= 0x1F300 && cp <= 0x1F64F) // Misc Symbols and Pictographs, Emoticons
                || (cp >= 0x1F680 && cp <= 0x1F6FF) // Transport and Map
                || (cp >= 0x1F900 && cp <= 0x1F9FF) // Supplemental Symbols/Pictographs
                || (cp >= 0x1F200 && cp <= 0x1F2FF); // Enclosed Ideographic Supplement
    }

    public static boolean isHangulWide(int cp) {
        return isL(cp) || isLV(cp) || isLVT(cp);
    }

    public static boolean isRegionalIndicator(int cp) {
        return cp >= 0x1F1E6 && cp <= 0x1F1FF;
    }

    public static boolean isL(int cp) {
        return (cp >= 0x1100 && cp <= 0x115F);
    }

    public static boolean isV(int cp) {
        return (cp >= 0x1160 && cp <= 0x11A7);
    }

    public static boolean isT(int cp) {
        return (cp >= 0x11A8 && cp <= 0x11FF);
    }

    public static boolean isLV(int cp) {
        return (cp >= 0xAC00 && cp <= 0xD7A3 && (cp - 0xAC00) % 28 == 0);
    }

    public static boolean isLVT(int cp) {
        return (cp >= 0xAC00 && cp <= 0xD7A3 && (cp - 0xAC00) % 28 != 0);
    }

    public static boolean isVirama(int cp) {
        return (cp >= 0x094D && cp <= 0x0D4D && (cp & 0xFF) == 0x4D) || cp == 0x0D4D;
    }

    public static boolean isPrepend(int cp) {
        return cp == 0x0600
                || cp == 0x0601
                || cp == 0x0602
                || cp == 0x0603
                || cp == 0x0604
                || cp == 0x0605
                || cp == 0x06DD
                || cp == 0x070F;
    }
}
