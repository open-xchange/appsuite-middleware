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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.webdav.acl.mixins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.folderstorage.Permission;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;

/**
 * {@link CurrentUserPrivilegeSet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CurrentUserPrivilegeSet implements PropertyMixin {

    private static enum Privilege {
        READ("read"),
        WRITE("write"),
        WRITE_PROPERTIES("write-properties"),
        WRITE_CONTENT("write-content"),
        UNLOCK("unlock"),
        READ_ACL("read-acl"),
        READ_CURRENT_USER_PRIVILEGE_SET("read-current-user-privilege-set"),
        WRITE_ACL("write-acl"),
        BIND("bind"),
        UNBIND("unbind"),
        ALL("all"), ;

        private String name;

        private Privilege(String name) {
            this.name = name;
        }

        public static List<Privilege> getApplying(Permission permission) {
            List<Privilege> applying = new ArrayList<Privilege>();
            applying.add(READ_ACL);
            applying.add(READ_CURRENT_USER_PRIVILEGE_SET);

            int readPermission = permission.getReadPermission();
            if (readPermission >= Permission.READ_OWN_OBJECTS) {
                applying.add(READ);
            }

            int writePermission = permission.getWritePermission();
            if (writePermission >= Permission.WRITE_OWN_OBJECTS) {
                applying.add(WRITE);
                applying.add(WRITE_PROPERTIES);
                applying.add(WRITE_CONTENT);
            }

            if (permission.isAdmin()) {
                applying.add(WRITE_ACL);
            }

            if (permission.getFolderPermission() > Permission.CREATE_OBJECTS_IN_FOLDER) {
                applying.add(BIND);
            }

            if (permission.getDeletePermission() > Permission.DELETE_OWN_OBJECTS) {
                applying.add(UNBIND);
            }

            return applying;
        }

        public String getName() {
            return name;
        }
    }

    private final List<Privilege> privileges;

    public CurrentUserPrivilegeSet(Permission permission) {
        this.privileges = Privilege.getApplying(permission);
    }

    @Override
    public List<WebdavProperty> getAllProperties() throws OXException {
        return Collections.emptyList();
    }

    @Override
    public WebdavProperty getProperty(String namespace, String name) throws OXException {
        if (namespace.equals(Protocol.DAV_NS.getURI()) && name.equals("current-user-privilege-set")) {
            WebdavProperty property = new WebdavProperty(namespace, name);
            property.setXML(true);
            StringBuilder xml = new StringBuilder();
            for (Privilege priv : privileges) {
                xml.append("<D:privilege>").append("<D:").append(priv.getName()).append(" />").append("</D:privilege>");
            }
            property.setValue(xml.toString());

            return property;
        }
        return null;
    }
}
