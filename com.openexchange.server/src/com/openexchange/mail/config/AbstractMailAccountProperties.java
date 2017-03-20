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

package com.openexchange.mail.config;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link AbstractMailAccountProperties}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class AbstractMailAccountProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractMailAccountProperties.class);

    /** The properties grabbed from mail account */
    protected final Map<String, String> properties;

    /** The flag signaling if there are any account properties available */
    protected final boolean hasAccountProperties;

    /** The user identifier */
    protected final int userId;

    /** The context identifier */
    protected final int contextId;

    /**
     * Initializes a new {@link AbstractMailAccountProperties}.
     */
    protected AbstractMailAccountProperties(MailAccount mailAccount, int userId, int contextId) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        if (null == mailAccount) {
            properties = new HashMap<String, String>(0);
            hasAccountProperties = false;
        } else {
            properties = mailAccount.getProperties();
            hasAccountProperties = null != properties && !properties.isEmpty();
        }
    }

    /**
     * Gets the value for named property from {@link #properties account properties}.
     *
     * @param name The name to look-up
     * @return The value or <code>null</code>
     */
    protected String getAccountProperty(String name) {
        return hasAccountProperties ? properties.get(name) : null;
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
    protected int lookUpIntProperty(String name, int defaultValue) {
        String value = hasAccountProperties ? properties.get(name) : null;
        if (null != value) {
            try {
                return Integer.parseInt(value.trim());
            } catch (final NumberFormatException e) {
                LOG.error("Non parseable integer value for property {}: {}", name, value, e);
            }
        }

        ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null != viewFactory) {
            try {
                ConfigView view = viewFactory.getView(userId, contextId);
                value = ConfigViews.getNonEmptyPropertyFrom(name, view);
                if (null == value) {
                    return defaultValue;
                }

                try {
                    return Integer.parseInt(value.trim());
                } catch (NumberFormatException e) {
                    LOG.error("Non parseable integer value for property {}: {}", name, value, e);
                }
            } catch (OXException e) {
                LOG.error("Failed to query property {} from config-cascade for user {} in context {}", name, I(userId), I(contextId), e);
            }
        }

        return defaultValue;
    }

    /**
     * Looks-up the denoted property.
     *
     * @param name The property name
     * @param defaultValue The default value to return if absent
     * @return The looked-up value or given <code>defaultValue</code>
     */
    protected char lookUpCharProperty(String name, char defaultValue) {
        String value = hasAccountProperties ? properties.get(name) : null;
        if (null != value) {
            return value.trim().charAt(0);
        }

        ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null != viewFactory) {
            try {
                ConfigView view = viewFactory.getView(userId, contextId);
                value = ConfigViews.getNonEmptyPropertyFrom(name, view);
                if (null == value) {
                    return defaultValue;
                }

                value = value.trim();
                if (value.length() <= 0) {
                    return defaultValue;
                }

                return value.charAt(0);
            } catch (OXException e) {
                LOG.error("Failed to query property {} from config-cascade for user {} in context {}", name, I(userId), I(contextId), e);
            }
        }

        return defaultValue;
    }

    /**
     * Looks-up the denoted property.
     *
     * @param name The property name
     * @param defaultValue The default value to return if absent
     * @return The looked-up value or given <code>defaultValue</code>
     */
    protected boolean lookUpBoolProperty(String name, boolean defaultValue) {
        String value = hasAccountProperties ? properties.get(name) : null;
        if (null != value) {
            return Boolean.parseBoolean(value.trim());
        }

        ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null != viewFactory) {
            try {
                ConfigView view = viewFactory.getView(userId, contextId);
                value = ConfigViews.getNonEmptyPropertyFrom(name, view);
                if (null == value) {
                    return defaultValue;
                }

                return Boolean.parseBoolean(value.trim());
            } catch (OXException e) {
                LOG.error("Failed to query property {} from config-cascade for user {} in context {}", name, I(userId), I(contextId), e);
            }
        }

        return defaultValue;
    }

    /**
     * Looks-up the denoted property.
     *
     * @param name The property name
     * @param defaultValue The default value to return if absent
     * @return The looked-up value or given <code>defaultValue</code>
     */
    protected String lookUpProperty(String name, String defaultValue) {
        String value = hasAccountProperties ? properties.get(name) : null;
        if (null != value) {
            return value;
        }

        ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null != viewFactory) {
            try {
                ConfigView view = viewFactory.getView(userId, contextId);
                value = ConfigViews.getNonEmptyPropertyFrom(name, view);
                return null == value ? defaultValue : value;
            } catch (OXException e) {
                LOG.error("Failed to query property {} from config-cascade for user {} in context {}", name, I(userId), I(contextId), e);
            }
        }

        return defaultValue;
    }

}
