/*
 * HtmlUpdateSFI.java - Sets new Structure-FI in modified SFI-files.
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

import java.io.*;

/**
 * Updates the StructureFragmentIdentifiers of a file or string.<br/>
 * Now works for h1 to h6 headings.<br/>
 * PRECODITION: the file contains old SFIs.<br/>
 * IDEA: on hide-element to add and info-FIs.
 *
 * @modified 2010.08.03
 * @since 2010.07.29 (v00.02.03)
 * @author HoKoNoUmo
 */
 public class HtmlUpdateSFI
{

	/** Holds the output-data as a string. */
	public String sOut= "";
	private BufferedWriter wrBuffered;
	/** We write the same data in 2 writers. */
	private StringWriter wrString;

	private int 						niCntrH1=0; //counter for heading1
	private int 						niCntrH2=0;
	private int 						niCntrH3=0;
	private int 						niCntrH4=0;
	private int 						niCntrH5=0;
	private int 						niCntrH6=0;
	private int 						niCntrH7=0;
	private int 						niCntrH8=0;
	private int 						niCntrH9=0;
	private int 						niCntrP=0;

	private String					sH1="";
	private String					sH2="";
	private String					sH3="";
	private String					sH4="";
	private String					sH5="";
	private String					sH6="";
	private String					sH7="";
	private String					sH8="";
	private String					sH9="";
	private String					sParentHeading="h0";
	private String					sLastSFI="h0"; //the last LocationSFI eg h0.1p1
	private String					sLinePrev="";


	/**
	 *
	 * @modified 2010.07.29
	 * @since 2010.07.29 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public static void main(String args[])
	{
		if (args.length!=1) {
			System.out.println("USAGE:");
			System.out.println("java pkHtml.HtmlUpdateSFI <g:/file1/.../aaj.html>");
		}
		else {
			if (args[0].startsWith("file"))
				new HtmlUpdateSFI(args[0], true);
			else
				new HtmlUpdateSFI("file:" +args[0], true);
		}
	}


	/**
	 * Constructor.
	 *
	 * @param bFile	denotes if the first parameter is a string of
	 * 		file-url or a string of html-data.
	 * @param sIn
	 * 		The url(file:g:/...) of the html-file OR the html-data.
	 * @modified 2010.08.12
	 * @since 2010.07.29 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public HtmlUpdateSFI(String sIn, boolean bFile)
	{
		String sUrlFile= HtmlMgr.sDirHome+"tmp-newsfi-"+HtmlUtilities.setCurrentDate();
		if (bFile) {
			sUrlFile= sIn;
			sUrlFile= sUrlFile.substring(5);
		}

		BufferedReader rdIn;
		try {
			if (bFile) {
				FileInputStream fis = new FileInputStream(sUrlFile);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
				rdIn= new BufferedReader(isr);
			} else
				rdIn= new BufferedReader(new StringReader(sIn));


			FileOutputStream fos= new FileOutputStream(sUrlFile+".html");
			OutputStreamWriter osw= new OutputStreamWriter(fos, "UTF8");
			wrBuffered= new BufferedWriter(osw);
			wrString= new StringWriter();

			String ln= null;
			while ((ln= rdIn.readLine()) != null)
			{
				if (ln.startsWith("<h1>")){
					sParentHeading="h0";
					writeLine(ln);
					ln= rdIn.readLine(); //SFI
					if (ln.indexOf("<a name=\"h0.toc\"")!=-1){
						sLastSFI= "h0.toc";
						writeLine(ln);
					} else if (ln.indexOf("Table-of-Contents")!=-1) {
						sLastSFI= "h0.toc";
						writeSFI(sLastSFI);
						writeLine(ln);
					} else {
						niCntrH1++;
						sLastSFI= "h0."+niCntrH1;
						writeSFI(sLastSFI);
						niCntrH2=niCntrH3=niCntrH4=niCntrH5=niCntrH6
							=niCntrH7=niCntrH8=niCntrH9=niCntrP=0;
					}
				}
				else if (ln.startsWith("<h2>")){
					if (niCntrH5==0){
						String sGap= "";
						sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, 1);
						if (sParentHeading.equals(""))
								sGap= sGap +".0";
						sParentHeading= sParentHeading +sGap;
					}
					else {
						sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, 1);
					}
					writeLine(ln);
					ln= rdIn.readLine(); //SFI
					niCntrH2++;
					sLastSFI= sParentHeading +"." +niCntrH2;
					writeSFI(sLastSFI);
					if (ln.indexOf("<a name=\"h0")==-1)// not an SFI
						writeLine(ln);
					niCntrH3=niCntrH4=niCntrH5=niCntrH6=niCntrH7=niCntrH8=niCntrH9=niCntrP=0;
				}
				else if (ln.startsWith("<h3>")){
					if (niCntrH3==0){
						String sGap= "";
						for (int i=2; i>0; i--){
							sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, i);
							if (sParentHeading.equals(""))
								sGap= sGap +".0";
							else
								break;
						}
						sParentHeading= sParentHeading +sGap;
					}
					else {
						sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, 2);
					}
					writeLine(ln);
					ln= rdIn.readLine(); //SFI
					niCntrH3++;
					sLastSFI= sParentHeading +"." +niCntrH3;
					writeSFI(sLastSFI);
					if (ln.indexOf("<a name=\"h0")==-1)// not an SFI
						writeLine(ln);
					niCntrH4=niCntrH5=niCntrH6=niCntrH7=niCntrH8=niCntrH9=niCntrP=0;
				}

				else if (ln.startsWith("<h4>")){
					if (niCntrH4==0){
						String sGap= "";
						for (int i=3; i>0; i--){
							sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, i);
							if (sParentHeading.equals(""))
								sGap= sGap +".0";
							else
								break;
						}
						sParentHeading= sParentHeading +sGap;
					}
					else {
						sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, 3);
					}
					writeLine(ln);
					ln= rdIn.readLine(); //SFI
					niCntrH4++;
					sLastSFI= sParentHeading +"." +niCntrH4;
					writeSFI(sLastSFI);
					if (ln.indexOf("<a name=\"h0")==-1)// not an SFI
						writeLine(ln);
					niCntrH5=niCntrH6=niCntrH7=niCntrH8=niCntrH9=niCntrP=0;
				}

				else if (ln.startsWith("<h5>")){
					if (niCntrH5==0){
						String sGap= "";
						for (int i=4; i>0; i--){
							sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, i);
							if (sParentHeading.equals(""))
								sGap= sGap +".0";
							else
								break;
						}
						sParentHeading= sParentHeading +sGap;
					}
					else {
						sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, 4);
					}
					writeLine(ln);
					ln= rdIn.readLine(); //SFI
					niCntrH5++;
					sLastSFI= sParentHeading +"." +niCntrH5;
					writeSFI(sLastSFI);
					if (ln.indexOf("<a name=\"h0")==-1)// not an SFI
						writeLine(ln);
					niCntrH6=niCntrH7=niCntrH8=niCntrH9=niCntrP=0;
				}

				else if (ln.startsWith("<h6>")){
					if (niCntrH6==0){
						String sGap= "";
						for (int i=5; i>0; i--){
							sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, i);
							if (sParentHeading.equals(""))
								sGap= sGap +".0";
							else
								break;
						}
						sParentHeading= sParentHeading +sGap;
					}
					else {
						sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, 5);
					}
/*
					if (niCntrH6==0){
						if (sLastSFI.indexOf("p")!=-1)
							sParentHeading= sLastSFI.substring(0, sLastSFI.indexOf("p"));
						else
							sParentHeading= sLastSFI;
					} else {
						int ni1,ni2,ni3,ni4,ni5;
						ni1=ni2=ni3=ni4=ni5= -1;
						if (sLastSFI.indexOf(".")!=-1)
							ni1= sLastSFI.indexOf(".")+1;
						if (ni1!=-1 && sLastSFI.indexOf(".",ni1)!=-1)
							ni2= sLastSFI.indexOf(".",ni1)+1;
						if (ni2!=-1 && sLastSFI.indexOf(".",ni2)!=-1)
							ni3= sLastSFI.indexOf(".",ni2)+1;
						if (ni3!=-1 && sLastSFI.indexOf(".",ni3)!=-1)
							ni4= sLastSFI.indexOf(".",ni3)+1;
						if (ni4!=-1 && sLastSFI.indexOf(".",ni4)!=-1)
							ni5= sLastSFI.indexOf(".",ni4)+1;
						if (ni5!=-1 && sLastSFI.indexOf(".",ni5)!=-1)
							sParentHeading= sLastSFI.substring(0, sLastSFI.indexOf(".", ni5));
						else {
							if (ni4!=-1)
								sParentHeading= sLastSFI.substring(0,sLastSFI.indexOf(".", ni4));
							else if (ni3!=-1)
								sParentHeading= sLastSFI.substring(0,sLastSFI.indexOf(".", ni3));
							else if (ni2!=-1)
								sParentHeading= sLastSFI.substring(0,sLastSFI.indexOf(".", ni2));
						}
					}
*/
					writeLine(ln);
					ln= rdIn.readLine(); //SFI
					niCntrH6++;
					sLastSFI= sParentHeading +"." +niCntrH6;
					writeSFI(sLastSFI);
					if (ln.indexOf("<a name=\"h0")==-1)// not an SFI
						writeLine(ln);
					niCntrH7=niCntrH8=niCntrH9=niCntrP=0;
				}

				else if (ln.startsWith("<p class=\"h7\"")) {
					if (niCntrH7==0){
						String sGap= "";
						for (int i=6; i>0; i--){
							sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, i);
							if (sParentHeading.equals(""))
								sGap= sGap +".0";
							else
								break;
						}
						sParentHeading= sParentHeading +sGap;
					}
					else {
						sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, 6);
					}
					writeLine(ln);
					ln= rdIn.readLine(); //SFI
					niCntrH7++;
					sLastSFI= sParentHeading +"." +niCntrH7;
					writeSFI(sLastSFI);
					if (ln.indexOf("<a name=\"h0")==-1)// not an SFI
						writeLine(ln);
					niCntrH8=niCntrH9=niCntrP=0;
				}

				else if (ln.startsWith("<p class=\"h8\"")) {
					if (niCntrH8==0){
						String sGap= "";
						for (int i=7; i>0; i--){
							sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, i);
							if (sParentHeading.equals(""))
								sGap= sGap +".0";
							else
								break;
						}
						sParentHeading= sParentHeading +sGap;
					}
					else {
						sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, 7);
					}
					writeLine(ln);
					ln= rdIn.readLine(); //SFI
					niCntrH8++;
					sLastSFI= sParentHeading +"." +niCntrH8;
					writeSFI(sLastSFI);
					if (ln.indexOf("<a name=\"h0")==-1)// not an SFI
						writeLine(ln);
					niCntrH9=niCntrP=0;
				}

				else if (ln.startsWith("<p class=\"h9\"")) {
					if (niCntrH9==0){
						String sGap= "";
						for (int i=8; i>0; i--){
							sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, i);
							if (sParentHeading.equals(""))
								sGap= sGap +".0";
							else
								break;
						}
						sParentHeading= sParentHeading +sGap;
					}
					else {
						sParentHeading= HtmlUtilities.getSfiPartLevel(sLastSFI, 8);
					}
					writeLine(ln);
					ln= rdIn.readLine(); //SFI
					niCntrH9++;
					sLastSFI= sParentHeading +"." +niCntrH9;
					writeSFI(sLastSFI);
					if (ln.indexOf("<a name=\"h0")==-1)// not an SFI
						writeLine(ln);
					niCntrP=0;
				}

				else if (ln.startsWith("<p>")
							|| (ln.startsWith("<p class=\"margin-25\""))
							|| (ln.startsWith("<p class=\"center\"")) ){
					if (niCntrP==0)
						sParentHeading= sLastSFI;
					writeLine(ln);
					ln= rdIn.readLine(); //SFI
					niCntrP++;
					sLastSFI= sParentHeading +"p" +niCntrP;
					writeSFI(sLastSFI);
					if (ln.indexOf("<a name=\"h0")==-1)// not an SFI
						writeLine(ln);
				}

				else if (ln.indexOf("<a class=\"hide\"")!=-1){
					writeHideElement(sLastSFI);
				}

				//missing hide-element
				else if (ln.startsWith("</p>")
							|| ln.startsWith("</h1>")
							|| ln.startsWith("</h2>")
							|| ln.startsWith("</h3>")
							|| ln.startsWith("</h4>")
							|| ln.startsWith("</h5>")
							|| ln.startsWith("</h6>") ){
					if (sLinePrev.indexOf("<a class=\"hide\"")==-1){
						writeHideElement(sLastSFI);
						writeLine(ln);
					} else
						writeLine(ln);
				}

				else {
					writeLine(ln);
				}
			}

			rdIn.close();
			wrBuffered.close();
			wrString.close();

			sOut= wrString.toString();
		} catch (IOException ioe) {
			System.out.println(">>HtmlUpdateSFI.Constructor: "+ioe.toString());
		}

		//rename files
//		File f1= new File(sFileIn);
//		f1.renameTo(new File(sFileIn+"2"));
//
//		File f2= new File(sFileOut);
//		f2.renameTo(new File(sFileIn));
	}


	/**
	 *
	 * @modified 2010.08.25
	 * @since 2010.07.29 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private void writeHideElement(String sSFI){
		if (sSFI.indexOf("p")==-1 && !sSFI.equals("h0")) {
			int niP= HtmlUtilities.findHeadingLevel(sSFI);
			sSFI= sSFI + " h"+niP;
		}
		sLinePrev= "  <a class=\"hide\">#" +sSFI +"#</a>";
		try {
			wrBuffered.write(sLinePrev);
			wrBuffered.newLine();
			wrString.write(sLinePrev+"\n");
		} catch (IOException e) {
			System.out.println("ex-HtmlUpdateSFI.writeHideElement:: "+e.toString());
		}
	}


	/**
	 *
	 * @modified 2010.07.29
	 * @since 2010.07.29 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private void writeLine(String sLn){
		sLinePrev= sLn;
		try {
			wrBuffered.write(sLinePrev);
			wrBuffered.newLine();
			wrString.write(sLinePrev+"\n");
		} catch (IOException e) {
			System.out.println("ex-HtmlUpdateSFI.writeLine:: "+e.toString());
		}
	}


	/**
	 *
	 * @modified 2010.07.29
	 * @since 2010.07.29 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private void writeSFI(String sSFI){
		sLinePrev= "  <a name=\"" +sSFI +"\"></a>";
		try {
			wrBuffered.write(sLinePrev);
			wrBuffered.newLine();
			wrString.write(sLinePrev+"\n");
		} catch (IOException e) {
			System.out.println("ex-HtmlUpdateSFI.writeSFI:: "+e.toString());
		}
	}

}