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
