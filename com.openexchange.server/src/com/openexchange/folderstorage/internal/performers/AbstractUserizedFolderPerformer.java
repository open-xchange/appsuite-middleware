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

package com.openexchange.folderstorage.internal.performers;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.GuestPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StorageParametersUtility;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.folderstorage.internal.FolderI18nNamesServiceImpl;
import com.openexchange.folderstorage.internal.UserizedFolderImpl;
import com.openexchange.folderstorage.osgi.FolderStorageServices;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Autoboxing;
import com.openexchange.objectusecount.IncrementArguments;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.share.CreatedShares;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractUserizedFolderPerformer} - Abstract super class for actions which return one or multiple instances of
 * {@link UserizedFolder}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractUserizedFolderPerformer extends AbstractPerformer {

    protected static final String RECURSION_MARKER = AbstractUserizedFolderPerformer.class.getName() + ".RECURSION_MARKER";

    private static final String DUMMY_ID = "dummyId";
    private static final Pattern IS_NUMBERED_PARENTHESIS = Pattern.compile("\\(\\d+\\)$");
    private static final Pattern IS_NUMBERED = Pattern.compile("\\d+$");

    private final FolderServiceDecorator decorator;
    private volatile TimeZone timeZone;
    private volatile Locale locale;
    private volatile java.util.List<ContentType> allowedContentTypes;

    /**
     * Initializes a new {@link AbstractUserizedFolderPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @throws OXException If passed session is invalid
     */
    public AbstractUserizedFolderPerformer(final ServerSession session, final FolderServiceDecorator decorator) throws OXException {
        super(session);
        this.decorator = decorator;
        storageParameters.setDecorator(decorator);
    }

    /**
     * Initializes a new {@link AbstractUserizedFolderPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     */
    public AbstractUserizedFolderPerformer(final User user, final Context context, final FolderServiceDecorator decorator) {
        super(user, context);
        this.decorator = decorator;
        storageParameters.setDecorator(decorator);
    }

    /**
     * Initializes a new {@link AbstractUserizedFolderPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public AbstractUserizedFolderPerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, folderStorageDiscoverer);
        this.decorator = decorator;
        storageParameters.setDecorator(decorator);
    }

    /**
     * Initializes a new {@link AbstractUserizedFolderPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public AbstractUserizedFolderPerformer(final User user, final Context context, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, folderStorageDiscoverer);
        this.decorator = decorator;
        storageParameters.setDecorator(decorator);
    }

    /** The PIM content types */
    protected static final Set<String> PIM_CONTENT_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(CalendarContentType.getInstance().toString(), ContactContentType.getInstance().toString(), TaskContentType.getInstance().toString(), InfostoreContentType.getInstance().toString())));

    /**
     * Checks if denoted folder is a public PIM folder
     *
     * @param parent The folder to check
     * @return
     */
    protected static boolean isPublicPimFolder(Folder folder) {
        if (FolderStorage.PUBLIC_ID.equals(folder.getID())) {
            return true;
        }

        if (PublicType.getInstance().equals(folder.getType())) {
            ContentType contentType = folder.getContentType();
            if (null != contentType && PIM_CONTENT_TYPES.contains(contentType.toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Those content type identifiers which are capable to accept folder names containing parenthesis characters.
     */
    protected static final Set<String> PARENTHESIS_CAPABLE = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        CalendarContentType.getInstance().toString(),
        TaskContentType.getInstance().toString(),
        ContactContentType.getInstance().toString(),
        InfostoreContentType.getInstance().toString(),
        FileStorageContentType.getInstance().toString())));

    /**
     * Gets the optional folder service decorator.
     *
     * @return The folder service decorator or <code>null</code>
     */
    protected final FolderServiceDecorator getDecorator() {
        return decorator;
    }

    /**
     * Gets the time zone.
     * <p>
     * If a {@link FolderServiceDecorator decorator} was set and its {@link FolderServiceDecorator#getTimeZone() getTimeZone()} method
     * returns a non-<code>null</code> value, then decorator's time zone is returned; otherwise user's time zone is returned.
     *
     * @return The time zone
     */
    protected final TimeZone getTimeZone() {
        TimeZone tmp = timeZone;
        if (null == tmp) {
            synchronized (this) {
                tmp = timeZone;
                if (null == tmp) {
                    final TimeZone tz = null == decorator ? null : decorator.getTimeZone();
                    timeZone = tmp = (tz == null ? TimeZoneUtils.getTimeZone(getUser().getTimeZone()) : tz);
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the locale.
     * <p>
     * If a {@link FolderServiceDecorator decorator} was set and its {@link FolderServiceDecorator#getLocale() getLocale()} method returns a
     * non-<code>null</code> value, then decorator's locale is returned; otherwise user's locale is returned.
     *
     * @return The locale
     */
    protected final Locale getLocale() {
        Locale tmp = locale;
        if (null == tmp) {
            synchronized (this) {
                tmp = locale;
                if (null == tmp) {
                    final Locale l = null == decorator ? null : decorator.getLocale();
                    locale = tmp = l == null ? getUser().getLocale() : l;
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the allowed content types.
     *
     * @return The allowed content types
     */
    protected final java.util.List<ContentType> getAllowedContentTypes() {
        java.util.List<ContentType> tmp = allowedContentTypes;
        if (null == tmp) {
            synchronized (this) {
                tmp = allowedContentTypes;
                if (null == tmp) {
                    tmp = null == decorator ? ALL_ALLOWED : decorator.getAllowedContentTypes();
                    allowedContentTypes = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the named property from decorator (if not <code>null</code>).
     *
     * @param name The property name
     * @return The property's value or <code>null</code>
     */
    @SuppressWarnings("unchecked")
    protected final <V> V getDecoratorProperty(final String name) {
        if (null == name || null == decorator) {
            return null;
        }
        final Object val = decorator.getProperty(name);
        return null == val ? null : (V) val;
    }

    /**
     * Gets the named property's string value from decorator (if not <code>null</code>).
     *
     * @param name The property name
     * @return The property's string value or <code>null</code>
     */
    protected final String getDecoratorStringProperty(final String name) {
        if (null == name || null == decorator) {
            return null;
        }
        final Object val = decorator.getProperty(name);
        return null == val ? null : val.toString();
    }

    /**
     * Gets the user-sensitive folder for given folder.
     *
     * @param folder The folder
     * @param ownPermission The user's permission on given folder
     * @param treeId The tree identifier
     * @param all <code>true</code> to add all subfolders; otherwise <code>false</code> to only add subscribed ones
     * @param nullIsPublicAccess <code>true</code> if a <code>null</code> value obtained from {@link Folder#getSubfolderIDs()} hints to
     *            publicly accessible folder; otherwise <code>false</code>
     * @param storageParameters The storage parameters to use
     * @param openedStorages The list of opened storages
     * @return The user-sensitive folder for given folder
     * @throws OXException If a folder error occurs
     */
    protected UserizedFolder getUserizedFolder(final Folder folder, final Permission ownPermission, final String treeId, final boolean all, final boolean nullIsPublicAccess, final StorageParameters storageParameters, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
        return getUserizedFolder(folder, ownPermission, treeId, all, nullIsPublicAccess, storageParameters, openedStorages, false);
    }

    /**
     * Gets the user-sensitive folder for given folder.
     *
     * @param folder The folder
     * @param ownPermission The user's permission on given folder
     * @param treeId The tree identifier
     * @param all <code>true</code> to add all subfolders; otherwise <code>false</code> to only add subscribed ones
     * @param nullIsPublicAccess <code>true</code> if a <code>null</code> value obtained from {@link Folder#getSubfolderIDs()} hints to
     *            publicly accessible folder; otherwise <code>false</code>
     * @param storageParameters The storage parameters to use
     * @param openedStorages The list of opened storages
     * @return The user-sensitive folder for given folder
     * @throws OXException If a folder error occurs
     */
    protected UserizedFolder getUserizedFolder(final Folder folder, final Permission ownPermission, final String treeId, final boolean all, final boolean nullIsPublicAccess, final StorageParameters storageParameters, final java.util.Collection<FolderStorage> openedStorages, final boolean checkOnly) throws OXException {
        Folder f = folder;
        final UserizedFolder userizedFolder;
        /*-
         * Type
         *
         * Create user-sensitive folder dependent on shared flag
         */
        final boolean isShared;
        {
            final int createdBy = f.getCreatedBy();
            final Type type = f.getType();
            if (SharedType.getInstance().equals(type)) {
                userizedFolder = new UserizedFolderImpl(f, storageParameters.getSession(), storageParameters.getUser(), storageParameters.getContext());
                userizedFolder.setDefault(false);
                userizedFolder.setDefaultType(0);
                isShared = true;
            } else if ((createdBy >= 0) && (createdBy != getUserId()) && PrivateType.getInstance().equals(type)) {
                /*
                 * Prepare
                 */
                final FolderStorage curStorage = folderStorageDiscoverer.getFolderStorage(treeId, f.getID());
                boolean alreadyOpened = false;
                final Iterator<FolderStorage> it = openedStorages.iterator();
                for (int j = 0; !alreadyOpened && j < openedStorages.size(); j++) {
                    if (it.next().equals(curStorage)) {
                        alreadyOpened = true;
                    }
                }
                if (!alreadyOpened && curStorage.startTransaction(storageParameters, false)) {
                    openedStorages.add(curStorage);
                }
                f = curStorage.prepareFolder(treeId, f, storageParameters);
                userizedFolder = new UserizedFolderImpl(f, storageParameters.getSession(), storageParameters.getUser(), storageParameters.getContext());
                userizedFolder.setDefault(false);
                userizedFolder.setDefaultType(0);
                userizedFolder.setType(SharedType.getInstance());
                isShared = true;
            } else {
                userizedFolder = new UserizedFolderImpl(f, storageParameters.getSession(), storageParameters.getUser(), storageParameters.getContext());
                isShared = false;
            }
        }
        /*
         * Set locale
         */
        userizedFolder.setLocale(getLocale());
        userizedFolder.setAltNames(StorageParametersUtility.getBoolParameter("altNames", storageParameters));
        /*
         * Permissions
         */
        userizedFolder.setOwnPermission(ownPermission);
        CalculatePermission.calculateUserPermissions(userizedFolder, getContext());
        /*
         * Check parent
         */
        if (userizedFolder.getID().startsWith(FolderObject.SHARED_PREFIX)) {
            userizedFolder.setParentID(FolderStorage.SHARED_ID);
        }
        /*
         * Time zone offset and last-modified in UTC
         */
        {
            final Date cd = f.getCreationDate();
            if (null != cd) {
                final long time = cd.getTime();
                userizedFolder.setCreationDate(new Date(addTimeZoneOffset(time, getTimeZone())));
                userizedFolder.setCreationDateUTC(new Date(time));
            }
        }
        {
            final Date lm = f.getLastModified();
            if (null != lm) {
                final long time = lm.getTime();
                userizedFolder.setLastModified(new Date(addTimeZoneOffset(time, getTimeZone())));
                userizedFolder.setLastModifiedUTC(new Date(time));
            }
        }
        if (!checkOnly) {
            if (isShared) {
                /*
                 * Subfolders already calculated for user
                 */
                final String[] visibleSubfolders = f.getSubfolderIDs();
                if (null == visibleSubfolders) {
                    userizedFolder.setSubfolderIDs(nullIsPublicAccess ? new String[] { DUMMY_ID } : new String[0]);
                } else {
                    userizedFolder.setSubfolderIDs(visibleSubfolders);
                }
            } else {
                /*
                 * Compute user-visible subfolders
                 */
                hasVisibleSubfolderIDs(f, treeId, all, userizedFolder, nullIsPublicAccess, storageParameters, openedStorages);
            }
        }
        return userizedFolder;
    }

    /**
     * Checks a folder's name in the supplied target folder for conflicts with reserved folder names. Depending on the
     * <code>autorename</code> decorator property, the folder name is either adjusted to no longer conflict, or the conflicting reserved
     * name is returned for further processing.
     *
     * @param treeId The tree identifier
     * @param targetFolderId The identifier of the parent folder where the folder should be saved in
     * @param folderToSave The folder to be saved
     * @param contentType The folder's content type
     * @param allowAutorename <code>true</code> to allow an automatic rename based on the <code>autorename</code> decorator property, <code>false</code>, otherwise
     * @return <code>null</code> if the folder name does not or no longer conflict due to auto-rename, or the conflicting reserved folder name, otherwise
     */
    protected String checkForReservedName(String treeId, String targetFolderId, Folder folderToSave, ContentType contentType, boolean allowAutorename) throws OXException {
        if (false == check4Duplicates || null == folderToSave.getName() ||
            InfostoreContentType.getInstance().toString().equals(contentType.toString())) {
            return null;
        }
        boolean autoRename = allowAutorename ? AJAXRequestDataTools.parseBoolParameter(getDecoratorStringProperty("autorename")) : false;
        String lowercaseTargetName = folderToSave.getName().toLowerCase(getLocale());
        /*
         * check reserved names for outlook folders (non-infostore folders)
         */
        Set<String> reservedNames = new HashSet<String>();
        Set<String> i18nNames = FolderI18nNamesServiceImpl.getInstance().getI18nNamesFor(Module.SYSTEM.getFolderConstant(),
            Module.CALENDAR.getFolderConstant(), Module.CONTACTS.getFolderConstant(), Module.MAIL.getFolderConstant(), Module.TASK.getFolderConstant());
        for (String i18nName : i18nNames) {
            String reservedName = i18nName.toLowerCase(getLocale());
            if (false == autoRename && lowercaseTargetName.equals(reservedName)) {
                return i18nName;
            }
            reservedNames.add(reservedName);
        }
        /*
         * auto-rename automatically as needed
         */
        if (autoRename) {
            autoRename(reservedNames, folderToSave, contentType);
        }
        return null;
    }

    /**
     * Checks a folder's name against equally named folders in the supplied target folder. Depending on the <code>autorename</code>
     * decorator property, the folder name is either adjusted to no longer conflict, or the conflicting existing folder is returned for
     * further processing.
     *
     * @param treeId The tree identifier
     * @param targetFolderId The identifier of the parent folder where the folder should be saved in
     * @param folderToSave The folder to be saved
     * @param contentType The folder's content type
     * @param allowAutorename <code>true</code> to allow an automatic rename based on the <code>autorename</code> decorator property, <code>false</code>, otherwise
     * @return <code>null</code> if the folder name does not or no longer conflict due to auto-rename, or the conflicting folder, otherwise
     */
    protected UserizedFolder checkForEqualName(String treeId, String targetFolderId, Folder folderToSave, ContentType contentType, boolean allowAutorename) throws OXException {
        if (false == check4Duplicates || null == folderToSave.getName()) {
            return null;
        }
        boolean autoRename = allowAutorename ? AJAXRequestDataTools.parseBoolParameter(getDecoratorStringProperty("autorename")) : false;
        String lowercaseTargetName = folderToSave.getName().toLowerCase(getLocale());
        /*
         * check for equally named folder on same level
         */
        Set<String> conflictingNames = new HashSet<String>();
        ListPerformer listPerformer;
        if (session == null) {
            listPerformer = new ListPerformer(user, context, null, folderStorageDiscoverer);
        } else {
            listPerformer = new ListPerformer(session, null, folderStorageDiscoverer);
        }
        UserizedFolder[] existingFolders = listPerformer.doList(treeId, targetFolderId, true, true);
        for (UserizedFolder existingFolder : existingFolders) {
            if (false == existingFolder.getID().equals(folderToSave.getID())) {
                String conflictingName = existingFolder.getName().toLowerCase(getLocale());
                if (false == autoRename && lowercaseTargetName.equals(conflictingName)) {
                    return existingFolder;
                }
                conflictingNames.add(conflictingName);
                if (false == InfostoreContentType.getInstance().toString().equals(contentType.toString())) {
                    String conflictingLocalizedName = existingFolder.getLocalizedName(getLocale()).toLowerCase(getLocale());
                    if (false == autoRename && lowercaseTargetName.equals(conflictingLocalizedName)) {
                        return existingFolder;
                    }
                    conflictingNames.add(conflictingLocalizedName);
                }
            }
        }
        /*
         * auto-rename automatically as needed
         */
        if (autoRename) {
            autoRename(conflictingNames, folderToSave, contentType);
        }
        return null;
    }

    /**
     * Automatically renames a folder until it no longer conflicts with other reserved names.
     *
     * @param conflictingNames The names to check against as <b>lowercase</b> strings for easy comparison
     * @param folderToSave The folder to save
     * @return <code>true</code> if the folder's name was adjusted, <code>false</code>, otherwise
     */
    private boolean autoRename(Set<String> conflictingNames, Folder folderToSave, ContentType contentType) {
        if (null == conflictingNames || 0 == conflictingNames.size()) {
            return false;
        }
        String targetName = folderToSave.getName();
        if (conflictingNames.contains(targetName.toLowerCase(getLocale()).trim())) {
            boolean useParenthesis = PARENTHESIS_CAPABLE.contains(contentType.toString());
            int counter = 0;
            do {
                targetName = enhance(targetName, ++counter, useParenthesis);
            } while (conflictingNames.contains(targetName.toLowerCase(getLocale()).trim()));
            folderToSave.setName(targetName);
            return true;
        }
        return false;
    }

    /**
     * Schedules cleanup tasks for removed guest permission entities.
     *
     * @param removedPermissions The removed permissions
     */
    protected void processRemovedGuestPermissions(List<Permission> removedPermissions) throws OXException {
        if (ignoreGuestPermissions()) {
            return;
        }
        List<Integer> guestIDs = new ArrayList<Integer>(removedPermissions.size());
        for (Permission permission : removedPermissions) {
            guestIDs.add(Integer.valueOf(permission.getEntity()));
        }
        FolderStorageServices.requireService(ShareService.class).scheduleGuestCleanup(getContextId(), Autoboxing.I2i(guestIDs));
    }

    /**
     * Adds share targets as a consequence of added guest permission entities. This also includes creating or resolving the corresponding
     * guest user. The supplied guest permissions are enriched by the matching guest user entities automatically. This method needs the
     * session set on the according folder performer. If no session is set, this is a no-op.
     *
     * @param folderID The ID of the parent folder
     * @param contentType The content type / module of the parent folder
     * @param comparedPermissions The compared permissions
     * @param connection The database connection to use or <code>null</code>
     */
    protected void processAddedGuestPermissions(String folderID, ContentType contentType, ComparedFolderPermissions comparedPermissions, Connection connection) throws OXException {
        if (session == null) {
            return;
        }

        if (comparedPermissions.hasNewGuests()) {
            Map<ShareTarget, List<GuestPermission>> permissionsPerTarget = getPermissionsPerTarget(folderID, contentType, comparedPermissions.getNewGuestPermissions());
            ShareService shareService = FolderStorageServices.requireService(ShareService.class);
            ObjectUseCountService useCountService = FolderStorageServices.getService(ObjectUseCountService.class);

            CreatedShares shares = null;
            boolean sessionParameterSet = false;
            try {
                if (false == session.containsParameter(Connection.class.getName() + '@' + Thread.currentThread().getId())) {
                    session.setParameter(Connection.class.getName() + '@' + Thread.currentThread().getId(), connection);
                    sessionParameterSet = true;
                }
                for (Map.Entry<ShareTarget, List<GuestPermission>> entry : permissionsPerTarget.entrySet()) {
                    List<GuestPermission> permissions = entry.getValue();
                    List<ShareRecipient> recipients = new ArrayList<ShareRecipient>(permissions.size());
                    for (GuestPermission permission : permissions) {
                        recipients.add(permission.getRecipient());
                    }
                    shares = shareService.addTarget(session, entry.getKey(), recipients);
                    if (null == shares || shares.size() != permissions.size()) {
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Shares not created as expected");
                    }
                    for (GuestPermission permission : permissions) {
                        ShareInfo share = shares.getShare(permission.getRecipient());
                        permission.setEntity(share.getGuest().getGuestID());
                        comparedPermissions.rememberGuestInfo(share.getGuest());
                        if (null != useCountService) {
                            IncrementArguments arguments = new IncrementArguments.Builder(share.getGuest().getEmailAddress()).build();
                            useCountService.incrementObjectUseCount(session, arguments);
                        }
                    }
                }
            } finally {
                if (sessionParameterSet) {
                    session.setParameter(Connection.class.getName() + '@' + Thread.currentThread().getId(), null);
                }
            }
        }
    }

    private boolean ignoreGuestPermissions() {
        return decorator != null && decorator.getBoolProperty(FolderServiceDecorator.PROPERTY_IGNORE_GUEST_PERMISSIONS);
    }

    /**
     * Gets the resulting share targets based on the supplied guest permissions.
     *
     * @param folderID The folder ID to get the share targets for
     * @param contentType The content type of the folder
     * @param permissions The guest permissions
     * @return The share targets, each one mapped to the corresponding list of guest permissions
     */
    private static Map<ShareTarget, List<GuestPermission>> getPermissionsPerTarget(String folderID, ContentType contentType, List<GuestPermission> permissions) {
        Map<ShareTarget, List<GuestPermission>> permissionsPerTarget = new HashMap<ShareTarget, List<GuestPermission>>();
        for (GuestPermission permission : permissions) {
            ShareTarget target = new ShareTarget(contentType.getModule(), String.valueOf(folderID));
            List<GuestPermission> exitingPermissions = permissionsPerTarget.get(target);
            if (null == exitingPermissions) {
                exitingPermissions = new ArrayList<GuestPermission>();
                permissionsPerTarget.put(target, exitingPermissions);
            }
            exitingPermissions.add(permission);
        }
        return permissionsPerTarget;
    }

    /**
     * Verifies that a permission change is valid in terms of anonymous and invited guest users. I.e.:
     * <ul>
     *  <li>every folder can carry at most one anonymous guest permission</li>
     *  <li>anonymous guest permissions must be read-only</li>
     *  <li>existing anonymous guests must not be added as permission entities (only new ones are allowed)</li>
     *  <li>invited guest permissions must be read-only depending on the folder module</li>
     * </ul>
     *
     * @param comparedPermissions The compared permissions
     * @throws OXException if at least one permission is invalid, {@link FolderExceptionErrorMessage#INVALID_PERMISSIONS} is thrown
     */
    protected void checkGuestPermissions(Folder folder, ComparedFolderPermissions comparedPermissions) throws OXException {
        /*
         * check added guest permissions
         */
        if (comparedPermissions.hasAddedGuests()) {
            Permission addedAnonymousPermission = null;
            List<Integer> addedGuests = comparedPermissions.getAddedGuests();
            for (Integer guestID : addedGuests) {
                /*
                 * check guest permission
                 */
                GuestInfo guestInfo = comparedPermissions.getGuestInfo(guestID);
                Permission guestPermission = comparedPermissions.getAddedGuestPermission(guestID);
                checkGuestPermission(folder, guestPermission, guestInfo);
                if (isAnonymous(guestInfo)) {
                    /*
                     * allow only one anonymous permission
                     */
                    if (null == addedAnonymousPermission) {
                        addedAnonymousPermission = guestPermission;
                    } else {
                        throw invalidPermissions(folder, guestPermission);
                    }
                }
            }
            /*
             * check for an already existing anonymous permission if a new one should be added
             */
            if (null != addedAnonymousPermission && containsOriginalAnonymousPermission(comparedPermissions)) {
                throw invalidPermissions(folder, addedAnonymousPermission);
            }
        }
        /*
         * check modified guest permissions
         */
        if (comparedPermissions.hasModifiedGuests()) {
            for (Integer guestID : comparedPermissions.getModifiedGuests()) {
                /*
                 * check guest permission
                 */
                GuestInfo guestInfo = comparedPermissions.getGuestInfo(guestID);
                Permission guestPermission = comparedPermissions.getModifiedGuestPermission(guestID);
                checkGuestPermission(folder, guestPermission, guestInfo);
            }
        }
        /*
         * check new guest permissions
         */
        if (comparedPermissions.hasNewGuests()) {
            GuestPermission newAnonymousPermission = null;
            for (GuestPermission guestPermission : comparedPermissions.getNewGuestPermissions()) {
                if (guestPermission.getRecipient().getType() == RecipientType.ANONYMOUS) {
                    /*
                     * allow only one anonymous permission with "read-only" permission bits
                     */
                    checkIsLinkPermission(folder, guestPermission);
                    if (null == newAnonymousPermission) {
                        newAnonymousPermission = guestPermission;
                    } else {
                        throw invalidPermissions(folder, guestPermission);
                    }
                } else if (isReadOnlySharing(folder)) {
                    /*
                     * allow only "read-only" permissions for invited guests in non-infostore folders
                     */
                    checkReadOnly(folder, guestPermission);
                }
            }
            /*
             * check for an already existing anonymous permission if a new one should be added
             */
            if (null != newAnonymousPermission && containsOriginalAnonymousPermission(comparedPermissions)) {
                throw invalidPermissions(folder, newAnonymousPermission);
            }
        }
    }

    /**
     * Gets a value indicating whether the original permissions in the supplied compared permissions instance already contain an
     * "anonymous" entity one or not.
     *
     * @param comparedPermissions The compared permissions to check
     * @return <code>true</code> if there's an "anonymous" entity in the original permissions, <code>false</code>, otherwise
     */
    private static boolean containsOriginalAnonymousPermission(ComparedFolderPermissions comparedPermissions) throws OXException {
        Collection<Permission> originalPermissions = comparedPermissions.getOriginalPermissions();
        if (null != originalPermissions && 0 < originalPermissions.size()) {
            for (Permission originalPermission : originalPermissions) {
                if (false == originalPermission.isGroup() && !comparedPermissions.isSystemPermission(originalPermission)) {
                    GuestInfo guestInfo = comparedPermissions.getGuestInfo(originalPermission.getEntity());
                    if (null != guestInfo && isAnonymous(guestInfo)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a specific guest permission is allowed for a folder or not.
     *
     * @param folder The folder where the permission should be applied to
     * @param permission The guest permission
     * @param guestInfo The guest information for the added permission
     * @throws OXException
     */
    private static void checkGuestPermission(Folder folder, Permission permission, GuestInfo guestInfo) throws OXException {
        if (isAnonymous(guestInfo)) {
            /*
             * allow only one anonymous permission with "read-only" permission bits, matching the guest's fixed target
             */
            checkIsLinkPermission(folder, permission);
            if (isNotEqualsTarget(folder, guestInfo.getLinkTarget())) {
                throw invalidPermissions(folder, permission);
            }
        } else if (isReadOnlySharing(folder)) {
            /*
             * allow only "read-only" permissions for invited guests in non-infostore folders
             */
            checkReadOnly(folder, permission);
        }
    }

    /**
     * Gets a value indicating whether a folder's content type enforces "read-only" sharing for guests or not.
     *
     * @param folder The folder to check
     * @return <code>true</code> if the folder may be shared "read-only" for guest users only, <code>false</code>, otherwise
     */
    private static boolean isReadOnlySharing(Folder folder) {
        ContentType contentType = folder.getContentType();
        return CalendarContentType.getInstance().equals(contentType) || TaskContentType.getInstance().equals(contentType) ||
            ContactContentType.getInstance().equals(contentType);
    }

    /**
     * Creates a new {@link FolderExceptionErrorMessage#INVALID_PERMISSIONS} instance for the supplied folder and permission.
     *
     * @param folder The folder to create the invalid permission excpetion for
     * @param permission The folder to create the invalid permission excpetion for
     * @return A suitable invalid permission exception
     */
    private static OXException invalidPermissions(Folder folder, Permission permission) {
        return FolderExceptionErrorMessage.INVALID_PERMISSIONS.create(
            Permissions.createPermissionBits(permission), permission.getEntity(), folder.getID() == null ? folder.getName() : folder.getID());
    }

    private static void checkReadOnly(Folder folder, Permission p) throws OXException {
        boolean writeFolder = p.getFolderPermission() > Permission.READ_FOLDER;
        boolean writeItems = p.getWritePermission() > Permission.NO_PERMISSIONS;
        boolean deleteItems = p.getDeletePermission() > Permission.NO_PERMISSIONS;
        if (writeFolder || writeItems || deleteItems) {
            throw invalidPermissions(folder, p);
        }
    }

    private static void checkIsLinkPermission(Folder folder, Permission p) throws OXException {
        if (p.isAdmin() || p.isGroup() || p.getFolderPermission() != Permission.READ_FOLDER || p.getReadPermission() != Permission.READ_ALL_OBJECTS ||
            p.getWritePermission() != Permission.NO_PERMISSIONS || p.getDeletePermission() != Permission.NO_PERMISSIONS) {
            throw invalidPermissions(folder, p);
        }
    }

    private static boolean isNotEqualsTarget(Folder folder, ShareTarget target) {
        return !(new ShareTarget(folder.getContentType().getModule(), folder.getID()).equals(target));
    }

    private static boolean isAnonymous(GuestInfo guestInfo) {
        return guestInfo.getRecipientType() == RecipientType.ANONYMOUS;
    }

    private void hasVisibleSubfolderIDs(final Folder folder, final String treeId, final boolean all, final UserizedFolder userizedFolder, final boolean nullIsPublicAccess, final StorageParameters storageParameters, final java.util.Collection<FolderStorage> openedStorages) throws OXException {
        /*
         * Subfolders
         */
        final String[] subfolders = folder.getSubfolderIDs();
        String dummyId = null;
        if (null == subfolders) {
            if (nullIsPublicAccess) {
                /*
                 * A null value hints to a special folder; e.g. a system folder which contains subfolder for all users
                 */
                dummyId = DUMMY_ID;
            } else {
                /*
                 * Get appropriate storages and start transaction
                 */
                final String folderId = folder.getID();
                final FolderStorage[] ss = folderStorageDiscoverer.getFolderStoragesForParent(treeId, folderId);
                for (int i = 0; (null == dummyId) && i < ss.length; i++) {
                    final FolderStorage curStorage = ss[i];
                    boolean alreadyOpened = false;
                    final Iterator<FolderStorage> it = openedStorages.iterator();
                    for (int j = 0; !alreadyOpened && j < openedStorages.size(); j++) {
                        if (it.next().equals(curStorage)) {
                            alreadyOpened = true;
                        }
                    }
                    if (!alreadyOpened && curStorage.startTransaction(storageParameters, false)) {
                        openedStorages.add(curStorage);
                    }
                    final SortableId[] visibleIds = curStorage.getSubfolders(treeId, folderId, storageParameters);
                    if (visibleIds.length > 0) {
                        /*
                         * Found a storage which offers visible subfolder(s)
                         */
                        for (int j = 0; (null == dummyId) && j < visibleIds.length; j++) {
                            final String id = visibleIds[0].getId();
                            final Folder subfolder = curStorage.getFolder(treeId, id, storageParameters);
                            if (all || (subfolder.isSubscribed() || subfolder.hasSubscribedSubfolders())) {
                                final Permission p;
                                if (session == null) {
                                    p = CalculatePermission.calculate(subfolder, user, context, getAllowedContentTypes());
                                } else {
                                    p = CalculatePermission.calculate(subfolder, session, getAllowedContentTypes());
                                }
                                if (p.isVisible()) {
                                    dummyId = id;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            final int length = subfolders.length;
            if (length > 0) {
                /*
                 * Check until a visible subfolder is found in case of a global folder
                 */
                if (folder.isGlobalID()) {
                    for (int i = 0; (null == dummyId) && i < length; i++) {
                        try {
                            final String id = subfolders[i];
                            final FolderStorage tmp = getOpenedStorage(id, treeId, storageParameters, openedStorages);
                            /*
                             * Get subfolder from appropriate storage
                             */
                            final Folder subfolder = tmp.getFolder(treeId, id, storageParameters);
                            if ((all || (subfolder.isSubscribed() || subfolder.hasSubscribedSubfolders())) && CalculatePermission.isVisible(subfolder, getUser(), getContext(), getAllowedContentTypes())) {
                                dummyId = id;
                            }
                        } catch (OXException e) {
                            if ("FLD-0008".equals(e.getErrorCode())) {
                                // subfolder not / no longer found; try next
                                continue;
                            }
                            throw e;
                        }
                    }
                } else if (all || folder.hasSubscribedSubfolders()) { // User-only folder
                    dummyId = DUMMY_ID;
                }
            }
        }
        userizedFolder.setSubfolderIDs((null == dummyId) ? new String[0] : new String[] { dummyId });
    }

    private static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return (date + timeZone.getOffset(date));
    }

    /**
     * Appends or modifies a counter in the supplied (folder-) name, optionally in parenthesis. <p/>
     * For example, passing the name <code>test</code> and a counter of <code>2</code> will result in the string <code>test (2)</code>,
     * while the name <code>test (1)</code> would be changed to <code>test (2)</code>.
     *
     * @param name The name to enhance
     * @param counter The counter to append
     * @return The enhanced name
     */
    private static String enhance(String name, int counter, boolean useParenthesis) {
        if (null == name) {
            return name;
        }
        if (useParenthesis) {
            Matcher matcher = IS_NUMBERED_PARENTHESIS.matcher(name);
            if (matcher.find()) {
                return new StringBuilder(name).replace(matcher.start(), matcher.end(), '(' + String.valueOf(counter) + ')').toString();
            } else {
                return name + " (" + counter + ')';
            }
        } else {
            Matcher matcher = IS_NUMBERED.matcher(name);
            if (matcher.find()) {
                return new StringBuilder(name).replace(matcher.start(), matcher.end(), String.valueOf(counter)).toString();
            } else {
                return name + ' ' + counter;
            }
        }
    }

    protected static final ThreadPools.ExpectedExceptionFactory<OXException> FACTORY =
        new ThreadPools.ExpectedExceptionFactory<OXException>() {

            @Override
            public Class<OXException> getType() {
                return OXException.class;
            }

            @Override
            public OXException newUnexpectedError(final Throwable t) {
                return FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
            }
        };

    /**
     * Creates a newly allocated array containing all elements of specified array in the same order except <code>null</code> values.
     *
     * @param userizedFolders The array to trim
     * @return A newly allocated copy-array with <code>null</code> elements removed
     */
    protected static UserizedFolder[] trimArray(final UserizedFolder[] userizedFolders) {
        if (null == userizedFolders) {
            return new UserizedFolder[0];
        }
        final List<UserizedFolder> l = new ArrayList<UserizedFolder>(userizedFolders.length);
        for (final UserizedFolder uf : userizedFolders) {
            if (null != uf) {
                l.add(uf);
            }
        }
        return l.toArray(new UserizedFolder[l.size()]);
    }

}
