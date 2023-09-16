/*
 * HtmlHistoryBackForward.java - Model for an URL History
 * Copyright (C) 1999-2001 Dirk Moebius (dmoebius@gmx.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package pkHtml;

import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

/**
 * this class maintains a list of visitid URLs and remembers
 * the current-entry, that is being viewed.
 */
public class HtmlHistoryBackForward
{

	private Stack<String> backStack = new Stack<String>();
	private Stack<String> forwardStack = new Stack<String>();

	public HtmlHistoryBackForward()
	{
	}

	/**
	 * add a new entry to the history. The new entry is made the current
	 * entry of the history.
	 */
	public synchronized void add(String e)
	{
		if (e == null)
			return;
		if (getCurrent()!=null && getCurrent().equals(e))
			return;
		backStack.push(e);
		forwardStack.clear();
	}

	/** returns the current URL of the history, as String. */
	public String getCurrent()
	{
		if (backStack.isEmpty()) return null;
		return backStack.lastElement();
	}


	public int getHistoryPos() {
		return backStack.size();
	}


	/**
	 * sets the internal state of the history to the next entry and returns
	 * its URL.
	 *
	 * @return the next URL as String, or null if the end of the history is
	 *         reached.
	 */
	public synchronized String getNext(String current)
	{
		if (forwardStack.isEmpty()) return null;
		String element = forwardStack.pop();
		backStack.push(current);
		return element;
	}

	/** return true, if there is a next entry in the history. */
	public boolean hasNext()
	{
		return !forwardStack.isEmpty();
	}

	/**
	 * sets the internal state of the history to the previous entry and
	 * returns its URL.
	 *
	 * @return the previous URL as String, or null if the beginning of the
	 *         history is reached.
	 */
	public synchronized String getPrevious(String current)
	{
		String element = backStack.pop();
		forwardStack.push(current);
		return element;
	}

	/** return true, if there is a previous entry in the history. */
	public boolean hasPrevious()
	{
		return !backStack.isEmpty();
	}


	/**
	 * get the last entries from the history, but now more than specified in
	 * the property 'htmlmgr.max_go_menu'. The entries are such that the
	 * current entry is among them.
	 */
	public String[] getGoMenuEntries()
	{
		int max = getMaxVisibleMenuEntries();
		int count = backStack.size();
		if (count > max) count = max;
		String[] entries = new String[count];
		Iterator itr = backStack.iterator();
		int i=0;
		while (itr.hasNext() && (i < count)) {
			String ent = (String) itr.next();
			entries[i++]=ent;
		}
		return entries;
	}

	private int getMaxVisibleMenuEntries()
	{
		String history = HtmlMgr.getProperty("hm.history");
		int max;
		try {
			max = Integer.parseInt(history);
			if (max < 1)
				throw new NumberFormatException();
		}
		catch (NumberFormatException e) {
			max = 20;
		}
		return max;
	}

}
