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

package com.openexchange.user;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import com.openexchange.groupware.contexts.FileStorageInfo;
import com.openexchange.java.Strings;

/**
 * Interface for the user object.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface User extends FileStorageInfo, Serializable {

    /**
     * Getter for userPassword.
     * @return Password.
     */
    String getUserPassword();

    /**
     * @return the mechanism for encrypting the password.
     */
    String getPasswordMech();

    /**
     * @return the salt used for encrypting the password.
     */
    byte[] getSalt();

    /**
     * Getter for uid.
     * @return User identifier.
     */
    int getId();

    /**
     * Gets the creators user id if this user is a guest.
     * @return The user id of the creator or <code>0</code> if this instance is a fully fledged user.
     */
    int getCreatedBy();

    /**
     * Returns if this user is a guest.
     * @return <code>true</code> if so, otherwise <code>false</code>.
     */
    boolean isGuest();

    /**
     * Getter for mailEnabled.
     * @return <code>true</code> if user is enabled.
     */
    boolean isMailEnabled();

    /**
     * Getter for shadowLastChange.
     * @return Days since Jan 1, 1970 that password was last changed.
     */
    int getShadowLastChange();

    /**
     * Getter for imapServer.
     * @return IMAP server.
     */
    String getImapServer();

    /**
     * Returns the login for the imap server. This field must not be filled.
     * Check IMAP configuration.
     * @return the login for the imap server.
     */
    String getImapLogin();

    /**
     * Getter for smtpServer.
     * @return SMTP server.
     */
    String getSmtpServer();

    /**
     * Getter for mailDomain.
     * @return mail domain.
     */
    String getMailDomain();

    /**
     * Getter for givenName.
     * @return given name.
     */
    String getGivenName();

    /**
     * Getter for sure name.
     * @return sure name.
     */
    String getSurname();

    /**
     * Getter for mail.
     * @return mail address.
     */
    String getMail();

    /**
     * @return mail aliases.
     */
    String[] getAliases();

    /**
     * Gets the user attributes as an unmodifiable map.
     * <p>
     * Each attribute may point to multiple values.
     *
     * @return user attributes
     */
    Map<String, String> getAttributes();

    /**
     * Getter for displayName.
     * @return Display name.
     */
    String getDisplayName();

    /**
     * Getter for timeZone.
     * @return Timezone.
     */
    String getTimeZone();

    /**
     * Getter for preferredLanguage. The preferred language of the user.
     * According to RFC 2798 and 2068 it should be something like de-de, en-gb
     * or en.
     *
     * @return Preferred Language.
     */
    String getPreferredLanguage();

    /**
     * Getter for locale constructed from set preferred language.
     *
     * @see #getPreferredLanguage()
     * @return Locale
     */
    Locale getLocale();

    /**
     * Getter for groups.
     *
     * @return the groups this user is member of.
     */
    int[] getGroups();

    /**
     * @return the contactId
     */
    int getContactId();

    /**
     * Gets the identifier for the owner of the file storage associated with this user.
     * <p>
     * Provided that {@link #getFilestoreId()} returns a positive, valid file storage identifier:<br>
     * If a value less than/equal to zero is returned, then {@link #getId()} is supposed to be considered as the file storage owner;
     * otherwise the returned value itself
     *
     * @return The identifier of the owner or a value less than/equal to zero if there is none
     */
    int getFileStorageOwner();

    /**
     * TODO a user can have multiple logins.
     * @return the login information of the user.
     */
    String getLoginInfo();
    
    /**
     * Checks if this user is an anonymous guest user or not.
     * 
     * @return - true, if this user is an anonymous guest user, false otherwise
     */
    default boolean isAnonymousGuest() {
        return this.isGuest() && Strings.isEmpty(this.getMail());
    }
}
