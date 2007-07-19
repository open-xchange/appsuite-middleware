package com.openexchange.admin.console.util;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededTriState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

public abstract class DatabaseAbstraction extends UtilAbstraction{
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
    
    protected Option databaseIdOption = null;
    protected Option databaseUsernameOption = null;
    protected Option databaseDriverOption = null;
    protected Option databasePasswdOption = null;
    protected Option databaseIsMasterOption = null;
    protected Option databaseMasterIDOption = null;
    protected Option databaseWeightOption = null;
    protected Option databaseNameOption = null;
    protected Option hostnameOption = null;
    protected Option maxUnitsOption = null;
    protected Option poolHardlimitOption = null;
    protected Option poolInitialOption = null;
    protected Option poolMaxOption = null;
    
    protected void parseAndSetHostname(final AdminParser parser, final Database db) {
        final String hostname = (String)parser.getOptionValue(this.hostnameOption);
        if (null != hostname) {
            db.setUrl("jdbc:mysql://" + hostname + "/?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useUnicode=true&useServerPrepStmts=false&useTimezone=true&serverTimezone=UTC&connectTimeout=15000&socketTimeout=15000");
        }
    }

    protected void parseAndSetDatabasename(final AdminParser parser, final Database db) {
        final String databasename = (String) parser.getOptionValue(this.databaseNameOption);
        if (databasename != null) {
            db.setDisplayname(databasename);
        }
    }

    protected void parseAndSetPasswd(final AdminParser parser, final Database db) {
        final String passwd = (String) parser.getOptionValue(this.databasePasswdOption);
        if (null != passwd) {
            db.setPassword(passwd);
        }
    }

    protected void parseAndSetPoolmax(final AdminParser parser, final Database db) {
        final String pool_max = (String) parser.getOptionValue(this.poolMaxOption);
        if (pool_max != null) {
            db.setPoolMax(Integer.parseInt(pool_max));
        }
    }

    protected void parseAndSetPoolInitial(final AdminParser parser, final Database db) {
        final String pool_initial = (String) parser.getOptionValue(this.poolInitialOption);
        if (null != pool_initial) {
            db.setPoolInitial(Integer.parseInt(pool_initial));
        }
    }

    protected void parseAndSetPoolHardLimit(final AdminParser parser, final Database db) throws InvalidDataException {
        final String pool_hard_limit = (String) parser.getOptionValue(this.poolHardlimitOption);
        if (pool_hard_limit != null) {
            if (!pool_hard_limit.matches("true|false")) {
                throw new InvalidDataException("Only true or false are allowed for " + OPT_NAME_POOL_HARDLIMIT_LONG);
            }
            db.setPoolHardLimit(Boolean.parseBoolean(pool_hard_limit) ? 1 : 0);
        }
    }

    protected void parseAndSetMaxUnits(final AdminParser parser, final Database db) {
        final String maxunits = (String) parser.getOptionValue(this.maxUnitsOption);
        if (maxunits != null) {
            db.setMaxUnits(Integer.parseInt(maxunits));
        }
    }

    protected void parseAndSetDatabaseWeight(final AdminParser parser, final Database db) {
        final String databaseweight = (String) parser.getOptionValue(this.databaseWeightOption);
        if (databaseweight != null) {
            db.setClusterWeight(Integer.parseInt(databaseweight));
        }
    }

    protected void parseAndSetDBUsername(final AdminParser parser, final Database db) {
        final String username = (String) parser.getOptionValue(this.databaseUsernameOption);
        if (null != username) {
            db.setLogin(username);
        }
    }

    protected void parseAndSetDriver(final AdminParser parser, final Database db) {
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
                printError("master id must be set if this database isn't the master");
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
        this.databaseIdOption = setShortLongOpt(parser, OPT_NAME_DATABASE_ID_SHORT,OPT_NAME_DATABASE_ID_LONG,"The id of the database.",true, NeededTriState.needed);
    }

    protected void setDatabasePoolMaxOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            this.poolMaxOption = setShortLongOptWithDefault(parser, OPT_NAME_POOL_MAX_SHORT, OPT_NAME_POOL_MAX_LONG, "Db pool max", defaultvalue, true, convertBooleantoTriState(required));
        } else {
            this.poolMaxOption = setShortLongOpt(parser, OPT_NAME_POOL_MAX_SHORT, OPT_NAME_POOL_MAX_LONG, "Db pool max", true, convertBooleantoTriState(required));
        }
    }

    protected void setDatabasePoolInitialOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            this.poolInitialOption = setShortLongOptWithDefault(parser, OPT_NAME_POOL_INITIAL_SHORT, OPT_NAME_POOL_INITIAL_LONG, "Db pool initial", defaultvalue, true, convertBooleantoTriState(required));
        } else {
            this.poolInitialOption = setShortLongOpt(parser, OPT_NAME_POOL_INITIAL_SHORT, OPT_NAME_POOL_INITIAL_LONG, "Db pool initial", true, convertBooleantoTriState(required));
        }
    }

    protected void setDatabaseHostnameOption(final AdminParser parser,final boolean required){
        this.hostnameOption =  setShortLongOpt(parser, OPT_NAME_HOSTNAME_SHORT,OPT_NAME_HOSTNAME_LONG,"Hostname of the server",true, convertBooleantoTriState(required)); 
    }

    protected void setDatabaseUsernameOption(final AdminParser parser,final boolean required){
        this.databaseUsernameOption = setShortLongOpt(parser, OPT_NAME_DB_USERNAME_SHORT,OPT_NAME_DB_USERNAME_LONG,"Name of the user for the database",true, convertBooleantoTriState(required));
    }

    protected void setDatabaseDriverOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            this.databaseDriverOption = setShortLongOptWithDefault(parser, OPT_NAME_DB_DRIVER_SHORT, OPT_NAME_DB_DRIVER_LONG, "The driver to be used for the database", defaultvalue, true, convertBooleantoTriState(required));
        } else {
            this.databaseDriverOption = setShortLongOpt(parser, OPT_NAME_DB_DRIVER_SHORT,OPT_NAME_DB_DRIVER_LONG,"The driver to be used for the database",true, convertBooleantoTriState(required));
        }
    }

    protected void setDatabasePasswdOption(final AdminParser parser,final boolean required){
        this.databasePasswdOption = setShortLongOpt(parser, OPT_NAME_DB_PASSWD_SHORT,OPT_NAME_DB_PASSWD_LONG,"Password for the database",true, convertBooleantoTriState(required));
    }

    protected void setDatabaseIsMasterOption(final AdminParser parser,final boolean required){
        this.databaseIsMasterOption = setShortLongOpt(parser, OPT_NAME_IS_MASTER_SHORT, OPT_NAME_IS_MASTER_LONG, "true/false", "Set this if the registered database is the master", required);
    }

    protected void setDatabaseMasterIDOption(final AdminParser parser,final boolean required){
        this.databaseMasterIDOption = setShortLongOpt(parser, OPT_NAME_MASTER_ID_SHORT, OPT_NAME_MASTER_ID_LONG, "If this database isn't the master give the id of the master here", true, convertBooleantoTriState(required));
    }

    protected void setDatabaseWeightOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            this.databaseWeightOption = setShortLongOptWithDefault(parser, OPT_NAME_WEIGHT_SHORT, OPT_NAME_WEIGHT_LONG, "The db weight for this database", defaultvalue, true, convertBooleantoTriState(required));
        } else {
            this.databaseWeightOption = setShortLongOpt(parser, OPT_NAME_WEIGHT_SHORT, OPT_NAME_WEIGHT_LONG, "The db weight for this database", true, convertBooleantoTriState(required));
        }
    }

    protected void setDatabaseMaxUnitsOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            this.maxUnitsOption = setShortLongOptWithDefault(parser, OPT_NAME_MAX_UNITS_SHORT, OPT_NAME_MAX_UNITS_LONG, "The maximum number of units in this database", defaultvalue, true, convertBooleantoTriState(required));
        } else {
            this.maxUnitsOption = setShortLongOpt(parser, OPT_NAME_MAX_UNITS_SHORT, OPT_NAME_MAX_UNITS_LONG, "The maximum number of units in this database", true, convertBooleantoTriState(required));
        }
    }

    protected void setDatabasePoolHardlimitOption(final AdminParser parser, final String defaultvalue, final boolean required) {
        if (null != defaultvalue) {
            // FIXME: choeger Enter right description here
            this.poolHardlimitOption = setShortLongOptWithDefault(parser, OPT_NAME_POOL_HARDLIMIT_SHORT, OPT_NAME_POOL_HARDLIMIT_LONG, "true/false", "Db pool hardlimit", defaultvalue, required);
        } else {
            this.poolHardlimitOption = setShortLongOpt(parser, OPT_NAME_POOL_HARDLIMIT_SHORT, OPT_NAME_POOL_HARDLIMIT_LONG, "true/false", "Db pool hardlimit", required);
        }
    }

    protected void setDatabaseNameOption(final AdminParser parser,final boolean required){
        this.databaseNameOption = setShortLongOpt(parser, OPT_NAME_DBNAME_SHORT,OPT_NAME_DBNAME_LONG,"Name of the database",true, convertBooleantoTriState(required)); 
    }

}
