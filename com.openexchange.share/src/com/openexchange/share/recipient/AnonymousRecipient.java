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

import java.util.Date;

/**
 * {@link AnonymousRecipient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class AnonymousRecipient extends ShareRecipient {

    private static final long serialVersionUID = -6939532786908091158L;

    private String password;
    private Date expiryDate;

    /**
     * Initializes a new {@link AnonymousRecipient}.
     */
    public AnonymousRecipient() {
        super();
    }

    /**
     * Initializes a new {@link AnonymousRecipient}.
     *
     * @param bits The permission bits to set
     * @param password The password to set, or <code>null</code> for no password
     * @param expiryDate The expiration date, or <code>null</code> if not set
     */
    public AnonymousRecipient(int bits, String password, Date expiryDate) {
        this();
        setBits(bits);
        setPassword(password);
        setExpiryDate(expiryDate);
    }

    @Override
    public RecipientType getType() {
        return RecipientType.ANONYMOUS;
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
     * Gets the expiration date of the anonymous guest.
     *
     * @return The expiration date, or <code>null</code> if not set
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiration date for the anonymous guest.
     *
     * @param expiryDate The expiration date, or <code>null</code> if not set
     */
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
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
        if (!(obj instanceof AnonymousRecipient)) {
            return false;
        }
        AnonymousRecipient other = (AnonymousRecipient) obj;
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
        return "ShareRecipient [type=" + getType() + ", bits=" + getBits() + "]";
    }

}
