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

package com.openexchange.mail;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link MailFetchListener} - A listener invoked right before and after fetching mails allowing to modify and/or enhance mails.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface MailFetchListener {

    /**
     * Invoked when mails are fetched from {@link MailMessageCache} to test whether this listener is satisfied with the information already available in cached mails.
     *
     * @param mailsFromCache The mails fetched from cache
     * @param fetchArguments The fetch arguments
     * @param session The user's session
     * @return <code>true</code> if satisfied; otherwise <code>false</code>
     * @throws OXException If acceptance cannot be checked
     */
    boolean accept(MailMessage[] mailsFromCache, MailFetchArguments fetchArguments, Session session) throws OXException;

    /**
     * Invoked prior to fetching mails from mail back-end and allows this listener to add its needed fields and/or header names (if any)
     *
     * @param fetchArguments The fetch arguments
     * @param mailAccess The user's session
     * @param state The state, which lets individual listeners store stuff
     * @return The mail attributation
     * @throws OXException If attributation fails
     */
    MailAttributation onBeforeFetch(MailFetchArguments fetchArguments, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, Map<String, Object> state) throws OXException;

    /**
     * Invoked after mails are fetched and allows to modify and/or enhance them.
     *
     * @param mails The fetched mails
     * @param cacheable Whether specified mails are supposed to be cached
     * @param mailAccess The user's session
     * @param state The state, which was passed to {@link #onBeforeFetch(MailFetchArguments, MailAccess, Map) onBeforeFetch} invocation
     * @return The listener's result
     * @throws OXException If an aborting error occurs; acts in the same way as returning {@link MailFetchListenerResult#deny(OXException)}
     */
    MailFetchListenerResult onAfterFetch(MailMessage[] mails, boolean cacheable, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, Map<String, Object> state) throws OXException;

    /**
     * Invoked when a single mail has been fully fetched from storage.
     *
     * @param mail The fetched mail
     * @param mailAccess The user's session
     * @return The resulting mail
     * @throws OXException If an aborting error occurs
     */
    MailMessage onSingleMailFetch(MailMessage mail, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException;

}
