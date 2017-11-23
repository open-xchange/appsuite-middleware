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
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;

/**
 * {@link MailAuthenticityResult} - The result of the overall mail authenticity validation.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailAuthenticityResult {

    /** The empty neutral authenticity result */
    public static MailAuthenticityResult NEUTRAL_RESULT = builder().build();

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder or instance of {@link MailAuthenticityResult} */
    public static class Builder {

        private MailAuthenticityStatus status;
        private String domain;
        private final List<MailAuthenticityMechanism> mailAuthenticityMechanisms;
        private final List<MailAuthenticityMechanismResult> mailAuthenticityMechanismResults;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
            status = MailAuthenticityStatus.NEUTRAL;
            mailAuthenticityMechanismResults = new ArrayList<>();
            mailAuthenticityMechanisms = new ArrayList<>();
        }

        /**
         * Adds the specified {@link MailAuthenticityMechanismResult} to the overall result {@link Set}
         *
         * @param result The {@link MailAuthenticityMechanismResult} to add
         * @return This builder
         */
        public Builder addResult(MailAuthenticityMechanismResult result) {
            if (!mailAuthenticityMechanisms.contains(result.getMechanism())) {
                mailAuthenticityMechanisms.add(result.getMechanism());
            }
            mailAuthenticityMechanismResults.add(result);
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
         * Sets the status of the entire mail authenticity
         *
         * @param status The status to set
         * @return This builder
         */
        public Builder setStatus(MailAuthenticityStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Builds the {@link MailAuthenticityResult}
         *
         * @return the built {@link MailAuthenticityResult}
         */
        public MailAuthenticityResult build() {
            return new MailAuthenticityResult(status, domain, ImmutableList.copyOf(mailAuthenticityMechanisms), ImmutableList.copyOf(mailAuthenticityMechanismResults));
        }

    }

    // ---------------------------------------------------------------------------------------------------------

    private final MailAuthenticityStatus status;
    private final String domain;
    private final List<MailAuthenticityMechanism> mailAuthenticityMechanisms;
    private final List<MailAuthenticityMechanismResult> mailAuthenticityMechanismResults;

    /**
     * Initializes a new {@link MailAuthenticityResult}.
     */
    MailAuthenticityResult(MailAuthenticityStatus status, String domain, ImmutableList<MailAuthenticityMechanism> mailAuthenticityMechanisms, ImmutableList<MailAuthenticityMechanismResult> mailAuthenticityMechanismResults) {
        super();
        this.status = status;
        this.domain = domain;
        this.mailAuthenticityMechanisms = mailAuthenticityMechanisms;
        this.mailAuthenticityMechanismResults = mailAuthenticityMechanismResults;
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
     * Gets the status of the entire mail authenticity
     *
     * @return The status
     */
    public MailAuthenticityStatus getStatus() {
        return status;
    }

    /**
     * Returns an unmodifiable {@link List} with the used mail authenticity mechanisms
     *
     * @return an unmodifiable {@link List} with the used mail authenticity mechanisms
     */
    public List<MailAuthenticityMechanism> getAuthenticityMechanisms() {
        return mailAuthenticityMechanisms;
    }

    /**
     * Returns an unmodifiable {@link List} with the results of the used mail authenticity mechanisms
     *
     * @return an unmodifiable {@link List} with the results of the used mail authenticity mechanisms
     */
    public List<MailAuthenticityMechanismResult> getMailAuthenticityMechanismResults() {
        return mailAuthenticityMechanismResults;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MailAuthenticityResult [status=").append(status).append(", domain=").append(domain).append(", mailAuthenticityMechanisms=");
        builder.append(mailAuthenticityMechanisms).append(", mailAuthenticityMechanismResults=").append(mailAuthenticityMechanismResults).append("]");
        return builder.toString();
    }
}
