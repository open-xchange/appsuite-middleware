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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.consistency;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;

/**
 * Provides the integration of the consistency tool in the OSGi OX.
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class OsgiOXConsistency extends Consistency {

    private DatabaseImpl database;

    @Override
    protected Context getContext(final int contextId) throws OXException {
        final ContextStorage ctxstor = ContextStorage.getInstance();
        return ctxstor.getContext(contextId);
    }

    @Override
    protected DatabaseImpl getDatabase() {
        if (database == null) {
            database = new DatabaseImpl(new DBPoolProvider());
        }
        return database;
    }

    @Override
    protected AttachmentBase getAttachments() {
        return Attachments.getInstance();
    }

    @Override
    protected FileStorage getFileStorage(final Context ctx) throws OXException {
        return QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx);
    }

    @Override
    protected List<Context> getContextsForFilestore(final int filestoreId) throws OXException {
        // Dear Santa.
        // For next christmas I would like to have blocks and closures
        // for Java.
        // Thanks
        //   Francisco

        return filter(getAllContexts(), new Filter() {
            @Override
            public boolean accepts(final Context ctx) {
                return ctx.getFilestoreId() == filestoreId;
            }
        });
    }

    @Override
    protected List<Context> getContextsForDatabase(final int databaseId) throws OXException {
        final DatabaseService configDB = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);
        final int[] contextIds = configDB.listContexts(databaseId);
        final List<Integer> ctxIds = new ArrayList<Integer>(contextIds.length);
        for (int i = 0; i < contextIds.length; i++) {
            ctxIds.add(com.openexchange.java.Autoboxing.I(contextIds[i]));
        }
        return loadContexts(ctxIds);
    }

    private List<Context> filter(final List<Context> contexts, final Filter filter) {
        final List<Context> filtered = new ArrayList<Context>();
        for(final Context ctx : contexts) {
            if(filter.accepts(ctx)) {
                filtered.add(ctx);
            }
        }
        return filtered;
    }

    @Override
    protected List<Context> getAllContexts() throws OXException {
        final ContextStorage ctxstor = ContextStorage.getInstance();
        final List<Integer> list = ctxstor.getAllContextIds();

        return loadContexts(list);
    }

    private List<Context> loadContexts(final List<Integer> list) throws OXException {
        final ContextStorage ctxstor = ContextStorage.getInstance();
        final List<Context> contexts = new ArrayList<Context>(list.size());
        for(final int id : list) {
            contexts.add(ctxstor.getContext(id));
        }
        return contexts;
    }

    @Override
    protected User getAdmin(final Context ctx) throws OXException {
        return UserStorage.getInstance().getUser(ctx.getMailadmin(),ctx);
    }


    private interface Filter {
        public boolean accepts(Context ctx);
    }
}
