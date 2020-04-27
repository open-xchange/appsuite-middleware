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

package com.openexchange.authentication.application;

/**
 * {@link ApplicationPassword}
 * Class that contains the information required to authenticate a user providing a password
 * with limited authorization
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class ApplicationPassword {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an <code>ApplicationPassword</code> instance */
    public static final class Builder {

        private String appPassword;
        private String fullPassword;
        private String userInfo;
        private String login;
        private String uuid;
        private String appType;
        private String name;

        /**
         * Initializes a new Builder.
         */
        Builder() {
            super();
        }

        /**
         * Sets the app specific password
         *
         * @param password
         * @return Builder
         */
        public Builder setAppPassword(String appPassword) {
            this.appPassword = appPassword;
            return this;
        }

        /**
         * Sets the users regular password
         *
         * @param password
         * @return Builder
         */
        public Builder setFullPassword(String fullPassword) {
            this.fullPassword = fullPassword;
            return this;
        }

        /**
         * Sets the userId used for login
         *
         * @param userInfo
         * @return Builder
         */
        public Builder setUserInfo(String userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        /**
         * Sets the uuiid
         * 
         * @param guid
         * @return Builder
         */
        public Builder setUUID(String guid) {
            this.uuid = guid;
            return this;
        }

        /**
         * Sets the login string for the user
         * 
         * @param login
         * @return Builder
         */
        public Builder setLogin(String login) {
            this.login = login;
            return this;
        }

        /**
         * Sets the application type
         *
         * @param appType
         * @return Builder
         */
        public Builder setAppType(String appType) {
            this.appType = appType;
            return this;
        }

        /**
         * Sets the application name
         *
         * @param name
         * @return Builder
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Builds the LimitingPassword
         *
         * @return new ApplicationPassword
         */
        public ApplicationPassword build() {
            //@formatter:off
            return new ApplicationPassword(
                uuid,
                userInfo,
                login,
                appPassword,
                fullPassword,
                name,
                appType
            );
            //@formatter:on
        }

    }

    private final String appPassword;  // App specific password used for authentciation
    private final String fullPassword; // The users regular password
    private final String name;     // The name of the application
    private final String appType;   // The type of the application
    private final String userInfo;              // The userId for authenticating the user
    private String login;          // The login for the user
    private final String guid;          // Unique guid
    private boolean newLogin;        // True if the username is new for the user

    ApplicationPassword(String guid, String userInfo, String login, String appPassword, String fullPassword, String name, String appType) {
        super();
        this.appPassword = appPassword;
        this.fullPassword = fullPassword;
        this.appType = appType;
        this.name = name;
        this.userInfo = userInfo;
        this.login = login;
        this.guid = guid;
        this.newLogin = false;
    }

    /**
     * Returns the userId for the authentication entry
     * 
     * @return userId
     */
    public String getUserInfo() {
        return (this.userInfo == null ? getUserInfo(this.login) : this.userInfo);
    }

    /**
     * Returns the login string
     * 
     * @return login string
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * Gets the unique UUID of the password entry
     * 
     * @return uuid
     */
    public String getGUID() {
        return this.guid;
    }

    /**
     * Gets the users full password
     * 
     * @return the full password
     */
    public String getFullPassword() {
        return this.fullPassword;
    }

    /**
     * Gets the application specific password
     * 
     * @return application specific password
     */
    public String getAppPassword() {
        return this.appPassword;
    }

    /**
     * Get the userId from login string
     * 
     * @param userInfo
     * @return userId
     */
    private static String getUserInfo(String loginString) {
        if (loginString == null) {
            return null;
        }
        if (loginString.contains("@")) {
            return loginString.substring(0, loginString.indexOf("@"));
        }
        return loginString;
    }

    /**
     * Gets the type of the application
     *
     * @return the type of the application
     */
    public String getAppType() {
        return this.appType;
    }

    /**
     * Gets the name of the application
     * getName
     *
     * @return Name of the password
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the login
     * setLogin
     *
     * @param login
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Sets flag that this is a new/different login
     * setNewLogin
     *
     */
    public void setNewLogin() {
        this.newLogin = true;
    }

    /**
     * Returns true if the login is possibly not
     * the same as used for appsuite login
     * isNewLogin
     *
     * @return
     */
    public boolean isNewLogin() {
        return this.newLogin;
    }
}
