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
    public static boolean matches(final MailMessage mailMessage, final SearchTerm<?> searchTerm) throws OXException {
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
    public static MailMessage[] matches(final MailMessage[] mailMessages, final SearchTerm<?> searchTerm) throws OXException {
        final List<MailMessage> matched = new ArrayList<MailMessage>(mailMessages.length);
        for (final MailMessage mailMessage : mailMessages) {
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
    public static List<MailMessage> matches(final List<MailMessage> mailMessages, final SearchTerm<?> searchTerm) throws OXException {
        final List<MailMessage> matched = new ArrayList<MailMessage>(mailMessages.size());
        for (final MailMessage mailMessage : mailMessages) {
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
