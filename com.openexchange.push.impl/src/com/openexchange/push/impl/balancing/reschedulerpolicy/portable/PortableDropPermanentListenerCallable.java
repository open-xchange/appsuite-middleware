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

package com.openexchange.push.impl.balancing.reschedulerpolicy.portable;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.push.PushUser;
import com.openexchange.push.impl.PushManagerRegistry;


/**
 * {@link PortableDropPermanentListenerCallable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PortableDropPermanentListenerCallable extends AbstractCustomPortable implements Callable<Boolean> {

    public static final String PARAMETER_CONTEXT_IDS = "contextIds";
    public static final String PARAMETER_USER_IDS = "userIds";

    private int[] contextIds;
    private int[] userIds;

    /**
     * Initializes a new {@link PortableDropPermanentListenerCallable}.
     */
    public PortableDropPermanentListenerCallable() {
        super();
    }

    /**
     * Initializes a new {@link PortableDropPermanentListenerCallable}.
     *
     * @param source The push user to drop
     */
    public PortableDropPermanentListenerCallable(List<PushUser> pushUsers) {
        super();

        int size = pushUsers.size();
        int[] contextIds = new int[size];
        int[] userIds = new int[size];

        for (int i = size; i-- > 0;) {
            PushUser pushUser = pushUsers.get(i);
            contextIds[i] = pushUser.getContextId();
            userIds[i] = pushUser.getUserId();
        }

        this.contextIds = contextIds;
        this.userIds = userIds;
    }

    @Override
    public Boolean call() throws Exception {
        List<PushUser> pushUsers = new LinkedList<PushUser>();

        int length = userIds.length;
        for (int i = 0; i < length; i++) {
            pushUsers.add(new PushUser(userIds[i], contextIds[i]));
        }

        PushManagerRegistry.getInstance().stopPermanentListenerFor(pushUsers);
        return Boolean.TRUE;
    }

    @Override
    public int getClassId() {
        return 104;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeIntArray(PARAMETER_CONTEXT_IDS, contextIds);
        writer.writeIntArray(PARAMETER_USER_IDS, userIds);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        contextIds = reader.readIntArray(PARAMETER_CONTEXT_IDS);
        userIds = reader.readIntArray(PARAMETER_USER_IDS);
    }

}
