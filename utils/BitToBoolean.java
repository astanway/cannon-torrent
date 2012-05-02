package utils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Class to convert between bit-mapped {@code byte[]} and {@code boolean[]}.
 * 
 * @author Robert Moore
 */
public class BitToBoolean {

	/**
	 * 
	 * @param array atomic integer array that is our bitfield
	 * @return boolean array of what we have
	 */
	public static boolean[] convert(AtomicIntegerArray array) {
		boolean[] retVal = new boolean[array.length()];
		for (int i = 0; i < array.length(); i++) {
			boolean val = false;
			if (array.get(i) == 1) {
				val = true;
			}
			retVal[i] = val;
		}

		return retVal;
	}

	/**
	 * Converts a {@code byte[]} to a {@code boolean[]}. It is assumed that the
	 * values are in most-significant-bit first order. Meaning that most
	 * significant bit of the 0th byte of {@code bits} is the first boolean
	 * value.
	 * 
	 * @param bits
	 *            a binary array of boolean values stored as a {@code byte[]}.
	 * @param significantBits
	 *            the number of important bits in the {@code byte[]}, and
	 *            therefore the length of the returned {@code boolean[]}
	 * @return a {@code boolean[]} containing the same boolean values as the
	 *         {@code byte[]}
	 */
	public static boolean[] convert(byte[] bits, int significantBits) {
		boolean[] retVal = new boolean[significantBits];
		int boolIndex = 0;
		for (int byteIndex = 0; byteIndex < bits.length; ++byteIndex) {
			for (int bitIndex = 7; bitIndex >= 0; --bitIndex) {
				if (boolIndex >= significantBits) {
					// Bad to return within a loop, but it's the easiest way
					return retVal;
				}
				retVal[boolIndex++] = (bits[byteIndex] >> bitIndex & 0x01) == 1 ? true
						: false;
			}
		}
		return retVal;
	}

	/**
	 * Converts a {@code byte[]} to a {@code boolean[]}. It is assumed that the
	 * values are in most-significant-bit first order. Meaning that most
	 * significant bit of the 0th byte of {@code bits} is the first boolean
	 * value.
	 * 
	 * @param bits
	 *            a binary array of boolean values stored as a {@code byte[]}.
	 * @return a {@code boolean[]} containing the same boolean values as the
	 *         {@code byte[]}
	 */
	public static boolean[] convert(byte[] bits) {
		return BitToBoolean.convert(bits, bits.length * 8);
	}

	/**
	 * Converts an {@code boolean[]} to a {@code byte[]} where each bit of the
	 * {@code byte[]} contains a 1 bit for a {@code true} value, and a 0 bit for
	 * a {@code false} value. The {@code byte[]} will contain the 0th index
	 * {@code boolean} value in the most significant bit of the 0th byte.
	 * 
	 * @param bools
	 *            an array of boolean values
	 * @return a {@code byte[]} containing the boolean values of {@code bools}
	 *         as bits.
	 */
	public static byte[] convert(boolean[] bools) {
		int length = bools.length / 8;
		int mod = bools.length % 8;
		if (mod != 0) {
			++length;
		}
		byte[] retVal = new byte[length];
		int boolIndex = 0;
		for (int byteIndex = 0; byteIndex < retVal.length; ++byteIndex) {
			for (int bitIndex = 7; bitIndex >= 0; --bitIndex) {
				// Another bad idea
				if (boolIndex >= bools.length) {
					return retVal;
				}
				if (bools[boolIndex++]) {
					retVal[byteIndex] |= (byte) (1 << bitIndex);
				}
			}
		}

		return retVal;
	}

	/**
	 * A method for testing the conversion between the {@code byte[]} and
	 * {@code boolean[]} representations.
	 * 
	 * @param args
	 *            ignored.
	 */
	public static void main(String[] args) {
		byte[] testBytes = new byte[] { (byte) 0xF8, 0x4A };

		//System.out.println(Arrays.toString(convert(testBytes, 3)));

		boolean[] testBools = new boolean[] { true, false, true, false, false,
				true, false, true, true };
		byte[] convertedBools = convert(testBools);
		for (byte b : convertedBools) {
			//System.out.print(Integer.toHexString(b & 0xFF) + ", ");
		}
	}
}