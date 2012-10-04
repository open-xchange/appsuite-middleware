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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.spamhandler.defaultspamhandler;

import com.openexchange.config.ConfigurationService;
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


    private static final String NAME = "DefaultSpamHandler";

    private static final DefaultSpamHandler instance = new DefaultSpamHandler();

    /**
     * Gets the singleton instance of {@link DefaultSpamHandler}
     *
     * @return The singleton instance of {@link DefaultSpamHandler}
     */
    public static DefaultSpamHandler getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link DefaultSpamHandler}
     */
    private DefaultSpamHandler() {
        super();
    }

    @Override
    public String getSpamHandlerName() {
        return NAME;
    }

    @Override
    public void handleHam(final int accountId, final String spamFullname, final String[] mailIDs, final boolean move, final Session session) throws OXException {
        /*
         * Copy to confirmed ham
         */
        final MailService mailService = Services.getService(MailService.class);
        if (null == mailService) {
            return;
        }
        final MailAccess<?, ?> mailAccess = mailService.getMailAccess(session, accountId);
        mailAccess.connect();
        try {
            final String confirmedHamFullname = mailAccess.getFolderStorage().getConfirmedHamFolder();
            mailAccess.getMessageStorage().copyMessages(spamFullname, confirmedHamFullname, mailIDs, true);
            if (move) {
                mailAccess.getMessageStorage().moveMessages(spamFullname, FULLNAME_INBOX, mailIDs, true);
            }
        } finally {
            mailAccess.close(true);
        }
    }

    @Override
    public boolean isCreateConfirmedSpam() {
        final ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        if (null == configurationService) {
            /*
             * Return default
             */
            return true;
        }
        return configurationService.getBoolProperty("com.openexchange.spamhandler.defaultspamhandler.createConfirmedSpam", true);
    }

    @Override
    public boolean isCreateConfirmedHam() {
        final ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        if (null == configurationService) {
            /*
             * Return default
             */
            return true;
        }
        return configurationService.getBoolProperty("com.openexchange.spamhandler.defaultspamhandler.createConfirmedHam", true);
    }

    @Override
    public boolean isUnsubscribeSpamFolders() {
        final ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        if (null == configurationService) {
            /*
             * Return default
             */
            return true;
        }
        return configurationService.getBoolProperty("com.openexchange.spamhandler.defaultspamhandler.unsubscribeSpamFolders", true);
    }

}
