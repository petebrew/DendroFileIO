/**
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tridas.io.util;

import java.math.BigInteger;

import org.tridas.schema.Certainty;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.Year;

/**
 * This is a year format using the astronomical conventions rather than BC/AD calendar. 
 * The value 0 is valid in the astronomical convention.  
 *  1 = 1AD
 *  0 = 1BC
 * -1 = 2BC 
 * 
 * Like Numbers and Strings, are immutable, so they are not Cloneable (there's no
 * reason for them to be).
 * </p>
 */
public final class AstronomicalYear implements Comparable {
	/** The default year: 1001. */
	public static final AstronomicalYear DEFAULT = new AstronomicalYear(1);
	
	/** Holds the year value as an <code>int</code>. */
	private final int y;
	
	/**
	 * Default constructor. Uses <code>DEFAULT</code> as the year.
	 * 
	 * @see #DEFAULT
	 */
	public AstronomicalYear() {
		y = DEFAULT.y;
	}
	
	/**
	 * Constructor for <code>int</code>s.
	 * 
	 * @param x
	 *            the year value, as an int
	 * @see #DEFAULT
	 */
	public AstronomicalYear(int x) {
		y = x;
	}
	
	/**
	 * Construct a AstronomicalYear from a native TridasYear. The TridasYear allows the
	 * use of suffixes (BP, AD, BC).
	 * problem.
	 * 
	 * @param x
	 */
	public AstronomicalYear(Year x) {
		int val = 0;
		BigInteger xBigInt = BigInteger.valueOf(x.getValue());
		switch (x.getSuffix()) {
			case AD :
				val = x.getValue().intValue();
				break;
			case BC :
				val = xBigInt.negate().intValue() +1 ;
				break;
			case BP :
				AstronomicalYear radioCarbonEra = new AstronomicalYear(1950);
				val = Integer.parseInt(radioCarbonEra.add(Integer.parseInt(xBigInt.negate().toString()))
						.toString());
				break;
		}
		
		y = val;
	}
	
	/**
	 * Construct a AstronomicalYear from a SafeIntYear. 
	 * 
	 * @param x
	 */
	public AstronomicalYear(SafeIntYear x) {
		
		y = Integer.parseInt(x.toAstronomicalYear().toString());
	}
	
	/**
	 * Constructor from (row,col) pair. Assumes 10-year rows. The column should
	 * always be between 0 and 9, inclusive.
	 * 
	 * @param row
	 *            the row; row 0 is the decade ending in year 9
	 * @param col
	 *            the column; in row 0, year is the column
	 */
	public AstronomicalYear(int row, int col) {
		int yy = 10 * row + col;

		y = yy;
	}
	
	/**
	 * Constructor from String.  The string should be in astronomical format
	 * where 0 is valid and is equal to 1BC.
	 * 
	 * @exception NumberFormatException
	 *                if the String cannot be parsed
	 * @see java.lang.String
	 */
	public AstronomicalYear(String s) throws NumberFormatException {	
		y = Integer.parseInt(s.trim());
	}

	
	/**
	 * Convert to a String
	 * 
	 * @return this year as a String
	 * @see java.lang.String
	 */
	@Override
	public String toString() {
		return String.valueOf(y);
	}
	
	/**
	 * This method always throws UnsupportedOperationException. It's not
	 * implemented, and don't even think about implementing it yourself! It
	 * encourages being lazy and bypassing Year's methods to just deal with
	 * ints. And that defeats the whole purpose of having Years. So I'll just
	 * disallow it. You don't need it anyway. If you really need the int for
	 * some reason I can't imagine, you can always do
	 * <code>Integer.parseInt(y.toString())</code>. That way you know you're
	 * doing it to get the int, and not for imagined performance or convenience
	 * reasons.
	 * 
	 * @return never returns
	 * @exception UnsupportedOperationException
	 *                always!
	 */
	public int intValue() {
		// i pity th' fool who tries to use intvalue!
		throw new UnsupportedOperationException();
	}
	
	public Year toTridasYear(DatingSuffix suffix) {
		
		return toSafeIntYear().toTridasYear(suffix);
		
	}
	
	public SafeIntYear toSafeIntYear()
	{
		if(y<=0)
		{
			return new SafeIntYear(y-1);
		}
		else
		{
			return new SafeIntYear(y);
		}
	}
	
	/**
	 * Return true, iff this is year 1. (This actually comes up fairly often.)
	 * 
	 * @return true iff this is year 1
	 */
	public boolean isYearOne() {
		return (y == 1);
	}
	
	/**
	 * The maximum (later) of two years.
	 * 
	 * @return the later of two years
	 */
	public static AstronomicalYear max(AstronomicalYear y1, AstronomicalYear y2) {
		return (y1.y > y2.y ? y1 : y2);
	}
	
	/**
	 * The minimum (earlier) of two years.
	 * 
	 * @return the earlier of two years
	 */
	public static AstronomicalYear min(AstronomicalYear y1, AstronomicalYear y2) {
		return (y1.y < y2.y ? y1 : y2);
	}
	
	/**
	 * Adds (or subtracts, for negative values) some number of years, and
	 * generates a new Year object.
	 * 
	 * @param dy
	 *            the number of years to add (subtract)
	 * @see #diff
	 */
	public AstronomicalYear add(int dy) {
		// copy, and convert to zys
		int r = y;
		if (r < 0) {
			r++;
		}
		
		// add dy
		r += dy;
		
		// convert back, and return
		if (r <= 0) {
			r--;
		}
		return new AstronomicalYear(r);
	}
	
	/**
	 * Calculate the number of years difference between two years. That is,
	 * there are this many years difference between <code>this</code> and <code>y2</code>;
	 * if they are equal, this number is zero.
	 * 
	 * @param y2
	 *            the year to subtract
	 * @return the number of years difference between <code>this</code> and
	 *         <code>y2</code>
	 * @see #add
	 */
	public int diff(AstronomicalYear y2) {
		// copy, and convert to zys
		int i1 = y;
		if (i1 < 0) {
			i1++;
		}
		
		int i2 = y2.y;
		if (i2 < 0) {
			i2++;
		}
		
		// subtract, and return
		return i1 - i2;
	}
	
	/**
	 * Computes <code>this</code> modulo <code>m</code>. Always gives a positive
	 * result, even for negative numbers, so it is suitable for computing a grid
	 * position for a span of years.
	 * 
	 * @param m
	 *            base for modulo
	 * @return the year modulo <code>m</code>
	 */
	public int mod(int m) {
		int r = y % m;
		if (r < 0) {
			r += m;
		}
		return r;
	}
	
	/**
	 * Determines what row this year would be, if years were in a grid 10 wide,
	 * with the left column years ending in zero. Row 0 is years 1 through 9.
	 * 
	 * @return this year's row
	 * @see #column
	 */
	public int row() {
		int z = y / 10;
		if (y < 0 && y % 10 != 0) {
			z--;
		}
		return z;
	}
	
	/**
	 * Determines what column this year would be, if years were in a grid 10
	 * wide, with the left column years ending in zero.
	 * Works for BC years, also:
	 * <table border="1" cellspacing="0">
	 * <tr>
	 * <th>column()</th>
	 * <td>0</td>
	 * <td>1</td>
	 * <td>2</td>
	 * <td>3</td>
	 * <td>4</td>
	 * <td>5</td>
	 * <td>6</td>
	 * <td>7</td>
	 * <td>8</td>
	 * <td>9</td>
	 * </tr>
	 * <tr>
	 * <th rowspan="3">Year</th>
	 * <td>-10</td>
	 * <td>-9</td>
	 * <td>-8</td>
	 * <td>-7</td>
	 * <td>-6</td>
	 * <td>-5</td>
	 * <td>-4</td>
	 * <td>-3</td>
	 * <td>-2</td>
	 * <td>-1</td>
	 * </tr>
	 * <tr>
	 * <td></td>
	 * <td>1</td>
	 * <td>2</td>
	 * <td>3</td>
	 * <td>4</td>
	 * <td>5</td>
	 * <td>6</td>
	 * <td>7</td>
	 * <td>8</td>
	 * <td>9</td>
	 * </tr>
	 * <tr>
	 * <td>10</td>
	 * <td>11</td>
	 * <td>12</td>
	 * <td>13</td>
	 * <td>14</td>
	 * <td>15</td>
	 * <td>16</td>
	 * <td>17</td>
	 * <td>18</td>
	 * <td>19</td>
	 * </tr>
	 * </table>
	 * 
	 * @return this year's column
	 * @see #row
	 */
	public int column() {
		return mod(10);
	}
	
	/**
	 * Compares this and <code>o</code>.
	 * 
	 * @see java.lang.Comparable
	 * @param o
	 *            Object to compare
	 * @return >0, =0, or <0 if this is greater-than, equal-to, or less-than o
	 * @throws ClassCastException
	 *             if o is not a Year
	 */
	public int compareTo(Object o) {
		return y - ((AstronomicalYear) o).y;
	}
	
	/**
	 * Returns <code>true</code> if and only if <code>this</code> is equal to
	 * <code>y2</code>.
	 * 
	 * @param y2
	 *            the year to compare <code>this</code> to
	 * @return <code>true</code> if <code>this</code> is equal to <code>y2</code>, else
	 *         <code>false</code>
	 */
	@Override
	public boolean equals(Object y2) {
		return (y == ((AstronomicalYear) y2).y);
	}
	
	// since i define equals(), i need to define hashCode()
	@Override
	public int hashCode() {
		// returning something based on y is logical, but returning y
		// itself might make people mistakenly think this is like
		// intValue(), so let's do something weird to it first.
		return y * y * y;
	}
	

}
