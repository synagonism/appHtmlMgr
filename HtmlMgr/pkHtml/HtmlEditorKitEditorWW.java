/*
 * HtmlEditorKitEditorWW.java -
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

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;


/**
 * The code that makes hyperlinks to work in editable JEditorPane
 * that wrote Stanislav Lapitsky,
 * http://java-sl.com/tip_links_in_editable.html
 * has problem when the file contains pictures, and I removed.
 * [2010.08.04]
 *
 * @modified 2010.08.04
 * @since 2010.08.04 (v00.02.03)
 * @author HoKoNoUmo
 */
 public class HtmlEditorKitEditorWW extends HTMLEditorKit
{
	static final long serialVersionUID = 21L;

	/**
	 * Maps java-doc-elements with html-elements.
	 *
	 * @modified 2010.07.02
	 * @since 2010.07.02 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public void write(Writer out, HTMLDocument doc, int pos, int len)
	throws IOException, BadLocationException {
		HtmlWriterExtended hwe= new HtmlWriterExtended(out, doc);
		hwe.write();
	}


	/**
	 *
	 * @modified 2010.07.07
	 * @since 2010.07.07 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public void read(Reader in, Document doc, int pos)
	throws IOException, BadLocationException {
		BufferedReader br=new BufferedReader(in);
		String s=br.readLine();
		StringBuffer buff=new StringBuffer();
		while (s!=null) {
			if (	!s.startsWith("<?xml"))
				buff.append(s);
			s=br.readLine();
		}
		br.close();
		super.read(new InputStreamReader(
			new ByteArrayInputStream(buff.toString().getBytes())), doc, pos);
	}


}
