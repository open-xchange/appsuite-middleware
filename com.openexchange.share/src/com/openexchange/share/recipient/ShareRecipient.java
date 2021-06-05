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

import java.io.Serializable;

/**
 * Describes a guest user to which a item or folder shall be shared.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class ShareRecipient implements Serializable {

    private static final long serialVersionUID = 1870155415515524040L;

    private int bits;

    /**
     * Initializes a new {@link ShareRecipient}.
     */
    protected ShareRecipient() {
        super();
    }

    /**
     * Gets the recipient type.
     *
     * @return The type
     */
    public abstract RecipientType getType();

    /**
     * Gets the bits
     *
     * @return The bits
     */
    public int getBits() {
        return bits;
    }

    /**
     * Gets whether this recipient is of type user or guest and can therefore be
     * casted to {@link InternalRecipient}.
     *
     * @return <code>true</code> if this recipient denotes an internal entity
     */
    public boolean isInternal() {
        return InternalRecipient.class.isAssignableFrom(getClass());
    }

    /**
     * Casts this recipient into an internal recipient.
     *
     * @return The casted instance
     * @throws ClassCastException If this recipient is not an internal one
     * @see #isInternal()
     */
    public InternalRecipient toInternal() {
        return InternalRecipient.class.cast(this);
    }

    /**
     * Sets the bits
     *
     * @param bits The bits to set
     */
    public void setBits(int bits) {
        this.bits = bits;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + bits;
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
        if (!(obj instanceof ShareRecipient)) {
            return false;
        }
        ShareRecipient other = (ShareRecipient) obj;
        if (bits != other.bits) {
            return false;
        }
        return true;
    }

}
