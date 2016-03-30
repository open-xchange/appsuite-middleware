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

package com.openexchange.realtime.hazelcast.serialization.cleanup;

import java.io.IOException;
import com.hazelcast.core.Member;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableRoutingInfo;

/**
 * {@link PortableCleanupStatus} - Holds data about a cluster wide cleanup for c.o.realtime.hazelcast resources that can be used to decide
 * if a cleanup was already started. Additionally it holds data about who performed the cleanup at what time.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class PortableCleanupStatus extends AbstractCustomPortable {

    public static int CLASS_ID = 16;

    private static String FIELD_CLEANING_MEMBER = "cleaningMember";

    private static String FIELD_MEMBER_TO_CLEAN = "memberToClean";
    
    private static String FIELD_CLEANING_START_TIME = "cleaningStartTime";
    
    private static String FIELD_CLEANING_FINISH_TIME = "cleaningFinishTime";

    public static ClassDefinition CLASS_DEFINITION = null;

    static {
        CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID)
        .addPortableField(FIELD_CLEANING_MEMBER, PortableRoutingInfo.CLASS_DEFINITION)
        .addPortableField(FIELD_MEMBER_TO_CLEAN, PortableRoutingInfo.CLASS_DEFINITION)
        .addLongField(FIELD_CLEANING_START_TIME)
        .addLongField(FIELD_CLEANING_FINISH_TIME)
        .build();
    }

    private PortableRoutingInfo cleaningMember, memberToClean;

    private long cleaningStartTime, cleaningFinishTime;

    /**
     * Initializes a new {@link PortableCleanupStatus}.
     */
    public PortableCleanupStatus() {
        super();
    }

    /**
     * Initializes a new {@link PortableCleanupStatus}.
     * 
     * @param cleaningMember The member that started the cleanup.
     * @param memberToClean The member that left the cluster.
     */
    public PortableCleanupStatus(Member cleaningMember, Member memberToClean) {
        super();
        this.cleaningMember = new PortableRoutingInfo(cleaningMember.getSocketAddress(), cleaningMember.getUuid());
        this.memberToClean = new PortableRoutingInfo(memberToClean.getSocketAddress(), memberToClean.getUuid());
        this.cleaningStartTime = System.currentTimeMillis();
        this.cleaningFinishTime = -1;
    }

    /**
     * Gets the cleaningMember
     * 
     * @return The cleaningMember
     */
    public PortableRoutingInfo getCleaningMember() {
        return cleaningMember;
    }

    /**
     * Sets the cleaningMember
     * 
     * @param cleaningMember The cleaningMember to set
     */
    public void setCleaningMember(PortableRoutingInfo cleaningMember) {
        this.cleaningMember = cleaningMember;
    }

    /**
     * Gets the memberToClean
     * 
     * @return The memberToClean
     */
    public PortableRoutingInfo getMemberToClean() {
        return memberToClean;
    }

    /**
     * Sets the memberToClean
     * 
     * @param memberToClean The memberToClean to set
     */
    public void setMemberToClean(PortableRoutingInfo memberToClean) {
        this.memberToClean = memberToClean;
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
        result = prime * result + ((cleaningMember == null) ? 0 : cleaningMember.hashCode());
        result = prime * result + (int) (cleaningStartTime ^ (cleaningStartTime >>> 32));
        result = prime * result + ((memberToClean == null) ? 0 : memberToClean.hashCode());
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
        PortableCleanupStatus other = (PortableCleanupStatus) obj;
        if (cleaningFinishTime != other.cleaningFinishTime)
            return false;
        if (cleaningMember == null) {
            if (other.cleaningMember != null)
                return false;
        } else if (!cleaningMember.equals(other.cleaningMember))
            return false;
        if (cleaningStartTime != other.cleaningStartTime)
            return false;
        if (memberToClean == null) {
            if (other.memberToClean != null)
                return false;
        } else if (!memberToClean.equals(other.memberToClean))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortableCleanupStatus [cleaningMember=" + cleaningMember + ", memberToClean=" + memberToClean + ", cleaningStartTime=" + cleaningStartTime + ", cleaningFinishTime=" + cleaningFinishTime + "]";
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writePortable(FIELD_CLEANING_MEMBER, cleaningMember);
        writer.writePortable(FIELD_MEMBER_TO_CLEAN, memberToClean);
        writer.writeLong(FIELD_CLEANING_START_TIME, cleaningStartTime);
        writer.writeLong(FIELD_CLEANING_FINISH_TIME, cleaningFinishTime);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        cleaningMember = reader.readPortable(FIELD_CLEANING_MEMBER);
        memberToClean = reader.readPortable(FIELD_MEMBER_TO_CLEAN);
        cleaningStartTime = reader.readLong(FIELD_CLEANING_START_TIME);
        cleaningFinishTime = reader.readLong(FIELD_CLEANING_FINISH_TIME);
    }

}
