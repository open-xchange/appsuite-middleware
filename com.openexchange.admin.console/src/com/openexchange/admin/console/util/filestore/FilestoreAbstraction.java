/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */
package com.openexchange.admin.console.util.filestore;

import java.net.URISyntaxException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
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

    protected CLIOption filestoreIdOption = null;

    protected CLIOption filestorePathOption = null;

    protected CLIOption filestoreSizeOption = null;

    protected CLIOption filestoreMaxContextsOption = null;

    protected String filestoreid = null;

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
            fstore.setSize(Long.valueOf(store_size));
        }
    }

    protected void parseAndSetFilestoreMaxCtxs(final AdminParser parser, final Filestore fstore) {
        final String store_max_ctx = (String) parser.getOptionValue(this.filestoreMaxContextsOption);
        if (store_max_ctx != null) {
            fstore.setMaxContexts(Integer.valueOf(store_max_ctx));
        }
    }

    protected void setPathOption(final AdminParser parser, final NeededQuadState needed) {
        this.filestorePathOption = setShortLongOpt(parser, OPT_NAME_STORE_PATH_SHORT, OPT_NAME_STORE_PATH_LONG, "Path to store filestore contents in URI format e.g. file:/tmp/filestore", true, needed);
    }

    protected void setSizeOption(final AdminParser parser, final String defaultvalue) {
        if (null != defaultvalue) {
            this.filestoreSizeOption = setShortLongOptWithDefault(parser, OPT_NAME_STORE_SIZE_SHORT, OPT_NAME_STORE_SIZE_LONG, "The maximum size of the filestore in MB", defaultvalue, true, NeededQuadState.notneeded);
        } else {
            this.filestoreSizeOption = setShortLongOpt(parser, OPT_NAME_STORE_SIZE_SHORT, OPT_NAME_STORE_SIZE_LONG, "The maximum size of the filestore in MB", true, NeededQuadState.notneeded);
        }
    }

    protected void setMaxCtxOption(final AdminParser parser, final String defaultvalue) {
        if (null != defaultvalue) {
            this.filestoreMaxContextsOption = setShortLongOptWithDefault(parser, OPT_NAME_STORE_MAX_CTX_SHORT, OPT_NAME_STORE_MAX_CTX_LONG, "The maximum number of contexts", defaultvalue, true, NeededQuadState.notneeded);
        } else {
            this.filestoreMaxContextsOption = setShortLongOpt(parser, OPT_NAME_STORE_MAX_CTX_SHORT, OPT_NAME_STORE_MAX_CTX_LONG, "The maximum number of contexts", true, NeededQuadState.notneeded);
        }
    }

    protected void setFilestoreIDOption(final AdminParser parser, String command) {
        this.filestoreIdOption = setShortLongOpt(parser, OPT_NAME_STORE_FILESTORE_ID_SHORT, OPT_NAME_STORE_FILESTORE_ID_LONG, "The id of the filestore which should be " + command, true, NeededQuadState.needed);
    }

    protected void parseAndSetFilestoreID(final AdminParser parser, final Filestore fstore) {
        filestoreid = (String) parser.getOptionValue(this.filestoreIdOption);
        fstore.setId(Integer.valueOf(filestoreid));
    }

    @Override
    protected String getObjectName() {
        return "filestore";
    }
}
