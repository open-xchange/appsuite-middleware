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

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class ListUsersByAliasDomain extends ListCore {

    private static final String ALIAS_DOMAIN = "alias-domain";
    private static final char ALIAS_DOMAIN_SHORT = 'd';
    private static CLIOption alias_domain_cli_option;

    public static void main(final String[] args) {
        new ListUsersByAliasDomain(args);
    }

    public ListUsersByAliasDomain(final String[] args2) {

        final AdminParser parser = new AdminParser("listuserbyaliasdomain");

        commonfunctions(parser, args2);
    }


    @Override
    protected User[] maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        String domain = (String) parser.getOptionValue(alias_domain_cli_option);
        User[] ids = oxusr.listByAliasDomain(ctx, domain, auth);
        if (ids.length == 0) {
            return new User[0];
        }
        return oxusr.getData(ctx, ids, auth);
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
        alias_domain_cli_option = setShortLongOpt(parser, ALIAS_DOMAIN_SHORT, ALIAS_DOMAIN, "The domain of the user aliases.", true, NeededQuadState.needed);
    }

    @Override
    protected ArrayList<String> getColumnsOfAllExtensions(final User user) {
        return new ArrayList<String>();
    }

    @Override
    protected ArrayList<String> getDataOfAllExtensions(final User user) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return new ArrayList<String>();
    }
}
