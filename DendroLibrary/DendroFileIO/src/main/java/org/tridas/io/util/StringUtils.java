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

import java.util.StringTokenizer;


/**
 * Some handy string utilities. <h2>Left to do</h2>
 * <ul>
 * <li>splitBy() would be shorter/simpler if it used a tokenizer (see StringTokenizer,
 * countTokens(), nextToken())
 * <li>splitBy() might be exactly the same as browser/SearchField.parseIntoWords() --
 * check it out
 * </ul>
 * Note from daniel--splitBy looks awfully replacable by String.split
 * 
 * @author Ken Harris &lt;kbh7 <i style="color: gray">at</i> cornell <i
 *         style="color: gray">dot</i> edu&gt;
 * @version $Id: StringUtils.java 1015 2008-02-19 21:11:33Z lucasm $
 */
public class StringUtils {
	// don't instantiate me
	private StringUtils() {}
	
	/**
	 * Pad some text on the left (i.e., right-align it) until it's a specified
	 * width. If the text is already longer than the desired length, it is
	 * returned.
	 * 
	 * @param text
	 *            string to pad
	 * @param size
	 *            length of resulting string
	 * @return the original text, padded on the left
	 */
	public static String leftPad(String text, int size) {
		int numSpaces = size - text.length();
		if (numSpaces <= 0) {
			return text;
		}
		
		StringBuffer buf = new StringBuffer(size);
		
		for (int i = 0; i < numSpaces; i++) {
			buf.append(' ');
		}
		for (int i = numSpaces; i < size; i++) {
			buf.append(text.charAt(i - numSpaces));
		}
		
		return buf.toString();
	}
	
	/**
	 * Returns a string of a specified length. If supplied text is longer
	 * than size then the string is truncated and returned. Otherwise it
	 * returns the string padded with spaces.
	 * 
	 * @param text
	 *            string to pad
	 * @param size
	 *            length of resulting string
	 * @return the original text, padded on the right if required
	 */
	public static String rightPadWithTrim(String text, int size) {
		
		if (text.length() >= size) {
			return text.substring(0, size);
		}
		
		return org.apache.commons.lang.StringUtils.rightPad(text, size);
		
	}
	
	public static String rightPad(String text, int size) {
		return org.apache.commons.lang.StringUtils.rightPad(text, size);
	}
	
	/**
	 * by daniel, for integer formatting
	 * 
	 * @param argNum
	 * @param argNumCharacters
	 * @return
	 */
	public static String addLefthandZeros(int argNum, int argNumCharacters) {
		String stringNum = argNum + "";
		if (stringNum.length() >= argNumCharacters) {
			return stringNum;
		}
		stringNum = org.apache.commons.lang.StringUtils.leftPad(stringNum, argNumCharacters);
		stringNum = stringNum.replace(' ', '0');
		return stringNum;
	}
	
	/**
	 * Chops the string into smaller strings with size of
	 * argLength. by daniel
	 * 
	 * @param argString
	 * @param argLength
	 * @return
	 */
	public static String[] chopString(String argString, int argLength) {
		if (argString.length() == 0) {
			return new String[0];
		}
		if (argString.length() <= argLength) {
			return new String[]{argString};
		}
		
		String[] ret = new String[(int) Math.ceil(argString.length() / argLength)];
		int i;
		for (i = 0; i < ret.length - 1; i++) {
			ret[i] = argString.substring(i * argLength, (i + 1) * argLength);
		}
		ret[i] = argString.substring(i * argLength);
		// the last one, so we don't get the
		// out of bounds exception
		return ret;
	}
	
	/**
	 * Split some text into lines. For example, <code>"a\nb\nc"</code> becomes
	 * <code>String[]
       {"a","b","c"}</code>.
	 * 
	 * @param text
	 *            the text, separated by <code>'\n'</code> chars
	 * @return the string, as an array of strings
	 */
	public static String[] splitByLines(String text) {
		return splitBy(text, '\n');
	}
	
	/**
	 * Split some text, using an arbitrary separator character.
	 * 
	 * @param text
	 *            the text
	 * @param sep
	 *            the separator character to watch for
	 * @return the string, as an array of strings
	 */
	public static String[] splitBy(String text, char sep) {
		// count separators
		int newlines = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == sep) {
				newlines++;
			}
		}
		
		// allocate space for output
		String result[] = new String[newlines + 1];
		int n = 0; // next space to fill
		
		// create output
		int start = 0;
		for (;;) {
			int newline = text.indexOf(sep, start);
			if (newline == -1) {
				result[n++] = text.substring(start); // (to end)
				break;
			}
			else {
				result[n++] = text.substring(start, newline);
				start = newline + 1;
			}
		}
		
		// return it
		return result;
	}
	
	/**
	 * Get a specified number of species
	 * 
	 * @param num
	 * @return
	 */
	public static String getSpaces(int num) {
		String spaces = "";
		for (int i = 0; i < num; i++) {
			spaces += " ";
		}
		return spaces;
	}
	
	/**
	 * Convert a sequence of integers in a string into an array of ints.
	 * <p>
	 * For example, extractInts("1 2 3") = int[] { 1, 2, 3 }.
	 * </p>
	 * <p>
	 * Bug: what happens if the string isn't parseable?
	 * </p>
	 * 
	 * @param s
	 *            the string to parse
	 * @return an array of ints, representing the string
	 */
	public static int[] extractInts(String s) {
		StringTokenizer tok = new StringTokenizer(s, " ");
		int n = tok.countTokens();
		int r[] = new int[n];
		for (int i = 0; i < n; i++) {
			r[i] = Integer.parseInt(tok.nextToken());
		}
		return r;
	}
	
	/**
	 * Given a string, escape any &lt; &gt; &amp; ' " characters for XML. Also,
	 * if any characters are unprintable, they're escaped as raw values
	 * (&amp;#xxxx;), so loading the output in any old text editor shouldn't
	 * mangle anything.
	 * 
	 * @param input
	 *            a string
	 * @return the same string, with &lt;/&gt;/&amp; escaped
	 */
	public static String escapeForXML(String input) {
		// FIXME: if there are no <>& symbols, just return the string
		// as-is to save the GC
		
		// MAYBE: does SAX or somebody already have a method that does
		// this better?
		
		// MAYBE: use regexps in 1.4?
		
		// BETTER: isn't there a String.replace() or something that
		// would do this in about 2 lines?
		
		StringBuffer output = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			switch (c) {
				case '&' :
					output.append("&amp;");
					break;
				case '<' :
					output.append("&lt;");
					break;
				case '>' :
					output.append("&gt;");
					break;
				case '\"' :
					output.append("&quot;");
					break;
				case '\'' :
					output.append("&apos;");
					break;
				default :
					if (Character.isISOControl(c)) {
						// if it's not printable, &#x-escape it.
						
						// (this came up when trying to save
						// "<dating>^D</dating>" which shouldn't happen,
						// anyway, but better safe than sorry.)
						
						// BUG: aren't there non-iso-control characters
						// which won't show up correctly? do they need
						// the right encoding=""? do they have it?
						
						if (c < 32) {
							output.append("ILLEGAL-XML-CHAR:");
						}
						else {
							output.append("&#x");
						}
						
						String hex = Integer.toHexString(c);
						for (int ii = 0; ii < 4 - hex.length(); ii++) {
							output.append("0");
						}
						output.append(hex);
						
						output.append(";");
					}
					else {
						output.append(c);
					}
			}
		}
		return output.toString();
	}
	
	/**
	 * escapeForCSV
	 * given a string, replace any occurances of " with double "'s, and return
	 * it surrounded in "'s
	 * 
	 * @param input
	 *            a string
	 * @return the same string, with "'s escaped
	 */
	
	public static String escapeForCSV(String input) {
		StringBuffer output = new StringBuffer();
		
		output.append("\"");
		
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			output.append(c);
			if (c == '"') {
				output.append(c);
			}
		}
		
		output.append("\"");
		
		return output.toString();
	}
	
	/**
	 * In a string, replace one substring with another.
	 * <p>
	 * If str contains source, return a new string with (the first occurrence of) source
	 * replaced by target.
	 * </p>
	 * <p>
	 * If str doesn't contain source (or str is the empty string), returns str.
	 * </p>
	 * <p>
	 * Think: str ~= s/source/target/
	 * </p>
	 * <p>
	 * This is like Java 1.4's String.replaceFirst() method; when I decide to drop support
	 * for Java 1.3, I can use that method instead.
	 * </p>
	 * 
	 * @param str
	 *            the base string
	 * @param source
	 *            the substring to look for
	 * @param target
	 *            the replacement string to use
	 * @return the original string, str, with its first instance of source
	 *         replaced by target
	 */
	public static String substitute(String str, String source, String target) {
		int index = str.indexOf(source);
		if (index == -1) {
			return str;
		}
		int start = index, end = index + source.length();
		return str.substring(0, start) + target + str.substring(end);
	}
	
	private static final char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f'};
	
	public static String bytesToHex(byte[] in) {
		StringBuffer hex = new StringBuffer();
		
		for (int i = 0; i < in.length; i++) {
			int msb = (in[i] & 0xFF) / 16;
			int lsb = (in[i] & 0xFF) % 16;
			
			hex.append(hexChars[msb]);
			hex.append(hexChars[lsb]);
		}
		
		return hex.toString();
	}
	
	/**
	 * Returns upper case initials from a persons name.  Expects fullName to be 
	 * in the format 'Firstname Surname' or 'Surname, Firstname'.  If fullName is 
	 * in a different format then it simply returns the first two characters with 
	 * case unchanged.
	 * 
	 * @param fullName
	 * @return
	 */
	public static String parseInitials(String fullName)
	{
		if(fullName==null)
		{
			return null;
		}
		
		if(fullName.length()<=2)
		{
			return fullName;
		}
		
		// Check if names are comma delimited.  If so, then 
		// assume 'Surname, Firstname'.
		String[] names = fullName.split(", ");
		if(names.length==2)
		{
			return names[1].substring(0,1).toUpperCase() + names[0].substring(0,1).toUpperCase();
		}
		else if(names.length>1)
		{
			// Just return first two letters instead but leave case alone
			return fullName.substring(0,2);
		}
		
		// Now try simple space delimited assuming 'Firstname Surname'.
		names = fullName.split("\\s");
		if(names.length==2)
		{
			return names[0].substring(0,1).toUpperCase() + names[1].substring(0,1).toUpperCase();
		}
		else
		{
			// Just return first two letters instead but leave case alone
			return fullName.substring(0,2);
		}
	}
	
	/**
	 * Returns true if string can be interpreted as a whole number integer.  If
	 * string includes a decimal place it will still return true as long as the 
	 * decimal portion is zero.
	 * 
	 * "130"   = TRUE
	 * "130.0" = TRUE
	 * "130.4" = FALSE
	 * "blah"  = FALSE
	 * 
	 * @param str
	 * @return
	 */
	public static Boolean isStringWholeInteger(String str)
	{
		try {
			Double dbl = Double.valueOf(str);	
			long iPart = (long) ((double)dbl);
			double fPart = dbl - iPart;
			
			if(fPart > 0)
			{
				return false;
			}
			
		} catch (NumberFormatException e2) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Takes a full name and returns the initials.  If name contains a comma
	 * then it is treated as "Lastname, Firstname".  If name is equal or shorter
	 * than charLimit then it is returned as is.
	 * 
	 * @param name
	 * @param charLimitGuide = 2
	 * @return
	 */
	public static String getIntialsFromName(String name, Integer charLimitGuide)
	{
		if(name==null || name.length()==0)
		{
			return null;
		}
	
		name = name.trim();
		
		// If name is already shorter than the number of chars allowed,
		// or the name doesn't contain spaces then simply return.
		if(charLimitGuide==null) charLimitGuide = 2;
		if(name.length()<=charLimitGuide || !name.contains(" ")) return StringUtils.rightPadWithTrim(name, charLimitGuide).trim();
		
		// if name is 3 or fewer characters and they are all upper case
		// presume they are already initials
		if(name.length()<=3)
		{
			Boolean isUpperCase = true;
			for(int i =0; i<name.length(); i++)
			{
				char ch = name.charAt(i);
				if(!Character.isUpperCase(ch))
				{
					isUpperCase = false;
					break;
				}
			}
			if(isUpperCase) return StringUtils.rightPadWithTrim(name, charLimitGuide).trim();
		}
		
		
		// Try and grab initials 
		StringBuilder sbInitials = new StringBuilder();
		String[] nameParts = name.split(" ");	
		
		if(name.contains(","))
		{
			name = name.replace(",", " ");
			nameParts = name.split("\\s");	
			
			// Name contains a comma so treat names as "Lastname, Firstname"
			for (int i = 1; i<nameParts.length; i++)
			{
				String part = nameParts[i];
				part = part.trim();
				if(part.length()>0) sbInitials.append(part.charAt(0));
			}
			sbInitials.append(name.charAt(0));
			return StringUtils.rightPadWithTrim(sbInitials.toString().toUpperCase(), charLimitGuide).trim();
		}
		else
		{
			for (String part : nameParts)
			{
				String part2 = part.trim();
				if(part2.length()>0) sbInitials.append(part2.charAt(0));
			}
			return StringUtils.rightPadWithTrim(sbInitials.toString().toUpperCase(), charLimitGuide).trim();
		}

	}
	
}
