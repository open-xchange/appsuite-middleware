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

    ApplicationPassword(String guid, String userInfo, String login, String appPassword, String fullPassword, String name, String appType) {
        super();
        this.appPassword = appPassword;
        this.fullPassword = fullPassword;
        this.appType = appType;
        this.name = name;
        this.userInfo = userInfo;
        this.login = login;
        this.guid = guid;
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

}
