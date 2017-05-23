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
 *     mail:	                 info@open-xchange.com
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

import java.util.ArrayList;
import java.util.List;
import org.jdom2.Element;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.FolderFields;

/**
 * {@link FolderParser} - The WebDAV/XML folder parser.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class FolderParser extends FolderChildParser {

    /**
     * Initializes a new {@link FolderParser}.
     */
    public FolderParser() {
        super();
    }

    /**
     * Parses specified folder element into given folder.
     *
     * @param folder The folder to fill
     * @param eProp The folder element
     * @throws OXException If a conflict occurs
     * @throws OXException If a test error occurs
     */
    public void parse(final FolderObject folder, final Element eProp) throws OXException, OXException {
        if (hasElement(eProp.getChild(FolderFields.TITLE, XmlServlet.NS))) {
            folder.setFolderName(getValue(eProp.getChild(FolderFields.TITLE, XmlServlet.NS)));
        }

        if (hasElement(eProp.getChild(FolderFields.TYPE, XmlServlet.NS))) {
            final String type = getValue(eProp.getChild(FolderFields.TYPE, XmlServlet.NS));
            if (type.equals("private") || type.equals("shared")) {
                folder.setType(FolderObject.PRIVATE);
            } else if (type.equals("public")) {
                folder.setType(FolderObject.PUBLIC);
            } else {
                throw OXException.general("unknown value in " + FolderFields.TYPE + ": " + type);
            }
        }

        if (hasElement(eProp.getChild(FolderFields.MODULE, XmlServlet.NS))) {
            final String module = eProp.getChild(FolderFields.MODULE, XmlServlet.NS).getValue();
            if (module.equals("calendar")) {
                folder.setModule(FolderObject.CALENDAR);
            } else if (module.equals("contact")) {
                folder.setModule(FolderObject.CONTACT);
            } else if (module.equals("task")) {
                folder.setModule(FolderObject.TASK);
            } else if (module.equals("unbound")) {
                folder.setModule(FolderObject.UNBOUND);
            } else {
                throw OXException.general("unknown value in " + FolderFields.MODULE + ": " + module);
            }
        }

        if (hasElement(eProp.getChild("defaultfolder", XmlServlet.NS))) {
            folder.setDefaultFolder(getValueAsBoolean(eProp.getChild("defaultfolder", XmlServlet.NS)));
        }

        if (hasElement(eProp.getChild(FolderFields.PERMISSIONS, XmlServlet.NS))) {
            parseElementPermissions(folder, eProp.getChild(FolderFields.PERMISSIONS, XmlServlet.NS));
        }

        parseElementFolderChildObject(folder, eProp);
    }

    /**
     * Parses specified folder permissions element into given folder.
     *
     * @param folder The folder to fill
     * @param ePermissions The folder permissions element
     * @throws OXException If a test error occurs
     */
    protected void parseElementPermissions(final FolderObject folder, final Element ePermissions) throws OXException {
        final List<OCLPermission> permissions = new ArrayList<OCLPermission>();

        final List<?> elementPermissions = ePermissions.getChildren();
        for (int a = 0; a < elementPermissions.size(); a++) {
            final Element e = (Element) elementPermissions.get(a);

            if (!e.getNamespace().equals(XmlServlet.NS)) {
                continue;
            }

            final OCLPermission oclp = new OCLPermission();

            if (e.getName().equals("user")) {
                parseElementPermissionAttributes(oclp, e);
                parseEntity(oclp, e);
            } else if (e.getName().equals("group")) {
                parseElementPermissionAttributes(oclp, e);
                parseEntity(oclp, e);
                oclp.setGroupPermission(true);
            } else {
                throw OXException.general("unknown xml tag in permissions!");
            }

            permissions.add(oclp);
        }

        folder.setPermissions(permissions);
    }

    protected void parseEntity(final OCLPermission oclp, final Element e) {
        oclp.setEntity(getValueAsInt(e));
    }

    protected void parseElementPermissionAttributes(final OCLPermission oclp, final Element e) {
        final int fp = getPermissionAttributeValue(e, "folderpermission");
        final int orp = getPermissionAttributeValue(e, "objectreadpermission");
        final int owp = getPermissionAttributeValue(e, "objectwritepermission");
        final int odp = getPermissionAttributeValue(e, "objectdeletepermission");

        oclp.setAllPermission(fp, orp, owp, odp);
        oclp.setFolderAdmin(getPermissionAdminFlag(e));
    }

    protected int getPermissionAttributeValue(final Element e, final String name) {
        return Integer.parseInt(e.getAttributeValue(name, XmlServlet.NS));
    }

    protected boolean getPermissionAdminFlag(final Element e) {
        return Boolean.parseBoolean(e.getAttributeValue("admin_flag", XmlServlet.NS));
    }
}
