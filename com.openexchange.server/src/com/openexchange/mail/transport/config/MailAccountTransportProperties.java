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

package com.openexchange.mail.transport.config;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailAccountTransportProperties} - Transport properties read from mail account with fallback to properties read from properties
 * file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountTransportProperties implements ITransportProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccountTransportProperties.class);

    protected Boolean enforceSecureConnection;
    protected final Map<String, String> properties;

    /**
     * Initializes a new {@link MailAccountTransportProperties}.
     *
     * @param mailAccount The mail account providing the properties
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountTransportProperties(MailAccount mailAccount) {
        super();
        if (null == mailAccount) {
            throw new IllegalArgumentException("mail account is null.");
        }
        properties = mailAccount.getProperties();
    }

    /**
     * Initializes a new {@link MailAccountTransportProperties} with empty properties.
     */
    protected MailAccountTransportProperties() {
        super();
        properties = new HashMap<String, String>(0);
    }

    /**
     * Looks-up the denoted property.
     *
     * @param name The property name
     * @return The looked-up value or <code>null</code>
     */
    protected String lookUpProperty(String name) {
        return lookUpProperty(name, null);
    }

    /**
     * Looks-up the denoted property.
     *
     * @param name The property name
     * @param defaultValue The default value to return if absent
     * @return The looked-up value or given <code>defaultValue</code>
     */
    protected String lookUpProperty(String name, String defaultValue) {
        ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        return null == service ? defaultValue : service.getProperty(name, defaultValue);
    }

    @Override
    public int getReferencedPartLimit() {
        final String referencedPartLimitStr = properties.get("com.openexchange.mail.transport.referencedPartLimit");
        if (null == referencedPartLimitStr) {
            return TransportProperties.getInstance().getReferencedPartLimit();
        }

        try {
            return Integer.parseInt(referencedPartLimitStr);
        } catch (final NumberFormatException e) {
            LOG.error("Referenced Part Limit: Invalid value.", e);
            return TransportProperties.getInstance().getReferencedPartLimit();
        }
    }

    @Override
    public boolean isEnforceSecureConnection() {
        Boolean b = this.enforceSecureConnection;
        if (null != b) {
            return b.booleanValue();
        }
        return TransportProperties.getInstance().isEnforceSecureConnection();
    }

    @Override
    public void setEnforceSecureConnection(boolean enforceSecureConnection) {
        this.enforceSecureConnection = Boolean.valueOf(enforceSecureConnection);
    }

}
