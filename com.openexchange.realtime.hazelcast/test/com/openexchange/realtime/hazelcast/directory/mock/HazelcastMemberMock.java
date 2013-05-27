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

package com.openexchange.realtime.hazelcast.directory.mock;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import com.hazelcast.core.Member;

/**
 * {@link HazelcastMemberMock} - Simple mock to use for testing withoug OSGi environment.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HazelcastMemberMock implements Member {

    private static final long serialVersionUID = -8830298673180018754L;

    private final InetAddress localInetAddress;

    private final InetSocketAddress localInetSocketAddress;

    private final UUID uuid = UUID.randomUUID();

    public HazelcastMemberMock() throws UnknownHostException {
        localInetAddress = InetAddress.getByName("127.0.0.1");
        localInetSocketAddress = new InetSocketAddress(localInetAddress, 1234);
    }

    @Override
    public void writeData(DataOutput out) throws IOException {
    }

    @Override
    public void readData(DataInput in) throws IOException {
    }

    @Override
    public boolean localMember() {
        return true;
    }

    @Override
    @Deprecated
    public int getPort() {
        return localInetSocketAddress.getPort();
    }

    @Override
    @Deprecated
    public InetAddress getInetAddress() {
        return localInetAddress;
    }

    @Override
    public InetSocketAddress getInetSocketAddress() {
        return localInetSocketAddress;
    }

    @Override
    @Deprecated
    public boolean isSuperClient() {
        return false;
    }

    @Override
    public boolean isLiteMember() {
        return false;
    }

    @Override
    public String getUuid() {
        return uuid.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((localInetAddress == null) ? 0 : localInetAddress.hashCode());
        result = prime * result + ((localInetSocketAddress == null) ? 0 : localInetSocketAddress.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof HazelcastMemberMock))
            return false;
        HazelcastMemberMock other = (HazelcastMemberMock) obj;
        if (localInetAddress == null) {
            if (other.localInetAddress != null)
                return false;
        } else if (!localInetAddress.equals(other.localInetAddress))
            return false;
        if (localInetSocketAddress == null) {
            if (other.localInetSocketAddress != null)
                return false;
        } else if (!localInetSocketAddress.equals(other.localInetSocketAddress))
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }

}
