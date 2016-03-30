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
package com.openexchange.admin.console.context;

import java.rmi.Naming;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.user.UserAbstraction;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

public class GetModuleAccess extends ContextAbstraction {

    public GetModuleAccess(final String[] args2) {

        final AdminParser parser = new AdminParser("getmoduleaccessforcontext");

        setOptions(parser);

        String successtext = null;

        try {
            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);

            parseAndSetContextName(parser, ctx);

            successtext = nameOrIdSetInt(this.ctxid, this.contextname, "context");

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            // Fetch access object
            final UserModuleAccess access = oxres.getModuleAccess(ctx, auth);

            // output access object
            doCsvOutput(access);

            // exit application
            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        }
    }

    private void doCsvOutput(final UserModuleAccess access) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        final ArrayList<String> datarow = new ArrayList<String>();
        datarow.add(String.valueOf(access.getCalendar()));
        datarow.add(String.valueOf(access.getContacts()));
        datarow.add(String.valueOf(access.getDelegateTask()));
        datarow.add(String.valueOf(access.getEditPublicFolders()));
        datarow.add(String.valueOf(access.getIcal()));
        datarow.add(String.valueOf(access.getInfostore()));
        datarow.add(String.valueOf(access.getReadCreateSharedFolders()));
        datarow.add(String.valueOf(access.getSyncml()));
        datarow.add(String.valueOf(access.getTasks()));
        datarow.add(String.valueOf(access.getVcard()));
        datarow.add(String.valueOf(access.getWebdav()));
        datarow.add(String.valueOf(access.getWebdavXml()));
        datarow.add(String.valueOf(access.getWebmail()));
        datarow.add(String.valueOf(access.getEditGroup()));
        datarow.add(String.valueOf(access.getEditResource()));
        datarow.add(String.valueOf(access.getEditPassword()));
        datarow.add(String.valueOf(access.isCollectEmailAddresses()));
        datarow.add(String.valueOf(access.isMultipleMailAccounts()));
        datarow.add(String.valueOf(access.isPublication()));
        datarow.add(String.valueOf(access.isSubscription()));
        datarow.add(String.valueOf(access.isActiveSync()));
        datarow.add(String.valueOf(access.isUSM()));
        datarow.add(String.valueOf(access.isOLOX20()));
        datarow.add(String.valueOf(access.isDeniedPortal()));
        datarow.add(String.valueOf(access.isGlobalAddressBookDisabled()));
        datarow.add(String.valueOf(access.isPublicFolderEditable()));
        data.add(datarow);
        doCSVOutput(getAccessColums(),data);
    }

    private static ArrayList<String> getAccessColums() {
        final ArrayList<String> columnnames = new ArrayList<String>(32);
        columnnames.add(UserAbstraction.OPT_ACCESS_CALENDAR);
        columnnames.add(UserAbstraction.OPT_ACCESS_CONTACTS);
        columnnames.add(UserAbstraction.OPT_ACCESS_DELEGATE_TASKS);
        columnnames.add(UserAbstraction.OPT_ACCESS_EDIT_PUBLIC_FOLDERS);
        columnnames.add(UserAbstraction.OPT_ACCESS_ICAL);
        columnnames.add(UserAbstraction.OPT_ACCESS_INFOSTORE);
        columnnames.add(UserAbstraction.OPT_ACCESS_READCREATE_SHARED_FOLDERS);
        columnnames.add(UserAbstraction.OPT_ACCESS_SYNCML);
        columnnames.add(UserAbstraction.OPT_ACCESS_TASKS);
        columnnames.add(UserAbstraction.OPT_ACCESS_VCARD);
        columnnames.add(UserAbstraction.OPT_ACCESS_WEBDAV);
        columnnames.add(UserAbstraction.OPT_ACCESS_WEBDAV_XML);
        columnnames.add(UserAbstraction.OPT_ACCESS_WEBMAIL);
        columnnames.add(UserAbstraction.OPT_ACCESS_EDIT_GROUP);
        columnnames.add(UserAbstraction.OPT_ACCESS_EDIT_RESOURCE);
        columnnames.add(UserAbstraction.OPT_ACCESS_EDIT_PASSWORD);
        columnnames.add(UserAbstraction.OPT_ACCESS_COLLECT_EMAIL_ADDRESSES);
        columnnames.add(UserAbstraction.OPT_ACCESS_MULTIPLE_MAIL_ACCOUNTS);
        columnnames.add(UserAbstraction.OPT_ACCESS_PUBLICATION);
        columnnames.add(UserAbstraction.OPT_ACCESS_SUBSCRIPTION);
        columnnames.add(UserAbstraction.OPT_ACCESS_ACTIVE_SYNC);
        columnnames.add(UserAbstraction.OPT_ACCESS_USM);
        columnnames.add(UserAbstraction.OPT_ACCESS_OLOX20);
        columnnames.add(UserAbstraction.OPT_ACCESS_DENIED_PORTAL);
        columnnames.add(UserAbstraction.OPT_DISABLE_GAB);
        columnnames.add(UserAbstraction.OPT_ACCESS_PUBLIC_FOLDER_EDITABLE);
        return columnnames;
    }

    public static void main(final String args[]) {
        new GetModuleAccess(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);
    }
}
