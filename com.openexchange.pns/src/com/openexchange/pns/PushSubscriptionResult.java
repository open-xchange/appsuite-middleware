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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns;

import com.openexchange.exception.OXException;

/**
 * {@link PushSubscriptionResult} - The result of a call to {@link PushSubscriptionRegistry#registerSubscription(PushSubscription)}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class PushSubscriptionResult {

    /** The status for a subscription result */
    public static enum Status {
        /** Token registration was successful */
        OK,
        /** Token registration could not be performed as given token is already in use */
        CONFLICT,
        /** Token registration failed due to an error */
        FAIL;
    }

    /**
     * Creates a new builder instance
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builds an instance of <code>PushSubscriptionResult</code> */
    public static class Builder {

        private Status status;
        private int tokenUsingUserId;
        private int tokenUsingContextId;
        private OXException error;

        Builder() {
            super();
            tokenUsingUserId = -1;
            tokenUsingContextId = -1;
        }

        /**
         * Sets the OK status.
         *
         * @return This builder
         */
        public Builder withOkStatus() {
            this.status = Status.OK;
            return this;
        }

        /**
         * Sets the user and context identifier in case of a conflict
         *
         * @param tokenUsingUserId The identifier of the user using the token
         * @param tokenUsingContextId The identifier of the context using the token
         * @return This builder
         */
        public Builder withConflictingUserId(int tokenUsingUserId, int tokenUsingContextId) {
            this.status = Status.CONFLICT;
            this.tokenUsingUserId = tokenUsingUserId;
            this.tokenUsingContextId = tokenUsingContextId;
            return this;
        }

        /**
         * Sets the error
         *
         * @param error The error to set
         * @return This builder
         */
        public Builder withError(OXException error) {
            this.status = Status.FAIL;
            this.error = error;
            return this;
        }

        /**
         * Builds the instance of <code>PushSubscriptionResult</code> from this builder's arguments.
         *
         * @return The <code>PushSubscriptionResult</code> instance
         * @throws IllegalStateException If no arguments were set for this builder
         */
        public PushSubscriptionResult build() {
            if (null == status) {
                throw new IllegalStateException("No arguments set for this builder instance.");
            }
            return new PushSubscriptionResult(status, tokenUsingUserId, tokenUsingContextId, error);
        }
    }

    // ------------------------------------------------------------------------------------------------------------

    /** The constant result signaling successful token registration */
    public static final PushSubscriptionResult OK_RESULT = builder().withOkStatus().build();

    private final Status status;
    private final int tokenUsingUserId;
    private final int tokenUsingContextId;
    private final OXException error;

    /**
     * Initializes a new {@link PushSubscriptionResult}.
     */
    PushSubscriptionResult(Status status, int tokenUsingUserId, int tokenUsingContextId, OXException error) {
        super();
        this.status = status;
        this.tokenUsingUserId = tokenUsingUserId;
        this.tokenUsingContextId = tokenUsingContextId;
        this.error = error;
    }

    /**
     * Gets the status
     *
     * @return The status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the identifier of the user using the token.
     *
     * @return The user identifier or <code>-1</code>
     */
    public int getTokenUsingUserId() {
        return tokenUsingUserId;
    }

    /**
     * Gets the identifier of the context using the token.
     *
     * @return The context identifier or <code>-1</code>
     */
    public int getTokenUsingContextId() {
        return tokenUsingContextId;
    }

    /**
     * Gets the error
     *
     * @return The error or <code>null</code>
     */
    public OXException getError() {
        return error;
    }

}
