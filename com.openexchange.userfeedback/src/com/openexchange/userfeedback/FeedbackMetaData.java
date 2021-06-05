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

package com.openexchange.userfeedback;

/**
 * {@link FeedbackMetaData}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class FeedbackMetaData {

    final String type;
    final long date;
    final int ctxId;
    final int userId;
    final String loginName;
    final long typeId;
    final String uiVersion;
    final String serverVersion;

    /**
     * Initializes a new {@link FeedbackMetaData}.
     *
     * @param type The feedback type
     * @param date The date of the feedback
     * @param ctxId The context id
     * @param userId The user id
     * @param loginName The login name
     * @param typeId The feedback id
     * @param uiVersion The ui version
     * @param serverVersion The server version
     */
    FeedbackMetaData(String type, long date, int ctxId, int userId, String loginName, long typeId, String uiVersion, String serverVersion) {
        super();
        this.type = type;
        this.date = date;
        this.ctxId = ctxId;
        this.userId = userId;
        this.loginName = loginName;
        this.typeId = typeId;
        this.uiVersion = uiVersion;
        this.serverVersion = serverVersion;
    }

    protected FeedbackMetaData(Builder builder) {
        this(builder.type, builder.date, builder.ctxId, builder.userId, builder.loginName, builder.typeId, builder.uiVersion, builder.serverVersion);
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the date
     *
     * @return The date
     */
    public long getDate() {
        return date;
    }

    /**
     * Gets the ctxId
     *
     * @return The ctxId
     */
    public int getCtxId() {
        return ctxId;
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the loginName
     *
     * @return The loginName
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Gets the typeId
     *
     * @return The typeId
     */
    public long getTypeId() {
        return typeId;
    }

    public String getUiVersion() {
        return uiVersion;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * Creates a new builder instance.
     *
     * @return The builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an <code>FeedbackMetaData</code> instance */
    public static class Builder {

        String type;
        long date;
        int ctxId;
        int userId;
        String loginName;
        long typeId;
        String uiVersion;
        String serverVersion;

        Builder() {
            super();
        }

        Builder(FeedbackMetaData meta) {
            type = meta.type;
            date = meta.date;
            ctxId = meta.ctxId;
            userId = meta.userId;
            loginName = meta.loginName;
            typeId = meta.typeId;
            uiVersion = meta.uiVersion;
            serverVersion = meta.serverVersion;
        }

        /**
         * Sets the feedback type
         *
         * @param type The type to set
         * @return This builder
         */
        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the date
         *
         * @param date The date to set
         * @return This builder
         */
        public Builder setDate(long date) {
            this.date = date;
            return this;
        }

        /**
         * Sets the context id
         *
         * @param type The ctxId to set
         * @return This builder
         */
        public Builder setCtxId(int ctxId) {
            this.ctxId = ctxId;
            return this;
        }

        /**
         * Sets the user id
         *
         * @param type The user id to set
         * @return This builder
         */
        public Builder setUserId(int userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the loginName
         *
         * @param type The loginName to set
         * @return This builder
         */
        public Builder setLoginName(String loginName) {
            this.loginName = loginName;
            return this;
        }

        /**
         * Sets the feedback type id
         *
         * @param type The type id to set
         * @return This builder
         */
        public Builder setTypeId(long typeId) {
            this.typeId = typeId;
            return this;
        }

        /**
         * Sets the UI Version
         *
         * @param type The uiVersion to set
         * @return This builder
         */
        public Builder setUiVersion(String uiVersion) {
            this.uiVersion = uiVersion;
            return this;
        }

        /**
         * Sets the Server Version
         *
         * @param type The serverVersion to set
         * @return This builder
         */
        public Builder setServerVersion(String serverVersion) {
            this.serverVersion = serverVersion;
            return this;
        }

        /**
         * Creates the <code>FeedbackMetaData</code> instance from this builder's arguments.
         *
         * @return The <code>FeedbackMetaData</code> instance
         */
        public FeedbackMetaData build() {
            return new FeedbackMetaData(type, date, ctxId, userId, loginName, typeId, uiVersion, serverVersion);
        }
    }

}
