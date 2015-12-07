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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.drive.client.windows.clt;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CLIIllegalOptionValueException;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.CLIParseException;
import com.openexchange.admin.console.CLIUnknownOptionException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.drive.client.windows.service.rmi.BrandingConfigurationRemote;
import com.openexchange.exception.OXException;

/**
 * {@link ReloadBrandings}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class ReloadBrandings extends BasicCommandlineOptions {

    private static final String PATH_LONG = "path";
    private static final char PATH_SHORT = 'p';

    private CLIOption path = null;

    /**
     * @param args
     */
    public static void main(String[] args) {
        new ReloadBrandings().execute(args);
    }

    private void execute(String[] args) {
        final AdminParser parser = new AdminParser("reload_brandings");
        setOptions(parser);
        try {
            parser.ownparse(args);
            BrandingConfigurationRemote remote = (BrandingConfigurationRemote) Naming.lookup(RMI_HOSTNAME + BrandingConfigurationRemote.RMI_NAME);
            String path = (String) parser.getOptionValue(this.path);
            List<String> brands;
            if (path == null || path.length() == 0) {
                brands = remote.reload();
            } else {
                brands = remote.reload(path);
            }
            System.out.println("Brandings successful reloaded!");
            System.out.println("Following brands are now available:");
            for (String brand : brands) {
                System.out.println("\t -" + brand);
            }
        } catch (CLIParseException e) {
            printError(e.getMessage(), parser);
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (CLIIllegalOptionValueException e) {
            printError(e.getMessage(), parser);
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (CLIUnknownOptionException e) {
            printError(e.getMessage(), parser);
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (MissingOptionException e) {
            printError(e.getMessage(), parser);
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (MalformedURLException e) {
            printError(e.getMessage(), parser);
            sysexit(1);
        } catch (RemoteException e) {
            printError(e.getMessage(), parser);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (NotBoundException e) {
            printError(e.getMessage(), parser);
            sysexit(1);
        } catch (OXException e) {
            printError(e.getMessage(), parser);
            sysexit(1);
        }
    }

    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        this.path = setShortLongOpt(parser, PATH_SHORT, PATH_LONG, "a path", "Defines the path to look for brandings. If set the configured path will be ignored.", false);
    }

}
