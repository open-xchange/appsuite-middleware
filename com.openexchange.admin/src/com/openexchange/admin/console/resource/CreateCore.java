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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.admin.console.resource;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class CreateCore extends ResourceAbstraction {
    
    protected final void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);

        setNameOption(parser, true);
        setDisplayNameOption(parser, true);
        setAvailableOption(parser, false);
        setDescriptionOption(parser, false);
        setEmailOption(parser, true);
        
        setFurtherOptions(parser);
    }

    protected abstract void setFurtherOptions(final AdminParser parser);

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        try {
            parser.ownparse(args);

            final Context ctx = contextparsing(parser);

            final Credentials auth = credentialsparsing(parser);
            
            final OXResourceInterface oxres = getResourceInterface();
            final Resource res = new Resource();

            res.setAvailable(Boolean.parseBoolean((String) parser.getOptionValue(this.resourceAvailableOption)));

            if (parser.getOptionValue(this.resourceDescriptionOption) != null) {
                res.setDescription((String) parser.getOptionValue(this.resourceDescriptionOption));
            }

            maincall(parser, oxres, ctx, res, auth);
            res.setDisplayname((String) parser.getOptionValue(this.resourceDisplayNameOption));
            res.setEmail((String) parser.getOptionValue(this.resourceEmailOption));
            res.setName((String) parser.getOptionValue(this.resourceNameOption));
            final Integer id = oxres.create(ctx, res, auth).getId();

            displayCreatedMessage(id, ctx.getIdAsInt());
            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(null, null, neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg(null, null, "Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerException(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerException(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerException(e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerException(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerException(e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final InvalidDataException e) {
            printServerException(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final IllegalOptionValueException e) {
            printError(null, null, "Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError(null, null, "Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(null, null, e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (final DatabaseUpdateException e) {
            printServerException(e);
            sysexit(1);
        } catch (final DuplicateExtensionException e) {
            printServerException(e);
            sysexit(1);
        }

    }

    protected abstract void maincall(final AdminParser parser, final OXResourceInterface oxres, final Context ctx, final Resource res, final Credentials auth) throws RemoteException, DuplicateExtensionException;
}
