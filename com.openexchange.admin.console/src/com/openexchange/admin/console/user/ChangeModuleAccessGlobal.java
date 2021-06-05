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

    private String filterString;

    private String accessCombinationName;

    public static void main(final String[] args) {
        new ChangeModuleAccessGlobal().execute(args);
    }

    /**
     * Initializes a new {@link ChangeModuleAccessGlobal}.
     */
    public ChangeModuleAccessGlobal() {
        super();
        addAccess = new UserModuleAccess();
        removeAccess = new UserModuleAccess();
    }

    /**
     * executes the command
     *
     * @param args The arguments list
     */
    public void execute(String[] args) {
        AdminParser parser = new AdminParser("changeaccessglobal");
        addAccess.disableAll();
        addAccess.setGlobalAddressBookDisabled(false);
        removeAccess.disableAll();
        removeAccess.setGlobalAddressBookDisabled(false);

        try {
            setOptions(parser);
            parse(parser, args);
            prepare();
            execute();
            printSuccessMessage();
        } catch (Exception e) {
            printErrors(null, ctxid, e, parser);
        }
    }

    private void printSuccessMessage() {
        System.out.println("Access successfully changed.");
    }

    /**
     * @param parser
     */
    private void setOptions(AdminParser parser) {
        parser.setExtendedOptions();
        setDefaultCommandLineOptionsWithoutContextID(parser);
        filterOption = setShortLongOpt(parser, FILTER_SHORT, FILTER_LONG, FILTER_DESCRIPTION, true, NeededQuadState.notneeded);
        accessRightsCombinationName = setShortLongOpt(parser,'a', OPT_ACCESSRIGHTS_COMBINATION_NAME, "The optional access combination name as replacement for specifying single permissions to enable/disable. A value for \"access-global-address-book-disabled\" will be ignored.", true, NeededQuadState.notneeded);
        setModuleAccessOptions(parser, false, true);
    }

    @Override
    protected void setDefaultCommandLineOptionsWithoutContextID(AdminParser admp) {
        setAdminUserOption(admp, "adminmaster", "The name of the admin master");
        setAdminPassOption(admp, "adminmasterpass", "The password of the admin master");
    }

    @SuppressWarnings("deprecation")
    @Override
    /**
     * Removed "access-global-address-book-disabled" since this is not supported in this CLT.
     */
    protected void setModuleAccessOptions(final AdminParser admp, final boolean required, final boolean extended) {
        this.accessCalendarOption = setLongOpt(admp, OPT_ACCESS_CALENDAR,"on/off","Calendar module (Default is off)", true, required, extended);
        this.accessContactOption = setLongOpt(admp, OPT_ACCESS_CONTACTS,"on/off","Contact module access (Default is on)", true, required, extended);
        this.accessDelegateTasksOption = setLongOpt(admp, OPT_ACCESS_DELEGATE_TASKS,"on/off","Delegate tasks access (Default is off)", true, required, extended);
        this.accessEditPublicFolderOption = setLongOpt(admp, OPT_ACCESS_EDIT_PUBLIC_FOLDERS,"on/off","Edit public folder access (Default is off)", true, required, extended);
        this.accessIcalOption = setLongOpt(admp, OPT_ACCESS_ICAL,"on/off","Ical module access (Default is off)", true, required, extended);
        this.accessInfostoreOption = setLongOpt(admp, OPT_ACCESS_INFOSTORE,"on/off","Infostore module access (Default is off)", true, required, extended);
        this.accessReadCreateSharedFolderOption = setLongOpt(admp, OPT_ACCESS_READCREATE_SHARED_FOLDERS,"on/off","Read create shared folder access (Default is off)", true, required, extended);
        this.accessSyncmlOption = setLongOpt(admp, OPT_ACCESS_SYNCML,"on/off","Syncml access (Default is off)", true, required, extended);
        this.accessTasksOption = setLongOpt(admp, OPT_ACCESS_TASKS,"on/off","Tasks access (Default is off)", true, required, extended);
        this.accessVcardOption = setLongOpt(admp, OPT_ACCESS_VCARD,"on/off","Vcard access (Default is off)", true, required, extended);
        this.accessWebdavOption = setLongOpt(admp, OPT_ACCESS_WEBDAV,"on/off","Webdav access (Default is off)", true, required, extended);
        this.accessWebdavXmlOption = setLongOpt(admp, OPT_ACCESS_WEBDAV_XML,"on/off","Webdav-Xml access (Default is off) [DEPRECATED]", true, required, extended);
        this.accessWebmailOption = setLongOpt(admp, OPT_ACCESS_WEBMAIL,"on/off","Webmail access (Default is on)", true, required, extended);
        this.accessEditGroupOption = setLongOpt(admp, OPT_ACCESS_EDIT_GROUP,"on/off","Edit Group access (Default is off)", true, required, extended);
        this.accessEditResourceOption = setLongOpt(admp, OPT_ACCESS_EDIT_RESOURCE,"on/off","Edit Resource access (Default is off)", true, required, extended);
        this.accessEditPasswordOption = setLongOpt(admp, OPT_ACCESS_EDIT_PASSWORD,"on/off","Edit Password access (Default is off)", true, required, extended);
        this.accessCollectEmailAddresses = setLongOpt(admp, OPT_ACCESS_COLLECT_EMAIL_ADDRESSES,"on/off","Collect Email Addresses access (Default is off)", true, required, extended);
        this.accessMultipleMailAccounts = setLongOpt(admp, OPT_ACCESS_MULTIPLE_MAIL_ACCOUNTS,"on/off","Multiple Mail Accounts access (Default is off)", true, required, extended);
        this.accessSubscription = setLongOpt(admp, OPT_ACCESS_SUBSCRIPTION,"on/off","Subscription access (Default is off)", true, required, extended);
        this.accessPublication = setLongOpt(admp, OPT_ACCESS_PUBLICATION,"on/off","Publication access (Default is off) [DEPRECATED]", true, required, extended);
        this.accessActiveSync = setLongOpt(admp, OPT_ACCESS_ACTIVE_SYNC, "on/off", "Exchange Active Sync access (Default is off)", true, required, extended);
        this.accessUSM = setLongOpt(admp, OPT_ACCESS_USM, "on/off", "Universal Sync access (Default is off)", true, required, extended);
        this.accessOLOX20 = setLongOpt(admp, OPT_ACCESS_OLOX20, "on/off", "OLOX v2.0 access (Default is off) [DEPRECATED]", true, required, extended);
        this.accessDeniedPortal = setLongOpt(admp, OPT_ACCESS_DENIED_PORTAL, "on/off", "Denies portal access (Default is off)", true, required, extended);
        this.accessPublicFolderEditable = setLongOpt(admp, OPT_ACCESS_PUBLIC_FOLDER_EDITABLE, "on/off", "Whether public folder(s) is/are editable (Default is off). Applies only to context admin user.", true, required, extended);
    }

    private void prepare() throws MalformedURLException, RemoteException, NotBoundException  {
        oxusr = getUserInterface();
    }

    private void execute() throws InvalidCredentialsException, StorageException, RemoteException, InvalidDataException {
        UserModuleAccess add = new UserModuleAccess();
        add.disableAll();
        add.setGlobalAddressBookDisabled(false);
        UserModuleAccess remove = new UserModuleAccess();
        remove.disableAll();
        remove.setGlobalAddressBookDisabled(false);

        if (null != accessCombinationName) {
            final UserModuleAccess moduleAccess = oxusr.moduleAccessForName(accessCombinationName.trim());

            if (null == moduleAccess) {
                throw new InvalidDataException("No such access combination name \""+accessCombinationName.trim()+"\"");
            }

            if (moduleAccess.isGlobalAddressBookDisabled()) {
                throw new InvalidDataException("Unable to set Global Address Book Permission.");
            }

            moduleAccess.transferTo(add, remove);
            add.setGlobalAddressBookDisabled(false);
            remove.setGlobalAddressBookDisabled(false);
        }

        UserModuleAccess dummy = new UserModuleAccess();
        addAccess.transferTo(add, dummy);
        removeAccess.transferTo(remove, dummy);
        oxusr.changeModuleAccessGlobal(filterString, add, remove, auth);
    }

    /**
     * Parses the given arguments
     *
     * @param parser The {@link AdminParser} to use
     * @param args The arguments to parse
     * @throws CLIParseException
     * @throws CLIIllegalOptionValueException
     * @throws CLIUnknownOptionException
     * @throws MissingOptionException
     */
    @SuppressWarnings("deprecation")
    private void parse(AdminParser parser, String[] args) throws CLIParseException, CLIIllegalOptionValueException, CLIUnknownOptionException, MissingOptionException {
        parser.ownparse(args);
        auth = credentialsparsing(parser);

        accessCombinationName = parseAndSetAccessCombinationName(parser);

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
        if (parser.getOptionValue(accessReadCreateSharedFolderOption) != null) {
            if (accessOption2Boolean(parser, accessReadCreateSharedFolderOption)) {
                addAccess.setReadCreateSharedFolders(true);
            } else {
                removeAccess.setReadCreateSharedFolders(true);
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
