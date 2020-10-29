/*
 *
 *  (c)  Copyright 2020 Undercurrency
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.undercurrency.audiomoth.usbhid;

public  class USBUtils{
    public static int toInt(byte b) {
        return (int) b & 0xFF;
    }

    public static byte toByte(int c) {
        return (byte) (c <= 0x7f ? c : ((c % 0x80) - 0x80));
    }


    public static byte readByteFromLittleEndian(byte[] buffer, int start){
        int a, b, c, d;
        c = (buffer[start+1]  & 0xFF) << 8;
        d =  buffer[start]  & 0xFF;
        return  (byte)( c | d);
    }
    public static int readIntFromLittleEndian(byte[] buffer, int start){
        int a, b, c, d;
        a = (buffer[start+3] & 0xFF) << 24;
        b = (buffer[start+2]  & 0xFF) << 16;
        c = (buffer[start+1]  & 0xFF) << 8;
        d =  buffer[start]  & 0xFF;
        return  a | b | c | d;
    }

    public static void writeLittleEndianBytes(byte[] buffer, int start, int byteCount, int value) {
        for (int i = 0; i < byteCount; i++) {
            buffer[start + i] = (byte) ((value >> (i * 8)) & 255);
        }
    }

    public static String byteToHexString(byte[] buffer){
        StringBuffer sb = new StringBuffer();
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        for (int j = 0; j < buffer.length; j++) {
            int v = buffer[j] & 0xFF;
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
            sb.append(' ');
        }
        return sb.toString();
    }
}
