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
            LOG.error("Unable to remove sessions for context ids: " + Strings.concat(", ", contextIds));
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
            arr[index++] = i;
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
            lContextIds.add(i);
        }
        this.contextIds = lContextIds;
    }
}
