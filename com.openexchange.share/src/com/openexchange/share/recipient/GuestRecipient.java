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

package com.openexchange.share.recipient;

/**
 * Describes a guest user to which a item or folder shall be shared.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GuestRecipient extends ShareRecipient {

    private String emailAddress;
    private String displayName;
    private String contactID;
    private String contactFolder;
    private String password;

    public GuestRecipient() {
        super();
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
