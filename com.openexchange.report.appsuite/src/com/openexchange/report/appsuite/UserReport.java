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

package com.openexchange.report.appsuite;

import java.io.Serializable;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.report.appsuite.serialization.Report;

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
