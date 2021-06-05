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

/**
 * {@link GetModuleAccess} gets the module access informations for a given context
 *
 */
@SuppressWarnings("deprecation")
public class GetModuleAccess extends ContextAbstraction {

    /**
     * Executes this clt
     *
     * @param args The arguments
     */
    public void execute(final String[] args) {

        final AdminParser parser = new AdminParser("getmoduleaccessforcontext");

        setOptions(parser);

        String successtext = null;

        try {
            parser.ownparse(args);
            final Context ctx = contextparsing(parser);

            parseAndSetContextName(parser, ctx);

            successtext = nameOrIdSetInt(this.ctxid, this.contextname, "context");

            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);

            // Fetch access object
            final UserModuleAccess access = oxres.getModuleAccess(ctx, auth);

            // output access object
            doCsvOutput(access);

            // exit application
            sysexit(0);
        } catch (Exception e) {
            printErrors(successtext, null, e, parser);
        }
    }

    /**
     * Outputs the result in the csv format
     *
     * @param access The {@link UserModuleAccess}
     * @throws InvalidDataException
     */
    private void doCsvOutput(final UserModuleAccess access) throws InvalidDataException {
        final ArrayList<ArrayList<String>> data = new ArrayList<>();
        final ArrayList<String> datarow = new ArrayList<>();
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
        doCSVOutput(getAccessColums(), false, data);
    }

    /**
     * Gets the column names
     *
     * @return An {@link ArrayList} of column names
     */
    private static ArrayList<String> getAccessColums() {
        final ArrayList<String> columnnames = new ArrayList<>(32);
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
        columnnames.add(UserAbstraction.OPT_ACCESS_SUBSCRIPTION);
        columnnames.add(UserAbstraction.OPT_ACCESS_PUBLICATION);
        columnnames.add(UserAbstraction.OPT_ACCESS_ACTIVE_SYNC);
        columnnames.add(UserAbstraction.OPT_ACCESS_USM);
        columnnames.add(UserAbstraction.OPT_ACCESS_OLOX20);
        columnnames.add(UserAbstraction.OPT_ACCESS_DENIED_PORTAL);
        columnnames.add(UserAbstraction.OPT_DISABLE_GAB);
        columnnames.add(UserAbstraction.OPT_ACCESS_PUBLIC_FOLDER_EDITABLE);
        return columnnames;
    }

    public static void main(final String args[]) {
        new GetModuleAccess().execute(args);
    }

    /**
     * Sets the options for this clt
     *
     * @param parser The {@link AdminParser}
     */
    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);
    }
}
