/*
 * HtmlEditorKitBrowser.java -
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
import java.io.*;


/**
 * Does not read the xml-declaration of XHtml files.
 *
 * @modified 2010.06.30
 * @since 2010.06.29 (v00.02.03)
 * @author HoKoNoUmo
 */
public class HtmlEditorKitBrowser extends HTMLEditorKit
{

	static final long serialVersionUID = 21L;

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
