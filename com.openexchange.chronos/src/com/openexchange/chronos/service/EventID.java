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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.service;

import java.util.Date;

/**
 * {@link EventID}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventID {

    private final int folderID;
    private final int objectID;
    private final Date recurrenceID;

    /**
     * Initializes a new {@link EventID}.
     *
     * @param folderID The folder ID
     * @param objectID The object ID
     */
    public EventID(int folderID, int objectID) {
        this(folderID, objectID, null);
    }

    /**
     * Initializes a new {@link EventID}.
     *
     * @param folderID The folder ID
     * @param objectID The object ID
     * @param recurrenceID The recurrence ID
     */
    public EventID(int folderID, int objectID, Date recurrenceID) {
        super();
        this.folderID = folderID;
        this.objectID = objectID;
        this.recurrenceID = recurrenceID;
    }

    public int getFolderID() {
        return folderID;
    }

    public int getObjectID() {
        return objectID;
    }

    public Date getRecurrenceID() {
        return recurrenceID;
    }

    public String toString1() {
        return "EventID [folderID=" + folderID + ", objectID=" + objectID + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + folderID;
        result = prime * result + objectID;
        result = prime * result + ((recurrenceID == null) ? 0 : recurrenceID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventID other = (EventID) obj;
        if (folderID != other.folderID)
            return false;
        if (objectID != other.objectID)
            return false;
        if (recurrenceID == null) {
            if (other.recurrenceID != null)
                return false;
        } else if (!recurrenceID.equals(other.recurrenceID))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return null == recurrenceID ? "EventID [folderID=" + folderID + ", objectID=" + objectID + "]" : "EventID [folderID=" + folderID + ", objectID=" + objectID + ", recurrenceID=" + recurrenceID + "]";
    }

}
