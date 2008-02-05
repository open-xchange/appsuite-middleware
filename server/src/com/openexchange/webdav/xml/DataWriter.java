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

import java.io.OutputStream;
import java.util.Date;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.webdav.xml.fields.DataFields;

/**
 * DataParser
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class DataWriter {
	
	public static final int ACTION_MODIFIED = 1;
	public static final int ACTION_DELETE = 2;
	public static final int ACTION_LIST = 3;
	
	private static final Namespace dav = Namespace.getNamespace("D", "DAV:");
	
	public static final Namespace namespace = Namespace.getNamespace(XmlServlet.PREFIX, XmlServlet.NAMESPACE);
	
	protected Session sessionObj = null;

	protected Context ctx = null;
	
	private User userObj = null;
	
	protected User getUser() {
		if (null == userObj) {
			userObj = UserStorage.getStorageUser(sessionObj.getUserId(), ctx);
		}
		return userObj;
	}
	
	protected void writeResponseElement(Element e_prop, int object_id, int status, String description, XMLOutputter xo, OutputStream os) throws Exception {
		Element e_response = new Element("response", dav);
		e_response.addNamespaceDeclaration(Namespace.getNamespace(XmlServlet.PREFIX, XmlServlet.NAMESPACE));
		e_response.addContent(new Element("href", dav).addContent(String.valueOf(object_id)));
		
		Element e_propstat = new Element("propstat", dav);
		e_response.addContent(e_propstat);
		
		e_propstat.addContent(e_prop);
		e_propstat.addContent(new Element("status", dav).addContent(String.valueOf(status)));
		e_propstat.addContent(new Element("responsedescription", dav).addContent(description));
		
		xo.output(e_response, os);
	}
	
	protected void writeDataElements(DataObject dataobject, Element e_prop) throws Exception {
		if (dataobject.containsCreatedBy()) {
			addElement(DataFields.CREATED_BY, dataobject.getCreatedBy(), e_prop);
		}

		if (dataobject.containsCreationDate()) {
			addElement(DataFields.CREATION_TIME, dataobject.getCreationDate(), e_prop);
		}
		
		if (dataobject.containsModifiedBy()) {
			addElement(DataFields.MODIFIED_BY, dataobject.getModifiedBy(), e_prop);
		}
		
		if (dataobject.containsLastModified()) {
			addElement(DataFields.LAST_MODIFIED, dataobject.getLastModified(), e_prop);
		}
		
		if (dataobject.containsObjectID()) {
			addElement(DataFields.OBJECT_ID, dataobject.getObjectID(), e_prop);
		}
	}
	
	public static void addElement(String name, String value, Element parent) throws Exception {
		if (value != null) {
			Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
			e.addContent(value);
			parent.addContent(e);
		}
	}
	
	public static void addElement(String name, Date value, Element parent) throws Exception {
		if (value != null) {
			Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
			e.addContent(String.valueOf(value.getTime()));
			parent.addContent(e);
		}
	}
	
	public static void addElement(String name, int value, Element parent) throws Exception {
		Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
		e.addContent(String.valueOf(value));
		parent.addContent(e);
	}
	
	public static void addElement(String name, float value, Element parent) throws Exception {
		Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
		e.addContent(String.valueOf(value));
		parent.addContent(e);
	}
	
	public static void addElement(String name, long value, Element parent) throws Exception {
		Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
		e.addContent(String.valueOf(value));
		parent.addContent(e);
	}
	
	public static void addElement(String name, boolean value, Element parent) throws Exception {
		Element e = new Element(name, XmlServlet.PREFIX, XmlServlet.NAMESPACE);
		if (value) {
			e.addContent("true");
		} else {
			e.addContent("false");
		}
		
		parent.addContent(e);
	}
}




