
package com.openexchange.report.appsuite.storage;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;

public class DataloaderMySQL {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataloaderMySQL.class);
    DatabaseService dbService;

    public DataloaderMySQL() {
        super();
        this.dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
    }

    public ArrayList<Integer> getAllContextsForSid(Long sid) throws SQLException, OXException {
        ArrayList<Integer> result = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet sqlResult = null;
        Connection currentConnection = this.dbService.getReadOnly();
        try {
            stmt = currentConnection.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=" + sid);
            sqlResult = stmt.executeQuery();
            while (sqlResult.next()) {
                result.add(sqlResult.getInt(1));
            }
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(sqlResult, stmt);
            dbService.backReadOnly(currentConnection);
        }
        return result;
    }

}
