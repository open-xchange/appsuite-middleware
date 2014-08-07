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
 * {@link CleanupStatus} - Holds data about a cluster wide cleanup for c.o.realtime.hazelcast resources that can be used to decide if a
 * cleanup was already started. Additionally it holds data about who performed the cleanup at what time.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class CleanupStatus implements Serializable {

    private static final long serialVersionUID = 411907049966171587L;

    private String cleaningMemberId, memberToCleanId;

    private InetSocketAddress cleaningMemberAddress, memberToCleanAddress;

    private long cleaningStartTime, cleaningFinishTime;

    /**
     * Initializes a new {@link CleanupStatus}.
     * 
     * @param cleaningMember The member that started the cleanup.
     * @param memberToClean The member that left the cluster.
     */
    public CleanupStatus(Member cleaningMember, Member memberToClean) {
        super();
        this.cleaningMemberId = cleaningMember.getUuid();
        this.cleaningMemberAddress = cleaningMember.getSocketAddress();
        this.memberToCleanId = memberToClean.getUuid();
        this.memberToCleanAddress = memberToClean.getSocketAddress();
        this.cleaningStartTime = System.currentTimeMillis();
        this.cleaningFinishTime = -1;
    }

    /**
     * Gets the cleaningMemberId
     * 
     * @return The cleaningMemberId
     */
    public String getCleaningMemberId() {
        return cleaningMemberId;
    }

    /**
     * Sets the cleaningMemberId
     * 
     * @param cleaningMemberId The cleaningMemberId to set
     */
    public void setCleaningMemberId(String cleaningMemberId) {
        this.cleaningMemberId = cleaningMemberId;
    }

    /**
     * Gets the memberToCleanId
     * 
     * @return The memberToCleanId
     */
    public String getMemberToCleanId() {
        return memberToCleanId;
    }

    /**
     * Sets the memberToCleanId
     * 
     * @param memberToCleanId The memberToCleanId to set
     */
    public void setMemberToCleanId(String memberToCleanId) {
        this.memberToCleanId = memberToCleanId;
    }

    /**
     * Gets the cleaningMemberAddress
     * 
     * @return The cleaningMemberAddress
     */
    public InetSocketAddress getCleaningMemberAddress() {
        return cleaningMemberAddress;
    }

    /**
     * Sets the cleaningMemberAddress
     * 
     * @param cleaningMemberAddress The cleaningMemberAddress to set
     */
    public void setCleaningMemberAddress(InetSocketAddress cleaningMemberAddress) {
        this.cleaningMemberAddress = cleaningMemberAddress;
    }

    /**
     * Gets the memberToCleanAddress
     * 
     * @return The memberToCleanAddress
     */
    public InetSocketAddress getMemberToCleanAddress() {
        return memberToCleanAddress;
    }

    /**
     * Sets the memberToCleanAddress
     * 
     * @param memberToCleanAddress The memberToCleanAddress to set
     */
    public void setMemberToCleanAddress(InetSocketAddress memberToCleanAddress) {
        this.memberToCleanAddress = memberToCleanAddress;
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
        result = prime * result + ((cleaningMemberAddress == null) ? 0 : cleaningMemberAddress.hashCode());
        result = prime * result + ((cleaningMemberId == null) ? 0 : cleaningMemberId.hashCode());
        result = prime * result + (int) (cleaningStartTime ^ (cleaningStartTime >>> 32));
        result = prime * result + ((memberToCleanAddress == null) ? 0 : memberToCleanAddress.hashCode());
        result = prime * result + ((memberToCleanId == null) ? 0 : memberToCleanId.hashCode());
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
        if (cleaningMemberAddress == null) {
            if (other.cleaningMemberAddress != null)
                return false;
        } else if (!cleaningMemberAddress.equals(other.cleaningMemberAddress))
            return false;
        if (cleaningMemberId == null) {
            if (other.cleaningMemberId != null)
                return false;
        } else if (!cleaningMemberId.equals(other.cleaningMemberId))
            return false;
        if (cleaningStartTime != other.cleaningStartTime)
            return false;
        if (memberToCleanAddress == null) {
            if (other.memberToCleanAddress != null)
                return false;
        } else if (!memberToCleanAddress.equals(other.memberToCleanAddress))
            return false;
        if (memberToCleanId == null) {
            if (other.memberToCleanId != null)
                return false;
        } else if (!memberToCleanId.equals(other.memberToCleanId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CleanupStatus [cleaningMemberId=" + cleaningMemberId + ", memberToCleanId=" + memberToCleanId
             + ", cleaningMemberAddress=" + cleaningMemberAddress + ", memberToCleanAddress=" + memberToCleanAddress 
             + ", cleaningStartTime=" + cleaningStartTime + ", cleaningFinishTime=" + cleaningFinishTime + "]";
    }

}
