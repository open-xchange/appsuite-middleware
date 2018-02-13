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
 *    trademarks of the OX Software GmbH. group of companies.
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
