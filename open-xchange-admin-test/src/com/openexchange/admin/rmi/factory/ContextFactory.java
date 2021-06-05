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

package com.openexchange.admin.rmi.factory;

import java.util.Random;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Filestore;

/**
 * {@link ContextFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ContextFactory {

    /**
     * Creates a new {@link Context} object with the specified quota
     * 
     * @param maxQuota the maximum quota of the context
     * @return The new {@link Context} object
     */
    public static Context createContext(long maxQuota) {
        return createContext(getRandomContextId(), maxQuota);
    }

    public static Context createContext(String name) {
        return createContext(getRandomContextId(), name);
    }

    /**
     * Creates a new {@link Context} object with the specified id
     * and max quota
     * 
     * @param contextId The context identifier
     * @param maxQuota The maximum quota of the context
     * @return The new {@link Context} object
     */
    public static Context createContext(int contextId, long maxQuota) {
        Context context = new Context(Integer.valueOf(contextId));
        context.setMaxQuota(maxQuota);
        return context;
    }

    /**
     * Creates a new {@link Context} object with 128M filestore
     * and the specified id and name
     * 
     * @param contextId The context identifier
     * @param name The context's name
     * @return The new {@link Context} object
     */
    public static Context createContext(int contextId, String name) {
        Context newContext = new Context(Integer.valueOf(contextId));
        Filestore filestore = new Filestore();
        filestore.setSize(Long.valueOf(128l));
        newContext.setFilestoreId(filestore.getId());
        newContext.setName(name);
        newContext.setMaxQuota(filestore.getSize());
        return newContext;
    }

    public static int getRandomContextId() {
        Random r = new Random();
        return r.ints(11, (10000000 + 1)).findFirst().getAsInt();
    }

}
