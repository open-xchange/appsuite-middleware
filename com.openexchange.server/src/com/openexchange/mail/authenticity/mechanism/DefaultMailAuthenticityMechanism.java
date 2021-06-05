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

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.mail.authenticity.MailAuthenticityAttribute;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;

/**
 * {@link DefaultMailAuthenticityMechanism} - The default supported {@link MailAuthenticityMechanism}s
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum DefaultMailAuthenticityMechanism implements MailAuthenticityMechanism {

    DMARC("DMARC", "dmarc", DMARCResult.class),
    DKIM("DKIM", "dkim", DKIMResult.class),
    SPF("SPF", "spf", SPFResult.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMailAuthenticityMechanism.class);

    private final Class<? extends AuthenticityMechanismResult> resultType;
    private final String displayName;
    private final String technicalName;

    /**
     * Initializes a new {@link DefaultMailAuthenticityMechanism}.
     */
    private DefaultMailAuthenticityMechanism(String displayName, String technicalName, Class<? extends AuthenticityMechanismResult> resultType) {
        this.displayName = displayName;
        this.technicalName = technicalName;
        this.resultType = resultType;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Class<? extends AuthenticityMechanismResult> getResultType() {
        return resultType;
    }

    @Override
    public String getTechnicalName() {
        return technicalName;
    }

    @Override
    public int getCode() {
        return ordinal();
    }

    /**
     * Converts the specified string to a {@link DefaultMailAuthenticityMechanism}
     *
     * @param s The string to convert
     * @return the converted {@link DefaultMailAuthenticityMechanism}
     */
    public static DefaultMailAuthenticityMechanism parse(String s) {
        int index = s.indexOf(' ');
        if (index > 0) {
            s = s.substring(0, index);
        }
        try {
            return DefaultMailAuthenticityMechanism.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Unknown mail authenticity mechanism '{}'", s);
        }
        return null;
    }

    /**
     * Tries to extract a known mechanism from the specified attributes
     *
     * @param attributes The attributes
     * @return The {@link DefaultMailAuthenticityMechanism} or <code>null</code> if none exists
     */
    public static DefaultMailAuthenticityMechanism extractMechanism(Map<String, String> attributes) {
        for (DefaultMailAuthenticityMechanism mechanism : DefaultMailAuthenticityMechanism.values()) {
            if (attributes.containsKey(mechanism.name().toLowerCase())) {
                return mechanism;
            }
        }
        return null;
    }

    public static DefaultMailAuthenticityMechanism extractMechanism(List<MailAuthenticityAttribute> attributes) {
        // Mechanism is always the first element in the List
        MailAuthenticityAttribute attribute = attributes.get(0);
        if (attribute == null) {
            return null;
        }
        for (DefaultMailAuthenticityMechanism mechanism : DefaultMailAuthenticityMechanism.values()) {
            if (attribute.getKey().equals(mechanism.name().toLowerCase())) {
                return mechanism;
            }
        }
        return null;
    }
}
