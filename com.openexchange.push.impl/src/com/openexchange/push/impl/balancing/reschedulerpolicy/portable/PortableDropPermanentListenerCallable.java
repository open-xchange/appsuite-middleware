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
