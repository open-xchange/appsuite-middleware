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
