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

package com.openexchange.share.recipient;

/**
 * Describes a guest user to which a item or folder shall be shared.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GuestRecipient extends ShareRecipient {

    private static final long serialVersionUID = -1684289453388992393L;

    private String emailAddress;
    private String displayName;
    private String contactID;
    private String contactFolder;
    private String password;
    private String preferredLanguage;

    public GuestRecipient() {
        super();
        this.preferredLanguage = null;
    }

    @Override
    public RecipientType getType() {
        return RecipientType.GUEST;
    }

    /**
     * Gets the emailAddress
     *
     * @return The emailAddress
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the emailAddress
     *
     * @param emailAddress The emailAddress to set
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Gets the displayName
     *
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the displayName
     *
     * @param displayName The displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the contactID
     *
     * @return The contactID
     */
    public String getContactID() {
        return contactID;
    }

    /**
     * Sets the contactID
     *
     * @param contactID The contactID to set
     */
    public void setContactID(String contactID) {
        this.contactID = contactID;
    }

    /**
     * Gets the contactFolder
     *
     * @return The contactFolder
     */
    public String getContactFolder() {
        return contactFolder;
    }

    /**
     * Sets the contactFolder
     *
     * @param contactFolder The contactFolder to set
     */
    public void setContactFolder(String contactFolder) {
        this.contactFolder = contactFolder;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password
     *
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the preferred language for the recipient
     * @param lang
     */
    public void setPreferredLanguage(String lang) {
        this.preferredLanguage = lang;
    }

    /**
     * Gets the preferred language for the recipient
     * @return language
     */
    public String getPreferredLanguage() {
        return this.preferredLanguage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((contactFolder == null) ? 0 : contactFolder.hashCode());
        result = prime * result + ((contactID == null) ? 0 : contactID.hashCode());
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof GuestRecipient)) {
            return false;
        }
        GuestRecipient other = (GuestRecipient) obj;
        if (contactFolder == null) {
            if (other.contactFolder != null) {
                return false;
            }
        } else if (!contactFolder.equals(other.contactFolder)) {
            return false;
        }
        if (contactID == null) {
            if (other.contactID != null) {
                return false;
            }
        } else if (!contactID.equals(other.contactID)) {
            return false;
        }
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (emailAddress == null) {
            if (other.emailAddress != null) {
                return false;
            }
        } else if (!emailAddress.equals(other.emailAddress)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ShareRecipient [type=" + getType() + ", bits=" + getBits() + ", emailAddress=" + emailAddress + "]";
    }

}
