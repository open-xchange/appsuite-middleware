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

package com.openexchange.push.impl.balancing.registrypolicy.portable;

import java.io.IOException;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.push.impl.balancing.registrypolicy.Owner;

/**
 * {@link PortableOwner}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PortableOwner implements CustomPortable {

    /** The unique portable class ID of the {@link PortableOwner} */
    public static final int CLASS_ID = 106;

    private String member;
    private int reason;

    /**
     * Initializes a new {@link PortableOwner}.
     *
     * @param member The associated member
     * @param reason The reason code
     */
    public PortableOwner(String member, int reason) {
        super();
        this.member = member;
        this.reason = reason;
    }

    /**
     * Initializes a new {@link PortableOwner}.
     *
     * @param owner The owner
     */
    public PortableOwner(Owner owner) {
        super();
        this.member = owner.getMember();
        this.reason = owner.getReason().ordinal();
    }

    /**
     * Initializes a new {@link PortableOwner}.
     */
    public PortableOwner() {
        super();
    }


    /**
     * Gets the member
     *
     * @return The member
     */
    public String getMember() {
        return member;
    }

    /**
     * Sets the member
     *
     * @param member The member to set
     */
    public void setMember(String member) {
        this.member = member;
    }

    /**
     * Gets the reason
     *
     * @return The reason
     */
    public int getReason() {
        return reason;
    }

    /**
     * Sets the reason
     *
     * @param reason The reason to set
     */
    public void setReason(int reason) {
        this.reason = reason;
    }

    // ----------------------------------------------- Portable methods ---------------------------------------------------------------

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF("member", member);
        writer.writeInt("reason", reason);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        member = reader.readUTF("member");
        reason = reader.readInt("reason");
    }

    @Override
    public int getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

}
