/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.dav.principals.groups;

import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ResourceId;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.mixins.CalendarUserAddressSet;
import com.openexchange.dav.mixins.DisplayName;
import com.openexchange.dav.mixins.ExpandedGroupMemberSet;
import com.openexchange.dav.mixins.GroupMemberSet;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.mixins.RecordType;
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
     * @param url The WebDAV path of the resource
     */
    public GroupPrincipalResource(DAVFactory factory, Group group, WebdavPath url) {
        super(factory, url);
        this.group = group;
        ConfigViewFactory configViewFactory = factory.getService(ConfigViewFactory.class);
        includeProperties(new DisplayName(group.getDisplayName()), new com.openexchange.dav.mixins.CalendarUserType(CalendarUserType.GROUP), 
            new RecordType(RecordType.RECORD_TYPE_GROUPS), new GroupMemberSet(group.getMember(), configViewFactory), new ExpandedGroupMemberSet(group.getMember(), configViewFactory), 
            new PrincipalURL(group.getIdentifier(), CalendarUserType.GROUP, configViewFactory), new CalendarUserAddressSet(factory.getContext().getContextId(), group, configViewFactory), 
            new com.openexchange.dav.mixins.ResourceId(ResourceId.forGroup(factory.getContext().getContextId(), group.getIdentifier()))
            
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
