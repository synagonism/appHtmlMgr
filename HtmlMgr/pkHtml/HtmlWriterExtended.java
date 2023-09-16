/*
 * version: 2010.09.15
 * HtmlWriterExtended.java - Maps java-html-elements to html-elements.
 * :font:monospaced16 :tab:2 :indent:2 :wrap:72
 *
 * Copyright (C) 2010 Kaseluris-Nikos (HoKoNoUmo)
 * nikkas@otenet.gr
 * users.otenet.gr/~nikkas/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Generic Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU Generic Public License for more details.
 *
 * You should have received a copy of the GNU Generic Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA	02111-1307, USA.
 */
package pkHtml;

import javax.swing.text.*;
import javax.swing.text.html.*;
import java.io.Writer;
import java.util.Stack;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.net.URL;

/**
 * With the help of Stanislav Lapitsky JEditorPane's information
 * especially from his structure viewer code.
 * http://java-sl.com/HtmlJEditorPaneStructureTool.html
 *
 * @modified 2010.06.27
 * @since 2010.06.27 (v00.02.03)
 * @author HoKoNoUmo
 */
public class HtmlWriterExtended extends HTMLWriter
{

	/**
	 * Constructor.
	 * @modified 2010.06.27
	 * @since 2010.06.27 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public HtmlWriterExtended(Writer w, HTMLDocument doc) {
		super(w, doc, 0, doc.getLength());
		setLineLength(72);
	}


	/**
	 * Writes out all empty elements (all tags that have no
	 * corresponding end tag).
	 *
	 * @param elem	 an HTMLDocument-Element
	 * @exception IOException on any I/O error
	 * @exception BadLocationException if pos represents an invalid
	 *			  location within the document.
	 *
	 * @modified 2010.08.03
	 */
	protected void emptyTag(Element elem) throws BadLocationException, IOException
	{
		String sName= elem.getName();
		sElemPrev= sElemLast;
		sElemLast= sName+";EMPTY";
//System.out.println(sElemLast);

		Hashtable<String,String> htAttrs= getAttributes(elem);
		String sAValue= "";
		String sAVValue= "";

		if (sName.equals("title") && elem.getStartOffset()==0) {
			//because java creates 2 title-empty-elements
			writeLineSeparator();
			String title = (String)elem.getDocument().getProperty(Document.TitleProperty);
			write("  <title>" +title+"</title>");
		}
		else if (sName.equals("link")) {
			writeLineSeparator();
			write("  <link" +writeAttributes(elem)+"/>");
		}
		else if (sName.equals("meta")) {
			writeLineSeparator();
			write("  <meta" +writeAttributes(elem)+"/>");
		}
		else if (sName.equals("script")) {
			if (htAttrs.containsKey("endtag")){
				writeLineSeparator();
				write("  </script>");
			} else {
				writeLineSeparator();
				write("  <script" +writeAttributes(elem)+">");
			}
		}
		else if (sName.equals("comment")) {
			//the content of script
			//ppp writes it in one line, then line-comments destroy the script
			writeLineSeparator();
			write("    "+htAttrs.get("comment"));
		}
		else if (sName.equals("input")) {
			writeLineSeparator();
			write("  <input" +writeAttributes(elem)+"/>");
		}
		else if (sName.equals("br")) {
			if (bBold && bSpan && !htAttrs.containsKey("b") && !htAttrs.containsKey("span")) {
				//the last java-content-element was bold and span
				//because, next-code sets the bPrev variable
				write("</span></b>");
				writeLineSeparator();
				write("  <br/>");
				bBold= false;
				bSpan= false;
			}
			else if (bBold && !htAttrs.containsKey("b")) {
				//the last java-content-element was bold
				write("</b>");
				writeLineSeparator();
				write("  <br/>");
				bBold= false;
			}
			else if (bSpan && !htAttrs.containsKey("span")) {
				//the last java-content-element was bold
				write("</span>");
				writeLineSeparator();
				write("  <br/>");
				bSpan= false;
			}
			else if (bInsideLi) {
				writeLineSeparator();
				write("      <br/>");
			} else if (bInsideTd) {
				writeLineSeparator();
				write("        <br/>");
			} else {
				writeLineSeparator();
				write("  <br/>");
			}
			bPrevAName= false;
		}
		else if (sName.equals("hr")) {
			writeLineSeparator();
			write("<hr/>");
		}
		else if (sName.equals("img")) {
			//ppp needs improvement
			if (htAttrs.containsKey("a")){
				sAValue= htAttrs.get("a");
				sAVValue= sAValue.substring(sAValue.indexOf("=")+1).trim();
				writeLineSeparator();
				write("  <a href=\""+sAVValue+"\">");
				writeLineSeparator();
				write("  <img" +writeAttributes(elem)+"/>");
				writeLineSeparator();
				write("  </a>");
			}
			else {
				//write in a new line
				writeLineSeparator();
				write("  <img" +writeAttributes(elem)+"/>");
			}
		}

		else if (sName.equals("content")){
			String sText= getText(elem);
			bPrevBold= bBold;
			bPrevSpan= bSpan;
			if (htAttrs.containsKey("b"))
				bBold= true;
			else
				bBold= false;
			if (htAttrs.containsKey("span"))
				bSpan= true;
			else
				bSpan= false;

			if (htAttrs.size()==1){ //content-element
				//one att ==> only content
				if (sElemPrev.equals("p;START") || bPrevAName) {
					// the FIRST text of <p>, on new line
					writeLineSeparator();
					write("  "+sText);
					bPrevAName= false;
				}
				else if (getText(elem).equals(" ") && bPrevBold){
					write("</b> ");
				}
				else if (bPrevBold)
						write("</b>"+sText);
				else if (getText(elem).equals(" ") && bPrevSpan){
					write("</span> ");
				}
				else if (bPrevSpan)
						write("</span>"+sText);
				else if (!sText.equals(""))
						write(sText);
			}

			else if (htAttrs.containsKey("a")){ //content-element
//					a,name=h0c1
//					a,name=nlMisc
//					a,href=www.xxx#yyy
//					a,class=hide
				sAValue= htAttrs.get("a");
				sAVValue= sAValue.substring(sAValue.indexOf("=")+1).trim();

				if (sAValue.startsWith("name=")){
					//nnn java does not understand empty-a-elements
					if (sText.endsWith("\\n")){
						writeLineSeparator();
						write("  <a name=\""+sAVValue+"\"></a>");
						bANameWritten= true;
						bPrevAName= true;
					}
//					else if (htAttrs.containsKey("span")){
//						String sVSpan= htAttrs.get("span");
//						String sVVSpan= sVSpan.substring(sVSpan.indexOf("=")+1).trim();
//						if (sElemPrev.equals("p;START")){
//							writeLineSeparator();
//							write("  <a name=\""+sAVValue+"\"/>");
//							writeLineSeparator();
//							write("  <span class=\""+sVVSpan+"\">" +sText +"</span>");
//							bANameWritten= true;
//						} else {
//							write("<span class=\""+sVVSpan+"\">" +sText +"</span>");
//						}
//					}
//					else if (!sText.endsWith("\\n")){
//						if (bPrevBold){
//							write("</b>"+sText);
//						}
//						else if (bANameWritten){
//							write(sText);
//						}
//						else {
//							writeLineSeparator();
//							write("  <a name=\""+sAVValue+"\"/>");
//							writeLineSeparator();
//							write("  "+sText);
//							bANameWritten= true;
//						}
//					}
				}// name-anchor-element
				else if (sAValue.startsWith("class=")){
					if (sElemPrev.equals("br;EMPTY")) {
						write("<a class=\""+sAVValue+"\">");
						write(getText(elem)+"</a>");
					}
					else {
						writeLineSeparator();
						write("  <a class=\""+sAVValue+"\">");
						write(getText(elem)+"</a>");
					}
				}
				else if (sAValue.startsWith("href=")){
					if (bPrevAName && bBold){
						writeLineSeparator();
						write("  <b><a href=\""+sAVValue+"\">");
						write(getText(elem)+"</a>");
						bPrevAName= false;
					}
					else if (bPrevAName && !bBold){
						if (htAttrs.containsKey("span")){
							String sVSpan= htAttrs.get("span");
							String sVVSpan= sVSpan.substring(sVSpan.indexOf("=")+1).trim();
							writeLineSeparator();
							write("  <a href=\""+sAVValue+"\"><span class=\""+sVVSpan+"\">");
							write(getText(elem)+"</span></a>");
							bPrevAName= false;
							bSpan=false;
						}
						else {
							writeLineSeparator();
							write("  <a href=\""+sAVValue+"\">");
							write(getText(elem)+"</a>");
							bPrevAName= false;
						}
					}
					else if (!bBold && bPrevBold){
						write("</b> <a href=\""+sAVValue+"\">");
						write(getText(elem)+"</a>");
					}
					else if (htAttrs.containsKey("span")){
						if (bPrevSpan) {
							write("<a href=\""+sAVValue+"\">");
							write(getText(elem)+"</a>");
						}
						else {
							String sVSpan= htAttrs.get("span");
							String sVVSpan= sVSpan.substring(sVSpan.indexOf("=")+1).trim();
							write("<a href=\""+sAVValue+"\"><span class=\""+sVVSpan+"\">");
							write(getText(elem)+"</span></a>");
						}
					}
					else {
						write("<a href=\""+sAVValue+"\">");
						write(getText(elem)+"</a>");
					}
				}
			}//anchor-element

			else if (htAttrs.containsKey("b") && htAttrs.containsKey("span")){
				String sVSpan= htAttrs.get("span");
				String sVVSpan= sVSpan.substring(sVSpan.indexOf("=")+1).trim();
				if (sElemPrev.equals("p;START")){
					writeLineSeparator();
					write("  <b><span class=\""+sVVSpan+"\">" +sText );
				}
				else if (bPrevAName){
					writeLineSeparator();
					write("  <b><span class=\""+sVVSpan+"\">" +sText );
					bPrevAName= false;
				} else if (bPrevBold && bPrevSpan){
					write(sText);
				} else if (bPrevBold){
					write("<span class=\""+sVVSpan+"\">" +sText );
				} else if (bPrevSpan){
					write("<b>" +sText);
				} else {
					write("<b><span class=\""+sVVSpan+"\">" +sText );
				}
			}
			else if (htAttrs.containsKey("b")){
				if (sElemPrev.equals("p;START") || bPrevAName){
					writeLineSeparator();
					write("  <b>" +sText);
					bPrevAName= false;
				}
				else if (bPrevBold){
					bPrevAName= false;
					if (!bPrevSpan)
						write(sText);
					else
						write("</span>"+sText);
				}
				else {
					write("<b>" +sText);
					bPrevAName= false;
				}
			}

			else if (htAttrs.containsKey("span")){
				sAValue= htAttrs.get("span");
				sAVValue= sAValue.substring(sAValue.indexOf("=")+1).trim();
				if (sElemPrev.equals("p;START") || bPrevAName){
					writeLineSeparator();
					write("  <span class=\"" +sAVValue +"\">" +getText(elem));
					bPrevAName= false;
				}
				else if (bPrevSpan){
					bPrevAName= false;
					if (!bPrevBold)
						write(sText);
					else
						if (sText.startsWith(",")||sText.startsWith(".")||sText.startsWith(":"))
							write("</b>" +sText);
						else
							write("</b> " +sText);
				}
				else {
					bPrevAName= false;
					if (!bPrevBold)
						write("<span class=\"" +sAVValue +"\">" +getText(elem));
					else
						write("</b> <span class=\"" +sAVValue +"\">" +getText(elem));
				}
			}

			else if (htAttrs.containsKey("code")){
				write("<code>"+getText(elem)+"</code>");
			}
			else if (htAttrs.containsKey("i")){
				if (sElemPrev.equals("p;START") || bPrevAName){
					writeLineSeparator();
					write("  <i>"+getText(elem)+"</i>");
					bPrevAName= false;
				}
				else
					write("<i>"+getText(elem)+"</i>");
			}

			else if (getText(elem).endsWith("\\n") && bPrevBold){
				write("</b>");
			}
			else if (getText(elem).endsWith("\\n") && bPrevSpan){
				write("</span>");
			}
		}
	}


	/**
	 * Writes out a start tag for the element.
	 * Ignores all synthesized elements.
	 *
	 * @param elem	 an HTMLDocument-Element
	 * @exception IOException on any I/O error
	 * @modified 2010.07.28
	 * @since 2010.06.27 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	protected void startTag(Element elem) throws IOException, BadLocationException
	{
		bANameWritten= false;
		bPrevAName= false;
		String sName= elem.getName();
		sElemPrev= sElemLast;
		sElemLast= sName+";START";
//System.out.println(sElemLast);
		AttributeSet attrs = elem.getAttributes();

		if (sName.equals("html")) {
			//nnn write the extra-info that java discards
			write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writeLineSeparator();
			write("<!DOCTYPE html");
			writeLineSeparator();
			write("  PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"");
			writeLineSeparator();
			write("  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
			writeLineSeparator();
			if (attrs.getAttributeCount()==0)
				write("<html>");
			else {
				write("<html" +writeAttributes(elem)+">");
			}
		}
		else if (sName.equals("head")) {
			writeLineSeparator();
			write("<head>");
		}
		else if (sName.equals("body")) {
			writeLineSeparator();
			write("<body>");
		}
		else if (sName.equals("h1")) {
			writeLineSeparator();
			write("<h1>");
		}
		else if (sName.equals("h2")) {
			writeLineSeparator();
			write("<h2>");
		}
		else if (sName.equals("h3")) {
			writeLineSeparator();
			write("<h3>");
		}
		else if (sName.equals("h4")) {
			writeLineSeparator();
			write("<h4>");
		}
		else if (sName.equals("h5")) {
			writeLineSeparator();
			write("<h5>");
		}
		else if (sName.equals("h6")) {
			writeLineSeparator();
			write("<h6>");
		}
		else if (sName.equals("p")) {
			writeLineSeparator();
			if (attrs.getAttributeCount()==0)
				write("<p>");
			else {
				write("<p" +writeAttributes(elem)+">");
			}
		}
		else if (sName.equals("pre")) {
			writeLineSeparator();
			if (attrs.getAttributeCount()==0)
				write("  <pre>");
			else {
				write("  <pre" +writeAttributes(elem)+">");
			}
		}
		else if (sName.equals("table")){
			writeLineSeparator();
			if (attrs.getAttributeCount()==0)
				write("  <table>");
			else {
				write("  <table" +writeAttributes(elem)+">");
			}
		}
		else if (sName.equals("tr")) {
			writeLineSeparator();
			write("    <tr>");
		}
		else if (sName.equals("td")) {
			bInsideTd= true;
			if (attrs.getAttributeCount()==0) {
				writeLineSeparator();
				write("      <td>");
			} else {
				writeLineSeparator();
				write("      <td" +writeAttributes(elem)+">");
			}
		}
		else if (sName.equals("dl")){
			writeLineSeparator();
			write("  <dl>");
		}
		else if (sName.equals("dt")) {
			writeLineSeparator();
			write("    <dt>");
		}
		else if (sName.equals("dd")) {
			writeLineSeparator();
			write("    <dd>");
			writeLineSeparator();
			write("      ");
		}
		else if (sName.equals("ol")) {
			if (attrs.getAttributeCount()==0){
				writeLineSeparator();
				write(sIndentList+"<ol>");
				sIndentList= sIndentList +"    ";
				sIndentListLI= sIndentListLI +"    ";
			} else {
				writeLineSeparator();
				write(sIndentList+"<ol" +writeAttributes(elem)+">");
				sIndentList= sIndentList +"    ";
				sIndentListLI= sIndentListLI +"    ";
			}
		}
		else if (sName.equals("ul")) {
			if (attrs.getAttributeCount()==0){
				writeLineSeparator();
				write(sIndentList+"<ul>");
				sIndentList= sIndentList +"    ";
				sIndentListLI= sIndentListLI +"    ";
			} else {
				writeLineSeparator();
				write(sIndentList+"<ul" +writeAttributes(elem)+">");
				sIndentList= sIndentList +"    ";
				sIndentListLI= sIndentListLI +"    ";
			}
		}
		else if (sName.equals("li")) {
			if (attrs.getAttributeCount()==0){
				writeLineSeparator();
				write(sIndentListLI+"<li>");
			} else {
				writeLineSeparator();
				write(sIndentListLI+"<li" +writeAttributes(elem)+">");
			}
			bInsideLi= true;
		}
	}


	/**
	 * Writes out an end tag for the element.
	 *
	 * @param elem	  an HTMLDocument-Element
	 * @exception IOException on any I/O error
	 */
	protected void endTag(Element elem) throws IOException {
		String sName= elem.getName();
		sElemPrev= sElemLast;
		sElemLast= sName+";END";
//System.out.println(sElemLast);
		if (sName.equals("head")) {
			writeLineSeparator();
			write("</head>");
		}
		else if (sName.equals("h1")) {
			writeLineSeparator();
			write("</h1>");
		}
		else if (sName.equals("h2")) {
			writeLineSeparator();
			write("</h2>");
		}
		else if (sName.equals("h3")) {
			writeLineSeparator();
			write("</h3>");
		}
		else if (sName.equals("h4")) {
			writeLineSeparator();
			write("</h4>");
		}
		else if (sName.equals("h5")) {
			writeLineSeparator();
			write("</h5>");
		}
		else if (sName.equals("h6")) {
			writeLineSeparator();
			write("</h6>");
		}
		else if (sName.equals("p")) {
			writeLineSeparator();
			write("</p>");
		}
		else if (sName.equals("pre")) {
			writeLineSeparator();
			write("  </pre>");
		}
		else if (sName.equals("table")) {
			writeLineSeparator();
			write("  </table>");
		}
		else if (sName.equals("tr")) {
			writeLineSeparator();
			write("    </tr>");
		}
		else if (sName.equals("td")) {
			write("</td>");
			bInsideTd=false;
		}
		else if (sName.equals("dl")) {
			writeLineSeparator();
			write("  </dl>");
		}
		else if (sName.equals("dt")) {
			write("</dt>");
		}
		else if (sName.equals("dd")) {
			writeLineSeparator();
			write("    </dd>");
		}
		else if (sName.equals("ol")) {
			writeLineSeparator();
			sIndentList= sIndentList.substring(0,sIndentList.length()-4);
			write(sIndentList+"</ol>");
			sIndentListLI= sIndentListLI.substring(0,sIndentListLI.length()-4);
		}
		else if (sName.equals("ul")) {
			writeLineSeparator();
			sIndentList= sIndentList.substring(0,sIndentList.length()-4);
			write(sIndentList+"</ul>");
			sIndentListLI= sIndentListLI.substring(0,sIndentListLI.length()-4);
		}
		else if (sName.equals("li")) {
			bInsideLi= false;
			if (sElemPrev.equals("ul;END")){
				writeLineSeparator();
				write(sIndentListLI+"</li>");
			}
			else
				write("</li>");
		}
		else if (sName.equals("body")) {
			writeLineSeparator();
			write("</body>");
		}
		else if (sName.equals("html")) {
			writeLineSeparator();
			write("</html>");
		}
	}


	/**
	 * Tests if this doc-element contains an html-attribute.
	 *
	 * @modified 2010.07.02
	 * @since 2010.07.02 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private boolean containsAttribute(Element elem, String sAttr)
	{
		Hashtable<String,String> phAttrs = getAttributes(elem);
		if (phAttrs.containsKey(sAttr))
			return true;
		else
			return false;
	}


	/**
	 * Returns a Hashtable with the attributes, as strings,
	 * of an HTMLDocument-Element.
	 *
	 * @modified 2010.07.09
	 * @since 2010.06.28 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private Hashtable<String,String> getAttributes(Element elem){
		AttributeSet attrs = elem.getAttributes();
		Hashtable<String,String> htAttrs= new Hashtable<String,String>();
		Enumeration keys=attrs.getAttributeNames();
		while( keys.hasMoreElements()) {
			Object key= keys.nextElement();
			Object value= attrs.getAttribute(key);
//			String sValue= HtmlUtilities.replaceCharWithReference(value.toString());
//			System.out.println("attr: "+key.toString()+","+value.toString());
			htAttrs.put(key.toString(), value.toString().replaceAll("\u0026","&amp;"));
		}
		return htAttrs;
	}


	/**
	 *
	 * @modified 2010.09.12
	 * @since 2010.06.26 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	protected String getText(Element elem) {
		int startOffset= elem.getStartOffset();
		int endOffset= elem.getEndOffset();
		try {
			String text=getDocument().getText(startOffset, endOffset-startOffset);
			text= text.replaceAll("\n", "\\\\n");
			text= text.replaceAll("\t", "\\\\t");
			text= text.replaceAll("\r", "\\\\r");
//			text= text.replaceAll("\"","&quot;");
//			text= text.replaceAll("\u0026","&amp;");
			text= text.replaceAll("\u003C","&lt;");
			text= text.replaceAll("\u003E","&gt;");
			text= text.replaceAll("\u00A0","&nbsp;");

			//GREEK-EXTENDED 7936-8190
			text= text.replaceAll("\u1F00","&#7936;"); //GREEK SMALL LETTER ALPHA WITH PSILI
			text= text.replaceAll("\u1F01","&#7937;"); //GREEK SMALL LETTER ALPHA WITH DASIA
			text= text.replaceAll("\u1F02","&#7938;"); //GREEK SMALL LETTER ALPHA WITH PSILI AND VARIA
			text= text.replaceAll("\u1F03","&#7939;"); //GREEK SMALL LETTER ALPHA WITH DASIA AND VARIA
			text= text.replaceAll("\u1F04","&#7940;"); //GREEK SMALL LETTER ALPHA WITH PSILI AND OXIA
			text= text.replaceAll("\u1F05","&#7941;"); //GREEK SMALL LETTER ALPHA WITH DASIA AND OXIA
			text= text.replaceAll("\u1F06","&#7942;"); //GREEK SMALL LETTER ALPHA WITH PSILI AND PERISPOMENI
			text= text.replaceAll("\u1F07","&#7943;"); //GREEK SMALL LETTER ALPHA WITH DASIA AND PERISPOMENI
			text= text.replaceAll("\u1F08","&#7944;"); //GREEK CAPITAL LETTER ALPHA WITH PSILI
			text= text.replaceAll("\u1F09","&#7945;"); //GREEK CAPITAL LETTER ALPHA WITH DASIA
			text= text.replaceAll("\u1F0A","&#7946;"); //GREEK CAPITAL LETTER ALPHA WITH PSILI AND VARIA
			text= text.replaceAll("\u1F0B","&#7947;"); //GREEK CAPITAL LETTER ALPHA WITH DASIA AND VARIA
			text= text.replaceAll("\u1F0C","&#7948;"); //GREEK CAPITAL LETTER ALPHA WITH PSILI AND OXIA
			text= text.replaceAll("\u1F0D","&#7949;"); //GREEK CAPITAL LETTER ALPHA WITH DASIA AND OXIA
			text= text.replaceAll("\u1F0E","&#7950;"); //GREEK CAPITAL LETTER ALPHA WITH PSILI AND PERISPOMENI
			text= text.replaceAll("\u1F0F","&#7951;"); //GREEK CAPITAL LETTER ALPHA WITH DASIA AND PERISPOMENI
			text= text.replaceAll("\u1F10","&#7952;"); //GREEK SMALL LETTER EPSILON WITH PSILI
			text= text.replaceAll("\u1F11","&#7953;"); //GREEK SMALL LETTER EPSILON WITH DASIA
			text= text.replaceAll("\u1F12","&#7954;"); //GREEK SMALL LETTER EPSILON WITH PSILI AND VARIA
			text= text.replaceAll("\u1F13","&#7955;"); //GREEK SMALL LETTER EPSILON WITH DASIA AND VARIA
			text= text.replaceAll("\u1F14","&#7956;"); //GREEK SMALL LETTER EPSILON WITH PSILI AND OXIA
			text= text.replaceAll("\u1F15","&#7957;"); //GREEK SMALL LETTER EPSILON WITH DASIA AND OXIA
			text= text.replaceAll("\u1F18","&#7960;"); //GREEK CAPITAL LETTER EPSILON WITH PSILI
			text= text.replaceAll("\u1F19","&#7961;"); //GREEK CAPITAL LETTER EPSILON WITH DASIA
			text= text.replaceAll("\u1F1A","&#7962;"); //GREEK CAPITAL LETTER EPSILON WITH PSILI AND VARIA
			text= text.replaceAll("\u1F1B","&#7963;"); //GREEK CAPITAL LETTER EPSILON WITH DASIA AND VARIA
			text= text.replaceAll("\u1F1C","&#7964;"); //GREEK CAPITAL LETTER EPSILON WITH PSILI AND OXIA
			text= text.replaceAll("\u1F1D","&#7965;"); //GREEK CAPITAL LETTER EPSILON WITH DASIA AND OXIA
			text= text.replaceAll("\u1F20","&#7968;"); //GREEK SMALL LETTER ETA WITH PSILI
			text= text.replaceAll("\u1F21","&#7969;"); //GREEK SMALL LETTER ETA WITH DASIA
			text= text.replaceAll("\u1F22","&#7970;"); //GREEK SMALL LETTER ETA WITH PSILI AND VARIA
			text= text.replaceAll("\u1F23","&#7971;"); //GREEK SMALL LETTER ETA WITH DASIA AND VARIA
			text= text.replaceAll("\u1F24","&#7972;"); //GREEK SMALL LETTER ETA WITH PSILI AND OXIA
			text= text.replaceAll("\u1F25","&#7973;"); //GREEK SMALL LETTER ETA WITH DASIA AND OXIA
			text= text.replaceAll("\u1F26","&#7974;"); //GREEK SMALL LETTER ETA WITH PSILI AND PERISPOMENI
			text= text.replaceAll("\u1F27","&#7975;"); //GREEK SMALL LETTER ETA WITH DASIA AND PERISPOMENI
			text= text.replaceAll("\u1F28","&#7976;"); //GREEK CAPITAL LETTER ETA WITH PSILI
			text= text.replaceAll("\u1F29","&#7977;"); //GREEK CAPITAL LETTER ETA WITH DASIA
			text= text.replaceAll("\u1F2A","&#7978;"); //GREEK CAPITAL LETTER ETA WITH PSILI AND VARIA
			text= text.replaceAll("\u1F2B","&#7979;"); //GREEK CAPITAL LETTER ETA WITH DASIA AND VARIA
			text= text.replaceAll("\u1F2C","&#7980;"); //GREEK CAPITAL LETTER ETA WITH PSILI AND OXIA
			text= text.replaceAll("\u1F2D","&#7981;"); //GREEK CAPITAL LETTER ETA WITH DASIA AND OXIA
			text= text.replaceAll("\u1F2E","&#7982;"); //GREEK CAPITAL LETTER ETA WITH PSILI AND PERISPOMENI
			text= text.replaceAll("\u1F2F","&#7983;"); //GREEK CAPITAL LETTER ETA WITH DASIA AND PERISPOMENI
			text= text.replaceAll("\u1F30","&#7984;"); //GREEK SMALL LETTER IOTA WITH PSILI
			text= text.replaceAll("\u1F31","&#7985;"); //GREEK SMALL LETTER IOTA WITH DASIA
			text= text.replaceAll("\u1F32","&#7986;"); //GREEK SMALL LETTER IOTA WITH PSILI AND VARIA
			text= text.replaceAll("\u1F33","&#7987;"); //GREEK SMALL LETTER IOTA WITH DASIA AND VARIA
			text= text.replaceAll("\u1F34","&#7988;"); //GREEK SMALL LETTER IOTA WITH PSILI AND OXIA
			text= text.replaceAll("\u1F35","&#7989;"); //GREEK SMALL LETTER IOTA WITH DASIA AND OXIA
			text= text.replaceAll("\u1F36","&#7990;"); //GREEK SMALL LETTER IOTA WITH PSILI AND PERISPOMENI
			text= text.replaceAll("\u1F37","&#7991;"); //GREEK SMALL LETTER IOTA WITH DASIA AND PERISPOMENI
			text= text.replaceAll("\u1F38","&#7992;"); //GREEK CAPITAL LETTER IOTA WITH PSILI
			text= text.replaceAll("\u1F39","&#7993;"); //GREEK CAPITAL LETTER IOTA WITH DASIA
			text= text.replaceAll("\u1F3A","&#7994;"); //GREEK CAPITAL LETTER IOTA WITH PSILI AND VARIA
			text= text.replaceAll("\u1F3B","&#7995;"); //GREEK CAPITAL LETTER IOTA WITH DASIA AND VARIA
			text= text.replaceAll("\u1F3C","&#7996;"); //GREEK CAPITAL LETTER IOTA WITH PSILI AND OXIA
			text= text.replaceAll("\u1F3D","&#7997;"); //GREEK CAPITAL LETTER IOTA WITH DASIA AND OXIA
			text= text.replaceAll("\u1F3E","&#7998;"); //GREEK CAPITAL LETTER IOTA WITH PSILI AND PERISPOMENI
			text= text.replaceAll("\u1F3F","&#7999;"); //GREEK CAPITAL LETTER IOTA WITH DASIA AND PERISPOMENI
			text= text.replaceAll("\u1F40","&#8000;"); //GREEK SMALL LETTER OMICRON WITH PSILI
			text= text.replaceAll("\u1F41","&#8001;"); //GREEK SMALL LETTER OMICRON WITH DASIA
			text= text.replaceAll("\u1F42","&#8002;"); //GREEK SMALL LETTER OMICRON WITH PSILI AND VARIA
			text= text.replaceAll("\u1F43","&#8003;"); //GREEK SMALL LETTER OMICRON WITH DASIA AND VARIA
			text= text.replaceAll("\u1F44","&#8004;"); //GREEK SMALL LETTER OMICRON WITH PSILI AND OXIA
			text= text.replaceAll("\u1F45","&#8005;"); //GREEK SMALL LETTER OMICRON WITH DASIA AND OXIA
			text= text.replaceAll("\u1F48","&#8008;"); //GREEK CAPITAL LETTER OMICRON WITH PSILI
			text= text.replaceAll("\u1F49","&#8009;"); //GREEK CAPITAL LETTER OMICRON WITH DASIA
			text= text.replaceAll("\u1F4A","&#8010;"); //GREEK CAPITAL LETTER OMICRON WITH PSILI AND VARIA
			text= text.replaceAll("\u1F4B","&#8011;"); //GREEK CAPITAL LETTER OMICRON WITH DASIA AND VARIA
			text= text.replaceAll("\u1F4C","&#8012;"); //GREEK CAPITAL LETTER OMICRON WITH PSILI AND OXIA
			text= text.replaceAll("\u1F4D","&#8013;"); //GREEK CAPITAL LETTER OMICRON WITH DASIA AND OXIA
			text= text.replaceAll("\u1F50","&#8016;"); //GREEK SMALL LETTER UPSILON WITH PSILI
			text= text.replaceAll("\u1F51","&#8017;"); //GREEK SMALL LETTER UPSILON WITH DASIA
			text= text.replaceAll("\u1F52","&#8018;"); //GREEK SMALL LETTER UPSILON WITH PSILI AND VARIA
			text= text.replaceAll("\u1F53","&#8019;"); //GREEK SMALL LETTER UPSILON WITH DASIA AND VARIA
			text= text.replaceAll("\u1F54","&#8020;"); //GREEK SMALL LETTER UPSILON WITH PSILI AND OXIA
			text= text.replaceAll("\u1F55","&#8021;"); //GREEK SMALL LETTER UPSILON WITH DASIA AND OXIA
			text= text.replaceAll("\u1F56","&#8022;"); //GREEK SMALL LETTER UPSILON WITH PSILI AND PERISPOMENI
			text= text.replaceAll("\u1F57","&#8023;"); //GREEK SMALL LETTER UPSILON WITH DASIA AND PERISPOMENI
			text= text.replaceAll("\u1F59","&#8025;"); //GREEK CAPITAL LETTER UPSILON WITH DASIA
			text= text.replaceAll("\u1F5B","&#8027;"); //GREEK CAPITAL LETTER UPSILON WITH DASIA AND VARIA
			text= text.replaceAll("\u1F5D","&#8029;"); //GREEK CAPITAL LETTER UPSILON WITH DASIA AND OXIA
			text= text.replaceAll("\u1F5F","&#8031;"); //GREEK CAPITAL LETTER UPSILON WITH DASIA AND PERISPOMENI
			text= text.replaceAll("\u1F60","&#8032;"); //GREEK SMALL LETTER OMEGA WITH PSILI
			text= text.replaceAll("\u1F61","&#8033;"); //GREEK SMALL LETTER OMEGA WITH DASIA
			text= text.replaceAll("\u1F62","&#8034;"); //GREEK SMALL LETTER OMEGA WITH PSILI AND VARIA
			text= text.replaceAll("\u1F63","&#8035;"); //GREEK SMALL LETTER OMEGA WITH DASIA AND VARIA
			text= text.replaceAll("\u1F64","&#8036;"); //GREEK SMALL LETTER OMEGA WITH PSILI AND OXIA
			text= text.replaceAll("\u1F65","&#8037;"); //GREEK SMALL LETTER OMEGA WITH DASIA AND OXIA
			text= text.replaceAll("\u1F66","&#8038;"); //GREEK SMALL LETTER OMEGA WITH PSILI AND PERISPOMENI
			text= text.replaceAll("\u1F67","&#8039;"); //GREEK SMALL LETTER OMEGA WITH DASIA AND PERISPOMENI
			text= text.replaceAll("\u1F68","&#8040;"); //GREEK CAPITAL LETTER OMEGA WITH PSILI
			text= text.replaceAll("\u1F69","&#8041;"); //GREEK CAPITAL LETTER OMEGA WITH DASIA
			text= text.replaceAll("\u1F6A","&#8042;"); //GREEK CAPITAL LETTER OMEGA WITH PSILI AND VARIA
			text= text.replaceAll("\u1F6B","&#8043;"); //GREEK CAPITAL LETTER OMEGA WITH DASIA AND VARIA
			text= text.replaceAll("\u1F6C","&#8044;"); //GREEK CAPITAL LETTER OMEGA WITH PSILI AND OXIA
			text= text.replaceAll("\u1F6D","&#8045;"); //GREEK CAPITAL LETTER OMEGA WITH DASIA AND OXIA
			text= text.replaceAll("\u1F6E","&#8046;"); //GREEK CAPITAL LETTER OMEGA WITH PSILI AND PERISPOMENI
			text= text.replaceAll("\u1F6F","&#8047;"); //GREEK CAPITAL LETTER OMEGA WITH DASIA AND PERISPOMENI
			text= text.replaceAll("\u1F70","&#8048;"); //GREEK SMALL LETTER ALPHA WITH VARIA
			text= text.replaceAll("\u1F71","&#8049;"); //GREEK SMALL LETTER ALPHA WITH OXIA
			text= text.replaceAll("\u1F72","&#8050;"); //GREEK SMALL LETTER EPSILON WITH VARIA
			text= text.replaceAll("\u1F73","&#8051;"); //GREEK SMALL LETTER EPSILON WITH OXIA
			text= text.replaceAll("\u1F74","&#8052;"); //GREEK SMALL LETTER ETA WITH VARIA
			text= text.replaceAll("\u1F75","&#8053;"); //GREEK SMALL LETTER ETA WITH OXIA
			text= text.replaceAll("\u1F76","&#8054;"); //GREEK SMALL LETTER IOTA WITH VARIA
			text= text.replaceAll("\u1F77","&#8055;"); //GREEK SMALL LETTER IOTA WITH OXIA
			text= text.replaceAll("\u1F78","&#8056;"); //GREEK SMALL LETTER OMICRON WITH VARIA
			text= text.replaceAll("\u1F79","&#8057;"); //GREEK SMALL LETTER OMICRON WITH OXIA
			text= text.replaceAll("\u1F7A","&#8058;"); //GREEK SMALL LETTER UPSILON WITH VARIA
			text= text.replaceAll("\u1F7B","&#8059;"); //GREEK SMALL LETTER UPSILON WITH OXIA
			text= text.replaceAll("\u1F7C","&#8060;"); //GREEK SMALL LETTER OMEGA WITH VARIA
			text= text.replaceAll("\u1F7D","&#8061;"); //GREEK SMALL LETTER OMEGA WITH OXIA
			text= text.replaceAll("\u1F80","&#8064;"); //GREEK SMALL LETTER ALPHA WITH PSILI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F81","&#8065;"); //GREEK SMALL LETTER ALPHA WITH DASIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F82","&#8066;"); //GREEK SMALL LETTER ALPHA WITH PSILI AND VARIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F83","&#8067;"); //GREEK SMALL LETTER ALPHA WITH DASIA AND VARIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F84","&#8068;"); //GREEK SMALL LETTER ALPHA WITH PSILI AND OXIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F85","&#8069;"); //GREEK SMALL LETTER ALPHA WITH DASIA AND OXIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F86","&#8070;"); //GREEK SMALL LETTER ALPHA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F87","&#8071;"); //GREEK SMALL LETTER ALPHA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F88","&#8072;"); //GREEK CAPITAL LETTER ALPHA WITH PSILI AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F89","&#8073;"); //GREEK CAPITAL LETTER ALPHA WITH DASIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F8A","&#8074;"); //GREEK CAPITAL LETTER ALPHA WITH PSILI AND VARIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F8B","&#8075;"); //GREEK CAPITAL LETTER ALPHA WITH DASIA AND VARIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F8C","&#8076;"); //GREEK CAPITAL LETTER ALPHA WITH PSILI AND OXIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F8D","&#8077;"); //GREEK CAPITAL LETTER ALPHA WITH DASIA AND OXIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F8E","&#8078;"); //GREEK CAPITAL LETTER ALPHA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F8F","&#8079;"); //GREEK CAPITAL LETTER ALPHA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F90","&#8080;"); //GREEK SMALL LETTER ETA WITH PSILI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F91","&#8081;"); //GREEK SMALL LETTER ETA WITH DASIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F92","&#8082;"); //GREEK SMALL LETTER ETA WITH PSILI AND VARIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F93","&#8083;"); //GREEK SMALL LETTER ETA WITH DASIA AND VARIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F94","&#8084;"); //GREEK SMALL LETTER ETA WITH PSILI AND OXIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F95","&#8085;"); //GREEK SMALL LETTER ETA WITH DASIA AND OXIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F96","&#8086;"); //GREEK SMALL LETTER ETA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F97","&#8087;"); //GREEK SMALL LETTER ETA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1F98","&#8088;"); //GREEK CAPITAL LETTER ETA WITH PSILI AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F99","&#8089;"); //GREEK CAPITAL LETTER ETA WITH DASIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F9A","&#8090;"); //GREEK CAPITAL LETTER ETA WITH PSILI AND VARIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F9B","&#8091;"); //GREEK CAPITAL LETTER ETA WITH DASIA AND VARIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F9C","&#8092;"); //GREEK CAPITAL LETTER ETA WITH PSILI AND OXIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F9D","&#8093;"); //GREEK CAPITAL LETTER ETA WITH DASIA AND OXIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F9E","&#8094;"); //GREEK CAPITAL LETTER ETA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
			text= text.replaceAll("\u1F9F","&#8095;"); //GREEK CAPITAL LETTER ETA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
			text= text.replaceAll("\u1FA0","&#8096;"); //GREEK SMALL LETTER OMEGA WITH PSILI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FA1","&#8097;"); //GREEK SMALL LETTER OMEGA WITH DASIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FA2","&#8098;"); //GREEK SMALL LETTER OMEGA WITH PSILI AND VARIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FA3","&#8099;"); //GREEK SMALL LETTER OMEGA WITH DASIA AND VARIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FA4","&#8100;"); //GREEK SMALL LETTER OMEGA WITH PSILI AND OXIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FA5","&#8101;"); //GREEK SMALL LETTER OMEGA WITH DASIA AND OXIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FA6","&#8102;"); //GREEK SMALL LETTER OMEGA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FA7","&#8103;"); //GREEK SMALL LETTER OMEGA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FA8","&#8104;"); //GREEK CAPITAL LETTER OMEGA WITH PSILI AND PROSGEGRAMMENI
			text= text.replaceAll("\u1FA9","&#8105;"); //GREEK CAPITAL LETTER OMEGA WITH DASIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1FAA","&#8106;"); //GREEK CAPITAL LETTER OMEGA WITH PSILI AND VARIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1FAB","&#8107;"); //GREEK CAPITAL LETTER OMEGA WITH DASIA AND VARIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1FAC","&#8108;"); //GREEK CAPITAL LETTER OMEGA WITH PSILI AND OXIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1FAD","&#8109;"); //GREEK CAPITAL LETTER OMEGA WITH DASIA AND OXIA AND PROSGEGRAMMENI
			text= text.replaceAll("\u1FAE","&#8110;"); //GREEK CAPITAL LETTER OMEGA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
			text= text.replaceAll("\u1FAF","&#8111;"); //GREEK CAPITAL LETTER OMEGA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
			text= text.replaceAll("\u1FB0","&#8112;"); //GREEK SMALL LETTER ALPHA WITH VRACHY
			text= text.replaceAll("\u1FB1","&#8113;"); //GREEK SMALL LETTER ALPHA WITH MACRON
			text= text.replaceAll("\u1FB2","&#8114;"); //GREEK SMALL LETTER ALPHA WITH VARIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FB3","&#8115;"); //GREEK SMALL LETTER ALPHA WITH YPOGEGRAMMENI
			text= text.replaceAll("\u1FB4","&#8116;"); //GREEK SMALL LETTER ALPHA WITH OXIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FB6","&#8118;"); //GREEK SMALL LETTER ALPHA WITH PERISPOMENI
			text= text.replaceAll("\u1FB7","&#8119;"); //GREEK SMALL LETTER ALPHA WITH PERISPOMENI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FB8","&#8120;"); //GREEK CAPITAL LETTER ALPHA WITH VRACHY
			text= text.replaceAll("\u1FB9","&#8121;"); //GREEK CAPITAL LETTER ALPHA WITH MACRON
			text= text.replaceAll("\u1FBA","&#8122;"); //GREEK CAPITAL LETTER ALPHA WITH VARIA
			text= text.replaceAll("\u1FBB","&#8123;"); //GREEK CAPITAL LETTER ALPHA WITH OXIA
			text= text.replaceAll("\u1FBC","&#8124;"); //GREEK CAPITAL LETTER ALPHA WITH PROSGEGRAMMENI
			text= text.replaceAll("\u1FBD","&#8125;"); //GREEK KORONIS
			text= text.replaceAll("\u1FBE","&#8126;"); //GREEK PROSGEGRAMMENI
			text= text.replaceAll("\u1FBF","&#8127;"); //GREEK PSILI
			text= text.replaceAll("\u1FC0","&#8128;"); //GREEK PERISPOMENI
			text= text.replaceAll("\u1FC1","&#8129;"); //GREEK DIALYTIKA AND PERISPOMENI
			text= text.replaceAll("\u1FC2","&#8130;"); //GREEK SMALL LETTER ETA WITH VARIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FC3","&#8131;"); //GREEK SMALL LETTER ETA WITH YPOGEGRAMMENI
			text= text.replaceAll("\u1FC4","&#8132;"); //GREEK SMALL LETTER ETA WITH OXIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FC6","&#8134;"); //GREEK SMALL LETTER ETA WITH PERISPOMENI
			text= text.replaceAll("\u1FC7","&#8135;"); //GREEK SMALL LETTER ETA WITH PERISPOMENI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FC8","&#8136;"); //GREEK CAPITAL LETTER EPSILON WITH VARIA
			text= text.replaceAll("\u1FC9","&#8137;"); //GREEK CAPITAL LETTER EPSILON WITH OXIA
			text= text.replaceAll("\u1FCA","&#8138;"); //GREEK CAPITAL LETTER ETA WITH VARIA
			text= text.replaceAll("\u1FCB","&#8139;"); //GREEK CAPITAL LETTER ETA WITH OXIA
			text= text.replaceAll("\u1FCC","&#8140;"); //GREEK CAPITAL LETTER ETA WITH PROSGEGRAMMENI
			text= text.replaceAll("\u1FCD","&#8141;"); //GREEK PSILI AND VARIA
			text= text.replaceAll("\u1FCE","&#8142;"); //GREEK PSILI AND OXIA
			text= text.replaceAll("\u1FCF","&#8143;"); //GREEK PSILI AND PERISPOMENI
			text= text.replaceAll("\u1FD0","&#8144;"); //GREEK SMALL LETTER IOTA WITH VRACHY
			text= text.replaceAll("\u1FD1","&#8145;"); //GREEK SMALL LETTER IOTA WITH MACRON
			text= text.replaceAll("\u1FD2","&#8146;"); //GREEK SMALL LETTER IOTA WITH DIALYTIKA AND VARIA
			text= text.replaceAll("\u1FD3","&#8147;"); //GREEK SMALL LETTER IOTA WITH DIALYTIKA AND OXIA
			text= text.replaceAll("\u1FD6","&#8150;"); //GREEK SMALL LETTER IOTA WITH PERISPOMENI
			text= text.replaceAll("\u1FD7","&#8151;"); //GREEK SMALL LETTER IOTA WITH DIALYTIKA AND PERISPOMENI
			text= text.replaceAll("\u1FD8","&#8152;"); //GREEK CAPITAL LETTER IOTA WITH VRACHY
			text= text.replaceAll("\u1FD9","&#8153;"); //GREEK CAPITAL LETTER IOTA WITH MACRON
			text= text.replaceAll("\u1FDA","&#8154;"); //GREEK CAPITAL LETTER IOTA WITH VARIA
			text= text.replaceAll("\u1FDB","&#8155;"); //GREEK CAPITAL LETTER IOTA WITH OXIA
			text= text.replaceAll("\u1FDD","&#8157;"); //GREEK DASIA AND VARIA
			text= text.replaceAll("\u1FDE","&#8158;"); //GREEK DASIA AND OXIA
			text= text.replaceAll("\u1FDF","&#8159;"); //GREEK DASIA AND PERISPOMENI
			text= text.replaceAll("\u1FE0","&#8160;"); //GREEK SMALL LETTER UPSILON WITH VRACHY
			text= text.replaceAll("\u1FE1","&#8161;"); //GREEK SMALL LETTER UPSILON WITH MACRON
			text= text.replaceAll("\u1FE2","&#8162;"); //GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND VARIA
			text= text.replaceAll("\u1FE3","&#8163;"); //GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND OXIA
			text= text.replaceAll("\u1FE4","&#8164;"); //GREEK SMALL LETTER RHO WITH PSILI
			text= text.replaceAll("\u1FE5","&#8165;"); //GREEK SMALL LETTER RHO WITH DASIA
			text= text.replaceAll("\u1FE6","&#8166;"); //GREEK SMALL LETTER UPSILON WITH PERISPOMENI
			text= text.replaceAll("\u1FE7","&#8167;"); //GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND PERISPOMENI
			text= text.replaceAll("\u1FE8","&#8168;"); //GREEK CAPITAL LETTER UPSILON WITH VRACHY
			text= text.replaceAll("\u1FE9","&#8169;"); //GREEK CAPITAL LETTER UPSILON WITH MACRON
			text= text.replaceAll("\u1FEA","&#8170;"); //GREEK CAPITAL LETTER UPSILON WITH VARIA
			text= text.replaceAll("\u1FEB","&#8171;"); //GREEK CAPITAL LETTER UPSILON WITH OXIA
			text= text.replaceAll("\u1FEC","&#8172;"); //GREEK CAPITAL LETTER RHO WITH DASIA
			text= text.replaceAll("\u1FED","&#8173;"); //GREEK DIALYTIKA AND VARIA
			text= text.replaceAll("\u1FEE","&#8174;"); //GREEK DIALYTIKA AND OXIA
			text= text.replaceAll("\u1FEF","&#8175;"); //GREEK VARIA
			text= text.replaceAll("\u1FF2","&#8178;"); //GREEK SMALL LETTER OMEGA WITH VARIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FF3","&#8179;"); //GREEK SMALL LETTER OMEGA WITH YPOGEGRAMMENI
			text= text.replaceAll("\u1FF4","&#8180;"); //GREEK SMALL LETTER OMEGA WITH OXIA AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FF6","&#8182;"); //GREEK SMALL LETTER OMEGA WITH PERISPOMENI
			text= text.replaceAll("\u1FF7","&#8183;"); //GREEK SMALL LETTER OMEGA WITH PERISPOMENI AND YPOGEGRAMMENI
			text= text.replaceAll("\u1FF8","&#8184;"); //GREEK CAPITAL LETTER OMICRON WITH VARIA
			text= text.replaceAll("\u1FF9","&#8185;"); //GREEK CAPITAL LETTER OMICRON WITH OXIA
			text= text.replaceAll("\u1FFA","&#8186;"); //GREEK CAPITAL LETTER OMEGA WITH VARIA
			text= text.replaceAll("\u1FFB","&#8187;"); //GREEK CAPITAL LETTER OMEGA WITH OXIA
			text= text.replaceAll("\u1FFC","&#8188;"); //GREEK CAPITAL LETTER OMEGA WITH PROSGEGRAMMENI
			text= text.replaceAll("\u1FFD","&#8189;"); //GREEK OXIA
			text= text.replaceAll("\u1FFE","&#8190;"); //GREEK DASIA

			text= text.replaceAll("\u21E8","&#8680;");
			text= text.replaceAll("\u25AA","&#9642;");
//			text= text.replaceAll("&amp;amp;","&amp;");
			return text;
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}

		return null;
	}

	/**
	 * Returns the attributes of an HTMLDocument-element
	 * as string as: " key1="value1" key2="value2"".
	 *
	 * @modified 2010.07.02
	 * @since 2010.06.27 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private String writeAttributes(Element elem){
		String sOut="";

		Hashtable<String,String> htAttrs= getAttributes(elem);

		if (elem.getName().equals("img")){
			String sSrc= htAttrs.get("src").replace("\u0026","&amp;");
			sSrc= sSrc.replaceAll("&amp;amp;","&amp;");
			sOut= " src=\"" +sSrc;
			if (htAttrs.get("width")!=null)
				sOut= sOut +"\" width=\"" +htAttrs.get("width");
			if (htAttrs.get("height")!=null)
				sOut= sOut +"\" height=\"" +htAttrs.get("height");
			if (htAttrs.get("alt")!=null)
				sOut= sOut +"\" alt=\"" +htAttrs.get("alt") +"\"";
		}
		else if (htAttrs.containsKey("xmlns")){
			//html-element, enfore the sequence: xmlns, lang
			sOut= " xmlns=\"" +htAttrs.get("xmlns")
						+"\" xml:lang=\"" +htAttrs.get("lang") +"\"";
		}
		else if (htAttrs.containsKey("rel")){
			//link-element, enfore the sequence: href, rel, type
			//<link href="AAj.css" rel="stylesheet" type="text/css"/>
			sOut= " href=\"" +htAttrs.get("href")
						+"\" rel=\"" +htAttrs.get("rel")
						+"\" type=\"" +htAttrs.get("type") +"\"";
		}
		else {
			Enumeration<String> keys= htAttrs.keys();
			while(keys.hasMoreElements()) {
				String strKey= keys.nextElement();
				String sValue= htAttrs.get(strKey);
				//the name-key contains the name of the html-element
				//this HTMLDocument-element contains in its attributes.
				if (strKey.equals("name"))
					continue;
				sOut= sOut +" " +strKey +"=\"" +sValue +"\"";
			}
		}
		return sOut;
	}

	private boolean bANameWritten= false;
	private boolean bBold=false;
	private boolean bInsideLi= false; //ListItem
	private boolean bInsideTd= false; //TableData
	private boolean bPrevAName= false;
	private boolean bPrevBold=false;
	private boolean bPrevSpan=false;
	private boolean bSpan=false;
	private String sElemPrev="";
	private String sElemLast="";
	private String sIndentList="  ";
	private String sIndentListLI="";


}
