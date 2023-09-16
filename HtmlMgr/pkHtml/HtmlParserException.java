/*
 * HtmlParserException.java -
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

/**
 * An HtmlParserException is thrown when an error occures while
 * parsing an HTML-file.
 *
 * @modified 2010.05.13
 * @since 2010.05.13 (v00.02.02)
 * @author HoKoNoUmo
 */
public class HtmlParserException
		extends RuntimeException
{
static final long serialVersionUID = 21L;

		/**
		 * Creates an exception.
		 *
		 * @param name
		 *		The name of the element where the error is located.
		 * @param message
		 *		A message describing what went wrong.
		 *
		 */
		public HtmlParserException(String name,String message){
				super("HTML Parse Exception during parsing of "
							+ name + " element"
							+ ": " + message);
		}


		/**
		 * Creates an exception.
		 *
		 * @param name		The name of the element where the error is located.
		 * @param lineNr	The number of the line in the input.
		 * @param message A message describing what went wrong.
		 *
		 */
		public HtmlParserException(String name,
														int		lineNr,
														String message)
		{
				super("HTML Parse Exception during parsing of "
							+ name + " element"
							+ " at line " + lineNr + ": " + message);
		}


		public HtmlParserException(String name,
														int		lineNr,
														String strFile,
														String message)
		{
				super("HTML Parse-Exception during parsing of "
							+ name + " element, of file "+strFile
							+ ", at line " + lineNr + ": " + message);
		}


}
