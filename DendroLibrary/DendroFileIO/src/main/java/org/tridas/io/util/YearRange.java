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

import org.tridas.interfaces.ITridasSeries;

/**
 * A range of years. Ranges are immutable; all otherwise-destructive operations
 * on a Range return a new Range.
 * 
 * @see SafeIntYear
 * @see TreeRingData
 * @author Ken Harris
 */
@SuppressWarnings("unchecked")
public class YearRange implements Comparable {
	
	/** Starting year of the Range. */
	private SafeIntYear start;
	
	/** Ending year of the range. */
	private SafeIntYear end;
	
	/**
	 * Construct a new empty range, starting at <code>Year.DEFAULT</code>.
	 * 
	 * @see SafeIntYear
	 */
	public YearRange() {
		// this is only used by GraphFrame (which shouldn't use it)
		// -- and Sample
		start = SafeIntYear.DEFAULT;
		end = start.add(-1);
	}
	
	/**
	 * Construct a new range, from y<sub>1</sub> to y<sub>2</sub>. (Neither year
	 * may be <code>null</code>.) If y<sub>2</sub> &lt; y<sub>1</sub>, it is an
	 * empty interval.
	 * 
	 * @param y1
	 *            starting year
	 * @param y2
	 *            ending year
	 */
	public YearRange(SafeIntYear y1, SafeIntYear y2) {
		// null argument?
		if (y1 == null || y2 == null) {
			throw new NullPointerException();
		}
		
		start = y1;
		end = y2;
		
		// empty interval?
		if (start.compareTo(end) > 0) {
			start = SafeIntYear.DEFAULT;
			end = start.add(-1);
		}
	}
	
	/**
	 * Construct a range, given a starting year and span.
	 * 
	 * @param y
	 *            the starting year
	 * @param span
	 *            the number of years
	 */
	public YearRange(SafeIntYear y, int span) {
		start = y;
		end = y.add(span - 1);
	}
	
	/**
	 * Construct a range from a String.
	 * 
	 * @param s
	 *            the String
	 */
	public YearRange(String s) {
		// (Grid.GridHandler.startElement is the only place this is used)
		
		// (ignore outside whitespace)
		String t = s.trim();
		
		// find the first dash that isn't t[0]
		int dash = t.indexOf('-', 1);
		
		// -- there must be a dash! --
		if (dash == -1) {
			throw new IllegalArgumentException();
		}
		
		// y1 is everything before, y2 is everything after
		String y1 = t.substring(0, dash);
		String y2 = t.substring(dash + 1);
		
		// construct years
		start = new SafeIntYear(y1);
		end = new SafeIntYear(y2);
	}
	
	/**
	 * Create a range from a Tridas series.  This attempts to extract a range using the
	 * interpretation.firstyear and interpretation.lastyear values.  If the firstyear is 
	 * null then it sets this to 1001.  If the last year is null, then it sets it to 
	 * first year + count of data values.
	 * 
	 * @param series
	 */
	public YearRange(ITridasSeries series)
	{

		SafeIntYear firstYear = null;
		SafeIntYear lastYear = null;
		try {
			// Try to set range using first/last year info from interpretation section
			firstYear = new SafeIntYear(series.getInterpretation().getFirstYear());
			lastYear = new SafeIntYear(series.getInterpretation().getLastYear());
			
		} catch (NullPointerException e) {
			// Otherwise set to default relative year and use count of values
			if (firstYear == null) {
				// First year is null so just use default relative year and count of values
				firstYear = new SafeIntYear();
				lastYear = firstYear.add(series.getValues().get(0).getValues().size());
			}
			else if (lastYear == null) {
				// We have firstYear but not last, so calculate last from count of values
				BigInteger intfirstyear = BigInteger.valueOf(Integer.parseInt(firstYear.toString()));
				BigInteger numofvalues = BigInteger.valueOf(series.getValues().get(0).getValues().size());
				BigInteger intlastyear = intfirstyear.add(numofvalues);
				lastYear = new SafeIntYear(intlastyear.intValue());
			}
		}
		
		start = firstYear;
		end = lastYear;
	}
	
	/**
	 * Construct a new range using Astronomical years
	 * 
	 * @param y1
	 *            starting year
	 * @param y2
	 *            ending year
	 */
	public YearRange(AstronomicalYear y1, AstronomicalYear y2) {
		// null argument?
		if (y1 == null || y2 == null) {
			throw new NullPointerException();
		}
		
		start = y1.toSafeIntYear();
		end = y2.toSafeIntYear();
		
		// empty interval?
		if (start.compareTo(end) > 0) {
			start = SafeIntYear.DEFAULT;
			end = start.add(-1);
		}
	}
	
	
	/**
	 * Get the starting year of this range.
	 * 
	 * @return the starting year
	 */
	public SafeIntYear getStart() {
		return start;
	}
	
	/**
	 * Get the ending year of this range.
	 * 
	 * @return the ending year
	 */
	public SafeIntYear getEnd() {
		return end;
	}
	
	/**
	 * Set the starting year of the range, and adjust the ending year to
	 * maintain the same length.
	 * 
	 * @param y
	 *            new starting year for the range
	 * @see #redateEndTo
	 */
	public YearRange redateStartTo(SafeIntYear y) {
		return redateBy(y.diff(start));
	}
	
	/**
	 * Redate a range by a certain number of years. Usually, you'll use
	 * redateStartTo() or redateEndTo(), which are more convenient.
	 * 
	 * @param dy
	 *            the number of years to shift this range by
	 */
	public YearRange redateBy(int dy) {
		return new YearRange(start.add(dy), end.add(dy));
	}
	
	/**
	 * Set the ending year of the range, and adjust the start year to maintain
	 * the same length.
	 * 
	 * @param y
	 *            new ending year for the range
	 * @see #redateStartTo
	 */
	public YearRange redateEndTo(SafeIntYear y) {
		return redateBy(y.diff(end));
	}
	
	/**
	 * Return the number of years spanned by this range. For example, the range
	 * 1001 - 1005 spans 5 years.
	 * 
	 * @return the span of this range (difference between start and end,
	 *         inclusive)
	 */
	public int span() {
		return end.diff(start) + 1;
	}
	
	/**
	 * Compute the number of rows this Range will take to display, assuming rows
	 * are marked off as the row() method does.
	 * 
	 * @return the number of rows this range spans
	 */
	public int rows() {
		return getEnd().row() - getStart().row() + 1;
	}
	
	/**
	 * Return a simple string representation of the range, like "1001 - 1036".
	 * 
	 * @return a string representation of the range
	 */
	@Override
	public String toString() {
		// this tends to get called a lot, so we'll memoize it.
		if (memo == null) {
			memo = start + " - " + end; // use \u2014 EM DASH?
		}
		return memo;
	}
	
	private String memo = null;
	
	/**
	 * Return a string representation of the range, including the span, like
	 * "(1001 - 1036, n=36)".
	 * 
	 * @return a string representation of the range, including span
	 */
	public String toStringWithSpan() {
		return "(" + start + " - " + end + ", n=" + span() + ")";
		// use \u2014 EM DASH?
	}
	
	/**
	 * Return true if (and only if) the given year is inside the range,
	 * inclusive.
	 * 
	 * @param y
	 *            year to check
	 * @return true if <code>y</code> is in the range, else false
	 */
	public boolean contains(SafeIntYear y) {
		return (start.compareTo(y) <= 0) && (y.compareTo(end) <= 0);
	}
	
	/**
	 * Return true if (and only if) the given range is completely inside the
	 * range, inclusive.
	 * 
	 * @param r
	 *            range to check
	 * @return true if <code>r</code> is entirely in the range, else false
	 */
	public boolean contains(YearRange r) {
		return contains(r.start) && contains(r.end);
	}
	
	/**
	 * Return true, iff this year is the start of a row. (Year 1 is considered
	 * the start of that row.)
	 * 
	 * @return true, iff this year is the start of a row
	 */
	public boolean startOfRow(SafeIntYear y) {
		return y.equals(start) || y.column() == 0 || y.isYearOne();
	}
	
	/**
	 * Return true, iff this year is the end of a row.
	 * 
	 * @return true, iff this year is the end of a row
	 */
	public boolean endOfRow(SafeIntYear y) {
		return y.equals(end) || y.column() == 9;
	}
	
	/**
	 * Return the number of years overlap between this range and the given
	 * range.
	 * 
	 * @param r
	 *            range to compare
	 * @return number of years overlap
	 */
	public int overlap(YearRange r) {
		return intersection(r).span();
	}
	
	/**
	 * The intersection of this range with r. If they don't overlap, returns an
	 * empty range (1 - -1).
	 * 
	 * @see #union
	 * @param r
	 *            the range to intersect with this range
	 * @return the intersection of this and r
	 */
	public YearRange intersection(YearRange r) {
		return new YearRange(SafeIntYear.max(start, r.start), SafeIntYear.min(end, r.end));
	}
	
	/**
	 * The union of this range with r. Since there is no concept of
	 * "range with a gap" in Corina, it assumes they overlap.
	 * 
	 * @see #intersection
	 * @param r
	 *            the range to union with this range
	 * @return the union of this and r
	 */
	public YearRange union(YearRange r) {
		YearRange newval = new YearRange(SafeIntYear.min(start, r.start), SafeIntYear.max(end, r.end));
		return newval;
	}
	
	/**
	 * Compare two ranges for equality.
	 * 
	 * @param r
	 *            range to compare with this
	 * @return true, if the ranges are equal, else false
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof YearRange) {
			YearRange r = (YearRange) o;
			return start.equals(r.start) && end.equals(r.end);
		}
		else {
			// not even a Range, can't be equal
			return false;
		}
	}
	
	/**
	 * A hash code for the Range. (Since I define equals(), I need to define
	 * hashCode().)
	 * 
	 * @return a hash code for this Range
	 */
	@Override
	public int hashCode() {
		return start.hashCode() + 2 * end.hashCode();
	}
	
	/**
	 * Compares this and o, for placing in fallback order. Fallback order sorts
	 * ranges by their ending year, latest to earliest, and then by their
	 * length, longest to shortest. (This is usually what people want when
	 * looking at bargraphs.)
	 * 
	 * @param o
	 *            Object to compare
	 * @return >0, ==0, or <0 if this is greater-than, equal-to, or less-than o
	 * @throws ClassCastException
	 *             if o is not a Range
	 */
	public int compareTo(Object o) {
		YearRange r2 = (YearRange) o;
		
		int c1 = end.compareTo(r2.end);
		if (c1 != 0) {
			return c1;
		}
		
		// negative, because fallback puts longest samples first
		int c2 = -start.compareTo(r2.start);
		return c2;
	}
}
