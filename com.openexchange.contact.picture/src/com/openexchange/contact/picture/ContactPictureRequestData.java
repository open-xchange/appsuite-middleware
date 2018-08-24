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

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link ContactPictureRequestData} - Object holding data to get a contact picture
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactPictureRequestData {

    private Integer userId;

    private Integer folderId;

    private Integer contactId;

    private final Set<String> emails;

    private final Session session;

    private boolean etag;

    /**
     * Initializes a new {@link ContactPictureRequestData}.
     *
     * @param session The current session
     * @param userId The user identifier
     * @param folderId The folder identifier
     * @param contactId The contact identifier
     * @param emails The email addresses
     * @param etag If only eTag should be generated
     * @throws OXException If session is missing
     *
     */
    public ContactPictureRequestData(Session session, Integer userId, Integer folderId, Integer contactId, Collection<String> emails, boolean etag) throws OXException {
        if (session == null) {
            throw ContactPictureExceptionCodes.MISSING_SESSION.create();
        }

        this.session = session;
        this.userId = userId;
        this.folderId = folderId;
        this.contactId = contactId;
        this.emails = new HashSet<>(emails);
        this.etag = etag;
    }

    /**
     * Get the context identifier of the current session
     *
     * @return The identifier or <code>null</code>
     */
    public final Integer getContextId() {
        return I(session.getContextId());
    }

    /**
     * Get the the current {@link Session}
     *
     * @return The {@link Session} or <code>null</code>
     */
    public final Session getSession() {
        return session;
    }

    /**
     * Get the user identifier
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
        return null != userId;
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
        return null != folderId;
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
        return null != contactId;
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

    /**
     * A value indicating if <b>only</b> the eTag shall be generated.
     * If the value is set to <code>false</code> the eTag still can be set, but
     * additionally services will try to set the pictures data.
     *
     * @return <code>true</code> if the mail address is set,
     *         <code>false</code> otherwise
     */
    public boolean onlyETag() {
        return etag;
    }

    /**
     * Set the user identifier
     *
     * @param userId The identifier
     */
    public void setUser(Integer userId) {
        this.userId = userId;
    }

    /**
     * Set the folder identifier
     *
     * @param folderId The folder identifier
     */
    public void setFolder(Integer folderId) {
        this.folderId = folderId;
    }

    /**
     * Set the contact identifier
     *
     * @param contactId the contact identifier
     */
    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }

    /**
     * Set the contacts E-Mails
     *
     * @param emails The E-Mails
     */
    public void setEmails(String... emails) {
        this.emails.clear();
        for (String mail : emails) {
            if (Strings.isNotEmpty(mail)) {
                this.emails.add(mail);
            }
        }
    }

    /**
     * Set the eTag for the contact picture
     *
     * @param etag The eTag
     */
    public void setETag(boolean etag) {
        this.etag = etag;
    }

    /**
     * Get a value indicating if this instances does not hold any usable data
     *
     * @return <code>true</code> if this instance doesn't hold any usable data
     */
    public boolean isEmpty() {
        return null == userId && null == folderId && null == contactId && null == emails;
    }

}
