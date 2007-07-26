package com.openexchange.admin.console.util.filestore;

import java.net.URISyntaxException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededTriState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.util.UtilAbstraction;
import com.openexchange.admin.rmi.dataobjects.Filestore;

public abstract class FilestoreAbstraction extends UtilAbstraction {
    protected final static char OPT_NAME_STORE_FILESTORE_ID_SHORT = 'i';
    protected final static String OPT_NAME_STORE_FILESTORE_ID_LONG = "id";
    protected final static char OPT_NAME_STORE_PATH_SHORT = 't';
    protected final static String OPT_NAME_STORE_PATH_LONG = "storepath";
    protected final static char OPT_NAME_STORE_SIZE_SHORT = 's';
    protected final static String OPT_NAME_STORE_SIZE_LONG = "storesize";
    protected final static char OPT_NAME_STORE_MAX_CTX_SHORT = 'x';
    protected final static String OPT_NAME_STORE_MAX_CTX_LONG = "maxcontexts";
    
    protected Option filestoreIdOption = null;

    protected Option filestorePathOption = null;
    
    protected Option filestoreSizeOption = null;
    
    protected Option filestoreMaxContextsOption = null;
    
    protected Integer filestoreid = null;
    
    protected void parseAndSetFilestorePath(final AdminParser parser, final Filestore fstore) throws URISyntaxException {
        final String store_path = (String) parser.getOptionValue(this.filestorePathOption);
        if (null != store_path) {
            // Setting the options in the dataobject
            final java.net.URI uri = new java.net.URI(store_path);
            fstore.setUrl(uri.toString());
        }
    }

    protected void parseAndSetFilestoreSize(final AdminParser parser, final Filestore fstore) {
        final String store_size = (String) parser.getOptionValue(this.filestoreSizeOption);
        if (store_size != null) {
            fstore.setSize(Long.parseLong(store_size));
        }
    }

    protected void parseAndSetFilestoreMaxCtxs(final AdminParser parser, final Filestore fstore) {
        final String store_max_ctx = (String) parser.getOptionValue(this.filestoreMaxContextsOption);
        if (store_max_ctx != null) {
            fstore.setMaxContexts(Integer.parseInt(store_max_ctx));
        }
    }

    protected void setPathOption(final AdminParser parser, final NeededTriState needed) {
        this.filestorePathOption = setShortLongOpt(parser, OPT_NAME_STORE_PATH_SHORT, OPT_NAME_STORE_PATH_LONG, "Path to store filestore contents in URI format e.g. file:///tmp/filestore", true, needed);
    }

    protected void setSizeOption(final AdminParser parser, final String defaultvalue) {
        if (null != defaultvalue) {
            this.filestoreSizeOption = setShortLongOptWithDefault(parser, OPT_NAME_STORE_SIZE_SHORT, OPT_NAME_STORE_SIZE_LONG, "The maximum size of the filestore", defaultvalue, true, NeededTriState.notneeded);
        } else {
            this.filestoreSizeOption = setShortLongOpt(parser, OPT_NAME_STORE_SIZE_SHORT, OPT_NAME_STORE_SIZE_LONG, "The maximum size of the filestore", true, NeededTriState.notneeded);
        }
    }

    protected void setMaxCtxOption(final AdminParser parser, final String defaultvalue) {
        if (null != defaultvalue) {
            this.filestoreMaxContextsOption = setShortLongOptWithDefault(parser, OPT_NAME_STORE_MAX_CTX_SHORT, OPT_NAME_STORE_MAX_CTX_LONG, "the maximum number of contexts", defaultvalue, true, NeededTriState.notneeded);
        } else {
            this.filestoreMaxContextsOption = setShortLongOpt(parser, OPT_NAME_STORE_MAX_CTX_SHORT, OPT_NAME_STORE_MAX_CTX_LONG, "the maximum number of contexts", true, NeededTriState.notneeded);
        }
    }

    protected void setFilestoreIDOption(final AdminParser parser) {
        this.filestoreIdOption = setShortLongOpt(parser, OPT_NAME_STORE_FILESTORE_ID_SHORT, OPT_NAME_STORE_FILESTORE_ID_LONG, "The id of the filestore which should be deleted", true, NeededTriState.needed);
    }

    protected void parseAndSetFilestoreID(final AdminParser parser, final Filestore fstore) {
        filestoreid = Integer.parseInt((String) parser.getOptionValue(this.filestoreIdOption));
        fstore.setId(filestoreid);
    }

    @Override
    protected String getObjectName() {
        return "filestore";
    }
}
