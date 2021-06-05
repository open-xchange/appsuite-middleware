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
package com.openexchange.admin.reseller.console.extensionimpl;

import java.util.ArrayList;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleListInterface;
import com.openexchange.admin.console.context.extensioninterfaces.PluginException;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.reseller.console.ResellerAbstraction;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;


public class ContextConsoleListImpl implements ContextConsoleListInterface {

    private static final ArrayList<String> columnnames = new ArrayList<String>();

    private static final ArrayList<String> columnnamesCSV = new ArrayList<String>();

    static {
        columnnames.add("CustomID");
        columnnames.add("Owner");
        columnnamesCSV.addAll(columnnames);
        columnnamesCSV.add("Restrictions");
    }

    @Override
    public void addExtensionOptions(final AdminParser parser) throws OXConsolePluginException {
        // Nothing
    }

    @Override
    public void setAndFillExtension(final AdminParser parser, final Context ctx, Credentials auth) throws OXConsolePluginException {
        // Nothing
    }

    @Override
    public ArrayList<String> getColumnNamesCSV() {
        return columnnamesCSV;
    }

    @Override
    public ArrayList<String> getColumnNamesHumanReadable() {
        return columnnames;
    }

    @Override
    public ArrayList<String> getCSVData(final Context ctx) throws PluginException {
        final ArrayList<String> retval = new ArrayList<String>();
        final OXContextExtensionImpl extension = (OXContextExtensionImpl) ctx.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
        if ( extension == null ) {
            throw new PluginException("No extension data found in server reply.");
        }
        final String customid = extension.getCustomid();
        if (extension.isCustomidset() && null != customid) {
            retval.add(customid);
        } else {
            retval.add(null);
        }
        final ResellerAdmin owner2 = extension.getOwner();
        if (extension.isOwnerset() && null != owner2) {
            retval.add(owner2.getName());
        } else {
            retval.add(null);
        }
        final Restriction[] restriction = extension.getRestriction();
        if (extension.isRestrictionset() && null != restriction) {
            retval.add(ResellerAbstraction.getObjectsAsString(restriction));
        } else {
            retval.add(null);
        }

        return retval;
    }

    @Override
    public ArrayList<String> getHumanReadableData(final Context ctx) throws PluginException {
        final ArrayList<String> retval = new ArrayList<String>();
        final OXContextExtensionImpl extension = (OXContextExtensionImpl) ctx.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
        if ( extension == null ) {
            throw new PluginException("No extension data found in server reply.");
        }
        final String customid = extension.getCustomid();
        if (extension.isCustomidset() && null != customid) {
            retval.add(customid);
        } else {
            retval.add(null);
        }
        final ResellerAdmin owner2 = extension.getOwner();
        if (extension.isOwnerset() && null != owner2) {
            retval.add(owner2.getName());
        } else {
            retval.add(null);
        }
        return retval;
    }

}
