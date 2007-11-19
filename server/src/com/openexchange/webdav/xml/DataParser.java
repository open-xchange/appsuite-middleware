/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */



package com.openexchange.webdav.xml;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.openexchange.groupware.container.DataObject;
import com.openexchange.session.Session;
import com.openexchange.webdav.xml.fields.DataFields;

/**
 * DataParser
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class DataParser {
	
	public static final int SAVE = 1;
	
	public static final int DELETE = 2;
	
	public static final int CONFIRM = 3;

	protected Session sessionObj;
	
	protected String client_id;
	
	protected int method = SAVE;
	
	protected int inFolder;
	
	private static final Log LOG = LogFactory.getLog(DataParser.class);
	
	protected void parseElement(final DataObject dataobject, final XmlPullParser parser) throws Exception {
		if (isTag(parser, DataFields.OBJECT_ID, XmlServlet.NAMESPACE)) {
			dataobject.setObjectID(getValueAsInt(parser));
		} else if (isTag(parser, DataFields.LAST_MODIFIED, XmlServlet.NAMESPACE)) {
			dataobject.setLastModified(getValueAsDate(parser));
		} else if (isTag(parser, "client_id", XmlServlet.NAMESPACE)) {
			client_id = getValue(parser);
		} else if (isTag(parser, "method", XmlServlet.NAMESPACE)) {
			final String s = getValue(parser);
			if (s != null) {
				if (s.equalsIgnoreCase("save")) {
					method = SAVE;
				} else if (s.equalsIgnoreCase("delete")) {
					method = DELETE;
				} else if (s.equalsIgnoreCase("confirm")) {
					method = CONFIRM;
				}
			} 
		} else {
			if (LOG.isTraceEnabled()) {
				LOG.trace("unknown xml tag: " + parser.getName());
			}
			getValue(parser);
		}
	}
	
	protected boolean hasCorrectNamespace(final XmlPullParser parser) throws Exception {
		if (parser.getEventType() == XmlPullParser.START_TAG && parser.getNamespace().equals(XmlServlet.NAMESPACE)) {
			//if (parser.getNamespace().equals(XmlServlet.NAMESPACE)) {
				return true;
			//}
		}
		return false;
	}
		
	public boolean isTag(final XmlPullParser parser, final String name) throws XmlPullParserException {
		return parser.getEventType() == XmlPullParser.START_TAG	&& (name == null || name.equals(parser.getName()));
	}
	
	public boolean isTag(final XmlPullParser parser, final String name, final String namespace) throws XmlPullParserException {
		return parser.getEventType() == XmlPullParser.START_TAG	&& (name == null || name.equals(parser.getName()));
	}
	
	public String getClientID() {
		return client_id;
	}
	
	public int getMethod() {
		return method;
	}

	public int getFolder() {
		return inFolder;
	}
	
	public int getValueAsInt(final XmlPullParser parser) throws XmlPullParserException, IOException {
		String s = null;
		
		if ((s = getValue(parser)) != null && s.length() > 0) {
			return Integer.parseInt(s);
		}
		return 0;
	}
	
	public float getValueAsFloat(final XmlPullParser parser) throws XmlPullParserException, IOException {
		String s = null;
		
		if ((s = getValue(parser)) != null && s.length() > 0) {
			return Float.parseFloat(s);
		}
		return 0;
	}
	
	public long getValueAsLong(final XmlPullParser parser) throws XmlPullParserException, IOException {
		String s = null;
		
		if ((s = getValue(parser)) != null && s.length() > 0) {
			return Long.parseLong(s);
		}
		return 0;
	}
	
	public Date getValueAsDate(final XmlPullParser parser) throws XmlPullParserException, IOException {
		String s = null;
		
		if ((s = getValue(parser)) != null && s.length() > 0) {
			return new Date(Long.parseLong(s));
		}
		return null;
	}
	
	public boolean getValueAsBoolean(final XmlPullParser parser) throws XmlPullParserException, IOException {
		String s = null;
		
		if ((s = getValue(parser)) != null && s.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}
	
	public byte[] getValueAsByteArray(final XmlPullParser parser) throws XmlPullParserException, IOException {
		final String s = parser.nextText();
		
		if (s != null && s.length() == 0) {
			return null;
		} 
		return s.getBytes();
	}
	
	public String getValue(final XmlPullParser parser) throws XmlPullParserException, IOException {
		final String s = parser.nextText();
		
		if (s != null && s.length() == 0) {
			return null;
		} 
		return s;
	}
	
	public static boolean isEnd(final XmlPullParser parser) throws XmlPullParserException {
		return (parser.getEventType() == XmlPullParser.END_DOCUMENT);
	}
}




