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

package com.openexchange.report.appsuite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.annotation.concurrent.NotThreadSafe;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.report.appsuite.serialization.Report;

/**
 * A {@link ContextReport} holds the information discovered about a certain context. See {@link Report}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public @NotThreadSafe class ContextReport extends Report {

    private static final long serialVersionUID = 3879587797122632468L;

    private final Context ctx;

    /**
     * capSToContext - This value is used to store the contexts/users for each capabilitySet
     * in this ContextReport. Each Context can have n users with different capability sets.
     */
    private final LinkedHashMap<String, LinkedHashMap<Integer, ArrayList<Integer>>> capSToContext;
    
    
    /**
     * userList - This value stores all user ids, that are in this context.
     */
    private final ArrayList<Integer> userList;

    /**
     * Initializes a new {@link ContextReport}.
     * 
     * @param uuid The UUID of the report this context report belongs to
     * @param type The report type. This determines which analyzers and cumulators partake in this report run.
     * @param ctx The context about which this report is
     */
    public ContextReport(String uuid, String type, Context ctx) {
        super(uuid, type, -1);
        this.ctx = ctx;
        this.capSToContext = new LinkedHashMap<>();
        this.userList = new ArrayList<>();
    }

    /**
     * @see Report#set(String, String, Serializable)
     */
    @Override
    public ContextReport set(String ns, String key, Serializable value) {
        super.set(ns, key, value);
        return this;
    }

    public Context getContext() {
        return ctx;
    }

    public LinkedHashMap<String, LinkedHashMap<Integer, ArrayList<Integer>>> getCapSToContext() {
        return capSToContext;
    }

    public ArrayList<Integer> getUserList() {
        return userList;
    }

    
}
