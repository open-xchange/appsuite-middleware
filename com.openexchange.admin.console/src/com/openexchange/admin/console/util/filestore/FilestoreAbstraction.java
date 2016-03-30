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
            fstore.setSize(Long.parseLong(store_size));
        }
    }

    protected void parseAndSetFilestoreMaxCtxs(final AdminParser parser, final Filestore fstore) {
        final String store_max_ctx = (String) parser.getOptionValue(this.filestoreMaxContextsOption);
        if (store_max_ctx != null) {
            fstore.setMaxContexts(Integer.parseInt(store_max_ctx));
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
        fstore.setId(Integer.parseInt(filestoreid));
    }

    @Override
    protected String getObjectName() {
        return "filestore";
    }
}
