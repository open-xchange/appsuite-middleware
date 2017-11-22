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

package com.openexchange.mail.dataobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.openexchange.mail.authenticity.common.MailAuthenticationStatus;
import com.openexchange.mail.authenticity.common.mechanism.MailAuthenticationMechanism;
import com.openexchange.mail.authenticity.common.mechanism.MailAuthenticationMechanismResult;

/**
 * {@link MailAuthenticationResult} - The result of the overall mail authentication
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailAuthenticationResult {

    /** The empty neutral authentication result */
    public static MailAuthenticationResult NEUTRAL_RESULT = builder().build();

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder or instance of <code>MailAuthenticationResult</code> */
    public static class Builder {

        private MailAuthenticationStatus status;
        private String domain;
        private final List<MailAuthenticationMechanism> mailAuthenticationMechanisms;
        private final List<MailAuthenticationMechanismResult> mailAuthenticationMechanismResults;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
            status = MailAuthenticationStatus.NEUTRAL;
            mailAuthenticationMechanismResults = new ArrayList<>();
            mailAuthenticationMechanisms = new ArrayList<>();
        }

        /**
         * Adds the specified {@link MailAuthenticationMechanismResult} to the overall result {@link Set}
         *
         * @param result The {@link MailAuthenticationMechanismResult} to add
         * @return This builder
         */
        public Builder addResult(MailAuthenticationMechanismResult result) {
            mailAuthenticationMechanisms.add(result.getMechanism());
            mailAuthenticationMechanismResults.add(result);
            return this;
        }

        /**
         * Sets the domain
         *
         * @param domain The domain to set
         * @return This builder
         */
        public Builder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        /**
         * Sets the status of the entire mail authentication
         *
         * @param status The status to set
         * @return This builder
         */
        public Builder setStatus(MailAuthenticationStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Builds the {@link MailAuthenticationResult}
         * 
         * @return the built {@link MailAuthenticationResult}
         */
        public MailAuthenticationResult build() {
            return new MailAuthenticationResult(status, domain, ImmutableList.copyOf(mailAuthenticationMechanisms), ImmutableList.copyOf(mailAuthenticationMechanismResults));
        }

    }

    // ---------------------------------------------------------------------------------------------------------

    private final MailAuthenticationStatus status;
    private final String domain;
    private final List<MailAuthenticationMechanism> mailAuthenticationMechanisms;
    private final List<MailAuthenticationMechanismResult> mailAuthenticationMechanismResults;

    /**
     * Initializes a new {@link MailAuthenticationResult}.
     */
    MailAuthenticationResult(MailAuthenticationStatus status, String domain, ImmutableList<MailAuthenticationMechanism> mailAuthenticationMechanisms, ImmutableList<MailAuthenticationMechanismResult> mailAuthenticationMechanismResults) {
        super();
        this.status = status;
        this.domain = domain;
        this.mailAuthenticationMechanisms = mailAuthenticationMechanisms;
        this.mailAuthenticationMechanismResults = mailAuthenticationMechanismResults;
    }

    /**
     * Gets the domain
     *
     * @return The domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Gets the status of the entire mail authentication
     *
     * @return The status
     */
    public MailAuthenticationStatus getStatus() {
        return status;
    }

    /**
     * Returns an unmodifiable {@link List} with the used mail authentication mechanisms
     *
     * @return an unmodifiable {@link List} with the used mail authentication mechanisms
     */
    public List<MailAuthenticationMechanism> getAuthenticationMechanisms() {
        return mailAuthenticationMechanisms;
    }

    /**
     * Returns an unmodifiable {@link List} with the results of the used mail authentication mechanisms
     *
     * @return an unmodifiable {@link List} with the results of the used mail authentication mechanisms
     */
    public List<MailAuthenticationMechanismResult> getMailAuthenticationMechanismResults() {
        return mailAuthenticationMechanismResults;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MailAuthenticationResult [status=").append(status).append(", domain=").append(domain).append(", mailAuthenticationMechanisms=");
        builder.append(mailAuthenticationMechanisms).append(", mailAuthenticationMechanismResults=").append(mailAuthenticationMechanismResults).append("]");
        return builder.toString();
    }
}
