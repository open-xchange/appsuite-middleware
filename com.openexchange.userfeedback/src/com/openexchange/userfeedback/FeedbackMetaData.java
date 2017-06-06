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

package com.openexchange.userfeedback;

/**
 * {@link FeedbackMetaData}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class FeedbackMetaData {

    private final String type;
    private final long date;
    private final int ctxId;
    private final int userId;
    private final String loginName;
    private final long typeId;
    private final String uiVersion;
    private final String serverVersion;

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

        private String type;
        private long date;
        private int ctxId;
        private int userId;
        private String loginName;
        private long typeId;
        private String uiVersion;
        private String serverVersion;

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
