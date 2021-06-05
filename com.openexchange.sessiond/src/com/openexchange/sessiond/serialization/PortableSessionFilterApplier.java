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
                actionsByID.put(Integer.valueOf(action.getID()), action);
            }
        }

        private final int id;

        private Action(int id) {
            this.id = id;
        }

        int getID() {
            return id;
        }

        static Action byID(int id) {
            return id < 0 ? null : actionsByID.get(Integer.valueOf(id));
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
