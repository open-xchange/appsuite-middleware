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

package com.openexchange.mail.categories.organizer;

import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.categories.MailObjectParameter;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.session.Session;

/**
 * {@link MailCategoriesOrganizer} is a helper class to reorganize a mail folder
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MailCategoriesOrganizer {

    private static MailField[] FIELDS = new MailField[] { MailField.ID };

    /**
     * Searches for all mails in the given folder which matches the given search term and set or unset the given flag to them
     *
     * @param session The user session
     * @param folder The folder id
     * @param searchTerm The search term
     * @param flag The flag
     * @param flagsToRemove The flags to remove
     * @throws OXException If retrieving or setting fails
     */
    public static void organizeExistingMails(Session session, String folder, SearchTerm<?> searchTerm, String flag, String[] flagsToRemove) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            FullnameArgument fa = prepareMailFolderParam(folder);
            mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            mailAccess.connect();

            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            MailMessage[] messages = messageStorage.searchMessages(fa.getFullName(), IndexRange.NULL, null, OrderDirection.ASC, searchTerm, FIELDS);
            String mailIds[] = new String[messages.length];
            for (int i = messages.length; i-- > 0;) {
                MailMessage message = messages[i];
                mailIds[i] = null == message ? null : message.getMailId();
            }
            if (flagsToRemove != null && flagsToRemove.length != 0) {
                messageStorage.updateMessageFlags(fa.getFullName(), mailIds, 0, flagsToRemove, false);
            }

            if (flag != null) {
                messageStorage.updateMessageFlags(fa.getFullName(), mailIds, 0, new String[] { flag }, true);
            }
        } finally {
            if (mailAccess != null) {
                mailAccess.close();
            }
        }
    }

    /**
     * Retrieves the given mails, removes all previous category flags and add the given flag.
     *
     * @param session The user session
     * @param folder The folder id
     * @param mails The mail to set the given flag to
     * @param flag The flag
     * @throws OXException If retrieving or setting fails
     */
    public static void organizeMails(Session session, String folder, List<MailObjectParameter> mailObjects, String flag, String[] flagsToRemove) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            FullnameArgument fa = prepareMailFolderParam(folder);
            mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            mailAccess.connect();

            String[] mailIds = new String[mailObjects.size()];
            int x = 0;
            for (MailObjectParameter mailObject : mailObjects) {
                mailIds[x++] = mailObject.getMailID();
            }
            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if (flagsToRemove != null && flagsToRemove.length != 0) {
                messageStorage.updateMessageFlags(fa.getFullName(), mailIds, 0, flagsToRemove, false);
            }
            if (flag != null) {
                messageStorage.updateMessageFlags(fa.getFullName(), mailIds, 0, new String[] { flag }, true);
            }
        } finally {
            if (mailAccess != null) {
                mailAccess.close();
            }
        }
    }

}
