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
public abstract class ShareRecipient {

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
