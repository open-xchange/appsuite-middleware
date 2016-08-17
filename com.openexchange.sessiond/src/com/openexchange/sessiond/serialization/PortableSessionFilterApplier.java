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

package com.openexchange.sessiond.serialization;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.impl.SessionHandler;


/**
 * {@link PortableSessionFilterApplier}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class PortableSessionFilterApplier extends AbstractCustomPortable implements Callable<Collection<String>> {

    public static enum Action {
        GET(1),
        REMOVE(2);

        private static final Map<Integer, Action> actionsByID = new TreeMap<Integer, Action>();
        static {
            for (Action action : Action.values()) {
                actionsByID.put(action.getID(), action);
            }
        }

        private final int id;

        private Action(int id) {
            this.id = id;
        }

        private int getID() {
            return id;
        }

        private static Action byID(int id) {
            return actionsByID.get(id);
        }
    }

    private static final String FIELD_FILTER_STRING = "FILTER_STRING";

    private static final String FIELD_ACTION = "ACTION";

    private SessionFilter filter;

    private Action action;

    public PortableSessionFilterApplier() {
        super();
    }

    public PortableSessionFilterApplier(SessionFilter filter, Action action) {
        super();
        this.filter = filter;
        this.action = action;
    }

    @Override
    public int getClassId() {
        return CustomPortable.PORTABLE_SESSIONS_FILTER_APPLIER_CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(FIELD_FILTER_STRING, filter.toString());
        writer.writeInt(FIELD_ACTION, action.getID());
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        action = Action.byID(reader.readInt(FIELD_ACTION));
        filter = SessionFilter.create(reader.readUTF(FIELD_FILTER_STRING));
    }

    @Override
    public Collection<String> call() throws Exception {
        switch (action) {
            case GET:
                return SessionHandler.findLocalSessions(filter);
            case REMOVE:
                return SessionHandler.removeLocalSessions(filter);
            default:
                throw new UnsupportedOperationException("Action " + action.name() + " is not implemented!");
        }
    }

    @Override
    public String toString() {
        return "PortableSessionFilterApplier with filter '" + filter.toString() + "' and action '" + action.name() + "'";
    }

}
