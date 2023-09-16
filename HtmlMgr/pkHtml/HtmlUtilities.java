/*
 * version: 2010.09.12
 * HtmlUtilities.java - Various miscallaneous utility functions
 * :tabSize=2:indentSize=2:noTabs=false:
 * :folding=none:collapseFolds=1:
 *
 * Copyright (C) 2010 Kaseluris-Nikos (HoKoNoUmo)
 * Copyright (C) 1999, 2006 Matthieu Casanova, Slava Pestov
 * Portions copyright (C) 2000 Richard S. Hall
 * Portions copyright (C) 2001 Dirk Moebius
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA	02111-1307, USA.
 */

package pkHtml;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.JOptionPane;
import javax.swing.text.Segment;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Several tools that depends on JDK only.
 *
 * @author Matthieu Casanova
 * @version $Id: HtmlUtilities.java 14923 2009-04-13 18:40:55Z shlomy $
 * @since 4.3pre5
 */
public class HtmlUtilities
{


	private HtmlUtilities(){}


	/**
	 * Escapes newlines, tabs, backslashes, and quotes in the specified
	 * string.
	 * @param str The string
	 * @since jEdit 4.3pre15
	 */
	public static String charsToEscapes(String str)
	{
		return charsToEscapes(str,"\n\t\\\"'");
	}


	/**
	 * Escapes the specified characters in the specified string.
	 * @param str The string
	 * @param toEscape Any characters that require escaping
	 * @since jEdit 4.3pre15
	 */
	public static String charsToEscapes(String str, String toEscape)
	{
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			if(toEscape.indexOf(c) != -1)
			{
				if(c == '\n')
					buf.append("\\n");
				else if(c == '\t')
					buf.append("\\t");
				else {
					buf.append('\\');
					buf.append(c);
				}
			}
			else
				buf.append(c);
		}
		return buf.toString();
	}


	/**
	 * Compares two strings.<p>
	 *
	 * Unlike <function>String.compareTo()</function>,
	 * this method correctly recognizes and handles embedded numbers.
	 * For example, it places "My file 2" before "My file 10".<p>
	 *
	 * @param str1 The first string
	 * @param str2 The second string
	 * @param ignoreCase If true, case will be ignored
	 * @return negative If str1 &lt; str2, 0 if both are the same,
	 * positive if str1 &gt; str2
	 * @since jEdit 4.3pre5
	 */
	public static int compareStrings(String str1, String str2, boolean ignoreCase)
	{
		char[] char1 = str1.toCharArray();
		char[] char2 = str2.toCharArray();

		int len = Math.min(char1.length,char2.length);

		for(int i = 0, j = 0; i < len && j < len; i++, j++)
		{
			char ch1 = char1[i];
			char ch2 = char2[j];
			if(Character.isDigit(ch1) && Character.isDigit(ch2)
				&& ch1 != '0' && ch2 != '0')
			{
				int _i = i + 1;
				int _j = j + 1;

				for(; _i < char1.length; _i++)
				{
					if(!Character.isDigit(char1[_i]))
					{
						//_i--;
						break;
					}
				}

				for(; _j < char2.length; _j++)
				{
					if(!Character.isDigit(char2[_j]))
					{
						//_j--;
						break;
					}
				}

				int len1 = _i - i;
				int len2 = _j - j;
				if(len1 > len2)
					return 1;
				else if(len1 < len2)
					return -1;
				else {
					for(int k = 0; k < len1; k++)
					{
						ch1 = char1[i + k];
						ch2 = char2[j + k];
						if(ch1 != ch2)
							return ch1 - ch2;
					}
				}

				i = _i - 1;
				j = _j - 1;
			}
			else {
				if(ignoreCase)
				{
					ch1 = Character.toLowerCase(ch1);
					ch2 = Character.toLowerCase(ch2);
				}

				if(ch1 != ch2)
					return ch1 - ch2;
			}
		}

		return char1.length - char2.length;
	}


	/**
	 * Adds "file:" infront. Replaces '\' with '/'.
	 *
	 * @modified 2010.05.12
	 * @since 2010.05.12 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public static String createUrlString(String sPath){
		sPath= sPath.replace('\\', '/');
		if (sPath.startsWith("http")) {
			return sPath;
		}
		else if (!sPath.startsWith("file:")) {
			sPath= "file:"+sPath;
		}
		return sPath;
	}


	/**
	 * INPUT: an SFI: h0.1.4p4<br/>
	 * OUTPUT: the level (= number of periods) of heading elements.
	 *
	 * @modified 2010.08.15
	 * @since 2010.08.15 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public static int findHeadingLevel(String sSFI) {
		int niC= 0;
		if (sSFI.indexOf("p")!=-1)
			sSFI= sSFI.substring(0, sSFI.indexOf("p"));
		for (int i=0; i<sSFI.length(); i++) {
			if (sSFI.charAt(i) == 46) {//.=46
				niC++;
			}
		}
		return niC;
	}


	/**
	 * INPUT: file:g:/file1/htmlmgr/doc/index.html#ifiSFI <br/>
	 * OUTPUT: ifiSFI or empty-string if no FragmentIdentifier.
	 *
	 * @modified 2010.08.18
	 * @since 2010.08.18 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public static String getFIfromUrlString(String sUrl)
	{
		if (sUrl.indexOf("#")!=-1)
			return sUrl.substring(sUrl.indexOf("#")+1);
		else
			return "";
	}


	/**
	 * @param str A java string
		 * @return the leading whitespace of that string, for indenting subsequent lines.
	 * @since jEdit 4.3pre10
	 */
	public static String getIndentString(String str)
	{
		StringBuilder indentString = new StringBuilder();
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			if (! Character.isWhitespace(ch))
				break;
			indentString.append(ch);
		}
		return indentString.toString();

	}


	/**
	 * Returns the virtual column number (taking tabs into account) of the
	 * specified offset in the segment.
	 *
	 * @param seg The segment
	 * @param tabSize The tab size
	 */
	public static int getVirtualWidth(Segment seg, int tabSize)
	{
		int virtualPosition = 0;

		for (int i = 0; i < seg.count; i++)
		{
			char ch = seg.array[seg.offset + i];

			if (ch == '\t')
			{
				virtualPosition += tabSize
					- virtualPosition % tabSize;
			}
			else {
				++virtualPosition;
			}
		}

		return virtualPosition;
	}


	/**
	 * Returns the array offset of a virtual column number (taking tabs
	 * into account) in the segment.
	 *
	 * @param seg The segment
	 * @param tabSize The tab size
	 * @param column The virtual column number
	 * @param totalVirtualWidth If this array is non-null, the total
	 * virtual width will be stored in its first location if this method
	 * returns -1.
	 *
	 * @return -1 if the column is out of bounds
	 */
	public static int getOffsetOfVirtualColumn(Segment seg, int tabSize,
					int column, int[] totalVirtualWidth)
	{
		int virtualPosition = 0;

		for (int i = 0; i < seg.count; i++)
		{
			char ch = seg.array[seg.offset + i];

			if (ch == '\t')
			{
				int tabWidth = tabSize
					- virtualPosition % tabSize;
				if(virtualPosition >= column)
					return i;
				else
					virtualPosition += tabWidth;
			}
			else {
				if(virtualPosition >= column)
					return i;
				else
					++virtualPosition;
			}
		}

		if(totalVirtualWidth != null)
			totalVirtualWidth[0] = virtualPosition;
		return -1;
	}


	/**
	 * INPUT: &lt;elem&gt;text&lt;/elem&gt;<br/>
	 * OUTPUT: text.
	 * @modified 2010.08.30
	 * @since 2010.08.30 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public static String getTextContentOfElement(String sElem) {
		return sElem.substring(sElem.indexOf(">")+1,
													sElem.lastIndexOf("<"));
	}


	/**
	 * INPUT: an SFI: h0.3.7.2.4 and a level-number eg 2.<br/>
	 * OUTUT: h0.3.7 (level-2 of SFI) of empty-string if something wrong.
	 *
	 * @modified 2010.08.31
	 * @since 2010.08.31 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public static String getSfiPartLevel(String sSFI, int niL) {
		String sOut= "";
		if (sSFI.indexOf("p")!=-1)
			sSFI= sSFI.substring(0, sSFI.indexOf("p"));

		int niLevelSFI= findHeadingLevel(sSFI);
		int ni1,ni2,ni3,ni4,ni5,ni6,ni7,ni8;
		ni1=ni2=ni3=ni4=ni5=ni6=ni7=ni8= -1;

		if (niL > niLevelSFI)
			return "";

		else if (niL== 1){
			if (1 > niLevelSFI)
				return "";
			else if (1 == niLevelSFI)
				return sSFI;
			else {
				//h0.1.
				ni1= sSFI.indexOf(".")+1;
				return sSFI.substring(0, sSFI.indexOf(".", ni1));
			}
		}

		else if (niL== 2){
			if (2 > niLevelSFI)
				return "";
			else if (2 == niLevelSFI)
				return sSFI;
			else {
				//h0.1.2.
				ni1= sSFI.indexOf(".")+1;
				ni2= sSFI.indexOf(".",ni1)+1;
				return sSFI.substring(0, sSFI.indexOf(".", ni2));
			}
		}

		else if (niL== 3){
			if (3 > niLevelSFI)
				return "";
			else if (3 == niLevelSFI)
				return sSFI;
			else {
				//h0.1.2.3.
				ni1= sSFI.indexOf(".")+1;
				ni2= sSFI.indexOf(".",ni1)+1;
				ni3= sSFI.indexOf(".",ni2)+1;
				return sSFI.substring(0, sSFI.indexOf(".", ni3));
			}
		}

		else if (niL== 4){
			if (4 > niLevelSFI)
				return "";
			else if (4 == niLevelSFI)
				return sSFI;
			else {
				//h0.1.2.3.4.
				ni1= sSFI.indexOf(".")+1;
				ni2= sSFI.indexOf(".",ni1)+1;
				ni3= sSFI.indexOf(".",ni2)+1;
				ni4= sSFI.indexOf(".",ni3)+1;
				return sSFI.substring(0, sSFI.indexOf(".", ni4));
			}
		}

		else if (niL== 5){
			if (5 > niLevelSFI)
				return "";
			else if (5 == niLevelSFI)
				return sSFI;
			else {
				//h0.1.2.3.4.5.
				ni1= sSFI.indexOf(".")+1;
				ni2= sSFI.indexOf(".",ni1)+1;
				ni3= sSFI.indexOf(".",ni2)+1;
				ni4= sSFI.indexOf(".",ni3)+1;
				ni5= sSFI.indexOf(".",ni4)+1;
				return sSFI.substring(0, sSFI.indexOf(".", ni5));
			}
		}

		else if (niL== 6){
			if (6 > niLevelSFI)
				return "";
			else if (6 == niLevelSFI)
				return sSFI;
			else {
				//h0.1.2.3.4.5.6.
				ni1= sSFI.indexOf(".")+1;
				ni2= sSFI.indexOf(".",ni1)+1;
				ni3= sSFI.indexOf(".",ni2)+1;
				ni4= sSFI.indexOf(".",ni3)+1;
				ni5= sSFI.indexOf(".",ni4)+1;
				ni6= sSFI.indexOf(".",ni5)+1;
				return sSFI.substring(0, sSFI.indexOf(".", ni6));
			}
		}

		else if (niL== 7){
			if (7 > niLevelSFI)
				return "";
			else if (7 == niLevelSFI)
				return sSFI;
			else {
				//h0.1.2.3.4.5.6.7.
				ni1= sSFI.indexOf(".")+1;
				ni2= sSFI.indexOf(".",ni1)+1;
				ni3= sSFI.indexOf(".",ni2)+1;
				ni4= sSFI.indexOf(".",ni3)+1;
				ni5= sSFI.indexOf(".",ni4)+1;
				ni6= sSFI.indexOf(".",ni5)+1;
				ni7= sSFI.indexOf(".",ni6)+1;
				return sSFI.substring(0, sSFI.indexOf(".", ni7));
			}
		}

		else
			return "";
	}


	/**
	 * Converts a Unix-style glob to a regular expression.<p>
	 *
	 * ? becomes ., * becomes .*, {aa,bb} becomes (aa|bb).
	 * @param glob The glob pattern
	 * @since jEdit 4.3pre7
	 */
	public static String globToRE(String glob)
	{
		if (glob.startsWith("(re)"))
		{
			return glob.substring(4);
		}

		final Object NEG = new Object();
		final Object GROUP = new Object();
		Stack<Object> state = new Stack<Object>();

		StringBuilder buf = new StringBuilder();
		boolean backslash = false;

		for(int i = 0; i < glob.length(); i++)
		{
			char c = glob.charAt(i);
			if(backslash)
			{
				buf.append('\\');
				buf.append(c);
				backslash = false;
				continue;
			}

			switch(c)
			{
			case '\\':
				backslash = true;
				break;
			case '?':
				buf.append('.');
				break;
			case '.':
			case '+':
			case '(':
			case ')':
				buf.append('\\');
				buf.append(c);
				break;
			case '*':
				buf.append(".*");
				break;
			case '|':
				if(backslash)
					buf.append("\\|");
				else
					buf.append('|');
				break;
			case '{':
				buf.append('(');
				if(i + 1 != glob.length() && glob.charAt(i + 1) == '!')
				{
					buf.append('?');
					state.push(NEG);
				}
				else
					state.push(GROUP);
				break;
			case ',':
				if(!state.isEmpty() && state.peek() == GROUP)
					buf.append('|');
				else
					buf.append(',');
				break;
			case '}':
				if(!state.isEmpty())
				{
					buf.append(')');
					if(state.pop() == NEG)
						buf.append(".*");
				}
				else
					buf.append('}');
				break;
			default:
				buf.append(c);
			}
		}

		return buf.toString();
	}


	/**
	 * Returns a boolean from a given object.
	 * @param obj the object
	 * @param def The default value
	 * @return the boolean value if obj is a Boolean,
	 * true if the value is "true", "yes", "on",
	 * false if the value is "false", "no", "off"
	 * def if the value is null or anything else
	 * @since jEdit 4.3pre17
	 */
	public static boolean getBoolean(Object obj, boolean def)
	{
		if(obj == null)
			return def;
		else if(obj instanceof Boolean)
			return ((Boolean)obj).booleanValue();
		else if("true".equals(obj) || "yes".equals(obj)
			|| "on".equals(obj))
			return true;
		else if("false".equals(obj) || "no".equals(obj)
			|| "off".equals(obj))
			return false;

		return def;
	}


	/**
	 * Test if an html-file contains Structure-Fragment-Identifiers (SFI).<br/>
	 * GIVEN: the string-url of the file.
	 *
	 * @modified 2010.07.02
	 * @since 2010.07.02 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public static boolean isSFIFile(String sUrl)
	{
		//reads the head-elements
		//and test if AAj.css found
		boolean bSFI= false;
		sUrl= createUrlString(sUrl);
		try {
			URL url = new URL(sUrl);//file:...
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			String sLn= "";
			while ((sLn=br.readLine()) !=null){
				//if contains the document's SFI
				if (sLn.indexOf("<a name=\"h0\"></a>")!=-1) {
					//wrka ppp
					bSFI= true;
					break;
				}
			}
			br.close();
		} catch (Exception e){
			System.out.println("HtmlUtilities.isSFIFile: "+sUrl+e.toString());
		}
		return bSFI;
	}


	/**
	 * Check if a string of html is SFI-html.
	 *
	 * @modified 2010.08.16
	 * @since 2010.08.16 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public static boolean isSFIHtml(String sHtml)
	{
		if (sHtml.indexOf("<a name=\"h0\"></a>")!=-1)
			return true;
		else
			return false;
	}


	/**
	 * Displays a dialog box.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property. The message
	 * is formatted by the property manager with <code>args</code> as
	 * positional parameters.
	 * @param comp The component to display the dialog for
	 * @param name The name of the dialog
	 * @param args Positional parameters to be substituted into the
	 * message text
	 */
	public static void message(Component comp, String name, Object[] args)
	{
//		hideSplashScreen();

		JOptionPane.showMessageDialog(comp,
			HtmlMgr.getProperty(name.concat(".message"),args),
			HtmlMgr.getProperty(name.concat(".title"),args),
			JOptionPane.INFORMATION_MESSAGE);
	}


	/**
	 * Returns if two strings are equal. This correctly handles null pointers,
	 * as opposed to calling <code>o1.equals(o2)</code>.
	 * @since jEdit 4.3pre6
	 */
	public static boolean objectsEqual(Object o1, Object o2)
	{
		if(o1 == null)
		{
			if(o2 == null)
				return true;
			else
				return false;
		}
		else if(o2 == null)
			return false;
		else
			return o1.equals(o2);
	}


	/**
	 *
	 * @modified 2010.05.20
	 * @since 2010.05.20 (v00.02.02)
	 * @author HoKoNoUmo, Slava Pestov
	 */
	public static boolean parseXML(InputStream in, DefaultHandler handler) throws IOException
	{
		try
		{
			XMLReader parser = XMLReaderFactory.createXMLReader();
			InputSource isrc = new InputSource(new BufferedInputStream(in));
//			isrc.setSystemId("jedit.jar");
			parser.setContentHandler(handler);
			parser.setDTDHandler(handler);
			parser.setEntityResolver(handler);
			parser.setErrorHandler(handler);
			parser.parse(isrc);
		}
		catch(SAXParseException se)
		{
			int line = se.getLineNumber();
			System.out.println("Utilites.parseXml from " + in
			+": SAXParseException: line " + line + ": "+se.toString());
			return true;
		}
		catch(SAXException e)
		{
			System.out.println(e.toString());;
			return true;
		}
		finally
		{
			try
			{
				if(in != null)
					in.close();
			}
			catch(IOException io)
			{
				System.out.println(io.toString());;
			}
		}
		return false;
	}


	/**
	 *
	 * @modified 2010.07.09
	 * @since 2010.07.09 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public static String replaceCharWithReference(String sIn)
	{
		String sOut;
		sOut= sIn.replace("\\u0022","&quot;"); //(34)		quotation mark HTML 2.0
		sOut= sIn.replace("\u0026","&amp;"); // 0026(38)	ampersand HTML 2.0
		sOut= sIn.replace("\u003C","&lt;"); // (60)		less-than sign HTML 2.0
		sOut= sIn.replace("\u003E","&gt;"); //62)		greater-than sign HTML 2.0
		sOut= sIn.replace("\u00A0","&nbsp;"); // (160)	non-breaking space HTML 3.2
		sOut= sIn.replace("\u00A1","&iexcl;"); //(161)	inverted exclamation mark HTML 3.2
		sOut= sIn.replace("\u00A2","&cent;"); //(162)		cent sign HTML 3.2
		sOut= sIn.replace("\u00A3","&pound;"); //(163)	pound sign HTML 3.2
		sOut= sIn.replace("\u00A4","&curren;"); //(164)		currency sign HTML 3.2
		sOut= sIn.replace("\u00A5","&yen;"); //(165)	yen sign HTML 3.2
		sOut= sIn.replace("\u00A6","&brvbar;"); //(166)		broken bar HTML 3.2
		sOut= sIn.replace("\u00A7","&sect;"); //(167)		section sign HTML 3.2
		sOut= sIn.replace("\u00A8","&uml;"); //(168)	diaeresis HTML 3.2
		sOut= sIn.replace("\u00A9","&copy;"); //(169)		copyright sign HTML 3.2
		sOut= sIn.replace("\u00AA","&ordf;"); //(170)		feminine ordinal indicator HTML 3.2
		sOut= sIn.replace("\u00AB","&laquo;"); //(171)	left-pointing double angle quotation mark HTML 3.2
		sOut= sIn.replace("\u00AC","&not;"); //(172)	not sign HTML 3.2
		sOut= sIn.replace("\u00AD","&shy;"); //(173)	soft hyphen HTML 3.2
		sOut= sIn.replace("\u00AE","&reg;"); //(174)	registered sign HTML 3.2
		sOut= sIn.replace("\u00AF","&macr;"); //(175)		macron HTML 3.2
		sOut= sIn.replace("\u00B0","&deg;"); //(176)	degree sign HTML 3.2
		sOut= sIn.replace("\u00B1","&plusmn;"); //(177)		plus-minus sign HTML 3.2
		sOut= sIn.replace("\u00B2","&sup2;"); //(178)		superscript two HTML 3.2
		sOut= sIn.replace("\u00B3","&sup3;"); //(179)		superscript three HTML 3.2
		sOut= sIn.replace("\u00B4","&acute;"); //(180)	acute accent HTML 3.2
		sOut= sIn.replace("\u00B5","&micro;"); //(181)	micro sign HTML 3.2
		sOut= sIn.replace("\u00B6","&para;"); //(182)		pilcrow sign HTML 3.2
		sOut= sIn.replace("\u00B7","&middot;"); //(183)		middle dot HTML 3.2
		sOut= sIn.replace("\u00B8","&cedil;"); //(184)	cedilla HTML 3.2
		sOut= sIn.replace("\u00B9","&sup1;"); //(185)		superscript one HTML 3.2
		sOut= sIn.replace("\u00BA","&ordm;"); //(186)		masculine ordinal indicator HTML 3.2
		sOut= sIn.replace("\u00BB","&raquo;"); //(187)	right-pointing double angle quotation mark HTML 3.2
		sOut= sIn.replace("\u00BC","&frac14;"); //(188)		vulgar fraction one quarter HTML 3.2
		sOut= sIn.replace("\u00BD","&frac12;"); //(189)		vulgar fraction one half HTML 3.2
		sOut= sIn.replace("\u00BE","&frac34;"); //(190)		vulgar fraction three quarters HTML 3.2
		sOut= sIn.replace("\u00BF","&iquest;"); //(191)		inverted question mark HTML 3.2
		sOut= sIn.replace("\u00C0","&Agrave;"); //(192)		Latin capital letter a with grave HTML 2.0
		sOut= sIn.replace("\u00C1","&Aacute;"); //(193)		Latin capital letter a with acute HTML 2.0
		sOut= sIn.replace("\u00C2","&Acirc;"); //(194)	Latin capital letter a with circumflex HTML 2.0
		sOut= sIn.replace("\u00C3","&Atilde;"); //(195)		Latin capital letter a with tilde HTML 2.0
		sOut= sIn.replace("\u00C4","&Auml;"); //(196)		Latin capital letter a with diaeresis HTML 2.0
		sOut= sIn.replace("\u00C5","&Aring;"); //(197)	Latin capital letter a with ring above HTML 2.0
		sOut= sIn.replace("\u00C6","&AElig;"); //(198)	Latin capital letter ae HTML 2.0
		sOut= sIn.replace("\u00C7","&Ccedil;"); //(199)		Latin capital letter c with cedilla HTML 2.0
		sOut= sIn.replace("\u00C8","&Egrave;"); //(200)		Latin capital letter e with grave HTML 2.0
		sOut= sIn.replace("\u00C9","&Eacute;"); //(201)		Latin capital letter e with acute HTML 2.0
		sOut= sIn.replace("\u00CA","&Ecirc;"); //(202)	Latin capital letter e with circumflex HTML 2.0
		sOut= sIn.replace("\u00CB","&Euml;"); //(203)		Latin capital letter e with diaeresis HTML 2.0
		sOut= sIn.replace("\u00CC","&Igrave;"); //(204)		Latin capital letter i with grave HTML 2.0
		sOut= sIn.replace("\u00CD","&Iacute;"); //(205)		Latin capital letter i with acute HTML 2.0
		sOut= sIn.replace("\u00CE","&Icirc;"); //(206)	Latin capital letter i with circumflex HTML 2.0
		sOut= sIn.replace("\u00CF","&Iuml;"); //(207)		Latin capital letter i with diaeresis HTML 2.0
		sOut= sIn.replace("\u00D0","&ETH;"); //(208)	Latin capital letter eth HTML 2.0
		sOut= sIn.replace("\u00D1","&Ntilde;"); //(209)		Latin capital letter n with tilde HTML 2.0
		sOut= sIn.replace("\u00D2","&Ograve;"); //(210)		Latin capital letter o with grave HTML 2.0
		sOut= sIn.replace("\u00D3","&Oacute;"); //(211)		Latin capital letter o with acute HTML 2.0
		sOut= sIn.replace("\u00D4","&Ocirc;"); //(212)	Latin capital letter o with circumflex HTML 2.0
		sOut= sIn.replace("\u00D5","&Otilde;"); //(213)		Latin capital letter o with tilde HTML 2.0
		sOut= sIn.replace("\u00D6","&Ouml;"); //(214)		Latin capital letter o with diaeresis HTML 2.0
		sOut= sIn.replace("\u00D7","&times;"); //(215)	multiplication sign HTML 3.2
		sOut= sIn.replace("\u00D8","&Oslash;"); //(216)		Latin capital letter o with stroke HTML 2.0
		sOut= sIn.replace("\u00D9","&Ugrave;"); //(217)		Latin capital letter u with grave HTML 2.0
		sOut= sIn.replace("\u00DA","&Uacute;"); //(218)		Latin capital letter u with acute HTML 2.0
		sOut= sIn.replace("\u00DB","&Ucirc;"); //(219)	Latin capital letter u with circumflex HTML 2.0
		sOut= sIn.replace("\u00DC","&Uuml;"); //(220)		Latin capital letter u with diaeresis HTML 2.0
		sOut= sIn.replace("\u00DD","&Yacute;"); //(221)		Latin capital letter y with acute HTML 2.0
		sOut= sIn.replace("\u00DE","&THORN;"); //(222)	Latin capital letter thorn HTML 2.0
		sOut= sIn.replace("\u00DF","&szlig;"); //(223)	Latin small letter sharp s (German Eszett) HTML 2.0
		sOut= sIn.replace("\u00E0","&agrave;"); //(224)		Latin small letter a with grave HTML 2.0
		sOut= sIn.replace("\u00E1","&aacute;"); //(225)		Latin small letter a with acute HTML 2.0
		sOut= sIn.replace("\u00E2","&acirc;"); //(226)	Latin small letter a with circumflex HTML 2.0
		sOut= sIn.replace("\u00E3","&atilde;"); //(227)		Latin small letter a with tilde HTML 2.0
		sOut= sIn.replace("\u00E4","&auml;"); //(228)		Latin small letter a with diaeresis HTML 2.0
		sOut= sIn.replace("\u00E5","&aring;"); //(229)	Latin small letter a with ring above HTML 2.0
		sOut= sIn.replace("\u00E6","&aelig;"); //(230)	Latin lowercase ligature ae HTML 2.0
		sOut= sIn.replace("\u00E7","&ccedil;"); //(231)		Latin small letter c with cedilla HTML 2.0
		sOut= sIn.replace("\u00E8","&egrave;"); //(232)		Latin small letter e with grave HTML 2.0
		sOut= sIn.replace("\u00E9","&eacute;"); //(233)		Latin small letter e with acute HTML 2.0
		sOut= sIn.replace("\u00EA","&ecirc;"); //(234)	Latin small letter e with circumflex HTML 2.0
		sOut= sIn.replace("\u00EB","&euml;"); //(235)		Latin small letter e with diaeresis HTML 2.0
		sOut= sIn.replace("\u00EC","&igrave;"); //(236)		Latin small letter i with grave HTML 2.0
		sOut= sIn.replace("\u00ED","&iacute;"); //(237)		Latin small letter i with acute HTML 2.0
		sOut= sIn.replace("\u00EE","&icirc;"); //(238)	Latin small letter i with circumflex HTML 2.0
		sOut= sIn.replace("\u00EF","&iuml;"); //(239)		Latin small letter i with diaeresis HTML 2.0
		sOut= sIn.replace("\u00F0","&eth;"); //(240)	Latin small letter eth HTML 2.0
		sOut= sIn.replace("\u00F1","&ntilde;"); //(241)		Latin small letter n with tilde HTML 2.0
		sOut= sIn.replace("\u00F2","&ograve;"); //(242)		Latin small letter o with grave HTML 2.0
		sOut= sIn.replace("\u00F3","&oacute;"); //(243)		Latin small letter o with acute HTML 2.0
		sOut= sIn.replace("\u00F4","&ocirc;"); //(244)	Latin small letter o with circumflex HTML 2.0
		sOut= sIn.replace("\u00F5","&otilde;"); //(245)		Latin small letter o with tilde HTML 2.0
		sOut= sIn.replace("\u00F6","&ouml;"); //(246)		Latin small letter o with diaeresis HTML 2.0
		sOut= sIn.replace("\u00F7","&divide;"); //(247)		division sign HTML 3.2
		sOut= sIn.replace("\u00F8","&oslash;"); //(248)		Latin small letter o with stroke HTML 2.0
		sOut= sIn.replace("\u00F9","&ugrave;"); //(249)		Latin small letter u with grave HTML 2.0
		sOut= sIn.replace("\u00FA","&uacute;"); //(250)		Latin small letter u with acute HTML 2.0
		sOut= sIn.replace("\u00FB","&ucirc;"); //(251)	Latin small letter u with circumflex HTML 2.0
		sOut= sIn.replace("\u00FC","&uuml;"); //(252)		Latin small letter u with diaeresis HTML 2.0
		sOut= sIn.replace("\u00FD","&yacute;"); //(253)		Latin small letter y with acute HTML 2.0
		sOut= sIn.replace("\u00FE","&thorn;"); //(254)	Latin small letter thorn HTML 2.0
		sOut= sIn.replace("\u00FF","&yuml;"); //(255)		Latin small letter y with diaeresis HTML 2.0
		sOut= sIn.replace("\u0152","&OElig;"); //(338)	Latin capital ligature oe HTML 4.0
		sOut= sIn.replace("\u0153","&oelig;"); //(339)	Latin small ligature oe HTML 4.0
		sOut= sIn.replace("\u0160","&Scaron;"); //(352)		Latin capital letter s with caron HTML 4.0
		sOut= sIn.replace("\u0161","&scaron;"); //(353)		Latin small letter s with caron HTML 4.0
		sOut= sIn.replace("\u0178","&Yuml;"); //(376)		Latin capital letter y with diaeresis HTML 4.0
		sOut= sIn.replace("\u0192","&fnof;"); //(402)		Latin small letter f with hook HTML 4.0
		sOut= sIn.replace("\u02C6","&circ;"); //(710)		modifier letter circumflex accent HTML 4.0
		sOut= sIn.replace("\u02DC","&tilde;"); //(732)	small tilde HTML 4.0
		sOut= sIn.replace("\u0391","&Alpha;"); //(913)	Greek capital letter alpha HTML 4.0
		sOut= sIn.replace("\u0392","&Beta;"); //(914)		Greek capital letter beta HTML 4.0
		sOut= sIn.replace("\u0393","&Gamma;"); //(915)	Greek capital letter gamma HTML 4.0
		sOut= sIn.replace("\u0394","&Delta;"); //(916)	Greek capital letter delta HTML 4.0
		sOut= sIn.replace("\u0395","&Epsilon;"); //(917)	Greek capital letter epsilon HTML 4.0
		sOut= sIn.replace("\u0396","&Zeta;"); //(918)		Greek capital letter zeta HTML 4.0
		sOut= sIn.replace("\u0397","&Eta;"); //(919)	Greek capital letter eta HTML 4.0
		sOut= sIn.replace("\u0398","&Theta;"); //(920)	Greek capital letter theta HTML 4.0
		sOut= sIn.replace("\u0399","&Iota;"); //(921)		Greek capital letter iota HTML 4.0
		sOut= sIn.replace("\u039A","&Kappa;"); //(922)	Greek capital letter kappa HTML 4.0
		sOut= sIn.replace("\u039B","&Lambda;"); //(923)		Greek capital letter lambda HTML 4.0
		sOut= sIn.replace("\u039C","&Mu;"); //(924)		Greek capital letter mu HTML 4.0
		sOut= sIn.replace("\u039D","&Nu;"); //(925)		Greek capital letter nu HTML 4.0
		sOut= sIn.replace("\u039E","&Xi;"); //(926)		Greek capital letter xi HTML 4.0
		sOut= sIn.replace("\u039F","&Omicron;"); //(927)	Greek capital letter omicron HTML 4.0
		sOut= sIn.replace("\u03A0","&Pi;"); //(928)		Greek capital letter pi HTML 4.0
		sOut= sIn.replace("\u03A1","&Rho;"); //(929)	Greek capital letter rho HTML 4.0
		sOut= sIn.replace("\u03A3","&Sigma;"); //(931)	Greek capital letter sigma HTML 4.0
		sOut= sIn.replace("\u03A4","&Tau;"); //(932)	Greek capital letter tau HTML 4.0
		sOut= sIn.replace("\u03A5","&Upsilon;"); //(933)	Greek capital letter upsilon HTML 4.0
		sOut= sIn.replace("\u03A6","&Phi;"); //(934)	Greek capital letter phi HTML 4.0
		sOut= sIn.replace("\u03A7","&Chi;"); //(935)	Greek capital letter chi HTML 4.0
		sOut= sIn.replace("\u03A8","&Psi;"); //(936)	Greek capital letter psi HTML 4.0
		sOut= sIn.replace("\u03A9","&Omega;"); //(937)	Greek capital letter omega HTML 4.0
		sOut= sIn.replace("\u03B1","&alpha;"); //(945)	Greek small letter alpha HTML 4.0
		sOut= sIn.replace("\u03B2","&beta;"); //(946)		Greek small letter beta HTML 4.0
		sOut= sIn.replace("\u03B3","&gamma;"); //(947)	Greek small letter gamma HTML 4.0
		sOut= sIn.replace("\u03B4","&delta;"); //(948)	Greek small letter delta HTML 4.0
		sOut= sIn.replace("\u03B5","&epsilon;"); //(949)	Greek small letter epsilon HTML 4.0
		sOut= sIn.replace("\u03B6","&zeta;"); //(950)		Greek small letter zeta HTML 4.0
		sOut= sIn.replace("\u03B7","&eta;"); //(951)	Greek small letter eta HTML 4.0
		sOut= sIn.replace("\u03B8","&theta;"); //(952)	Greek small letter theta HTML 4.0
		sOut= sIn.replace("\u03B9","&iota;"); //(953)		Greek small letter iota HTML 4.0
		sOut= sIn.replace("\u03BA","&kappa;"); //(954)	Greek small letter kappa HTML 4.0
		sOut= sIn.replace("\u03BB","&lambda;"); //(955)		Greek small letter lambda HTML 4.0
		sOut= sIn.replace("\u03BC","&mu;"); //(956)		Greek small letter mu HTML 4.0
		sOut= sIn.replace("\u03BD","&nu;"); //(957)		Greek small letter nu HTML 4.0
		sOut= sIn.replace("\u03BE","&xi;"); //(958)		Greek small letter xi HTML 4.0
		sOut= sIn.replace("\u03BF","&omicron;"); //(959)	Greek small letter omicron HTML 4.0
		sOut= sIn.replace("\u03C0","&pi;"); //(960)		Greek small letter pi HTML 4.0
		sOut= sIn.replace("\u03C1","&rho;"); //(961)	Greek small letter rho HTML 4.0
		sOut= sIn.replace("\u03C2","&sigmaf;"); //(962)		Greek small letter final sigma HTML 4.0
		sOut= sIn.replace("\u03C3","&sigma;"); //(963)	Greek small letter sigma HTML 4.0
		sOut= sIn.replace("\u03C4","&tau;"); //(964)	Greek small letter tau HTML 4.0
		sOut= sIn.replace("\u03C5","&upsilon;"); //(965)	Greek small letter upsilon HTML 4.0
		sOut= sIn.replace("\u03C6","&phi;"); //(966)	Greek small letter phi HTML 4.0
		sOut= sIn.replace("\u03C7","&chi;"); //(967)	Greek small letter chi HTML 4.0
		sOut= sIn.replace("\u03C8","&psi;"); //(968)	Greek small letter psi HTML 4.0
		sOut= sIn.replace("\u03C9","&omega;"); //(969)	Greek small letter omega HTML 4.0
		sOut= sIn.replace("\u03D1","&thetasym;"); //(977)		Greek theta symbol HTML 4.0
		sOut= sIn.replace("\u03D2","&upsih;"); //(978)	Greek upsilon with hook symbol HTML 4.0
		sOut= sIn.replace("\u03D6","&piv;"); //(982)	Greek pi symbol HTML 4.0
		sOut= sIn.replace("\u2002","&ensp;"); //(8194)	en space [1] HTML 4.0
		sOut= sIn.replace("\u2003","&emsp;"); //(8195)	em space [2] HTML 4.0
		sOut= sIn.replace("\u2009","&thinsp;"); //(8201)	thin space [3] HTML 4.0
		sOut= sIn.replace("\u200C","&zwnj;"); //(8204)	zero width non-joiner HTML 4.0
		sOut= sIn.replace("\u200D","&zwj;"); //(8205)		zero width joiner HTML 4.0
		sOut= sIn.replace("\u200E","&lrm;"); //(8206)		left-to-right mark HTML 4.0
		sOut= sIn.replace("\u200F","&rlm;"); //(8207)		right-to-left mark HTML 4.0
		sOut= sIn.replace("\u2013","&ndash;"); //(8211)		en dash HTML 4.0
		sOut= sIn.replace("\u2014","&mdash;"); //(8212)		em dash HTML 4.0
		sOut= sIn.replace("\u2018","&lsquo;"); //(8216)		left single quotation mark HTML 4.0
		sOut= sIn.replace("\u2019","&rsquo;"); //(8217)		right single quotation mark HTML 4.0
		sOut= sIn.replace("\u201A","&sbquo;"); //(8218)		single low-9 quotation mark HTML 4.0
		sOut= sIn.replace("\u201C","&ldquo;"); //(8220)		left double quotation mark HTML 4.0
		sOut= sIn.replace("\u201D","&rdquo;"); //(8221)		right double quotation mark HTML 4.0
		sOut= sIn.replace("\u201E","&bdquo;"); //(8222)		double low-9 quotation mark HTML 4.0
		sOut= sIn.replace("\u2020","&dagger;"); //(8224)	dagger HTML 4.0
		sOut= sIn.replace("\u2021","&Dagger;"); //(8225)	double dagger HTML 4.0
		sOut= sIn.replace("\u2022","&bull;"); //(8226)	bullet HTML 4.0
		sOut= sIn.replace("\u2026","&hellip;"); //(8230)	horizontal ellipsis HTML 4.0
		sOut= sIn.replace("\u2030","&permil;"); //(8240)	per mille sign HTML 4.0
		sOut= sIn.replace("\u2032","&prime;"); //(8242)		prime HTML 4.0
		sOut= sIn.replace("\u2033","&Prime;"); //(8243)		double prime HTML 4.0
		sOut= sIn.replace("\u2039","&lsaquo;"); //(8249)	single left-pointing angle quotation mark HTML 4.0
		sOut= sIn.replace("\u203A","&rsaquo;"); //(8250)	single right-pointing angle quotation mark HTML 4.0
		sOut= sIn.replace("\u203E","&oline;"); //(8254)		overline HTML 4.0
		sOut= sIn.replace("\u2044","&frasl;"); //(8260)		fraction slash HTML 4.0
		sOut= sIn.replace("\u20AC","&euro;"); //(8364)	euro sign HTML 4.0
		sOut= sIn.replace("\u2111","&image;"); //(8465)		black-letter capital i HTML 4.0
		sOut= sIn.replace("\u2118","&weierp;"); //(8472)	script capital p (Weierstrass p) HTML 4.0
		sOut= sIn.replace("\u211C","&real;"); //(8476)	black-letter capital r HTML 4.0
		sOut= sIn.replace("\u2122","&trade;"); //(8482)		trademark sign HTML 4.0
		sOut= sIn.replace("\u2135","&alefsym;"); //(8501)		alef symbol HTML 4.0
		sOut= sIn.replace("\u2190","&larr;"); //(8592)	leftwards arrow HTML 4.0
		sOut= sIn.replace("\u2191","&uarr;"); //(8593)	upwards arrow HTML 4.0
		sOut= sIn.replace("\u2192","&rarr;"); //(8594)	rightwards arrow HTML 4.0
		sOut= sIn.replace("\u2193","&darr;"); //(8595)	downwards arrow HTML 4.0
		sOut= sIn.replace("\u2194","&harr;"); //(8596)	left right arrow HTML 4.0
		sOut= sIn.replace("\u21B5","&crarr;"); //(8629)		downwards arrow with corner leftwards HTML 4.0
		sOut= sIn.replace("\u21D0","&lArr;"); //(8656)	leftwards double arrow HTML 4.0
		sOut= sIn.replace("\u21D1","&uArr;"); //(8657)	upwards double arrow HTML 4.0
		sOut= sIn.replace("\u21D2","&rArr;"); //(8658)	rightwards double arrow HTML 4.0
		sOut= sIn.replace("\u21D3","&dArr;"); //(8659)	downwards double arrow HTML 4.0
		sOut= sIn.replace("\u21D4","&hArr;"); //(8660)	left right double arrow HTML 4.0
		sOut= sIn.replace("\u2200","&forall;"); //(8704)	for all HTML 4.0
		sOut= sIn.replace("\u2202","&part;"); //(8706)	partial differential HTML 4.0
		sOut= sIn.replace("\u2203","&exist;"); //(8707)		there exists HTML 4.0
		sOut= sIn.replace("\u2205","&empty;"); //(8709)		empty set HTML 4.0
		sOut= sIn.replace("\u2207","&nabla;"); //(8711)		nabla HTML 4.0
		sOut= sIn.replace("\u2208","&isin;"); //(8712)	element of HTML 4.0
		sOut= sIn.replace("\u2209","&notin;"); //(8713)		not an element of HTML 4.0
		sOut= sIn.replace("\u220B","&ni;"); //(8715)	contains as member HTML 4.0
		sOut= sIn.replace("\u220F","&prod;"); //(8719)	n-ary product HTML 4.0
		sOut= sIn.replace("\u2211","&sum;"); //(8721)		n-ary summation HTML 4.0
		sOut= sIn.replace("\u2212","&minus;"); //(8722)		minus sign HTML 4.0
		sOut= sIn.replace("\u2217","&lowast;"); //(8727)	asterisk operator HTML 4.0
		sOut= sIn.replace("\u221A","&radic;"); //(8730)		square root HTML 4.0
		sOut= sIn.replace("\u221D","&prop;"); //(8733)	proportional to HTML 4.0
		sOut= sIn.replace("\u221E","&infin;"); //(8734)		infinity HTML 4.0
		sOut= sIn.replace("\u2220","&ang;"); //(8736)		angle HTML 4.0
		sOut= sIn.replace("\u2227","&and;"); //(8743)		logical and HTML 4.0
		sOut= sIn.replace("\u2228","&or;"); //(8744)	logical or HTML 4.0
		sOut= sIn.replace("\u2229","&cap;"); //(8745)		intersection HTML 4.0
		sOut= sIn.replace("\u222A","&cup;"); //(8746)		union HTML 4.0
		sOut= sIn.replace("\u222B","&int;"); //(8747)		integral HTML 4.0
		sOut= sIn.replace("\u2234","&there4;"); //(8756)	therefore HTML 4.0
		sOut= sIn.replace("\u223C","&sim;"); //(8764)		tilde operator HTML 4.0
		sOut= sIn.replace("\u2245","&cong;"); //(8773)	congruent to HTML 4.0
		sOut= sIn.replace("\u2248","&asymp;"); //(8776)		almost equal to HTML 4.0
		sOut= sIn.replace("\u2260","&ne;"); //(8800)	not equal to HTML 4.0
		sOut= sIn.replace("\u2261","&equiv;"); //(8801)		identical to (equivalent to) HTML 4.0
		sOut= sIn.replace("\u2264","&le;"); //(8804)	less-than or equal to HTML 4.0
		sOut= sIn.replace("\u2265","&ge;"); //(8805)	greater-than or equal to HTML 4.0
		sOut= sIn.replace("\u2282","&sub;"); //(8834)		subset of HTML 4.0
		sOut= sIn.replace("\u2283","&sup;"); //(8835)		superset of HTML 4.0
		sOut= sIn.replace("\u2284","&nsub;"); //(8836)	not a subset of HTML 4.0
		sOut= sIn.replace("\u2286","&sube;"); //(8838)	subset of or equal to HTML 4.0
		sOut= sIn.replace("\u2287","&supe;"); //(8839)	superset of or equal to HTML 4.0
		sOut= sIn.replace("\u2295","&oplus;"); //(8853)		circled plus HTML 4.0
		sOut= sIn.replace("\u2297","&otimes;"); //(8855)	circled times HTML 4.0
		sOut= sIn.replace("\u22A5","&perp;"); //(8869)	up tack (perpendicular sign in math) HTML 4.0
		sOut= sIn.replace("\u22C5","&sdot;"); //(8901)	dot operator HTML 4.0
		sOut= sIn.replace("\u2308","&lceil;"); //(8968)		left ceiling HTML 4.0
		sOut= sIn.replace("\u2309","&rceil;"); //(8969)		right ceiling HTML 4.0
		sOut= sIn.replace("\u230A","&lfloor;"); //(8970)	left floor HTML 4.0
		sOut= sIn.replace("\u230B","&rfloor;"); //(8971)	right floor HTML 4.0
		sOut= sIn.replace("\u2329","&lang;"); //(9001)	left-pointing angle bracket HTML 4.0
		sOut= sIn.replace("\u232A","&rang;"); //(9002)	right-pointing angle bracket HTML 4.0
		sOut= sIn.replace("\u25CA","&loz;"); //(9674)		lozenge HTML 4.0
		sOut= sIn.replace("\u2660","&spades;"); //(9824)	black spade suit HTML 4.0
		sOut= sIn.replace("\u2663","&clubs;"); //(9827)		black club suit HTML 4.0
		sOut= sIn.replace("\u2665","&hearts;"); //(9829)	black heart suit HTML 4.0
		sOut= sIn.replace("\u2666","&diams;"); //
		return sOut;
	}

	/**
	 *
	 * @modified 2010.05.24
	 * @since 2010.05.24 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public static String replaceCharReferencies(String sIn)
	{
		String sOut;
		sOut= sIn.replace("&quot;","\\u0022"); //(34)		quotation mark HTML 2.0
		sOut= sIn.replace("&amp;","\u0026"); // (38)	ampersand HTML 2.0
		sOut= sIn.replace("&lt;","\u003C"); // (60)		less-than sign HTML 2.0
		sOut= sIn.replace("&gt;","\u003E"); //62)		greater-than sign HTML 2.0
		sOut= sIn.replace("&nbsp;","\u00A0"); // (160)	non-breaking space HTML 3.2
		sOut= sIn.replace("&iexcl;","\u00A1"); //(161)	inverted exclamation mark HTML 3.2
		sOut= sIn.replace("&cent;","\u00A2"); //(162)		cent sign HTML 3.2
		sOut= sIn.replace("&pound;","\u00A3"); //(163)	pound sign HTML 3.2
		sOut= sIn.replace("&curren;","\u00A4"); //(164)		currency sign HTML 3.2
		sOut= sIn.replace("&yen;","\u00A5"); //(165)	yen sign HTML 3.2
		sOut= sIn.replace("&brvbar;","\u00A6"); //(166)		broken bar HTML 3.2
		sOut= sIn.replace("&sect;","\u00A7"); //(167)		section sign HTML 3.2
		sOut= sIn.replace("&uml;","\u00A8"); //(168)	diaeresis HTML 3.2
		sOut= sIn.replace("&copy;","\u00A9"); //(169)		copyright sign HTML 3.2
		sOut= sIn.replace("&ordf;","\u00AA"); //(170)		feminine ordinal indicator HTML 3.2
		sOut= sIn.replace("&laquo;","\u00AB"); //(171)	left-pointing double angle quotation mark HTML 3.2
		sOut= sIn.replace("&not;","\u00AC"); //(172)	not sign HTML 3.2
		sOut= sIn.replace("&shy;","\u00AD"); //(173)	soft hyphen HTML 3.2
		sOut= sIn.replace("&reg;","\u00AE"); //(174)	registered sign HTML 3.2
		sOut= sIn.replace("&macr;","\u00AF"); //(175)		macron HTML 3.2
		sOut= sIn.replace("&deg;","\u00B0"); //(176)	degree sign HTML 3.2
		sOut= sIn.replace("&plusmn;","\u00B1"); //(177)		plus-minus sign HTML 3.2
		sOut= sIn.replace("&sup2;","\u00B2"); //(178)		superscript two HTML 3.2
		sOut= sIn.replace("&sup3;","\u00B3"); //(179)		superscript three HTML 3.2
		sOut= sIn.replace("&acute;","\u00B4"); //(180)	acute accent HTML 3.2
		sOut= sIn.replace("&micro;","\u00B5"); //(181)	micro sign HTML 3.2
		sOut= sIn.replace("&para;","\u00B6"); //(182)		pilcrow sign HTML 3.2
		sOut= sIn.replace("&middot;","\u00B7"); //(183)		middle dot HTML 3.2
		sOut= sIn.replace("&cedil;","\u00B8"); //(184)	cedilla HTML 3.2
		sOut= sIn.replace("&sup1;","\u00B9"); //(185)		superscript one HTML 3.2
		sOut= sIn.replace("&ordm;","\u00BA"); //(186)		masculine ordinal indicator HTML 3.2
		sOut= sIn.replace("&raquo;","\u00BB"); //(187)	right-pointing double angle quotation mark HTML 3.2
		sOut= sIn.replace("&frac14;","\u00BC"); //(188)		vulgar fraction one quarter HTML 3.2
		sOut= sIn.replace("&frac12;","\u00BD"); //(189)		vulgar fraction one half HTML 3.2
		sOut= sIn.replace("&frac34;","\u00BE"); //(190)		vulgar fraction three quarters HTML 3.2
		sOut= sIn.replace("&iquest;","\u00BF"); //(191)		inverted question mark HTML 3.2
		sOut= sIn.replace("&Agrave;","\u00C0"); //(192)		Latin capital letter a with grave HTML 2.0
		sOut= sIn.replace("&Aacute;","\u00C1"); //(193)		Latin capital letter a with acute HTML 2.0
		sOut= sIn.replace("&Acirc;","\u00C2"); //(194)	Latin capital letter a with circumflex HTML 2.0
		sOut= sIn.replace("&Atilde;","\u00C3"); //(195)		Latin capital letter a with tilde HTML 2.0
		sOut= sIn.replace("&Auml;","\u00C4"); //(196)		Latin capital letter a with diaeresis HTML 2.0
		sOut= sIn.replace("&Aring;","\u00C5"); //(197)	Latin capital letter a with ring above HTML 2.0
		sOut= sIn.replace("&AElig;","\u00C6"); //(198)	Latin capital letter ae HTML 2.0
		sOut= sIn.replace("&Ccedil;","\u00C7"); //(199)		Latin capital letter c with cedilla HTML 2.0
		sOut= sIn.replace("&Egrave;","\u00C8"); //(200)		Latin capital letter e with grave HTML 2.0
		sOut= sIn.replace("&Eacute;","\u00C9"); //(201)		Latin capital letter e with acute HTML 2.0
		sOut= sIn.replace("&Ecirc;","\u00CA"); //(202)	Latin capital letter e with circumflex HTML 2.0
		sOut= sIn.replace("&Euml;","\u00CB"); //(203)		Latin capital letter e with diaeresis HTML 2.0
		sOut= sIn.replace("&Igrave;","\u00CC"); //(204)		Latin capital letter i with grave HTML 2.0
		sOut= sIn.replace("&Iacute;","\u00CD"); //(205)		Latin capital letter i with acute HTML 2.0
		sOut= sIn.replace("&Icirc;","\u00CE"); //(206)	Latin capital letter i with circumflex HTML 2.0
		sOut= sIn.replace("&Iuml;","\u00CF"); //(207)		Latin capital letter i with diaeresis HTML 2.0
		sOut= sIn.replace("&ETH;","\u00D0"); //(208)	Latin capital letter eth HTML 2.0
		sOut= sIn.replace("&Ntilde;","\u00D1"); //(209)		Latin capital letter n with tilde HTML 2.0
		sOut= sIn.replace("&Ograve;","\u00D2"); //(210)		Latin capital letter o with grave HTML 2.0
		sOut= sIn.replace("&Oacute;","\u00D3"); //(211)		Latin capital letter o with acute HTML 2.0
		sOut= sIn.replace("&Ocirc;","\u00D4"); //(212)	Latin capital letter o with circumflex HTML 2.0
		sOut= sIn.replace("&Otilde;","\u00D5"); //(213)		Latin capital letter o with tilde HTML 2.0
		sOut= sIn.replace("&Ouml;","\u00D6"); //(214)		Latin capital letter o with diaeresis HTML 2.0
		sOut= sIn.replace("&times;","\u00D7"); //(215)	multiplication sign HTML 3.2
		sOut= sIn.replace("&Oslash;","\u00D8"); //(216)		Latin capital letter o with stroke HTML 2.0
		sOut= sIn.replace("&Ugrave;","\u00D9"); //(217)		Latin capital letter u with grave HTML 2.0
		sOut= sIn.replace("&Uacute;","\u00DA"); //(218)		Latin capital letter u with acute HTML 2.0
		sOut= sIn.replace("&Ucirc;","\u00DB"); //(219)	Latin capital letter u with circumflex HTML 2.0
		sOut= sIn.replace("&Uuml;","\u00DC"); //(220)		Latin capital letter u with diaeresis HTML 2.0
		sOut= sIn.replace("&Yacute;","\u00DD"); //(221)		Latin capital letter y with acute HTML 2.0
		sOut= sIn.replace("&THORN;","\u00DE"); //(222)	Latin capital letter thorn HTML 2.0
		sOut= sIn.replace("&szlig;","\u00DF"); //(223)	Latin small letter sharp s (German Eszett) HTML 2.0
		sOut= sIn.replace("&agrave;","\u00E0"); //(224)		Latin small letter a with grave HTML 2.0
		sOut= sIn.replace("&aacute;","\u00E1"); //(225)		Latin small letter a with acute HTML 2.0
		sOut= sIn.replace("&acirc;","\u00E2"); //(226)	Latin small letter a with circumflex HTML 2.0
		sOut= sIn.replace("&atilde;","\u00E3"); //(227)		Latin small letter a with tilde HTML 2.0
		sOut= sIn.replace("&auml;","\u00E4"); //(228)		Latin small letter a with diaeresis HTML 2.0
		sOut= sIn.replace("&aring;","\u00E5"); //(229)	Latin small letter a with ring above HTML 2.0
		sOut= sIn.replace("&aelig;","\u00E6"); //(230)	Latin lowercase ligature ae HTML 2.0
		sOut= sIn.replace("&ccedil;","\u00E7"); //(231)		Latin small letter c with cedilla HTML 2.0
		sOut= sIn.replace("&egrave;","\u00E8"); //(232)		Latin small letter e with grave HTML 2.0
		sOut= sIn.replace("&eacute;","\u00E9"); //(233)		Latin small letter e with acute HTML 2.0
		sOut= sIn.replace("&ecirc;","\u00EA"); //(234)	Latin small letter e with circumflex HTML 2.0
		sOut= sIn.replace("&euml;","\u00EB"); //(235)		Latin small letter e with diaeresis HTML 2.0
		sOut= sIn.replace("&igrave;","\u00EC"); //(236)		Latin small letter i with grave HTML 2.0
		sOut= sIn.replace("&iacute;","\u00ED"); //(237)		Latin small letter i with acute HTML 2.0
		sOut= sIn.replace("&icirc;","\u00EE"); //(238)	Latin small letter i with circumflex HTML 2.0
		sOut= sIn.replace("&iuml;","\u00EF"); //(239)		Latin small letter i with diaeresis HTML 2.0
		sOut= sIn.replace("&eth;","\u00F0"); //(240)	Latin small letter eth HTML 2.0
		sOut= sIn.replace("&ntilde;","\u00F1"); //(241)		Latin small letter n with tilde HTML 2.0
		sOut= sIn.replace("&ograve;","\u00F2"); //(242)		Latin small letter o with grave HTML 2.0
		sOut= sIn.replace("&oacute;","\u00F3"); //(243)		Latin small letter o with acute HTML 2.0
		sOut= sIn.replace("&ocirc;","\u00F4"); //(244)	Latin small letter o with circumflex HTML 2.0
		sOut= sIn.replace("&otilde;","\u00F5"); //(245)		Latin small letter o with tilde HTML 2.0
		sOut= sIn.replace("&ouml;","\u00F6"); //(246)		Latin small letter o with diaeresis HTML 2.0
		sOut= sIn.replace("&divide;","\u00F7"); //(247)		division sign HTML 3.2
		sOut= sIn.replace("&oslash;","\u00F8"); //(248)		Latin small letter o with stroke HTML 2.0
		sOut= sIn.replace("&ugrave;","\u00F9"); //(249)		Latin small letter u with grave HTML 2.0
		sOut= sIn.replace("&uacute;","\u00FA"); //(250)		Latin small letter u with acute HTML 2.0
		sOut= sIn.replace("&ucirc;","\u00FB"); //(251)	Latin small letter u with circumflex HTML 2.0
		sOut= sIn.replace("&uuml;","\u00FC"); //(252)		Latin small letter u with diaeresis HTML 2.0
		sOut= sIn.replace("&yacute;","\u00FD"); //(253)		Latin small letter y with acute HTML 2.0
		sOut= sIn.replace("&thorn;","\u00FE"); //(254)	Latin small letter thorn HTML 2.0
		sOut= sIn.replace("&yuml;","\u00FF"); //(255)		Latin small letter y with diaeresis HTML 2.0
		sOut= sIn.replace("&OElig;","\u0152"); //(338)	Latin capital ligature oe HTML 4.0
		sOut= sIn.replace("&oelig;","\u0153"); //(339)	Latin small ligature oe HTML 4.0
		sOut= sIn.replace("&Scaron;","\u0160"); //(352)		Latin capital letter s with caron HTML 4.0
		sOut= sIn.replace("&scaron;","\u0161"); //(353)		Latin small letter s with caron HTML 4.0
		sOut= sIn.replace("&Yuml;","\u0178"); //(376)		Latin capital letter y with diaeresis HTML 4.0
		sOut= sIn.replace("&fnof;","\u0192"); //(402)		Latin small letter f with hook HTML 4.0
		sOut= sIn.replace("&circ;","\u02C6"); //(710)		modifier letter circumflex accent HTML 4.0
		sOut= sIn.replace("&tilde;","\u02DC"); //(732)	small tilde HTML 4.0
		sOut= sIn.replace("&Alpha;","\u0391"); //(913)	Greek capital letter alpha HTML 4.0
		sOut= sIn.replace("&Beta;","\u0392"); //(914)		Greek capital letter beta HTML 4.0
		sOut= sIn.replace("&Gamma;","\u0393"); //(915)	Greek capital letter gamma HTML 4.0
		sOut= sIn.replace("&Delta;","\u0394"); //(916)	Greek capital letter delta HTML 4.0
		sOut= sIn.replace("&Epsilon;","\u0395"); //(917)	Greek capital letter epsilon HTML 4.0
		sOut= sIn.replace("&Zeta;","\u0396"); //(918)		Greek capital letter zeta HTML 4.0
		sOut= sIn.replace("&Eta;","\u0397"); //(919)	Greek capital letter eta HTML 4.0
		sOut= sIn.replace("&Theta;","\u0398"); //(920)	Greek capital letter theta HTML 4.0
		sOut= sIn.replace("&Iota;","\u0399"); //(921)		Greek capital letter iota HTML 4.0
		sOut= sIn.replace("&Kappa;","\u039A"); //(922)	Greek capital letter kappa HTML 4.0
		sOut= sIn.replace("&Lambda;","\u039B"); //(923)		Greek capital letter lambda HTML 4.0
		sOut= sIn.replace("&Mu;","\u039C"); //(924)		Greek capital letter mu HTML 4.0
		sOut= sIn.replace("&Nu;","\u039D"); //(925)		Greek capital letter nu HTML 4.0
		sOut= sIn.replace("&Xi;","\u039E"); //(926)		Greek capital letter xi HTML 4.0
		sOut= sIn.replace("&Omicron;","\u039F"); //(927)	Greek capital letter omicron HTML 4.0
		sOut= sIn.replace("&Pi;","\u03A0"); //(928)		Greek capital letter pi HTML 4.0
		sOut= sIn.replace("&Rho;","\u03A1"); //(929)	Greek capital letter rho HTML 4.0
		sOut= sIn.replace("&Sigma;","\u03A3"); //(931)	Greek capital letter sigma HTML 4.0
		sOut= sIn.replace("&Tau;","\u03A4"); //(932)	Greek capital letter tau HTML 4.0
		sOut= sIn.replace("&Upsilon;","\u03A5"); //(933)	Greek capital letter upsilon HTML 4.0
		sOut= sIn.replace("&Phi;","\u03A6"); //(934)	Greek capital letter phi HTML 4.0
		sOut= sIn.replace("&Chi;","\u03A7"); //(935)	Greek capital letter chi HTML 4.0
		sOut= sIn.replace("&Psi;","\u03A8"); //(936)	Greek capital letter psi HTML 4.0
		sOut= sIn.replace("&Omega;","\u03A9"); //(937)	Greek capital letter omega HTML 4.0
		sOut= sIn.replace("&alpha;","\u03B1"); //(945)	Greek small letter alpha HTML 4.0
		sOut= sIn.replace("&beta;","\u03B2"); //(946)		Greek small letter beta HTML 4.0
		sOut= sIn.replace("&gamma;","\u03B3"); //(947)	Greek small letter gamma HTML 4.0
		sOut= sIn.replace("&delta;","\u03B4"); //(948)	Greek small letter delta HTML 4.0
		sOut= sIn.replace("&epsilon;","\u03B5"); //(949)	Greek small letter epsilon HTML 4.0
		sOut= sIn.replace("&zeta;","\u03B6"); //(950)		Greek small letter zeta HTML 4.0
		sOut= sIn.replace("&eta;","\u03B7"); //(951)	Greek small letter eta HTML 4.0
		sOut= sIn.replace("&theta;","\u03B8"); //(952)	Greek small letter theta HTML 4.0
		sOut= sIn.replace("&iota;","\u03B9"); //(953)		Greek small letter iota HTML 4.0
		sOut= sIn.replace("&kappa;","\u03BA"); //(954)	Greek small letter kappa HTML 4.0
		sOut= sIn.replace("&lambda;","\u03BB"); //(955)		Greek small letter lambda HTML 4.0
		sOut= sIn.replace("&mu;","\u03BC"); //(956)		Greek small letter mu HTML 4.0
		sOut= sIn.replace("&nu;","\u03BD"); //(957)		Greek small letter nu HTML 4.0
		sOut= sIn.replace("&xi;","\u03BE"); //(958)		Greek small letter xi HTML 4.0
		sOut= sIn.replace("&omicron;","\u03BF"); //(959)	Greek small letter omicron HTML 4.0
		sOut= sIn.replace("&pi;","\u03C0"); //(960)		Greek small letter pi HTML 4.0
		sOut= sIn.replace("&rho;","\u03C1"); //(961)	Greek small letter rho HTML 4.0
		sOut= sIn.replace("&sigmaf;","\u03C2"); //(962)		Greek small letter final sigma HTML 4.0
		sOut= sIn.replace("&sigma;","\u03C3"); //(963)	Greek small letter sigma HTML 4.0
		sOut= sIn.replace("&tau;","\u03C4"); //(964)	Greek small letter tau HTML 4.0
		sOut= sIn.replace("&upsilon;","\u03C5"); //(965)	Greek small letter upsilon HTML 4.0
		sOut= sIn.replace("&phi;","\u03C6"); //(966)	Greek small letter phi HTML 4.0
		sOut= sIn.replace("&chi;","\u03C7"); //(967)	Greek small letter chi HTML 4.0
		sOut= sIn.replace("&psi;","\u03C8"); //(968)	Greek small letter psi HTML 4.0
		sOut= sIn.replace("&omega;","\u03C9"); //(969)	Greek small letter omega HTML 4.0
		sOut= sIn.replace("&thetasym;","\u03D1"); //(977)		Greek theta symbol HTML 4.0
		sOut= sIn.replace("&upsih;","\u03D2"); //(978)	Greek upsilon with hook symbol HTML 4.0
		sOut= sIn.replace("&piv;","\u03D6"); //(982)	Greek pi symbol HTML 4.0
		sOut= sIn.replace("&ensp;","\u2002"); //(8194)	en space [1] HTML 4.0
		sOut= sIn.replace("&emsp;","\u2003"); //(8195)	em space [2] HTML 4.0
		sOut= sIn.replace("&thinsp;","\u2009"); //(8201)	thin space [3] HTML 4.0
		sOut= sIn.replace("&zwnj;","\u200C"); //(8204)	zero width non-joiner HTML 4.0
		sOut= sIn.replace("&zwj;","\u200D"); //(8205)		zero width joiner HTML 4.0
		sOut= sIn.replace("&lrm;","\u200E"); //(8206)		left-to-right mark HTML 4.0
		sOut= sIn.replace("&rlm;","\u200F"); //(8207)		right-to-left mark HTML 4.0
		sOut= sIn.replace("&ndash;","\u2013"); //(8211)		en dash HTML 4.0
		sOut= sIn.replace("&mdash;","\u2014"); //(8212)		em dash HTML 4.0
		sOut= sIn.replace("&lsquo;","\u2018"); //(8216)		left single quotation mark HTML 4.0
		sOut= sIn.replace("&rsquo;","\u2019"); //(8217)		right single quotation mark HTML 4.0
		sOut= sIn.replace("&sbquo;","\u201A"); //(8218)		single low-9 quotation mark HTML 4.0
		sOut= sIn.replace("&ldquo;","\u201C"); //(8220)		left double quotation mark HTML 4.0
		sOut= sIn.replace("&rdquo;","\u201D"); //(8221)		right double quotation mark HTML 4.0
		sOut= sIn.replace("&bdquo;","\u201E"); //(8222)		double low-9 quotation mark HTML 4.0
		sOut= sIn.replace("&dagger;","\u2020"); //(8224)	dagger HTML 4.0
		sOut= sIn.replace("&Dagger;","\u2021"); //(8225)	double dagger HTML 4.0
		sOut= sIn.replace("&bull;","\u2022"); //(8226)	bullet HTML 4.0
		sOut= sIn.replace("&hellip;","\u2026"); //(8230)	horizontal ellipsis HTML 4.0
		sOut= sIn.replace("&permil;","\u2030"); //(8240)	per mille sign HTML 4.0
		sOut= sIn.replace("&prime;","\u2032"); //(8242)		prime HTML 4.0
		sOut= sIn.replace("&Prime;","\u2033"); //(8243)		double prime HTML 4.0
		sOut= sIn.replace("&lsaquo;","\u2039"); //(8249)	single left-pointing angle quotation mark HTML 4.0
		sOut= sIn.replace("&rsaquo;","\u203A"); //(8250)	single right-pointing angle quotation mark HTML 4.0
		sOut= sIn.replace("&oline;","\u203E"); //(8254)		overline HTML 4.0
		sOut= sIn.replace("&frasl;","\u2044"); //(8260)		fraction slash HTML 4.0
		sOut= sIn.replace("&euro;","\u20AC"); //(8364)	euro sign HTML 4.0
		sOut= sIn.replace("&image;","\u2111"); //(8465)		black-letter capital i HTML 4.0
		sOut= sIn.replace("&weierp;","\u2118"); //(8472)	script capital p (Weierstrass p) HTML 4.0
		sOut= sIn.replace("&real;","\u211C"); //(8476)	black-letter capital r HTML 4.0
		sOut= sIn.replace("&trade;","\u2122"); //(8482)		trademark sign HTML 4.0
		sOut= sIn.replace("&alefsym;","\u2135"); //(8501)		alef symbol HTML 4.0
		sOut= sIn.replace("&larr;","\u2190"); //(8592)	leftwards arrow HTML 4.0
		sOut= sIn.replace("&uarr;","\u2191"); //(8593)	upwards arrow HTML 4.0
		sOut= sIn.replace("&rarr;","\u2192"); //(8594)	rightwards arrow HTML 4.0
		sOut= sIn.replace("&darr;","\u2193"); //(8595)	downwards arrow HTML 4.0
		sOut= sIn.replace("&harr;","\u2194"); //(8596)	left right arrow HTML 4.0
		sOut= sIn.replace("&crarr;","\u21B5"); //(8629)		downwards arrow with corner leftwards HTML 4.0
		sOut= sIn.replace("&lArr;","\u21D0"); //(8656)	leftwards double arrow HTML 4.0
		sOut= sIn.replace("&uArr;","\u21D1"); //(8657)	upwards double arrow HTML 4.0
		sOut= sIn.replace("&rArr;","\u21D2"); //(8658)	rightwards double arrow HTML 4.0
		sOut= sIn.replace("&dArr;","\u21D3"); //(8659)	downwards double arrow HTML 4.0
		sOut= sIn.replace("&hArr;","\u21D4"); //(8660)	left right double arrow HTML 4.0
		sOut= sIn.replace("&forall;","\u2200"); //(8704)	for all HTML 4.0
		sOut= sIn.replace("&part;","\u2202"); //(8706)	partial differential HTML 4.0
		sOut= sIn.replace("&exist;","\u2203"); //(8707)		there exists HTML 4.0
		sOut= sIn.replace("&empty;","\u2205"); //(8709)		empty set HTML 4.0
		sOut= sIn.replace("&nabla;","\u2207"); //(8711)		nabla HTML 4.0
		sOut= sIn.replace("&isin;","\u2208"); //(8712)	element of HTML 4.0
		sOut= sIn.replace("&notin;","\u2209"); //(8713)		not an element of HTML 4.0
		sOut= sIn.replace("&ni;","\u220B"); //(8715)	contains as member HTML 4.0
		sOut= sIn.replace("&prod;","\u220F"); //(8719)	n-ary product HTML 4.0
		sOut= sIn.replace("&sum;","\u2211"); //(8721)		n-ary summation HTML 4.0
		sOut= sIn.replace("&minus;","\u2212"); //(8722)		minus sign HTML 4.0
		sOut= sIn.replace("&lowast;","\u2217"); //(8727)	asterisk operator HTML 4.0
		sOut= sIn.replace("&radic;","\u221A"); //(8730)		square root HTML 4.0
		sOut= sIn.replace("&prop;","\u221D"); //(8733)	proportional to HTML 4.0
		sOut= sIn.replace("&infin;","\u221E"); //(8734)		infinity HTML 4.0
		sOut= sIn.replace("&ang;","\u2220"); //(8736)		angle HTML 4.0
		sOut= sIn.replace("&and;","\u2227"); //(8743)		logical and HTML 4.0
		sOut= sIn.replace("&or;","\u2228"); //(8744)	logical or HTML 4.0
		sOut= sIn.replace("&cap;","\u2229"); //(8745)		intersection HTML 4.0
		sOut= sIn.replace("&cup;","\u222A"); //(8746)		union HTML 4.0
		sOut= sIn.replace("&int;","\u222B"); //(8747)		integral HTML 4.0
		sOut= sIn.replace("&there4;","\u2234"); //(8756)	therefore HTML 4.0
		sOut= sIn.replace("&sim;","\u223C"); //(8764)		tilde operator HTML 4.0
		sOut= sIn.replace("&cong;","\u2245"); //(8773)	congruent to HTML 4.0
		sOut= sIn.replace("&asymp;","\u2248"); //(8776)		almost equal to HTML 4.0
		sOut= sIn.replace("&ne;","\u2260"); //(8800)	not equal to HTML 4.0
		sOut= sIn.replace("&equiv;","\u2261"); //(8801)		identical to (equivalent to) HTML 4.0
		sOut= sIn.replace("&le;","\u2264"); //(8804)	less-than or equal to HTML 4.0
		sOut= sIn.replace("&ge;","\u2265"); //(8805)	greater-than or equal to HTML 4.0
		sOut= sIn.replace("&sub;","\u2282"); //(8834)		subset of HTML 4.0
		sOut= sIn.replace("&sup;","\u2283"); //(8835)		superset of HTML 4.0
		sOut= sIn.replace("&nsub;","\u2284"); //(8836)	not a subset of HTML 4.0
		sOut= sIn.replace("&sube;","\u2286"); //(8838)	subset of or equal to HTML 4.0
		sOut= sIn.replace("&supe;","\u2287"); //(8839)	superset of or equal to HTML 4.0
		sOut= sIn.replace("&oplus;","\u2295"); //(8853)		circled plus HTML 4.0
		sOut= sIn.replace("&otimes;","\u2297"); //(8855)	circled times HTML 4.0
		sOut= sIn.replace("&perp;","\u22A5"); //(8869)	up tack (perpendicular sign in math) HTML 4.0
		sOut= sIn.replace("&sdot;","\u22C5"); //(8901)	dot operator HTML 4.0
		sOut= sIn.replace("&lceil;","\u2308"); //(8968)		left ceiling HTML 4.0
		sOut= sIn.replace("&rceil;","\u2309"); //(8969)		right ceiling HTML 4.0
		sOut= sIn.replace("&lfloor;","\u230A"); //(8970)	left floor HTML 4.0
		sOut= sIn.replace("&rfloor;","\u230B"); //(8971)	right floor HTML 4.0
		sOut= sIn.replace("&lang;","\u2329"); //(9001)	left-pointing angle bracket HTML 4.0
		sOut= sIn.replace("&rang;","\u232A"); //(9002)	right-pointing angle bracket HTML 4.0
		sOut= sIn.replace("&loz;","\u25CA"); //(9674)		lozenge HTML 4.0
		sOut= sIn.replace("&spades;","\u2660"); //(9824)	black spade suit HTML 4.0
		sOut= sIn.replace("&clubs;","\u2663"); //(9827)		black club suit HTML 4.0
		sOut= sIn.replace("&hearts;","\u2665"); //(9829)	black heart suit HTML 4.0
		sOut= sIn.replace("&diams;","\u2666"); //
		return sOut;
	}


	/**
	 * Replaces the first occurance in the input string, a regular-expression
	 * with its replacement.
	 *
	 * @modified 2010.09.12
	 * @since 2010.09.12 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public static String replaceFirst(String sIn, String sWhat, String sWith)
	{
		Pattern p = Pattern.compile(sWhat);
		Matcher m = p.matcher(sIn);
		sIn = m.replaceFirst(sWith);
		return sIn;
	}


	/**
	 *
	 * @modified 2010.08.06
	 * @since 2010.08.06 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public static String setCurrentDate()
	{
		SimpleDateFormat formatter= new SimpleDateFormat ("yyyy.MM.dd");
		Date currentTime= new Date();
		String stringDate= formatter.format(currentTime);
		return stringDate;
	}


	/**
	 * Input: a path "g:/.../index.html/" or "g:/.../index.html"<br/>
	 * Output: index.html (g:/.../)
	 *
	 * @modified 2010.05.28
	 * @since 2010.05.28 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public static String setLastPartFirst(String sPath){
		sPath= sPath.replace('\\', '/');
		if (sPath.endsWith("/") || sPath.endsWith("\\"))
			sPath= sPath.substring(0, sPath.length()-1);
		String lp= "";
		String fp= "";
		lp= sPath.substring(sPath.lastIndexOf("/")+1);
		fp= sPath.substring(0, sPath.lastIndexOf("/")+1);

		return lp +" (" +fp +")";
	}



	/**
	 * Compares objects as strings.
	 */
	public static class StringCompare<E> implements Comparator<E>
	{
		private boolean icase;

		public StringCompare(boolean icase)
		{
			this.icase = icase;
		}

		public StringCompare()
		{
		}

		public int compare(E obj1, E obj2)
		{
			return compareStrings(obj1.toString(),
				obj2.toString(),icase);
		}
	}


}
