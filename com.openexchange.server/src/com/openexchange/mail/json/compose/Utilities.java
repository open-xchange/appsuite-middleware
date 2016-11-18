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

package com.openexchange.mail.json.compose;

import org.apache.commons.lang.Validate;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link Utilities} - A utility class for compose module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public enum Utilities {
    ;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Utilities.class);

    private static ConfigurationService getConfigurationService() throws OXException {
        ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == configService) {
            throw ServiceExceptionCode.absentService(ConfigurationService.class);
        }
        return configService;
    }

    private static ConfigViewFactory getConfigViewFactory() throws OXException {
        ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }
        return viewFactory;
    }

    private static CapabilityService getCapabilityService() throws OXException {
        CapabilityService capabilityService = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
        if (null == capabilityService) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }
        return capabilityService;
    }

    /**
     * Checks if the capabilities associated with given session contain the named capabilities.
     *
     * @param session The session for which to check
     * @param capabilities The capabilities to check by
     * @return <code>true</code> if session-associated capabilities contain the denoted capabilities; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public static boolean hasCapabilities(Session session, String... capabilities) throws OXException {
        if (null == capabilities || 0 == capabilities.length) {
            return false;
        }

        CapabilitySet capabilitySet = getCapabilityService().getCapabilities(session);
        for (String capability : capabilities) {
            if (Strings.isEmpty(capability) || false == capabilitySet.contains(capability)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the capability set contain the specified capabilities.
     *
     * @param capabilitySet The capability set
     * @param capabilities The capabilities to check by
     * @return <code>true</code> if session-associated capabilities contain the denoted capabilities; otherwise <code>false</code>
     */
    public static boolean hasCapabilities(CapabilitySet capabilitySet, String... capabilities) {
        if (null == capabilities || 0 == capabilities.length) {
            return false;
        }

        for (String capability : capabilities) {
            if (Strings.isEmpty(capability) || false == capabilitySet.contains(capability)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parses a time span property.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return if property is absent
     * @param minimumValue The minimum allowed value
     * @param allowDisabling Whether it is allowed that the property is effectively disabled
     * @param configService The config service to use
     * @return The parsed time span
     * @throws OXException If parsing fails
     */
    public static long parseTimespanProperty(String propertyName, long defaultValue, long minimumValue, boolean allowDisabling, ConfigurationService configService) throws OXException  {
        String value = (null == configService ? getConfigurationService() : configService).getProperty(propertyName);
        if (Strings.isEmpty(value)) {
            return defaultValue;
        }

        long timespan;
        try {
            timespan = TimeSpanParser.parseTimespan(value).longValue();
        } catch (IllegalArgumentException e) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, propertyName);
        }

        if (allowDisabling && 0 >= timespan) {
            return 0;
        }

        if (0 < minimumValue && minimumValue > timespan) {
            LOG.warn("Ignoring too low value of \"{}\" for \"{}\", falling back to defaults.", value, propertyName);
            return defaultValue;
        }

        return timespan;
    }

    /**
     * Gets the value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param session The session from requesting user
     * @return The value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static String getValueFromProperty(String propertyName, String defaultValue, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        return Strings.isEmpty(value) ? defaultValue : value;
    }

    /**
     * Gets the value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param userId The user id
     * @param contextId The context id
     * @return The value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     */
    public static String getValueFromProperty(String propertyName, String defaultValue, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        return Strings.isEmpty(value) ? defaultValue : value;
    }

    /**
     * Gets the integer value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param session The session from requesting user
     * @return The integer value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static Integer getIntFromProperty(String propertyName, Integer defaultValue, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        try {
            String value = property.get();
            return Strings.isEmpty(value) ? defaultValue : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets the integer value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param userId The user id
     * @param contextId The context id
     * @return The integer value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     */
    public static Integer getIntFromProperty(String propertyName, Integer defaultValue, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        try {
            String value = property.get();
            return Strings.isEmpty(value) ? defaultValue : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets the long value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param session The session from requesting user
     * @return The long value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static Long getLongFromProperty(String propertyName, Long defaultValue, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        try {
            String value = property.get();
            return Strings.isEmpty(value) ? defaultValue : Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets the long value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param userId The user id
     * @param contextId The context id
     * @return The long value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     */
    public static Long getLongFromProperty(String propertyName, Long defaultValue, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        try {
            String value = property.get();
            return Strings.isEmpty(value) ? defaultValue : Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets the boolean value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param session The session from requesting user
     * @return The boolean value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static boolean getBoolFromProperty(String propertyName, boolean defaultValue, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            return defaultValue;
        }

        value = value.trim();
        return ("true".equalsIgnoreCase(value) ? true : ("false".equalsIgnoreCase(value) ? false : defaultValue));
    }

    /**
     * Gets the boolean value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param userId The user id
     * @param contextId The context id
     * @return The boolean value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     */
    public static boolean getBoolFromProperty(String propertyName, boolean defaultValue, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            return defaultValue;
        }

        value = value.trim();
        return ("true".equalsIgnoreCase(value) ? true : ("false".equalsIgnoreCase(value) ? false : defaultValue));
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a copy of compose context's source message
     *
     * @param composeContext The compose context
     * @return The copy
     * @throws OXException If a copy cannot be created
     */
    public static ComposedMailMessage copyOfSourceMessage(ComposeContext composeContext) throws OXException {
        ServerSession session = composeContext.getSession();
        ComposedMailMessage composedMail = composeContext.getProvider().getNewComposedMailMessage(session, session.getContext());
        ComposedMailMessage source = composeContext.getSourceMessage();
        if (source.containsFlags()) {
            composedMail.setFlags(source.getFlags());
        }
        if (source.containsThreadLevel()) {
            composedMail.setThreadLevel(source.getThreadLevel());
        }
        if (source.containsUserFlags()) {
            composedMail.addUserFlags(source.getUserFlags());
        }
        if (source.containsHeaders()) {
            composedMail.addHeaders(source.getHeaders());
        }
        if (source.containsFrom()) {
            composedMail.addFrom(source.getFrom());
        }
        if (source.containsTo()) {
            composedMail.addTo(source.getTo());
        }
        if (source.containsCc()) {
            composedMail.addCc(source.getCc());
        }
        if (source.containsBcc()) {
            composedMail.addBcc(source.getBcc());
        }
        if (source.containsReplyTo()) {
            composedMail.addReplyTo(source.getReplyTo());
        }
        if (source.containsDispositionNotification()) {
            composedMail.setDispositionNotification(source.getDispositionNotification());
        }
        if (source.containsDispositionNotification()) {
            composedMail.setDispositionNotification(source.getDispositionNotification());
        }
        if (source.containsPriority()) {
            composedMail.setPriority(source.getPriority());
        }
        if (source.containsColorLabel()) {
            composedMail.setColorLabel(source.getColorLabel());
        }
        if (source.containsAppendVCard()) {
            composedMail.setAppendVCard(source.isAppendVCard());
        }
        if (source.containsMsgref()) {
            composedMail.setMsgref(source.getMsgref());
        }
        if (source.containsSubject()) {
            composedMail.setSubject(source.getSubject());
        }
        if (source.containsSize()) {
            composedMail.setSize(source.getSize());
        }
        if (source.containsSentDate()) {
            composedMail.setSentDate(source.getSentDate());
        }
        if (source.containsReceivedDate()) {
            composedMail.setReceivedDate(source.getReceivedDate());
        }
        if (source.containsContentType()) {
            composedMail.setContentType(source.getContentType());
        }
        return composedMail;
    } // End of copyOfSourceMessage()

}
