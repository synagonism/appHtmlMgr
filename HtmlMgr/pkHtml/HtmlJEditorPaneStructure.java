/*
 * HtmlJEditorPaneStructure.java - Shows the element-structure of a JEditorPane
 * :tabSize=2:indentSize=2:noTabs=false:
 * :folding=none:collapseFolds=1:
 *
 * Copyright (C) 2010 Stanislav Lapitsky (http://java-sl.com/JEditorPaneStructureTool.html)
 * Kaseluris-Nikos (HoKoNoUmo).
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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;

/**
 *
 * @modified 2010.08.01
 * @since 2010.08.01 (v00.02.03)
 * @author HoKoNoUmo
 */
public class HtmlJEditorPaneStructure extends JFrame {
	static final long serialVersionUID = 21L;

	JEditorPane jEdPane;

	public JTree trDocument=new JTree() {
		static final long serialVersionUID = 21L;
		public String getToolTipText(MouseEvent event) {
			return processDocumentTooltip(event);
		}
	};

	/**
	 *
	 * @modified 2010.08.01
	 * @since 2010.08.01 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public HtmlJEditorPaneStructure(JEditorPane jep) {
		super("JEditorPane structure of elements");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.jEdPane= jep;

		if (jEdPane!=null) {
			Document doc=jEdPane.getDocument();

			Element elem=doc.getDefaultRootElement();
			trDocument.setModel(new DefaultTreeModel((TreeNode)elem));
			trDocument.expandRow(2);//body-element
//			int row=0;
//			while(row < trDocument.getRowCount()) {
//				trDocument.expandRow(row);
//				row++;
//			}
			trDocument.setToolTipText(" ");
		}

		JScrollPane jScrPn=new JScrollPane(trDocument);
		getContentPane().add(jScrPn, BorderLayout.CENTER);
		setLocation(859,59);
		setSize(new Dimension(300,700));
		setVisible(true);
	}


	/**
	 *
	 * @modified 2010.08.01
	 * @since 2010.08.01 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	protected AttributeSet getAttributes(Element elem) {
		if (elem instanceof AbstractDocument.AbstractElement) {
			try {
				Field f=AbstractDocument.AbstractElement.class.getDeclaredField("attributes");
				f.setAccessible(true);
				AttributeSet res=(AttributeSet)f.get(elem);
				return res;
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 *
	 * @modified 2010.08.01
	 * @since 2010.08.02 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	protected String getText(Document doc, int startOffset, int endOffset) {
		try {
			String text=doc.getText(startOffset, endOffset-startOffset);
			text=text.replaceAll("\n", "\\\\n");
			text=text.replaceAll("\t", "\\\\t");
			text=text.replaceAll("\r", "\\\\r");

			return text;
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}

		return null;
	}

	/**
	 *
	 * @modified 2010.08.01
	 * @since 2010.08.02 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	protected String processDocumentTooltip(MouseEvent e) {
		int rn=trDocument.getRowForLocation(e.getX(), e.getY());
		if (trDocument.getPathForRow(rn)!=null) {
			Element tn=(Element)trDocument.getPathForRow(rn).getLastPathComponent();
			StringBuffer buff=new StringBuffer();
			buff.append("<html>");
			buff.append("<b>Start offset: </b>").append(tn.getStartOffset()).append("<br>");
			buff.append("<b>End offset: </b>").append(tn.getEndOffset()).append("<br>");
			buff.append("<b>Child count: </b>").append(tn.getElementCount()).append("<br>");
			buff.append("<b>Text: </b>\"").append(getText(tn.getDocument(), tn.getStartOffset(), tn.getEndOffset())).append("\"<br>");
			buff.append("<b>Attributes: </b>").append("<br>");
			Enumeration names=tn.getAttributes().getAttributeNames();
			while( names.hasMoreElements()) {
				Object name=names.nextElement();
				Object value=tn.getAttributes().getAttribute(name);
				buff.append("&nbsp;&nbsp;<b>").append(name).append(":</b>").append(value).append("<br>");
			}
			buff.append("</html>");
			return buff.toString();
		}

		return null;
	}

	/**
	 * Selects the row that contains the string with
	 * the start and end offsets.
	 *
	 * @modified 2010.08.02
	 * @since 2010.08.02 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public void selectRow(String sSufix) {
		int niRow=0;
		while(niRow < trDocument.getRowCount()) {
			TreePath tp= trDocument.getPathForRow(niRow);
			String sTp= tp.toString();
			if (sTp.indexOf(sSufix)!=-1){
				trDocument.setSelectionPath (tp);
				trDocument.scrollPathToVisible (tp);
				return;
			}
			niRow++;
		}
	}

}