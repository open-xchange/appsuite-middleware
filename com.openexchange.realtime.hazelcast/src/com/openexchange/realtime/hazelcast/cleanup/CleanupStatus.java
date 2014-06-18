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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.realtime.hazelcast.cleanup;

import java.io.Serializable;
import java.net.InetSocketAddress;
import com.hazelcast.core.Member;

/**
 * {@link CleanupStatus} - Holds data about a cluster wide cleanup for c.o.realtime.hazelcast resources.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class CleanupStatus implements Serializable {

    private static final long serialVersionUID = 411907049966171587L;

    private String cleaningNodeId;

    private InetSocketAddress cleaningNodeAddress;

    private long cleaningStartTime;

    private long cleaningFinishTime;

    /**
     * Initializes a new {@link CleanupStatus}.
     * 
     * @param cleaningMember The member that started the cleanup.
     */
    public CleanupStatus(Member cleaningMember) {
        super();
        this.cleaningNodeId = cleaningMember.getUuid();
        this.cleaningNodeAddress = cleaningMember.getInetSocketAddress();
        this.cleaningStartTime = System.currentTimeMillis();
        this.cleaningFinishTime = -1;
    }

    
    /**
     * Gets the cleaningNodeId
     *
     * @return The cleaningNodeId
     */
    public String getCleaningNodeId() {
        return cleaningNodeId;
    }

    
    /**
     * Sets the cleaningNodeId
     *
     * @param cleaningNodeId The cleaningNodeId to set
     */
    public void setCleaningNodeId(String cleaningNodeId) {
        this.cleaningNodeId = cleaningNodeId;
    }

    
    /**
     * Gets the cleaningNodeAddress
     *
     * @return The cleaningNodeAddress
     */
    public InetSocketAddress getCleaningNodeAddress() {
        return cleaningNodeAddress;
    }

    
    /**
     * Sets the cleaningNodeAddress
     *
     * @param cleaningNodeAddress The cleaningNodeAddress to set
     */
    public void setCleaningNodeAddress(InetSocketAddress cleaningNodeAddress) {
        this.cleaningNodeAddress = cleaningNodeAddress;
    }

    
    /**
     * Gets the cleaningStartTime
     *
     * @return The cleaningStartTime i milliseconds
     */
    public long getCleaningStartTime() {
        return cleaningStartTime;
    }

    
    /**
     * Sets the cleaningStartTime
     *
     * @param cleaningStartTime The cleaningStartTime to set in milliseconds.
     */
    public void setCleaningStartTime(long cleaningStartTime) {
        this.cleaningStartTime = cleaningStartTime;
    }

    
    /**
     * Gets the cleaningFinishTime
     *
     * @return The cleaningFinishTime in milliseconds, -1 if not finished.
     */
    public long getCleaningFinishTime() {
        return cleaningFinishTime;
    }

    
    /**
     * Sets the cleaningFinishTime
     *
     * @param cleaningFinishTime The cleaningFinishTime to set in milliseconds.
     */
    public void setCleaningFinishTime(long cleaningFinishTime) {
        this.cleaningFinishTime = cleaningFinishTime;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (cleaningFinishTime ^ (cleaningFinishTime >>> 32));
        result = prime * result + ((cleaningNodeAddress == null) ? 0 : cleaningNodeAddress.hashCode());
        result = prime * result + ((cleaningNodeId == null) ? 0 : cleaningNodeId.hashCode());
        result = prime * result + (int) (cleaningStartTime ^ (cleaningStartTime >>> 32));
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof CleanupStatus))
            return false;
        CleanupStatus other = (CleanupStatus) obj;
        if (cleaningFinishTime != other.cleaningFinishTime)
            return false;
        if (cleaningNodeAddress == null) {
            if (other.cleaningNodeAddress != null)
                return false;
        } else if (!cleaningNodeAddress.equals(other.cleaningNodeAddress))
            return false;
        if (cleaningNodeId == null) {
            if (other.cleaningNodeId != null)
                return false;
        } else if (!cleaningNodeId.equals(other.cleaningNodeId))
            return false;
        if (cleaningStartTime != other.cleaningStartTime)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CleanupStatus [cleaningNodeId=" + cleaningNodeId + ", cleaningNodeAddress=" + cleaningNodeAddress + ", cleaningStartTime=" + cleaningStartTime + ", cleaningFinishTime=" + cleaningFinishTime + "]";
    }

}
