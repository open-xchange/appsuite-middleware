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

package com.openexchange.spamhandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link SpamHandlerRegistry} - The spam handler registry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SpamHandlerRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamHandlerRegistry.class);

    /**
     * Dummy value to associate with an Object in the backing Map.
     */
    private static final Object PRESENT = new Object();

    /**
     * Concurrent map for spam handlers.
     */
    private static final Map<String, SpamHandler> spamHandlers = new ConcurrentHashMap<String, SpamHandler>();

    /**
     * Concurrent "set" for unknown spam handlers.
     */
    private static final Map<String, Object> unknownSpamHandlers = new ConcurrentHashMap<String, Object>();

    /**
     * Initializes a new {@link SpamHandlerRegistry}.
     */
    private SpamHandlerRegistry() {
        super();
    }

    /**
     * Checks if a spam handler is present for the user denoted by specified session.
     * <p>
     * This is a convenience method that checks if the spam handler returned by {@link #getSpamHandlerBySession(Session)} is not an instance
     * of {@link NoSpamHandler}.
     *
     * @param session The session providing user data
     * @param accountId The account ID
     * @return <code>true</code> if a spam handler is defined by user's mail provider; otherwise <code>false</code>
     * @throws OXException If existence of a spam handler cannot be checked
     */
    public static boolean hasSpamHandler(final Session session, final int accountId) throws OXException {
        return !SpamHandler.SPAM_HANDLER_FALLBACK.equals(getSpamHandlerBySession(session, accountId).getSpamHandlerName());
    }

    /**
     * Checks if a spam handler is present for the denoted mail account.
     *
     * @param mailAccount The mail account
     * @return <code>true</code> if a spam handler is defined by user's mail provider; otherwise <code>false</code>
     */
    public static boolean hasSpamHandler(final MailAccount mailAccount) {
        final SpamHandler handler;
        try {
            final MailProvider provider = MailProviderRegistry.getRealMailProvider(mailAccount.getMailProtocol());
            handler = getSpamHandler0(mailAccount, new StaticMailProviderGetter(provider));
        } catch (final OXException e) {
            // Cannot occur
            LOG.error("", e);
            return false;
        }
        return handler == null ? false : !SpamHandler.SPAM_HANDLER_FALLBACK.equals(handler.getSpamHandlerName());
    }

    /**
     * Gets the spam handler appropriate for specified session.
     * <p>
     * At first the mail account's spam handler is checked, if invalid the session provider's spam handler is checked. For last instance the
     * fallback spam handler {@link NoSpamHandler} is returned to accomplish no spam handler support.
     *
     * @param session The session which probably caches spam handler
     * @param accountId The account ID
     * @return The appropriate spam handler
     * @throws OXException If no supporting spam handler can be found
     */
    public static SpamHandler getSpamHandlerBySession(final Session session, final int accountId) throws OXException {
        return getSpamHandlerBySession(session, accountId, null);
    }

    /**
     * Gets the spam handler appropriate for specified session.
     * <p>
     * At first the mail account's spam handler is checked, if invalid the specified provider's spam handler is checked. For last instance
     * the fallback spam handler {@link NoSpamHandler} is returned to accomplish no spam handler support.
     *
     * @param session The session which probably caches spam handler
     * @param accountId The account ID
     * @param mailProvider The mail provider whose spam handler is returned if account's one is empty (if <code>null</code> session's
     *            provider is used as fallback)
     * @return The appropriate spam handler
     * @throws OXException If no supporting spam handler can be found
     */
    public static SpamHandler getSpamHandlerBySession(final Session session, final int accountId, final MailProvider mailProvider) throws OXException {
        final MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        final String key = MailSessionParameterNames.getParamSpamHandler();
        SpamHandler handler;
        try {
            handler = mailSessionCache.getParameter(accountId, key);
        } catch (final ClassCastException e) {
            /*
             * Probably caused by bundle update(s)
             */
            handler = null;
        }
        if (null != handler) {
            return handler;
        }
        /*
         * Session does not hold spam handler
         */
        final MailAccount mailAccount;
        try {
            final MailAccountStorageService storageService =
                ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
        } catch (final OXException e) {
            throw e;
        }
        final MailProviderGetter mailProviderGetter;
        if (null == mailProvider) {
            mailProviderGetter = new SessionMailProviderGetter(mailAccount.getMailProtocol());
        } else {
            mailProviderGetter = new SimpleMailProviderGetter(mailProvider);
        }
        handler = getSpamHandler0(mailAccount, mailProviderGetter);
        //if (!SpamHandler.SPAM_HANDLER_FALLBACK.equals(handler.getSpamHandlerName())) {
        /*
         * Cache in session
         */
        mailSessionCache.putParameter(accountId, key, handler);
        //}
        return handler;
    }

    private static SpamHandler getSpamHandler0(final MailAccount mailAccount, final MailProviderGetter mailProviderGetter) throws OXException {
        /*
         * On first load account's spam handler
         */
        final String spamHandlerName;
        if (mailAccount.isDefaultAccount()) {
            // TODO: Decide whether to return provider's spam handler if default account is denoted by account ID
            /*-
             * By now the providers spam handler is returned to maintain backward compatibility.
             * To retrieve account's spam handler type:
             *
             * spamHandlerName = mailAccount.getSpamHandler();
             */
            final MailProvider mailProvider = mailProviderGetter.getMailProvider();
            if (null == mailProvider) {
                return NoSpamHandler.getInstance();
            }
            spamHandlerName = mailProvider.getSpamHandler().getSpamHandlerName();
        } else {
            /*
             * No spam handler for external accounts
             */
            LOG.debug("No spam handler for the external account with login {} (user {}) available per design.", mailAccount.getLogin(), mailAccount.getUserId());
            return NoSpamHandler.getInstance();
        }
        SpamHandler handler;
        if (null != spamHandlerName && spamHandlerName.length() > 0) {
            /*
             * Account specifies a valid spam handler name
             */
            handler = getSpamHandler(spamHandlerName);
        } else {
            /*
             * Account does not specify a valid spam handler name; take from mail provider
             */
            handler = mailProviderGetter.getMailProvider().getSpamHandler();
        }
        return handler;
    }

    /**
     * Gets the spam handler appropriate for specified registration name.
     * <p>
     * If specified registration name is <code>null</code> or equals {@link SpamHandler#SPAM_HANDLER_FALLBACK},
     * {@link NoSpamHandler#getInstance()} is returned.
     *
     * @param registrationName The spam handler's registration name
     * @return The appropriate spam handler or <code>null</code>
     */
    public static SpamHandler getSpamHandler(final String registrationName) {
        if (null == registrationName) {
            LOG.warn("Given registration name is null. Using fallback spam handler '{}'", SpamHandler.SPAM_HANDLER_FALLBACK);
            return NoSpamHandler.getInstance();
        } else if (SpamHandler.SPAM_HANDLER_FALLBACK.equals(registrationName) || unknownSpamHandlers.containsKey(registrationName)) {
            return NoSpamHandler.getInstance();
        }
        final SpamHandler spamHandler = spamHandlers.get(registrationName);
        if (null == spamHandler) {
            LOG.warn("No spam handler found for registration name '{}'. Using fallback '{}'", registrationName, SpamHandler.SPAM_HANDLER_FALLBACK);
            unknownSpamHandlers.put(registrationName, PRESENT);
            return NoSpamHandler.getInstance();
        }
        return spamHandler;
    }

    /**
     * Registers a spam handler.
     *
     * @param registrationName The spam handler's registration name
     * @param spamHandler The spam handler to register
     * @return <code>true</code> if spam handler has been successfully registered and no other spam handler uses the same registration name;
     *         otherwise <code>false</code>
     */
    public static boolean registerSpamHandler(final String registrationName, final SpamHandler spamHandler) {
        if (null == registrationName || SpamHandler.SPAM_HANDLER_FALLBACK.equals(registrationName)) {
            return false;
        } else if (spamHandlers.containsKey(registrationName)) {
            return false;
        }
        try {
            /*
             * Add to registry
             */
            spamHandlers.put(registrationName, spamHandler);
            unknownSpamHandlers.remove(registrationName);
            return true;
        } catch (final RuntimeException t) {
            LOG.error("", t);
            return false;
        }
    }

    /**
     * Unregisters all spam handlers.
     */
    public static void unregisterAll() {
        /*
         * Clear registry
         */
        spamHandlers.clear();
        unknownSpamHandlers.clear();
    }

    /**
     * Unregisters the spam handler.
     *
     * @param spamHandler The spam handler to unregister
     * @return The unregistered spam handler, or <code>null</code>
     */
    public static SpamHandler unregisterSpamHandler(final SpamHandler spamHandler) {
        /*
         * Unregister
         */
        final String registrationName = spamHandler.getSpamHandlerName();
        unknownSpamHandlers.put(registrationName, PRESENT);
        return spamHandlers.remove(registrationName);
    }

    /**
     * Unregisters the spam handler registered by specified name.
     *
     * @param registrationName The registration name
     * @return The unregistered instance of {@link SpamHandler}, or <code>null</code> if there was no spam handler supporting registered by
     *         specified name
     */
    public static SpamHandler unregisterSpamHandlerByName(final String registrationName) {
        /*
         * Unregister
         */
        unknownSpamHandlers.put(registrationName, PRESENT);
        return spamHandlers.remove(registrationName);
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * +++++++++++++++++++++++++++++++++++++++++++ HELPER CLASSES +++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static interface MailProviderGetter {

        public MailProvider getMailProvider() throws OXException;
    }

    private static final class SimpleMailProviderGetter implements MailProviderGetter {

        private final MailProvider mailProvider;

        public SimpleMailProviderGetter(final MailProvider mailProvider) {
            super();
            this.mailProvider = mailProvider;
        }

        @Override
        public MailProvider getMailProvider() {
            return mailProvider;
        }
    }

    private static final class SessionMailProviderGetter implements MailProviderGetter {

        private final String protocolName;

        SessionMailProviderGetter(final String protocolName) {
            super();
            this.protocolName = protocolName;
        }

        @Override
        public MailProvider getMailProvider() throws OXException {
            return MailProviderRegistry.getRealMailProvider(protocolName);
        }
    }

    private static final class StaticMailProviderGetter implements MailProviderGetter {

        private final MailProvider mailProvider;

        StaticMailProviderGetter(final MailProvider mailProvider) {
            super();
            this.mailProvider = mailProvider;
        }

        @Override
        public MailProvider getMailProvider() {
            return mailProvider;
        }

    }
}
