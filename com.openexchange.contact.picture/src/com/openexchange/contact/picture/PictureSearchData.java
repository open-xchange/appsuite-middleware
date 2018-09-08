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

package com.openexchange.contact.picture;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link PictureSearchData} - The object containing information about the contact to find a picture for.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class PictureSearchData {

    private final Integer userId;

    private final Integer folderId;

    private final Integer contactId;

    private final Set<String> emails;

    public static final PictureSearchData EMPTY_DATA = new PictureSearchData(null, null, null, null);

    /**
     * Initializes a new {@link PictureSearchData}.
     *
     * @param userId The user identifier
     * @param folderId The folder identifier
     * @param contactId The contact identifier
     * @param emails The email addresses
     *
     */
    public PictureSearchData(Integer userId, Integer folderId, Integer contactId, Collection<String> emails) {
        this.userId = userId;
        this.folderId = folderId;
        this.contactId = contactId;
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
     * Get the folder identifier
     *
     * @return The identifier or <code>null</code>
     */
    public final Integer getFolderId() {
        return folderId;
    }

    /**
     * A value indicating if the folder identifier is set
     *
     * @return <code>true</code> if the folder identifier is set,
     *         <code>false</code> otherwise
     */
    public boolean hasFolder() {
        return null != folderId && folderId.intValue() > 0;
    }

    /**
     * Get the contact identifier
     *
     * @return The identifier or <code>null</code>
     */
    public final Integer getContactId() {
        return contactId;
    }

    /**
     * A value indicating if the contact identifier is set
     *
     * @return <code>true</code> if the contact identifier is set,
     *         <code>false</code> otherwise
     */
    public boolean hasContact() {
        return null != contactId && contactId.intValue() > 0;
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
        return "ContactPictureRequestData [userId=" + userId + ", folderId=" + folderId + ", contactId=" + contactId + ", emails=" + emails.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
    }

}
