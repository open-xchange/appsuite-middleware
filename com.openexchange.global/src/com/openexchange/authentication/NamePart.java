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

package com.openexchange.authentication;

/**
 * {@link NamePart} denotes the part of a name, provided as user input or
 * returned by the authorization server, that shall be used to determine
 * the name or identifier of a user or context.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public enum NamePart {

    /**
     * The full string as provided by a user or returned by the authorization server.
     */
    FULL("full"),
    /**
     * The local part of an email address (<code>local-part@domain</code>),
     * if the provided name matches such. In case the name does not match
     * an email address, the full string is taken.
     */
    LOCAL_PART("local-part"),
    /**
     * The domain part of an email address (<code>local-part@domain</code>),
     * if the provided name matches such. In case the name does not match
     * an email address, a default is taken (e.g. <code>defaultcontext</code>
     * for context mappings).
     */
    DOMAIN("domain");

    private final String configName;

    private NamePart(String configName) {
        this.configName = configName;
    }

    /**
     * Gets the name of this part as it would be defined in a configuration property.
     * 
     * @return The configuration name
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * Extracts this part from the given name. In case the part is {@link NamePart#DOMAIN}
     * but no domain part exists, a default value is returned.
     *
     * @param name The name
     * @param defaultDomain The default domain
     * @return The name part
     * @throws IllegalArgumentException If no extraction rule was implemented for this part
     */
    public String getFrom(String name, String defaultDomain) {
        switch (this) {
            case FULL:
                return name;
            case LOCAL_PART: {
                int pos = name.lastIndexOf('@');
                return pos < 0 ? name : name.substring(0, pos);
            }
            case DOMAIN: {
                int pos = name.lastIndexOf('@');
                String domain = pos < 0 ? defaultDomain : name.substring(pos + 1);
                if (domain.length() == 0) {
                    return defaultDomain;
                }

                return domain;
            }
            default:
                throw new IllegalArgumentException("Invalid name part: " + this.name());
        }
    }

    /**
     * Gets the {@link NamePart} for a given config name or <code>null</code> if none matches.
     * 
     * @param configName The configuration name
     * @return A {@link NamePart}
     */
    public static NamePart of(String configName) {
        for (NamePart value : NamePart.values()) {
            if (value.configName.equals(configName)) {
                return value;
            }
        }

        return null;
    }

}
