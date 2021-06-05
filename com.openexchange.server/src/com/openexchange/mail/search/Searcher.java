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

package com.openexchange.mail.search;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link Searcher} - Provides methods to check if a single mail message matches a search term.
 * <p>
 * Moreover it provides a method to search for matching mail messages in a given message array with a given search term.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Searcher {

    /**
     * Checks if specified mail message matches given search term
     *
     * @param mailMessage The mail message to check
     * @param searchTerm The search term to apply
     * @return <code>true</code> if specified mail message matches given search term; otherwise <code>false</code>
     * @throws OXException If checking mail message against search term fails
     * @deprecated Invoke {@link SearchTerm#matches(MailMessage)} instead
     */
    @Deprecated
    public static boolean matches(MailMessage mailMessage, SearchTerm<?> searchTerm) throws OXException {
        return searchTerm.matches(mailMessage);
    }

    /**
     * Applies specified search term against given instances of {@link MailMessage}
     *
     * @param mailMessages The mail messages to check
     * @param searchTerm The search term to apply
     * @return The matching mail messages in order of appearance
     * @throws OXException If checking mail messages against search term fails
     */
    public static MailMessage[] matches(MailMessage[] mailMessages, SearchTerm<?> searchTerm) throws OXException {
        final List<MailMessage> matched = new ArrayList<MailMessage>(mailMessages.length);
        for (MailMessage mailMessage : mailMessages) {
            if (searchTerm.matches(mailMessage)) {
                matched.add(mailMessage);
            }
        }
        return matched.toArray(new MailMessage[matched.size()]);
    }

    /**
     * Applies specified search term against given instances of {@link MailMessage}
     *
     * @param mailMessages The mail messages to check
     * @param searchTerm The search term to apply
     * @return The matching mail messages in order of appearance
     * @throws OXException If checking mail messages against search term fails
     */
    public static List<MailMessage> matches(List<MailMessage> mailMessages, SearchTerm<?> searchTerm) throws OXException {
        final List<MailMessage> matched = new ArrayList<MailMessage>(mailMessages.size());
        for (MailMessage mailMessage : mailMessages) {
            if (searchTerm.matches(mailMessage)) {
                matched.add(mailMessage);
            }
        }
        return matched;
    }

    /**
     * Initializes a new {@link Searcher}
     */
    private Searcher() {
        super();
    }
}
