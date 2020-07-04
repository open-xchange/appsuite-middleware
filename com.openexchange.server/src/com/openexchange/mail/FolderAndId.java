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

package com.openexchange.mail;

import java.util.Objects;

/**
 * {@link FolderAndId} - An immutable tuple of folder and mail identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class FolderAndId {

    private final String folderId;
    private final String mailId;
    private int hash;

    /**
     * Initializes a new {@link FolderAndId}.
     *
     * @param folderId The folder identifier; e.g. <code>"default0/INBOX"</code>
     * @param mailId The mail identifier
     * @throws NullPointerException If either folder or mail identifier is <code>null</code>
     */
    public FolderAndId(String folderId, String mailId) {
        super();
        Objects.requireNonNull(folderId, "Folder identifier must not be null.");
        Objects.requireNonNull(mailId, "Mail identifier must not be null.");
        this.folderId = folderId;
        this.mailId = mailId;
        hash = 0;
    }

    /**
     * Gets the folder identifier; e.g. <code>"default0/INBOX"</code>.
     *
     * @return The folder identifier
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Gets the mail identifier.
     *
     * @return The mail identifier
     */
    public String getMailId() {
        return mailId;
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
            result = prime * result + ((mailId == null) ? 0 : mailId.hashCode());
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FolderAndId)) {
            return false;
        }
        FolderAndId other = (FolderAndId) obj;
        if (folderId == null) {
            if (other.folderId != null) {
                return false;
            }
        } else if (!folderId.equals(other.folderId)) {
            return false;
        }
        if (mailId == null) {
            if (other.mailId != null) {
                return false;
            }
        } else if (!mailId.equals(other.mailId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("FolderAndId [");
        if (folderId != null) {
            builder.append("folderId=").append(folderId).append(", ");
        }
        if (mailId != null) {
            builder.append("mailId=").append(mailId);
        }
        builder.append("]");
        return builder.toString();
    }

}
