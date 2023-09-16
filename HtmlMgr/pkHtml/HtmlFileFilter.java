/*
 * HtmlFileFilter.java - Filters .html and .txt files.
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

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filters the file-names that end with ".html"a given name.
 *
 *
 * @modified 2010.04.25
 * @since 2010.04.25 (v00.02.02)
 * @author HoKoNoUmo
 */
 public class HtmlFileFilter implements FilenameFilter {


	public HtmlFileFilter (){

	}

	public boolean accept(File dir, String name) {
		name = name.toLowerCase();
		if (name.endsWith(".html")
				|| name.endsWith(".htm")
				|| name.endsWith(".txt")
				)
			return true;
		return false;
	}

}