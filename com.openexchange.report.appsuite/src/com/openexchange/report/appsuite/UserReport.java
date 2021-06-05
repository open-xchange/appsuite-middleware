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
import com.openexchange.groupware.contexts.Context;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.user.User;

/**
 * A {@link UserReport} holds information about a user
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UserReport extends Report {

    private static final long serialVersionUID = -8802071039164053141L;

    private final Context ctx;

    private final User user;

    private final ContextReport contextReport;

    /**
     * Initializes a new {@link UserReport}.
     *
     * @param uuid The uuid of this report run
     * @param type The type of report to run
     * @param ctx The context the analyzed user belongs to
     * @param user The analyzed user
     * @param contextReport The accompanying contextReport
     */
    public UserReport(String uuid, String type, Context ctx, User user, ContextReport contextReport) {
        super(uuid, type, -1);
        this.ctx = ctx;
        this.user = user;
        this.contextReport = contextReport;
    }

    /**
     * see {@link Report#set(String, String, Serializable)}
     */
    @Override
    public UserReport set(String ns, String key, Serializable value) {
        super.set(ns, key, value);
        return this;
    }

    public ContextReport getContextReport() {
        return contextReport;
    }

    public User getUser() {
        return user;
    }

    public Context getContext() {
        return ctx;
    }

}
