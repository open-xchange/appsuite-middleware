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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.dav.principals.groups;

import com.openexchange.dav.CUType;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.mixins.CalendarUserAddressSet;
import com.openexchange.dav.mixins.CalendarUserType;
import com.openexchange.dav.mixins.DisplayName;
import com.openexchange.dav.mixins.ExpandedGroupMemberSet;
import com.openexchange.dav.mixins.GroupMemberSet;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.mixins.RecordType;
import com.openexchange.dav.mixins.ResourceId;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.group.Group;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link GroupPrincipalResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class GroupPrincipalResource extends DAVResource {

    private final Group group;

    /**
     * Initializes a new {@link GroupPrincipalResource}.
     *
     * @param factory The factory
     * @param group The group
     */
    public GroupPrincipalResource(DAVFactory factory, Group group) {
        super(factory, new WebdavPath("principals", "groups", String.valueOf(group.getIdentifier())));
        this.group = group;
        includeProperties(new DisplayName(group.getDisplayName()), new CalendarUserType(CUType.GROUP), new RecordType(RecordType.RECORD_TYPE_GROUPS),
            new GroupMemberSet(group.getMember()), new PrincipalURL(group.getIdentifier(), CUType.GROUP), new ExpandedGroupMemberSet(group.getMember()),
            new ResourceId(factory.getContext().getContextId(), group.getIdentifier(), CUType.GROUP),
            new CalendarUserAddressSet(factory.getContext().getContextId(), group)
        );
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        return"<D:resourcetype><D:principal /></D:resourcetype>";
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return group.getDisplayName();
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        return "http://www.open-xchange.com/webdav/groups/" + (null != group.getLastModified() ? group.getLastModified().getTime() : group.getIdentifier());
    }

}
