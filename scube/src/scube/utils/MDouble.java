package scube.utils;

import java.io.Serializable;

/** Modifiable double. */
public class MDouble extends Number implements Comparable<MDouble>, Serializable  {
	private static final long serialVersionUID = -8766521224787252767L;
	/** Value. */
	public double value;
	
	/** Constructor. */
	public MDouble(String arg0) {
		value = Double.parseDouble(arg0);
	}
	/** Constructor. */
	public MDouble(double n) {
		value = n;
	}
	/** Constructor. */
	public MDouble() {
		value = 0;
	}
	
	@Override
	public int compareTo(MDouble o) {
		if( value < o.value )
			return -1;
		if( value > o.value )
			return 1;
		return 0;
	}
	
	public boolean equals(Object o) {
		return value == ((MDouble)o).value;
	}
	
	public int hashCode() {
		return (int)value;
	}
	@Override
	public String toString() {
		return ""+value;
	}
	@Override
	public double doubleValue() {
		// TODO Auto-generated method stub
		return this.value;
	}
	@Override
	public float floatValue() {
		// TODO Auto-generated method stub
		return new Double(value).floatValue();
	}
	@Override
	public int intValue() {
		// TODO Auto-generated method stub
		return new Double(value).intValue();
	}
	@Override
	public long longValue() {
		// TODO Auto-generated method stub
		return  new Double(value).longValue();
	}
}
