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

package com.openexchange.mail.messaging;

import java.util.List;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.MessagingService;
import com.openexchange.session.Session;

/**
 * {@link MailMessagingService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessagingService implements MessagingService {

    private static final MailMessagingService INSTANCE = new MailMessagingService();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static MailMessagingService getInstance() {
        return INSTANCE;
    }

    /**
     * The identifier of mail messaging service.
     */
    public static final String ID = "com.openexchange.messaging.mail";

    /**
     * Initializes a new {@link MailMessagingService}.
     */
    private MailMessagingService() {
        super();
    }

    @Override
    public MessagingAccountAccess getAccountAccess(int accountId, Session session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public MessagingAccountManager getAccountManager() {
        // Nothing to do
        return null;
    }

    @Override
    public MessagingAccountTransport getAccountTransport(int accountId, Session session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public String getDisplayName() {
        // Nothing to do
        return null;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<MessagingAction> getMessageActions() {
        // Nothing to do
        return null;
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        // Nothing to do
        return null;
    }

    @Override
    public Set<String> getSecretProperties() {
        // Nothing to do
        return null;
    }

    @Override
    public int[] getStaticRootPermissions() {
        // Nothing to do
        return null;
    }

}
