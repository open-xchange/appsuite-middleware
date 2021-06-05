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

package com.openexchange.spamhandler.defaultspamhandler;

import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.service.MailService;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link DefaultSpamHandler} - The default spam handler which copies/moves spam/ham mails as they are.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultSpamHandler extends SpamHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSpamHandler.class);

    private static final DefaultSpamHandler instance = new DefaultSpamHandler();

    /**
     * Gets the singleton instance of {@link DefaultSpamHandler}
     *
     * @return The singleton instance of {@link DefaultSpamHandler}
     */
    public static DefaultSpamHandler getInstance() {
        return instance;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DefaultSpamHandler}
     */
    private DefaultSpamHandler() {
        super();
    }

    private <V> V getPropertyFromView(ConfigView view, String propertyName, V defaultValue, Class<V> clazz) throws OXException {
        ComposedConfigProperty<V> property = view.property(propertyName, clazz);
        return (null != property && property.isDefined()) ? property.get() : defaultValue;
    }

    @Override
    public String getSpamHandlerName() {
        return "DefaultSpamHandler";
    }

    @Override
    public void handleSpam(int accountId, String fullName, String[] mailIDs, boolean move, Session session) throws OXException {
        // Copy to confirmed spam folder (if exists)
        boolean confirmedSpamExists = isCreateConfirmedSpam(session);
        if (!confirmedSpamExists && !move) {
            // Nothing to do...
            return;
        }

        // Check for mail service availability
        MailService mailService = Services.getService(MailService.class);
        if (null == mailService) {
            LOG.warn("No mail service available. Aborting DefaultSpamHandler.handleSpam()...");
            return;
        }

        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();

            if (confirmedSpamExists) {
                String confirmedSpamFullname = mailAccess.getFolderStorage().getConfirmedSpamFolder();
                mailAccess.getMessageStorage().copyMessages(fullName, confirmedSpamFullname, mailIDs, true);
            }

            if (move) {
                // Move to spam folder
                String spamFullname = mailAccess.getFolderStorage().getSpamFolder();
                mailAccess.getMessageStorage().moveMessages(fullName, spamFullname, mailIDs, true);
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public void handleHam(int accountId, String spamFullName, String[] mailIDs, boolean move, Session session) throws OXException {
        // Copy to confirmed ham (if exists)
        boolean confirmedHamExists = isCreateConfirmedHam(session);
        if (!confirmedHamExists && !move) {
            // Nothing to do...
            return;
        }

        // Check for mail service availability
        MailService mailService = Services.getService(MailService.class);
        if (null == mailService) {
            LOG.warn("No mail service available. Aborting DefaultSpamHandler.handleHam()...");
            return;
        }

        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();

            if (confirmedHamExists) {
                String confirmedHamFullname = mailAccess.getFolderStorage().getConfirmedHamFolder();
                mailAccess.getMessageStorage().copyMessages(spamFullName, confirmedHamFullname, mailIDs, true);
            }

            if (move) {
                mailAccess.getMessageStorage().moveMessages(spamFullName, FULLNAME_INBOX, mailIDs, true);
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public boolean isCreateConfirmedSpam(Session session) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());

        return getPropertyFromView(view, "com.openexchange.spamhandler.defaultspamhandler.createConfirmedSpam", Boolean.TRUE, Boolean.class).booleanValue();
    }

    @Override
    public boolean isCreateConfirmedHam(Session session) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());

        return getPropertyFromView(view, "com.openexchange.spamhandler.defaultspamhandler.createConfirmedHam", Boolean.TRUE, Boolean.class).booleanValue();
    }

    @Override
    public boolean isUnsubscribeSpamFolders(Session session) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());

        return getPropertyFromView(view, "com.openexchange.spamhandler.defaultspamhandler.unsubscribeSpamFolders", Boolean.TRUE, Boolean.class).booleanValue();
    }

}
