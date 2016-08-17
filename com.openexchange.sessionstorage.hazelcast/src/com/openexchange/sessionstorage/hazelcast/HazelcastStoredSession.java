///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the OX Software GmbH group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2016-2020 OX Software GmbH
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//
//package com.openexchange.sessionstorage.hazelcast;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import com.hazelcast.nio.ObjectDataInput;
//import com.hazelcast.nio.ObjectDataOutput;
//import com.hazelcast.nio.serialization.DataSerializable;
//import com.openexchange.session.Session;
//import com.openexchange.sessionstorage.StoredSession;
//
///**
// * {@link HazelcastStoredSession}
// *
// * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
// */
//public class HazelcastStoredSession extends StoredSession implements DataSerializable {
//
//    private static final long serialVersionUID = -2346327568417617677L;
//
//    /**
//     * Initializes a new {@link HazelcastStoredSession}.
//     */
//    public HazelcastStoredSession() {
//        super();
//    }
//
//    /**
//     * Initializes a new {@link HazelcastStoredSession}.
//     *
//     * @param session
//     */
//    public HazelcastStoredSession(final Session session) {
//        super(session);
//    }
//
//    @Override
//    public void writeData(ObjectDataOutput out) throws IOException {
//        /*
//         * basic properties
//         */
//        out.writeUTF(loginName);
//        out.writeUTF(password);
//        out.writeInt(contextId);
//        out.writeInt(userId);
//        out.writeUTF(sessionId);
//        out.writeUTF(secret);
//        out.writeUTF(login);
//        out.writeUTF(randomToken);
//        out.writeUTF(localIp);
//        out.writeUTF(authId);
//        out.writeUTF(hash);
//        out.writeUTF(client);
//        out.writeUTF(userLogin);
//        /*
//         * special handling for parameters map
//         */
//        Object alternativeID = parameters.get(PARAM_ALTERNATIVE_ID);
//        out.writeUTF(null != alternativeID && String.class.isInstance(alternativeID) ? (String)alternativeID : null);
//        Object capabilitiesValue = parameters.get(PARAM_CAPABILITIES);
//        if (null != capabilitiesValue && java.util.Collection.class.isInstance(capabilitiesValue)) {
//            Collection<?> capabilities = (Collection<?>)capabilitiesValue;
//            out.writeInt(capabilities.size());
//            for (Object capability : capabilities) {
//                out.writeUTF(null != capability && String.class.isInstance(capability) ? (String)capability : null);
//            }
//        } else {
//            out.writeInt(0);
//        }
//    }
//
//    @Override
//    public void readData(ObjectDataInput in) throws IOException {
//        /*
//         * basic properties
//         */
//        loginName = in.readUTF();
//        password = in.readUTF();
//        contextId = in.readInt();
//        userId = in.readInt();
//        sessionId = in.readUTF();
//        secret = in.readUTF();
//        login = in.readUTF();
//        randomToken = in.readUTF();
//        localIp = in.readUTF();
//        authId = in.readUTF();
//        hash = in.readUTF();
//        client = in.readUTF();
//        userLogin = in.readUTF();
//        /*
//         * special handling for parameters map
//         */
//        String alternativeID = in.readUTF();
//        if (null != alternativeID) {
//            parameters.put(PARAM_ALTERNATIVE_ID, alternativeID);
//        }
//        int capabilitiesSize = in.readInt();
//        if (0 < capabilitiesSize) {
//            List<String> capabilities = new ArrayList<String>();
//            for (int i = 0; i < capabilitiesSize; i++) {
//                capabilities.add(in.readUTF());
//            }
//            parameters.put(PARAM_CAPABILITIES, capabilities);
//        }
//    }
//
//}
