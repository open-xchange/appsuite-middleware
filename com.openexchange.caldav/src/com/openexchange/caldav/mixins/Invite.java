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

package com.openexchange.caldav.mixins;

import com.openexchange.caldav.CalDAVPermission;
import com.openexchange.caldav.CalDAVServiceLookup;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.resources.CommonFolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.ldap.User;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link Invite}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Invite extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Invite.class);

    private final CommonFolderCollection<?> collection;
    private final GroupwareCaldavFactory factory;

    /**
     * Initializes a new {@link Invite}.
     *
     * @param factory The CalDAV factory
     * @param collection The collection
     */
    public Invite(GroupwareCaldavFactory factory, CommonFolderCollection<?> collection) {
        super(CaldavProtocol.CALENDARSERVER_NS.getURI(), "invite");
        this.factory = factory;
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        if (null == collection || null == collection.getFolder() || null == collection.getFolder().getPermissions()) {
            return null;
        }
        StringBuilder StringBuilder = new StringBuilder();
        Permission[] permissions = collection.getFolder().getPermissions();
        for (Permission permission : permissions) {
            try {
                if (permission.isAdmin()) {
                    StringBuilder.append("<CS:organizer>").append(getEntityElements(permission)).append("</CS:organizer>");
                } else if (CalDAVPermission.impliesReadPermissions(permission)) {
                    StringBuilder.append("<CS:user>").append(getEntityElements(permission)).append("<CS:invite-accepted/>")
                        .append("<CS:access><CS:read");
                    if (CalDAVPermission.impliesReadWritePermissions(permission)) {
                        StringBuilder.append("-write");
                    }
                    StringBuilder.append("/></CS:access></CS:user>");
                }
            } catch (OXException e) {
                LOG.warn("error resolving permission entity from '{}'", collection.getFolder(), e);
            }
        }
        return StringBuilder.toString();
    }

    private String getEntityElements(Permission permission) throws OXException {
        StringBuilder StringBuilder = new StringBuilder();
        if (permission.isGroup()) {
            Group group = CalDAVServiceLookup.getService(GroupService.class).getGroup(factory.getContext(), permission.getEntity());
            StringBuilder.append("<D:href>/principals/groups/").append(group.getIdentifier())
                .append("/</D:href><CS:common-name> + ").append(group.getDisplayName()).append("</CS:common-name>");
        } else {
            User user = factory.resolveUser(permission.getEntity());
            StringBuilder.append("<D:href>/principals/users/").append(user.getLoginInfo())
                .append("/</D:href><CS:common-name>").append(user.getDisplayName()).append("</CS:common-name>");
        }
        return StringBuilder.toString();
    }

}
