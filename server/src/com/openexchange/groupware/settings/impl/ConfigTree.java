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

package com.openexchange.groupware.settings.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.groupware.settings.SettingException.Code;

/**
 * This class is a container for the settings tree.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ConfigTree {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(ConfigTree.class);

    /**
     * Reference to the settings tree.
     */
    private static Setting tree;

    /**
     * Set of all identifiers for the database to check for duplicate ones.
     */
    private static Set<Integer> dbIdentifier = new HashSet<Integer>();

    /**
     * Prevent instantiation
     */
    private ConfigTree() {
        super();
    }

    /**
     * Resolves a path to the according setting object.
     * @param path Path to resolve.
     * @return a setting object.
     * @throws SettingException if the path cannot be resolved to a setting
     * object.
     */
    public static Setting getSettingByPath(final String path)
        throws SettingException {
        final String[] pathParts = path.split("/");
        return new Setting(getSettingByPath(tree, pathParts));
    }

    /**
     * This is the recursive method for resolving the path.
     * @param actual setting object that is already resolved from the path.
     * @param path the path that must be resolved.
     * @return a setting object.
     * @throws SettingException if the path cannot be resolved to a setting
     * object.
     */
    public static Setting getSettingByPath(final Setting actual,
        final String[] path) throws SettingException {
        Setting retval = actual;
        if (path.length != 0) {
            final String[] remainingPath = new String[path.length - 1];
            System.arraycopy(path, 1, remainingPath, 0, path.length - 1);
            final Setting child;
            if (0 == path[0].length()) {
                child = actual;
            } else {
                child = actual.getElement(path[0]);
            }
            if (null == child) {
                throw new SettingException(Code.UNKNOWN_PATH, actual.getName()
                    + '/' + path[0]);
            }
            retval = getSettingByPath(child, remainingPath);
        }
        return retval;
    }

    /**
     * Returns the corresponding reader for a setting.
     * @param setting shared setting.
     * @return the reader for the shared setting.
     */
    static IValueHandler getSharedValue(final Setting setting) {
        IValueHandler retval = null;
        if (setting.isLeaf()) {
            retval = setting.getShared();
        }
        return retval;
    }

    public static void addPreferencesItem(final PreferencesItemService item)
        throws SettingException {
        addSharedValue(null, item.getPath(), item.getSharedValue());
    }

    private static void addSharedValue(final Setting actual, final String[] path,
        final IValueHandler shared) throws SettingException {
        if (null == actual) {
            addSharedValue(tree, path, shared);
        } else if (1 == path.length) {
            if (-1 != shared.getId()) {
                final Integer tmp = Integer.valueOf(shared.getId());
                if (dbIdentifier.contains(tmp)) {
                    throw new SettingException(Code.DUPLICATE_ID, tmp);
                } else {
                    dbIdentifier.add(tmp);
                }
            }
            actual.addElement(new Setting(path[0], shared.getId(), shared));
        } else {
            Setting sub = actual.getElement(path[0]);
            if (null == sub) {
                final IValueHandler node = new SharedNode(path[0]);
                sub = new Setting(path[0], node.getId(), node);
                actual.addElement(sub);
            }
            final String[] subPath = new String[path.length - 1];
            System.arraycopy(path, 1, subPath, 0, subPath.length);
            addSharedValue(sub, subPath, shared);
        }
    }

    public static void removePreferencesItem(final PreferencesItemService item) {
        try {
            removeSharedValue(getSettingByPath(tree, item.getPath()));
        } catch (SettingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static void removeSharedValue(final Setting setting) {
        final Setting parent = setting.getParent();
        parent.removeElement(setting);
        if (-1 != setting.getId()) {
            dbIdentifier.remove(Integer.valueOf(setting.getId()));
        }
        if (parent.isLeaf() && parent != tree) {
            removeSharedValue(parent);
        }
    }

    private static Class< ? extends PreferencesItemService>[] getClasses() {
        return new Class[] {
            com.openexchange.groupware.settings.tree.AvailableModules.class,
            com.openexchange.groupware.settings.tree.CalendarNotification.class,
            com.openexchange.groupware.settings.tree.ContactID.class,
            com.openexchange.groupware.settings.tree.ContextID.class,
            com.openexchange.groupware.settings.tree.CurrentTime.class,
            com.openexchange.groupware.settings.tree.FastGUI.class,
            com.openexchange.groupware.settings.tree.folder.Calendar.class,
            com.openexchange.groupware.settings.tree.folder.Contacts.class,
            com.openexchange.groupware.settings.tree.folder.Infostore.class,
            com.openexchange.groupware.settings.tree.folder.Tasks.class,
            com.openexchange.groupware.settings.tree.GUI.class,
            com.openexchange.groupware.settings.tree.Identifier.class,
            com.openexchange.groupware.settings.tree.Language.class,
            com.openexchange.groupware.settings.tree.mail.AppendMailTextSP3.class,
            com.openexchange.groupware.settings.tree.mail.AddressesSP3.class,
            com.openexchange.groupware.settings.tree.mail.ColorquotedSP3.class,
            com.openexchange.groupware.settings.tree.mail.DefaultAddressSP3.class,
            com.openexchange.groupware.settings.tree.mail.DeleteMailSP3.class,
            com.openexchange.groupware.settings.tree.mail.EmoticonsSP3.class,
            com.openexchange.groupware.settings.tree.mail.folder.DraftsSP3.class,
            com.openexchange.groupware.settings.tree.mail.folder.InboxSP3.class,
            com.openexchange.groupware.settings.tree.mail.folder.SentSP3.class,
            com.openexchange.groupware.settings.tree.mail.folder.SpamSP3.class,
            com.openexchange.groupware.settings.tree.mail.folder.TrashSP3.class,
            com.openexchange.groupware.settings.tree.mail.ForwardMessageSP3.class,
            com.openexchange.groupware.settings.tree.mail.InlineAttachmentsSP3.class,
            com.openexchange.groupware.settings.tree.mail.LineWrapSP3.class,
            com.openexchange.groupware.settings.tree.mail.SendAddressSP3.class,
            com.openexchange.groupware.settings.tree.mail.SpamButtonSP3.class,
            com.openexchange.groupware.settings.tree.mail.VCardSP3.class,
            com.openexchange.groupware.settings.tree.MaxUploadIdleTimeout.class,
            com.openexchange.groupware.settings.tree.modules.calendar.Module.class,
            com.openexchange.groupware.settings.tree.modules.calendar.CalendarConflict.class,
            com.openexchange.groupware.settings.tree.modules.calendar.CalendarFreeBusy.class,
            com.openexchange.groupware.settings.tree.modules.calendar.CalendarTeamView.class,
            com.openexchange.groupware.settings.tree.modules.contacts.Module.class,
            com.openexchange.groupware.settings.tree.modules.folder.PublicFolders.class,
            com.openexchange.groupware.settings.tree.modules.folder.SharedFolders.class,
            com.openexchange.groupware.settings.tree.modules.infostore.Module.class,
            com.openexchange.groupware.settings.tree.modules.interfaces.ICal.class,
            com.openexchange.groupware.settings.tree.modules.interfaces.SyncML.class,
            com.openexchange.groupware.settings.tree.modules.interfaces.VCard.class,
            com.openexchange.groupware.settings.tree.modules.mail.Addresses.class,
            com.openexchange.groupware.settings.tree.modules.mail.AppendMailText.class,
            com.openexchange.groupware.settings.tree.modules.mail.Colorquoted.class,
            com.openexchange.groupware.settings.tree.modules.mail.DefaultAddress.class,
            com.openexchange.groupware.settings.tree.modules.mail.DeleteMail.class,
            com.openexchange.groupware.settings.tree.modules.mail.Emoticons.class,
            com.openexchange.groupware.settings.tree.modules.mail.folder.Drafts.class,
            com.openexchange.groupware.settings.tree.modules.mail.folder.Inbox.class,
            com.openexchange.groupware.settings.tree.modules.mail.folder.Sent.class,
            com.openexchange.groupware.settings.tree.modules.mail.folder.Spam.class,
            com.openexchange.groupware.settings.tree.modules.mail.folder.Trash.class,
            com.openexchange.groupware.settings.tree.modules.mail.ForwardMessage.class,
            com.openexchange.groupware.settings.tree.modules.mail.InlineAttachments.class,
            com.openexchange.groupware.settings.tree.modules.mail.LineWrap.class,
            com.openexchange.groupware.settings.tree.modules.mail.Module.class,
            com.openexchange.groupware.settings.tree.modules.mail.SendAddress.class,
            com.openexchange.groupware.settings.tree.modules.mail.SpamButton.class,
            com.openexchange.groupware.settings.tree.modules.mail.VCard.class,
            com.openexchange.groupware.settings.tree.modules.portal.Module.class,
            com.openexchange.groupware.settings.tree.modules.tasks.Module.class,
            com.openexchange.groupware.settings.tree.modules.tasks.DelegateTasks.class,
            com.openexchange.groupware.settings.tree.participants.ShowWithoutEmail.class,
            com.openexchange.groupware.settings.tree.ReloadTimes.class,
            com.openexchange.groupware.settings.tree.ServerVersion.class,
            com.openexchange.groupware.settings.tree.TaskNotification.class,
            com.openexchange.groupware.settings.tree.TimeZone.class
        };
    }

    /**
     * Initializes the configuration tree.
     * @throws SettingException if initializing doesn't work.
     */
    static void init() throws SettingException {
        if (null != tree) {
            LOG.error("Duplicate initialization of configuration tree.");
            return;
        }
        tree = new Setting("", -1, new SharedNode(""));
        try {
            final Class< ? extends PreferencesItemService>[] clazzes = getClasses();
            final PreferencesItemService[] items = new PreferencesItemService[clazzes.length];
            for (int i = 0; i < clazzes.length; i++) {
                items[i] = clazzes[i].newInstance();
            }
            for (PreferencesItemService item : items) {
                addPreferencesItem(item);
            }
        } catch (InstantiationException e) {
            throw new SettingException(Code.INIT, e);
        } catch (IllegalAccessException e) {
            throw new SettingException(Code.INIT, e);
        }
    }

    static void stop() {
        if (null == tree) {
            LOG.error("Duplicate shutdown of configuration tree.");
            return;
        }
        try {
            final Class< ? extends PreferencesItemService>[] clazzes = getClasses();
            final PreferencesItemService[] items = new PreferencesItemService[clazzes.length];
            for (int i = 0; i < clazzes.length; i++) {
                items[i] = clazzes[i].newInstance();
            }
            for (PreferencesItemService item : items) {
                removePreferencesItem(item);
            }
        } catch (InstantiationException e) {
            final SettingException se = new SettingException(Code.INIT, e);
            LOG.error(se.getMessage(), se);
        } catch (IllegalAccessException e) {
            final SettingException se = new SettingException(Code.INIT, e);
            LOG.error(se.getMessage(), se);
        }
        tree = null;
    }
}
