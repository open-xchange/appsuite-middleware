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
            } catch (NumberFormatException e) {
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
