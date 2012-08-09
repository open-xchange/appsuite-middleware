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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.container;

import java.io.Serializable;

/**
 * {@link LinkObject} - Represents a link.
 *
 * @author <a href="mailto:ben.pahne@open-xchange.com">Benjamin Frederic Pahne</a>
 */
public class LinkObject implements Serializable {

    private static final long serialVersionUID = -287785895305471105L;

    private int firstId;

    private int secondId;

    private int firstType;

    private int secondType;

    private int firstFolder;

    private int secondFolder;

    private int cid;

    /**
     * Initializes a new {@link LinkObject}.
     */
    public LinkObject() {
        super();
    }

    /**
     * Initializes a new {@link LinkObject}.
     *
     * @param firstId The first object's ID
     * @param firstType The first object's type
     * @param firstFolder The first object's folder
     * @param secondId The second object's ID
     * @param secondType The second object's type
     * @param secondFolder The second object's folder
     * @param cid The context ID
     */
    public LinkObject(final int firstId, final int firstType, final int firstFolder, final int secondId, final int secondType, final int secondFolder, final int cid) {
        this();
        this.firstId = firstId;
        this.firstType = firstType;
        this.firstFolder = firstFolder;
        this.secondId = secondId;
        this.secondType = secondType;
        this.secondFolder = secondFolder;
        this.cid = cid;
    }

    /**
     * Sets all link information.
     *
     * @param firstId The first object's ID
     * @param firstType The first object's type
     * @param firstFolder The first object's folder
     * @param secondId The second object's ID
     * @param secondType The second object's type
     * @param secondFolder The second object's folder
     * @param cid The context ID
     */
    public void setLink(final int firstId, final int firstType, final int firstFolder, final int secondId, final int secondType, final int secondFolder, final int cid) {
        this.firstId = firstId;
        this.firstType = firstType;
        this.firstFolder = firstFolder;
        this.secondId = secondId;
        this.secondType = secondType;
        this.secondFolder = secondFolder;
        this.cid = cid;
    }

    /**
     * Sets the first object's ID.
     *
     * @param id The first object's ID
     */
    public void setFirstId(final int id) {
        firstId = id;
    }

    /**
     * Sets the first object's type.
     *
     * @param type The first object's type
     */
    public void setFirstType(final int type) {
        firstType = type;
    }

    /**
     * Sets the first object's folder.
     *
     * @param folder The first object's folder
     */
    public void setFirstFolder(final int folder) {
        firstFolder = folder;
    }

    /**
     * Sets the second object's ID.
     *
     * @param id The second object's ID.
     */
    public void setSecondId(final int id) {
        secondId = id;
    }

    /**
     * Sets the second object's type.
     *
     * @param type The second object's type.
     */
    public void setSecondType(final int type) {
        secondType = type;
    }

    /**
     * Sets the second object's folder.
     *
     * @param folder The second object's folder.
     */
    public void setSecondFolder(final int folder) {
        secondFolder = folder;
    }

    /**
     * Sets the context ID.
     *
     * @param cid The context ID.
     */
    public void setContext(final int cid) {
        this.cid = cid;
    }

    /**
     * Gets the first object's ID
     *
     * @return The first object's ID
     */
    public int getFirstId() {
        return firstId;
    }

    /**
     * Gets the second object's ID
     *
     * @return The second object's ID
     */
    public int getSecondId() {
        return secondId;
    }

    /**
     * Gets the first object's type
     *
     * @return The first object's type
     */
    public int getFirstType() {
        return firstType;
    }

    /**
     * Gets the second object's type
     *
     * @return The second object's type
     */
    public int getSecondType() {
        return secondType;
    }

    /**
     * Gets the first object's folder ID
     *
     * @return The first object's folder ID
     */
    public int getFirstFolder() {
        return firstFolder;
    }

    /**
     * Gets the second object's folder ID
     *
     * @return The second object's folder ID
     */
    public int getSecondFolder() {
        return secondFolder;
    }

    /**
     * Gets the context ID
     *
     * @return The context ID
     */
    public int getContectId() {
        return cid;
    }

    /**
     * Resets this link
     */
    public void reset() {
        firstId = 0;
        secondId = 0;

        firstType = 0;
        secondType = 0;

        firstFolder = 0;
        secondFolder = 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + cid;
        result = prime * result + firstFolder;
        result = prime * result + firstId;
        result = prime * result + firstType;
        result = prime * result + secondFolder;
        result = prime * result + secondId;
        result = prime * result + secondType;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LinkObject other = (LinkObject) obj;
        if (cid != other.cid) {
            return false;
        }
        if (firstFolder != other.firstFolder) {
            return false;
        }
        if (firstId != other.firstId) {
            return false;
        }
        if (firstType != other.firstType) {
            return false;
        }
        if (secondFolder != other.secondFolder) {
            return false;
        }
        if (secondId != other.secondId) {
            return false;
        }
        if (secondType != other.secondType) {
            return false;
        }
        return true;
    }
}
