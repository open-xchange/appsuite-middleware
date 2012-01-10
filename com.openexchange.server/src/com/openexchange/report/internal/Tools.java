package com.openexchange.report.internal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.management.MBeanException;
import org.apache.commons.logging.Log;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;


public class Tools {
    
    public static final Map<String, Integer> getAllSchemata(final Log logger) throws MBeanException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        final Map<String, Integer> schemaMap = new LinkedHashMap<String, Integer>(50); // Keep insertion order
        {
            final Connection readcon;
            try {
                readcon = dbService.getReadOnly();
            } catch (final OXException e) {
                logger.error(e.getMessage(), e);
                throw new MBeanException(e, "Couldn't get connection to configdb.");
            }
            /*
             * Get all schemas and put them into a map.
             */
            Statement statement = null;
            ResultSet rs = null;
            try {
                statement = readcon.createStatement();
                rs = statement.executeQuery("SELECT read_db_pool_id, db_schema FROM context_server2db_pool GROUP BY db_schema");
                while (rs.next()) {
                    schemaMap.put(rs.getString(2), Integer.valueOf(rs.getInt(1)));
                }
            } catch (final SQLException e) {
                logger.error(e.getMessage(), e);
                throw new MBeanException(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, statement);
                dbService.backReadOnly(readcon);
            }
        }
        return schemaMap;
    }
}
