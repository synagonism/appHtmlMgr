/*
 * version: 2010.09.01
 * HtmlParser.java - An event-based parser that parses html-SFI-data.
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

import java.awt.Color;
import java.io.*;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.EmptyStackException;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * An EVENT-BASED parser that reads html-SFI-data (files of strings).<br/>
 * - Finds the SFIs in the data: &lt;a name="h0.2.3p1"&gt;
 * and puts &lt;location, title&gt; in the index of locations.
 * From this information we create the ToC.<br/>
 * - Indexes the words of the data to make a fast search.
 *
 * CODE:<code><br/>
 * HtmlParser parser = new HtmlParser(sUrlFile, null, tocNodeParent);</code><br/>
 * or</br><code>
 * HtmlParser parser = new HtmlParser(sHtml, sUrlData, tocNodeParent);</code>
 *
 * @modified 2010.05.12
 * @since 2010.05.12 (v00.02.02)
 * @author HoKoNoUmo
 */
public class HtmlParser
{

	private char ch;

	private DefaultMutableTreeNode tndParent; //the parent of fileNode
	private DefaultMutableTreeNode tndFile; 	//the parent of h1= the fileNode
	private DefaultMutableTreeNode tndH1; 		//the parent of h2
	private DefaultMutableTreeNode tndH2; 		//the parent of h3
	private DefaultMutableTreeNode tndH3; 		//the parent of h4
	private DefaultMutableTreeNode tndH4; 		//the parent of h5
	private DefaultMutableTreeNode tndH5; 		//the parent of h6
	private DefaultMutableTreeNode tndH6; 		//the parent of h7
	private DefaultMutableTreeNode tndH7; 		//the parent of h8
	private DefaultMutableTreeNode tndH8;
	private DefaultMutableTreeNode tndH9;
	private DefaultMutableTreeNode tndP;

	/** The current line number in the source content. */
	private int 						niLineHtmlFile=1;

	/** The reader provided by the caller of the parse method.	 */
	private Reader					readerBR;

	private Stack<String> 	stackParent = new Stack<String>();

	private String					sHeadingLast="";
	/** The location IN the file we are working: #h0.1.7p2 */
	private String					sSFI="";
	/** The url as string of the file: g:/file1/.../index.html. */
	private String 				sUrlFile="";
	private String 				sParent = null;
	private String 				sParent2 = "noparent2";
	private String 				sParent3 = "noparent3";
	/** String read too much. Provides push-back functionality. */
	private String 				sReadTooMuch= "\u0000";
	/** Holds the text-content of h1,h2,p elements. */
	private String					sContentPrevious="";



	/**
	 * Creates and initializes a parser.
	 * @param sIn	The string-url(file:g:/...) of the html-file we read-in
	 * 		OR the string of html-data we read.
	 * @param sUrlData	IF sIn is html-data then this is the string-url of
	 * 		the file we created the data. OTHERWISE null;
	 * @param tndParent
	 * 		The parent-node in the ToC on which will add the locations
	 * 		of the file and the file's-locations.
	 * @throws java.io.IOException
	 *			If an error occured while reading the input.
	 * @throws HtmlParserException
	 *			If an error occured while parsing the read data.
	 */
	public HtmlParser(String sIn, String sUrlData, DefaultMutableTreeNode tndParent)
	{
		//2. create map phUrlTitle(url<-->title) nnn When I'll use id for SFI, then the title-attribute can give a name for paragraph-elements.
		//3. create the ToC
		//4. create map phWord_UrlTS(word<-->urlTreeSet)

		this.tndParent= tndParent;
		readWordIgnored();

		try {
			if (sUrlData==null) {
				sUrlFile= sIn;
				if (sUrlFile.startsWith("http")) {
					try {
						URL url = new URL(sUrlFile);
						URLConnection connection = url.openConnection();
						InputStream is = connection.getInputStream();
						InputStreamReader isr = new InputStreamReader(is);
						readerBR = new BufferedReader(isr);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					FileInputStream fis = new FileInputStream(sUrlFile.substring(5));//wrka ppp
					InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
					readerBR = new BufferedReader(isr);
				}
			} else {
				sUrlFile= sUrlData;
				readerBR = new BufferedReader(new StringReader(sIn));
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println("!!!pHtml.Constructor: "+fnfe.toString());
		} catch (UnsupportedEncodingException uee) {
			System.out.println("!!!pHtml.Constructor: "+uee.toString());
		}

		niLineHtmlFile = 1;
		stackParent.push("noparent");
		stackParent.push("noparent2");
		stackParent.push("noparent3");

		for (;;){
			ch = scanWhitespace();
			if (ch == '\u0003')
				return;//we reached end of text
			else if (ch != '<') {
				throw throwExpectedInput("in-the-beginning-of-file","<");
			}
			ch = scanChar();
			if (ch == '?') {
				//<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
				skipElementXmlDeclaration();
//				ch= scanWhitespace();
				continue; //to read and doctype
			} else if (ch=='!'){
				ch= scanChar();
				if (ch== '-'){
					//<!-
					skipComment();
					continue;
				}
				if (ch== 'D'){
					//<!D
					skipElementDOCTYPE();
				}
				ch= scanWhitespace();
			}
			scanElement();//recursively. when read <html> go beginning and finish.
		}
	}


	/**
	 * Tests if a toc-tree-node is child of another.
	 * "h0.31.7.0.1" is child of "h0.31.7"
	 *
	 * @modified 2010.09.01
	 * @since 2010.08.31 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private boolean isChild(DefaultMutableTreeNode ndCh, DefaultMutableTreeNode ndPr)
	{
		String sChSFI= HtmlUtilities.getFIfromUrlString((String)ndCh.getUserObject());
		String sPrSFI= HtmlUtilities.getFIfromUrlString((String)ndPr.getUserObject());
		int niPrLevel= HtmlUtilities.findHeadingLevel(sPrSFI);

		return (HtmlUtilities.getSfiPartLevel(sChSFI,niPrLevel).equals(sPrSFI));
	}


	/**
	 *
	 * @modified 2010.05.16
	 * @since 2010.05.16 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private String getAnchorContent(String txtAnchor){
		//<a href="#h2.1">PREAMBLE</a>)
		//<a class="hide">hd0: treaties2009</a>
		return txtAnchor.substring(
					txtAnchor.indexOf(">")+1, txtAnchor.lastIndexOf("<"));
	}


	/**
	 * Indexes the words and the terms of a text with
	 * ONE url.
	 *
	 * @modified 2010.05.24
	 * @since 2010.05.24 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void indexText(String strText, String strUrl)
	{
		strText= HtmlUtilities.replaceCharReferencies(strText);
		StringTokenizer stkz = new StringTokenizer(strText, ",.:?()[]\" ");
		while (stkz.hasMoreTokens()) {
			String s= stkz.nextToken();
			//index-terms
			indexWord(s, strUrl);
			if (s.indexOf("-")!=-1  		// greek-language
					|| s.indexOf("_")!=-1	// language_greek
					|| s.indexOf("'")!=-1	// language'verb
					) {
				StringTokenizer stkz2 = new StringTokenizer(s, "-_");
				while (stkz2.hasMoreTokens())
					//index-words
					indexWord(stkz2.nextToken(), strUrl);
			}
		}
	}


	/**
	 * Adds words and terms to phWord_UrlTS
	 *
	 * @modified 2010.05.13
	 * @since 2010.05.13 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void indexWord(String strWord, String strUrl)
	{
		strWord = strWord.toLowerCase();

		//IF it is an ignored-word do nothing
		if (HtmlMgr.tvWordIgnored.contains(strWord))
			return;

		TreeSet<String> o = HtmlMgr.phWord_UrlTS.get(strWord);
		if(o == null) {
			TreeSet<String> arl= new TreeSet<String>();
			arl.add(strUrl);
			HtmlMgr.phWord_UrlTS.put(strWord, arl);
		}
		else
			o.add(strUrl);

		//FROM the map phUrlTitle, we will display only titles.
	}


	/**
	 * At element's TEXT-CONTENT we do the followings:<br/>
	 * - we put the titles of locations in the map &lt;Url,Title&gt;
	 *
	 * @modified 2010.05.18
	 * @since 2004.07.02 (v00.02.00)
	 * @author HoKoNoUmo
	 */
	private void processElementContentText(String elName, String cntnt,
																Hashtable pairAttr, String sParent)
	{
//System.out.println("..el: "+elName);//+": "+cntnt);
		String sUrlDoc= sUrlFile+"#"+sSFI;

		//the anchor-elements are-sended here.
		//>>AND<< the a-elements just after anchor.
		//we get from the empty ones the SFI, we are in.
		if (elName.equals("a")){
			if (cntnt.indexOf("name=\"h") != -1) {
				sSFI= cntnt.substring(
							cntnt.indexOf("=\"h")+2, cntnt.lastIndexOf("\""));
				//index the locators.
				indexWord(sSFI,sUrlDoc);
			}
			else if (cntnt.indexOf("href=") != -1) {
				sContentPrevious= HtmlUtilities.getTextContentOfElement(cntnt)+" ";
			}
		}

		else if (elName.equals("b")) {
			sContentPrevious= cntnt+" ";
		}
		else if (elName.equals("i")) {
			sContentPrevious= cntnt+" ";
		}

		//we create the heading-tree-nodes
		else if (elName.equals("title")){
			HtmlMgr.phUrlTitle.put(sUrlFile, cntnt);
			tndFile= HtmlMgr.createTocNode(sUrlFile);
			if (tndParent != null)
				tndParent.add(tndFile);
			else
				HtmlMgr.cTocNdRoot= tndFile;
			//index the locators.
			indexText(cntnt,sUrlDoc);

		}
		else if (elName.equals("h1")){
			cntnt= sContentPrevious+cntnt;
			sHeadingLast= elName;
			HtmlMgr.phUrlTitle.put(sUrlDoc, cntnt);
			tndH1= HtmlMgr.createTocNode(sUrlDoc);
			tndFile.add(tndH1);
			//index the locators.
			indexText(cntnt,sUrlDoc);
			sContentPrevious="";
		}
		else if (elName.equals("h2")){
			cntnt= sContentPrevious+cntnt;
			sHeadingLast= elName;
			HtmlMgr.phUrlTitle.put(sUrlDoc, cntnt);
			tndH2= HtmlMgr.createTocNode(sUrlDoc);
			if (isChild(tndH2,tndH1))
				tndH1.add(tndH2);
			else
				tndFile.add(tndH2);
			//index the locators.
			indexText(cntnt,sUrlDoc);
			sContentPrevious="";
		}
		else if (elName.equals("h3")){
			cntnt= sContentPrevious+cntnt;
			sHeadingLast= elName;
			HtmlMgr.phUrlTitle.put(sUrlDoc, cntnt);
			tndH3= HtmlMgr.createTocNode(sUrlDoc);
			if (isChild(tndH3,tndH2))
				tndH2.add(tndH3);
			else if (isChild(tndH3,tndH1))
				tndH1.add(tndH3);
			//index the locators.
			indexText(cntnt,sUrlDoc);
			sContentPrevious="";
		}
		else if (elName.equals("h4")){
			cntnt= sContentPrevious+cntnt;
			sHeadingLast= elName;
			HtmlMgr.phUrlTitle.put(sUrlDoc, cntnt);
			tndH4= HtmlMgr.createTocNode(sUrlDoc);
			if (isChild(tndH4,tndH3))
				tndH3.add(tndH4);
			else if (isChild(tndH4,tndH2))
				tndH2.add(tndH4);
			else if (isChild(tndH4,tndH1))
				tndH1.add(tndH4);
			//index the locators.
			indexText(cntnt,sUrlDoc);
			sContentPrevious="";
		}
		else if (elName.equals("h5")){
			cntnt= sContentPrevious+cntnt;
			sHeadingLast= elName;
			HtmlMgr.phUrlTitle.put(sUrlDoc, cntnt);
			tndH5= HtmlMgr.createTocNode(sUrlDoc);
			if (isChild(tndH5,tndH4))
				tndH4.add(tndH5);
			else if (isChild(tndH5,tndH3))
				tndH3.add(tndH5);
			else if (isChild(tndH5,tndH2))
				tndH2.add(tndH5);
			else if (isChild(tndH5,tndH1))
				tndH1.add(tndH5);
			//index the locators.
			indexText(cntnt,sUrlDoc);
			sContentPrevious="";
		}
		else if (elName.equals("h6")){
			cntnt= sContentPrevious+cntnt;
			sHeadingLast= elName;
			HtmlMgr.phUrlTitle.put(sUrlDoc, cntnt);
			tndH6= HtmlMgr.createTocNode(sUrlDoc);
			if (isChild(tndH6,tndH5))
				tndH5.add(tndH6);
			else if (isChild(tndH6,tndH4))
				tndH4.add(tndH6);
			else if (isChild(tndH6,tndH3))
				tndH3.add(tndH6);
			else if (isChild(tndH6,tndH2))
				tndH2.add(tndH6);
			else if (isChild(tndH6,tndH1))
				tndH1.add(tndH6);
			//index the locators.
			indexText(cntnt,sUrlDoc);
			sContentPrevious="";
		}

		else if (elName.equals("p")){
			cntnt= sContentPrevious+cntnt;
			//put and paragrafs on ToC
			//we can make smaller this pair, not put content because
			//its title is the same with SFI. nnn
			if (pairAttr!=null && pairAttr.contains("h0")){
				sHeadingLast= "h0";
			}
			else if (pairAttr!=null && pairAttr.contains("h7")){
				sHeadingLast= "h7";
				HtmlMgr.phUrlTitle.put(sUrlDoc, cntnt);
				tndH7= HtmlMgr.createTocNode(sUrlDoc);
				if (isChild(tndH7,tndH6))
					tndH6.add(tndH7);
				else if (isChild(tndH7,tndH5))
					tndH5.add(tndH7);
				else if (isChild(tndH7,tndH4))
					tndH4.add(tndH7);
				else if (isChild(tndH7,tndH3))
					tndH3.add(tndH7);
				else if (isChild(tndH7,tndH2))
					tndH2.add(tndH7);
				else if (isChild(tndH7,tndH1))
					tndH1.add(tndH7);
			}
			else if (pairAttr!=null && pairAttr.contains("h8")){
				sHeadingLast= "h8";
				HtmlMgr.phUrlTitle.put(sUrlDoc, cntnt);
				tndH8= HtmlMgr.createTocNode(sUrlDoc);
				if (isChild(tndH8,tndH7))
					tndH7.add(tndH8);
				else if (isChild(tndH8,tndH6))
					tndH6.add(tndH8);
				else if (isChild(tndH8,tndH5))
					tndH5.add(tndH8);
				else if (isChild(tndH8,tndH4))
					tndH4.add(tndH8);
				else if (isChild(tndH8,tndH3))
					tndH3.add(tndH8);
				else if (isChild(tndH8,tndH2))
					tndH2.add(tndH8);
				else if (isChild(tndH8,tndH1))
					tndH1.add(tndH8);
			}
			else if (pairAttr!=null
						&& !pairAttr.containsValue("notoc")
						&& !pairAttr.containsValue("notoc-center")
						&& !pairAttr.containsValue("notoc-last")
						&& !pairAttr.containsValue("notoc-last-center")
						&& !sSFI.equals("h0.toc")
						) {// ppp nnn for w3.org docs
				HtmlMgr.phUrlTitle.put(sUrlDoc,"#"+sSFI);
				tndP= HtmlMgr.createTocNode(sUrlDoc);
				if (sHeadingLast.equals("h0"))
					tndFile.add(tndP);
				else if (sHeadingLast.equals("h1"))
					tndH1.add(tndP);
				else if (sHeadingLast.equals("h2"))
					tndH2.add(tndP);
				else if (sHeadingLast.equals("h3"))
					tndH3.add(tndP);
				else if (sHeadingLast.equals("h4"))
					tndH4.add(tndP);
				else if (sHeadingLast.equals("h5"))
					tndH5.add(tndP);
				else if (sHeadingLast.equals("h6"))
					tndH6.add(tndP);
				else if (sHeadingLast.equals("h7"))
					tndH7.add(tndP);
				else if (sHeadingLast.equals("h8"))
					tndH8.add(tndP);
			}

			//index the locators.
			//remove markup
			cntnt=cntnt.replaceAll("</[^>]+>", "");
			cntnt=cntnt.replaceAll("<[^>]+>", "");
			indexText(cntnt,sUrlDoc);
			sContentPrevious="";
		}
//System.out.println(".. LastHeading: "+sHeadingLast);
	}


	/**
	 * At element's END we do the followings:<br/>
	 * a) XCONCEPT: we expand the row with parts at toc.<br/>
	 * b) INTxCPT: we display it as internal.
	 *
	 * @modified 2009.11.02
	 * @since 2004.07.02 (v00.02.00)
	 * @author HoKoNoUmo
	 */
	private void processElementEnd(String elName, String parent)
	{

		if (elName.equals("p")){
			sContentPrevious="";
		}
	}


	/**
	 * At element's START we do the followings:<br/>
	 * - at XCONCEPT:<b>
	 * - at Name_NounCase: <b>
	 * - at Name_Short: <b>
	 * - at REFINO_DEFINITION: <b>
	 * - at ANALYTIC: on file-xcpts, we write the "analytic-definition"
	 * in the attributes-area.<b>
	 * - at REFINO_PART: <b>
	 * - at XCPT: <b>
	 * - at INTxCPT: <b>
	 * - at REFINO_SPECIFICdIVISION: we add the "attribute" on which we create
	 * the division on toc.<b>
	 *
	 * @modified 2004.07.02
	 * @since 2004.07.02 (v00.02.00)
	 * @author HoKoNoUmo
	 */
	private void processElementStart(String elName,
													Hashtable atts,
													String parent, String parent2, String parent3)
	{
//		if (elName.equals("p")){
//			if (attr.contains("h0")){
//				sHeadingLast= "h0";
//				HtmlMgr.phUrlTitle.put(sUrlDoc, cntnt);
//				tndH1= HtmlMgr.createTocNode(sUrlDoc);
//				tndFile.add(tndH1);
//		}

	}


	/**
	 * Reads from the file "wordIgnored.txt" the words
	 * we ignore when we search for a term.
	 *
	 * @modified 2010.05.13
	 * @since 2010.05.13 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void readWordIgnored(){
		try {
			FileReader fr = new FileReader(HtmlMgr.sDirHome+"/pkHtml/wordIgnored.txt");
			BufferedReader br= new BufferedReader(fr);
			String ln = null;
			while ((ln = br.readLine()) != null){
				if (!ln.startsWith("#")){
					HtmlMgr.tvWordIgnored.add(ln);
//					System.out.println(ln);
				}
			}
			br.close();
		} catch (IOException e) {
			System.err.println("phtml: ignored-word >> "+e.toString());
		}
	}


	/**
	 * Reads a character from the reader with the html-file.
	 */
	private char scanChar() {
		//first check if the character is already read
		if (!sReadTooMuch.equals("\u0000")) {
			ch = sReadTooMuch.substring(0,1).charAt(0);
			sReadTooMuch = sReadTooMuch.substring(1);
			if (sReadTooMuch.length()==0)
				sReadTooMuch= "\u0000";
			return ch;
		}

		else {
			try {
				int i = readerBR.read();
				if (i < 0) {
					return '\u0003';//end of text
				} else if (i == 10) {//ppp on different OS.
						niLineHtmlFile += 1;
						return '\n';
				} else {
						return (char) i;
				}
			} catch (IOException e) {
				System.out.println("!!!pHtml.scanCar: "+e.toString());
			}
		}
		return '\u0000'; //something wrong
	}


	/**
	 * Html has text-content mixed with elements!!!!.<br/>
	 * - <code>&lt;br /&gt;</code> is replaced with new-line.<br/>
	 * - anchor elements absored inside text-content.<br/>
	 * - we stop at '&lt;/'. IMPORTANT: Everything before the end-tag
	 * belongs to text-content.
	 *
	 * <b>Precoditions:</b><br/>
	 * - text-content does NOT contain the "&lt;" character.
	 *
	 * @modified 2010.05.30
	 * @since 2004.07.04 (v00.02.00)
	 * @author HoKoNoUmo
	 */
	private String scanContentText() {
		StringBuffer data = new StringBuffer();

		ch = scanChar();

		for (;;) {

			if (ch == '<') { //found inline-element or end-tag.
				ch = scanChar();

				if (ch == '/'){ //end-tag
					scanStringBack("</");
					return data.toString();
				}

				else { //element inside text-content
					//ppp nnn ToDo what OTHER elements allowed
					// >>>inside<<<< TEXT-CONTENT.
					scanStringBack(String.valueOf(ch));
					String sElmName = scanIdentifier();

					if (sElmName.equals("br")){
						ch= scanWhitespace();
						if (ch != '/')
							throw throwExpectedInput("pHtml.scanContent","/ of br");
						ch= scanChar();//>
						data.append("\n");
					}

					else if (sElmName.equals("a")){
						String strElA= scanElementAnchorAndReturn();
						//ppp what to do with anchors.
						if (!strElA.endsWith("/>")){
							//put its text-content on content
							//<a href="#h2.1">PREAMBLE</a>)
							//<a class="hide">hd0: treaties2009</a>
							if (strElA.indexOf("class=\"hide\"") == -1)
								//ppp if we want to index the hide words.
								data.append(strElA.substring(strElA.indexOf(">")+1,
																						strElA.lastIndexOf("<")));
						}
					}
					else if (sElmName.equals("b")){
						String strElB= scanElementNonEmptyAndReturnContent("b");
						data.append(" "+strElB+" ");
					}
					else if (sElmName.equals("i")){
						String strElB= scanElementNonEmptyAndReturnContent("i");
						data.append(" "+strElB+" ");
					}
					else if (sElmName.equals("em")){
						String sElm= scanElementNonEmptyAndReturnContent("em");
						data.append(" "+sElm+" ");
					}
					else if (sElmName.equals("span")){
						String sElm= scanElementNonEmptyAndReturnContent("span");
						data.append(" "+sElm+" ");
					}
					else if (sElmName.equals("strong")){
						String sElm= scanElementNonEmptyAndReturnContent("strong");
						//ppp IF elements inside, do nothing
						if (sElm.indexOf("<")==-1)
							data.append(sElm+" ");
					}
					else if (sElmName.equals("sup")){
						String sElm= scanElementNonEmptyAndReturnContent("sup");
						data.append(sElm+" ");
					}
					else if (sElmName.equals("var")){
						String sElm= scanElementNonEmptyAndReturnContent("var");
						data.append(sElm);
					}

					//SKIP ELEMENTS
					else if (sElmName.equals("code")){
						skipElement("code");
					}

				}
			} else {
				data.append(ch);
			}
			ch = scanChar();
			if (ch == '\n')
				ch= scanWhitespace();
		}
	}


	/**
	 * Scans a txt-HTML-element.<p>
	 *
	 * <b>Preconditions:</b><br/>
	 * - The first &lt; has already been read.<br/>
	 *
	 * @modified 2004.11.17
	 * @since 2004.07.02 (v00.02.00)
	 * @author HoKoNoUmo
	 */
	private void scanElement() 	{
		StringBuffer buf = new StringBuffer();
		String nameElement = scanIdentifier();
		Hashtable<String,String> pairAttr = new Hashtable<String,String>();
		String sTxtCntnt="";
		sParent = stackParent.peek();

		//ppp, nnn
		//Elem >>>before<<< TEXT-CONTENT, we send to content for processing
		//Elem >>>inside<<< TEXT-CONTENT (br|b|..) handled when we scan text-content.

		//		if (nameElement.equals("strong")){
//			String sTest= scanNumberOfChar(20);
//			sTxtCntnt= scanElementNonEmptyAndReturnContent("strong");
//System.out.println("...strong: "+sTxtCntnt);
//			processElementContentText(nameElement,sTxtCntnt,null,sParent);
//			return;//Begin another scanElement.
//		}

		if (nameElement.equals("a")){
			//the a-elements AFTER h1,h2, p,
			sTxtCntnt= scanElementAnchorAndReturn();

			if (sTxtCntnt.indexOf("class=\"hide\"") !=-1) {
				//we reach the hide-element, WHEN "text-content" are elements a,b,i, ...
				if (!sContentPrevious.equals(""))
					processElementContentText(sParent,"",null,null);
				else
					processElementContentText(nameElement,sTxtCntnt,null,sParent);
			}
			else {
				processElementContentText(nameElement,sTxtCntnt,null,sParent);
			}
			return;//Begin another scanElement.
		}
		else if (nameElement.equals("b")){
			sTxtCntnt= scanElementNonEmptyAndReturnContent("b");
			processElementContentText(nameElement,sTxtCntnt,null,sParent);
			return;//Begin another scanElement.
		}
		else if (nameElement.equals("i")){
			sTxtCntnt= scanElementNonEmptyAndReturnContent("i");
			processElementContentText(nameElement,sTxtCntnt,null,sParent);
			return;//Begin another scanElement.
		}

		//elements to SKIP
		if (nameElement.equals("ol")){
			skipElementList("ol");
			return;
		}
		if (nameElement.equals("ul")){
			skipElementList("ul");
			return;
		}
		if (nameElement.equals("dl")){
			skipElementList("dl");
			return;
		}
		if (nameElement.equals("script")){
			skipElement("script");
			return;
		}
		if (nameElement.equals("style")){
			skipElement("style");
			return;
		}
		if (nameElement.equals("table")){
			skipElement("table");
			return;
		}

		ch = scanWhitespace();
		while ((ch != '>') && (ch != '/')) {//found attributes
			scanStringBack(String.valueOf(ch));
			String key = scanIdentifier();
			ch = scanWhitespace();
			if (ch != '=') {
				throw throwExpectedInput(nameElement+ " (start-tag)","=");
			}
			scanStringBack(String.valueOf(scanWhitespace()));
			pairAttr.put(key, scanDelimitedString());
			ch = scanWhitespace();
		}

		//done with attributes, then process the start of element.
		try {
			String p = stackParent.pop();//remove parent
			sParent2 = stackParent.pop();//remove parent2
			sParent3 = stackParent.peek();//read parent3
			stackParent.push(sParent2);
			stackParent.push(p);
		} catch (EmptyStackException ese) {
			System.out.println("phtml: empty-stack >> "+ese.toString());
		}
		processElementStart(nameElement, pairAttr, sParent,
												sParent2, sParent3);

		//found / or >, empty-element OR subelements OR text-content OR comments
		if (ch == '/') { //empty-element
			ch = scanChar();
			if (ch != '>') {
				throw throwExpectedInput("end-of-empty-element", ">");
			}
			processElementEnd(nameElement, sParent);
		}

		else { //ch='>' FOUND 1)subelements 2)text-content 3)comments
			ch = scanWhitespace();//< or letter

			if (ch == '<') { //ch==< inside subelement or comment
				stackParent.push(nameElement);
				ch = scanChar();

				while (ch != '/') {
					if (ch == '!') {
						skipComment();
					} else {
						scanStringBack(String.valueOf(ch));
						scanElement();
					}
					ch = scanWhitespace();
					if (ch != '<' ){//found text-content
							scanStringBack(String.valueOf(ch));
							sTxtCntnt= scanContentText();
							processElementContentText(nameElement,sTxtCntnt,pairAttr,sParent);
							//inside: end-tag of element
							ch = scanWhitespace(); //THE < after content
					}
					ch = scanChar();
				}

				//inside end-tag
				stackParent.pop();
				scanStringBack(String.valueOf(scanWhitespace()));
				if (! scanLiteral(nameElement)) {
					throw throwExpectedInput("non-empty", nameElement);
				}
				scanWhitespace();//the >
				processElementEnd(nameElement, sParent);
			}

			else { //found text-content
				scanStringBack(String.valueOf(ch));
				sTxtCntnt= scanContentText();
				processElementContentText(nameElement,sTxtCntnt,pairAttr,sParent);

				//inside: end-tag of element
				ch = scanWhitespace(); //THE < after content
				ch = scanWhitespace(); //THE /
				if (! scanLiteral(nameElement)) {
						throw throwExpectedInput(nameElement, "its end-tag (" +nameElement+")");
				}
				ch = scanWhitespace(); //THE >
				processElementEnd(nameElement, sParent);
			}
		}
	}


	/**
	 * Scans a unique-name from the current reader.<p>
	 *
	 * <b>Preconditions:</b><br/>
	 * - <code>result != null</code><br/>
	 * - The next character read from the reader is a valid first
	 *	 character of an XML unique-name.<p>
	 *
	 * <b>Postconditions:</b><br/>
	 * - The next character read from the reader won't be an unique-name
	 *	 character.
	 *
	 * @return The unique-name as a java-string.
	 * @author HoKoNoUmo
	 */
	private String scanIdentifier() {
		StringBuffer result = new StringBuffer();
		for (;;) {
			ch = scanChar();
			//stops when finds not a "character".
			if (		(ch < '-') || (ch > 'z') ||
						//<45 > 122
						// 48-57: 0-9
						//<65-90: A-Z
						//<97-122: a-z
						(ch == '/')  || //47
						(ch == '<')  || //60
						(ch == '=')  || //61
						(ch == '>')  || //62
						(ch == '?')  || //63
						(ch == '@')  || //64
						(ch == '\\') || //92
						(ch == ']')  || //93
						(ch == '^')  || //94
						(ch == '`')  //96
					)
			{
				scanStringBack(String.valueOf(ch));
				return result.toString();
			}
			result.append(ch);
		}
	}


	/**
	 * Scans the data for literal text.<br/>
	 * Scanning stops when a character does not match or after
	 * the complete text has been checked, whichever comes first.<p>
	 *
	 * <b>Preconditions:</b><br/>
	 * - <code>literal != null</code>
	 *
	 * @param literal The literal to check.
	 */
	private boolean scanLiteral(String literal) {
			int length = literal.length();
			for (int i = 0; i < length; i += 1) {
					if (scanChar() != literal.charAt(i)) {
							return false;
					}
			}
			return true;
	}


	/**
	 * Scans a non-empty element and returns the whole-element.<p>
	 *
	 * <b>Precodition:</b><br/>
	 * - The "&lt;name" has already been read.
	 *
	 * @param strEl
	 * 		The name of the element
	 * @modified 2010.05.23
	 * @since 2010.05.23 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private String scanElementNonEmptyAndReturn(String strEl) {
		StringBuffer data = new StringBuffer();
		data.append("<"+strEl);//what we have already read

		while (!data.toString().endsWith("/"+strEl+">")) {
			ch = scanChar();
			data.append(ch);
		}
		return data.toString();
	}


	/**
	 * Scans a non-empty element and returns its content whatever it be.<p>
	 *
	 * <b>Precodition:</b><br/>
	 * - The "&lt;name" has already been read.
	 *
	 * @modified 2010.05.23
	 * @since 2010.05.23 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private String scanElementNonEmptyAndReturnContent(String strEl) {
		StringBuffer data = new StringBuffer();

		ch= scanChar();
		while (ch != '>'){//scan attributes
			ch= scanChar();
		}

		while (!data.toString().endsWith("</"+strEl+">")) {
			ch = scanChar();
			data.append(ch);
		}

		String dt= data.toString();
		return dt.substring(0,dt.indexOf("</"+strEl+">"));
	}


	/**
	 * Scans from reader an ANCHOR element
	 * and returns the whole-element as String.<p>
	 *
	 * <b>Precodition:</b><br/>
	 * - The "&lt;a" has already been read.
	 *
	 * @modified 2010.06.03
	 * @since 2008.10.16 (v00.02.00)
	 * @author HokoYono
	 */
	private String scanElementAnchorAndReturn() {
		StringBuffer result = new StringBuffer();
		result.append("<a ");// we have read it.

		ch= scanChar();
		String str;
		boolean bLT= false;// > found LessThan

		//because <img ../> can be inside a-element
		while (true){
			str = result.toString();
			if ((str.endsWith("/>") && !bLT)
				|| (str.endsWith("</a>") && bLT)){ //empty or not
				break;
			} else {
				ch= scanChar();
				if (ch=='<')
					bLT= true;
				result.append(ch);
			}
		}
//System.out.println("a: "+result.toString());
		return result.toString();
	}

	/**
	 * This method scans a delimited string from the current reader.
	 * The scanned string without delimiters is returned.
	 */
	private String scanDelimitedString() {
			StringBuffer sb = new StringBuffer();
			char delimiter = scanChar();
			if (delimiter != '"') {
					throw throwExpectedInput("DelimitedString" ,"\"");
			}
			for (;;) {
					ch = scanChar();
					if (ch == delimiter) {
							return sb.toString();
					} else {
							sb.append(ch);
					}
			}
	}


	/**
	 *
	 * @modified 2010.08.16
	 * @since 2010.08.16 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private String scanNumberOfChar(int niCh) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < niCh; i++) {
			ch= scanChar();
			sb.append(ch);
		}
		return sb.toString();
	}


	/**
	 * Pushes a string back to the read-back buffer.<p>
	 *
	 * <b>Preconditions:</b><br/>
	 * - The read-back buffer is empty.<br/>
	 * - <code>ch != '\u0000'</code>
	 *
	 * @param str
	 *	 		The character-string to push back.
	 * @modified 2010.05.14
	 * @since 2010.05.14 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void scanStringBack(String str)
	{
			sReadTooMuch = str;
	}


	/**
	 * This method scans whitespace from the current reader.
	 *
	 * @return The next character following the whitespace.
	 */
	private char scanWhitespace() {
		for (;;) {
			ch = scanChar();
			switch (ch) {
					case ' ':
					case '\t':
					case '\n':
					case '\r':
							break;
					default:
							return ch;
			}
		}
	}


	/**
	 * This method scans whitespace from the current reader.
	 * The scanned whitespace is appended to <code>result</code>.<p>
	 *
	 * <b>Preconditions:</b><br/>
	 * - <code>result != null</code>
	 *
	 * @return The next character following the whitespace.
	 */
	private char scanWhitespace(StringBuffer result) {
		for (;;) {
			ch = scanChar();
			switch (ch) {
				case ' ':
				case '\t':
				case '\n':
					result.append(ch);
					break;
				case '\r':
					break;
				default:
					return ch;
			}
		}
	}


	/**
	 * Skips a comment: &lt;!-- data --&gt;<p>
	 *
	 * <b>Preconditions:</b><br/>
	 * - The first "&lt;!-" has already been read, before DOCTYPE
	 * - The first "&lt;!" has already been read, in other places.
	 */
	/**
	 *
	 * @modified 2010.05.29
	 * @since 2010.05.12 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	 private void skipComment() {

		scanChar();
		scanChar();
//		if (scanChar() != '-') {
//				throw throwExpectedInput("Comment" ,"-, in <!--");
//		}
		int dashesToRead = 2;
		//stops after 2 dashes reeded.
		while (dashesToRead > 0) {
				ch = scanChar();
				if (ch == '-') {
						dashesToRead -= 1;
				} else {
						dashesToRead = 2;
				}
		}
		if (scanChar() != '>') {
				throw throwExpectedInput("Comment" ,">");
		}
	}


	/**
	 * Skips any element of the form:<br/>
	 * &lt;element ...&gt;<br/>
	 * ... content<br/>
	 * &lt;/element&gt;<br/>
	 *
	 * @modified 2010.05.22
	 * @since 2010.05.22 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void skipElement(String strEl) {
		StringBuffer data = new StringBuffer();
		ch = scanChar();

		while (!data.toString().endsWith("/"+strEl+">")) {
			ch = scanChar();
			data.append(ch);
		}
	}


	/**
	 * PRECODITIONS:<br/>
	 *		- The first "&lt;!D" has already been read.<br/>
	 *
	 * @modified 2010.05.13
	 * @since 2010.05.13 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void skipElementDOCTYPE() {
//		<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
//		"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

		ch = scanChar();
		while (ch!='>') {
				ch = scanChar();
		}
//		if (scanChar() != '>') {
//				throw throwExpectedInput("DOCTYPE", ">");
//		}
	}


	/**
	 * Skip lists ol|ul which include other lists.<p>
	 *
	 * PRECODITIONS:<br/>
	 *	- The first "&lt;ol|ul" has already been read.<br/>
	 *
	 * @modified 2010.05.24
	 * @since 2010.05.15 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void skipElementList(String strNameLst) {
//System.out.println("..skiplist: "+strNameLst);
		StringBuffer data = new StringBuffer();
		ch = scanChar();
		int intCount=1;

		while (intCount!=0){

			while (!data.toString().endsWith("/"+strNameLst+">")) {
				ch = scanChar();
				data.append(ch);
				if (data.toString().endsWith("<"+strNameLst)){//<ul attributes
					intCount++;
//System.out.println(".+NbOfLists: "+intCount);
				}
			}

			ch = scanChar();
			data.append(ch);
			intCount--;
//System.out.println(".-NbOfLists: "+intCount);
		}
//		scanStringBack(String.valueOf(ch));
//System.out.println(data.toString());
	}


	/**
	 * Skips the xml-declaration &lt;?xml version... ?&gt;.<p>
	 *
	 * <b>Preconditions:</b><br/>
	 *	- The first &lt;? has already been read.<br/>
	 */
	private void skipElementXmlDeclaration() {
		ch = scanChar();
		while (ch!='?') {
				ch = scanChar();
		}
		if (scanChar() != '>') {
				throw throwExpectedInput("XmlDeclaration", ">");
		}
	}


	/**
	 * Creates a parse exception for when the next character read is not
	 * the character that was expected.<p>
	 *
	 * <b>Preconditions:</b><br/>
	 * - <code>charSet != null</code><br/>
	 * - <code>charSet.length() &gt; 0</code>
	 *
	 * @param charSet
	 *	The set of characters (in human readable form) that was expected.
	 */
	private HtmlParserException throwExpectedInput(String strElemName,
																									String charSet)
	{
			String msg = "Expected: " + charSet;
			return new HtmlParserException(strElemName, niLineHtmlFile,
			sUrlFile, msg);
	}


}
