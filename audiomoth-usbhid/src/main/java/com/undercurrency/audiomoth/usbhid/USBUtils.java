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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public  class USBUtils{
    public static int toInt(byte b) {
        return (int) b & 0xFF;
    }

    public static byte toByte(int c) {
        return (byte) (c <= 0x7f ? c : ((c % 0x80) - 0x80));
    }


    public static short readShortFromLittleEndian(byte[] buffer, int start){
        ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(buffer,start,start+2));
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort();
    }
    public static int readIntFromLittleEndian(byte[] buffer, int start){
        ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(buffer,start,start+4));
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static void writeIntToLittleEndian(byte[] buffer, int start,int value){
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) value );
        byte[] le =  bb.array();
        for (int i = 0; i < 4; i++) {
            buffer[start + i] = le[i];
        }
    }

    public static void writeShortToLittleEndian(byte[] buffer, int start, short value){
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort( value );
        byte[] le =  bb.array();
        for (int i = 0; i < 2; i++) {
            buffer[start + i] = le[i];
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
