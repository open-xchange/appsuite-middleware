/*
 *
 *    OPEN-XCHANGE - "the communication and information enviroment"
 *
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all
 *    other brand and product names are or may be trademarks of, and are
 *    used to identify products or services of, their respective owners.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original code will still remain
 *    copyrighted by the copyright holder(s) or original author(s).
 *
 *
 *     Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 *     mail:	                 info@netline-is.de
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License as published by the Free
 *     Software Foundation; either version 2 of the License, or (at your option)
 *     any later version.
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
 *
 
 */

package com.openexchange.webdav.xml.parser;

import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.FolderFields;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 * FolderParser
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class FolderParser extends FolderChildParser {
	
	public FolderParser() {
		
	}
	
	public void parse(FolderObject folderObj, Element eProp) throws Exception {
		if (hasElement(eProp.getChild(FolderFields.TITLE, XmlServlet.NS))) {
			folderObj.setFolderName(getValue(eProp.getChild(FolderFields.TITLE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(FolderFields.TYPE, XmlServlet.NS))) {
			String type = getValue(eProp.getChild(FolderFields.TYPE, XmlServlet.NS));
			if (type.equals("private") || type.equals("shared")) {
				folderObj.setType(FolderObject.PRIVATE);
			} else if (type.equals("public")) {
				folderObj.setType(FolderObject.PUBLIC);
			} else {
				throw new OXConflictException("unknown value in " + FolderFields.TYPE + ": " + type);
			}
		}
		
		if (hasElement(eProp.getChild(FolderFields.MODULE, XmlServlet.NS))) {
			String module = eProp.getChild(FolderFields.MODULE, XmlServlet.NS).getValue();
			if (module.equals("calendar")) {
				folderObj.setModule(FolderObject.CALENDAR);
			} else if (module.equals("contact")) {
				folderObj.setModule(FolderObject.CONTACT);
			} else if (module.equals("task")) {
				folderObj.setModule(FolderObject.TASK);
			} else if (module.equals("unbound")) {
				folderObj.setModule(FolderObject.UNBOUND);
			} else {
				throw new OXConflictException("unknown value in " + FolderFields.MODULE + ": " + module);
			}
		}
		
		if (hasElement(eProp.getChild("defaultfolder", XmlServlet.NS))) {
			folderObj.setDefaultFolder(getValueAsBoolean(eProp.getChild("defaultfolder", XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(FolderFields.PERMISSIONS, XmlServlet.NS))) {
			parseElementPermissions(folderObj, eProp.getChild(FolderFields.PERMISSIONS, XmlServlet.NS));
		}

		parseElementFolderChildObject(folderObj, eProp);
	}
	
	protected void parseElementPermissions(FolderObject folderObj, Element ePermissions) throws Exception {
		ArrayList permissions = new ArrayList();
		
		try {
			int entity = 0;
			
			List elementPermissions = ePermissions.getChildren();
			for (int a = 0; a < elementPermissions.size(); a++) {
				Element e = (Element)elementPermissions.get(a);
				
				if (!e.getNamespace().equals(XmlServlet.NS)) {
					continue;
				}
				
				OCLPermission oclp = new OCLPermission();
				
				if (e.getName().equals("user")) {
					parseElementPermissionAttributes(oclp, e);
					parseEntity(oclp, e);
				} else if (e.getName().equals("group")) {
					parseElementPermissionAttributes(oclp, e);
					parseEntity(oclp, e);
					oclp.setGroupPermission(true);
				} else {
					throw new OXConflictException("unknown xml tag in permissions!");
				}
				
				permissions.add(oclp);
			}
		} catch (Exception exc) {
			throw new TestException(exc);
		}
		
		folderObj.setPermissions(permissions);
	}
	
	protected void parseEntity(OCLPermission oclp, Element e) throws Exception {
		oclp.setEntity( getValueAsInt(e));
	}
	
	protected void parseElementPermissionAttributes(OCLPermission oclp, Element e) throws Exception {
		int fp = getPermissionAttributeValue(e, "folderpermission");
		int orp = getPermissionAttributeValue(e, "objectreadpermission");
		int owp = getPermissionAttributeValue(e, "objectwritepermission");
		int odp = getPermissionAttributeValue(e, "objectdeletepermission");
		
		oclp.setAllPermission(fp, orp, owp, odp);
		oclp.setFolderAdmin(getPermissionAdminFlag(e));
	}
	
	protected int getPermissionAttributeValue(Element e, String name) throws Exception {
		return Integer.parseInt(e.getAttributeValue(name, XmlServlet.NS));
	}

	protected boolean getPermissionAdminFlag(Element e) throws Exception {
		return Boolean.parseBoolean(e.getAttributeValue("admin_flag", XmlServlet.NS));
	}
}




