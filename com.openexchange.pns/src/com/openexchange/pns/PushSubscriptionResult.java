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
