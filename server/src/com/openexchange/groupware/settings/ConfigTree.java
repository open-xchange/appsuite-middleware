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

package com.openexchange.groupware.settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.SettingException.Code;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;

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
        return new Setting(getSettingByPath(tree, path));
    }

    /**
     * This is the recursive method for resolving the path.
     * @param actual setting object that is already resolved from the path.
     * @param remainingPath the path that must be resolved.
     * @return a setting object.
     * @throws SettingException if the path cannot be resolved to a setting
     * object.
     */
    public static Setting getSettingByPath(final Setting actual,
        final String remainingPath) throws SettingException {
        Setting retval = actual;
        if (remainingPath.length() != 0) {
            String pathElement;
            String newRemaining;
            final int slashPos = remainingPath.indexOf('/');
            if (-1 == slashPos) {
                pathElement = remainingPath;
                newRemaining = "";
            } else {
                pathElement = remainingPath.substring(0, slashPos);
                newRemaining = remainingPath.substring(slashPos + 1);
            }
            final Setting child = actual.getElement(pathElement);
            if (null == child) {
                throw new SettingException(Code.UNKNOWN_PATH,
                    actual.getName() + '/' + pathElement);
            }
            retval = getSettingByPath(child, newRemaining);
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

    static void addSharedValue(final Setting actual, final String[] path,
        final IValueHandler shared) {
        if (null == actual) {
            addSharedValue(tree, path, shared);
        } else if (1 == path.length) {
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
            com.openexchange.groupware.settings.tree.mail.folder.DraftsSP3.class,
            com.openexchange.groupware.settings.tree.mail.folder.InboxSP3.class,
            com.openexchange.groupware.settings.tree.mail.folder.SentSP3.class,
            com.openexchange.groupware.settings.tree.mail.folder.SpamSP3.class,
            com.openexchange.groupware.settings.tree.mail.folder.TrashSP3.class,
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
            com.openexchange.groupware.settings.tree.modules.mail.folder.Drafts.class,
            com.openexchange.groupware.settings.tree.modules.mail.folder.Inbox.class,
            com.openexchange.groupware.settings.tree.modules.mail.folder.Sent.class,
            com.openexchange.groupware.settings.tree.modules.mail.folder.Spam.class,
            com.openexchange.groupware.settings.tree.modules.mail.folder.Trash.class,
            com.openexchange.groupware.settings.tree.modules.mail.Module.class,
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

        final Setting mail = new Setting("mail", -1, new SharedNode("mail"));
        tree.addElement(mail);

        mail.addElement(new Setting("defaultaddress", -1, new ReadOnlyValue() {
            public void getValue(final Session session, final Context ctx,
                final User user, UserConfiguration userConfig, final Setting setting) throws SettingException {
                setting.setSingleValue(user.getMail());
            }
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
        }));

        mail.addElement(new Setting("addresses", -1, new ReadOnlyValue() {
            public void getValue(final Session session, final Context ctx,
                final User user, UserConfiguration userConfig, final Setting setting) throws SettingException {
                final String[] aliases = user.getAliases();
                if (null != aliases) {
                    for (String alias : aliases) {
                        setting.addMultiValue(alias);
                    }
                }
            }
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
        }));

        mail.addElement(new Setting("sendaddress", -1, new IValueHandler() {
            public void getValue(final Session session, final Context ctx,
                final User user, UserConfiguration userConfig,
                final Setting setting) throws SettingException {
                final UserSettingMail settings = UserSettingMailStorage
                    .getInstance().getUserSettingMail(user.getId(), ctx);
                if (null != settings) {
                    setting.setSingleValue(settings.getSendAddr());
                }
            }
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            public boolean isWritable() {
                return true;
            }
            public void writeValue(final Context ctx, User user, final Setting setting) throws SettingException {
                final String newAlias = (String) setting.getSingleValue();
                final String[] aliases = user.getAliases();
                boolean found = false;
                for (int i = 0; aliases != null && i < aliases.length && !found; i++) {
                    found = aliases[i].equals(newAlias);
                }
                if (user.getMail().equals(newAlias)) {
                    found = true;
                }
                if (!found) {
                    throw new SettingException(Code.INVALID_VALUE, newAlias,
                        setting.getName());
                }
                final UserSettingMailStorage storage = UserSettingMailStorage
                    .getInstance();
                final UserSettingMail settings = storage.getUserSettingMail(
                    user.getId(), ctx);
                if (null != settings) {
                    settings.setSendAddr(newAlias);
                    try {
                        storage.saveUserSettingMail(settings, user.getId(), ctx);
                    } catch (OXException e) {
                        throw new SettingException(e);
                    }
                }
            }
            public int getId() {
                return -1;
            }
        }));

        mail.addElement(new Setting("colorquoted", -1, new AbstractMailFuncs() {
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            @Override
            protected Boolean isSet(final UserSettingMail settings) {
                return Boolean.valueOf(settings.isUseColorQuote());
            }
            @Override
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setUseColorQuote(Boolean.parseBoolean(value));
            }
        }));

        mail.addElement(new Setting("emoticons", -1, new AbstractMailFuncs() {
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            @Override
            protected Boolean isSet(final UserSettingMail settings) {
                return Boolean.valueOf(settings.isShowGraphicEmoticons());
            }
            @Override
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setShowGraphicEmoticons(Boolean.parseBoolean(value));
            }
        }));

        mail.addElement(new Setting("deletemail", -1, new AbstractMailFuncs() {
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            @Override
            protected Boolean isSet(final UserSettingMail settings) {
                return Boolean.valueOf(settings.isHardDeleteMsgs());
            }
            @Override
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setHardDeleteMsgs(Boolean.parseBoolean(value));
            }
        }));

        mail.addElement(new Setting("inlineattachments", -1,
            new AbstractMailFuncs() {
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            @Override
            protected Boolean isSet(final UserSettingMail settings) {
                return Boolean.valueOf(settings.isDisplayHtmlInlineContent());
            }
            @Override
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setDisplayHtmlInlineContent(
                    Boolean.parseBoolean(value));
            }
        }));

        mail.addElement(new Setting("appendmailtext", -1, new AbstractMailFuncs() {
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            @Override
            protected Boolean isSet(final UserSettingMail settings) {
                return Boolean.valueOf(!settings.isIgnoreOriginalMailTextOnReply());
            }
            @Override
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setIgnoreOriginalMailTextOnReply(
                    !Boolean.parseBoolean(value));
            }
        }));

        mail.addElement(new Setting("forwardmessage", -1, new AbstractMailFuncs() {
            private static final String INLINE = "Inline";
            private static final String ATTACHMENT = "Attachment";
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            @Override
            protected Object isSet(final UserSettingMail settings) {
                final String retval;
                if (settings.isForwardAsAttachment()) {
                    retval = ATTACHMENT;
                } else {
                    retval = INLINE;
                }
                return retval;
            }
            @Override
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setForwardAsAttachment(ATTACHMENT.equals(value));
            }
        }));

        mail.addElement(new Setting("linewrap", -1, new IValueHandler() {
            public void getValue(final Session session, final Context ctx,
                final User user, UserConfiguration userConfig,
                final Setting setting) throws SettingException {
                final UserSettingMail settings = UserSettingMailStorage
                    .getInstance().getUserSettingMail(session.getUserId(), ctx);
                if (null != settings) {
                    setting.setSingleValue(Integer.valueOf(settings
                        .getAutoLinebreak()));
                }
            }
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            public boolean isWritable() {
                return true;
            }
            public void writeValue(final Context ctx, final User user,
                final Setting setting) throws SettingException {
                final UserSettingMailStorage storage = UserSettingMailStorage
                    .getInstance();
                final UserSettingMail settings = storage.getUserSettingMail(
                        user.getId(), ctx);
                if (null != settings) {
                    try {
                        settings.setAutoLinebreak(Integer.parseInt(
                            (String) setting.getSingleValue()));
                        storage.saveUserSettingMail(settings, user.getId(),
                                ctx);
                    } catch (NumberFormatException e) {
                        throw new SettingException(Code.JSON_READ_ERROR, e);
                    } catch (OXException e) {
                        throw new SettingException(e);
                    }
                }
            }
            public int getId() {
                return -1;
            }
        }));

        mail.addElement(new Setting("vcard", -1, new AbstractMailFuncs() {
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            @Override
            protected Boolean isSet(final UserSettingMail settings) {
                return Boolean.valueOf(settings.isAppendVCard());
            }
            @Override
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setAppendVCard(Boolean.parseBoolean(value));
            }
        }));

        mail.addElement(new Setting("spambutton", -1, new ReadOnlyValue() {
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            public void getValue(final Session session, final Context ctx,
                final User user, UserConfiguration userConfig,
                final Setting setting) throws SettingException {
                final UserSettingMail settings = UserSettingMailStorage
                    .getInstance().getUserSettingMail(session.getUserId(), ctx);
                try {
                    setting.setSingleValue(Boolean.valueOf(settings
                        .isSpamEnabled()));
                } catch (final MailConfigException e) {
                    throw new SettingException(e);
                }
            }
        }));

        try {
            final Class< ? extends PreferencesItemService>[] clazzes = getClasses();
            final PreferencesItemService[] setups = new PreferencesItemService[clazzes.length];
            for (int i = 0; i < clazzes.length; i++) {
                setups[i] = clazzes[i].newInstance();
            }
            for (PreferencesItemService setup : setups) {
                addSharedValue(null, setup.getPath(), setup.getSharedValue());
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
        tree = null;
    }
}
