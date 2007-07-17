package com.openexchange.admin.console.util;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

public abstract class DatabaseAbstraction extends UtilAbstraction{

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

}
