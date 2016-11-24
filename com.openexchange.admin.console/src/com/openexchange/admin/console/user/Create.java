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

package com.openexchange.admin.console.user;

import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.java.Strings;

public class Create extends CreateCore {

    private CLIOption primaryAccountNameOption;
    private static final String OPT_PRIMARY_ACCOUNT_NAME_OPTION = "primary-account-name";

    public static void main(String[] args) {
        new Create(args);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    public Create(String[] args2) {
        AdminParser parser = new AdminParser("createuser");
        commonfunctions(parser, args2);
    }

    @Override
    protected void maincall(AdminParser parser, OXUserInterface oxusr, Context ctx, User usr, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        parseAndSetUserQuota(parser, usr);
        parseAndSetFilestoreId(parser, usr);
        parseAndSetFilestoreOwner(parser, usr);
        final String primaryAccountName = parseAndGetPrimaryAccountName(parser);
        String accesscombinationname = parseAndSetAccessCombinationName(parser);
        if (null != accesscombinationname) {
            // Create user with access rights combination name
            Integer id = oxusr.create(ctx, usr, accesscombinationname, auth, primaryAccountName).getId();
            displayCreatedMessage(String.valueOf(id), ctx.getId(), parser);
        } else {
            // get the context-admins module access rights as baseline
            UserModuleAccess access = oxusr.getContextAdminUserModuleAccess(ctx, auth);

            if (access.isPublicFolderEditable()) {
                // publicFolderEditable can only be applied to the context administrator.
                access.setPublicFolderEditable(false);
            }

            // adjust module access rights according to parameters given on the command line
            setModuleAccessOptions(parser, access);

            // create the user with the adjusted module access rights
            Integer id = oxusr.create(ctx, usr, access, auth, primaryAccountName).getId();
            displayCreatedMessage(String.valueOf(id), ctx.getId(), parser);
        }
    }

    private String parseAndGetPrimaryAccountName(AdminParser parser) {
        CLIOption option = primaryAccountNameOption;
        if (null == option) {
            return null;
        }
        String primAccountName = (String) parser.getOptionValue(option);
        if (Strings.isEmpty(primAccountName)) {
            return null;
        }
        return primAccountName;
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {
        setAddAccessRightCombinationNameOption(parser);
        setFilestoreIdOption(parser, false);
        setFilestoreOwnerOption(parser, false);
        setUserQuotaOption(parser, false);
        this.primaryAccountNameOption = setLongOpt(parser, OPT_PRIMARY_ACCOUNT_NAME_OPTION, "The name of the primary mail account.", true, false);
    }

}
