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

package com.openexchange.user.copy.internal.connection;

import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.context.ContextLoadTask;

/**
 * Fetches a read connection for the source context and a write connection for the destination context. Puts connection for the destination
 * context into transaction mode.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConnectionFetcherTask implements CopyUserTaskService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConnectionFetcherTask.class);

    private final DatabaseService service;

    private Context srcCtx;
    private int dstCtxId;
    private Connection srcCon;
    private Connection dstCon;

    public ConnectionFetcherTask(final DatabaseService service) {
        super();
        this.service = service;
    }

    @Override
    public String[] getAlreadyCopied() {
        return new String[] { ContextLoadTask.class.getName() };
    }

    @Override
    public String getObjectName() {
        return Connection.class.getName();
    }

    @Override
    public ConnectionHolder copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools tools = new CopyTools(copied);
        srcCtx = tools.getSourceContext();
        dstCtxId = i(tools.getDestinationContextId());
        srcCon = service.getReadOnly(srcCtx);

        try {
            dstCon = service.getForUpdateTask(dstCtxId);
        } catch (final OXException e) {
            service.backReadOnly(srcCtx, srcCon);
            throw e;
        }

        try {
            DBUtils.startTransaction(dstCon);
        } catch (final SQLException e) {
            service.backForUpdateTask(dstCtxId, dstCon);
            service.backReadOnly(srcCtx, srcCon);
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        }
        final ConnectionHolder retval = new ConnectionHolder();
        retval.addMapping(srcCtx.getContextId(), srcCon, dstCtxId, dstCon);
        return retval;
    }

    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
        if (null != dstCon) {
            try {
                if (failed) {
                    dstCon.rollback();
                } else {
                    dstCon.commit();
                }
            } catch (final SQLException e) {
                LOG.error("", e);
            }
            service.backForUpdateTask(dstCtxId, dstCon);
            dstCon = null;
        }
        if (null != srcCon) {
            service.backReadOnly(srcCtx, srcCon);
            srcCon = null;
        }
        dstCtxId = 0;
        srcCtx = null;
    }
}
