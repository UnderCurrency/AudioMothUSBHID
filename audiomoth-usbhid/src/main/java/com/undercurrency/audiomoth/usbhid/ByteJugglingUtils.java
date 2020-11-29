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

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;

/**
 * The ByteJugglingUtils provides functions to read and write integers to byte arrays,
 * deals with the issues of signed/unsigned integers in Java.
 * Java is a signed integers and big endian programming language
 */
public  class ByteJugglingUtils {
    /**
     * Converts a single byte to integer
     * @param b
     * @return int
     */
    public static int toInt(byte b) {
        return (int) b & 0xFF;
    }

    /**
     * Converts a single integer to byte
     * @param c
     * @return byte
     */
    public static byte toByte(int c) {
        return (byte) (c <= 0x7f ? c : ((c % 0x80) - 0x80));
    }

    /**
     * Translates a 2 byte array to an int, deals with the high order bytes if the number overflows
     * Let b a 2 index byte array {0xFF, 0xFF}, then b corresponds to an unsigned integer value
     * of 65535, but converting it by a standard java way will cause a number overflow returning
     * -1 as the value.
     * @param buffer a byte array
     * @param start index from which to start the conversion
     * @return an int
     */
    public static int readShortFromLittleEndian(byte[] buffer, int start){
       ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(buffer,start,start+2));
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int data = bb.getShort();
        if(data < 0) return data &0xFFFF;
            return data;
    }

    /**
     * Translates a 4 byte array to an int
     * @param buffer a byte array
     * @param start index from which to start the conversion
     * @return an int
     */
    public static int readIntFromLittleEndian(byte[] buffer, int start){
        ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(buffer,start,start+4));
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    /**
     * Writes an int to a 4 byte array
     * @param buffer a byte array to write in
     * @param start index from which to start writing bytes
     * @param value an integer number
     */
    public static void writeIntToLittleEndian(byte[] buffer, int start,int value){
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) value );
        byte[] le =  bb.array();
        for (int i = 0; i < 4; i++) {
            buffer[start + i] = le[i];
        }
    }

    /**
     * Writes an long to a 4 byte array
     * @param buffer a byte array to write in
     * @param start index from which to start writing bytes
     * @param value an integer number
     */
    public static void writeLongToLittleEndian(byte[] buffer, int start, long value){
        ByteBuffer bb= ByteBuffer.allocate(8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(value);
        byte[] le = bb.array();
        for (int i = 0; i < 4; i++) {
            buffer[start + i] = le[i];
        }
    }

    /**
     * Writes an int to a 2 byte array
     * @param buffer a byte array to write in
     * @param start index from which to start writing bytes
     * @param value an integer number
     */
    public static void writeShortToLittleEndian(byte[] buffer, int start, short value){
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort( value );
        byte[] le =  bb.array();
        for (int i = 0; i < 2; i++) {
            buffer[start + i] = le[i];
        }
    }

    /**
     * Reads a date from a byte array
     * @param buffer a byte array to read from
     * @param start index from which to start writing bytes
     * @return Date
     */
    public static Date readDateFromByteArray(byte[] buffer, int start){
        byte[] fakeLongArray = Arrays.copyOfRange(buffer,start,start+4);
        byte[] longArray = new byte[8];
        for(int i=0; i<4;i++){
            longArray[i]=fakeLongArray[i];
        }
        for(int i=4; i<8;i++){
            longArray[i]=0;
        }
        ByteBuffer bb = ByteBuffer.wrap(longArray);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Date date = new Date();
        long timestamp = bb.getLong();
        if(timestamp==0) return  null;
        date.setTime(timestamp*1000L);
        return date;
    }

    public static Long readMillisFromByteArray(byte[] buffer, int start){
        byte[] fakeLongArray = Arrays.copyOfRange(buffer,start,start+4);
        byte[] longArray = new byte[8];
        for(int i=0; i<4;i++){
            longArray[i]=fakeLongArray[i];
        }
        for(int i=4; i<8;i++){
            longArray[i]=0;
        }
        ByteBuffer bb = ByteBuffer.wrap(longArray);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Date date = new Date();
        long timestamp = bb.getLong();
        if(timestamp==0) return  null;
        return timestamp*1000L;
    }

    /**
     * Converts a Date to a byte array
     * @param aDate a date
     * @return a little endian byte array representation of the date, loses time resolution
     */
    public static byte[] writeDateToByteArray(Date aDate){
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(aDate.getTime()/1000));
        int timestamp = bigDecimal.intValue();
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(timestamp);
        return bb.array();
    }


    /**
     * Converts a byte array to a String representation
     * @param buffer a byte array
     * @return an String with the format ## ##
     */
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
