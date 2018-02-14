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
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableList;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.session.Session;

/**
 * {@link MailFetchListenerRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailFetchListenerRegistry {

    private static final AtomicReference<MailFetchListenerRegistry> INSTANCE_REFERENCE = new AtomicReference<MailFetchListenerRegistry>(null);

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static MailFetchListenerRegistry getInstance() {
        return INSTANCE_REFERENCE.get();
    }

    /**
     * Initializes the instance
     *
     * @param listing The associated service listing
     */
    public static synchronized void initInstance(ServiceListing<MailFetchListener> listing) {
        INSTANCE_REFERENCE.set(new MailFetchListenerRegistry(listing));
    }

    /**
     * Release the instance
     */
    public static synchronized void releaseInstance() {
        INSTANCE_REFERENCE.set(null);
    }

    /**
     * Determines the effective listener chain for specified arguments.
     *
     * @param fetchArguments The fetch arguments
     * @param mailAccess The opened mail access
     * @param state The state, which lets individual listeners store stuff
     * @return The effective listener chain
     * @throws OXException If listener chain cannot be returned
     */
    public static MailFetchListenerChain determineFetchListenerChainFor(MailFetchArguments fetchArguments, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, Map<String, Object> state) throws OXException {
        MailFetchListenerRegistry registry = INSTANCE_REFERENCE.get();
        return null == registry ? MailFetchListenerChain.EMPTY_CHAIN : registry.getFetchListenerChainFor(fetchArguments, mailAccess, state);
    }

    /**
     * Determines the effective acceptance for specified arguments.
     *
     * @param mailsFromCache The mails fetched from cache
     * @param fetchArguments The fetch arguments
     * @param session The session providing user data
     * @return <code>true</code> if accepted; otherwise <code>false</code>
     * @throws OXException If listener chain cannot be returned
     */
    public static boolean determineAcceptance(MailMessage[] mailsFromCache, MailFetchArguments fetchArguments, Session session) throws OXException {
        MailFetchListenerRegistry registry = INSTANCE_REFERENCE.get();
        return null == registry ? true : registry.isAccepted(mailsFromCache, fetchArguments, session);
    }

    /**
     * Gets the fetch listeners.
     *
     * @return The listeners
     */
    public static List<MailFetchListener> getFetchListeners() {
        MailFetchListenerRegistry registry = INSTANCE_REFERENCE.get();
        return null == registry ? Collections.emptyList() : registry.getListeners();
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private final ServiceListing<MailFetchListener> listeners;

    /**
     * Initializes a new {@link MailFetchListenerRegistry}.
     */
    private MailFetchListenerRegistry(ServiceListing<MailFetchListener> listeners) {
        super();
        this.listeners = listeners;
    }

    /**
     * Gets all registered listeners
     *
     * @return The listeners
     */
    public List<MailFetchListener> getListeners() {
        return listeners.getServiceList();
    }

    /**
     * Gets the effective listener chain for specified arguments.
     *
     * @param fetchArguments The fetch arguments
     * @param mailAccess The mail access
     * @param state The state, which lets individual listeners store stuff
     * @return The effective listener chain
     * @throws OXException If listener chain cannot be returned
     */
    public MailFetchListenerChain getFetchListenerChainFor(MailFetchArguments fetchArguments, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, Map<String, Object> state) throws OXException {
        Iterator<MailFetchListener> iterator = this.listeners.iterator();
        if (false == iterator.hasNext()) {
            return MailFetchListenerChain.EMPTY_CHAIN;
        }

        ImmutableList.Builder<MailFetchListener> applicableListeners = ImmutableList.builder();
        MailField[] fs = fetchArguments.getFields();
        String[] hns = fetchArguments.getHeaderNames();
        boolean anyApplicable = false;
        do {
            MailFetchListener listener = iterator.next();
            MailAttributation attributation = listener.onBeforeFetch(MailFetchArguments.copy(fetchArguments, fs, hns), mailAccess, state);
            if (attributation.isApplicable()) {
                applicableListeners.add(listener);
                fs = attributation.getFields();
                hns = attributation.getHeaderNames();
                anyApplicable = true;
            }
        } while (iterator.hasNext());

        return anyApplicable ? new MailFetchListenerChain(applicableListeners.build(), MailAttributation.builder(fs, hns).build()) : MailFetchListenerChain.EMPTY_CHAIN;
    }

    /**
     * Gets the effective listener chain for specified arguments.
     *
     * @param mailsFromCache The mails fetched from cache
     * @param fetchArguments The fetch arguments
     * @param session The user's session
     * @return <code>true</code> if satisfied; otherwise <code>false</code>
     * @throws OXException If acceptance cannot be checked
     */
    public boolean isAccepted(MailMessage[] mailsFromCache, MailFetchArguments fetchArguments, Session session) throws OXException {
        Iterator<MailFetchListener> iterator = this.listeners.iterator();
        if (false == iterator.hasNext()) {
            return true;
        }

        do {
            MailFetchListener listener = iterator.next();
            if (false == listener.accept(mailsFromCache, fetchArguments, session)) {
                return false;
            }
        } while (iterator.hasNext());

        return true;
    }

}
