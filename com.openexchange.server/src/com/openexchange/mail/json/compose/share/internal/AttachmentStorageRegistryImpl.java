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

package com.openexchange.mail.json.compose.share.internal;

import com.openexchange.exception.OXException;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.share.AttachmentStorageRegistry;
import com.openexchange.mail.json.compose.share.DefaultAttachmentStorage;
import com.openexchange.mail.json.compose.share.spi.AttachmentStorage;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.session.Session;

/**
 * {@link AttachmentStorageRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class AttachmentStorageRegistryImpl implements AttachmentStorageRegistry {

    private final ServiceListing<AttachmentStorage> storages;

    /**
     * Initializes a new {@link AttachmentStorageRegistryImpl}.
     */
    public AttachmentStorageRegistryImpl(ServiceListing<AttachmentStorage> storages) {
        super();
        this.storages = storages;
    }

    @Override
    public AttachmentStorage getAttachmentStorageFor(ComposeRequest composeRequest) throws OXException {
        for (AttachmentStorage attachmentStorage : storages.getServiceList()) {
            if (attachmentStorage.applicableFor(composeRequest)) {
                return attachmentStorage;
            }
        }

        return DefaultAttachmentStorage.getInstance();
    }

    @Override
    public AttachmentStorage getAttachmentStorageFor(Session session) throws OXException {
        for (AttachmentStorage attachmentStorage : storages.getServiceList()) {
            if (attachmentStorage.applicableFor(session)) {
                return attachmentStorage;
            }
        }

        return DefaultAttachmentStorage.getInstance();
    }

}
