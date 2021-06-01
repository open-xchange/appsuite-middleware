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

package com.openexchange.dav.principals.resources;

import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ResourceId;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.mixins.CalendarUserAddressSet;
import com.openexchange.dav.mixins.DisplayName;
import com.openexchange.dav.mixins.EmailAddressSet;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.mixins.RecordType;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.resource.Resource;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link ResourcePrincipalResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class ResourcePrincipalResource extends DAVResource {

    private final Resource resource;

    /**
     * Initializes a new {@link ResourcePrincipalResource}.
     *
     * @param factory The factory
     * @param resource The resource
     * @param url The WebDAV path of the resource
     */
    public ResourcePrincipalResource(DAVFactory factory, Resource resource, WebdavPath url) {
        super(factory, url);
        this.resource = resource;
        ConfigViewFactory configViewFactory = factory.getService(ConfigViewFactory.class);
        includeProperties(new DisplayName(resource.getDisplayName()), new com.openexchange.dav.mixins.CalendarUserType(CalendarUserType.RESOURCE),
            new RecordType(RecordType.RECORD_TYPE_RESOURCES), new PrincipalURL(resource.getIdentifier(), CalendarUserType.RESOURCE, configViewFactory),
            new CalendarUserAddressSet(factory.getContext().getContextId(), resource, configViewFactory), new EmailAddressSet(resource),
            new com.openexchange.dav.mixins.ResourceId(ResourceId.forResource(factory.getContext().getContextId(), resource.getIdentifier()))
        );
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        return"<D:resourcetype><D:principal /></D:resourcetype>";
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return resource.getDisplayName();
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        return "http://www.open-xchange.com/webdav/resource/" + (null != resource.getLastModified() ? resource.getLastModified().getTime() : resource.getIdentifier());
    }

}
