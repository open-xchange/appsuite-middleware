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


import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.user.UserAbstraction;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;

public abstract class ContextAbstraction extends UserAbstraction {   

    private static final String OPT_NAME_CONTEXT_QUOTA_DESCRIPTION = "Context wide filestore quota in MB.";
    private final static char OPT_QUOTA_SHORT = 'q';

    private final static String OPT_QUOTA_LONG = "quota";
    protected static final String OPT_NAME_ADMINPASS_DESCRIPTION="master Admin password";
    protected static final String OPT_NAME_ADMINUSER_DESCRIPTION="master Admin user name";
    
    protected Option contextQuotaOption = null;

    protected String contextname = null;
    
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
    
    protected void setAdminPassOption(final AdminParser admp) {
        this.adminPassOption = setShortLongOpt(admp,OPT_NAME_ADMINPASS_SHORT, OPT_NAME_ADMINPASS_LONG, OPT_NAME_ADMINPASS_DESCRIPTION, true, NeededQuadState.possibly);
    }
    
    protected void setAdminUserOption(final AdminParser admp) {
        this.adminUserOption= setShortLongOpt(admp,OPT_NAME_ADMINUSER_SHORT, OPT_NAME_ADMINUSER_LONG, OPT_NAME_ADMINUSER_DESCRIPTION, true, NeededQuadState.possibly);
    }
    
    protected void setContextQuotaOption(final AdminParser parser,final boolean required ){
        this.contextQuotaOption = setShortLongOpt(parser, OPT_QUOTA_SHORT,OPT_QUOTA_LONG,OPT_NAME_CONTEXT_QUOTA_DESCRIPTION,true, convertBooleantoTriState(required));
    }

    protected String contextnameOrIdSet() throws MissingOptionException {
        String successtext;
        // Through the order of this checks we archive that the id is preferred over the name
        if (null == this.ctxid) {
            if (null == this.contextname) {
                throw new MissingOptionException("Either contextname or contextid must be given");
            } else {
                successtext = this.contextname;
            }
        } else {
            successtext = String.valueOf(this.ctxid);
        }
        return successtext;
    }
}

