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

package com.openexchange.mail.compose.mailstorage;

import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.mailstorage.association.IAssociationStorageManager;
import com.openexchange.mail.compose.mailstorage.storage.IMailStorage;
import com.openexchange.mail.compose.mailstorage.util.ExceptionLoggingCompositionSpaceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link MailStorageCompositionSpaceServiceFactory} - The composition space service implementation using mail back-end as storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class MailStorageCompositionSpaceServiceFactory implements CompositionSpaceServiceFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailStorageCompositionSpaceServiceFactory.class);

    private static final String SERICE_ID = "draft";

    private final ServiceLookup services;
    private final IMailStorage mailStorage;
    private final IAssociationStorageManager associationStorageManager;

    /**
     * Initializes a new {@link MailStorageCompositionSpaceServiceFactory}.
     *
     * @param mailStorage The mail storage
     * @param associationStorageManager The storage manager for active composition spaces having a backing draft message
     * @param services The service look-up
     */
    public MailStorageCompositionSpaceServiceFactory(IMailStorage mailStorage, IAssociationStorageManager associationStorageManager, ServiceLookup services) {
        super();
        this.associationStorageManager = associationStorageManager;
        if (null == mailStorage) {
            throw new IllegalArgumentException("Storage must not be null");
        }
        if (null == associationStorageManager) {
            throw new IllegalArgumentException("Association storage manager must not be null");
        }
        if (null == services) {
            throw new IllegalArgumentException("Service registry must not be null");
        }
        this.mailStorage = mailStorage;
        this.services = services;
    }

    @Override
    public String getServiceId() {
        return SERICE_ID;
    }

    @Override
    public int getRanking() {
        return 10;
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        return MailStorageCompositionSpaceConfig.getInstance().isEnabled(session.getUserId(), session.getContextId());
    }

    @Override
    public CompositionSpaceService createServiceFor(Session session) throws OXException {
        if (LOG.isDebugEnabled() == false) {
            return new MailStorageCompositionSpaceService(session, mailStorage, associationStorageManager, services, SERICE_ID);
        }

        return new ExceptionLoggingCompositionSpaceService(new MailStorageCompositionSpaceService(session, mailStorage, associationStorageManager, services, SERICE_ID));
    }

}
