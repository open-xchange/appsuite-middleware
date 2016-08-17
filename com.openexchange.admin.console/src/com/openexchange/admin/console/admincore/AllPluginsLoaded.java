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

package com.openexchange.admin.console.admincore;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.rmi.OXAdminCoreInterface;

public class AllPluginsLoaded extends BasicCommandlineOptions {

    private AllPluginsLoaded() {
        super();
    }

    @Override
    protected void setDefaultCommandLineOptionsWithoutContextID(final AdminParser parser) {
        // Do nothing
    }

    public static void main(final String[] args) {
        new AllPluginsLoaded().internalMain(args);
    }

    private void internalMain(String[] args) {
        AdminParser parser = new AdminParser("allpluginsloaded");
        setDefaultCommandLineOptionsWithoutContextID(parser);

        try {
            parser.ownparse(args);

            final OXAdminCoreInterface oxadmincore = (OXAdminCoreInterface) Naming.lookup(RMI_HOSTNAME+OXAdminCoreInterface.RMI_NAME);
            if (oxadmincore.allPluginsLoaded()) {
                sysexit(0);
            } else {
                sysexit(1);
            }
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage(), null);
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(e,null);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(e,null);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerException(e,null);
            sysexit(1);
        } catch (final Exception e) {
            printServerException(e,null);
            sysexit(1);
        }
    }
}
