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

package com.openexchange.user.copy.internal.uwa;

import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.user.copy.internal.CopyTools.getIntOrNegative;
import static com.openexchange.user.copy.internal.CopyTools.setStringOrNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link UWACopyTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UWACopyTask implements CopyUserTaskService {

    private static final String SELECT_WIDGET_SQL = "SELECT id, autorefresh, standalone, title, url, visible, protected, parameters FROM uwaWidget WHERE cid = ? AND user = ?";

    private static final String SELECT_POSITION_SQL = "SELECT adj FROM uwaWidgetPosition WHERE cid = ? AND user = ? AND id = ?";

    private static final String INSERT_WIDGET_SQL = "INSERT INTO uwaWidget (cid, user, id, autorefresh, standalone, title, url, visible, protected, parameters) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_POSITION_SQL = "INSERT INTO uwaWidgetPosition (cid, user, id, adj) VALUES (?, ?, ?, ?)";


    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return "uwaWidget";
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public ObjectMapping<?> copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final Integer srcCtxId = copyTools.getSourceContextId();
        final Integer dstCtxId = copyTools.getDestinationContextId();
        final Integer srcUsrId = copyTools.getSourceUserId();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();

        try {
            if (DBUtils.tableExists(srcCon, "uwaWidget")) {
                final List<Widget> widgets = loadWidgetsFromDB(srcCon, i(srcCtxId), i(srcUsrId));
                writeWidgetsToDB(dstCon, i(dstCtxId), i(dstUsrId), widgets);
            }
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        }

        return null;
    }

    void writeWidgetsToDB(final Connection con, final int cid, final int uid, final List<Widget> widgets) throws OXException {
        PreparedStatement wstmt = null;
        PreparedStatement pstmt = null;
        try {
            wstmt = con.prepareStatement(INSERT_WIDGET_SQL);
            pstmt = con.prepareStatement(INSERT_POSITION_SQL);
            for (final Widget widget : widgets) {
                int i = 1;
                wstmt.setInt(i++, cid);
                wstmt.setInt(i++, uid);
                wstmt.setString(i++, widget.getId());
                wstmt.setInt(i++, widget.isAutorefresh() ? 1 : 0);
                wstmt.setInt(i++, widget.isStandalone() ? 1 : 0);
                setStringOrNull(i++, wstmt, widget.getTitle());
                setStringOrNull(i++, wstmt, widget.getUrl());
                wstmt.setInt(i++, widget.isVisible() ? 1 : 0);
                wstmt.setInt(i++, widget.isProtectedAttr() ? 1 : 0);
                setStringOrNull(i++, wstmt, widget.getParameters());

                wstmt.addBatch();

                final String adj = widget.getAdj();
                if (adj != null) {
                    pstmt.setInt(1, cid);
                    pstmt.setInt(2, uid);
                    pstmt.setString(3, widget.getId());
                    pstmt.setString(4, adj);

                    pstmt.addBatch();
                }
            }

            wstmt.executeBatch();
            pstmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(wstmt);
            DBUtils.closeSQLStuff(pstmt);
        }
    }

    List<Widget> loadWidgetsFromDB(final Connection con, final int cid, final int uid) throws OXException {
        final List<Widget> widgets = new ArrayList<Widget>();
        PreparedStatement wstmt = null;
        PreparedStatement pstmt = null;
        ResultSet wrs = null;
        try {
            wstmt = con.prepareStatement(SELECT_WIDGET_SQL);
            wstmt.setInt(1, cid);
            wstmt.setInt(2, uid);

            wrs = wstmt.executeQuery();
            pstmt = con.prepareStatement(SELECT_POSITION_SQL);
            while (wrs.next()) {
                int i = 1;
                final Widget widget = new Widget();
                final String id = wrs.getString(i++);
                widget.setId(id);
                widget.setAutorefresh(getIntOrNegative(i++, wrs) == 1 ? true : false);
                widget.setStandalone(getIntOrNegative(i++, wrs) == 1 ? true : false);
                widget.setTitle(wrs.getString(i++));
                widget.setUrl(wrs.getString(i++));
                widget.setVisible(getIntOrNegative(i++, wrs) == 1 ? true : false);
                widget.setProtectedAttr(getIntOrNegative(i++, wrs) == 1 ? true : false);
                widget.setParameters(wrs.getString(i++));

                ResultSet prs = null;
                try {
                    pstmt.setInt(1, cid);
                    pstmt.setInt(2, uid);
                    pstmt.setString(3, id);
                    prs = pstmt.executeQuery();
                    if (prs.next()) {
                        widget.setAdj(prs.getString(1));
                    }
                } finally {
                    DBUtils.closeSQLStuff(prs);
                }

                widgets.add(widget);
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(wrs, wstmt);
            DBUtils.closeSQLStuff(pstmt);
        }

        return widgets;
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

}
