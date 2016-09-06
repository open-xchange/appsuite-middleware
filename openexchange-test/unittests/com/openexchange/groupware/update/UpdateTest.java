
package com.openexchange.groupware.update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.SortedSet;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import junit.framework.TestCase;

public abstract class UpdateTest extends TestCase {

    protected Schema schema = null;

    protected int existing_ctx_id = 0;

    protected int user_id = -1;

    protected Context ctx;

    protected User user;

    protected SessionObject session;

    private DBProvider provider;

    @Override
    public void setUp() throws Exception {
        Init.startServer();

        final TestConfig config = new TestConfig();
        existing_ctx_id = ContextStorage.getInstance().getContextId(config.getContextName());
        ctx = ContextStorage.getInstance().getContext(existing_ctx_id);

        schema = SchemaStore.getInstance().getSchema(ctx);

        user_id = ctx.getMailadmin();
        user = UserStorage.getInstance().getUser(user_id, ctx);
        session = SessionObjectWrapper.createSessionObject(user_id, ctx.getContextId(), String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void tearDown() throws Exception {
        Init.stopServer();
    }

    protected final void exec(final String sql, final Object... args) throws OXException, SQLException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = Database.get(existing_ctx_id, true);
            stmt = con.prepareStatement(sql);
            int count = 1;
            for (final Object o : args) {
                stmt.setObject(count++, o);
            }

            stmt.execute();
        } finally {

            if (null != stmt) {
                stmt.close();
            }
            Database.back(existing_ctx_id, true, con);
        }
    }

    protected final void execSafe(final String sql, final Object... args) {
        try {
            exec(sql, args);
        } catch (OXException x) {
            x.printStackTrace();
        } catch (SQLException x) {
            x.printStackTrace();
        }
    }

    protected final void assertNoResults(final String sql, final Object... args) throws OXException, SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = Database.get(existing_ctx_id, true);
            stmt = con.prepareStatement(sql);
            int count = 1;
            for (final Object o : args) {
                stmt.setObject(count++, o);
            }

            rs = stmt.executeQuery();
            assertFalse("'" + stmt.toString() + "' shouldn't select anything", rs.next());
        } finally {
            if (null != rs) {
                rs.close();
            }
            if (null != stmt) {
                stmt.close();
            }
            Database.back(existing_ctx_id, true, con);
        }
    }

    protected final void assertResult(final String sql, final Object... args) throws OXException, SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = Database.get(existing_ctx_id, true);
            stmt = con.prepareStatement(sql);
            int count = 1;
            for (final Object o : args) {
                stmt.setObject(count++, o);
            }

            rs = stmt.executeQuery();
            assertTrue("'" + stmt.toString() + "' should select something", rs.next());
        } finally {
            if (null != rs) {
                rs.close();
            }
            if (null != stmt) {
                stmt.close();
            }
            Database.back(existing_ctx_id, true, con);
        }
    }

    protected final void assertNotInFilestorage(List<String> paths) throws OXException, OXException {

        FileStorage fs = QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx);
        SortedSet<String> existingPaths = fs.getFileList();
        for (String path : paths) {
            assertFalse(existingPaths.contains(path));
        }
    }

    protected DBProvider getProvider() {
        if (provider != null) {
            return provider;
        }
        return provider = createProvider();
    }

    private DBProvider createProvider() {
        return new UpdateTaskDBProvider();
    }

    private class UpdateTaskDBProvider implements DBProvider {

        @Override
        public Connection getReadConnection(final Context ctx) throws OXException {
            return Database.get(ctx, false);
        }

        @Override
        public void releaseReadConnection(final Context ctx, final Connection con) {
            Database.back(ctx, false, con);
        }

        @Override
        public Connection getWriteConnection(final Context ctx) throws OXException {
            return Database.get(ctx, true);

        }

        @Override
        public void releaseWriteConnection(final Context ctx, final Connection con) {
            Database.back(ctx, true, con);
        }

        @Override
        public void releaseWriteConnectionAfterReading(final Context ctx, final Connection con) {
            Database.backAfterReading(ctx, con);
        }
    }
}
