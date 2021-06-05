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

package com.openexchange.contact.picture;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import com.openexchange.java.Strings;

/**
 * {@link PictureSearchData} - The object containing information about the contact to find a picture for.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class PictureSearchData {

    private final Integer userId;
    private final String folderId;
    private final String contactId;
    private final String accountId;
    private final Set<String> emails;

    /**
     * Search result representing an unsuccessful search
     */
    public static final PictureSearchData EMPTY_DATA = new PictureSearchData(null, null, null, null, null);

    /**
     * Initializes a new {@link PictureSearchData}.
     *
     * @param userId The user identifier
     * @param folderId The folder identifier
     * @param contactId The contact identifier
     * @param emails The email addresses
     *
     */
    public PictureSearchData(Integer userId, String folderId, String contactId, Collection<String> emails) {
        this(userId, null, folderId, contactId, emails);
    }

    /**
     * Initializes a new {@link PictureSearchData}.
     *
     * @param userId The user identifier
     * @param accountId The account identifier
     * @param folderId The folder identifier
     * @param contactId The contact identifier
     * @param emails The email addresses
     *
     */
    public PictureSearchData(Integer userId, String accountId, String folderId, String contactId, Collection<String> emails) {
        this.userId = userId;
        this.folderId = folderId;
        this.contactId = contactId;
        this.accountId = accountId;
        this.emails = emails == null ? new LinkedHashSet<>() : new LinkedHashSet<>(emails);
    }

    /**
     * Get the identifier of the user to get the picture for
     *
     * @return The identifier or <code>null</code>
     */
    public final Integer getUserId() {
        return userId;
    }

    /**
     * A value indicating if the user identifier is set
     *
     * @return <code>true</code> if the user identifier is set,
     *         <code>false</code> otherwise
     */
    public boolean hasUser() {
        return null != userId && userId.intValue() > 0;
    }

    /**
     * Get the identifier of the account to get the picture for
     *
     * @return The identifier or <code>null</code>
     */
    public final String getAccountId() {
        return accountId;
    }

    /**
     * A value indicating if the account identifier is set
     *
     * @return <code>true</code> if the account identifier is set,
     *         <code>false</code> otherwise
     */
    public boolean hasAccount() {
        return null != accountId;
    }

    /**
     * Get the folder identifier
     *
     * @return The identifier or <code>null</code>
     */
    public final String getFolderId() {
        return folderId;
    }

    /**
     * A value indicating if the folder identifier is set
     *
     * @return <code>true</code> if the folder identifier is set,
     *         <code>false</code> otherwise
     */
    public boolean hasFolder() {
        return Strings.isNotEmpty(folderId);
    }

    /**
     * Get the contact identifier
     *
     * @return The identifier or <code>null</code>
     */
    public final String getContactId() {
        return contactId;
    }

    /**
     * A value indicating if the contact identifier is set
     *
     * @return <code>true</code> if the contact identifier is set,
     *         <code>false</code> otherwise
     */
    public boolean hasContact() {
        return Strings.isNotEmpty(contactId);
    }

    /**
     * Get the mail addresses
     *
     * @return The email or <code>null</code>
     */
    public final Set<String> getEmails() {
        return emails;
    }

    /**
     * A value indicating if the mail address is set
     *
     * @return <code>true</code> if the mail address is set,
     *         <code>false</code> otherwise
     */
    public boolean hasEmail() {
        return false == emails.isEmpty();
    }

    @Override
    public String toString() {
        return "ContactPictureRequestData [userId=" + userId + ", accountId=" + accountId + ", folderId=" + folderId + ", contactId=" + contactId + ", emails=" + emails.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
    }

}
