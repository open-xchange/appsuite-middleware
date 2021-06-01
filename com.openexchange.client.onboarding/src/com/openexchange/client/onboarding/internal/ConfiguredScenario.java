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

package com.openexchange.client.onboarding.internal;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.client.onboarding.Icon;
import com.openexchange.client.onboarding.OnboardingType;

/**
 * {@link ConfiguredScenario} - Represents a configured scenario parsed from appropriate .yml file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ConfiguredScenario {

    private final String id;
    private final boolean enabled;
    private final OnboardingType type;
    private final ConfiguredLink link;
    private final List<String> providerIds;
    private final List<String> alternativeIds;
    private final Icon icon;
    private final String displayName;
    private final String description;
    private final List<String> capabilities;
    private final AtomicBoolean warningLoggedForAbsentProvider;

    /**
     * Initializes a new {@link ConfiguredScenario}.
     */
    public ConfiguredScenario(String id, boolean enabled, OnboardingType type, ConfiguredLink link, List<String> providerIds, List<String> alternativeIds, String displayName, Icon icon, String description, List<String> capabilities) {
        super();
        this.id = id;
        this.enabled = enabled;
        this.type = type;
        this.link = link;
        this.providerIds = providerIds;
        this.alternativeIds = alternativeIds;
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
        this.capabilities = null == capabilities ? Collections.<String> emptyList() : Collections.<String> unmodifiableList(capabilities);
        warningLoggedForAbsentProvider = new AtomicBoolean(false);
    }

    /**
     * Check whether a WARN message was already logged that notifies about missing/absent providers associated with this configured scenario.
     *
     * @return <code>true</code> if already logged; otherwise <code>false</code>
     */
    public boolean logWarningForAbsentProvider() {
        return warningLoggedForAbsentProvider.compareAndSet(false, true);
    }

    /**
     * Gets the capabilities
     *
     * @return The capabilities
     */
    public List<String> getCapabilities() {
        return capabilities;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the enabled
     *
     * @return The enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public OnboardingType getType() {
        return type;
    }

    /**
     * Gets the link
     *
     * @return The link
     */
    public ConfiguredLink getLink() {
        return link;
    }

    /**
     * Gets the identifiers of associated providers
     *
     * @return The identifiers of associated providers
     */
    public List<String> getProviderIds() {
        return providerIds;
    }

    /**
     * Gets the identifiers for alternative scenarios.
     *
     * @return The identifiers for alternative scenarios
     */
    public List<String> getAlternativeIds() {
        return alternativeIds;
    }

    /**
     * Gets the icon
     *
     * @return The icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Gets the display name
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConfiguredScenario [");
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        builder.append("enabled=").append(enabled).append(", ");
        if (type != null) {
            builder.append("type=").append(type).append(", ");
        }
        if (providerIds != null) {
            builder.append("providerIds=").append(providerIds).append(", ");
        }
        if (alternativeIds != null) {
            builder.append("alternativeIds=").append(alternativeIds).append(", ");
        }
        if (icon != null) {
            builder.append("icon=").append(icon).append(", ");
        }
        if (displayName != null) {
            builder.append("displayName=").append(displayName).append(", ");
        }
        if (description != null) {
            builder.append("description=").append(description);
        }
        builder.append("]");
        return builder.toString();
    }

}
