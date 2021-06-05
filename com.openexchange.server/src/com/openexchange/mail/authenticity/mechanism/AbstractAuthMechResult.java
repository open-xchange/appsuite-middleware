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

package com.openexchange.mail.authenticity.mechanism;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCAuthMechResult;

/**
 * {@link AbstractAuthMechResult}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractAuthMechResult implements MailAuthenticityMechanismResult {

    private final String domain;
    private final String clientIP;
    private final AuthenticityMechanismResult result;
    private boolean domainMatch;
    private String reason;
    private final Map<String, String> properties;

    /**
     * Initialises a new {@link DMARCAuthMechResult}.
     *
     * @param domain The domain for which this mail authentication mechanism was applied to
     * @param clientIP The optional client IP used to send the e-mail
     * @param result The {@link AuthenticityMechanismResult}
     */
    public AbstractAuthMechResult(String domain, String clientIP, AuthenticityMechanismResult result) {
        super();
        this.domain = domain;
        this.clientIP = clientIP;
        this.result = result;
        properties = new HashMap<>();
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getClientIP() {
        return clientIP;
    }

    @Override
    public AuthenticityMechanismResult getResult() {
        return result;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Adds the specified property to the properties {@link Map}
     * 
     * @param key The name of the property
     * @param value The value of the property
     */
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason of the result
     *
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Gets the domainMatch
     *
     * @return The domainMatch
     */
    @Override
    public boolean isDomainMatch() {
        return domainMatch;
    }

    /**
     * Sets the domainMatch
     *
     * @param domainMatch The domainMatch to set
     */
    public void setDomainMatch(boolean domainMatch) {
        this.domainMatch = domainMatch;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientIP == null) ? 0 : clientIP.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + (domainMatch ? 1231 : 1237);
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractAuthMechResult other = (AbstractAuthMechResult) obj;
        if (clientIP == null) {
            if (other.clientIP != null) {
                return false;
            }
        } else if (!clientIP.equals(other.clientIP)) {
            return false;
        }
        if (domain == null) {
            if (other.domain != null) {
                return false;
            }
        } else if (!domain.equals(other.domain)) {
            return false;
        }
        if (domainMatch != other.domainMatch) {
            return false;
        }
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        if (reason == null) {
            if (other.reason != null) {
                return false;
            }
        } else if (!reason.equals(other.reason)) {
            return false;
        }
        if (result == null) {
            if (other.result != null) {
                return false;
            }
        } else if (!result.equals(other.result)) {
            return false;
        }
        return true;
    }
}
