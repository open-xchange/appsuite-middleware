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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.mail.internet.idn.IDNA;
import org.osgi.framework.BundleContext;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.properties.PropertyScope;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.sqlStorage.OXToolSQLStorage;
import com.openexchange.admin.storage.utils.Filestore2UserUtil;
import com.openexchange.admin.storage.utils.PoolAndSchema;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.GenericChecks;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.BitAND;
import com.openexchange.sql.grammar.BitOR;
import com.openexchange.sql.grammar.Column;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INVERT;
import com.openexchange.sql.grammar.Table;
import com.openexchange.sql.grammar.UPDATE;
import com.openexchange.tools.oxfolder.OXFolderDefaultMode;

/**
 * {@link OXToolMySQLStorage}
 *
 * @author d7
 * @author cutmasta
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OXToolMySQLStorage extends OXToolSQLStorage implements OXMySQLDefaultValues {

    /** The logger constant */
    final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXToolMySQLStorage.class);

    private static final String FALLBACK_LANGUAGE_CREATE = "en";
    private static final String FALLBACK_COUNTRY_CREATE = "US";

    @Override
    public boolean domainInUse(final Context ctx, final String domain) throws StorageException {
        int contextId = ctx.getId().intValue();
        Connection con = null;
        try {
            con = cache.getConnectionForContext(contextId);
            final Resource[] res = getDomainUsedbyResource(ctx, domain, con);
            final Group[] grp = getDomainUsedbyGroup(ctx, domain, con);
            final User[] usr = getDomainUsedbyUser(ctx, domain, con);
            return (res != null || grp != null || usr != null);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public Group[] domainInUseByGroup(final Context ctx, final String domain) {
        // currently mailaddresse not used  in core for groups
        return null;
    }

    @Override
    public Resource[] domainInUseByResource(final Context ctx, final String domain) throws StorageException {
        int context_id = ctx.getId().intValue();

        Connection con = null;
        try {
            con = cache.getConnectionForContext(context_id);
            return getDomainUsedbyResource(ctx, domain, con);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(context_id, con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public User[] domainInUseByUser(final Context ctx, final String domain) throws StorageException {
        int context_id = ctx.getId().intValue();

        Connection con = null;
        try {
            con = cache.getConnectionForContext(context_id);
            return getDomainUsedbyUser(ctx, domain, con);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(context_id, con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db read connection to pool!", e);
                }
            }
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsContext(int)
     */
    @Override
    public boolean existsContext(final Context ctx) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            stmt = con.prepareStatement("SELECT 1 FROM context WHERE cid = ?");
            stmt.setInt(1, ctx.getId().intValue());
            rs = stmt.executeQuery();
            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    @Override
    public boolean existsContextInServer(Context ctx) throws StorageException {
        Connection con = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            return existsContextInServer(ctx, con);
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    /**
     * Checks if specified context does exist in the registered server this provisioning is running in.
     * <p>
     * Although this method accepts a context Object it will only look after the context identifier
     *
     * @param ctx The context providing the context identifier
     * @param con The connection to use or <code>null</code>
     * @return <code>true</code> if context exists in server; otherwise <code>false</code> (either non-existing at all or resides in another server)
     * @throws StorageException If checks fails
     */
    public boolean existsContextInServer(Context ctx, Connection con) throws StorageException {
        if (null == con) {
            return existsContextInServer(ctx);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int serverId = cache.getServerId();
            stmt = con.prepareStatement("SELECT 1 FROM context AS c JOIN context_server2db_pool AS c2s ON c.cid=c2s.cid WHERE c2s.server_id=? AND c.cid=?");
            stmt.setInt(1, serverId);
            stmt.setInt(2, ctx.getId().intValue());
            rs = stmt.executeQuery();
            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /*
     * Check if any login mapping in the given context already exists in the system
     */
    @Override
    public boolean existsContextLoginMappings(final Context ctx) throws StorageException {
        Connection con = null;
        try {
            con = cache.getConnectionForContext(ctx.getId().intValue());
            return existsContextLoginMappings(ctx, con);
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (con != null) {
                try {
                    cache.pushConnectionForContextAfterReading(ctx.getId().intValue(), con);
                } catch (PoolException e) {
                    log.error("Error pushing configdb write connection to pool!", e);
                }
            }
        }
    }

    /*
     * Check if any login mapping in the given context already exists in the system
     */
    @Override
    public boolean existsContextLoginMappings(final Context ctx, final Connection configdb_connection) throws StorageException {
        if (ctx.getLoginMappings() != null) {
            boolean retval = false;
            // check if any sent mapping entry already exists
            PreparedStatement prep_check = null;
            ResultSet rs = null;
            try {
                final HashSet<String> logmaps = ctx.getLoginMappings();

                for (final String mpi : logmaps) {
                    if (Strings.containsSurrogatePairs(mpi)) {
                        continue;
                    }
                    prep_check = configdb_connection.prepareStatement("SELECT cid from login2context where login_info = ?");
                    prep_check.setString(1, mpi);
                    rs = prep_check.executeQuery();
                    if (rs.next()) {
                        retval = true;
                    }
                    rs.close();
                    prep_check.close();
                    if (retval) {
                        break;
                    }
                }
                return retval;
            } catch (SQLException e) {
                log.error("SQL Error", e);
                throw new StorageException(e.toString());
            } finally {
                closeRecordSet(rs);
                closePreparedStatement(prep_check);
            }
        }
        return false;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsDatabase(int)
     */
    @Override
    public boolean existsDatabase(final int db_id) throws StorageException {
        return selectwithint(-1, "SELECT name FROM db_pool WHERE db_pool_id = ?", db_id);
    }

    @Override
    public boolean existsDisplayName(final Context ctx, final User user, final int userId) throws StorageException {
        int ctxId = i(ctx.getId());
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(ctxId);
            stmt = con.prepareStatement("SELECT field01,userid FROM prg_contacts WHERE cid=? AND field01=? AND fid=?");
            stmt.setInt(1, ctxId);
            stmt.setString(2, user.getDisplay_name());
            stmt.setInt(3, FolderObject.SYSTEM_LDAP_FOLDER_ID);
            rs = stmt.executeQuery();

            boolean foundOther = false;
            while (!foundOther && rs.next()) {
                foundOther = user.getDisplay_name().equals(rs.getString(1)) && (userId == 0 || userId != rs.getInt(2));
            }
            return foundOther;
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(ctxId, con);
                } catch (PoolException e) {
                    log.error("Error pushing context connection to pool.", e);
                }
            }
        }
    }

    @Override
    public boolean existsGroup(final Context ctx, final Group[] grps) throws StorageException {
        boolean retBool = false;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(ctx.getId().intValue());
            for (final Group grp : grps) {
                prep_check = con.prepareStatement("SELECT id FROM groups WHERE cid = ? AND id = ?;");
                prep_check.setInt(1, ctx.getId().intValue());
                prep_check.setInt(2, grp.getId().intValue());
                rs = prep_check.executeQuery();

                if (rs.next()) {
                    retBool = true;
                    prep_check.close();
                } else {
                    prep_check.close();
                    return false;
                }
            }
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);
            if (con != null) {
                try {
                    cache.pushConnectionForContextAfterReading(ctx.getId().intValue(), con);
                } catch (PoolException e) {
                    log.error("Error pushing configdb write connection to pool!", e);
                }
            }
        }
        return retBool;
    }

    @Override
    public boolean existsGroup(final Context ctx, final Group grp) throws StorageException {
        return existsGroup(ctx, new Group[] { grp });
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroup(int,
     *      int)
     */
    @Override
    public boolean existsGroup(final Context ctx, final int gid) throws StorageException {
        return selectwithint(ctx.getId().intValue(), "SELECT id FROM groups WHERE cid = ? AND id = ?;", ctx.getId().intValue(), gid);
    }

    @Override
    public boolean existsGroup(final Context ctx, final Connection con, final int id) throws StorageException {
        return selectwithint(con, "SELECT id FROM groups WHERE cid = ? AND id = ?;", ctx.getId().intValue(), id);
    }

    /**
     * @deprecated Use method with User[] instead
     */
    @Override
    @Deprecated
    public boolean existsGroup(final Context ctx, final int[] gids) throws StorageException {
        boolean retBool = false;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(ctx.getId().intValue());
            for (final int elem : gids) {
                prep_check = con.prepareStatement("SELECT id FROM groups WHERE cid = ? AND id = ?;");
                prep_check.setInt(1, ctx.getId().intValue());
                prep_check.setInt(2, elem);
                rs = prep_check.executeQuery();

                if (rs.next()) {
                    retBool = true;
                    prep_check.close();
                } else {
                    prep_check.close();
                    return false;
                }
            }
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);
            if (con != null) {
                try {
                    cache.pushConnectionForContextAfterReading(ctx.getId().intValue(), con);
                } catch (PoolException e) {
                    log.error("Error pushing configdb write connection to pool!", e);
                }
            }
        }
        return retBool;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroupMember(int,
     *      int, int)
     */
    @Override
    public boolean existsGroupMember(final Context ctx, final int group_ID, final int member_ID) throws StorageException {
        return selectwithint(ctx.getId().intValue(), "SELECT id FROM groups_member WHERE cid = ? AND id = ? AND member = ?", ctx.getId().intValue(), group_ID, member_ID);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroupMember(int,
     *      int, int[])
     */
    @Override
    public boolean existsGroupMember(final Context ctx, final int group_ID, final int[] user_ids) throws StorageException {
        final int contextId = ctx.getId().intValue();

        boolean ret = false;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try {
            final StringBuffer sb = new StringBuffer();
            for (final int element : user_ids) {
                sb.append(element + ",");
            }
            sb.delete(sb.length() - 1, sb.length());
            con = cache.getConnectionForContext(contextId);
            prep = con.prepareStatement("SELECT member FROM groups_member WHERE cid = ? AND id = ? AND member IN (" + sb.toString() + ")");
            prep.setInt(1, contextId);
            prep.setInt(2, group_ID);
            rs = prep.executeQuery();
            if (rs.next()) {
                // one of the members is already in this group
                ret = true;
            }
            prep.close();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException ecp) {
                    log.error("Error pushing ox db write connection to pool!", ecp);
                }
            }
        }
        return ret;
    }

    @Override
    public boolean existsGroupMember(final Context ctx, final int group_ID, final User[] users) throws StorageException {
        final int[] ids = new int[users.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = users[i].getId().intValue();
        }
        return existsGroupMember(ctx, group_ID, ids);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsReason(int)
     */
    @Override
    public boolean existsReason(final int rid) throws StorageException {
        return selectwithint(-1, "SELECT id FROM reason_text WHERE id = ?;", rid);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsReason(java.lang.String)
     */
    @Override
    public boolean existsReason(final String reason) throws StorageException {
        return selectwithstring(-1, "SELECT id FROM reason_text WHERE text = ?;", reason);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsResource(int,
     *      int)
     */
    @Override
    public boolean existsResource(final Context ctx, final int resource_id) throws StorageException {
        final int contextId = ctx.getId().intValue();

        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(contextId);

            prep_check = con.prepareStatement("SELECT id FROM resource WHERE cid = ? AND id = ?");
            prep_check.setInt(1, contextId);
            prep_check.setInt(2, resource_id);
            rs = prep_check.executeQuery();
            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing ox write connection to pool!", e);
                }
            }
        }
    }

    @Override
    public boolean existsResourceAddress(final Context ctx, final String address) throws StorageException {
        final int context_id = ctx.getId().intValue();

        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {

            con = cache.getConnectionForContext(context_id);

            prep_check = con.prepareStatement("SELECT mail FROM resource WHERE cid = ? AND mail = ?");
            prep_check.setInt(1, context_id);
            prep_check.setString(2, address);
            rs = prep_check.executeQuery();
            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (con != null) {
                try {
                    cache.pushConnectionForContextAfterReading(context_id, con);
                } catch (PoolException e) {
                    log.error("Error pushing ox write connection to pool!", e);
                }
            }
        }
    }

    @Override
    public boolean existsResourceAddress(final Context ctx, final String address, final Integer resource_id) throws StorageException {
        final int context_id = ctx.getId().intValue();

        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(context_id);

            prep_check = con.prepareStatement("SELECT id from resource where cid = ? and mail = ? AND id != ?");
            prep_check.setInt(1, context_id);
            prep_check.setString(2, address);
            prep_check.setInt(3, resource_id.intValue());
            rs = prep_check.executeQuery();
            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (con != null) {
                try {
                    cache.pushConnectionForContextAfterReading(context_id, con);
                } catch (PoolException e) {
                    log.error("Error pushing ox write connection to pool!", e);
                }
            }
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsServer(int)
     */
    @Override
    public boolean existsServer(final int server_id) throws StorageException {
        return selectwithint(-1, "SELECT server_id FROM server WHERE server_id = ?", server_id);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsServerID(int,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public boolean existsServerID(final int check_ID, final String table, final String field) throws StorageException {
        return selectwithint(-1, "SELECT server_id FROM " + table + " WHERE " + field + " = ?;", check_ID);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsStore(int)
     */
    @Override
    public boolean existsStore(final int store_id) throws StorageException {
        return selectwithint(-1, "SELECT uri FROM filestore WHERE id = ?", store_id);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsStore(java.lang.String)
     */
    @Override
    public boolean existsStore(final String url) throws StorageException {
        return selectwithstring(-1, "SELECT uri FROM filestore WHERE uri = ?", url);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      int)
     */
    @Override
    public boolean existsUser(final Context ctx, final int uid) throws StorageException {
        final int contextId = ctx.getId().intValue();

        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(contextId);

            prep_check = con.prepareStatement("SELECT id FROM user WHERE cid = ? AND id = ?;");
            prep_check.setInt(1, contextId);
            prep_check.setInt(2, uid);
            rs = prep_check.executeQuery();
            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (con != null) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing configdb write connection to pool!", e);
                }
            }
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      int[])
     */
    @Override
    public boolean existsUser(final Context ctx, final int[] user_ids) throws StorageException {
        int contextId = ctx.getId().intValue();

        boolean ret = false;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try {
            final StringBuffer sb = new StringBuffer();
            for (int j = user_ids.length; j-- > 0;) {
                sb.append("?,");
            }
            sb.delete(sb.length() - 1, sb.length());
            con = cache.getConnectionForContext(contextId);
            prep = con.prepareStatement("SELECT id FROM user WHERE cid = ? AND id IN (" + sb.toString() + ")");
            prep.setInt(1, contextId);

            int prep_index = 2;
            for (final int element : user_ids) {
                prep.setInt(prep_index, element);
                prep_index++;
            }

            rs = prep.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
            }
            rs.close();

            if (count == user_ids.length) {
                // ok, those users all exist
                ret = true;
            }
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closePreparedStatement(prep);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException ecp) {
                    log.error("Error pushing ox db write connection to pool!", ecp);
                }
            }
        }
        return ret;
    }

    @Override
    public boolean existsUser(final Context ctx, final User[] users) throws StorageException {
        int intValue = ctx.getId().intValue();

        boolean autoLowerCase = cache.getProperties().getUserProp(AdminProperties.User.AUTO_LOWERCASE, false);

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        PreparedStatement prep2 = null;
        try {
            con = cache.getConnectionForContext(intValue);
            prep = con.prepareStatement("SELECT id FROM user WHERE cid = ? AND id = ?");
            prep.setInt(1, intValue);
            prep2 = con.prepareStatement("SELECT id FROM login2user WHERE cid = ? AND uid = ?");
            prep2.setInt(1, intValue);
            for (User user : users) {
                Integer userid = user.getId();
                String username = user.getName();
                if (null != userid) {
                    prep.setInt(2, userid.intValue());
                    rs = prep.executeQuery();
                    if (!rs.next()) {
                        return false;
                    }
                    rs.close();
                } else if (null != username) {
                    if (autoLowerCase) {
                        username = username.toLowerCase();
                    }
                    prep2.setString(2, username);
                    rs = prep.executeQuery();
                    if (rs.next()) {
                        user.setId(I(rs.getInt(1)));
                    } else {
                        return false;
                    }
                    rs.close();
                } else {
                    throw new StorageException("One user object doesn't contain a user id or user name");
                }
            }
            return true;
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            if (prep != null) {
                try {
                    prep.close();
                } catch (SQLException e) {
                    log.error("Error closing statement", e);
                }
            }

            if (prep2 != null) {
                try {
                    prep2.close();
                } catch (SQLException e) {
                    log.error("Error closing statement", e);
                }
            }

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(intValue, con);
                } catch (PoolException ecp) {
                    log.error("Error pushing ox db write connection to pool!", ecp);
                }
            }
        }
    }

    @Override
    public boolean isGuestUser(Context ctx, int userId) throws StorageException {
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try {
            con = cache.getConnectionForContext(ctx.getId().intValue());
            prep = con.prepareStatement("SELECT 1 FROM user WHERE cid=? AND id=? AND guestCreatedBy > 0");
            prep.setInt(1, ctx.getId().intValue());
            prep.setInt(2, userId);
            rs = prep.executeQuery();
            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            if (prep != null) {
                try {
                    prep.close();
                } catch (SQLException e) {
                    log.error("Error closing statement", e);
                }
            }
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(ctx.getId().intValue(), con);
                } catch (PoolException ecp) {
                    log.error("Error pushing ox db write connection to pool!", ecp);
                }
            }
        }
    }

    @Override
    public boolean existsUser(final Context ctx, final User user) throws StorageException {
        // FIXME: Should be rewritten to optimize performance
        return existsUser(ctx, new User[] { user });
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#getAdminForContext(int,
     *      java.sql.Connection)
     */
    @Override
    public int getAdminForContext(final Context ctx, final Connection con) throws StorageException {
        int admin_id = 1;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT user FROM user_setting_admin WHERE cid = ?");
            stmt.setInt(1, ctx.getId().intValue());
            rs = stmt.executeQuery();
            if (rs.next()) {
                admin_id = rs.getInt("user");
            } else {
                throw new SQLException("Unable to determine admin for context " + ctx.getId());
            }
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            closePreparedStatement(stmt);
        }
        return admin_id;
    }

    @Override
    public int getAdminForContext(final Context ctx) throws StorageException {
        int contextId = ctx.getId().intValue();

        Connection con = null;
        try {
            con = cache.getConnectionForContext(contextId);
            return getAdminForContext(ctx, con);
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (con != null) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing oxdb read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public int getContextIDByContextname(String ctxName) throws StorageException, NoSuchObjectException {
        return getByNameForConfigDB(ctxName, "context", "SELECT cid FROM context WHERE name=?");
    }

    @Override
    public int getDatabaseIDByDatabasename(String dbName) throws StorageException, NoSuchObjectException {
        return getByNameForConfigDB(dbName, "database", "SELECT db_pool_id FROM db_pool WHERE name=?");
    }

    @Override
    public int getDatabaseIDByDatabaseSchema(String schemaName) throws StorageException, NoSuchObjectException {
        return getByNameForConfigDB(schemaName, "database", "SELECT write_db_pool_id FROM context_server2db_pool WHERE db_schema=?");
    }

    @Override
    public Database getSchemaByContextId(int contextId) throws StorageException, NoSuchObjectException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            stmt = con.prepareStatement("SELECT c2db.write_db_pool_id,c2db.db_schema,db.url,db.driver,db.login,db.password,db.name,dbc.read_db_pool_id,dbc.max_units FROM context_server2db_pool c2db JOIN db_pool db ON db.db_pool_id=c2db.write_db_pool_id JOIN db_cluster dbc ON dbc.write_db_pool_id=c2db.write_db_pool_id WHERE c2db.cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new NoSuchObjectException("No database for context " + contextId);
            }

            Database retval = new Database();
            int pos = 1;
            retval.setId(I(rs.getInt(pos++)));
            retval.setScheme(rs.getString(pos++));
            retval.setUrl(rs.getString(pos++));
            retval.setDriver(rs.getString(pos++));
            retval.setLogin(rs.getString(pos++));
            retval.setPassword(rs.getString(pos++));
            retval.setName(rs.getString(pos++));
            int slaveId = rs.getInt(pos++);
            if (slaveId > 0) {
                retval.setRead_id(I(slaveId));
            }
            retval.setMaxUnits(I(rs.getInt(pos++)));
            return retval;
        } catch (PoolException e) {
            log.error("", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public DatabaseUpdateException generateDatabaseUpdateException(int contextId) {
        DatabaseUpdateException e;
        if (contextId > 0) {
            try {
                // Schema "db1_1273" from database 1996 on host "db-master.host.invalid" is locked or is now being updated, please try again later
                Database db = getSchemaByContextId(contextId);
                StringBuilder sb = new StringBuilder(256);
                sb.append("Schema \"").append(db.getScheme()).append("\" from database ").append(db.getId());
                String host = extractHostFrom(db);
                if (null != host) {
                    sb.append(" on host \"").append(host).append("\"");
                }
                sb.append(" is locked or is now being updated, please try again later");
                e = new DatabaseUpdateException(sb.toString());
            } catch (NoSuchObjectException x) {
                e = new DatabaseUpdateException("Database is locked or is now being updated, please try again later");
            } catch (StorageException x) {
                e = new DatabaseUpdateException("Database is locked or is now being updated, please try again later");
            }
        } else {
            e = new DatabaseUpdateException("Database is locked or is now being updated, please try again later");
        }
        return e;
    }

    private static String extractHostFrom(Database db) {
        if (null == db) {
            return null;
        }

        String url = db.getUrl();
        if (Strings.isEmpty(url)) {
            return null;
        }

        try {
            return new URI(url.substring("jdbc:".length())).getHost();
        } catch (URISyntaxException e) {
            log.error("Failed to extract host name from URL {}", url, e);
            return null;
        }
    }

    @Override
    public boolean isDistinctWritePoolIDForSchema(String schema) throws StorageException, NoSuchObjectException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            stmt = con.prepareStatement("SELECT DISTINCT write_db_pool_id FROM context_server2db_pool WHERE db_schema = ?");
            stmt.setString(1, schema);
            rs = stmt.executeQuery();

            int numPools = 0;
            while (rs.next()) {
                numPools++;
            }

            if (numPools == 0) {
                throw new NoSuchObjectException("No such schema " + schema);
            }

            return numPools == 1;
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (con != null) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing oxdb read connection to pool!", e);
                }
            }
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#getDefaultGroupForContext(int,
     *      java.sql.Connection)
     */
    @Override
    public int getDefaultGroupForContext(final Context ctx, final Connection con) throws StorageException {
        int group_id = 0;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT MIN(id) FROM groups WHERE cid=?");
            stmt.setInt(1, ctx.getId().intValue());
            rs = stmt.executeQuery();
            if (rs.next()) {
                group_id = rs.getInt("MIN(id)");
            } else {
                throw new SQLException("UNABLE TO GET DEFAULT GROUP FOR CONTEXT " + ctx.getId());
            }
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset!", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("Error closing prepared statement!", e);
                }
            }
        }
        return group_id;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#getDefaultGroupForContext(int,
     *      java.sql.Connection)
     */
    @Override
    public int getDefaultGroupForContextWithOutConnection(final Context ctx) throws StorageException {
        int group_id = 0;
        Connection con = null;
        int context_id = ctx.getId().intValue();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(context_id);
            stmt = con.prepareStatement("SELECT MIN(id) FROM groups WHERE cid=?");
            stmt.setInt(1, context_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("UNABLE TO GET DEFAULT GROUP FOR CONTEXT " + ctx.getId());
            }
            group_id = rs.getInt("MIN(id)");
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset!", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("Error closing prepared statement!", e);
                }
            }
            if (con != null) {
                try {
                    cache.pushConnectionForContextAfterReading(context_id, con);
                } catch (PoolException e) {
                    log.error("Error pushing oxdb read connection to pool!", e);
                }
            }
        }

        return group_id;
    }

    @Override
    public int getGidNumberOfGroup(final Context ctx, final int group_id, final Connection con) throws StorageException {
        int gid_number = -1;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT gidNumber FROM groups WHERE cid=? AND id=?");
            stmt.setInt(1, ctx.getId().intValue());
            stmt.setInt(2, group_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new StorageException(new StringBuilder(32).append("No group with ID ").append(group_id).append(" in context ").append(ctx.getId().intValue()).toString());
            }
            gid_number = rs.getInt("gidNumber");
            if (rs.wasNull()) {
                gid_number = -1;
            }
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(stmt);
        }
        return gid_number;
    }

    @Override
    public int getGroupIDByGroupname(Context ctx, String groupName) throws StorageException, NoSuchGroupException {
        final Connection con;
        try {
            con = cache.getConnectionForContext(i(ctx.getId()));
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        }

        boolean autoLowerCase = cache.getProperties().getGroupProp(AdminProperties.Group.AUTO_LOWERCASE, false);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id from groups where cid=? and identifier=?");
            stmt.setInt(1, i(ctx.getId()));
            stmt.setString(2, autoLowerCase ? groupName.toLowerCase() : groupName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new NoSuchGroupException("No such group " + groupName + " in context " + ctx.getId());
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(rs, stmt);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(i(ctx.getId()), con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public String getGroupnameByGroupID(final Context ctx, final int group_id) throws StorageException {
        int contextId = ctx.getId().intValue();

        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(contextId);
            prep_check = con.prepareStatement("SELECT identifier from groups where cid = ? and id = ?");
            prep_check.setInt(1, contextId);
            prep_check.setInt(2, group_id);
            rs = prep_check.executeQuery();
            if (!rs.next()) {
                throw new StorageException("No such group " + group_id + " in context " + contextId);
            }
            // grab username and return
            return rs.getString("identifier");
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public int getResourceIDByResourcename(Context ctx, String resourceName) throws StorageException, NoSuchResourceException {
        final Connection con;
        try {
            con = cache.getConnectionForContext(i(ctx.getId()));
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        }

        boolean autoLowerCase = cache.getProperties().getResourceProp(AdminProperties.Resource.AUTO_LOWERCASE, false);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id from resource where cid=? and identifier=?");
            stmt.setInt(1, i(ctx.getId()));
            stmt.setString(2, autoLowerCase ? resourceName.toLowerCase() : resourceName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new NoSuchResourceException("No such resource " + resourceName + " in context " + ctx.getId());
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            try {
                cache.pushConnectionForContextAfterReading(i(ctx.getId()), con);
            } catch (PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }
        }
    }

    @Override
    public String getResourcenameByResourceID(final Context ctx, final int resource_id) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(ctx.getId().intValue());
            prep_check = con.prepareStatement("SELECT identifier from resource where cid = ? and id = ?");
            prep_check.setInt(1, ctx.getId().intValue());
            prep_check.setInt(2, resource_id);
            rs = prep_check.executeQuery();
            if (!rs.next()) {
                throw new StorageException("No such resource " + resource_id + " in context " + ctx.getId().intValue());
            }
            // grab user name and return
            return rs.getString("identifier");
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(ctx.getId().intValue(), con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public int getServerIDByServername(String serverName) throws StorageException, NoSuchObjectException {
        return getByNameForConfigDB(serverName, "server", "SELECT server_id FROM server WHERE name=?");
    }

    @Override
    public int getUserIDByUsername(Context ctx, String userName) throws StorageException, NoSuchUserException {
        final Connection con;
        try {
            con = cache.getConnectionForContext(i(ctx.getId()));
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        }

        boolean autoLowerCase = cache.getProperties().getUserProp(AdminProperties.User.AUTO_LOWERCASE, false);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id from login2user where cid=? and uid=?");
            stmt.setInt(1, i(ctx.getId()));
            stmt.setString(2, autoLowerCase ? userName.toLowerCase() : userName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                // grab user id and return
                return rs.getInt(1);
            }
            throw new NoSuchUserException("No such user " + userName + " in context " + ctx.getId());
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(rs, stmt);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(i(ctx.getId()), con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public String getUsernameByUserID(final Context ctx, final int user_id) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(ctx.getId().intValue());
            if (isGuest(con, ctx, user_id)) {
                prep_check = con.prepareStatement("SELECT field65 FROM prg_contacts WHERE cid = ? AND userid = ?");
                prep_check.setInt(1, ctx.getId().intValue());
                prep_check.setInt(2, user_id);
                rs = prep_check.executeQuery();
                if (rs.next() && !rs.getString(1).isEmpty() && !"".equals(rs.getString(1))) {
                    return rs.getString(1) + " (Guest)";
                }
                return "Anonymous Guest";
            }
            prep_check = con.prepareStatement("SELECT uid from login2user where cid = ? and id = ?");
            prep_check.setInt(1, ctx.getId().intValue());
            prep_check.setInt(2, user_id);
            rs = prep_check.executeQuery();
            if (!rs.next()) {
                throw new StorageException("No such user " + user_id + " in context " + ctx.getId().intValue());
            }
            // grab user name and return
            return rs.getString("uid");
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(ctx.getId().intValue(), con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db read connection to pool!", e);
                }
            }
        }
    }

    @Override
    public boolean getIsGuestByUserID(final Context ctx, final int user_id) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(ctx.getId().intValue());
            return isGuest(con, ctx, user_id);
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(ctx.getId().intValue(), con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db read connection to pool!", e);
                }
            }
        }
    }

    private boolean isGuest(Connection con, Context ctx, int userId) throws SQLException {
        String sql = "SELECT id FROM user WHERE cid = ? AND id = ? AND guestCreatedBy > 0";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, ctx.getId().intValue());
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(stmt);
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#isContextAdmin(int,
     *      int)
     */
    @Override
    public boolean isContextAdmin(final Context ctx, final int user_id) throws StorageException {
        final int contextId = ctx.getId().intValue();

        boolean isadmin = false;
        Connection con = null;

        try {
            con = cache.getConnectionForContext(contextId);
            final int a = getAdminForContext(ctx, con);
            if (a == user_id) {
                isadmin = true;
            }
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (con != null) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing oxdb read connection to pool!", e);
                }
            }
        }
        return isadmin;
    }

    @Override
    public boolean isContextAdmin(final Context ctx, final User user) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        final String username = user.getName();
        final Integer userid = user.getId();
        final Integer ctxid = ctx.getId();
        try {
            if (null == userid) {
                if (null == username) {
                    throw new StorageException("No id or user name given");
                }
                con = cache.getConnectionForContext(ctxid.intValue());
                stmt = con.prepareStatement("SELECT id, user FROM login2user LEFT JOIN user_setting_admin ON user_setting_admin.user = login2user.id AND user_setting_admin.cid = login2user.cid WHERE login2user.cid = ? AND uid = ?");
                stmt.setInt(1, ctxid.intValue());
                stmt.setString(2, username);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw new StorageException("No such user " + username + " in context " + ctxid);
                }
                user.setId(I(rs.getInt(1)));
                return null == rs.getString(2) ? false : true;
            }
            con = cache.getConnectionForContext(ctxid.intValue());
            stmt = con.prepareStatement("SELECT user FROM user_setting_admin WHERE cid = ? AND user = ?");
            stmt.setInt(1, ctxid.intValue());
            stmt.setInt(2, userid.intValue());
            rs = stmt.executeQuery();
            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closePreparedStatement(stmt);
            closeRecordSet(rs);
            if (con != null) {
                try {
                    cache.pushConnectionForContextAfterReading(ctxid.intValue(), con);
                } catch (PoolException e) {
                    log.error("Error pushing oxdb read connection to pool!", e);
                }
            }
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#isContextEnabled(int)
     */
    @Override
    public boolean isContextEnabled(final Context ctx) throws StorageException {
        boolean retBool = false;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            prep_check = con.prepareStatement("SELECT enabled FROM context WHERE cid = ?;");
            prep_check.setInt(1, ctx.getId().intValue());
            rs = prep_check.executeQuery();
            if (!rs.next()) {
                throw new SQLException("UNABLE TO QUERY CONTEXT STATUS");
            }
            retBool = rs.getBoolean("enabled");
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            try {
                cache.pushReadConnectionForConfigDB(con);
            } catch (PoolException e) {
                log.error("Error pushing configdb read connection to pool!", e);
            }
        }
        return retBool;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#isMasterDatabase(int)
     */
    @Override
    public boolean isMasterDatabase(final int database_id) throws StorageException {
        return selectwithint(-1, "SELECT cluster_id FROM db_cluster WHERE write_db_pool_id = ?", database_id);
    }

    @Override
    public boolean isUserSettingMailBitSet(final Context ctx, final User user, final int bit, final Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT bits FROM user_setting_mail WHERE cid = ? AND user = ?");
            stmt.setInt(1, ctx.getId().intValue());
            stmt.setInt(2, user.getId().intValue());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Unable to get features from bitfield for User: " + user.getId() + ", Context: " + ctx.getId());
            }
            final int bits = rs.getInt("bits");
            return (bits & bit) == bit;
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(stmt);
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#poolInUse(long)
     */
    @Override
    public boolean poolInUse(final int pool_id) throws StorageException {
        return selectwithint(-1, "SELECT cid FROM context_server2db_pool WHERE write_db_pool_id = ? OR read_db_pool_id = ?", pool_id, pool_id);
    }

    @Override
    public boolean schemaInUse(int pool_id, String schemaName) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            stmt = con.prepareStatement("SELECT 1 FROM context_server2db_pool WHERE (write_db_pool_id = ? OR read_db_pool_id = ?) AND db_schema=?");
            stmt.setInt(1, pool_id);
            stmt.setInt(2, pool_id);
            stmt.setString(3, schemaName);
            rs = stmt.executeQuery();

            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    @Override
    public void primaryMailExists(Context ctx, String mail) throws StorageException, InvalidDataException {
        int context_id = ctx.getId().intValue();
        Connection con = null;
        try {
            con = cache.getConnectionForContext(context_id);
            primaryMailExists(con, ctx, mail);
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(context_id, con);
                } catch (PoolException e) {
                    log.error("Error pushing ox db write connection to pool!", e);
                }
            }
        }
    }

    private void primaryMailExists(Connection con, Context ctx, String mail) throws StorageException, InvalidDataException {
        int contextId = ctx.getId().intValue();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT mail, id FROM user WHERE cid=? AND mail=?");
            stmt.setInt(1, contextId);
            stmt.setString(2, mail);
            result = stmt.executeQuery();
            if (result.next()) {
                int userId = result.getInt(2);
                throw new InvalidDataException("Primary mail address \"" + mail + "\" already exists in context " + contextId + " (Already assigned to user " + userId + ").");
            }
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(result, stmt);
        }
    }

    @Override
    public boolean checkAndUpdateSchemaIfRequired(final int contextId) throws StorageException {
        UpdateStatusProvider provider = new UpdateStatusProvider() {

            @Override
            public UpdateStatus generateUpdateStatus() throws OXException {
                Updater updater = Updater.getInstance();
                return updater.getStatus(contextId);
            }
        };
        return condCheckAndUpdateSchemaIfRequired(provider, true, contextId);
    }

    @Override
    public boolean schemaBeingLockedOrNeedsUpdate(final int writePoolId, final String schema) throws StorageException {
        UpdateStatusProvider provider = new UpdateStatusProvider() {

            @Override
            public UpdateStatus generateUpdateStatus() throws OXException {
                Updater updater = Updater.getInstance();
                return updater.getStatus(schema, writePoolId);
            }
        };
        return condCheckAndUpdateSchemaIfRequired(provider, false, 0);
    }

    private static interface UpdateStatusProvider {

        UpdateStatus generateUpdateStatus() throws OXException;
    }

    private boolean condCheckAndUpdateSchemaIfRequired(UpdateStatusProvider statusProvider, boolean doUpdate, int contextId) throws StorageException {
        try {
            UpdateStatus status = statusProvider.generateUpdateStatus();
            if (status.blockingUpdatesRunning()) {
                log.info("Another database update process is already running");
                return true;
            }
            // we only reach this point, if no other thread is already locking us
            if (!status.needsBlockingUpdates()) {
                return false;
            }
            // we only reach this point, if we need an update
            if (doUpdate) {
                if (contextId <= 0) {
                    final StorageException e = new StorageException("A valid context identifier is required in case schema update should be done");
                    log.error("", e);
                    throw e;
                }
                Updater.getInstance().startUpdate(contextId);
            }
            // either with or without starting an update task, when we reach this point, we
            // must return true
            return true;
        } catch (OXException e) {
            if (e.getCode() == 102) {
                // NOTE: this situation should not happen!
                // it can only happen, when a schema has not been initialized correctly!
                log.debug("FATAL: this error must not happen", e);
            }
            log.error("Error while checking/updating schema", e);
            throw StorageException.wrapForRMI(e);
        }
    }

    @Override
    public List<Database> unblockDatabaseSchema(Database db) throws StorageException {
        try {
            int poolId = db.getId().intValue();
            if (Strings.isNotEmpty(db.getScheme())) {
                // Only check specified schema
                Database ldb = loadDatabaseById(poolId);
                Updater updater = Updater.getInstance();
                try {
                    updater.unblock(db.getScheme(), poolId, getAnyContextFromSchema(poolId, db.getScheme()));
                    ldb.setScheme(db.getScheme());
                    return Collections.singletonList(ldb);
                } catch (OXException e) {
                    // UPD-0005
                    if (!e.equalsCode(5, "UPD")) {
                        throw e;
                    }
                    return Collections.emptyList();
                }
            }

            // Query all schemas from denoted database
            List<Database> databases;
            {
                Connection con = null;
                try {
                    con = cache.getReadConnectionForConfigDB();
                    databases = PoolAndSchema.listDatabaseSchemas(poolId, con);
                } catch (PoolException e) {
                    throw new StorageException(e.getMessage(), e);
                } finally {
                    if (null != con) {
                        try {
                            cache.pushReadConnectionForConfigDB(con);
                        } catch (PoolException e) {
                            log.error("Error pushing connection to pool!", e);
                        }
                    }
                }
            }

            // Determine outdated threshold
            List<Database> outdatedUpdating = null;

            // Unblock outdated schemas
            Updater updater = Updater.getInstance();
            for (Database database : databases) {
                UpdateStatus status = updater.getStatus(database.getScheme(), poolId);
                if (status.blockingUpdatesRunning()) {
                    // Currently updating
                    if (status.blockingUpdatesTimedOut()) {
                        int contextId = getAnyContextFromSchema(poolId, database.getScheme());
                        try {
                            updater.unblock(database.getScheme(), poolId, contextId);
                            if (outdatedUpdating == null) {
                                outdatedUpdating = new LinkedList<>();
                            }
                            outdatedUpdating.add(database);
                        } catch (OXException e) {
                            if (!e.equalsCode(5, "UPD")) {
                                throw e;
                            }
                            // Ignore
                        }
                    }
                }
            }
            return outdatedUpdating == null ? Collections.emptyList() : outdatedUpdating;
        } catch (OXException e) {
            if (e.getCode() == 102) {
                // NOTE: this situation should not happen!
                // it can only happen, when a schema has not been initialized correctly!
                log.debug("FATAL: this error must not happen", e);
            }
            log.error("Error in checking/updating schema", e);
            throw StorageException.wrapForRMI(e);
        }
    }

    private int getAnyContextFromSchema(int writePoolId, String schema) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            stmt = con.prepareStatement("SELECT cid FROM context_server2db_pool WHERE server_id=? AND write_db_pool_id=? AND db_schema=? LIMIT 1");
            stmt.setInt(1, cache.getServerId());
            stmt.setInt(2, writePoolId);
            stmt.setString(3, schema);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (PoolException e) {
            throw new StorageException(e.getMessage(), e);
        } catch (SQLException e) {
            throw new StorageException(e.toString(), e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(stmt);
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    @Override
    public List<List<Database>> listSchemasBeingLockedOrNeedsUpdate() throws StorageException {
        List<Database> databases;
        {
            Connection con = null;
            try {
                con = cache.getReadConnectionForConfigDB();
                databases = PoolAndSchema.listAllSchemas(con);
            } catch (PoolException e) {
                throw new StorageException(e.getMessage(), e);
            } finally {
                if (null != con) {
                    try {
                        cache.pushReadConnectionForConfigDB(con);
                    } catch (PoolException e) {
                        log.error("Error pushing connection to pool!", e);
                    }
                }
            }
        }

        List<Database> needingUpdate = null;
        List<Database> currentlyUpdating = null;
        List<Database> outdatedUpdating = null;

        try {
            Updater updater = Updater.getInstance();
            for (Database database : databases) {
                UpdateStatus status = updater.getStatus(database.getScheme(), database.getId().intValue());
                if (status.blockingUpdatesRunning()) {
                    // Currently updating
                    if (status.blockingUpdatesTimedOut()) {
                        if (outdatedUpdating == null) {
                            outdatedUpdating = new LinkedList<>();
                        }
                        outdatedUpdating.add(database);
                    } else {
                        if (currentlyUpdating == null) {
                            currentlyUpdating = new LinkedList<>();
                        }
                        currentlyUpdating.add(database);
                    }
                } else if (status.needsBlockingUpdates()) {
                    // Needs update
                    if (needingUpdate == null) {
                        needingUpdate = new LinkedList<>();
                    }
                    needingUpdate.add(database);
                }
            }
        } catch (OXException e) {
            if (e.getCode() == 102) {
                // NOTE: this situation should not happen!
                // it can only happen, when a schema has not been initialized correctly!
                log.debug("FATAL: this error must not happen", e);
            }
            log.error("Error in checking/updating schema", e);
            throw StorageException.wrapForRMI(e);
        }

        List<List<Database>> retval = new ArrayList<>(3);
        retval.add(needingUpdate == null ? Collections.emptyList() : needingUpdate);
        retval.add(currentlyUpdating == null ? Collections.emptyList() : currentlyUpdating);
        retval.add(outdatedUpdating == null ? Collections.emptyList() : outdatedUpdating);
        return retval;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#serverInUse(long)
     */
    @Override
    public boolean serverInUse(final int server_id) throws StorageException {
        return selectwithint(-1, "SELECT cid FROM context_server2db_pool WHERE server_id = ?", server_id);
    }

    @Override
    public void setUserSettingMailBit(final Context ctx, final User user, final int bit, final Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT bits FROM user_setting_mail WHERE cid = ? AND user = ?");
            stmt.setInt(1, ctx.getId().intValue());
            stmt.setInt(2, user.getId().intValue());
            rs = stmt.executeQuery();
            if (rs.next()) {
                int bits = rs.getInt("bits");
                rs.close();
                stmt.close();
                bits |= bit;
                stmt = con.prepareStatement("UPDATE user_setting_mail SET bits = ? WHERE cid = ? AND user = ?");
                stmt.setInt(1, bits);
                stmt.setInt(2, ctx.getId().intValue());
                stmt.setInt(3, user.getId().intValue());
                stmt.executeUpdate();
            } else {
                throw new SQLException("Unable to set features from bitfield for User: " + user.getId() + ", Context: " + ctx.getId());
            }
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(stmt);
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#storeInUse(long)
     */
    @Override
    public boolean storeInUse(final int store_id) throws StorageException {
        boolean retval = selectwithint(-1, "SELECT cid FROM context WHERE filestore_id = ?", store_id);
        if (!retval) {
            retval = hasUsers(store_id);
        }
        return retval;
    }

    private boolean hasUsers(int id) throws StorageException {
        return Filestore2UserUtil.getUserCountFor(id, cache) > 0;
    }

    @Override
    public void unsetUserSettingMailBit(final Context ctx, final User user, final int bit, final Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT bits FROM user_setting_mail WHERE cid = ? AND user = ?");
            stmt.setInt(1, ctx.getId().intValue());
            stmt.setInt(2, user.getId().intValue());
            rs = stmt.executeQuery();
            if (rs.next()) {
                int bits = rs.getInt("bits");
                rs.close();
                stmt.close();
                bits &= ~bit;
                stmt = con.prepareStatement("UPDATE user_setting_mail SET bits = ? WHERE cid = ? AND user = ?");
                stmt.setInt(1, bits);
                stmt.setInt(2, ctx.getId().intValue());
                stmt.setInt(3, user.getId().intValue());
                stmt.executeUpdate();
            } else {
                throw new SQLException("Unable to set features from bitfield for User: " + user.getId() + ", Context: " + ctx.getId());
            }
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(stmt);
        }
    }

    private void closePreparedStatement(final PreparedStatement ps) {
        try {
            if (null != ps) {
                ps.close();
            }
        } catch (SQLException e) {
            log.error("Error closing prepared statement!", e);
        }
    }

    private void closeRecordSet(final ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Error closing resultset", e);
            }
        }
    }

    private static int getByNameForConfigDB(String name, String objectName, String query) throws StorageException, NoSuchObjectException {
        final Connection con;
        try {
            con = cache.getReadConnectionForConfigDB();
        } catch (PoolException e) {
            log.error("", e);
            throw new StorageException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new NoSuchObjectException("No such " + objectName + " " + name);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            try {
                cache.pushReadConnectionForConfigDB(con);
            } catch (PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }
        }
    }

    @SuppressWarnings("unused")
    private Group[] getDomainUsedbyGroup(final Context ctx, final String domain, final Connection oxcon) throws SQLException {
        // groups are currently not used with mail addresses in the core
        return null;
    }

    private Resource[] getDomainUsedbyResource(final Context ctx, final String domain, final Connection oxcon) throws SQLException {
        final ArrayList<Resource> data = new ArrayList<>();
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            // fetch
            prep_check = oxcon.prepareStatement("SELECT id FROM resource where cid = ? and mail like ?");
            prep_check.setInt(1, ctx.getId().intValue());
            prep_check.setString(2, "%@" + domain);
            rs = prep_check.executeQuery();
            while (rs.next()) {
                data.add(new Resource(I(rs.getInt("id"))));
            }
            rs.close();
            prep_check.close();
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);
        }

        return data.size() == 0 ? null : data.toArray(new Resource[data.size()]);
    }

    private User[] getDomainUsedbyUser(final Context ctx, final String domain, final Connection oxcon) throws SQLException {
        final ArrayList<User> data = new ArrayList<>();
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            final HashSet<Integer> usr_ids = new HashSet<>();
            // fetch from alias table
            prep_check = oxcon.prepareStatement("SELECT user FROM user_alias WHERE cid=? AND alias LIKE ?");
            prep_check.setInt(1, ctx.getId().intValue());
            prep_check.setString(2, "%@" + domain);
            rs = prep_check.executeQuery();
            while (rs.next()) {
                usr_ids.add(I(rs.getInt("id")));
            }
            rs.close();
            prep_check.close();
            // fetch from user table
            prep_check = oxcon.prepareStatement("SELECT id FROM user WHERE cid = ? AND mail like ?");
            prep_check.setInt(1, ctx.getId().intValue());
            prep_check.setString(2, "%@" + domain);
            rs = prep_check.executeQuery();
            while (rs.next()) {
                usr_ids.add(I(rs.getInt("id")));
            }
            rs.close();
            prep_check.close();

            // if we had time we could resolv the complete user object in db but at the moment we only need the ids of the user
            final Iterator<Integer> ids_itr = usr_ids.iterator();
            while (ids_itr.hasNext()) {
                final Integer id = ids_itr.next();
                data.add(new User(id.intValue()));
            }
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);
        }

        return data.size() == 0 ? null : data.toArray(new User[data.size()]);
    }

    /**
     * This function is used for all sql queries which insert an integer
     *
     * @param sql_select_string
     * @param context_id
     *            if -1 we use configbd connection for query, else ox db
     *            connection with given context id
     * @param ins_number
     * @return
     * @throws StorageException
     */
    private boolean selectwithint(final int context_id, final String sql_select_string, final int... ins_numbers) throws StorageException {
        boolean retBool = false;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            if (context_id != -1) {
                con = cache.getConnectionForContext(context_id);
            } else {
                con = cache.getReadConnectionForConfigDB();
            }
            prep_check = con.prepareStatement(sql_select_string);
            int sql_counter = 1;
            for (final int element : ins_numbers) {
                prep_check.setInt(sql_counter, element);
                sql_counter++;
            }

            rs = prep_check.executeQuery();
            if (rs.next()) {
                retBool = true;
            }
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    if (context_id != -1) {
                        cache.pushConnectionForContextAfterReading(context_id, con);
                    } else {
                        cache.pushReadConnectionForConfigDB(con);
                    }
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
        return retBool;
    }

    /**
     * This function is used for all sql queries which check for some data to exist.
     *
     * @param con readable database connection to use.
     * @param sql SQL statement to execute with inserted sqlInts.
     * @param sqlInts numbers to insert into the sql query.
     * @return <code>true</code> if the database contains an entry, <code>false</code> otherwise.
     * @throws StorageException if some problem occurs while executing the statement.
     */
    private boolean selectwithint(final Connection con, final String sql, final int... sqlInts) throws StorageException {
        boolean retval = false;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql);
            int pos = 1;
            for (final int sqlInt : sqlInts) {
                stmt.setInt(pos++, sqlInt);
            }
            result = stmt.executeQuery();
            if (result.next()) {
                retval = true;
            }
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString(), e);
        } finally {
            closeRecordSet(result);
            closePreparedStatement(stmt);
        }
        return retval;
    }

    /**
     * This function is used for all sql queries which insert a string
     *
     * @param sql_select_string
     * @param ins_Strings
     * @return
     * @throws StorageException
     */
    private boolean selectwithstring(final int context_id, final String sql_select_string, final String... ins_strings) throws StorageException {
        boolean retBool = false;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            if (context_id != -1) {
                con = cache.getConnectionForContext(context_id);
            } else {
                con = cache.getReadConnectionForConfigDB();
            }
            prep_check = con.prepareStatement(sql_select_string);
            int sql_counter = 1;
            for (final String element : ins_strings) {
                prep_check.setString(sql_counter, element);
                sql_counter++;
            }

            rs = prep_check.executeQuery();
            if (rs.next()) {
                retBool = true;
            }
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    if (context_id != -1) {
                        cache.pushConnectionForContextAfterReading(context_id, con);
                    } else {
                        cache.pushReadConnectionForConfigDB(con);
                    }
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
        return retBool;
    }

    @Override
    public boolean existsContextName(final Context ctx) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;

        try {
            con = cache.getReadConnectionForConfigDB();
            prep_check = con.prepareStatement("SELECT cid FROM context WHERE name = ? and cid !=?");
            prep_check.setString(1, ctx.getName());
            prep_check.setInt(2, ctx.getId().intValue());

            rs = prep_check.executeQuery();

            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    @Override
    public boolean existsContextNameInServer(Context ctx) throws StorageException {
        Connection con = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            return existsContextNameInServer(ctx, con);
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    /**
     * Checks context existence in server and if there is no other context with the same name
     * <p>
     * Should be used in change method!
     *
     * @param ctx The context
     * @param con The connection to use or <code>null</code>
     * @return <code>true</code> if denoted context currently holds a different name, otherwise <code>false</code> if name is equal
     * @throws StorageException If existence check fails
     */
    public boolean existsContextNameInServer(Context ctx, Connection con) throws StorageException {
        if (null == con) {
            return existsContextNameInServer(ctx);
        }

        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            int serverId = cache.getServerId();
            prep_check = con.prepareStatement("SELECT c.cid FROM context AS c JOIN context_server2db_pool AS c2s ON c.cid=c2s.cid WHERE c2s.server_id=? AND c.name = ? AND c.cid !=?");
            prep_check.setInt(1, serverId);
            prep_check.setString(2, ctx.getName());
            prep_check.setInt(3, ctx.getId().intValue());
            rs = prep_check.executeQuery();
            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);
        }
    }

    @Override
    public boolean existsContextName(final String contextName) throws StorageException {
        final Context ctx = new Context(-1, contextName);
        return existsContextName(ctx);
    }

    @Override
    public boolean existsContextNameInServer(String contextName) throws StorageException {
        Context ctx = new Context(-1, contextName);
        return existsContextNameInServer(ctx);
    }

    @Override
    public void checkContextIdentifier(Context ctx) throws InvalidDataException, StorageException, ContextExistsException {
        if (null == ctx || null == ctx.getId()) {
            throw new InvalidDataException("There must be a context identifier!");
        }
        if (i(ctx.getId()) <= 0) {
            throw new InvalidDataException("The context identifier " + ctx.getId() + " is not allowed!");
        }

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            int contextId = ctx.getId().intValue();
            stmt = con.prepareStatement("SELECT 1 FROM context WHERE cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                throw new ContextExistsException("Context " + ctx.getId().intValue() + " already exists!");
            }
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(stmt);

            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    @Override
    public boolean checkContextName(Context ctx) throws NoSuchContextException, InvalidDataException, StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            int contextId = ctx.getId().intValue();
            String name = ctx.getName();
            prep_check = con.prepareStatement("SELECT cid, name FROM context WHERE name=? OR cid=?");
            prep_check.setString(1, name);
            prep_check.setInt(2, contextId);
            rs = prep_check.executeQuery();

            String foundName = null;
            while (rs.next()) {
                int cid = rs.getInt(1);
                if (cid != contextId) {
                    throw new InvalidDataException("Context " + name + " already exists!");
                }
                foundName = rs.getString(2);
            }

            if (null == foundName) {
                throw new NoSuchContextException();
            }
            return !name.equals(foundName);
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    @Override
    public boolean existsDatabaseName(final Database db) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;

        try {
            con = cache.getReadConnectionForConfigDB();

            prep_check = con.prepareStatement("SELECT db_pool_id FROM db_pool WHERE name = ? AND db_pool_id !=?");
            prep_check.setString(1, db.getName());
            prep_check.setInt(2, db.getId().intValue());

            rs = prep_check.executeQuery();

            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    @Override
    public boolean existsDatabaseName(final String db_name) throws StorageException {
        final Database db = new Database(-1);
        db.setName(db_name);
        return existsDatabaseName(db);
    }

    @Override
    public boolean existsGroupName(final Context ctx, final Group grp) throws StorageException {
        int contextId = ctx.getId().intValue();

        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(contextId);
            prep_check = con.prepareStatement("SELECT id FROM groups WHERE cid = ? AND identifier = ? AND id !=?");
            prep_check.setInt(1, contextId);
            prep_check.setString(2, grp.getName());
            prep_check.setInt(3, grp.getId().intValue());
            rs = prep_check.executeQuery();

            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    @Override
    public boolean existsGroupName(final Context ctx, final String identifier) throws StorageException {
        final Group grp = new Group(I(-1));
        grp.setName(identifier);
        return existsGroupName(ctx, grp);
    }

    @Override
    public boolean existsResourceName(final Context ctx, final Resource res) throws StorageException {
        int contextId = ctx.getId().intValue();

        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForContext(contextId);
            prep_check = con.prepareStatement("SELECT id FROM resource WHERE cid = ? AND identifier = ? AND id != ?");
            prep_check.setInt(1, contextId);
            prep_check.setString(2, res.getName());
            prep_check.setInt(3, res.getId().intValue());

            rs = prep_check.executeQuery();

            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    @Override
    public boolean existsResourceName(final Context ctx, final String identifier) throws StorageException {
        final Resource res = new Resource(I(-1));
        res.setName(identifier);
        return existsResourceName(ctx, res);
    }

    @Override
    public boolean existsServerName(final Server srv) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;

        try {
            con = cache.getReadConnectionForConfigDB();
            prep_check = con.prepareStatement("SELECT server_id FROM server WHERE name = ? AND server_id != ?");
            prep_check.setString(1, srv.getName());
            prep_check.setInt(2, srv.getId().intValue());
            rs = prep_check.executeQuery();

            return rs.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (RuntimeException e) {
            log.error("Runtime Error", e);
            throw StorageException.storageExceptionFor(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            try {
                cache.pushReadConnectionForConfigDB(con);
            } catch (PoolException e) {
                log.error("Error pushing connection to pool!", e);
            }
        }
    }

    @Override
    public boolean existsServerName(final String server_name) throws StorageException {
        final Server srv = new Server();
        srv.setId(I(-1));
        srv.setName(server_name);
        return existsServerName(srv);
    }

    @Override
    public boolean existsUserName(Context ctx, User user) throws StorageException {
        int contextId = ctx.getId().intValue();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        boolean foundOther = false;
        try {
            con = cache.getConnectionForContext(contextId);
            boolean autoLowerCase = cache.getProperties().getUserProp(AdminProperties.User.AUTO_LOWERCASE, false);
            String uname = autoLowerCase ? user.getName().toLowerCase() : user.getName();
            stmt = con.prepareStatement("SELECT uid FROM login2user WHERE cid=? AND uid=? AND id!=?");
            stmt.setInt(1, contextId);
            stmt.setString(2, uname);
            stmt.setInt(3, user.getId().intValue());
            result = stmt.executeQuery();
            while (!foundOther && result.next()) {
                foundOther = uname.equals(result.getString(1));
            }
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(result, stmt);
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing context connection to pool.", e);
                }
            }
        }
        return foundOther;
    }

    @Override
    public boolean existsUserName(final Context ctx, final String username) throws StorageException {
        final User tmp = new User(-1);
        tmp.setName(username);
        return existsUserName(ctx, tmp);
    }

    @Override
    public boolean existsDisplayName(Context ctx, String displayName) throws StorageException {
        if (Strings.isEmpty(displayName)) {
            return false;
        }

        int contextId = ctx.getId().intValue();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = cache.getConnectionForContext(contextId);
            stmt = con.prepareStatement("SELECT 1 FROM prg_contacts WHERE cid=? AND field01=? AND userid IS NOT NULL AND userid > 0");
            stmt.setInt(1, contextId);
            stmt.setString(2, displayName);
            result = stmt.executeQuery();
            return result.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(result, stmt);
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing context connection to pool.", e);
                }
            }
        }
    }

    @Override
    public void checkCreateUserData(final Context ctx, final User usr) throws InvalidDataException, EnforceableDataObjectException, StorageException {
        checkAndSetLanguage(ctx, usr);
        checkAndSetTimezone(usr, null);
        GenericChecks.checkCreateValidPasswordMech(usr);
        if (usr.getPassword() == null || usr.getPassword().trim().length() == 0) {
            throw new InvalidDataException("Empty password is not allowed.");
        }
        if (!usr.mandatoryCreateMembersSet()) {
            throw new InvalidDataException("Mandatory fields not set: " + usr.getUnsetMembers());
        }
        if (!usr.isContextadmin()) {
            if (existsDisplayName(ctx, usr, 0)) {
                try {
                    ConfigViewFactory configViewFactory = AdminServiceRegistry.getInstance().getService(ConfigViewFactory.class, true);
                    ConfigView view = configViewFactory.getView(-1, ctx.getId().intValue());
                    if (null == view || view.opt("com.openexchange.user.enforceUniqueDisplayName", Boolean.class, Boolean.TRUE).booleanValue()) {
                        // Do enforce unique display names
                        throw new InvalidDataException("The displayname is already used");
                    }
                } catch (OXException e) {
                    log.debug("Unable to get \"com.openexchange.user.enforceUniqueDisplayName\". Fallback to enforce display name uniqueness.", e);
                    throw new InvalidDataException("The displayname is already used", e);
                }

            }
        }
        if (prop.getUserProp(AdminProperties.User.CHECK_NOT_ALLOWED_CHARS, true)) {
            validateUserName(usr.getName());
        }
        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            usr.setName(usr.getName().toLowerCase());
        }

        // Checks below throw InvalidDataException
        checkValidEmailsInUserObject(usr);
        if (existsContext(ctx)) {
            checkValidEmailsInUserObjectRegex(usr, PropertyScope.propertyScopeForContext(ctx.getId().intValue()));
        } else {
            checkValidEmailsInUserObjectRegex(usr, PropertyScope.propertyScopeForServer());
        }

        // ### Do some mail attribute checks cause of bug 5444
        // check if primary email address is also set in Email1,
        if (!usr.getPrimaryEmail().equals(usr.getEmail1())) {
            throw new InvalidDataException("primarymail must have the same value as email1");
        }
        // if default sender address is != primary mail, add it to list of aliases
        if (usr.getDefaultSenderAddress() != null) {
            usr.addAlias(usr.getDefaultSenderAddress());
        } else {
            // if default sender address is not set, set it to primary mail address
            usr.setDefaultSenderAddress(usr.getPrimaryEmail());
        }
        // put primary mail in the aliases,
        usr.addAlias(usr.getPrimaryEmail());
        // Check mail attributes
        {
            HashSet<String> useraliases = usr.getAliases();
            String primaryEmail = usr.getPrimaryEmail();
            String email1 = usr.getEmail1();
            boolean foundPrimaryMail = useraliases.contains(primaryEmail);
            boolean foundEmail1 = useraliases.contains(email1);
            boolean foundDefaultSenderAddress = useraliases.contains(usr.getDefaultSenderAddress());
            if (!foundPrimaryMail || !foundEmail1 || !foundDefaultSenderAddress) {
                throw new InvalidDataException("primaryMail, Email1 and defaultSenderAddress must be present in set of aliases.");
            }
            // added "usrdata.getPrimaryEmail() != null" for this check, else we cannot update user data without mail data
            // which is not very good when just changing the display name for example
            if (primaryEmail != null && email1 == null) {
                throw new InvalidDataException("email1 not set but required!");
            }
        }

        if (usr.isDriveFolderModeSet() && OXFolderDefaultMode.fromString(usr.getDriveFolderMode()) == null) {
            String modes = "";
            for (OXFolderDefaultMode mode : OXFolderDefaultMode.values()) {
                modes = modes.concat("\n ").concat(mode.getText());
            }
            throw new InvalidDataException("An invalid folder mode was selected. Only the following modes are allowed: " + modes);
        }
    }

    @Override
    public void checkChangeUserData(Context ctx, User newuser, User dbuser, PropertyHandler prop) throws InvalidDataException, StorageException {
        if (newuser.getName() != null) {
            if (usernameIsChangeable()) {
                if (prop.getUserProp(AdminProperties.User.CHECK_NOT_ALLOWED_CHARS, true)) {
                    validateUserName(newuser.getName());
                }
                if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                    newuser.setName(newuser.getName().toLowerCase());
                }
            }
            // must be loaded additionally because the user loading method gets the new user name passed and therefore does not load the
            // current one.
            final String currentName = getUsernameByUserID(ctx, newuser.getId().intValue());
            if (!newuser.getName().equals(currentName)) {
                if (usernameIsChangeable()) {
                    if (existsUserName(ctx, newuser.getName())) {
                        throw new InvalidDataException("User " + newuser.getName() + " already exists in this context");
                    }
                } else {
                    throw new InvalidDataException("Changing username is disabled!");
                }
            }
        }

        {
            String lang = newuser.getLanguage();
            if (lang != null && lang.indexOf('_') < 0) {
                throw new InvalidDataException("Language must contain an underscore, e.g. en_US.");
            }
        }

        checkAndSetTimezone(newuser, dbuser);

        String newDefaultSenderAddress = newuser.getDefaultSenderAddress();
        String newPrimaryEmail = newuser.getPrimaryEmail();
        String newEmail1 = newuser.getEmail1();
        boolean mailCheckNeeded = (null != newDefaultSenderAddress) || (null != newPrimaryEmail) || (null != newEmail1) || (null != newuser.getAliases());

        if (prop.getUserProp(AdminProperties.User.PRIMARY_MAIL_UNCHANGEABLE, true)) {
            if (newPrimaryEmail != null && !newPrimaryEmail.equalsIgnoreCase(dbuser.getPrimaryEmail())) {
                throw new InvalidDataException("primary mail must not be changed");
            }
        }

        GenericChecks.checkChangeValidPasswordMech(newuser);

        // if no password mech supplied, use the old one as set in db
        if (newuser.getPasswordMech() == null) {
            newuser.setPasswordMech(dbuser.getPasswordMech());
        }

        if (mailCheckNeeded) {
            // Check if E-Mail addresses should be checked for context administrator, too (default is false)
            boolean enableAdminMailChecks = false;
            {
                ConfigurationService configService = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (null != configService) {
                    enableAdminMailChecks = configService.getBoolProperty(AdminProperties.User.ENABLE_ADMIN_MAIL_CHECKS, enableAdminMailChecks);
                }
            }

            // Validate E-Mail addresses for either all users (com.openexchange.admin.enableAdminMailChecks=true) or only non-admin users
            if (enableAdminMailChecks || !isContextAdmin(ctx, newuser.getId().intValue())) {
                // checks below throw InvalidDataException
                checkValidEmailsInUserObject(newuser);
                checkValidEmailsInUserObjectRegex(newuser, PropertyScope.propertyScopeForUser(newuser.getId().intValue(), ctx.getId().intValue()));
                Set<String> useraliases = newuser.getAliases();
                if (useraliases == null) {
                    useraliases = dbuser.getAliases();
                }
                if (null != useraliases) {
                    Set<String> tmp = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                    for (String email : useraliases) {
                        tmp.add(IDNA.toIDN(email));
                    }
                    useraliases = tmp;
                }

                if (newPrimaryEmail != null && newEmail1 != null && !newPrimaryEmail.equalsIgnoreCase(newEmail1)) {
                    // primary mail value must be same with email1
                    throw new InvalidDataException("email1 not equal with primarymail!");
                }

                if (useraliases == null) {
                    useraliases = Collections.emptySet();
                } else {
                    Set<String> useraliasesAddresses = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                    for (String sAddr : useraliases) {
                        try {
                            QuotedInternetAddress addr = new QuotedInternetAddress(sAddr, false);
                            useraliasesAddresses.add(addr.getIDNAddress());
                            useraliasesAddresses.add(QuotedInternetAddress.toACE(addr.getAddress()));
                        } catch (Exception e) {
                            // Failed to parse as E-Mail address
                            useraliasesAddresses.add(IDNA.toIDN(extractRealMailAddressFrom(sAddr)));
                        }
                    }
                    useraliases = useraliasesAddresses;
                }

                String check_primary_mail;
                String check_email1;
                String check_default_sender_address;
                if (newPrimaryEmail != null) {
                    check_primary_mail = IDNA.toIDN(newPrimaryEmail);
                    if (!newPrimaryEmail.equalsIgnoreCase(dbuser.getPrimaryEmail())) {
                        primaryMailExists(ctx, newPrimaryEmail);
                    }
                } else {
                    final String email = dbuser.getPrimaryEmail();
                    check_primary_mail = email == null ? email : IDNA.toIDN(email);
                }
                check_primary_mail = parseRealMailAddressFrom(check_primary_mail, true);

                if (newEmail1 != null) {
                    check_email1 = IDNA.toIDN(newEmail1);
                } else {
                    final String s = dbuser.getEmail1();
                    check_email1 = s == null ? s : IDNA.toIDN(s);
                }
                check_email1 = parseRealMailAddressFrom(check_email1, true);

                if (newDefaultSenderAddress != null) {
                    check_default_sender_address = IDNA.toIDN(newDefaultSenderAddress);
                } else {
                    final String s = dbuser.getDefaultSenderAddress();
                    check_default_sender_address = s == null ? s : IDNA.toIDN(s);
                }
                check_default_sender_address = parseRealMailAddressFrom(check_default_sender_address, true);

                final boolean found_primary_mail = useraliases.contains(check_primary_mail);
                final boolean found_email1 = useraliases.contains(check_email1);
                final boolean found_default_sender_address = useraliases.contains(check_default_sender_address);

                if (!found_primary_mail || !found_email1 || !found_default_sender_address) {
                    throw new InvalidDataException("primaryMail, Email1 and defaultSenderAddress must be present in set of aliases.");
                }
                // added "usrdata.getPrimaryEmail() != null" for this check, else we cannot update user data without mail data
                // which is not very good when just changing the displayname for example
                if (newPrimaryEmail != null && newEmail1 == null) {
                    throw new InvalidDataException("email1 not sent but required!");

                }
            }
        }

        // Other mail checks?
    }

    /**
     * Checks whether the E-Mail addresses specified in the user object are valid according to the configured regular expression.
     *
     * @param user The user to check
     * @param propertyScope The scope to apply
     * @throws InvalidDataException If one of the user's E-Mail address is invalid
     */
    private void checkValidEmailsInUserObjectRegex(User user, PropertyScope propertyScope) throws InvalidDataException {
        String propertyName = AdminProperties.User.ADDITIONAL_EMAIL_CHECK_REGEX;
        String regex = AdminProperties.optScopedProperty(propertyName, propertyScope, String.class);
        if (Strings.isEmpty(regex)) {
            return;
        }
        try {
            checkValidEmailsInUserObject(user, Pattern.compile(regex));
        } catch (PatternSyntaxException e) {
            log.warn("Unable to compile the value of the '{}' property to a regular expression. User E-Mail addresses cannot be further checked for validity.", propertyName, e);
        }
    }

    private boolean usernameIsChangeable() {
        return cache.getProperties().getUserProp(AdminProperties.User.USERNAME_CHANGEABLE, false);
    }

    private static String parseRealMailAddressFrom(String sAddress, boolean idn) {
        if (sAddress == null) {
            return null;
        }

        try {
            QuotedInternetAddress addr = new QuotedInternetAddress(sAddress, false);
            return idn ? addr.getIDNAddress() : QuotedInternetAddress.toACE(addr.getAddress());
        } catch (Exception e) {
            // Failed to parse as E-Mail address
            String s = extractRealMailAddressFrom(sAddress);
            return idn ? IDNA.toIDN(s) : s;
        }
    }

    private static String extractRealMailAddressFrom(String sAddress) {
        if (sAddress == null) {
            return null;
        }

        int indexOf = sAddress.indexOf('<');
        if (indexOf < 0) {
            return sAddress;
        }

        return sAddress.substring(indexOf + 1, sAddress.indexOf('>'));
    }

    /**
     * Checks and applies a set timezone identifier in the passed user reference, ensuring that only supported values are taken over.
     *
     * @param user The user to check the timezone property for, as passed by the client
     * @param originalUser An optional reference to the original user in case of update operation, or <code>null</code> if not applicable
     * @throws InvalidDataException - If the set timezone identifier is invalid
     */
    private void checkAndSetTimezone(User user, User originalUser) throws InvalidDataException {
        String timezone = user.getTimezone();
        if (null != timezone && (null == originalUser || false == timezone.equals(originalUser.getTimezone()))) {
            TimeZone timeZone = TimeZone.getTimeZone(timezone);
            if (null == timeZone || false == timeZone.getID().equals(timezone)) {
                throw new InvalidDataException("The given timezone is invalid");
            }
            user.setTimezone(timeZone.getID());
        }
    }

    private void checkAndSetLanguage(final Context ctx, final User user) throws InvalidDataException {
        String lang = user.getLanguage();
        if (lang == null) {
            final ConfigViewFactory viewFactory = AdminServiceRegistry.getInstance().getService(ConfigViewFactory.class);
            if (viewFactory != null) {
                try {
                    int ctxId = ctx.getId().intValue();
                    if (user.isContextadmin() && user.getId() == null) {
                        ctxId = -1;
                    }
                    final ConfigView view = viewFactory.getView(-1, ctxId);
                    final String userAttribute = ctx.getUserAttribute("config", "com.openexchange.admin.user.defaultLanguage");
                    if (userAttribute != null) {
                        final ComposedConfigProperty<String> property = view.property("com.openexchange.admin.user.defaultLanguage", String.class).precedence("server, contextSets, reseller");
                        if (property.isDefined()) {
                            lang = property.get();
                        } else {
                            lang = userAttribute;
                        }
                    } else {
                        final ComposedConfigProperty<String> property = view.property("com.openexchange.admin.user.defaultLanguage", String.class);
                        if (property.isDefined()) {
                            lang = property.get();
                        }
                    }

                    if (lang != null) {
                        if (lang.indexOf('_') < 0) {
                            throw new InvalidDataException("language must contain an underscore, e.g. en_US");
                        }
                        user.setLanguage(lang);
                        return;
                    }
                } catch (OXException e) {
                    log.error("", e);
                    throw new InvalidDataException(e.getMessage());
                }

            }
            user.setLanguage(FALLBACK_LANGUAGE_CREATE + '_' + FALLBACK_COUNTRY_CREATE);
        } else {
            /*
             * Check if locale matches pattern like "en_US"
             */
            int underscoreIndex = lang.indexOf('_');
            if (underscoreIndex < 0 || -1 != lang.indexOf('_', underscoreIndex + 1)) {
                throw new InvalidDataException("language must contain an underscore, e.g. en_US");
            }
            Locale locale = LocaleTools.getLocale(lang);
            user.setLanguage(locale.toString());
        }
    }

    @Override
    public void validateUserName(String userName) throws InvalidDataException {
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@
        String usrUidRegexp = prop.getUserProp("CHECK_USER_UID_REGEXP", "[$@%\\.+a-zA-Z0-9_-]");
        String illegal = userName.replaceAll(usrUidRegexp, "");
        if (illegal.length() > 0) {
            throw new InvalidDataException("Illegal chars: \"" + illegal + "\"");
        }
    }

    @Override
    public void checkValidEmailsInUserObject(User usr) throws InvalidDataException {
        GenericChecks.checkValidMailAddress(usr.getPrimaryEmail());
        GenericChecks.checkValidMailAddress(usr.getEmail1());
        GenericChecks.checkValidMailAddress(usr.getEmail2());
        GenericChecks.checkValidMailAddress(usr.getEmail3());
        GenericChecks.checkValidMailAddress(usr.getDefaultSenderAddress());
        HashSet<String> aliases = usr.getAliases();
        if (aliases == null) {
            return;
        }
        for (String addr : aliases) {
            GenericChecks.checkValidMailAddress(addr);
        }
    }

    @Override
    public void checkValidEmailsInUserObject(User user, Pattern pattern) throws InvalidDataException {
        GenericChecks.checkValidMailAddressRegex(user.getPrimaryEmail(), pattern);
        GenericChecks.checkValidMailAddressRegex(user.getEmail1(), pattern);
        GenericChecks.checkValidMailAddressRegex(user.getEmail2(), pattern);
        GenericChecks.checkValidMailAddressRegex(user.getEmail3(), pattern);
        GenericChecks.checkValidMailAddressRegex(user.getDefaultSenderAddress(), pattern);
        HashSet<String> aliases = user.getAliases();
        if (aliases == null) {
            return;
        }
        for (String addr : aliases) {
            GenericChecks.checkValidMailAddressRegex(addr, pattern);
        }
    }

    @Override
    public void changeAccessCombination(int filter, int addAccess, int removeAccess) throws StorageException {
        // Collecting ONE Context id for each schema
        List<Integer> contextIdsForSchema;
        {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                con = cache.getReadConnectionForConfigDB();

                // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
                stmt = con.prepareStatement("SELECT MIN(cid) FROM context_server2db_pool GROUP BY db_schema");
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    return;
                }

                contextIdsForSchema = new LinkedList<>();
                do {
                    contextIdsForSchema.add(Integer.valueOf(rs.getInt(1)));
                } while (rs.next());
                Databases.closeSQLStuff(rs, stmt);
            } catch (PoolException e) {
                log.error("Pool Error", e);
                throw new StorageException(e);
            } catch (SQLException e) {
                log.error("SQL Error", e);
                throw new StorageException(e);
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                if (null != con) {
                    try {
                        cache.pushReadConnectionForConfigDB(con);
                    } catch (PoolException e) {
                        log.error("Error pushing COnfigDB connection to pool.", e);
                    }
                }
            }
        }

        // Execute once for each schema
        for (int cid : contextIdsForSchema) {
            changeAccessCombination(cid, filter, addAccess, removeAccess);
        }
    }

    private void changeAccessCombination(int cid, int filter, int addAccess, int removeAccess) throws StorageException {
        Connection con = null;
        Table table = new Table("user_configuration");
        Column column = new Column("permissions");

        try {
            con = cache.getConnectionForContextNoTimeout(cid);
            UPDATE update = new UPDATE(table).SET(column, new BitAND(new BitOR(column, PLACEHOLDER), new INVERT(PLACEHOLDER)));

            List<Object> values = new ArrayList<>();
            values.add(I(addAccess));
            values.add(I(removeAccess));

            if (filter != -1) {
                update.WHERE(new EQUALS(column, PLACEHOLDER));
                values.add(I(filter));
            }
            new StatementBuilder().executeStatement(con, update, values);

            // JCS
            final BundleContext context = AdminCache.getBundleContext();
            if (null != context) {
                final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                if (null != cacheService) {
                    try {
                        final Cache cache = cacheService.getCache("Capabilities");
                        cache.invalidateGroup(Integer.toString(cid));
                    } catch (OXException e) {
                        log.error("", e);
                    }
                }
            }
            // End of JCS
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            try {
                cache.pushConnectionForContextNoTimeout(cid, con);
            } catch (PoolException e) {
                log.error("Error pushing context connection to pool.", e);
            }
        }
    }

    @Override
    public Database loadDatabaseById(int id) throws StorageException {
        final Connection con;
        try {
            con = cache.getReadConnectionForConfigDB();
        } catch (PoolException e) {
            throw new StorageException(e.getMessage(), e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT url,driver,login,password,name,read_db_pool_id,max_units FROM db_pool JOIN db_cluster ON write_db_pool_id=db_pool_id WHERE db_pool_id=?");
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new StorageException("Database with identifer " + id + " does not exist.");
            }

            Database retval = new Database();
            int pos = 1;
            retval.setId(I(id));
            retval.setMaster(Boolean.TRUE);
            retval.setUrl(rs.getString(pos++));
            retval.setDriver(rs.getString(pos++));
            retval.setLogin(rs.getString(pos++));
            retval.setPassword(rs.getString(pos++));
            retval.setName(rs.getString(pos++));
            final int slaveId = rs.getInt(pos++);
            if (slaveId > 0) {
                retval.setRead_id(I(slaveId));
            }
            retval.setMaxUnits(I(rs.getInt(pos++)));
            return retval;
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            try {
                cache.pushReadConnectionForConfigDB(con);
            } catch (PoolException e) {
                log.error("Error pushing connection to pool!", e);
            }
        }
    }

    @Override
    public boolean isMasterFilestoreOwner(Context context, int userId) throws StorageException {
        int contextId = context.getId().intValue();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = cache.getConnectionForContext(contextId);
            stmt = con.prepareStatement("SELECT 1 FROM user WHERE id = ? AND cid = ? AND filestore_id > 0 AND filestore_owner = 0");
            stmt.setInt(1, userId);
            stmt.setInt(2, contextId);
            result = stmt.executeQuery();

            return result.next();
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(result, stmt);
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing context connection to pool.", e);
                }
            }
        }
    }

    @Override
    public Map<Integer, List<Integer>> fetchSlaveUsersOfMasterFilestore(Context context, int userId) throws StorageException {
        int contextId = context.getId().intValue();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = cache.getConnectionForContext(contextId);
            stmt = con.prepareStatement("SELECT id, cid FROM user WHERE cid = ? AND filestore_owner = ? AND filestore_id > 0");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            result = stmt.executeQuery();

            Map<Integer, List<Integer>> users = new HashMap<>();
            while (result.next()) {
                int id = result.getInt(1);
                int cid = result.getInt(2);
                List<Integer> userIds = users.get(I(cid));
                if (userIds == null) {
                    userIds = new ArrayList<>();
                    users.put(I(cid), userIds);
                }
                userIds.add(I(id));
            }

            return users;
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(result, stmt);
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(contextId, con);
                } catch (PoolException e) {
                    log.error("Error pushing context connection to pool.", e);
                }
            }
        }
    }

    @Override
    public boolean isLastContextInSchema(Context context) throws StorageException, InvalidDataException {
        int contextId = context.getId().intValue();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            // Fetch the schema name
            stmt = con.prepareStatement("SELECT db_schema FROM context_server2db_pool WHERE cid = ?");
            stmt.setInt(1, contextId);
            result = stmt.executeQuery();
            String schemaName;
            if (result.next()) {
                schemaName = result.getString(1);
            } else {
                throw new InvalidDataException("The specified context '" + contextId + "' does not exist in any known database schema.");
            }
            stmt.close();
            result.close();

            // Count the contexts that the schema contains
            stmt = con.prepareStatement("SELECT COUNT(cid) FROM context_server2db_pool WHERE db_schema = ?");
            stmt.setString(1, schemaName);
            result = stmt.executeQuery();
            if (result.next()) {
                int count = result.getInt(1);
                return count == 1;
            }
            throw new InvalidDataException("The specified schema '" + schemaName + "' does not exist.");
        } catch (PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(result, stmt);
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    log.error("Error pushing context connection to pool.", e);
                }
            }
        }
    }
}
