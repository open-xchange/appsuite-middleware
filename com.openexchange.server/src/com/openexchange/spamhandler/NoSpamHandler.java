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

package com.openexchange.spamhandler;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;

/**
 * {@link NoSpamHandler} - The special spam handler ignoring invocations to both {@link #handleSpam(String, long[], boolean, MailAccess)}
 * and {@link #handleHam(String, long[], boolean, MailAccess)}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NoSpamHandler extends SpamHandler {

    private static final NoSpamHandler instance = new NoSpamHandler();

    /**
     * Gets the {@link NoSpamHandler} instance
     *
     * @return The {@link NoSpamHandler} instance
     */
    public static NoSpamHandler getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link NoSpamHandler}
     */
    private NoSpamHandler() {
        super();
    }

    @Override
    public String getSpamHandlerName() {
        return SpamHandler.SPAM_HANDLER_FALLBACK;
    }

    @Override
    public void handleHam(final int accountId, final String spamFullName, final String[] mailIDs, final boolean move, final Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public void handleSpam(final int accountId, final String fullName, final String[] mailIDs, final boolean move, final Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public boolean isCreateConfirmedSpam(Session session) {
        return false;
    }

    @Override
    public boolean isCreateConfirmedHam(Session session) {
        return false;
    }

    @Override
    public boolean isUnsubscribeSpamFolders(Session session) {
        return true;
    }
}
