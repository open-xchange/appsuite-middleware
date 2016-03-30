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

package com.openexchange.admin.console.context;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleCommonInterface;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleCreateInterface;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.console.user.UserAbstraction;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.Quota;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

public abstract class ContextAbstraction extends UserAbstraction {

    private interface ClosureInterface {

        public ArrayList<String> getData(final Context ctx);
    }

    protected static int CONTEXT_INITIAL_CONSTANTS_VALUE = Constants.values().length + AccessCombinations.values().length;

    public enum ContextConstants implements CSVConstants {
        contextname(OPT_NAME_CONTEXT_NAME_LONG, false),
        quota(OPT_QUOTA_LONG, true),
        lmapping(OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG, false),
        schema(SCHEMA_OPT, false),
        schemaStrategy(SCHEMA_STRATEGY_OPT, false),
        destination_store_id(OPT_CONTEXT_DESTINATION_STORE_ID_LONG, false),
        destination_database_id(OPT_CONTEXT_DESTINATION_DATABASE_ID_LONG, false),

        ;

        private final String string;
        private final int index;
        private boolean required;

        private ContextConstants(String string, boolean required) {
            this.index = CONTEXT_INITIAL_CONSTANTS_VALUE + ordinal();
            this.string = string;
            this.required = required;
        }

        @Override
        public String getString() {
            return string;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public void setRequired(final boolean required) {
            this.required = required;
        }

    }

    private final static char OPT_NAME_DATABASE_ID_SHORT = 'd';
    private final static String OPT_NAME_DATABASE_ID_LONG = "database";

    private final static char OPT_NAME_DBNAME_SHORT = 'n';
    private final static String OPT_NAME_DBNAME_LONG = "name";

    private static final String SCHEMA_OPT_DESC = "The schema name, where the context will be created. This bypasses any weight balancing. Must not be set, if \"schema-strategy\" is set.";
    private static final String SCHEMA_OPT = "schema";

    private static final String SCHEMA_STRATEGY_OPT_DESC = "The schema select strategy. \"automatic\" for automatic selection (default), \"in-memory\" for in memory selection. Must not be set, if \"schema\" option is set.";
    private static final String SCHEMA_STRATEGY_OPT = "schema-strategy";

    public final static char OPT_CONTEXT_ADD_LOGIN_MAPPINGS_SHORT = 'L';
    public final static String OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG = "addmapping";

    public final static char OPT_CONTEXT_DESTINATION_STORE_ID_SHORT = 'F';
    public final static String OPT_CONTEXT_DESTINATION_STORE_ID_LONG = "destination-store-id";

    public final static char OPT_CONTEXT_DESTINATION_DATABASE_ID_SHORT = 'D';
    public final static String OPT_CONTEXT_DESTINATION_DATABASE_ID_LONG = "destination-database-id";

    public final static char OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_SHORT = 'R';
    public final static String OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_LONG = "removemapping";
    static final char OPT_FILESTORE_SHORT = 'f';
    static final String OPT_FILESTORE_LONG = "filestore";

    private CLIOption databaseIdOption = null;
    private CLIOption databaseNameOption = null;
    private CLIOption schemaOption;
    private CLIOption schemaStrategyOption;

    protected Integer dbid = null;
    protected String dbname = null;
    protected String schema;
    protected String schemaStrategy;

    protected final String SCHEMA_NAME_AND_SCHEMA_STRATEGY_ERROR = "You can not specify \"schema\" and \"schema-strategy\" at the same time.";
    protected final String SCHEMA_NAME_ERROR = "Invalid value for \"schema\". Available values: \"automatic\", \"in-memory\"";

    protected Integer filestoreid = null;

    protected CLIOption targetFilestoreIDOption = null;

    private static final String OPT_NAME_CONTEXT_QUOTA_DESCRIPTION = "Context wide filestore quota in MB.";
    private final static char OPT_QUOTA_SHORT = 'q';

    private final static String OPT_QUOTA_LONG = "quota";
    protected static final String OPT_NAME_ADMINPASS_DESCRIPTION = "master Admin password";
    protected static final String OPT_NAME_ADMINUSER_DESCRIPTION = "master Admin user name";

    protected CLIOption contextQuotaOption = null;

    protected String contextname = null;

    private ServiceLoader<? extends ContextConsoleCommonInterface> subclasses = null;

    @Override
    protected String getObjectName() {
        return "context";
    }

    protected void applyExtensionValuesFromCSV(final String[] nextLine, final int[] idarray, final Context context) throws OXConsolePluginException {
        // We don't check for subclasses being null here because if someone has forgotten
        // to set the options he will directly fix it and thus there no need for the
        // future to check everytime
        for (final ContextConsoleCommonInterface ctxconsole : this.subclasses) {
            if (ctxconsole instanceof ContextConsoleCreateInterface) {
                ((ContextConsoleCreateInterface) ctxconsole).applyExtensionValuesFromCSV(nextLine, idarray, context);
            }
        }
    }

    protected void parseAndSetContextName(final AdminParser parser, final Context ctx) {
        this.contextname = (String) parser.getOptionValue(contextNameOption);
        if (this.contextname != null) {
            ctx.setName(this.contextname);
        }
    }

    protected void parseAndSetContextQuota(final AdminParser parser, final Context ctx) {
        final String contextQuota = (String) parser.getOptionValue(this.contextQuotaOption);
        if (null != contextQuota) {
            ctx.setMaxQuota(Long.parseLong(contextQuota));
        }
    }

    protected void parseAndSetExtensions(final AdminParser parser, final Context ctx, final Credentials auth) {
        // We don't check for subclasses being null here because if someone has forgotten
        // to set the options he will directly fix it and thus there no need for the
        // future to check everytime
        try {
            for (final ContextConsoleCommonInterface ctxconsole : this.subclasses) {
                ctxconsole.setAndFillExtension(parser, ctx, auth);
            }
        } catch (final OXConsolePluginException e) {
            printError(null, null, "Error while parsing extension options: " + e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        }
    }

    protected void extensionConstantProcessing(final HashMap<String, CSVConstants> constantsMap) {
        // We don't check for subclasses being null here because if someone has forgotten
        // to set the options he will directly fix it and thus there no need for the
        // future to check everytime
        for (final ContextConsoleCommonInterface ctxconsole : this.subclasses) {
            if (ctxconsole instanceof ContextConsoleCreateInterface) {
                ((ContextConsoleCreateInterface) ctxconsole).processCSVConstants(constantsMap);
            }
        }
    }

    @Override
    protected void setAdminPassOption(final AdminParser admp) {
        this.adminPassOption = setShortLongOpt(admp, OPT_NAME_ADMINPASS_SHORT, OPT_NAME_ADMINPASS_LONG, OPT_NAME_ADMINPASS_DESCRIPTION, true, NeededQuadState.possibly);
    }

    @Override
    protected void setAdminUserOption(final AdminParser admp) {
        this.adminUserOption = setShortLongOpt(admp, OPT_NAME_ADMINUSER_SHORT, OPT_NAME_ADMINUSER_LONG, OPT_NAME_ADMINUSER_DESCRIPTION, true, NeededQuadState.possibly);
    }

    protected void setExtensionOptions(final AdminParser parser, final Class<? extends ContextConsoleCommonInterface> clazz) {
        this.subclasses = ServiceLoader.load(clazz);

        try {
            for (final ContextConsoleCommonInterface ctxconsole : this.subclasses) {
                ctxconsole.addExtensionOptions(parser);
            }
        } catch (final OXConsolePluginException e) {
            printError(null, null, "Error while adding extension options: " + e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        }
    }

    protected void setContextQuotaOption(final AdminParser parser, final boolean required) {
        this.contextQuotaOption = setShortLongOpt(parser, OPT_QUOTA_SHORT, OPT_QUOTA_LONG, OPT_NAME_CONTEXT_QUOTA_DESCRIPTION, true, convertBooleantoTriState(required));
    }

    protected void sysoutOutput(final Context[] ctxs, final AdminParser parser) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Context ctx : ctxs) {
            data.add(makeData(ctx, new ClosureInterface() {

                @Override
                public ArrayList<String> getData(final Context ctx) {
                    return getHumanReableDataOfAllExtensions(ctx, parser);
                }
            }, false));
        }

        final ArrayList<String> humanReadableColumnsOfAllExtensions = getHumanReadableColumnsOfAllExtensions(parser);
        final ArrayList<String> alignment = new ArrayList<String>();
        alignment.add("r");
        alignment.add("r");
        alignment.add("l");
        alignment.add("l");
        alignment.add("r");
        alignment.add("r");
        alignment.add("l");
        alignment.add("l");
        for (int i = 0; i < humanReadableColumnsOfAllExtensions.size(); i++) {
            alignment.add("l");
        }
        final ArrayList<String> columnnames = new ArrayList<String>();
        columnnames.add("cid");
        columnnames.add("fid");
        columnnames.add("fname");
        columnnames.add("enabled");
        columnnames.add("qmax");
        columnnames.add("qused");
        columnnames.add("name");
        columnnames.add("lmappings");
        columnnames.addAll(humanReadableColumnsOfAllExtensions);

        doOutput(alignment.toArray(new String[alignment.size()]), columnnames.toArray(new String[columnnames.size()]), data);
    }

    protected void sysoutOutput(Quota[] quotas) throws InvalidDataException {
        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (Quota quota : quotas) {
            ArrayList<String> curData = new ArrayList<String>(2);

            {
                String module = quota.getModule();
                if (null != module) {
                    curData.add(module);
                } else {
                    curData.addAll(null);
                }
            }

            {
                long limit = quota.getLimit();
                curData.add(Long.toString(limit));
            }

            data.add(curData);
        }

        ArrayList<String> alignment = new ArrayList<String>();
        alignment.add("r");
        alignment.add("l");

        ArrayList<String> columnnames = new ArrayList<String>();
        columnnames.add("module");
        columnnames.add("qlimit");

        doOutput(alignment.toArray(new String[alignment.size()]), columnnames.toArray(new String[columnnames.size()]), data);
    }

    protected void precsvinfos(final Context[] ctxs, final AdminParser parser) throws InvalidDataException {
        // needed for csv output, KEEP AN EYE ON ORDER!!!
        final ArrayList<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("filestore_id");
        columns.add("filestore_name");
        columns.add("enabled");
        columns.add("max_quota");
        columns.add("used_quota");
        columns.add("name");
        columns.add("lmappings");
        columns.add("attributes");
        columns.addAll(getCSVColumnsOfAllExtensions(parser));
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

        for (final Context ctx_tmp : ctxs) {
            data.add(makeData(ctx_tmp, new ClosureInterface() {

                @Override
                public ArrayList<String> getData(final Context ctx) {
                    return getCSVDataOfAllExtensions(ctx_tmp, parser);
                }

            }, true));
        }

        doCSVOutput(columns, data);
    }

    protected void precsvinfos(Quota[] quotas) throws InvalidDataException {
        ArrayList<String> columns = new ArrayList<String>();
        columns.add("module");
        columns.add("limit");

        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (Quota quota : quotas) {
            ArrayList<String> curData = new ArrayList<String>(2);

            {
                String module = quota.getModule();
                if (null != module) {
                    curData.add(module);
                } else {
                    curData.addAll(null);
                }
            }

            {
                long limit = quota.getLimit();
                curData.add(Long.toString(limit));
            }

            data.add(curData);
        }

        doCSVOutput(columns, data);
    }

    protected void setDatabaseIDOption(final AdminParser parser) {
        this.databaseIdOption = setShortLongOpt(parser, OPT_NAME_DATABASE_ID_SHORT, OPT_NAME_DATABASE_ID_LONG, "The id of the database.", true, NeededQuadState.eitheror);
    }

    protected void setDatabaseNameOption(final AdminParser parser, final NeededQuadState required) {
        this.databaseNameOption = setShortLongOpt(parser, OPT_NAME_DBNAME_SHORT, OPT_NAME_DBNAME_LONG, "Name of the database", true, required);
    }

    protected void setSchemaOptions(AdminParser parser) {
        schemaOption = setLongOpt(parser, SCHEMA_OPT, SCHEMA_OPT_DESC, true, false);
        schemaStrategyOption = setLongOpt(parser, SCHEMA_STRATEGY_OPT, SCHEMA_STRATEGY_OPT_DESC, true, false);
    }

    protected final void displayDisabledMessage(final String id, final Integer ctxid, final AdminParser parser) {
        createMessageForStdout(id, ctxid, "disabled", parser);
    }

    protected final void displayEnabledMessage(final String id, final Integer ctxid, final AdminParser parser) {
        createMessageForStdout(id, ctxid, "enabled", parser);
    }

    protected final void displayDowngradedMessage(final String id, final Integer ctxid, final AdminParser parser) {
        createMessageForStdout(id, ctxid, "invisible data deleted", parser);
    }

    protected final void displayMovedMessage(final String id, final Integer ctxid, final String text, final AdminParser parser) {
        createMessageForStdout(id, ctxid, text, parser);
    }

    /**
     * The disable, enable and move* command line tools are extended from this class so we can override
     * this method in order to create proper error messages.
     */
    @Override
    protected void printFirstPartOfErrorText(final String id, final Integer ctxid, final AdminParser parser) {
        if (getClass().getName().matches("^.*\\.\\w*(?i)enable\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be enabled: ", parser);
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)disable\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be disabled: ", parser);
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)move\\wdatabase\\w*$")) {
            final StringBuilder sb = new StringBuilder(getObjectName());
            if (null != id) {
                sb.append(" ");
                sb.append(id);
            }
            if (null != ctxid) {
                sb.append(" to database ");
                sb.append(ctxid);
            }
            sb.append(" could not be scheduled: ");
            System.err.println(sb.toString());
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)move\\wfilestore\\w*$")) {
            final StringBuilder sb = new StringBuilder(getObjectName());
            if (null != id) {
                sb.append(" ");
                sb.append(id);
            }
            if (null != ctxid) {
                sb.append(" to filestore ");
                sb.append(ctxid);
            }
            sb.append(" could not be scheduled: ");
            System.err.println(sb.toString());
        } else {
            super.printFirstPartOfErrorText(id, ctxid, parser);
        }
    }

    protected void parseAndSetDatabaseID(final AdminParser parser, final Database db) {
        final String optionvalue = (String) parser.getOptionValue(this.databaseIdOption);
        if (null != optionvalue) {
            dbid = Integer.parseInt(optionvalue);
            db.setId(dbid);
        }
    }

    protected void parseAndSetDatabasename(final AdminParser parser, final Database db) {
        dbname = (String) parser.getOptionValue(this.databaseNameOption);
        if (null != dbname) {
            db.setName(dbname);
        }
    }

    protected void parseAndSetSchemaOptions(AdminParser parser) {
        schema = (String) parser.getOptionValue(schemaOption);
        schemaStrategy = (String) parser.getOptionValue(schemaStrategyOption);
    }

    protected void setFilestoreIdOption(final AdminParser parser) {
        this.targetFilestoreIDOption = setShortLongOpt(parser, OPT_FILESTORE_SHORT, OPT_FILESTORE_LONG, "Target filestore id", true, NeededQuadState.needed);
    }

    protected Filestore parseAndSetFilestoreId(final AdminParser parser) {
        filestoreid = Integer.parseInt((String) parser.getOptionValue(this.targetFilestoreIDOption));
        final Filestore fs = new Filestore(filestoreid);
        return fs;
    }

    @Override
    protected Context getContext(final String[] nextLine, final int[] idarray) throws InvalidDataException, ParseException {
        final Context context = super.getContext(nextLine, idarray);
        setValue(nextLine, idarray, ContextConstants.contextname, new MethodStringClosure() {

            @Override
            public void callMethod(String value) throws ParseException, InvalidDataException {
                context.setName(value);
            }
        });
        setValue(nextLine, idarray, ContextConstants.lmapping, new MethodStringClosure() {

            @Override
            public void callMethod(String value) throws ParseException, InvalidDataException {
                context.addLoginMapping(value);
            }
        });
        setValue(nextLine, idarray, ContextConstants.quota, new MethodStringClosure() {

            @Override
            public void callMethod(String value) throws ParseException, InvalidDataException {
                try {
                    context.setMaxQuota(Long.valueOf(value));
                } catch (final NumberFormatException e) {
                    throw new InvalidDataException("Value in field " + ContextConstants.quota.getString() + " is no integer");
                }
            }
        });
        setValue(nextLine, idarray, ContextConstants.destination_database_id, new MethodStringClosure() {

            @Override
            public void callMethod(String value) throws ParseException, InvalidDataException {
                try {
                    context.setWriteDatabase(new Database(Integer.parseInt(value)));
                } catch (final NumberFormatException e) {
                    throw new InvalidDataException("Value in field " + ContextConstants.destination_database_id.getString() + " is no integer");
                }
            }
        });
        setValue(nextLine, idarray, ContextConstants.destination_store_id, new MethodStringClosure() {

            @Override
            public void callMethod(String value) throws ParseException, InvalidDataException {
                try {
                    context.setFilestoreId(Integer.valueOf(value));
                } catch (final NumberFormatException e) {
                    throw new InvalidDataException("Value in field " + ContextConstants.destination_store_id.getString() + " is no integer");
                }
            }
        });

        return context;
    }

    protected SchemaSelectStrategy getSchemaSelectStrategy(String[] nextLine, int[] idArray) throws InvalidDataException {
        int schemaId = idArray[ContextConstants.schema.getIndex()];
        int strategyId = idArray[ContextConstants.schemaStrategy.getIndex()];

        if (schemaId != -1 && strategyId != -1) {
            if (nextLine[schemaId].length() > 0 && nextLine[strategyId].length() > 0) {
                throw new InvalidDataException(SCHEMA_NAME_AND_SCHEMA_STRATEGY_ERROR);
            }
        }

        if (schemaId != -1 && nextLine[schemaId].length() > 0) {
            return SchemaSelectStrategy.schema(nextLine[schemaId]);
        }

        if (strategyId != -1 && nextLine[strategyId].length() > 0) {
            String strategyName = nextLine[strategyId];
            if (strategyName.equals("automatic")) {
                return SchemaSelectStrategy.automatic();
            } else if (strategyName.equals("in-memory")) {
                return SchemaSelectStrategy.inMemory();
            } else {
                throw new InvalidDataException(SCHEMA_NAME_ERROR);
            }
        }

        return SchemaSelectStrategy.getDefault();
    }

    /**
     * @return the filestoreid
     */
    public final Integer getFilestoreid() {
        return filestoreid;
    }

    protected ArrayList<String> getHumanReadableColumnsOfAllExtensions(final AdminParser parser) {
        return new ArrayList<String>();
    }

    protected ArrayList<String> getHumanReableDataOfAllExtensions(final Context ctx, final AdminParser parser) {
        return new ArrayList<String>();
    }

    protected Collection<? extends String> getCSVColumnsOfAllExtensions(final AdminParser parser) {
        return new ArrayList<String>();
    }

    protected ArrayList<String> getCSVDataOfAllExtensions(final Context ctx_tmp, final AdminParser parser) {
        return new ArrayList<String>();
    }

    private ArrayList<String> makeData(final Context ctx, final ClosureInterface iface, final boolean csv) {
        final ArrayList<String> srv_data = new ArrayList<String>();
        srv_data.add(String.valueOf(ctx.getId()));

        final Integer filestoreId = ctx.getFilestoreId();
        if (filestoreId != null) {
            srv_data.add(String.valueOf(filestoreId));
        } else {
            srv_data.add(null);
        }

        final String filestore_name = ctx.getFilestore_name();
        if (filestore_name != null) {
            srv_data.add(filestore_name);
        } else {
            srv_data.add(null);
        }

        final Boolean enabled = ctx.isEnabled();
        if (enabled != null) {
            srv_data.add(String.valueOf(enabled));
        } else {
            srv_data.add(null);
        }

        final Long maxQuota = ctx.getMaxQuota();
        if (maxQuota != null) {
            srv_data.add(String.valueOf(maxQuota));
        } else {
            srv_data.add(null);
        }

        final Long usedQuota = ctx.getUsedQuota();
        if (usedQuota != null) {
            srv_data.add(String.valueOf(usedQuota));
        } else {
            srv_data.add(null);
        }

        final String name = ctx.getName();
        if (name != null) {
            srv_data.add(name);
        } else {
            srv_data.add(null);
        }

        // loginl mappings

        final HashSet<String> loginMappings = ctx.getLoginMappings();
        if (loginMappings != null && loginMappings.size() > 0) {
            srv_data.add(getObjectsAsString(loginMappings.toArray()));
        } else {
            srv_data.add(null);
        }

        if (csv) {
            final StringBuilder attrs = new StringBuilder();
            final Map<String, Map<String, String>> attributes = ctx.getUserAttributes();
            for (final Map.Entry<String, Map<String, String>> entry : attributes.entrySet()) {
                final String namespace = entry.getKey();
                for (final Map.Entry<String, String> attribute : entry.getValue().entrySet()) {
                    attrs.append(namespace);
                    attrs.append('/');
                    attrs.append(attribute.getKey());
                    attrs.append('=');
                    attrs.append(attribute.getValue());
                    attrs.append(',');
                }
            }
            if (attrs.length() != 0) {
                attrs.setLength(attrs.length() - 1);
            }
            srv_data.add(attrs.toString());
        }

        srv_data.addAll(iface.getData(ctx));

        return srv_data;
    }

    protected void applyDynamicOptionsToContext(final AdminParser parser, final Context ctx) {
        final Map<String, Map<String, String>> dynamicArguments = parser.getDynamicArguments();
        for (final Map.Entry<String, Map<String, String>> namespaced : dynamicArguments.entrySet()) {
            final String namespace = namespaced.getKey();
            for (final Map.Entry<String, String> pair : namespaced.getValue().entrySet()) {
                final String name = pair.getKey();
                final String value = pair.getValue();

                ctx.setUserAttribute(namespace, name, value);
            }
        }
    }

}
