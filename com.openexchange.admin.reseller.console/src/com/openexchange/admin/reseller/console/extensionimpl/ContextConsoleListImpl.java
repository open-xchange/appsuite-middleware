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
    }

    @Override
    public void setAndFillExtension(final AdminParser parser, final Context ctx, Credentials auth) throws OXConsolePluginException {
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
        if( extension == null ) {
            throw new PluginException("No extension data found in server reply.");
        }
        final String customid = extension.getCustomid();
        if(extension.isCustomidset() && null != customid) {
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
        if( extension == null ) {
            throw new PluginException("No extension data found in server reply.");
        }
        final String customid = extension.getCustomid();
        if(extension.isCustomidset() && null != customid) {
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
