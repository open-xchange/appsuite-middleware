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

package com.openexchange.admin.console.user;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
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

public class List extends ListCoreExtended {

    public static void main(final String[] args) {
        new List().execute(args);
    }

    public void execute(final String[] args) {
        commonfunctions(new AdminParser("listuser"), args);
    }

    @Override
    protected void setOptions(final AdminParser parser) {
        super.setOptions(parser);
        setLengthOption(parser);
        setOffsetOption(parser);
    }

    @Override
    protected User[] maincall(final AdminParser parser, final OXUserInterface oxusr, final String search_pattern, final boolean ignoreCase, final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        return maincall(parser, oxusr, search_pattern, ignoreCase, ctx, auth, false, false, null, null);
    }

    @Override
    protected User[] maincall(final AdminParser parser, final OXUserInterface oxusr, final String search_pattern, final boolean ignoreCase, final Context ctx, final Credentials auth, final boolean includeGuests, final boolean excludeUsers, final Integer length, final Integer offset) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        final User[] allusers = ignoreCase ? oxusr.listCaseInsensitive(ctx, search_pattern, auth, includeGuests, excludeUsers, length, offset) : oxusr.list(ctx, search_pattern, auth, includeGuests, excludeUsers, length, offset);
        if (allusers.length == 0) {
            return new User[0];
        }
        return oxusr.getData(ctx, allusers, auth);
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
        setIncludeGuestsOption(parser);
        setExcludeUsersOption(parser);
    }

    @Override
    protected ArrayList<String> getColumnsOfAllExtensions(final User user) {
        return new ArrayList<>();
    }

    @Override
    protected ArrayList<String> getDataOfAllExtensions(final User user) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return new ArrayList<>();
    }
}
