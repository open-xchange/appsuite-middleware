package com.openexchange.groupware.update;

import com.openexchange.database.Database;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.server.impl.DBPoolingException;
import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateTest extends TestCase {
    protected Schema schema = null;
    protected int existing_ctx_id = 0;
    protected int user_id = -1;
    protected Context ctx;
    protected User user;
    private DBProvider provider;

    public void setUp() throws Exception {
        Init.startServer();
        ContextStorage.init();

        existing_ctx_id = ContextStorage.getInstance().getContextId("defaultcontext");
        ctx = ContextStorage.getInstance().getContext(existing_ctx_id);

        schema = SchemaStore.getInstance(SchemaStoreImpl.class.getName()).getSchema(existing_ctx_id);
     
        user_id = ctx.getMailadmin();
        user = UserStorage.getInstance().getUser(user_id, ctx);
    }

    public void tearDown() throws Exception {
        Init.stopServer();
    }

    protected final void exec(String sql, Object...args) throws DBPoolingException, SQLException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = Database.get(existing_ctx_id, true);
            stmt = con.prepareStatement(sql);
            int count = 1;
            for(Object o : args) {
                stmt.setObject(count++,o);
            }

            stmt.executeUpdate();
        } finally {

            if(null != stmt) {
                stmt.close();
            }
            Database.back(existing_ctx_id, true, con);
        }
    }

    protected final void assertNoResults(String sql, Object...args) throws DBPoolingException, SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = Database.get(existing_ctx_id, true);
            stmt = con.prepareStatement(sql);
            int count = 1;
            for(Object o : args) {
                stmt.setObject(count++,o);
            }

            rs = stmt.executeQuery();
            assertFalse("'"+stmt.toString()+"' shouldn't select anything", rs.next());
        } finally {
            if(null != rs) {
                rs.close();
            }
            if(null != stmt) {
                stmt.close();
            }
            Database.back(existing_ctx_id, true, con);
        }
    }

    protected DBProvider getProvider(){
        if(provider != null)
            return provider;
        return provider = createProvider();
    }

    private DBProvider createProvider() {
        return new UpdateTaskDBProvider();
    }

    private class UpdateTaskDBProvider implements DBProvider {
        public Connection getReadConnection(Context ctx) throws TransactionException {
            try {
                return Database.get(ctx, false);
            } catch (DBPoolingException e) {
                throw new TransactionException(e);
            }
        }

        public void releaseReadConnection(Context ctx, Connection con) {
            Database.back(ctx, false, con);
        }

        public Connection getWriteConnection(Context ctx) throws TransactionException {
            try {
                return Database.get(ctx, true);
            } catch (DBPoolingException e) {
                throw new TransactionException(e);
            }
        }

        public void releaseWriteConnection(Context ctx, Connection con) {
            Database.back(ctx, true, con);
        }
    }
}
