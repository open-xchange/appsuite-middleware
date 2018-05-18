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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.mail.authenticity;

import com.openexchange.config.lean.Property;

/**
 * {@link MailAuthenticityProperty} - Properties for mail authenticity validation.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum MailAuthenticityProperty implements Property {
    /**
     * Defines whether the mail authenticity core feature is enabled
     * <p>
     * Defaults to <code>false</code>
     */
    ENABLED("enabled", Boolean.FALSE),
    /**
     * Defines the date after which the e-mails will be analyzed
     * <p>
     * Defaults to 0
     */
    THRESHOLD("threshold", Long.valueOf(0)),
    /**
     * Defines the MANDATORY <code>authserv-id</code>. It can contain a single arbitrary string
     * or a comma separated list of arbitrary strings
     * <p>
     * Default is empty.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.2">RFC-7601, Section 2.2</a>
     */
    AUTHSERV_ID("authServId", ""),
    /**
     * <p>Defines whether the raw headers of a message will be logged in DEBUG level.</p>
     * 
     * <p>Defaults to <code>false</code></p>
     */
    LOG_RAW_HEADERS("logRawHeaders", Boolean.FALSE),
    /**
     * <p>
     * Defines whether the policy of the DMARC mechanism will be considered when determining
     * the overall result. If this property is enabled, then in case the DMARC mechanism defines
     * a policy and that policy is 'NONE', then the DMARC mechanism result will not be taken
     * into consideration and will not influence the overall result.
     * </p>
     * 
     * <p>Defaults to <code>false</code></p>
     */
    CONSIDER_DMARC_POLICY("considerDMARCPolicy", Boolean.FALSE);

    private final Object defaultValue;
    private final String fqn;

    /**
     * Initializes a new {@link MailAuthenticityProperty}.
     */
    private MailAuthenticityProperty(String suffix, Object defaultValue) {
        this.defaultValue = defaultValue;
        fqn = "com.openexchange.mail.authenticity." + suffix;
    }

    /**
     * Gets the fully qualified name for the property
     *
     * @return the fully qualified name for the property
     */
    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    /**
     * Gets the default value of this property
     *
     * @return the default value of this property
     */
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
