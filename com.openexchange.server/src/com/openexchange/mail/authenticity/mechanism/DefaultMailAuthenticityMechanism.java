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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.mechanism.MailAuthMech#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.mechanism.MailAuthMech#getResultType()
     */
    @Override
    public Class<? extends AuthenticityMechanismResult> getResultType() {
        return resultType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism#getTechnicalName()
     */
    @Override
    public String getTechnicalName() {
        return technicalName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism#getCode()
     */
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
