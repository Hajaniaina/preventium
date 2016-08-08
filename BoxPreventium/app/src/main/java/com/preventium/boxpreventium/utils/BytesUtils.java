package com.preventium.boxpreventium.utils;

/**
 * Created by Franck on 08/08/2016.
 */

public class BytesUtils {

    public static String dataToHex( byte[] data ){
        String ret = "";
        for (int i = 0; i < data.length; i++) {
            if( !ret.isEmpty() ) ret += " ";
            ret += "0x" + Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1);
        }
        return ret;
    }

    public static String dataToBin( byte[] data ){
        String ret = "";
        for (int i = 0; i < data.length; i++) {
            if( !ret.isEmpty() ) ret += " ";
            ret += "0x" + Integer.toString((data[i] & 0xff) + 0x100, 2).substring(1);
        }
        return ret;
    }

    public static String dataToString( byte[] data ){
        String ret = "";
        for (int i = 0; i < data.length; i++)
            ret += (char)data[i];
        return ret;
    }

    public static String dataToDecimal( byte[] data ){
        String ret = "";
        for (int i = 0; i < data.length; i++) {
            if( !ret.isEmpty() ) ret += " ";
            ret += Integer.toString((data[i] & 0xff) + 0x100, 10).substring(1);
        }
        return ret;
    }

    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static String[] concatenateStringArrays(String[] a, String[] b) {
        String[] result = new String[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] dataCircularShifting( byte[] source, int shift ) {
        if( shift != 0 ) {
            boolean left = false;
            if (shift < 0) {
                left = true;
                shift = -shift;
            }
            if (shift > source.length ) shift = source.length % shift;
            if (shift > 0) {
                byte[] result = new byte[source.length];
                if (left) {
                    System.arraycopy(source, shift, result, 0, source.length-shift);
                    System.arraycopy(source, 0, result, source.length - shift, shift);
                } else {
                    System.arraycopy(source, 0, result, shift, source.length-shift);
                    System.arraycopy(source, source.length - shift, result, 0, shift);
                }
                return result;
            }
        }
        return source;
    }

    /**
     * Return a new byte array containing a sub-portion of the source array
     *
     * @param srcBegin
     *          The beginning index (inclusive)
     * @return The new, populated byte array
     */
    public static byte[] subbytes(byte[] source, int srcBegin) {
        return subbytes(source, srcBegin, source.length);
    }

    /**
     * Return a new byte array containing a sub-portion of the source array
     *
     * @param srcBegin
     *          The beginning index (inclusive)
     * @param srcEnd
     *          The ending index (exclusive)
     * @return The new, populated byte array
     */
    public static byte[] subbytes(byte[] source, int srcBegin, int srcEnd) {
        byte destination[];
        destination = new byte[srcEnd - srcBegin];
        getBytes(source, srcBegin, srcEnd, destination, 0);
        return destination;
    }

    /**
     * Copies bytes from the source byte array to the destination array
     *
     * @param source
     *          The source array
     * @param srcBegin
     *          Index of the first source byte to copy
     * @param srcEnd
     *          Index after the last source byte to copy
     * @param destination
     *          The destination array
     * @param dstBegin
     *          The starting offset in the destination array
     */
    public static void getBytes(byte[] source, int srcBegin, int srcEnd, byte[] destination,
                                int dstBegin) {
        System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }
}
