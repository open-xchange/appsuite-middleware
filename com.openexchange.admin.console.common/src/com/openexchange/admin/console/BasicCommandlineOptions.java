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

package com.openexchange.admin.console;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.ExtendableDataObject;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;

/**
 * This abstract class contains all the common options between all command line tools
 * @author cutmasta,d7
 *
 */
public abstract class BasicCommandlineOptions {

    private static final char dividechar = ' ';

    /**
     * Used when username/password credentials were not correct!
     */
    public static final int SYSEXIT_INVALID_CREDENTIALS=101;

    /**
     * Used when the requested context does not exists on the server!
     */
    public static final int SYSEXIT_NO_SUCH_CONTEXT=102;

    /**
     * Used when wrong data was sent to the server!
     */
    public static final int SYSEXIT_INVALID_DATA=103;

    /**
     * Used when an option is missing to execute the cmd tool!
     */
    public static final int SYSEXIT_MISSING_OPTION=104;

    /**
     * Used when a communication problem was encountered
     */
    public static final int SYSEXIT_COMMUNICATION_ERROR =105;

    /**
     * Used when a storage problem was encountered on the server!
     */
    public static final int SYSEXIT_SERVERSTORAGE_ERROR =106;

    /**
     * Used when a remote server problem was encountered !
     */
    public static final int SYSEXIT_REMOTE_ERROR =107;

    /**
     * Used when an user does not exists
     */
    public static final int SYSEXIT_NO_SUCH_USER =108;

    /**
     * Used when an unknown option was passed to the cmd tool!
     */
    public static final int SYSEXIT_ILLEGAL_OPTION_VALUE=109;

    /**
     * Used when a context already exists
     */
    public static final int SYSEXIT_CONTEXT_ALREADY_EXISTS=110;

    /**
     * Used when an unknown option was passed to the cmd tool!
     */
    public static final int SYSEXIT_UNKNOWN_OPTION=111;

    /**
     * Used when a group does not exists
     */
    public static final int SYSEXIT_NO_SUCH_GROUP =112;

    /**
     * Used when a resource does not exists
     */
    public static final int SYSEXIT_NO_SUCH_RESOURCE =113;

    public static final int SYSEXIT_UNABLE_TO_PARSE = 114;

    /**
     * Used when a publication does not exists
     */
    public static final int SYSEXIT_NO_SUCH_PUBLICATION = 115;

    protected static final int DEFAULT_CONTEXT=1;
    protected static final char OPT_NAME_CONTEXT_SHORT='c';
    protected static final String OPT_NAME_CONTEXT_LONG="contextid";
    protected static final char OPT_NAME_CONTEXT_NAME_SHORT='N';
    protected static final String OPT_NAME_CONTEXT_NAME_LONG="contextname";
    protected static final String OPT_NAME_CONTEXT_NAME_DESCRIPTION="context name";
    protected static final String OPT_NAME_CONTEXT_DESCRIPTION="The id of the context";
    protected static final char OPT_NAME_ADMINUSER_SHORT='A';
    protected static final String OPT_NAME_ADMINUSER_LONG="adminuser";
    protected static final char OPT_NAME_ADMINPASS_SHORT='P';
    protected static final String OPT_NAME_ADMINPASS_LONG="adminpass";
    protected static final String OPT_NAME_ADMINPASS_DESCRIPTION="Admin password";
    protected static final String OPT_NAME_ADMINUSER_DESCRIPTION="Admin username";
    protected static final String OPT_NAME_SEARCHPATTERN_LONG = "searchpattern";
    protected static final char OPT_NAME_SEARCHPATTERN = 's';
    protected static final String OPT_NAME_IGNORECASE_LONG = "ignorecase";
    protected static final char OPT_NAME_IGNORECASE = 'i';

    protected static final String OPT_NAME_CSVOUTPUT_LONG = "csv";
    protected static final String OPT_NAME_CSVOUTPUT_DESCRIPTION = "Format output to csv";

    protected static final String OPT_NAME_INCLUDEGUESTS_LONG = "includeguests";
    protected static final String OPT_NAME_INCLUDEGUESTS_DESCRIPTION = "Include guest users";

    protected static final String OPT_NAME_EXCLUDEUSERS_LONG = "excludeusers";
    protected static final String OPT_NAME_EXCLUDEUSERS_DESCRIPTION = "Exclude users, only show guests";

    private static final String[] ENV_OPTIONS = new String[] {
        "RMI_HOSTNAME", "COMMANDLINE_TIMEZONE", "COMMANDLINE_DATEFORMAT", "ADMIN_PASSWORD", "NEW_USER_PASSWORD" };

    protected static String RMI_HOSTNAME           = "rmi://localhost:1099/";
    protected static String COMMANDLINE_TIMEZONE   = "GMT";
    protected static String COMMANDLINE_DATEFORMAT = "yyyy-MM-dd";
    protected static String ADMIN_PASSWORD         = null;
    protected static String NEW_USER_PASSWORD      = null;

    protected CLIOption contextOption = null;
    protected CLIOption contextNameOption = null;
    protected CLIOption adminUserOption = null;
    protected CLIOption adminPassOption = null;
    protected CLIOption searchOption = null;
    protected CLIOption ignoreCaseOption = null;
    protected CLIOption csvOutputOption = null;
    protected CLIOption includeGuestsOption = null;
    protected CLIOption excludeUsersOption = null;

    // Used for right error output
    protected Integer ctxid = null;

    public BasicCommandlineOptions() {
        super();
        for( final String opt : ENV_OPTIONS ) {
            setEnvConfigOption(opt);
        }
    }

    public static final Hashtable<String,String> getEnvOptions() {
        final Hashtable<String, String> opts = new Hashtable<String, String>();
        for( final String opt : ENV_OPTIONS ) {
            try {
                final Field f = BasicCommandlineOptions.class.getDeclaredField(opt);
                String val = (String)f.get(null);
                // to be able to print also opts set to null, override with empty string
                if( val == null ) {
                    val = "<NOT SET>";
                }
                opts.put(opt, val);
            } catch (final SecurityException e) {
                System.err.println("unable to get commandline option \""+opt+"\"");
                e.printStackTrace();
            } catch (final NoSuchFieldException e) {
                System.err.println("unable to get commandline option \""+opt+"\"");
                e.printStackTrace();
            } catch (final IllegalArgumentException e) {
                System.err.println("unable to get commandline option \""+opt+"\"");
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                System.err.println("unable to get commandline option \""+opt+"\"");
                e.printStackTrace();
            }
        }
        return opts;
    }

    private final void setEnvConfigOption(final String opt) {
        final String property = System.getProperties().getProperty(opt);
        final String env = System.getenv(opt);
        String setOpt = null;
        if (null != env && env.trim().length()>0) {
            setOpt = env;
        } else if (null != property && property.trim().length()>0) {
            setOpt = property;
        }
        if( setOpt != null ) {
            if( opt.equals("RMI_HOSTNAME") ) {
                setRMI_HOSTNAME(setOpt);
            } else {
                try {
                    final Field f = BasicCommandlineOptions.class.getDeclaredField(opt);
                    f.set(this, setOpt);
                } catch (final SecurityException e) {
                    System.err.println("unable to set commandline option for \""+opt+"\" to \"" + setOpt+ "\"");
                    e.printStackTrace();
                } catch (final NoSuchFieldException e) {
                    System.err.println("unable to set commandline option for \""+opt+"\" to \"" + setOpt+ "\"");
                    e.printStackTrace();
                } catch (final IllegalArgumentException e) {
                    System.err.println("unable to set commandline option for \""+opt+"\" to \"" + setOpt+ "\"");
                    e.printStackTrace();
                } catch (final IllegalAccessException e) {
                    System.err.println("unable to set commandline option for \""+opt+"\" to \"" + setOpt+ "\"");
                    e.printStackTrace();
                }
            }
        }
    }

    protected final void printServerException(final Exception e, final AdminParser parser){
        String output = "";
        final String msg = e.getMessage();
        if( parser != null && parser.checkNoNewLine() ) {
            if( msg != null ) {
                output = "Server response: " + msg.replace("\n", "");
            } else {
                output += e.toString();
            }
            for(final StackTraceElement ste : e.getStackTrace() ) {
                output += ": " + ste.toString().replace("\n", "");
            }
        } else {
            if( msg != null ) {
                output = "Server response:\n "+msg+'\n';
            } else {
                output += e.toString() + "\n";
            }
            for (final StackTraceElement ste : e.getStackTrace()) {
                output += "\tat " + ste.toString() + "\n";
            }
        }
        System.err.println(output);
    }

    //    protected final void printNotBoundResponse(final NotBoundException nbe){
    //        System.err.println("RMI module "+nbe.getMessage()+" not available on server");
    //    }

    protected final void printError(final String msg, final AdminParser parser){
        String output = null;
        if( parser == null ) {
            output = msg;
        } else {
            if (parser.checkNoNewLine()) {
                output =  msg.replace("\n", "");
            } else {
                output = msg;
            }
        }
        System.err.println("Error: " + output);
    }

    protected final void printServerResponse(final String msg){
        System.err.println("Server response:\n "+msg+"\n");
    }

    protected final void printInvalidInputMsg(final String msg){
        System.err.println("Invalid input detected: "+msg);
    }

    /**
     * Prints out the given data as csv output.
     * The first ArrayList contains the columns which describe the following data lines.<br><br>
     *
     * Example output:<br><br>
     * username,email,mycolumn<br>
     * testuser,test@test.org,mycolumndata<br>
     *
     * @param columns
     * @param data
     * @throws InvalidDataException
     */
    protected final void doCSVOutput(final List<String> columns, final ArrayList<ArrayList<String>> data) throws InvalidDataException {
        // first prepare the columns line
        StringBuilder sb = new StringBuilder();
        for (final String column_entry : columns) {
            sb.append(column_entry);
            sb.append(",");
        }
        if(sb.length()>0) {
            // remove last ","
            sb.deleteCharAt(sb.length()-1);
        }

        // print the columns line
        System.out.println(sb.toString());
        if (data != null && !data.isEmpty()) {
            if (columns.size()!=data.get(0).size()) {
                throw new InvalidDataException("Number of columnnames and number of columns in data object must be the same");
            }

            // now prepare all data lines
            for (final ArrayList<String> data_list : data) {
                sb = new StringBuilder();
                for (final String data_column : data_list) {
                    if(data_column!=null){
                        sb.append("\"");
                        sb.append(data_column.replaceAll("\"", "\"\""));
                        sb.append("\"");
                    }
                    sb.append(",");
                }
                if(sb.length()>0){
                    // remove trailing ","
                    sb.deleteCharAt(sb.length()-1);
                }
                // print out data line with linessbreak
                System.out.println(sb.toString());
            }
        }
    }

    protected final void setRMI_HOSTNAME(final String rmi_hostname) {
        String host = rmi_hostname;
        if(!host.startsWith("rmi://")){
            host = "rmi://"+host;
        }
        if(!host.endsWith("/")){
            host = host+"/";
        }
        RMI_HOSTNAME = host;

    }

    protected final CLIOption setLongOpt(final AdminParser admp, final String longopt, final String description, final boolean hasarg, final boolean required) {

        final CLIOption retval = admp.addOption(longopt, longopt, description, required,hasarg);
        //        //OptionBuilder.withLongOpt( longopt ).withDescription( description ).withValueSeparator( '=' ).create();
        //        if (hasarg) {
        //            retval.hasArg();
        //        }
        //        retval.setRequired(required);
        return retval;
    }

    protected final CLIOption setLongOpt(final AdminParser admp, final String longopt, final String description, final boolean hasarg, final boolean required, final boolean extended) {

        final CLIOption retval = admp.addOption(longopt, longopt, description, required, hasarg, extended);
        return retval;
    }

    protected final CLIOption setLongOpt(final AdminParser admp, final String longopt, final String argdescription, final String description, final boolean hasarg, final boolean required, final boolean extended) {

        final CLIOption retval = admp.addOption(longopt, argdescription, description, required, hasarg, extended);
        return retval;
    }

    protected final CLIOption setIntegerLongOpt(final AdminParser admp, final String longopt, final String argdescription, final String description, final boolean hasarg, final boolean required, final boolean extended) {

        final CLIOption retval = admp.addIntegerOption(longopt, argdescription, description, required, hasarg, extended);
        return retval;
    }

    protected final CLIOption setSettableBooleanLongOpt(final AdminParser admp, final String longopt, final String argdescription, final String description, final boolean hasarg, final boolean required, final boolean extended) {

        final CLIOption retval = admp.addSettableBooleanOption(longopt, argdescription, description, required, hasarg, extended);
        return retval;
    }

    protected final CLIOption setShortLongOpt(final AdminParser admp,final char shortopt, final String longopt, final String argdescription, final String description, final boolean required) {
        final CLIOption retval = admp.addOption(shortopt, longopt, argdescription, description, required);
        return retval;
    }

    protected final CLIOption setShortLongOpt(final AdminParser admp,final char shortopt, final String longopt, final String description, final boolean hasarg, final NeededQuadState required) {
        final CLIOption retval = admp.addOption(shortopt,longopt, longopt, description, required, hasarg);
        return retval;
    }

    protected final CLIOption setShortLongOptWithDefault(final AdminParser admp,final char shortopt, final String longopt, final String description, final String defaultvalue, final boolean hasarg, final NeededQuadState required) {
        final StringBuilder desc = new StringBuilder();
        desc.append(description);
        desc.append(". Default: ");
        desc.append(defaultvalue);

        return setShortLongOpt(admp, shortopt, longopt, desc.toString(), hasarg, required);
    }

    protected final CLIOption setShortLongOptWithDefault(final AdminParser admp,final char shortopt, final String longopt, final String argdescription, final String description, final String defaultvalue, final boolean required) {
        final StringBuilder desc = new StringBuilder();
        desc.append(description);
        desc.append(". Default: ");
        desc.append(defaultvalue);

        return setShortLongOpt(admp, shortopt, longopt, argdescription, desc.toString(), required);
    }

    protected final void setContextOption(final AdminParser admp, final NeededQuadState needed) {
        this.contextOption = setShortLongOpt(admp,OPT_NAME_CONTEXT_SHORT, OPT_NAME_CONTEXT_LONG, OPT_NAME_CONTEXT_DESCRIPTION, true, needed);
    }

    protected final void setContextNameOption(final AdminParser admp, final NeededQuadState needed) {
        this.contextNameOption = setShortLongOpt(admp,OPT_NAME_CONTEXT_NAME_SHORT, OPT_NAME_CONTEXT_NAME_LONG, OPT_NAME_CONTEXT_NAME_DESCRIPTION, true, needed);
    }

    // ------------------------------------------------------------------------------------- //

    protected void setAdminPassOption(final AdminParser admp) {
        setAdminPassOption(admp, OPT_NAME_ADMINPASS_LONG, OPT_NAME_ADMINPASS_DESCRIPTION);
    }

    protected void setAdminPassOption(final AdminParser admp, final String nameAdminPassLong, final String description) {
        this.adminPassOption = setShortLongOpt(admp,OPT_NAME_ADMINPASS_SHORT, nameAdminPassLong, description, true, NeededQuadState.possibly);
    }

    // ------------------------------------------------------------------------------------- //

    protected final void setCSVOutputOption(final AdminParser admp) {
        this.csvOutputOption = setLongOpt(admp, OPT_NAME_CSVOUTPUT_LONG, OPT_NAME_CSVOUTPUT_DESCRIPTION, false, false);
    }

    protected final void setIncludeGuestsOption(final AdminParser admp) {
        this.includeGuestsOption = setLongOpt(admp, OPT_NAME_INCLUDEGUESTS_LONG, OPT_NAME_INCLUDEGUESTS_DESCRIPTION, false, false);
    }

    protected final void setExcludeUsersOption(final AdminParser admp) {
        this.excludeUsersOption = setLongOpt(admp, OPT_NAME_EXCLUDEUSERS_LONG, OPT_NAME_EXCLUDEUSERS_DESCRIPTION, false, false);
    }

    // ------------------------------------------------------------------------------------- //

    protected void setAdminUserOption(final AdminParser admp) {
        setAdminUserOption(admp, OPT_NAME_ADMINUSER_LONG, OPT_NAME_ADMINUSER_DESCRIPTION);
    }

    protected void setAdminUserOption(final AdminParser admp, final String nameAdminUserLong, final String description) {
        this.adminUserOption= setShortLongOpt(admp,OPT_NAME_ADMINUSER_SHORT, nameAdminUserLong, description, true, NeededQuadState.possibly);
    }

    // ------------------------------------------------------------------------------------- //

    protected final void setSearchPatternOption(final AdminParser admp){
        this.searchOption = setShortLongOpt(admp,OPT_NAME_SEARCHPATTERN, OPT_NAME_SEARCHPATTERN_LONG, "The search pattern which is used for listing", true, NeededQuadState.notneeded);
    }

    protected final void setIgnoreCaseOption(final AdminParser admp){
        this.ignoreCaseOption = setShortLongOpt(admp,OPT_NAME_IGNORECASE, OPT_NAME_IGNORECASE_LONG, "Whether to perform look-up case-insensitive", false, NeededQuadState.notneeded);
    }

    protected final int testStringAndGetIntOrDefault(final String test, final int defaultvalue) throws NumberFormatException {
        final int retval;
        if (null != test) {
            retval = Integer.parseInt(test);
        } else {
            retval = defaultvalue;
        }
        return retval;
    }

    protected final String testStringAndGetStringOrDefault(final String test, final String defaultvalue) {
        final String retval;
        if (null != test) {
            retval = test;
        } else {
            retval = defaultvalue;
        }
        return retval;
    }

    protected final boolean testStringAndGetBooleanOrDefault(final String test, final boolean defaultvalue) {
        final boolean retval;
        if (null != test) {
            retval = Boolean.parseBoolean(test);
        } else {
            retval = defaultvalue;
        }
        return retval;
    }

    /**
     *
     */
    protected void setDefaultCommandLineOptions(final AdminParser admp){
        setContextOption(admp, NeededQuadState.needed);
        setAdminUserOption(admp);
        setAdminPassOption(admp);
    }

    protected void setDefaultCommandLineOptionsWithoutContextID(final AdminParser parser) {
        setAdminUserOption(parser);
        setAdminPassOption(parser);
    }

    protected void sysexit(final int exitcode) {
        // see http://java.sun.com/j2se/1.5.0/docs/guide/rmi/faq.html#leases
        System.gc();
        System.runFinalization();
        System.exit(exitcode);
    }

    protected final Context contextparsing(final AdminParser parser) {
        final Context ctx = new Context();

        if (parser.getOptionValue(this.contextOption) != null) {
            ctxid = Integer.valueOf((String) parser.getOptionValue(this.contextOption));
            ctx.setId(ctxid);
        }
        return ctx;
    }

    protected final Credentials credentialsparsing(final AdminParser parser) {
        // prefer password from options
        String password = (String) parser.getOptionValue(this.adminPassOption);
        if (null == password && null != ADMIN_PASSWORD) {
            password = ADMIN_PASSWORD;
        }
        return new Credentials((String) parser.getOptionValue(this.adminUserOption), password);
    }

    /**
     * Strips a string to the given size and adds the given lastmark to it to signal that the string is longer
     * than specified
     *
     * @param test
     * @param length
     * @return
     */
    private String stripString(final String text, final int length, final String lastmark) {
        if (null != text && text.length() > length) {
            final int stringlength = length - lastmark.length();
            return new StringBuffer(text.substring(0, stringlength)).append(lastmark).toString();
        } else if (text == null) {
            return "";
        } else {
            return text;
        }
    }

    /**
     * This method takes an array of objects and format them in one comma-separated string
     *
     * @param objects
     * @return
     */
    protected String getObjectsAsString(final Object[] objects) {
        final StringBuilder sb = new StringBuilder();
        if (objects != null && objects.length > 0) {
            for (final Object id : objects) {
                sb.append(id);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return "";
    }

    private final int longestLine(final ArrayList<ArrayList<String>> data, final String[] columnnames, final int column) throws InvalidDataException {
        //long start = System.currentTimeMillis();
        int max = columnnames[column].length();
        for(int row=0; row<data.size(); row++) {
            final ArrayList<String> arrayList = data.get(row);
            if (columnnames.length != arrayList.size()) {
                throw new InvalidDataException("The sizes of columnnames and the columns in line " + row + " of the data aren't equal");
            }
            final String value = arrayList.get(column);
            if( value != null ) {
                final int curLength = arrayList.get(column).length();
                if( curLength > max ) {
                    max = curLength;
                }
            }
        }
        //System.out.println("calc took " + (System.currentTimeMillis()-start) + "ms");
        return max;
    }

    protected void doOutput(final String[] columnsizesandalignments, final String[] columnnames, final ArrayList<ArrayList<String>> data) throws InvalidDataException {
        if (columnsizesandalignments.length != columnnames.length) {
            throw new InvalidDataException("The sizes of columnsizes and columnnames aren't equal");
        }
        final int[] columnsizes = new int[columnsizesandalignments.length];
        final char[] alignments = new char[columnsizesandalignments.length];
        final StringBuilder formatsb = new StringBuilder();
        for (int i = 0; i < columnsizesandalignments.length; i++) {
            // fill up part
            try {
                columnsizes[i] = Integer.parseInt(columnsizesandalignments[i].substring(0, columnsizesandalignments[i].length() - 1));
            } catch (final NumberFormatException e) {
                // there's no number, so use longest line as alignment value
                columnsizes[i] = longestLine(data,columnnames,i);
            }
            alignments[i] = columnsizesandalignments[i].charAt(columnsizesandalignments[i].length() - 1);

            // check part
            if (columnnames[i].length() > columnsizes[i]) {
                throw new InvalidDataException("Columnsize for column " + columnnames[i] + " is too small for columnname");
            }

            // formatting part
            formatsb.append("%");
            if (alignments[i] == 'l') {
                formatsb.append('-');
            }
            formatsb.append(columnsizes[i]);
            formatsb.append('s');
            formatsb.append(dividechar);
        }
        formatsb.deleteCharAt(formatsb.length() - 1);
        formatsb.append('\n');
        System.out.format(formatsb.toString(), (Object[]) columnnames);
        for (final ArrayList<String> row : data) {
            if (row.size() != columnsizesandalignments.length) {
                throw new InvalidDataException("The size of one of the rows isn't correct");
            }
            final Object[] outputrow = new Object[columnsizesandalignments.length];
            for (int i = 0; i < columnsizesandalignments.length; i++) {
                final String value = row.get(i);
                outputrow[i] = stripString(value, columnsizes[i], "~");
            }
            System.out.format(formatsb.toString(), outputrow);
        }
    }

    protected final void printExtensionsError(final ExtendableDataObject obj) {
        //+ loop through extensions and check for errors
        if (obj != null && obj.getAllExtensionsAsHash() != null) {
            for (final OXCommonExtension obj_extension : obj.getAllExtensionsAsHash().values()) {
                if (obj_extension.getExtensionError() != null) {
                    printServerResponse(obj_extension.getExtensionError());
                }
            }
        }
    }

    protected final NeededQuadState convertBooleantoTriState(final boolean needed) {
        return needed ? NeededQuadState.needed : NeededQuadState.notneeded;
    }

    // We have to serve this 2nd method here because String.valueOf return "null" as String
    // instead of null from an Integer null object. So we have to deal with this situation
    // ourself
    protected String nameOrIdSetInt(final Integer id, final String name, final String objectname) throws MissingOptionException {
        if (null == id) {
            return nameOrIdSet(null, name, objectname);
        }
        return nameOrIdSet(String.valueOf(id), name, objectname);
    }

    protected String nameOrIdSet(final String id, final String name, final String objectname) throws MissingOptionException {
        String successtext;
        // Through the order of this checks we archive that the id is preferred over the name
        if (null == id) {
            if (null == name) {
                throw new MissingOptionException("Either " + objectname + "name or " + objectname + "id must be given");
            }
            successtext = name;
        } else {
            successtext = String.valueOf(id);
        }
        return successtext;
    }
}
