package com.openexchange.admin.contextrestore.rmi.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.contextrestore.rmi.OXContextRestoreInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;

/**
 * This class contains the implementation of the API defined in {@link OXContextRestoreInterface}
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class OXContextRestore extends OXCommonImpl implements OXContextRestoreInterface {

    private static class Parser {
        private final static Pattern database = Pattern.compile("^Current\\s+Database:\\s+`([^`]*)`.*$");
        
        private final static Pattern table = Pattern.compile("^Table\\s+structure\\s+for\\s+table\\s+`([^`]*)`.*$");
        
        private final static Pattern cidpattern = Pattern.compile(".*`cid`.*");
        
        private final static Pattern engine = Pattern.compile("^\\).*ENGINE=.*.*$");
        
        private final static Pattern foreignkey = Pattern.compile("^\\s+CONSTRAINT.*FOREIGN KEY\\s+\\(`([^`]*)`(?:,\\s+`([^`]*)`)*\\)\\s+REFERENCES `([^`]*)`.*$");
        
        private final static Pattern datadump = Pattern.compile("^Dumping\\s+data\\s+for\\s+table\\s+`([^`]*)`.*$");
        
        private final static Pattern insertIntoVersion = Pattern.compile("^INSERT INTO `version` VALUES \\((?:([^\\),]*),)(?:([^\\),]*),)(?:([^\\),]*),)(?:([^\\),]*),)([^\\),]*)\\).*$");

        public void start(final int cid, final String filename) throws FileNotFoundException, IOException {
            final BufferedReader in = new BufferedReader(new FileReader(filename));
            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("/tmp/test.txt"));
            int c;
            int state = 0;
            int oldstate = 0;
            int cidpos = -1;
            String table_name = null;
            // Set if a database is found in which the search for cid should be done
            boolean furthersearch = true;
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
                            // Reset values
                            cidpos = -1;
                            state = 0;
                            oldstate = 0;
                        } else if (furthersearch && tablematcher.matches()) {
                            // Table found
                            table_name = tablematcher.group(1);
                            System.out.println("Table: " + table_name);
                            // TODO: If the version table is found, we must check the information in the version table against the current stored information
                            // for this scheme
                            cidpos = -1;
                            oldstate = 0;
                            state = 3;
                        } else if (furthersearch && datadumpmatcher.matches()) {
                            // Content found
                            System.out.println("Dump found");
                            if ("version".equals(table_name)) {
                                // The version table is quite small so it is safe to read the whole line here:
                                if (!searchAndCheckVersion(in)) {
                                    System.err.println("No version found. Fatal error");
                                    System.exit(1);
                                }
                            }
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
                    // Now we search for matching cids and write them to a tmp file
                    searchAndWriteMatchingCidValues(in, bufferedWriter, cidpos, cid, table_name);
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
        }

        private boolean searchAndCheckVersion(final BufferedReader in) throws IOException {
            String readLine2 = in.readLine();
            while ((readLine2 = in.readLine()) != null && !readLine2.equals("--")) {
                final Matcher matcher = insertIntoVersion.matcher(readLine2);
                if (matcher.matches()) {
                    final int version = Integer.parseInt(matcher.group(1));
                    final int locked = Integer.parseInt(matcher.group(2));
                    final int gw_compatible = Integer.parseInt(matcher.group(3));
                    final int admin_compatible = Integer.parseInt(matcher.group(4));
                    final String server = matcher.group(5);
                    
                    // Now check against the values in the db....
                    System.out.println("Version: " + version);
                    System.out.println("locked: " + locked);
                    System.out.println("gw_compatible: " + gw_compatible);
                    System.out.println("admin_compatible: " + admin_compatible);
                    System.out.println("Server: " + server);
                    
                    return true;
                }
            }
            return false;
        }
        
        private void searchAndWriteMatchingCidValues(final BufferedReader in, final Writer bufferedWriter, final int cidpos, final int cid, final String table_name) throws IOException {
            final StringBuilder currentValues = new StringBuilder();
            currentValues.append("(");
            final StringBuilder lastpart = new StringBuilder();
            int c = 0;
            int counter = 1;
            boolean instring = false;
            boolean indatarow = true;
            boolean found = false;
            boolean firstfound = true;
            while ((c = in.read()) != -1) {
                if (indatarow) {
                    if (c == ',') {
                        if (counter == cidpos) {
                            try {
                                final int parseInt = Integer.parseInt(lastpart.toString());
                                if (parseInt == cid) {
                                    System.out.println("Context found in values");
                                    found = true;
                                }
                            } catch (final NumberFormatException e) {
                                System.err.println("The part doesn't contain a number");
                            }
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
                        if (!firstfound) {
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
                    if (found) {
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

    public void restore(final Context ctx, final String[] filenames, final Credentials auth) throws InvalidDataException, InvalidCredentialsException, StorageException {
        try {
            doNullCheck(ctx, filenames);
        } catch (final InvalidDataException e) {
            LOG.error("One of the arguments for restore is null", e);
            throw e;
        }
        
        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }

        final Parser parser = new Parser();
        System.out.println("Context: " + ctx);
        System.out.println("Filenames: " + filenames);
        System.out.println("Creds:" + auth);
        
        try {
            parser.start(ctx.getId(), filenames[0]);
        } catch (final FileNotFoundException e) {
            LOG.error("File not found");
        } catch (final IOException e) {
            // TODO: Decide what to do with this exception
            LOG.error(e.getMessage(), e);
        }
    }

}
