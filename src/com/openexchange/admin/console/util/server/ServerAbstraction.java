package com.openexchange.admin.console.util.server;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.util.UtilAbstraction;
import com.openexchange.admin.rmi.dataobjects.Server;

public abstract class ServerAbstraction extends UtilAbstraction {

    static final String OPT_NAME_LONG = "name";
    static final String OPT_NAME_SERVER_ID_LONG = "id";
    static final char OPT_NAME_SERVER_ID_SHORT = 'i';
    static final char OPT_NAME_SHORT = 'n';

    protected String serverid = null;
    protected Option serverIdOption = null;

    protected String servername = null;
    protected Option serverNameOption = null;

    @Override
    protected String getObjectName() {
        return "server";
    }

    protected void parseAndSetServerID(final AdminParser parser, final Server sv) {
        serverid = (String) parser.getOptionValue(this.serverIdOption);
        if (null != serverid) {
            sv.setId(Integer.parseInt(serverid));
        }
    }
    
    protected void parseAndSetServername(final AdminParser parser, final Server sv) {
        servername = (String) parser.getOptionValue(this.serverNameOption);
        if (null != servername) {
            sv.setName(servername);
        }
    }

    protected void setServeridOption(AdminParser parser) {
        serverIdOption = setShortLongOpt(parser, OPT_NAME_SERVER_ID_SHORT, OPT_NAME_SERVER_ID_LONG, "The id of the server which should be deleted", true, NeededQuadState.eitheror);
    }

    protected void setServernameOption(final AdminParser parser, final NeededQuadState needed) {
        this.serverNameOption = setShortLongOpt(parser, OPT_NAME_SHORT, OPT_NAME_LONG, "The name of the server", true, needed);
    }
}
