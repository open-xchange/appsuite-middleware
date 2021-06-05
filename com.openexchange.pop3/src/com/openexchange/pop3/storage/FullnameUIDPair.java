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

package com.openexchange.pop3.storage;

/**
 * {@link FullnameUIDPair} - Represents a fullname-mailId-pair.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FullnameUIDPair implements Comparable<FullnameUIDPair> {

    private static final String INBOX = "INBOX";

    /**
     * Creates a new {@link FullnameUIDPair} with full name set to <code>&quot;INBOX&quot;</code> and mail ID set to given mail ID.
     *
     * @param mailId The mail ID
     * @return A new {@link FullnameUIDPair} with full name set to <code>&quot;INBOX&quot;</code> and mail ID set to given mail ID
     */
    public static FullnameUIDPair newINBOXInstance(final String mailId) {
        return new FullnameUIDPair(INBOX, mailId);
    }

    private final String fullName;
    private final String mailId;
    private final int hash;

    /**
     * Initializes a new {@link FullnameUIDPair}.
     *
     * @param fullName The folder full name
     * @param mailId The mail ID
     */
    public FullnameUIDPair(final String fullName, final String mailId) {
        super();
        this.fullName = fullName;
        this.mailId = mailId;
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
        result = prime * result + ((mailId == null) ? 0 : mailId.hashCode());
        this.hash = result;
    }

    /**
     * Gets the full name.
     *
     * @return The full name
     */
    public String getFullname() {
        return fullName;
    }

    /**
     * Gets the mail ID.
     *
     * @return The mail ID
     */
    public String getMailId() {
        return mailId;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FullnameUIDPair)) {
            return false;
        }
        final FullnameUIDPair other = (FullnameUIDPair) obj;
        if (fullName == null) {
            if (other.fullName != null) {
                return false;
            }
        } else if (!fullName.equals(other.fullName)) {
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
    public int compareTo(final FullnameUIDPair other) {
        if (fullName == null) {
            if (other.fullName != null) {
                return -1;
            }
            return 0;
        } else if (other.fullName == null) {
            return 1;
        }
        final int folderComp = fullName.compareTo(other.fullName);
        if (folderComp != 0) {
            return folderComp;
        }
        if (mailId == null) {
            if (other.mailId != null) {
                return -1;
            }
            return 0;
        } else if (other.mailId == null) {
            return 1;
        }
        return mailId.compareTo(other.mailId);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Fullname=").append(fullName).append(" Mail-ID=").append(mailId).toString();
    }
}
