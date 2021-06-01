///*
// * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
// * @license AGPL-3.0
// *
// * This code is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
// *
// * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
