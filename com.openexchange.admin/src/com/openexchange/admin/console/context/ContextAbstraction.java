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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.ServiceLoader;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleCommonInterface;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.console.user.UserAbstraction;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

public abstract class ContextAbstraction extends UserAbstraction {   

    private interface ClosureInterface {
        public ArrayList<String> getData(final Context ctx);
    }

    private static final String OPT_NAME_CONTEXT_QUOTA_DESCRIPTION = "Context wide filestore quota in MB.";
    private final static char OPT_QUOTA_SHORT = 'q';

    private final static String OPT_QUOTA_LONG = "quota";
    protected static final String OPT_NAME_ADMINPASS_DESCRIPTION="master Admin password";
    protected static final String OPT_NAME_ADMINUSER_DESCRIPTION="master Admin user name";
    
    protected Option contextQuotaOption = null;

    protected String contextname = null;
    
    private ServiceLoader<? extends ContextConsoleCommonInterface> subclasses = null;
    
    @Override
    protected String getObjectName() {
        return "context";
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
    
    protected void parseAndSetExtensions(final AdminParser parser, final Context ctx, Credentials auth) {
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
    
    protected void setAdminPassOption(final AdminParser admp) {
        this.adminPassOption = setShortLongOpt(admp,OPT_NAME_ADMINPASS_SHORT, OPT_NAME_ADMINPASS_LONG, OPT_NAME_ADMINPASS_DESCRIPTION, true, NeededQuadState.possibly);
    }
    
    protected void setAdminUserOption(final AdminParser admp) {
        this.adminUserOption= setShortLongOpt(admp,OPT_NAME_ADMINUSER_SHORT, OPT_NAME_ADMINUSER_LONG, OPT_NAME_ADMINUSER_DESCRIPTION, true, NeededQuadState.possibly);
    }
    
    protected void setExtensionOptions(final AdminParser parser, Class<? extends ContextConsoleCommonInterface> clazz) {
        try {
            this.subclasses = ServiceLoader.load(clazz);
        } catch (final IllegalAccessException e) {
            printError(null, null, "Error during initializing extensions: " + e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        } catch (final InstantiationException e) {
            printError(null, null, "Error during initializing extensions: " + e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        } catch (final ClassNotFoundException e) {
            printError(null, null, "Error during initializing extensions: " + e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        } catch (final IOException e) {
            printError(null, null, "Error during initializing extensions: " + e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        }

        try {
            for (final ContextConsoleCommonInterface ctxconsole : this.subclasses) {
                ctxconsole.addExtensionOptions(parser);
            }
        } catch (final OXConsolePluginException e) {
            printError(null, null, "Error while adding extension options: " + e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
            sysexit(1);
        }
    }
    
    protected void setContextQuotaOption(final AdminParser parser,final boolean required ){
        this.contextQuotaOption = setShortLongOpt(parser, OPT_QUOTA_SHORT,OPT_QUOTA_LONG,OPT_NAME_CONTEXT_QUOTA_DESCRIPTION,true, convertBooleantoTriState(required));
    }

    protected void sysoutOutput(final Context[] ctxs, final AdminParser parser) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final Context ctx : ctxs) {
            data.add(makeData(ctx, new ClosureInterface() {
                public ArrayList<String> getData(Context ctx) {
                    return getHumanReableDataOfAllExtensions(ctx, parser);
                }
            }));
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
        columns.addAll(getCSVColumnsOfAllExtensions(parser));
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    
        for (final Context ctx_tmp : ctxs) {
            data.add(makeData(ctx_tmp, new ClosureInterface() {
                public ArrayList<String> getData(Context ctx) {
                    return getCSVDataOfAllExtensions(ctx_tmp, parser);
                }

            }));
        }
    
        doCSVOutput(columns, data);
    }

    protected ArrayList<String> getHumanReadableColumnsOfAllExtensions(AdminParser parser) {
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

    private ArrayList<String> makeData(final Context ctx, final ClosureInterface iface) {
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
    
        srv_data.addAll(iface.getData(ctx));
    
        return srv_data;
    }

}

