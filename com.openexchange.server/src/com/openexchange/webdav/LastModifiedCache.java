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

package com.openexchange.webdav;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Date;

/**
 * {@link LastModifiedCache} - Simple cache for last-modified time stamps.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class LastModifiedCache {

    private final TIntObjectMap<LastModifiedMemory> storage;

    /**
     * Initializes a new {@link LastModifiedCache}.
     */
    public LastModifiedCache() {
        storage = new TIntObjectHashMap<LastModifiedMemory>();
    }

    /**
     * Returns the current last-modified time stamp for a given objectId, if the object has been changed in the meantime. Returns the given
     * lastModified, if the object has not been changed or the original lastModified is greater than the given one.
     *
     * @param objectId The object ID
     * @param lastModified The last-modified time stamp
     * @return The current valid last-modified time stamp for the given objectId
     */
    public long getLastModified(final int objectId, final long lastModified) {
        if (storage.containsKey(objectId)) {
            final LastModifiedMemory memory = storage.get(objectId);
            if (lastModified >= memory.getOriginal()) {
                return memory.getCurrent();
            }
        }
        return lastModified;
    }

    public Date getLastModified(final int objectId, final Date lastModified) {
        if (lastModified == null) {
            return null;
        }
        return new Date(getLastModified(objectId, lastModified.getTime()));
    }

    public void update(final int objectId, final int recurrenceId, final Date lastModified) {
        if (lastModified == null) {
            return;
        }

        if (recurrenceId != 0) {
            if (storage.containsKey(recurrenceId)) {
                storage.get(recurrenceId).setCurrent(lastModified.getTime());
            } else {
                storage.put(recurrenceId, new LastModifiedMemory(lastModified.getTime(), lastModified.getTime()));
            }
        }

        if (objectId != 0) {
            if (storage.containsKey(objectId)) {
                storage.get(objectId).setCurrent(lastModified.getTime());
            } else {
                storage.put(objectId, new LastModifiedMemory(lastModified.getTime(), lastModified.getTime()));
            }
        }
    }

    private static class LastModifiedMemory {

        private long original;

        private long current;

        public LastModifiedMemory(final long original, final long current) {
            this.original = original;
            this.current = current;
        }

        public long getOriginal() {
            return original;
        }

        public void setOriginal(final long original) {
            this.original = original;
        }

        public long getCurrent() {
            return current;
        }

        public void setCurrent(final long current) {
            this.current = current;
        }
    }
}
