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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIIllegalOptionValueException;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.CLIParseException;
import com.openexchange.admin.console.CLIUnknownOptionException;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * {@link ChangeModuleAccessGlobal}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ChangeModuleAccessGlobal extends UserAbstraction {

    private static final String FILTER_LONG = "filter";

    private static final char FILTER_SHORT = 'f';

    private static final String FILTER_DESCRIPTION = "The call will only affect users with this access combination. Can be an Integer or a String, representing a module access definition. If left out, all users will be changed.";

    private CLIOption filterOption;

    private Credentials auth;

    private OXUserInterface oxusr;

    private final UserModuleAccess addAccess;

    private final UserModuleAccess removeAccess;

    private int filter;

    private String filterString;

    public static void main(final String[] args) {
        new ChangeModuleAccessGlobal(args);
    }

    /**
     * Initializes a new {@link ChangeModuleAccessGlobal}.
     *
     * @param args
     */
    public ChangeModuleAccessGlobal(String[] args) {
        AdminParser parser = new AdminParser("changeaccessglobal");
        addAccess = new UserModuleAccess();
        addAccess.disableAll();
        removeAccess = new UserModuleAccess();
        removeAccess.disableAll();

        try {
            setOptions(parser);
            prepare(parser);
            parse(parser, args);
            execute();
        } catch (Exception e) {
            printErrors(null, ctxid, e, parser);
        }
    }

    /**
     * @param parser
     */
    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        filterOption = setShortLongOpt(parser, FILTER_SHORT, FILTER_LONG, FILTER_DESCRIPTION, true, NeededQuadState.notneeded);
        accessRightsCombinationName = setShortLongOpt(parser,'a', OPT_ACCESSRIGHTS_COMBINATION_NAME, "The optional access combination name as replacement for specifying single permissions to enable/disable.", true, NeededQuadState.notneeded);
        setModuleAccessOptions(parser, false, false);
    }

    @Override
    protected void setDefaultCommandLineOptionsWithoutContextID(AdminParser admp) {
        setAdminUserOption(admp, "adminmaster", "The name of the admin master");
        setAdminPassOption(admp, "adminmasterpass", "The password of the admin master");
    }

    private void prepare(AdminParser parser) throws MalformedURLException, RemoteException, NotBoundException, CLIParseException, CLIIllegalOptionValueException, CLIUnknownOptionException, MissingOptionException, InvalidDataException {
        oxusr = getUserInterface();
    }

    private void execute() throws InvalidCredentialsException, StorageException, RemoteException, InvalidDataException {
        oxusr.changeModuleAccessGlobal(filterString, addAccess, removeAccess, auth);
    }

    private void parse(AdminParser parser, String[] args) throws CLIParseException, CLIIllegalOptionValueException, CLIUnknownOptionException, MissingOptionException, InvalidDataException, RemoteException {
        parser.ownparse(args);
        auth = credentialsparsing(parser);

        final String accessCombinationName = parseAndSetAccessCombinationName(parser);
        if (null != accessCombinationName) {
            final UserModuleAccess moduleAccess = oxusr.moduleAccessForName(accessCombinationName.trim());

            if (null == moduleAccess) {
                throw new InvalidDataException("No such access combination name \""+accessCombinationName.trim()+"\"");
            }

            if (moduleAccess.isGlobalAddressBookDisabled()) {
                throw new InvalidDataException("Unable to set Global Address Book Permission.");
            }

            moduleAccess.transferTo(addAccess, removeAccess);
            addAccess.setGlobalAddressBookDisabled(false);
            removeAccess.setGlobalAddressBookDisabled(false);


        }

        if (parser.getOptionValue(accessGAB) != null) {
            throw new InvalidDataException("Unable to set Global Address Book Permission.");
        }

        if (parser.getOptionValue(accessCalendarOption) != null) {
            if (accessOption2Boolean(parser, accessCalendarOption)) {
                addAccess.setCalendar(true);
            } else {
                removeAccess.setCalendar(true);
            }
        }
        if (parser.getOptionValue(accessContactOption) != null) {
            if (accessOption2Boolean(parser, accessContactOption)) {
                addAccess.setContacts(true);
            } else {
                removeAccess.setContacts(true);
            }
        }
        if (parser.getOptionValue(accessDelegateTasksOption) != null) {
            if (accessOption2Boolean(parser, accessDelegateTasksOption)) {
                addAccess.setDelegateTask(true);
            } else {
                removeAccess.setDelegateTask(true);
            }
        }
        if (parser.getOptionValue(accessEditPublicFolderOption) != null) {
            if (accessOption2Boolean(parser, accessEditPublicFolderOption)) {
                addAccess.setEditPublicFolders(true);
            } else {
                removeAccess.setEditPublicFolders(true);
            }
        }
        if (parser.getOptionValue(accessForumOption) != null) {
            if (accessOption2Boolean(parser, accessForumOption)) {
                addAccess.setForum(true);
            } else {
                removeAccess.setForum(true);
            }
        }
        if (parser.getOptionValue(accessIcalOption) != null) {
            if (accessOption2Boolean(parser, accessIcalOption)) {
                addAccess.setIcal(true);
            } else {
                removeAccess.setIcal(true);
            }
        }
        if (parser.getOptionValue(accessInfostoreOption) != null) {
            if (accessOption2Boolean(parser, accessInfostoreOption)) {
                addAccess.setInfostore(true);
            } else {
                removeAccess.setInfostore(true);
            }
        }
        if (parser.getOptionValue(accessPinboardWriteOption) != null) {
            if (accessOption2Boolean(parser, accessPinboardWriteOption)) {
                addAccess.setPinboardWrite(true);
            } else {
                removeAccess.setPinboardWrite(true);
            }
        }
        if (parser.getOptionValue(accessProjectsOption) != null) {
            if (accessOption2Boolean(parser, accessProjectsOption)) {
                addAccess.setProjects(true);
            } else {
                removeAccess.setProjects(true);
            }
        }
        if (parser.getOptionValue(accessReadCreateSharedFolderOption) != null) {
            if (accessOption2Boolean(parser, accessReadCreateSharedFolderOption)) {
                addAccess.setReadCreateSharedFolders(true);
            } else {
                removeAccess.setReadCreateSharedFolders(true);
            }
        }
        if (parser.getOptionValue(accessRssBookmarkOption) != null) {
            if (accessOption2Boolean(parser, accessRssBookmarkOption)) {
                addAccess.setRssBookmarks(true);
            } else {
                removeAccess.setRssBookmarks(true);
            }
        }
        if (parser.getOptionValue(accessRssPortalOption) != null) {
            if (accessOption2Boolean(parser, accessRssPortalOption)) {
                addAccess.setRssPortal(true);
            } else {
                removeAccess.setRssPortal(true);
            }
        }
        if (parser.getOptionValue(accessSyncmlOption) != null) {
            if (accessOption2Boolean(parser, accessSyncmlOption)) {
                addAccess.setSyncml(true);
            } else {
                removeAccess.setSyncml(true);
            }
        }
        if (parser.getOptionValue(accessTasksOption) != null) {
            if (accessOption2Boolean(parser, accessTasksOption)) {
                addAccess.setTasks(true);
            } else {
                removeAccess.setTasks(true);
            }
        }
        if (parser.getOptionValue(accessVcardOption) != null) {
            if (accessOption2Boolean(parser, accessVcardOption)) {
                addAccess.setVcard(true);
            } else {
                removeAccess.setVcard(true);
            }
        }
        if (parser.getOptionValue(accessWebdavOption) != null) {
            if (accessOption2Boolean(parser, accessWebdavOption)) {
                addAccess.setWebdav(true);
            } else {
                removeAccess.setWebdav(true);
            }
        }
        if (parser.getOptionValue(accessWebdavXmlOption) != null) {
            if (accessOption2Boolean(parser, accessWebdavXmlOption)) {
                addAccess.setWebdavXml(true);
            } else {
                removeAccess.setWebdavXml(true);
            }
        }
        if (parser.getOptionValue(accessWebmailOption) != null) {
            if (accessOption2Boolean(parser, accessWebmailOption)) {
                addAccess.setWebmail(true);
            } else {
                removeAccess.setWebmail(true);
            }
        }
        if (parser.getOptionValue(accessEditGroupOption) != null) {
            if (accessOption2Boolean(parser, accessEditGroupOption)) {
                addAccess.setEditGroup(true);
            } else {
                removeAccess.setEditGroup(true);
            }
        }
        if (parser.getOptionValue(accessEditResourceOption) != null) {
            if (accessOption2Boolean(parser, accessEditResourceOption)) {
                addAccess.setEditResource(true);
            } else {
                removeAccess.setEditResource(true);
            }
        }
        if (parser.getOptionValue(accessEditPasswordOption) != null) {
            if (accessOption2Boolean(parser, accessEditPasswordOption)) {
                addAccess.setEditPassword(true);
            } else {
                removeAccess.setEditPassword(true);
            }
        }
        if (parser.getOptionValue(accessCollectEmailAddresses) != null) {
            if (accessOption2Boolean(parser, accessCollectEmailAddresses)) {
                addAccess.setCollectEmailAddresses(true);
            } else {
                removeAccess.setCollectEmailAddresses(true);
            }
        }
        if (parser.getOptionValue(accessMultipleMailAccounts) != null) {
            if (accessOption2Boolean(parser, accessMultipleMailAccounts)) {
                addAccess.setMultipleMailAccounts(true);
            } else {
                removeAccess.setMultipleMailAccounts(true);
            }
        }
        if (parser.getOptionValue(accessSubscription) != null) {
            if (accessOption2Boolean(parser, accessSubscription)) {
                addAccess.setSubscription(true);
            } else {
                removeAccess.setSubscription(true);
            }
        }
        if (parser.getOptionValue(accessPublication) != null) {
            if (accessOption2Boolean(parser, accessPublication)) {
                addAccess.setPublication(true);
            } else {
                removeAccess.setPublication(true);
            }
        }
        if (parser.getOptionValue(accessActiveSync) != null) {
            if (accessOption2Boolean(parser, accessActiveSync)) {
                addAccess.setActiveSync(true);
            } else {
                removeAccess.setActiveSync(true);
            }
        }
        if (parser.getOptionValue(accessUSM) != null) {
            if (accessOption2Boolean(parser, accessUSM)) {
                addAccess.setUSM(true);
            } else {
                removeAccess.setUSM(true);
            }
        }
        if (parser.getOptionValue(accessOLOX20) != null) {
            if (accessOption2Boolean(parser, accessOLOX20)) {
                addAccess.setOLOX20(true);
            } else {
                removeAccess.setOLOX20(true);
            }
        }
        if (parser.getOptionValue(accessPublicFolderEditable) != null) {
            if (accessOption2Boolean(parser, accessPublicFolderEditable)) {
                addAccess.setPublicFolderEditable(true);
            } else {
                removeAccess.setPublicFolderEditable(true);
            }
        }

        if (parser.getOptionValue(filterOption) == null) {
            this.filterString = null;
        } else {
            this.filterString = (String) parser.getOptionValue(filterOption);
        }
    }

    private boolean accessOption2Boolean(AdminParser parser, CLIOption accessOption) {
        final String optionValue = (String) parser.getOptionValue(accessOption);
        return optionValue.trim().equalsIgnoreCase("on");
    }
}
