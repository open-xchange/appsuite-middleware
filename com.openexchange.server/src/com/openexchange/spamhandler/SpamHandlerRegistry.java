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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link SpamHandlerRegistry} - The spam handler registry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SpamHandlerRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamHandlerRegistry.class);

    /** Dummy value to associate with an Object in the backing Map. */
    private static final Object PRESENT = new Object();

    /** Concurrent map for spam handlers. */
    private static final ConcurrentMap<String, SpamHandler> SPAM_HANDLERS = new ConcurrentHashMap<String, SpamHandler>();

    /** Concurrent "set" for unknown spam handlers. */
    private static final ConcurrentMap<String, Object> UNKNOWN_SPAM_HANDLERS = new ConcurrentHashMap<String, Object>();

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
     * Checks if a spam handler is enabled for specified user's primary account.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if a spam handler is defined for specified user's primary account; otherwise <code>false</code>
     * @throws OXException If spam handler check fails
     */
    public static boolean hasSpamHandler(int userId, int contextId) throws OXException {
        ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = factory.getView(userId, contextId);
        ComposedConfigProperty<String> property = view.property("com.openexchange.spamhandler.name", String.class);
        if (null != property && property.isDefined()) {
            String spamHandlerName = property.get();
            return Strings.isNotEmpty(spamHandlerName) && !SpamHandler.SPAM_HANDLER_FALLBACK.equals(spamHandlerName) && null != getSpamHandler(spamHandlerName);
        }

        // Fall-back to old behavior to look-up spam handler by provider of the primary account
        MailAccountStorageService storage = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
        if (null == storage) {
            throw ServiceExceptionCode.absentService(MailAccountStorageService.class);
        }

        MailAccount primaryAccount = storage.getDefaultMailAccount(userId, contextId);
        MailProvider provider = MailProviderRegistry.getRealMailProvider(primaryAccount.getMailProtocol());
        SpamHandler handler = getSpamHandlerByPrimaryProvider(provider);
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
     * @throws OXException If no appropriate spam handler can be found
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
     * @param session The session providing
     * @param accountId The account identifier
     * @param mailProvider The mail provider whose spam handler is returned if account's one is empty (if <code>null</code> session's provider is used as fallback)
     * @return The appropriate spam handler or special {@link NoSpamHandler#getInstance() NoSpamHandler} if no spam handler is applicable
     * @throws OXException If no appropriate spam handler can be found
     */
    public static SpamHandler getSpamHandlerBySession(Session session, int accountId, MailProvider mailProvider) throws OXException {
        if (MailAccount.DEFAULT_ID != accountId) {
            // No spam handler for external accounts
            LOG.debug("As per design no spam handler for the external account {} of user {} in context {}.", accountId, session.getUserId(), session.getContextId());
            return NoSpamHandler.getInstance();
        }

        ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        ComposedConfigProperty<String> property = view.property("com.openexchange.spamhandler.name", String.class);
        if (null != property && property.isDefined()) {
            String spamHandlerName = property.get();
            if (Strings.isNotEmpty(spamHandlerName)) {
                return getSpamHandler(spamHandlerName);
            }
        }

        /*-
         * Fall-back to old behavior to look-up spam handler by provider of the primary account
         *
         * Check session-associated mail cache
         */
        MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        String key = MailSessionParameterNames.getParamSpamHandler();
        SpamHandler handler;
        try {
            handler = mailSessionCache.getParameter(accountId, key);
            if (null != handler) {
                return handler;
            }
        } catch (final ClassCastException e) {
            // Probably caused by bundle update(s)
            LOG.debug("Failed to cast spam handler. Continuing with regular look-up.", e);
        }

        // Session-associated mail cache does not hold spam handler. Get it from primary account's provider
        MailProvider provider = mailProvider;
        if (null == provider) {
            MailAccountStorageService storage = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            if (null == storage) {
                throw ServiceExceptionCode.absentService(MailAccountStorageService.class);
            }

            MailAccount mailAccount = storage.getMailAccount(accountId, session.getUserId(), session.getContextId());
            provider = MailProviderRegistry.getRealMailProvider(mailAccount.getMailProtocol());
        }
        handler = getSpamHandlerByPrimaryProvider(provider);
        /*
         * Cache in session
         */
        mailSessionCache.putParameter(accountId, key, handler);
        return handler;
    }

    private static SpamHandler getSpamHandlerByPrimaryProvider(MailProvider mailProvider) {
        return null == mailProvider ? NoSpamHandler.getInstance() : mailProvider.getSpamHandler();
    }

    /**
     * Gets the spam handler appropriate for specified registration name.
     * <p>
     * If specified registration name is <code>null</code> or equals {@link SpamHandler#SPAM_HANDLER_FALLBACK},
     * {@link NoSpamHandler#getInstance()} is returned.
     *
     * @param registrationName The spam handler's registration name
     * @return The appropriate spam handler or special {@link NoSpamHandler#getInstance() NoSpamHandler} if such a registration name is unknown
     */
    public static SpamHandler getSpamHandler(final String registrationName) {
        if (null == registrationName) {
            LOG.warn("Given registration name is null. Using fallback spam handler '{}'", SpamHandler.SPAM_HANDLER_FALLBACK);
            return NoSpamHandler.getInstance();
        }
        if (SpamHandler.SPAM_HANDLER_FALLBACK.equals(registrationName) || UNKNOWN_SPAM_HANDLERS.containsKey(registrationName)) {
            return NoSpamHandler.getInstance();
        }

        SpamHandler spamHandler = SPAM_HANDLERS.get(registrationName);
        if (null == spamHandler) {
            LOG.warn("No spam handler found for registration name '{}'. Using fallback '{}'", registrationName, SpamHandler.SPAM_HANDLER_FALLBACK);
            UNKNOWN_SPAM_HANDLERS.put(registrationName, PRESENT);
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
        }

        if (null != SPAM_HANDLERS.putIfAbsent(registrationName, spamHandler)) {
            // There is already such a spam handler
            return false;
        }

        UNKNOWN_SPAM_HANDLERS.remove(registrationName);
        return true;
    }

    /**
     * Unregisters all spam handlers.
     */
    public static void unregisterAll() {
        /*
         * Clear registry
         */
        SPAM_HANDLERS.clear();
        UNKNOWN_SPAM_HANDLERS.clear();
    }

    /**
     * Unregisters the spam handler.
     *
     * @param spamHandler The spam handler to unregister
     * @return The unregistered spam handler, or <code>null</code>
     */
    public static SpamHandler unregisterSpamHandler(final SpamHandler spamHandler) {
        return null == spamHandler ? null : unregisterSpamHandlerByName(spamHandler.getSpamHandlerName());
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
        UNKNOWN_SPAM_HANDLERS.put(registrationName, PRESENT);
        return SPAM_HANDLERS.remove(registrationName);
    }

}
