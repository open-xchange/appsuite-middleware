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
package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.TimeZone;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class CreateCore extends ContextAbstraction {
    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptions(parser);
        setContextNameOption(parser);
        setMandatoryOptions(parser);
        
        setLanguageOption(parser);
        setTimezoneOption(parser);

        setContextQuotaOption(parser, true);
        
        setFurtherOptions(parser);
    }
    
    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        try {

            parser.ownparse(args);

            final Context ctx = contextparsing(parser);

            parseAndSetContextName(parser, ctx);

            final Credentials auth = credentialsparsing(parser);

            // create user obj
            final User usr = new User();

            // fill user obj with mandatory values from console
            parseAndSetMandatoryOptionsinUser(parser, usr);
            // fill user obj with mandatory values from console
            final String tz = (String) parser.getOptionValue(this.timezoneOption);
            if (null != tz) {
                usr.setTimezone(TimeZone.getTimeZone(tz));
            }

            final String languageoptionvalue = (String) parser.getOptionValue(this.languageOption);
            if (languageoptionvalue != null) {
                final String[] lange = languageoptionvalue.split("_");
                if (lange != null && lange.length == 2) {
                    usr.setLanguage(new Locale(lange[0].toLowerCase(), lange[1].toUpperCase()));
                }
            }

            parseAndSetContextQuota(parser, ctx);
            
            maincall(parser, ctx, usr, auth).getIdAsInt();
            
            displayCreatedMessage(String.valueOf(ctxid), null);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(String.valueOf(ctxid), null, e, parser);
        }
    }

    protected abstract Context maincall(final AdminParser parser, Context ctx, User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MalformedURLException, NotBoundException, ContextExistsException, NoSuchContextException;
        
    protected abstract void setFurtherOptions(final AdminParser parser);
}
