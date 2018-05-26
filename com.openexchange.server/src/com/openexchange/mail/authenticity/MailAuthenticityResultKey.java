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

import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;

/**
 * {@link MailAuthenticityResultKey} - Defines the {@link MailAuthenticityResultKey}s
 * used in the core/default implementation of the {@link MailAuthenticityHandler} to record
 * the results of the evaluation of an e-mail and compile the {@link MailAuthenticityResult}.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum MailAuthenticityResultKey {

    /**
     * Defines the overall status of the result object
     */
    STATUS("status"),
    /**
     * Defines the domain of the 'From' header from the e-mail
     * which the authenticity was evaluated.
     */
    FROM_HEADER_DOMAIN("from_header_domain", false),
    /**
     * Defines the domain of the '*.from' attribute of a mechanism
     * result that is part of the response.
     */
    FROM_DOMAIN("from_domain"),
    /**
     * Defines if there is a domain mismatch between the domain of the
     * mechanism and the domain from the 'From' header
     */
    DOMAIN_MISMATCH("domain_mismatch"),
    /**
     * Defines the mechanism from which the authenticated from domain ({@link #FROM_DOMAIN})
     * was determined.
     */
    DOMAIN_MECH("domain_mech"),
    /**
     * Defines the {@link MailAuthenticityMechanismResult}s that were
     * collected during the evaluation process of the e-mail.
     */
    MAIL_AUTH_MECH_RESULTS("mailAuthenticityMechanismResults"),
    /**
     * Defines the unknown/unconsidered mail authenticity mechanism results
     * that were collected during the evaluation process of the e-mail.
     */
    UNCONSIDERED_AUTH_MECH_RESULTS("unconsideredAuthenticityMechanismResults"),
    /**
     * Defines the complete address of the trusted sender of the e-mail
     */
    TRUSTED_SENDER("trustedSender", false),
    /**
     * Contains a map of custom properties
     */
    CUSTOM_PROPERTIES("custom", false),
    /**
     * Defines the 'image' key for the response object
     */
    IMAGE("image");

    private final String key;
    private final boolean isVisible;

    /**
     * Initialises a new {@link DefaultMailAuthenticityResultKey}.
     */
    private MailAuthenticityResultKey(String key) {
        this(key, true);
    }

    /**
     * Initialises a new {@link DefaultMailAuthenticityResultKey}.
     */
    private MailAuthenticityResultKey(String key, boolean isVisible) {
        this.key = key;
        this.isVisible = isVisible;
    }

    /**
     * Returns the key
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * A flag indicating whether this attribute is visible or not
     *
     * @return
     */
    public boolean isVisible() {
        return isVisible;
    }
}
