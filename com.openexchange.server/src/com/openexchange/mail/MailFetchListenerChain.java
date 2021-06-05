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

package com.openexchange.mail;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailFetchListenerResult.ListenerReply;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link MailFetchListenerChain}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailFetchListenerChain implements MailFetchListener {

    /** The empty listener chain */
    public static MailFetchListenerChain EMPTY_CHAIN = new MailFetchListenerChain(null, MailAttributation.NOT_APPLICABLE);

    /**
     * Checks if given listener chain is <b>not</b> empty.
     *
     * @param listenerChain The listener chain to check
     * @return <code>true</code> if listener chain is not empty; otherwise <code>false</code>
     */
    public static boolean isNotEmptyChain(MailFetchListenerChain listenerChain) {
        return isEmptyChain(listenerChain) == false;
    }

    /**
     * Checks if given listener chain is empty.
     *
     * @param listenerChain The listener chain to check
     * @return <code>true</code> if listener chain is empty; otherwise <code>false</code>
     */
    public static boolean isEmptyChain(MailFetchListenerChain listenerChain) {
        return listenerChain == null || listenerChain == EMPTY_CHAIN ? true : listenerChain.listeners.isEmpty();
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private final List<MailFetchListener> listeners;
    private final MailAttributation optPrecomputedAttributation;

    /**
     * Initializes a new {@link MailFetchListenerChain}.
     */
    public MailFetchListenerChain(List<MailFetchListener> listeners, MailAttributation optPrecomputedAttributation) {
        super();
        this.optPrecomputedAttributation = optPrecomputedAttributation;
        this.listeners = null == listeners ? Collections.<MailFetchListener> emptyList() : listeners;
    }

    @Override
    public boolean accept(MailMessage[] mailsFromCache, MailFetchArguments fetchArguments, Session session) throws OXException {
        Iterator<MailFetchListener> iterator = this.listeners.iterator();
        if (false == iterator.hasNext()) {
            return true;
        }

        boolean accepted = true;
        do {
            MailFetchListener listener = iterator.next();
            accepted = listener.accept(mailsFromCache, fetchArguments, session);
        } while (accepted && iterator.hasNext());

        return accepted;
    }

    @Override
    public MailAttributation onBeforeFetch(MailFetchArguments fetchArguments, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, Map<String, Object> state) throws OXException {
        if (null != optPrecomputedAttributation) {
            return optPrecomputedAttributation;
        }

        Iterator<MailFetchListener> iterator = this.listeners.iterator();
        if (false == iterator.hasNext()) {
            return MailAttributation.NOT_APPLICABLE;
        }

        MailField[] fs = fetchArguments.getFields();
        String[] hns = fetchArguments.getHeaderNames();
        boolean anyApplicable = false;
        do {
            MailFetchListener listener = iterator.next();
            MailAttributation attributation = listener.onBeforeFetch(MailFetchArguments.copy(fetchArguments, fs, hns), mailAccess, state);
            if (attributation.isApplicable()) {
                fs = attributation.getFields();
                hns = attributation.getHeaderNames();
                anyApplicable = true;
            }
        } while (iterator.hasNext());

        return anyApplicable ? MailAttributation.builder(fs, hns).build() : MailAttributation.NOT_APPLICABLE;
    }

    @Override
    public MailFetchListenerResult onAfterFetch(MailMessage[] mails, boolean cacheable, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, Map<String, Object> state) throws OXException {
        Iterator<MailFetchListener> iterator = this.listeners.iterator();
        if (false == iterator.hasNext()) {
            return MailFetchListenerResult.neutral(mails, true);
        }

        boolean wannaCache = cacheable;
        MailMessage[] ms = mails;
        do {
            MailFetchListener listener = iterator.next();
            MailFetchListenerResult result = listener.onAfterFetch(ms, wannaCache, mailAccess, state);
            if (ListenerReply.NEUTRAL != result.getReply()) {
                return MailFetchListenerResult.copy(result, wannaCache);
            }
            ms = result.getMails();
            if (false == result.isCacheable()) {
                wannaCache = false;
            }
        } while (iterator.hasNext());

        return MailFetchListenerResult.neutral(ms, wannaCache);
    }

    @Override
    public MailMessage onSingleMailFetch(MailMessage mail, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        if (null == mail) {
            return null;
        }

        Iterator<MailFetchListener> iterator = this.listeners.iterator();
        if (false == iterator.hasNext()) {
            return mail;
        }

        MailMessage result = mail;
        do {
            MailFetchListener listener = iterator.next();
            result = listener.onSingleMailFetch(result, mailAccess);
        } while (iterator.hasNext());
        return result;
    }

}
