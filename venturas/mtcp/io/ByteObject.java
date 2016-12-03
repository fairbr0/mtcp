package venturas.mtcp.io;

import java.util.*;
import java.net.*;
import java.io.*;

public class ByteObject {
	int paddingSize;
	private int length;

	public ByteObject () {
		this.paddingSize = 0;
		this.length = 0;
	}

	public void setValues(byte[] array) {

		byte[] paddingSize = new byte[4];
		byte[] length = new byte[4];
		//arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
		System.arraycopy(array, 0, paddingSize, 0, 4);
		System.arraycopy(array, 4, length, 0, 4);

		this.paddingSize = (int) byteToInt(paddingSize);
		this.length = (int) byteToInt(length);
	}

	public byte[] returnArray(int paddingSize, int length, int arrayLength) {
		byte[] returnArray = new byte[arrayLength];
		byte[] paddingArray = intToByteArray(paddingSize);
		byte[] lengthArray = intToByteArray(length);

		System.arraycopy(paddingArray, 0, returnArray, 0, 4);
		System.arraycopy(lengthArray, 0, returnArray, 4, 4);

		return returnArray;
	}

	public byte[] intToByteArray(int value) {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value};
		}

	public long byteToInt(byte[] bytes) {
        int val = 0;
        for (int i = 0; i < bytes.length; i++) {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }
    return val;
    }

	public int size() {
		return this.length;
	}

}
