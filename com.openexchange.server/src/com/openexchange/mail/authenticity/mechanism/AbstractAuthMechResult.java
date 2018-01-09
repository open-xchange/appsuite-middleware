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
    public AbstractAuthMechResult(final String domain, final String clientIP, final AuthenticityMechanismResult result) {
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult#getProperties()
     */
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
    public void addProperty(final String key, final String value) {
        properties.put(key, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mail.authenticity.common.mechanism.MailAuthenticityMechanismResult#getReason()
     */
    @Override
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason of the result
     *
     * @param reason the reason to set
     */
    public void setReason(final String reason) {
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
    public void setDomainMatch(final boolean domainMatch) {
        this.domainMatch = domainMatch;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
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
