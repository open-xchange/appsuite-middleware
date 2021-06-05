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

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.lang.Validate;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.java.Strings;
import com.openexchange.sessiond.impl.SessionHandler;

/**
 * {@link PortableContextSessionsCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class PortableContextSessionsCleaner extends AbstractCustomPortable implements Callable<Set<Integer>> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PortableContextSessionsCleaner.class);

    private static final String FIELD_CONTEXT_IDS = "contextIds";

    private Set<Integer> contextIds;

    /**
     * Initializes a new {@link ClearRemoteContextSessions}.
     *
     * @param contextIds
     */
    public PortableContextSessionsCleaner(Set<Integer> contextIds) {
        Validate.notNull(contextIds, "Mandatory parameter contextIds is missing.");

        this.contextIds = contextIds;
    }

    public PortableContextSessionsCleaner() {
        super();
    }

    /**
     * {@inheritDoc}
     * @throws Exception
     */
    @Override
    public Set<Integer> call() throws Exception {
        try {
            return SessionHandler.removeContextSessions(this.contextIds);
        } catch (Exception exception) {
            LOG.error("Unable to remove sessions for context ids: {}", Strings.concat(", ", contextIds));
            throw exception;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getClassId() {
        return PORTABLE_CONTEXT_SESSIONS_CLEANER_CLASS_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        int[] arr = new int[this.contextIds.size()];
        int index = 0;
        for (Integer i : this.contextIds) {
            arr[index++] = i.intValue();
        }

        writer.writeIntArray(FIELD_CONTEXT_IDS, arr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readPortable(PortableReader reader) throws IOException {
        Set<Integer> lContextIds = new HashSet<Integer>();
        int[] contextIdArray = reader.readIntArray(FIELD_CONTEXT_IDS);
        for (int i : contextIdArray) {
            lContextIds.add(I(i));
        }
        this.contextIds = lContextIds;
    }
}
