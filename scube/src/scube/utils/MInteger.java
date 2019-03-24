package scube.utils;

import java.io.Serializable;

/** Modifiable double. */
public class MInteger extends Number implements Comparable<MInteger>, Serializable {
	private static final long serialVersionUID = -2834317748757937109L;
	/** Value. */
	public int value;

	/** Constructor. */
	public MInteger(String arg0) {
		value = Integer.parseInt(arg0);
	}

	/** Constructor. */
	public MInteger() {
		value = 0;
	}

	public MInteger(int i) {
		value = i;
	}

	@Override
	public int compareTo(MInteger o) {
		if (value < o.value)
			return -1;
		if (value > o.value)
			return 1;
		return 0;
	}

	public boolean equals(Object o) {
		return value == ((MInteger) o).value;
	}

	public int hashCode() {
		return value;
	}
	@Override
	public String toString() {
		return "" + value;
	}

	@Override
	public double doubleValue() {
		// TODO Auto-generated method stub
		return (double) this.value;
	}

	@Override
	public float floatValue() {
		// TODO Auto-generated method stub
		return new Integer(value).floatValue();
	}

	@Override
	public int intValue() {
		// TODO Auto-generated method stub
		return this.value;
	}

	@Override
	public long longValue() {
		// TODO Auto-generated method stub
		return new Integer(value).longValue();
	}
}
