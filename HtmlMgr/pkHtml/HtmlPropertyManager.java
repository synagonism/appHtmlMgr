/*
 * HtmlPropertyManager.java - Manages property files
 * :tabSize=2:indentSize=2:noTabs=false:
 * :folding=none:collapseFolds=1:
 *
 * Copyright (C) 2004 Slava Pestov
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

import java.io.*;
import java.util.*;

class HtmlPropertyManager
{
	private Properties system = new Properties();
	private Properties user = new Properties();


	Properties getProperties()
	{
		Properties total = new Properties();
		total.putAll(system);
		total.putAll(user);
		return total;
	}


	void loadSystemProps(InputStream in)
		throws IOException
	{
		loadProps(system,in);
	}


	void loadUserProps(InputStream in)
		throws IOException
	{
		loadProps(user,in);
	}


	void saveUserProps(OutputStream out)
		throws IOException
	{
		user.store(out,"HtmlMgr properties");
	}


	String getProperty(String name)
	{
		String value = user.getProperty(name);
		if(value != null)
			return value;
		else
			return getDefaultProperty(name);
	}


	void setProperty(String name, String value)
	{
		String prop = getDefaultProperty(name);

		/* if value is null:
		 * - if default is null, unset user prop
		 * - else set user prop to ""
		 * else
		 * - if default equals value, ignore
		 * - if default doesn't equal value, set user
		 */
		if(value == null)
		{
			if(prop == null || prop.length() == 0)
				user.remove(name);
			else
				user.setProperty(name,"");
		}
		else {
			if(value.equals(prop))
				user.remove(name);
			else
				user.setProperty(name,value);
		}
	}


	public void setTemporaryProperty(String name, String value)
	{
		user.remove(name);
		system.setProperty(name,value);
	}


	void unsetProperty(String name)
	{
		if(getDefaultProperty(name) != null)
			user.setProperty(name,"");
		else
			user.remove(name);
	}


	public void resetProperty(String name)
	{
		user.remove(name);
	}


	private String getDefaultProperty(String name)
	{
		return system.getProperty(name);
	}


	private static void loadProps(Properties into, InputStream in)
		throws IOException
	{
		try
		{
			into.load(in);
		}
		finally
		{
			in.close();
		}
	}


}
