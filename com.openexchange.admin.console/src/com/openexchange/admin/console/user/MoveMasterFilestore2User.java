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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
package com.openexchange.admin.console.user;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;

public class MoveMasterFilestore2User extends UserAbstraction {

    public static void main(String args[]) {
        new MoveMasterFilestore2User(args);
    }

    private static final char OPT_MASTER_SHORT = 'm';
    private static final String OPT_MASTER_LONG = "master";
    private static final char OPT_FILESTORE_SHORT = 'f';
    private static final String OPT_FILESTORE_LONG = "filestore";
    private static final char OPT_QUOTA_SHORT = 'q';
    private static final String OPT_QUOTA_LONG = "quota";

    // -----------------------------------------------------------------------------------------------

    private Integer filestoreid = null;
    private CLIOption targetFilestoreIDOption = null;
    private Integer masterId = null;
    private CLIOption masterIdOption = null;
    private CLIOption contextQuotaOption = null;

    public MoveMasterFilestore2User(String[] args) {

        final AdminParser parser = new AdminParser("movemasterfilestore2user");
        setOptions(parser);

        String successtext = null;
        try {
            parser.ownparse(args);

            User usr = new User();
            parseAndSetUserId(parser, usr);
            parseAndSetUsername(parser, usr);
            successtext = nameOrIdSetInt(this.userid, this.username, "user");

            Context ctx = contextparsing(parser);
            Credentials auth = credentialsparsing(parser);

            Filestore filestore = parseAndSetFilestoreId(parser);

            long maxQuota = parseAndGetUserQuota(parser);

            User masterUser = parseAndSetMaster(parser);

            // get rmi ref
            OXUserInterface oxusr = getUserInterface();

            if (null == masterUser) {
                masterUser = oxusr.getContextAdmin(ctx, auth);
            }

            int jobId = oxusr.moveFromMasterToUserFilestore(ctx, usr, masterUser, filestore, maxQuota, auth);

            displayMovedMessage(successtext, null, "to user filestore " + filestore.getId() + " scheduled as job " + jobId, parser);
            sysexit(0);
        } catch (final Exception e) {
            // In this special case the second parameter is not the context id but the filestore id
            // this also applies to all following error outputting methods
            // see com.openexchange.admin.console.context.ContextHostingAbstraction.printFirstPartOfErrorText(Integer, Integer)
            printErrors(successtext, masterId, e, parser);
        }
    }

    protected void setFilestoreIdOption(final AdminParser parser) {
        this.targetFilestoreIDOption = setShortLongOpt(parser, OPT_FILESTORE_SHORT, OPT_FILESTORE_LONG, "Target filestore id", true, NeededQuadState.needed);
    }

    protected Filestore parseAndSetFilestoreId(final AdminParser parser) {
        filestoreid = Integer.valueOf((String) parser.getOptionValue(this.targetFilestoreIDOption));
        final Filestore fs = new Filestore(filestoreid);
        return fs;
    }

    /**
     * Initializes the quota option <code>'q'</code>/<code>"quota"</code>.
     * <p>
     * Invoke this method in <code>setFurtherOptions(AdminParser)</code> method to add that option to the command-line tool.
     *
     * @param parser The parser to add the option to
     * @param required Whether that option is required or not
     */
    protected void setUserQuotaOption(AdminParser parser, boolean required){
        this.contextQuotaOption = setShortLongOpt(parser, OPT_QUOTA_SHORT, OPT_QUOTA_LONG, "The file storage quota in MB for associated user.", true, required ? NeededQuadState.needed : NeededQuadState.notneeded);
    }

    /**
     * Parses and sets the value for the quota option <code>'q'</code>/<code>"quota"</code>.
     *
     * @param parser The parser to get the value from
     */
    protected long parseAndGetUserQuota(AdminParser parser) {
        String contextQuota = (String) parser.getOptionValue(this.contextQuotaOption);
        return null == contextQuota ? -1L : Long.parseLong(contextQuota);
    }

    protected void setMasterOption(final AdminParser parser) {
        this.masterIdOption = setShortLongOpt(parser, OPT_MASTER_SHORT, OPT_MASTER_LONG, "Master user id. If not set, the context administrator is assumed to be the master user.", true, NeededQuadState.notneeded);
    }

    protected User parseAndSetMaster(final AdminParser parser) {
        Object optionValue = parser.getOptionValue(this.masterIdOption);
        if (null == optionValue) {
            return null;
        }

        masterId = Integer.valueOf((String) optionValue);
        User masterUser = new User(masterId.intValue());
        return masterUser;
    }

    protected final void displayMovedMessage(final String id, final Integer ctxid, final String text, final AdminParser parser) {
        createMessageForStdout(id, ctxid, text, parser);
    }

    private void setOptions(final AdminParser parser) {
        setIdOption(parser);
        setUsernameOption(parser, NeededQuadState.eitheror);

        setDefaultCommandLineOptionsWithoutContextID(parser);

        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);
        setMasterOption(parser);
        setFilestoreIdOption(parser);
        setUserQuotaOption(parser, true);
    }
}
