package com.preventium.boxpreventium.utils.superclass.bluetooth.scanner;

import java.util.Arrays;

class ObjectsCompat {
    ObjectsCompat() {
    }

    static String toString(Object o) {
        return o == null ? "null" : o.toString();
    }

    static int hash(Object... values) {
        return Arrays.hashCode(values);
    }

    static boolean equals(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    static boolean deepEquals(Object a, Object b) {
        if (a == null || b == null) {
            if (a == b) {
                return true;
            }
            return false;
        } else if ((a instanceof Object[]) && (b instanceof Object[])) {
            return Arrays.deepEquals((Object[]) a, (Object[]) b);
        } else {
            if ((a instanceof boolean[]) && (b instanceof boolean[])) {
                return Arrays.equals((boolean[]) a, (boolean[]) b);
            }
            if ((a instanceof byte[]) && (b instanceof byte[])) {
                return Arrays.equals((byte[]) a, (byte[]) b);
            }
            if ((a instanceof char[]) && (b instanceof char[])) {
                return Arrays.equals((char[]) a, (char[]) b);
            }
            if ((a instanceof double[]) && (b instanceof double[])) {
                return Arrays.equals((double[]) a, (double[]) b);
            }
            if ((a instanceof float[]) && (b instanceof float[])) {
                return Arrays.equals((float[]) a, (float[]) b);
            }
            if ((a instanceof int[]) && (b instanceof int[])) {
                return Arrays.equals((int[]) a, (int[]) b);
            }
            if ((a instanceof long[]) && (b instanceof long[])) {
                return Arrays.equals((long[]) a, (long[]) b);
            }
            if ((a instanceof short[]) && (b instanceof short[])) {
                return Arrays.equals((short[]) a, (short[]) b);
            }
            return a.equals(b);
        }
    }
}
