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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import com.openexchange.java.Strings;

/**
 * {@link ContactPictureRequestData} - Object holding data to get a contact picture
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactPictureRequestData {

    private Integer contextId;

    private Integer userId;

    private Integer folderId;

    private Integer contactId;

    private final Set<String> emails;

    private boolean etag;

    /**
     * Initializes a new {@link ContactPictureRequestData}.
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param folderId The folder identifier
     * @param contactId The contact identifier
     * @param emails The email addresses
     * @param etag If only eTag should be generated
     *
     */
    public ContactPictureRequestData(Integer contextId, Integer userId, Integer folderId, Integer contactId, Collection<String> emails, boolean etag) {
        this.contextId = contextId;
        this.userId = userId;
        this.folderId = folderId;
        this.contactId = contactId;
        this.emails = emails == null ? new HashSet<>() : new HashSet<>(emails);
        this.etag = etag;
    }

    /**
     * Get the context identifier of the current session
     *
     * @return The identifier or <code>null</code>
     */
    public final Integer getContextId() {
        return contextId;
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
        addEmails(emails);
    }

    /**
     * Add contacts E-Mails
     *
     * @param emails The E-Mails
     */
    public void addEmails(String... emails) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contactId == null) ? 0 : contactId.hashCode());
        result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
        result = prime * result + ((emails == null) ? 0 : emails.hashCode());
        result = prime * result + (etag ? 1231 : 1237);
        result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ContactPictureRequestData other = (ContactPictureRequestData) obj;
        if (contactId == null) {
            if (other.contactId != null) {
                return false;
            }
        } else if (!contactId.equals(other.contactId)) {
            return false;
        }
        if (contextId == null) {
            if (other.contextId != null) {
                return false;
            }
        } else if (!contextId.equals(other.contextId)) {
            return false;
        }
        if (emails == null) {
            if (other.emails != null) {
                return false;
            }
        } else if (!emails.equals(other.emails)) {
            return false;
        }
        if (etag != other.etag) {
            return false;
        }
        if (folderId == null) {
            if (other.folderId != null) {
                return false;
            }
        } else if (!folderId.equals(other.folderId)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ContactPictureRequestData [contextId=" + contextId + ", userId=" + userId + ", folderId=" + folderId + ", contactId=" + contactId + ", emails=" + emails.stream().map(String::valueOf).collect(Collectors.joining(",")) + ", etag=" + etag + "]";
    }

}
