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

package com.openexchange.realtime.atmosphere.impl;

import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.realtime.packet.ID;

/**
 * {@link Moribund}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class Moribund implements Comparable<Moribund> {

    public static final org.apache.commons.logging.Log LOG = Log.loggerFor(Moribund.class);

    /* Start of lingering in milliseconds */
    private final long lingeringStart;

    private ID id;


    /**
     * Initializes a new {@link Moribund}.
     * 
     * @param concreteId
     * @param atmosphereResource
     * @param generalToFullIDMap
     * @param fullIDToResourceMap
     * @param outboxes
     * @param resendBuffers 
     * @param sequenceNumbers 
     */
    public Moribund(ID concreteID) {
        this.lingeringStart = System.currentTimeMillis();
        this.id = concreteID;
    }

    @Override
    public int compareTo(final Moribund otherEntry) {
        if (this.lingeringStart < otherEntry.lingeringStart) {// this one is older
            return 1;
        } else if (lingeringStart > otherEntry.lingeringStart) {// this one is younger
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Get the lingering time of this moribund in milliseconds.
     * 
     * @return the lingering time of this moribund in milliseconds
     */
    public long getLinger() {
        return System.currentTimeMillis() - lingeringStart;
    }
    
    public ID getConcreteID() {
        return id;
    }

    /**
     * Cause the moribund to clean up the traces he left behind and die.
     * 
     * @throws OXException when he fails to to clean up the traces he left behind and die
     */
    public abstract void die() throws OXException;

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (int) (lingeringStart ^ (lingeringStart >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Moribund))
            return false;
        Moribund other = (Moribund) obj;
    
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (lingeringStart != other.lingeringStart)
            return false;
        return true;
    }

}
