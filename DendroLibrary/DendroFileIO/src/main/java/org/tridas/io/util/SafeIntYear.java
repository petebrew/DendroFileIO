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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.schema.Certainty;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.Year;

/**
 * <p>
 * A BC/AD calendar year in the form of a signed integer It normally acts similar to an
 * integer, but skips the mythical "year 0".
 * </p>
 * <p>
 * In <code>SafeIntYear</code> math:
 * </p>
 * <ul>
 * <li>-1 + 1 = 1</li>
 * <li>2 - 4 = -3</li>
 * </ul>
 * <p>
 * Years, like Numbers and Strings, are immutable, so they are not Cloneable (there's no
 * reason for them to be).
 * </p>
 * 
 * @author Ken Harris &lt;kbh7 <i style="color: gray">at</i> cornell <i
 *         style="color: gray">dot</i> edu&gt;
 * @version $Id: Year.java 1671 2009-04-29 22:11:14Z lucasm $
 */
@SuppressWarnings("unchecked")
public final class SafeIntYear implements Comparable {
	private static final Logger log = LoggerFactory.getLogger(SafeIntYear.class);

	/** The default year: 1. */
	public static final SafeIntYear DEFAULT = new SafeIntYear(1);
	
	/** Holds the year value as an <code>int</code>. */
	private final int y;
	
	/**
	 * Default constructor. Uses <code>DEFAULT</code> as the year.
	 * 
	 * @see #DEFAULT
	 */
	public SafeIntYear() {
		y = DEFAULT.y;
	}
	
	/**
	 * Constructor for <code>int</code>s. Uses <code>DEFAULT</code> as the year
	 * if an invalid value is passed.
	 * 
	 * @param x
	 *            the year value, as an int
	 * @see #DEFAULT
	 */
	public SafeIntYear(int x) {
		y = (x == 0 ? DEFAULT.y : x);
	}
	
	/**
	 * Construct a SafeIntYear from a native TridasYear. The TridasYear allows the
	 * use of suffixes (BP, AD, BC) but does not know how to handle the 0BC/AD
	 * problem.
	 * 
	 * @param x
	 */
	public SafeIntYear(Year x) {
		int val = 0;
		BigInteger xBigInt = BigInteger.valueOf(x.getValue());
		switch (x.getSuffix()) {
			case AD :
				val = x.getValue().intValue();
				y = (val == 0 ? DEFAULT.y : val);
				return;
			case BC :
				val = xBigInt.negate().intValue();
				y = (val == 0 ? DEFAULT.y : val);
				return;
			case BP :
				SafeIntYear radioCarbonEra = new SafeIntYear(1950);
				val = Integer.parseInt(radioCarbonEra.add(Integer.parseInt(xBigInt.negate().toString()))
						.toString());
				y = val;
				return;
			case RELATIVE:			
				val = x.getValue().intValue();
				if(val<=0) 
				{
					y = val-1;
				}
				else
				{
					y = val;
				}
				return;
			default:
				log.error("Error creating SafeIntYear.  Unhandled year suffix.");
				val = x.getValue().intValue();
				y = val;
		}
		
		
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
	public SafeIntYear(int row, int col) {
		int yy = 10 * row + col;
		if (yy == 0) {
			yy = DEFAULT.y; // should this be 1?
		}
		y = yy;
	}
	
	/**
	 * Constructor from String. No AD/BC; reads it like C's <code>scanf(" %d ", &y)</code>
	 * would.
	 * 
	 * @exception NumberFormatException
	 *                if the String cannot be parsed, or is equal to zero
	 * @see java.lang.String
	 */
	public SafeIntYear(String s) throws NumberFormatException {
		y = Integer.parseInt(s.trim());
		if (y == 0) {
			throw new NumberFormatException();
		}
	}
	
	/**
	 * Constructor from String. No AD/BC; reads it like C's <code>scanf(" %d ", &y)</code>
	 * would. If isAstronomical is true, then it subtracts 1 from all values which are <=0.
	 * i.e., -5 means 6 BC.
	 * 
	 * @exception NumberFormatException
	 *                if the String cannot be parsed
	 * @see java.lang.String
	 */
	public SafeIntYear(String s, boolean isAstronomical) throws NumberFormatException {
		int yy = Integer.parseInt(s.trim());
		
		// back up a year, if this system assumed a zero-year
		if (isAstronomical && yy <= 0) {
			yy--;
		}
		y = yy;
	}
	
	/**
	 * Convert to a String. No "AD"/"BC"; simply the integer value.
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
	
    private String getCorrectSuffix(DatingSuffix suffix)
    {
            BigInteger yBigInt = BigInteger.valueOf(y);
            if (suffix == DatingSuffix.AD || suffix == DatingSuffix.BC) {

                    if(y>0)
                    {
                            return "AD";
                    }
                    else
                    {
                            return "BC";
                    }

            }
            else if (suffix == DatingSuffix.BP) {
                    if (y > 0) {
                            return (1950 - y)+"BP";
                    }
                    if (y < 0) {
                            return Integer.valueOf((yBigInt.negate()).add((BigInteger.valueOf(1950))).toString()) + "BP";
                    }
            }
            else if (suffix == DatingSuffix.RELATIVE)
            {
                    return "Rel.Yr.";
            }
            else
            {
                    log.error("Unhandled date suffix type");
            }

            return null;
    }

	
    /**
     * Return this year as a formatted string depending on the dating type of this year
     * and the preferred dating suffix. 
     *   
     * @param datingtype
     * @param suffix
     * @return
     */
    public String formattedYear(NormalTridasDatingType datingtype, DatingSuffix suffix)
    {


            if(datingtype==null || datingtype.equals(NormalTridasDatingType.RELATIVE))
            {
                    return getCorrectSuffix(DatingSuffix.RELATIVE)+" "+y;
            }
            else if (datingtype.equals(NormalTridasDatingType.DATED_WITH_UNCERTAINTY) ||
                            datingtype.equals(NormalTridasDatingType.RADIOCARBON))
            {
                    return "\u2248"+y+" "+getCorrectSuffix(suffix);
            }
            else if (datingtype.equals(NormalTridasDatingType.ABSOLUTE))
            {
                    return Math.abs(y)+" "+getCorrectSuffix(suffix);
            }

            return y+"";


    }

	
	public Year toTridasYear(DatingSuffix suffix) {
		
		BigInteger yBigInt = BigInteger.valueOf(y);

		Year yr = new Year();
		
		
		if (suffix == DatingSuffix.AD || suffix == DatingSuffix.BC) {
			yr.setCertainty(Certainty.EXACT);
			yr.setValue(Integer.valueOf(yBigInt.abs().toString()));
			if (y > 0) {
				yr.setSuffix(DatingSuffix.AD);
			}
			else {
				yr.setSuffix(DatingSuffix.BC);
			}
		}
		else if (suffix == DatingSuffix.BP) {
			yr.setCertainty(Certainty.EXACT);
			yr.setSuffix(DatingSuffix.BP);
			if (y > 0) {
				yr.setValue(1950 - y);
			}
			if (y < 0) {
				yr.setValue(Integer.valueOf((yBigInt.negate()).add((BigInteger.valueOf(1950))).toString()));
			}
		}
		else if (suffix == DatingSuffix.RELATIVE)
		{
			// Year is relative so use astronomical calendar
			yr.setSuffix(DatingSuffix.RELATIVE);
			yr.setValue(this.toAstronomicalInteger());
		}
		else
		{
			log.error("Unhandled date suffix type.  Defaulting to 'relative'");
			yr.setSuffix(DatingSuffix.RELATIVE);
			yr.setValue(this.toAstronomicalInteger());
		}
		
		return yr;
	}
	
	/**
	 * Convert to an AstronomicalYear where 0 is valid and is
	 * equal to 1BC.
	 * 
	 * @return
	 */
	public AstronomicalYear toAstronomicalYear(){
		
		if (y<0)
		{
			return new AstronomicalYear(y+1);
		}
		else
		{
			return new AstronomicalYear(y);
		}
		
	}
	
	/**
	 * Returns this SafeIntYear as an integer using the astronomical
	 * convention for BC years.
	 * 
	 * @return
	 */
	public Integer toAstronomicalInteger(){
		if (y<0)
		{
			return y+1;
		}
		else
		{
			return y;
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
	public static SafeIntYear max(SafeIntYear y1, SafeIntYear y2) {
		return (y1.y > y2.y ? y1 : y2);
	}
	
	/**
	 * The minimum (earlier) of two years.
	 * 
	 * @return the earlier of two years
	 */
	public static SafeIntYear min(SafeIntYear y1, SafeIntYear y2) {
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
	public SafeIntYear add(int dy) {
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
		return new SafeIntYear(r);
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
	public int diff(SafeIntYear y2) {
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
		return y - ((SafeIntYear) o).y;
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
		return (y == ((SafeIntYear) y2).y);
	}
	
	// since i define equals(), i need to define hashCode()
	@Override
	public int hashCode() {
		// returning something based on y is logical, but returning y
		// itself might make people mistakenly think this is like
		// intValue(), so let's do something weird to it first.
		return y * y * y;
	}
	
	// THESE TWO METHODS ARE BUGGY AND NEED WORK!
	public SafeIntYear cropToCentury() {
		return add(-mod(100)); // is this correct?
	}
	
	public SafeIntYear nextCentury() {
		SafeIntYear tmp = add(100); // COMPLETELY INCORRECT!
		if (tmp.y == 101) {
			return new SafeIntYear(100);
		}
		return tmp;
	}
}
