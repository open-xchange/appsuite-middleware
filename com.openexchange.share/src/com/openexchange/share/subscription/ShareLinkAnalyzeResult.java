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

package com.openexchange.share.subscription;

import com.openexchange.exception.OXException;

/**
 * {@link ShareLinkAnalyzeResult}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public final class ShareLinkAnalyzeResult {

    private final ShareLinkState state;
    private final OXException error;
    private final ShareSubscriptionInformation infos;

    /**
     * Initializes a new {@link ShareLinkAnalyzeResult}.
     * 
     * @param state The state of the result
     * @param infos Detailed information about the share
     */
    public ShareLinkAnalyzeResult(ShareLinkState state, ShareSubscriptionInformation infos) {
        this(state, null, infos);
    }

    /**
     * Initializes a new {@link ShareLinkAnalyzeResult}.
     * 
     * @param state The state of the result
     * @param error The details about the state as {@link OXException}
     * @param infos Detailed information about the share
     */
    public ShareLinkAnalyzeResult(ShareLinkState state, OXException error, ShareSubscriptionInformation infos) {
        super();
        this.state = state;
        this.error = error;
        this.infos = infos;
    }

    ShareLinkAnalyzeResult(Builder builder) {
        this.state = builder.state;
        this.error = builder.error;
        this.infos = builder.infos;
    }

    /**
     * Gets the state
     *
     * @return The state
     */
    public ShareLinkState getState() {
        return state;
    }

    /**
     * Further details of the state
     *
     * @return Details as {@link OXException}, might be <code>null</code>
     */
    public OXException getDetails() {
        return error;
    }

    /**
     * Gets the infos
     *
     * @return The infos
     */
    public ShareSubscriptionInformation getInfos() {
        return infos;
    }

    @Override
    public String toString() {
        return "ShareLinkAnalyzeResult [state=" + state + ", error=" + (null == error ? "null" : error.getMessage()) + ", infos=" + (null != infos ? infos.toString() : "null") + "]";
    }

    /**
     * {@link Builder}
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.5
     */
    public static class Builder {

        ShareLinkState state;
        OXException error;
        ShareSubscriptionInformation infos;

        /**
         * Initializes a new {@link Builder}.
         */
        public Builder() {}

        /**
         * Initializes a new {@link Builder}.
         * 
         * @param state The state of the result
         * @param error The detailed error about the state as {@link OXException}
         * @param infos Detailed information about the share
         */
        public Builder(ShareLinkState state, OXException error, ShareSubscriptionInformation infos) {
            this.state = state;
            this.error = error;
            this.infos = infos;
        }

        /**
         * Add the state
         *
         * @param state The state
         * @return This instance for chaining
         */
        public Builder state(ShareLinkState state) {
            this.state = state;
            return Builder.this;
        }

        /**
         * Add a detailed error message to the state
         *
         * @param error The details
         * @return This instance for chaining
         */
        public Builder error(OXException error) {
            this.error = error;
            return Builder.this;
        }

        /**
         * Add the infos
         *
         * @param infos The infos
         * @return This instance for chaining
         */
        public Builder infos(ShareSubscriptionInformation infos) {
            this.infos = infos;
            return Builder.this;
        }

        /**
         * Builds the result
         *
         * @return The analyze result
         */
        public ShareLinkAnalyzeResult build() {

            return new ShareLinkAnalyzeResult(this);
        }

    }

}
