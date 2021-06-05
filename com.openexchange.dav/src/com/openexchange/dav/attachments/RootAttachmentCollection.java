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

package com.openexchange.dav.attachments;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.AttachmentUtils;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.mixins.AddressbookHomeSet;
import com.openexchange.dav.mixins.CalendarHomeSet;
import com.openexchange.dav.mixins.CurrentUserPrincipal;
import com.openexchange.dav.mixins.PrincipalCollectionSet;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.dav.resources.DAVRootCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link RootAttachmentCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class RootAttachmentCollection extends DAVRootCollection {

    /**
     * Initializes a new {@link RootAttachmentCollection}.
     *
     * @param factory The factory
     */
    public RootAttachmentCollection(AttachmentFactory factory) {
        super(factory, "Attachments");
        ConfigViewFactory configViewFactory = factory.getService(ConfigViewFactory.class);
        includeProperties(new CurrentUserPrincipal(factory), new PrincipalCollectionSet(configViewFactory), new CalendarHomeSet(configViewFactory), new AddressbookHomeSet(configViewFactory));
    }

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    public DAVResource getChild(String name) throws WebdavProtocolException {
        /*
         * decode name
         */
        AttachmentMetadata metadata;
        try {
            metadata = AttachmentUtils.decodeName(name);
        } catch (IllegalArgumentException e) {
            throw DAVProtocol.protocolException(getUrl(), e, HttpServletResponse.SC_NOT_FOUND);
        }
        /*
         * re-load full metadata (necessary to signal appropriate headers in response)
         */
        AttachmentBase attachments = Attachments.getInstance();
        DAVFactory factory = getFactory();
        try {
            metadata = attachments.getAttachment(factory.getSession(),
                metadata.getFolderId(), metadata.getAttachedId(), metadata.getModuleId(), metadata.getId(),
                factory.getContext(), factory.getUser(), factory.getUserConfiguration());
        } catch (OXException e) {
            throw AttachmentUtils.protocolException(e, getUrl());
        }
        /*
         * create attachment resource
         */
        return new AttachmentResource(getFactory(), metadata, constructPathForChildResource(name));
    }

}
