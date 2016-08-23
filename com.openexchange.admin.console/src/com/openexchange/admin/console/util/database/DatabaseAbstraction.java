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

package com.openexchange.admin.console.util.database;

import java.net.URI;
import java.net.URISyntaxException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.util.UtilAbstraction;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 * This is an abstract class for all common attributes and methods of database related command line tools
 *
 * @author d7
 */
public abstract class DatabaseAbstraction extends UtilAbstraction {

    protected static final char OPT_NAME_DATABASE_ID_SHORT = 'i';

    protected static final String OPT_NAME_DATABASE_ID_LONG = "id";

    protected final static char OPT_NAME_DB_USERNAME_SHORT = 'u';

    protected final static String OPT_NAME_DB_USERNAME_LONG = "dbuser";

    protected final static char OPT_NAME_DBNAME_SHORT = 'n';

    protected final static String OPT_NAME_DBNAME_LONG = "name";

    protected final static char OPT_NAME_DB_PASSWD_SHORT = 'p';

    protected final static String OPT_NAME_DB_PASSWD_LONG = "dbpasswd";

    protected final static char OPT_NAME_POOL_HARDLIMIT_SHORT = 'l';

    protected final static String OPT_NAME_POOL_HARDLIMIT_LONG = "poolhardlimit";

    protected final static char OPT_NAME_POOL_INITIAL_SHORT = 'o';

    protected final static String OPT_NAME_POOL_INITIAL_LONG = "poolinitial";

    protected final static char OPT_NAME_POOL_MAX_SHORT = 'a';

    protected final static String OPT_NAME_POOL_MAX_LONG = "poolmax";

    protected final static char OPT_NAME_DB_DRIVER_SHORT = 'd';

    protected final static String OPT_NAME_DB_DRIVER_LONG = "dbdriver";

    protected final static char OPT_NAME_MASTER_ID_SHORT = 'M';

    protected final static String OPT_NAME_MASTER_ID_LONG = "masterid";

    protected final static char OPT_NAME_WEIGHT_SHORT = 'w';

    protected final static String OPT_NAME_WEIGHT_LONG = "dbweight";

    protected final static char OPT_NAME_MAX_UNITS_SHORT = 'x';

    protected final static String OPT_NAME_MAX_UNITS_LONG = "maxunit";

    protected final static char OPT_NAME_HOSTNAME_SHORT = 'H';

    protected final static String OPT_NAME_HOSTNAME_LONG = "hostname";

    protected final static char OPT_NAME_IS_MASTER_SHORT = 'm';

    protected final static String OPT_NAME_IS_MASTER_LONG = "master";

    protected final static String OPT_NAME_SCHEMA_LONG = "schema";

    protected CLIOption databaseIdOption = null;

    protected CLIOption databaseUsernameOption = null;

    protected CLIOption databaseDriverOption = null;

    protected CLIOption databasePasswdOption = null;

    protected CLIOption databaseIsMasterOption = null;

    protected CLIOption databaseMasterIDOption = null;

    protected CLIOption databaseWeightOption = null;

    protected CLIOption databaseNameOption = null;

    protected CLIOption hostnameOption = null;

    protected CLIOption maxUnitsOption = null;

    protected CLIOption poolHardlimitOption = null;

    protected CLIOption poolInitialOption = null;

    protected CLIOption poolMaxOption = null;

    protected CLIOption schemaOption = null;

    // Needed for right error output
    protected String dbid = null;
    protected String dbname = null;

    protected void parseAndSetDatabaseID(final AdminParser parser, final Database db) {
        dbid = (String) parser.getOptionValue(this.databaseIdOption);
        if (null != dbid) {
            db.setId(Integer.parseInt(dbid));
        }
    }

    protected void parseAndSetDatabasename(final AdminParser parser, final Database db) {
        dbname = (String) parser.getOptionValue(this.databaseNameOption);
        if (null != dbname) {
            db.setName(dbname);
        }
    }

    protected void parseAndSetSchema(final AdminParser parser, final Database db) {
        String schema = (String) parser.getOptionValue(this.schemaOption);
        if (null != schema) {
            db.setScheme(schema);
        }
    }

    private void parseAndSetHostname(final AdminParser parser, final Database db) throws InvalidDataException {
        String hostname = (String) parser.getOptionValue(this.hostnameOption);
        if (null != hostname) {
            if (hostname.startsWith("mysql://")) {
                URI uri;
                try {
                    uri = new URI(hostname);
                    int port = uri.getPort();
                    hostname = uri.getHost() + String.valueOf(port != -1 ? port : 3306);
                } catch (URISyntaxException e) {
                    throw new InvalidDataException(e);
                }
            }
            db.setUrl("jdbc:mysql://" + hostname + "/?useUnicode=true&characterEncoding=UTF-8&autoReconnect=false&useUnicode=true&useServerPrepStmts=false&useTimezone=true&serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
        }
    }

    private void parseAndSetPasswd(final AdminParser parser, final Database db) {
        final String passwd = (String) parser.getOptionValue(this.databasePasswdOption);
        if (null != passwd) {
            db.setPassword(passwd);
        }
    }

    private void parseAndSetPoolmax(final AdminParser parser, final Database db) {
        final String pool_max = (String) parser.getOptionValue(this.poolMaxOption);
        if (pool_max != null) {
            db.setPoolMax(Integer.parseInt(pool_max));
        }
    }

    private void parseAndSetPoolInitial(final AdminParser parser, final Database db) {
        final String pool_initial = (String) parser.getOptionValue(this.poolInitialOption);
        if (null != pool_initial) {
            db.setPoolInitial(Integer.parseInt(pool_initial));
        }
    }

    private void parseAndSetPoolHardLimit(final AdminParser parser, final Database db) throws InvalidDataException {
        final String pool_hard_limit = (String) parser.getOptionValue(this.poolHardlimitOption);
        if (pool_hard_limit != null) {
            if (!pool_hard_limit.matches("true|false")) {
                throw new InvalidDataException("Only true or false are allowed for " + OPT_NAME_POOL_HARDLIMIT_LONG);
            }
            db.setPoolHardLimit(Boolean.parseBoolean(pool_hard_limit) ? 1 : 0);
        }
    }

    private void parseAndSetMaxUnits(final AdminParser parser, final Database db) {
        final String maxunits = (String) parser.getOptionValue(this.maxUnitsOption);
        if (maxunits != null) {
            db.setMaxUnits(Integer.parseInt(maxunits));
        }
    }

    private void parseAndSetDatabaseWeight(final AdminParser parser, final Database db) {
        final String databaseweight = (String) parser.getOptionValue(this.databaseWeightOption);
        if (databaseweight != null) {
            db.setClusterWeight(Integer.parseInt(databaseweight));
        }
    }

    private void parseAndSetDBUsername(final AdminParser parser, final Database db) {
        final String username = (String) parser.getOptionValue(this.databaseUsernameOption);
        if (null != username) {
            db.setLogin(username);
        }
    }

    private void parseAndSetDriver(final AdminParser parser, final Database db) {
        final String driver = (String) parser.getOptionValue(this.databaseDriverOption);
        if (driver != null) {
            db.setDriver(driver);
        }
    }

    protected void parseAndSetMasterAndID(final AdminParser parser, final Database db) throws InvalidDataException {
        Boolean ismaster = null;
        final String databaseismaster = (String) parser.getOptionValue(this.databaseIsMasterOption);
        if (databaseismaster != null) {
            ismaster = Boolean.parseBoolean(databaseismaster);
            db.setMaster(ismaster);
        }
        final String databasemasterid = (String) parser.getOptionValue(this.databaseMasterIDOption);
        if (null != ismaster && false == ismaster) {
            if (databasemasterid != null) {
                db.setMasterId(Integer.parseInt(databasemasterid));
            } else {
                printError(null, null, "master id must be set if this database isn't the master", parser);
                parser.printUsage();
                sysexit(SYSEXIT_MISSING_OPTION);
            }
        } else if (null == ismaster || true == ismaster) {
            if (databasemasterid != null) {
                throw new InvalidDataException("Master ID can only be set if this is a slave.");
            }
        }
    }

    protected void setDatabaseIDOption(final AdminParser parser) {
        this.databaseIdOption = setShortLongOpt(
            parser,
            OPT_NAME_DATABASE_ID_SHORT,
            OPT_NAME_DATABASE_ID_LONG,
            "The id of the database.",
            true,
            NeededQuadState.eitheror);
    }

    protected void setDatabaseSchemaOption(final AdminParser parser) {
        this.schemaOption = setLongOpt(parser, OPT_NAME_SCHEMA_LONG, "The optional schema name of the database.", true, false);
    }

    protected void setDatabasePoolMaxOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            this.poolMaxOption = setShortLongOptWithDefault(
                parser,
                OPT_NAME_POOL_MAX_SHORT,
                OPT_NAME_POOL_MAX_LONG,
                "Db pool max",
                defaultvalue,
                true,
                convertBooleantoTriState(required));
        } else {
            this.poolMaxOption = setShortLongOpt(
                parser,
                OPT_NAME_POOL_MAX_SHORT,
                OPT_NAME_POOL_MAX_LONG,
                "Db pool max",
                true,
                convertBooleantoTriState(required));
        }
    }

    protected void setDatabasePoolInitialOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            this.poolInitialOption = setShortLongOptWithDefault(
                parser,
                OPT_NAME_POOL_INITIAL_SHORT,
                OPT_NAME_POOL_INITIAL_LONG,
                "Db pool initial",
                defaultvalue,
                true,
                convertBooleantoTriState(required));
        } else {
            this.poolInitialOption = setShortLongOpt(
                parser,
                OPT_NAME_POOL_INITIAL_SHORT,
                OPT_NAME_POOL_INITIAL_LONG,
                "Db pool initial",
                true,
                convertBooleantoTriState(required));
        }
    }

    protected void setDatabaseHostnameOption(final AdminParser parser, final boolean required) {
        this.hostnameOption = setShortLongOpt(
            parser,
            OPT_NAME_HOSTNAME_SHORT,
            OPT_NAME_HOSTNAME_LONG,
            "Hostname of the server",
            true,
            convertBooleantoTriState(required));
    }

    protected void setDatabaseUsernameOption(final AdminParser parser, final boolean required) {
        this.databaseUsernameOption = setShortLongOpt(
            parser,
            OPT_NAME_DB_USERNAME_SHORT,
            OPT_NAME_DB_USERNAME_LONG,
            "Name of the user for the database",
            true,
            convertBooleantoTriState(required));
    }

    protected void setDatabaseDriverOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            this.databaseDriverOption = setShortLongOptWithDefault(
                parser,
                OPT_NAME_DB_DRIVER_SHORT,
                OPT_NAME_DB_DRIVER_LONG,
                "The driver to be used for the database",
                defaultvalue,
                true,
                convertBooleantoTriState(required));
        } else {
            this.databaseDriverOption = setShortLongOpt(
                parser,
                OPT_NAME_DB_DRIVER_SHORT,
                OPT_NAME_DB_DRIVER_LONG,
                "The driver to be used for the database",
                true,
                convertBooleantoTriState(required));
        }
    }

    protected void setDatabasePasswdOption(final AdminParser parser, final boolean required) {
        this.databasePasswdOption = setShortLongOpt(
            parser,
            OPT_NAME_DB_PASSWD_SHORT,
            OPT_NAME_DB_PASSWD_LONG,
            "Password for the database",
            true,
            convertBooleantoTriState(required));
    }

    protected void setDatabaseIsMasterOption(final AdminParser parser, final boolean required) {
        this.databaseIsMasterOption = setShortLongOpt(
            parser,
            OPT_NAME_IS_MASTER_SHORT,
            OPT_NAME_IS_MASTER_LONG,
            "true/false",
            "Set this if the registered database is the master",
            required);
    }

    protected void setDatabaseMasterIDOption(final AdminParser parser, final boolean required) {
        this.databaseMasterIDOption = setShortLongOpt(
            parser,
            OPT_NAME_MASTER_ID_SHORT,
            OPT_NAME_MASTER_ID_LONG,
            "If this database isn't the master give the id of the master here",
            true,
            convertBooleantoTriState(required));
    }

    protected void setDatabaseWeightOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            this.databaseWeightOption = setShortLongOptWithDefault(
                parser,
                OPT_NAME_WEIGHT_SHORT,
                OPT_NAME_WEIGHT_LONG,
                "The db weight for this database",
                defaultvalue,
                true,
                convertBooleantoTriState(required));
        } else {
            this.databaseWeightOption = setShortLongOpt(
                parser,
                OPT_NAME_WEIGHT_SHORT,
                OPT_NAME_WEIGHT_LONG,
                "The db weight for this database",
                true,
                convertBooleantoTriState(required));
        }
    }

    protected void setDatabaseMaxUnitsOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            this.maxUnitsOption = setShortLongOptWithDefault(
                parser,
                OPT_NAME_MAX_UNITS_SHORT,
                OPT_NAME_MAX_UNITS_LONG,
                "The maximum number of contexts in this database.",
                defaultvalue,
                true,
                convertBooleantoTriState(required));
        } else {
            this.maxUnitsOption = setShortLongOpt(
                parser,
                OPT_NAME_MAX_UNITS_SHORT,
                OPT_NAME_MAX_UNITS_LONG,
                "The maximum number of contexts in this database.",
                true,
                convertBooleantoTriState(required));
        }
    }

    protected void setDatabasePoolHardlimitOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            // FIXME: choeger Enter right description here
            this.poolHardlimitOption = setShortLongOptWithDefault(
                parser,
                OPT_NAME_POOL_HARDLIMIT_SHORT,
                OPT_NAME_POOL_HARDLIMIT_LONG,
                "true/false",
                "Db pool hardlimit",
                defaultvalue,
                required);
        } else {
            this.poolHardlimitOption = setShortLongOpt(
                parser,
                OPT_NAME_POOL_HARDLIMIT_SHORT,
                OPT_NAME_POOL_HARDLIMIT_LONG,
                "true/false",
                "Db pool hardlimit",
                required);
        }
    }

    protected void setDatabaseNameOption(final AdminParser parser, final NeededQuadState required) {
        this.databaseNameOption = setShortLongOpt(
            parser,
            OPT_NAME_DBNAME_SHORT,
            OPT_NAME_DBNAME_LONG,
            "Name of the database",
            true,
            required);
    }

    @Override
    protected String getObjectName() {
        return "database";
    }

    protected void parseAndSetMandatoryOptions(final AdminParser parser, final Database db) throws InvalidDataException {
        parseAndSetHostname(parser, db);

        parseAndSetDriver(parser, db);

        parseAndSetDBUsername(parser, db);

        parseAndSetPasswd(parser, db);

        parseAndSetMaxUnits(parser, db);

        parseAndSetPoolHardLimit(parser, db);

        parseAndSetPoolInitial(parser, db);

        parseAndSetPoolmax(parser, db);

        parseAndSetDatabaseWeight(parser, db);
    }
}
