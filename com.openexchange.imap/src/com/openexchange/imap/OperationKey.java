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

package com.openexchange.imap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.imap.services.Services;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;

/**
 * {@link OperationKey} - An operation key.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OperationKey implements Serializable {

    private static final long serialVersionUID = -3628236985679806438L;

    /**
     * The default value associated with a key.
     */
    private static final Object PRESENT = new Object();

    /**
     * Operation type.
     */
    public static enum Type {
        /** Message copy operation */
        MSG_COPY,
        /** Message delete operation */
        MSG_DELETE,
        /** Message label update operation */
        MSG_LABEL_UPDATE,
        /** Message user flags update operation */
        MSG_USER_FLAGS_UPDATE,
        /** Message flags update operation */
        MSG_FLAGS_UPDATE,
        /** Message append operation */
        MSG_APPEND, ;
    }

    // --------------------------------------------------------------------------------------- //

    private final Type type;
    private final int accountId;
    private final Object[] objects;
    private final int hash;

    /**
     * Initializes a new {@link OperationKey}.
     */
    public OperationKey(Type type, int accountId, Object... objects) {
        super();
        this.type = type;
        this.accountId = accountId;
        this.objects = objects;

        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + accountId;
        result = prime * result + Arrays.hashCode(objects);
        hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OperationKey)) {
            return false;
        }
        final OperationKey other = (OperationKey) obj;
        if (type != other.type) {
            return false;
        }
        if (accountId != other.accountId) {
            return false;
        }
        if (!Arrays.equals(objects, other.objects)) {
            return false;
        }
        return true;
    }

    // --------------------------------------------------------------------------------------- //

    private static volatile Boolean synchronizeWriteAccesses;
    private static boolean synchronizeWriteAccesses() {
        Boolean tmp = synchronizeWriteAccesses;
        if (null == tmp) {
            synchronized (OperationKey.class) {
                tmp = synchronizeWriteAccesses;
                if (null == tmp) {
                    final ConfigurationService configService = Services.optService(ConfigurationService.class);
                    if (null == configService) {
                        return false;
                    }
                    tmp = Boolean.valueOf(configService.getBoolProperty("com.openexchange.imap.synchronizeWriteAccesses", false));
                    synchronizeWriteAccesses = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    static {
        IMAPReloadable.getInstance().addReloadable(new Reloadable() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                synchronizeWriteAccesses = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.imap.synchronizeWriteAccesses");
            }
        });
    }

    private static final String IMAP_OPERATIONS = "__imap-operations".intern();

    /**
     * Unsets the marker for specified operation for given session.
     *
     * @param key The operation to unmark
     * @param session The associated session
     */
    public static void unsetMarker(OperationKey key, Session session) {
        @SuppressWarnings("unchecked") final ConcurrentMap<OperationKey, Object> map = (ConcurrentMap<OperationKey, Object>) session.getParameter(IMAP_OPERATIONS);
        if (null != map) {
            map.remove(key);
        }
    }

    /**
     * Sets the marker for specified operation for given session.
     *
     * @param key The operation to mark
     * @param session The associated session
     * @return A positive integer if mark was acquired, zero if not (don't care), or a negative integer if there was already a mark and has
     *         not been releases in time
     */
    @SuppressWarnings("unchecked")
    public static int setMarker(OperationKey key, Session session) {
        if (!synchronizeWriteAccesses()) {
            return 0; // zero -- do not care
        }

        // Acquire mark
        if (session instanceof PutIfAbsent) {
            final PutIfAbsent psession = (PutIfAbsent) session;
            ConcurrentMap<OperationKey, Object> map = (ConcurrentMap<OperationKey, Object>) psession.getParameter(IMAP_OPERATIONS);
            if (null == map) {
                final ConcurrentMap<OperationKey, Object> newMap = new ConcurrentHashMap<OperationKey, Object>(16, 0.9f, 1);
                map = (ConcurrentMap<OperationKey, Object>) psession.setParameterIfAbsent(IMAP_OPERATIONS, newMap);
                if (null == map) {
                    map = newMap;
                }
            }
            return (null == map.putIfAbsent(key, OperationKey.PRESENT)) ? 1 : -1;
        }
        return 0;
    }

}
