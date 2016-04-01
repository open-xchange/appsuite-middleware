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

package com.openexchange.admin.contextrestore.rmi.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.output.NullWriter;
import com.openexchange.admin.contextrestore.dataobjects.UpdateTaskEntry;
import com.openexchange.admin.contextrestore.dataobjects.UpdateTaskInformation;
import com.openexchange.admin.contextrestore.osgi.Activator;
import com.openexchange.admin.contextrestore.rmi.OXContextRestoreInterface;
import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException;
import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException.Code;
import com.openexchange.admin.contextrestore.rmi.impl.OXContextRestore.Parser.PoolIdSchemaAndVersionInfo;
import com.openexchange.admin.contextrestore.storage.interfaces.OXContextRestoreStorageInterface;
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

/**
 * This class contains the implementation of the API defined in {@link OXContextRestoreInterface}
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>: Bugfix 20044
 */
public class OXContextRestore extends OXCommonImpl implements OXContextRestoreInterface {

    public class RunParserResult {

        private final PoolIdSchemaAndVersionInfo result;

        private final UpdateTaskInformation updateTaskInfo;

        public RunParserResult(PoolIdSchemaAndVersionInfo result, UpdateTaskInformation updateTaskInfo) {
            super();
            this.result = result;
            this.updateTaskInfo = updateTaskInfo;
        }

        public PoolIdSchemaAndVersionInfo getResult() {
            return result;
        }

        public UpdateTaskInformation getUpdateTaskInfo() {
            return updateTaskInfo;
        }

    }

    /** The reference for ConfigDB name */
    static final AtomicReference<String> CONFIGDB_NAME = new AtomicReference<String>("configdb");

    /**
     * Sets the name of the ConfigDB.
     *
     * @param configDbName The name
     */
    public static void setConfigDbName(final String configDbName) {
        CONFIGDB_NAME.set(configDbName);
    }

    /**
     * Safely closes specified {@link Closeable} instance.
     *
     * @param toClose The {@link Closeable} instance
     */
    protected static void close(final Closeable toClose) {
        if (null != toClose) {
            try {
                toClose.close();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Safely flushes specified {@link Flushable} instance.
     *
     * @param toFlush The {@link Flushable} instance
     */
    protected static void flush(final Flushable toFlush) {
        if (null != toFlush) {
            try {
                toFlush.flush();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Parser for MySQL dump files.
     */
    public static class Parser {

        public class PoolIdSchemaAndVersionInfo {

            private final int poolId;
            private final int contextId;
            private final String schema;
            private final String fileName;

            private Map<String, File> tempfilemap;
            private UpdateTaskInformation updateTaskInformation;

            protected PoolIdSchemaAndVersionInfo(final String fileName, final int contextId, int poolId, String schema, UpdateTaskInformation updateTaskInformation) {
                super();
                this.fileName = fileName;
                this.contextId = contextId;
                this.poolId = poolId;
                this.schema = schema;
                this.updateTaskInformation = updateTaskInformation;
            }

            public String getFileName() {
                return fileName;
            }

            public int getContextId() {
                return contextId;
            }

            public final int getPoolId() {
                return poolId;
            }

            public final String getSchema() {
                return schema;
            }

            public Map<String, File> getTempfilemap() {
                return tempfilemap;
            }

            public void setTempfilemap(Map<String, File> tempfilemap) {
                this.tempfilemap = tempfilemap;
            }

            public UpdateTaskInformation getUpdateTaskInformation() {
                return updateTaskInformation;
            }

            public void setUpdateTaskInformation(UpdateTaskInformation updateTaskInformation) {
                this.updateTaskInformation = updateTaskInformation;
            }

        }

        private final static Pattern database = Pattern.compile("^.*?(?:Current )?Database:\\s+`?([^` ]*)`?.*$");

        private final static Pattern table = Pattern.compile("^Table\\s+structure\\s+for\\s+table\\s+`([^`]*)`.*$");

        private final static Pattern cidpattern = Pattern.compile(".*`cid`.*");

        private final static Pattern engine = Pattern.compile("^\\).*ENGINE=.*.*$");

        private final static Pattern foreignkey =
            Pattern.compile("^\\s+CONSTRAINT.*FOREIGN KEY\\s+\\(`([^`]*)`(?:,\\s+`([^`]*)`)*\\)\\s+REFERENCES `([^`]*)`.*$");

        private final static Pattern datadump = Pattern.compile("^Dumping\\s+data\\s+for\\s+table\\s+`([^`]*)`.*$");

        /**
         * Starts parsing named MySQL dump file
         *
         * @param cid The context identifier
         * @param fileName The name of the MySQL dump file
         * @param optConfigDbName The optional name of the ConfigDB schema
         * @param schema TODO
         * @return The information object for parsed MySQL dump file
         * @throws IOException If an I/O error occurs
         * @throws OXContextRestoreException If a context restore error occurs
         */
        @SuppressWarnings("synthetic-access")
        public PoolIdSchemaAndVersionInfo start(final int cid, final String fileName, final String optConfigDbName, String schema, final Map<String, File> tempfilemap) throws IOException, OXContextRestoreException {
            int c;
            int state = 0;
            int oldstate = 0;
            int cidpos = -1;
            String tableName = null;
            // Set if a database is found in which the search for cid should be done
            boolean furthersearch = true;
            // Defines if we have found a contextserver2pool table
            boolean searchcontext = false;
            // boolean searchdbpool = false;
            int poolId = -1;
            UpdateTaskInformation updateTaskInformation = null;

            final BufferedReader in = new BufferedReader(new FileReader(fileName));
            BufferedWriter bufferedWriter = null;
            try {
                while ((c = in.read()) != -1) {
                    if (0 == state && c == '-') {
                        state = 1; // Started comment line
                        continue;
                    } else if (1 == state) {
                        if (c == '-') {
                            state = 2; // Read comment prefix "--"
                            continue;
                        }
                        // Not a comment prefix; an interpretable line
                        state = oldstate;
                        continue;
                    } else if (2 == state) {
                        if (c == ' ') { // Comment line: "-- " + <rest-of-line>
                            searchcontext = false;
                            final String readLine = in.readLine();
                            final Matcher dbmatcher = database.matcher(readLine);
                            final Matcher tablematcher = table.matcher(readLine);
                            final Matcher datadumpmatcher = datadump.matcher(readLine);

                            if (dbmatcher.matches()) {
                                // Database found
                                final String databasename = dbmatcher.group(1);
                                if (getConfigDbName(optConfigDbName).equals(databasename) || (null != schema && schema.equals(databasename))) {
                                    furthersearch = true;
                                    LOG.info("Database: {}", databasename);
                                    if (null != bufferedWriter) {
                                        bufferedWriter.append("/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;\n");
                                        bufferedWriter.flush();
                                        bufferedWriter.close();
                                    }

                                    if (!tempfilemap.containsKey(databasename)) {
                                        final File createTempFile = File.createTempFile(databasename, null);
                                        tempfilemap.put(databasename, createTempFile);
                                        bufferedWriter = new BufferedWriter(new FileWriter(createTempFile));
                                        bufferedWriter.append("/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;\n");
                                    } else {
                                        // We are in the seconds pass so we don't need to write the configdb entries again
                                        bufferedWriter = new BufferedWriter(new NullWriter());
                                    }
                                    // Reset values
                                    cidpos = -1;
                                    state = 0;
                                    oldstate = 0;
                                } else {
                                    furthersearch = false;
                                }
                            } else if (furthersearch && tablematcher.matches()) {
                                // Table found
                                tableName = tablematcher.group(1);
                                LOG.info("Table: {}", tableName);
                                cidpos = -1;
                                oldstate = 0;
                                state = 3;
                            } else if (furthersearch && datadumpmatcher.matches()) {
                                // Content found
                                LOG.info("Dump found");
                                if ("updateTask".equals(tableName)) {
                                    // One or more entries for 'updateTask' table
                                    updateTaskInformation = searchAndCheckUpdateTask(in, cid);
                                }
                                if ("context_server2db_pool".equals(tableName)) {
                                    searchcontext = true;
                                }
                                // if ("db_pool".equals(table_name)) {
                                // // As the table in the dump are sorted alphabetically it's safe to
                                // // assume that we have the pool id here
                                // searchdbpool = true;
                                // }
                                state = 5;
                                oldstate = 0;
                            } else {
                                state = 0;
                                oldstate = 0;
                            }
                            continue;
                        }
                        // Reset to old state
                        state = oldstate;
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
                        LOG.info("Cid pos: {}", cidpos);
                        state = 0;
                        continue;
                    } else if (5 == state && c == 'I') {
                        state = returnRightStateToString(in, "NSERT", 6, 5);
                        continue;
                    } else if (5 == state && c == '-') {
                        oldstate = 5;
                        state = 1;
                    } else if (6 == state && c == '(') {
                        LOG.info("Insert found and cid={}", cidpos);
                        // Now we search for matching cids and write them to the tmp file
                        if (searchcontext && null != bufferedWriter) {
                            final String value[] =
                                searchAndWriteMatchingCidValues(in, bufferedWriter, cidpos, Integer.toString(cid), tableName, true, true);
                            if (value.length >= 2) {
                                try {
                                    poolId = Integer.parseInt(value[1]);
                                } catch (final NumberFormatException e) {
                                    throw new OXContextRestoreException(Code.COULD_NOT_CONVERT_POOL_VALUE);
                                }
                                schema = value[2];
                                // } else if (searchdbpool) {
                                // final String value[] = searchAndWriteMatchingCidValues(in, bufferedWriter, 1, Integer.toString(pool_id),
                                // table_name, true, false);
                                // searchdbpool = false;
                                // System.out.println(Arrays.toString(value));
                            } else {
                                state=5;
                                continue;
                            }
                        } else if (null != bufferedWriter) {
                            // Here we should only search if a fitting db was found and thus the writer was set
                            searchAndWriteMatchingCidValues(in, bufferedWriter, cidpos, Integer.toString(cid), tableName, false, true);
                        }
                        searchcontext = false;
                        oldstate = 0;
                        state = 5;
                    }
                    // Reset state machine at the end of the line if we are in the first two states
                    if (3 > state && c == '\n') {
                        state = 0;
                        continue;
                    }
                }
            } finally {
                flush(bufferedWriter);
                close(bufferedWriter);
                close(in);
            }
            //if (null == updateTaskInformation) {
            //    throw new OXContextRestoreException(Code.NO_UPDATE_TASK_INFORMATION_FOUND);
            // }
            return new PoolIdSchemaAndVersionInfo(fileName, cid, poolId, schema, updateTaskInformation);
        }

        private final static String REGEX_VALUE = "([^\\),]*)";
        private final static Pattern insertIntoUpdateTaskValues =
            Pattern.compile("\\((?:" + REGEX_VALUE + ",)(?:" + REGEX_VALUE + ",)(?:" + REGEX_VALUE + ",)(?:" + REGEX_VALUE + ",)" + ".*?\\)");

        private UpdateTaskInformation searchAndCheckUpdateTask(final BufferedReader in, final int contextId) throws IOException {
            final UpdateTaskInformation retval = new UpdateTaskInformation();
            String line = in.readLine();
            while ((line = in.readLine()) != null && !line.startsWith("--")) {
                if (line.startsWith("INSERT INTO `updateTask` VALUES ")) {
                    final Matcher matcher = insertIntoUpdateTaskValues.matcher(line.substring(32));
                    while (matcher.find()) {
                        final UpdateTaskEntry updateTaskEntry = new UpdateTaskEntry();
                        final int contextId2 = Integer.parseInt(matcher.group(1));
                        if (contextId2 <= 0 || contextId2 == contextId) {
                            updateTaskEntry.setContextId(contextId2);
                            updateTaskEntry.setTaskName(matcher.group(2).replaceAll("'", ""));
                            updateTaskEntry.setSuccessful((Integer.parseInt(matcher.group(3)) > 0));
                            updateTaskEntry.setLastModified(Long.parseLong(matcher.group(4)));
                            retval.add(updateTaskEntry);
                        }
                    }
                }
            }
            return retval;
        }

        /**
         * @param in
         * @param bufferedWriter
         * @param valuepos The position of the value inside the value row
         * @param value The value itself
         * @param table_name
         * @param readall If the rest of the row should be returned as string array after a match or not
         * @param contextsearch
         * @throws IOException
         */
        private String[] searchAndWriteMatchingCidValues(final BufferedReader in, final Writer bufferedWriter, final int valuepos, final String value, final String table_name, boolean readall, boolean contextsearch) throws IOException {
            final StringBuilder currentValues = new StringBuilder();
            currentValues.append("(");
            final StringBuilder lastpart = new StringBuilder();
            int c = 0;
            int counter = 1;
            // If we are inside a string '' or not
            boolean instring = false;
            // If we are inside a dataset () or not
            boolean indatarow = true;
            // Have we found the value we searched for?
            boolean found = false;
            // Is this the first time we found the value
            boolean firstfound = true;
            // Are we in escapted mode
            boolean escapted = false;
            // Used for only escaping one char
            boolean firstescaperun = false;
            // Used to leave the loop
            boolean continuation = true;
            final ArrayList<String> retval = new ArrayList<String>();
            while ((c = in.read()) != -1 && continuation) {
                if (firstescaperun && escapted) {
                    escapted = false;
                    firstescaperun = false;
                }
                if (escapted) {
                    firstescaperun = true;
                }
                switch (c) {
                case '(':
                    if (!indatarow) {
                        indatarow = true;
                        currentValues.setLength(0);
                        currentValues.append('(');
                    } else {
                        currentValues.append((char) c);
                    }
                    break;
                case ')':
                    if (indatarow) {
                        if (!instring) {
                            if (counter == valuepos) {
                                if (lastpart.toString().equals(value)) {
                                    found = true;
                                }
                            } else if (readall && found) {
                                retval.add(lastpart.toString());
                            }
                            lastpart.setLength(0);
                            indatarow = false;
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
                                bufferedWriter.write(")");
                                bufferedWriter.flush();
                                found = false;
                            }
                        }
                        currentValues.append((char) c);
                    }
                    break;
                case ',':
                    if (indatarow) {
                        if (!instring) {
                            if (counter == valuepos) {
                                if (lastpart.toString().equals(value)) {
                                    found = true;
                                }
                            } else if (readall && found) {
                                retval.add(lastpart.toString());
                            }
                            counter++;
                            lastpart.setLength(0);
                        }
                        currentValues.append((char) c);
                    } else {
                        // New datarow comes
                        counter = 1;
                    }
                    break;
                case '\'':
                    if (indatarow) {
                        if (!instring) {
                            instring = true;
                        } else {
                            if (!escapted) {
                                instring = false;
                            }
                        }
                        currentValues.append((char) c);
                    }
                    break;
                case '\\':
                    if (indatarow) {
                        if (instring && !escapted) {
                            escapted = true;
                        }
                        currentValues.append((char) c);
                    }
                    break;
                case ';':
                    if (!indatarow) {
                        if (!firstfound && contextsearch) {
                            // End of VALUES part
                            bufferedWriter.write(";");
                            bufferedWriter.write("\n");
                        }
                        continuation = false;
                    } else {
                        currentValues.append((char) c);
                    }
                    break;
                default:
                    if (indatarow) {
                        lastpart.append((char) c);
                        currentValues.append((char) c);
                    }
                    break;
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
                }
                return failstate;
            }
            // File at the end or no more chars
            return -1;
        }

        /**
         * Searches for the cid and returns the line number in which is was found, after this method the reader's position is behind the
         * create structure
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
                    LOG.info("Foreign Keys: {}", searchingForeignKey);
                    found = true;
                    break;
                } else if (enginematcher.matches()) {
                    break;
                }
                columnpos++;
                readLine = in.readLine();
            }
            if (!found) {
                return -1;
            }
            return columnpos;
        }

        private List<String> searchingForeignKey(final BufferedReader in) throws IOException {
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

    protected final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXContextRestore.class);

    private final BasicAuthenticator basicauth;

    public OXContextRestore() throws StorageException {
        super();
        basicauth = new BasicAuthenticator();
    }

    @Override
    public String restore(final Context ctx, final String[] fileNames, final String optConfigDbName, final Credentials auth, final boolean dryrun) throws InvalidDataException, InvalidCredentialsException, StorageException, OXContextRestoreException, DatabaseUpdateException {
        try {
            doNullCheck(ctx, fileNames);
            for (final String filename : fileNames) {
                doNullCheck(filename);
            }
        } catch (final InvalidDataException e) {
            LOG.error("One of the arguments for restore is null", e);
            throw e;
        }

        try {
            basicauth.doAuthentication(auth);
        } catch (final InvalidCredentialsException e) {
            LOG.error("", e);
            throw e;
        }
        
        final OXToolStorageInterface storage = OXToolStorageInterface.getInstance();
        if (storage.isLastContextInSchema(ctx)) {
            throw new OXContextRestoreException(Code.LAST_CONTEXT_IN_SCHEMA, ctx.getIdAsString());
        }
        
        LOG.info("Context: {}", ctx);
        LOG.info("Filenames: {}", java.util.Arrays.toString(fileNames));

        try {
            final HashMap<String, File> tempfilemap = new HashMap<String, File>();
            RunParserResult test = runParser(ctx, fileNames, optConfigDbName, null, tempfilemap);
            if (null == test.getResult()) {
                throw new OXContextRestoreException(Code.NO_CONFIGDB_FOUND);
            }
            if (null == test.getUpdateTaskInfo()) {
                // Trigger seconds round because the user database can be located before the configdb entries
                test = runParser(ctx, fileNames, optConfigDbName, test.getResult().getSchema(), tempfilemap);
                if (null == test.getUpdateTaskInfo()) {
                    // Still no user database found. Exiting
                    throw new OXContextRestoreException(Code.NO_USER_DATA_DB_FOUND);
                }
            }
            final PoolIdSchemaAndVersionInfo result = test.getResult();

            final OXContextRestoreStorageInterface instance = OXContextRestoreStorageInterface.getInstance();
            result.setUpdateTaskInformation(test.getUpdateTaskInfo());
            result.setTempfilemap(tempfilemap);

            if (dryrun) {
                return "Done nothing (dry run)";
            }

            final OXContextInterface contextInterface = Activator.getContextInterface();

            // We have to do the exists check beforehand otherwise you'll find a stack trace in the logs
            if (storage.existsContext(ctx)) {
                try {
                    contextInterface.delete(ctx, auth);
                } catch (final NoSuchContextException e) {
                    // As we check for the existence beforehand this exception should never occur. Nevertheless we will log this
                    LOG.error("FATAL", e);
                }
            }
            return instance.restorectx(ctx, result, getConfigDbName(optConfigDbName));
        } catch (final StorageException e) {
            LOG.error("", e);
            throw e;
        } catch (final IOException e) {
            LOG.error("", e);
            throw new OXContextRestoreException(Code.IO_EXCEPTION, e);
        } catch (final SQLException e) {
            LOG.error("", e);
            throw new OXContextRestoreException(Code.DATABASE_OPERATION_ERROR, e, e.getMessage());
        } catch (final OXContextRestoreException e) {
            LOG.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOG.error("", e);
            throw e;
        } catch (final Exception e) {
            LOG.error("", e);
            throw new OXContextRestoreException(Code.UNEXPECTED_ERROR, e, e.getMessage());
        }
    }

    private RunParserResult runParser(final Context ctx, final String[] fileNames, final String optConfigDbName, String schema, Map<String, File> filemap) throws IOException, OXContextRestoreException {
        UpdateTaskInformation updateTaskInfo = null;
        PoolIdSchemaAndVersionInfo result = null;
        for (final String fileName : fileNames) {
            final PoolIdSchemaAndVersionInfo infoObject = new Parser().start(ctx.getId().intValue(), fileName, optConfigDbName, schema, filemap);
            final UpdateTaskInformation updateTaskInformation = infoObject.getUpdateTaskInformation();
            if (null != updateTaskInformation) {
                updateTaskInfo = updateTaskInformation;
            }
            if (null != infoObject.getSchema() && -1 != infoObject.getPoolId()) {
                result = infoObject;
            }
        }
        return new RunParserResult(result, updateTaskInfo);
    }

    private static String getConfigDbName(final String optConfigDbName) {
        String configDbName = optConfigDbName;
        if (null == configDbName) {
            configDbName = CONFIGDB_NAME.get();
            if (null == configDbName) {
                configDbName = "configdb";
            }
        }
        return configDbName;
    }

}
