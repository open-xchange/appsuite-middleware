package com.openexchange.admin.contextrestore.rmi.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.contextrestore.exceptions.OXContextRestoreException;
import com.openexchange.admin.contextrestore.exceptions.OXContextRestoreException.Code;
import com.openexchange.admin.contextrestore.osgi.Activator;
import com.openexchange.admin.contextrestore.rmi.OXContextRestoreInterface;
import com.openexchange.admin.contextrestore.rmi.impl.OXContextRestore.Parser.PoolIdSchemaAndFilenames;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.database.Database;
import com.openexchange.server.impl.DBPoolingException;

/**
 * This class contains the implementation of the API defined in {@link OXContextRestoreInterface}
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class OXContextRestore extends OXCommonImpl implements OXContextRestoreInterface {

    protected static class Parser {
        
        protected class PoolIdSchemaAndFilenames {
            private final int pool_id;
            
            private final String schema;
            
            private final String[] filenames;
            
            /**
             * @param pool_id
             * @param schema
             */
            private PoolIdSchemaAndFilenames(int pool_id, String schema, String[] filenames) {
                this.pool_id = pool_id;
                this.schema = schema;
                this.filenames = filenames;
            }
            
            public final int getPool_id() {
                return pool_id;
            }
            
            public final String getSchema() {
                return schema;
            }

            public final String[] getFilenames() {
                return filenames;
            }
        }
        
        public class VersionInformation {
            private final int version;
            
            private final int locked;
            
            private final int gw_compatible;
            
            private final int admin_compatible;
            
            private final String server;
            
            /**
             * @param admin_compatible
             * @param gw_compatible
             * @param locked
             * @param server
             * @param version
             */
            public VersionInformation(final int admin_compatible, final int gw_compatible, final int locked, final String server, final int version) {
                this.admin_compatible = admin_compatible;
                this.gw_compatible = gw_compatible;
                this.locked = locked;
                this.server = server;
                this.version = version;
            }

            public final int getVersion() {
                return version;
            }

            public final int getLocked() {
                return locked;
            }

            public final int getGw_compatible() {
                return gw_compatible;
            }

            public final int getAdmin_compatible() {
                return admin_compatible;
            }

            public final String getServer() {
                return server;
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + admin_compatible;
                result = prime * result + gw_compatible;
                result = prime * result + locked;
                result = prime * result + ((server == null) ? 0 : server.hashCode());
                result = prime * result + version;
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                VersionInformation other = (VersionInformation) obj;
                if (admin_compatible != other.admin_compatible)
                    return false;
                if (gw_compatible != other.gw_compatible)
                    return false;
                if (locked != other.locked)
                    return false;
                if (server == null) {
                    if (other.server != null)
                        return false;
                } else if (!server.equals(other.server))
                    return false;
                if (version != other.version)
                    return false;
                return true;
            }

        }
        
        private final static Pattern database = Pattern.compile("^Current\\s+Database:\\s+`([^`]*)`.*$");
        
        private final static Pattern table = Pattern.compile("^Table\\s+structure\\s+for\\s+table\\s+`([^`]*)`.*$");
        
        private final static Pattern cidpattern = Pattern.compile(".*`cid`.*");
        
        private final static Pattern engine = Pattern.compile("^\\).*ENGINE=.*.*$");
        
        private final static Pattern foreignkey = Pattern.compile("^\\s+CONSTRAINT.*FOREIGN KEY\\s+\\(`([^`]*)`(?:,\\s+`([^`]*)`)*\\)\\s+REFERENCES `([^`]*)`.*$");
        
        private final static Pattern datadump = Pattern.compile("^Dumping\\s+data\\s+for\\s+table\\s+`([^`]*)`.*$");
        
        private final static Pattern insertIntoVersion = Pattern.compile("^INSERT INTO `version` VALUES \\((?:([^\\),]*),)(?:([^\\),]*),)(?:([^\\),]*),)(?:([^\\),]*),)([^\\),]*)\\).*$");

        public PoolIdSchemaAndFilenames start(final int cid, final String filename) throws FileNotFoundException, IOException, DBPoolingException, SQLException, OXContextRestoreException {
            final BufferedReader in = new BufferedReader(new FileReader(filename));
            BufferedWriter bufferedWriter = null;
            int c;
            int state = 0;
            int oldstate = 0;
            int cidpos = -1;
            final List<String> filenames = new ArrayList<String>();
            String table_name = null;
            // Set if a database is found in which the search for cid should be done
            boolean furthersearch = true;
            boolean searchcontext = false;
//            boolean searchdbpool = false;
            int pool_id = -1;
            String schema = null;
            VersionInformation versionInformation = null;
            while ((c = in.read()) != -1) {
                if (0 == state && c == '-') {
                    state = 1;
                    continue;
                } else if (1 == state) {
                    if (c == '-') {
                        state = 2;
                        continue;
                    } else {
                        state = oldstate;
                        continue;
                    }
                } else if (2 == state) {
                    if (c == ' ') {
                        final String readLine = in.readLine();
                        final Matcher dbmatcher = database.matcher(readLine);
                        final Matcher tablematcher = table.matcher(readLine);
                        final Matcher datadumpmatcher = datadump.matcher(readLine);
                        
                        if (dbmatcher.matches()) {
                            // Database found
                            final String databasename = dbmatcher.group(1);
                            if ("mysql".equals(databasename) || "information_schema".equals(databasename)) {
                                furthersearch = false;
                            } else {
                                furthersearch = true;
                            }
                            System.out.println("Database: " + databasename);
                            if (null != bufferedWriter) {
                                bufferedWriter.append("/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;" + '\n');
                                bufferedWriter.close();
                            }
                            
                            final String file = "/tmp/" + databasename + ".txt";
                            filenames.add(file);
                            bufferedWriter = new BufferedWriter(new FileWriter(file));
                            bufferedWriter.append("/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;" + '\n');
                            // Reset values
                            cidpos = -1;
                            state = 0;
                            oldstate = 0;
                        } else if (furthersearch && tablematcher.matches()) {
                            // Table found
                            table_name = tablematcher.group(1);
                            System.out.println("Table: " + table_name);
                            cidpos = -1;
                            oldstate = 0;
                            state = 3;
                        } else if (furthersearch && datadumpmatcher.matches()) {
                            // Content found
                            System.out.println("Dump found");
                            if ("version".equals(table_name)) {
                                // The version table is quite small so it is safe to read the whole line here:
                                if ((versionInformation = searchAndCheckVersion(in)) == null) {
                                    throw new OXContextRestoreException(Code.NO_VERSION_INFORMATION_FOUND);
                                }
                            }
                            if ("context_server2db_pool".equals(table_name)) {
                                searchcontext = true;
                            }
//                            if ("db_pool".equals(table_name)) {
//                                // As the table in the dump are sorted alphabetically it's safe to
//                                // assume that we have the pool id here
//                                searchdbpool = true;
//                            }
                            state = 5;
                            oldstate = 0;
                        } else {
                            state = 0;
                            oldstate = 0;
                        }
                        continue;
                    } else {
                        state = oldstate;
                    }
                } else if (3 == state && c == 'C') {
                    final String creatematchpart = "REATE";
                    state = returnRightStateToString(in, creatematchpart, 4, 3);
                    continue;
                } else if (3 == state && c == '-') {
                    oldstate = 3;
                    state = 1;
                    continue;
                } else if (4 == state && c == '(') {
                    cidpos = cidsearch(in);
                    System.out.println("Cid pos: " + cidpos);
                    state = 0;
                    continue;
                } else if (5 == state && c == 'I') {
                    state = returnRightStateToString(in, "NSERT", 6, 5);
                    continue;
                } else if (5 == state && c == '-') {
                    oldstate = 5;
                    state = 1;
                } else if (6 == state && c == '(') {
                    System.out.println("Insert found and cid=" + cidpos);
                    // Now we search for matching cids and write them to the tmp file
                    if (searchcontext) {
                        final String value[] = searchAndWriteMatchingCidValues(in, bufferedWriter, cidpos, Integer.toString(cid), table_name, true, true);
                        try {
                            pool_id = Integer.parseInt(value[1]);
                        } catch (final NumberFormatException e) {
                            throw new OXContextRestoreException(Code.COULD_NOT_CONVERT_POOL_VALUE);
                        }
                        schema = value[2];
//                    } else if (searchdbpool) {
//                        final String value[] = searchAndWriteMatchingCidValues(in, bufferedWriter, 1, Integer.toString(pool_id), table_name, true, false);
//                        searchdbpool = false;
//                        System.out.println(Arrays.toString(value));
                    } else {
                        searchAndWriteMatchingCidValues(in, bufferedWriter, cidpos, Integer.toString(cid), table_name, false, true);
                    }
                    searchcontext = false;
                    oldstate = 0;
                    state = 0;
                }
                // Reset state machine at the end of the line if we are in the first two states
                if (3 > state && c == '\n') {
                    state = 0;
                    continue;
                }
            }
            bufferedWriter.close();
            checkVersion(versionInformation, pool_id, schema);
            return new PoolIdSchemaAndFilenames(pool_id, schema, filenames.toArray(new String[filenames.size()]));
        }

        private void checkVersion(final VersionInformation versionInformation, final int pool_id, final String schema) throws SQLException, DBPoolingException, OXContextRestoreException {
            Connection connection = null;
            PreparedStatement prepareStatement = null;
            try {
                connection = Database.get(pool_id, schema);
                prepareStatement = connection.prepareStatement("SELECT `version`, `locked`, `gw_compatible`, `admin_compatible`, `server` FROM `version`");
                
                final ResultSet result = prepareStatement.executeQuery();
                if (result.next()) {
                    final VersionInformation versionInformation2 = new VersionInformation(result.getInt(4), result.getInt(3), result.getInt(2), result.getString(5), result.getInt(1));
                    if (!versionInformation.equals(versionInformation2)) {
                        throw new OXContextRestoreException(Code.VERSION_TABLES_INCOMPATIBLE);
                    }
                } else {
                    // Error there must be at least one row
                    throw new OXContextRestoreException(Code.NO_ENTRIES_IN_VERSION_TABLE);
                }
                
            } finally {
                if (null != prepareStatement) {
                    prepareStatement.close();
                }
                if (null != connection) {
                    Database.back(pool_id, connection);
                }
            }
        }

        /**
         * @param in
         * @return
         * @throws IOException
         */
        private VersionInformation searchAndCheckVersion(final BufferedReader in) throws IOException {
            String readLine2 = in.readLine();
            while ((readLine2 = in.readLine()) != null && !readLine2.equals("--")) {
                final Matcher matcher = insertIntoVersion.matcher(readLine2);
                if (matcher.matches()) {
                    final int version = Integer.parseInt(matcher.group(1));
                    final int locked = Integer.parseInt(matcher.group(2));
                    final int gw_compatible = Integer.parseInt(matcher.group(3));
                    final int admin_compatible = Integer.parseInt(matcher.group(4));
                    final String server = matcher.group(5);
                    
                    return new VersionInformation(admin_compatible, gw_compatible, locked, server.substring(1, server.length() - 1), version);
                }
            }
            return null;
        }
        
        /**
         * @param in
         * @param bufferedWriter
         * @param valuepos The position of the value inside the value row
         * @param value The value itself
         * @param table_name
         * @param readall If the rest of the row should be returned as string array after a match or not
         * @param contextsearch TODO
         * @throws IOException
         */
        private String[] searchAndWriteMatchingCidValues(final BufferedReader in, final Writer bufferedWriter, final int valuepos, final String value, final String table_name, boolean readall, boolean contextsearch) throws IOException {
            final StringBuilder currentValues = new StringBuilder();
            currentValues.append("(");
            final StringBuilder lastpart = new StringBuilder();
            int c = 0;
            int counter = 1;
            boolean instring = false;
            boolean indatarow = true;
            boolean found = false;
            boolean firstfound = true;
            final ArrayList<String> retval = new ArrayList<String>();
            while ((c = in.read()) != -1) {
                if (indatarow) {
                    if (c == ',' || (!instring && c == ')')) {
                        if (counter == valuepos) {
                            if (lastpart.toString().equals(value)) {
                                found = true;
                            }
                        } else if (readall && found) {
                            retval.add(lastpart.toString());
                        }
                        counter++;
                        lastpart.setLength(0);
                    } else if (c == '\'') {
                        instring = !instring;
                    } else {
                        lastpart.append((char)c);
                    }
                    currentValues.append((char)c);
                } else {
                    if (c == ',') {
                        // New datarow comes
                        counter = 1;
                    } else if (c == '(') {
                        indatarow = true;
                        currentValues.setLength(0);
                        currentValues.append('(');
                    } else if (c == ';') {
                        if (!firstfound && contextsearch) {
                            // End of VALUES part
                            bufferedWriter.write(";");
                            bufferedWriter.write("\n");
                        }
                        break;
                    }
                }
                if (!instring && c == ')') {
                    indatarow = false;
                    lastpart.setLength(0);
                    if (found && contextsearch) {
                        if (firstfound) {
                            bufferedWriter.write("INSERT INTO `");
                            bufferedWriter.write(table_name);
                            bufferedWriter.write("` VALUES ");
                            firstfound = false;
                        } else {
                            bufferedWriter.write(",");
                        }
                        bufferedWriter.write(currentValues.toString());
                        bufferedWriter.flush();
                        found = false;
                    }
                }
            }
            return retval.toArray(new String[retval.size()]);
        }

        private int returnRightStateToString(final BufferedReader in, final String string, int successstate, int failstate) throws IOException {
            final int length = string.length();
            char[] arr = new char[length];
            int i;
            if ((i = in.read(arr)) != -1 && length == i) {
                if (string.equals(new String(arr))) {
                    return successstate;
                } else {
                    return failstate;
                }
            } else {
                // File at the end or no more chars
                return -1;
            }
        }
        
        /**
         * Searches for the cid and returns the line number in which is was found,
         * after this method the reader's position is behind the create structure 
         * 
         * @param in
         * @return
         * @throws IOException 
         */
        private int cidsearch(final BufferedReader in) throws IOException {
            String readLine;
            readLine = in.readLine();
            int columnpos = 0;
            boolean found = false;
            while (null != readLine) {
                final Matcher cidmatcher = cidpattern.matcher(readLine);
                final Matcher enginematcher = engine.matcher(readLine);
                // Now searching for cid text...
                if (cidmatcher.matches()) {
                    final List<String> searchingForeignKey = searchingForeignKey(in);
                    System.out.println("Foreign Keys: " + searchingForeignKey);
                    found = true;
                    break;
                } else if (enginematcher.matches()) {
                    break;
                }
                columnpos++;
                readLine = in.readLine();
            }
            if (found) {
                return columnpos;
            } else {
                return -1;
            }
        }
        
        private List<String> searchingForeignKey(BufferedReader in) throws IOException {
            String readLine;
            readLine = in.readLine();
            List<String> foreign_keys = null;
            while (null != readLine) {
                final Matcher matcher = foreignkey.matcher(readLine);
                final Matcher enginematcher = engine.matcher(readLine);
                if (matcher.matches()) {
                    foreign_keys = get_foreign_keys(matcher);
                } else if (enginematcher.matches()) {
                    return foreign_keys;
                }
                readLine = in.readLine();
            }
            return null;
        }

        private List<String> get_foreign_keys(Matcher matcher) {
            final ArrayList<String> retval = new ArrayList<String>();
            final int groupCount = matcher.groupCount();
            for (int i = 1; i < groupCount; i++) {
                final String group = matcher.group(i);
                if (null != group) {
                    retval.add(group);
                }
            }
            return retval;
        }

    }

    private final static Log LOG = LogFactory.getLog(OXContextRestore.class);
    
    private final BasicAuthenticator basicauth;

    public OXContextRestore() throws StorageException {
        super();
        basicauth = new BasicAuthenticator();
    }

    public String restore(final Context ctx, final String[] filenames, final Credentials auth) throws InvalidDataException, InvalidCredentialsException, StorageException, OXContextRestoreException, DatabaseUpdateException {
        try {
            doNullCheck(ctx, filenames);
        } catch (final InvalidDataException e) {
            LOG.error("One of the arguments for restore is null", e);
            throw e;
        }
        
        try {
            basicauth.doAuthentication(auth);
        } catch (final InvalidCredentialsException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }

        final Parser parser = new Parser();
        System.out.println("Context: " + ctx);
        System.out.println("Filenames: " + filenames);
        System.out.println("Creds:" + auth);
        
        try {
            final PoolIdSchemaAndFilenames start = parser.start(ctx.getId(), filenames[0]);
            
            final OXContextInterface contextInterface = Activator.getContextInterface();
            
            final OXToolStorageInterface storage = OXToolStorageInterface.getInstance();
            if (storage.existsContext(ctx)) {
                try {
                    contextInterface.delete(ctx, auth);
                } catch (final NoSuchContextException e) {
                    // As we check for the existence beforehand this exception should never occur. Nevertheless we will log this
                    LOG.fatal("FATAL:" + e.getMessage(), e);
                }
            }
            // We have to do the exists check beforehand otherwise you'll find a stack trace in the logs
            return restorectx(ctx, start);
        } catch (final FileNotFoundException e) {
            LOG.error("File not found");
            // TODO: Throw right exception here
            throw new OXContextRestoreException(Code.COULD_NOT_CONVERT_POOL_VALUE);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            // TODO: Throw right exception here
            throw new OXContextRestoreException(Code.COULD_NOT_CONVERT_POOL_VALUE);
        } catch (final DBPoolingException e) {
            LOG.error(e);
            // TODO: Throw right exception here
            throw new OXContextRestoreException(Code.COULD_NOT_CONVERT_POOL_VALUE);
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new OXContextRestoreException(Code.DATABASE_OPERATION_ERROR, e.getMessage());
        } catch (final OXContextRestoreException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }
    
    private String restorectx(final Context ctx, final PoolIdSchemaAndFilenames poolidandschema) throws DBPoolingException, SQLException, FileNotFoundException, IOException, OXContextRestoreException {
        Connection connection = null;
        Connection connection2 = null;
        PreparedStatement prepareStatement = null;
        PreparedStatement prepareStatement2 = null;
        PreparedStatement prepareStatement3 = null;
        final int pool_id = poolidandschema.getPool_id();
        try {
            File file = new File("/tmp/" + poolidandschema.getSchema()  + ".txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String in = null;
            connection = Database.get(pool_id, poolidandschema.getSchema());
            connection.setAutoCommit(false);
            while ((in = reader.readLine()) != null) {
                prepareStatement = connection.prepareStatement(in);
                prepareStatement.execute();
                prepareStatement.close();
            }
            file = new File("/tmp/configdb.txt");
            reader = new BufferedReader(new FileReader(file));
            in = null;
            connection2 = Database.get(true);
            connection2.setAutoCommit(false);
            while ((in = reader.readLine()) != null) {
                prepareStatement2 = connection2.prepareStatement(in);
                prepareStatement2.execute();
                prepareStatement2.close();
            }
            connection.commit();
            connection.setAutoCommit(true);
            connection2.commit();
            connection2.setAutoCommit(true);
            
            prepareStatement3 = connection2.prepareStatement("SELECT `filestore_name`, `uri` FROM `context` INNER JOIN `filestore` ON context.filestore_id = filestore.id WHERE cid=?");
            prepareStatement3.setInt(1, ctx.getId());
            final ResultSet executeQuery = prepareStatement3.executeQuery();
            if (executeQuery.next()) {
                final String filestore_name = executeQuery.getString(1);
                final String uri = executeQuery.getString(2);
                return uri + File.separatorChar + filestore_name;
            } else {
                // TODO: Throw right exception here
                throw new OXContextRestoreException(Code.COULD_NOT_CONVERT_POOL_VALUE);
            }
        } catch (final SQLException e) {
            dorollback(connection, connection2, e);
            throw e;
        } catch (final FileNotFoundException e) {
            dorollback(connection, connection2, e);
            throw e;
        } catch (final DBPoolingException e) {
            dorollback(connection, connection2, e);
            throw e;
        } catch (final IOException e) {
            dorollback(connection, connection2, e);
            throw e;
        } finally {
            closePreparedStatement(prepareStatement);
            closePreparedStatement(prepareStatement2);
            closePreparedStatement(prepareStatement3);
            if (null != connection) {
                Database.back(pool_id, connection);
            }
        }
    }

    private void dorollback(Connection conn, Connection conn2, Exception e2) throws OXContextRestoreException {
        if (null != conn) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                LOG.error(e2.getMessage(), e2);
                throw new OXContextRestoreException(Code.ROLLBACK_ERROR, e.getMessage());
            }
        }
        if (null != conn2) {
            try {
                conn2.rollback();
            } catch (SQLException e) {
                LOG.error(e2.getMessage(), e2);
                throw new OXContextRestoreException(Code.ROLLBACK_ERROR, e.getMessage());
            }
        }
    }

    private void closePreparedStatement(final PreparedStatement ps) {
        try {
            if (null != ps) {
                ps.close();
            }
        } catch (final SQLException e) {
            LOG.error("Error closing prepared statement!", e);
        }
    }


}
