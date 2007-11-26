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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.settings.SettingException.Code;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailInterface;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.impl.Version;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;

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
     * Path name containing the timezone of the user.
     */
    public static final String TIMEZONE = "timezone";

    /**
     * Reference to the settings tree.
     */
    private static Setting tree;

    /**
     * A map for the readers for shared settings.
     */
    private static Map<String, SharedValue> readers;

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
        Setting retval = tree;
        retval = getSettingByPath(retval, path);
        try {
            retval = (Setting) retval.clone();
        } catch (CloneNotSupportedException e) {
            throw new SettingException(SettingException.Code.CLONE, e);
        }
        return retval;
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
    static SharedValue getSharedValue(final Setting setting) {
        SharedValue retval = null;
        if (setting.isLeaf()) {
            retval = readers.get(setting.getPath());
        }
        return retval;
    }

    /**
     * This class contains the shared functions for all user settings.
     */
    public abstract static class AbstractUserFuncs implements SharedValue {

        /**
         * {@inheritDoc}
         */
        public void writeValue(final Session session,
            final Setting setting) throws SettingException {
            try {
                final UserStorage storage = UserStorage.getInstance();
                final User oldUser = storage.getUser(session.getUserId(),
                    session.getContext());
                final UserImpl user = new UserImpl(oldUser);
                setValue(user, (String) setting.getSingleValue());
                storage.updateUser(user, session.getContext());
            } catch (LdapException e) {
                throw new SettingException(e);
            }
        }

        /**
         * @param user in this user object the value should be set.
         * @param value the value to set.
         * @throws SettingException if writing of the value fails.
         */
        protected abstract void setValue(UserImpl user, String value)
            throws SettingException;
    }

    private static Class< ? extends SettingSetup>[] getClasses() {
        return (Class< ? extends SettingSetup>[]) new Class[] {
            com.openexchange.groupware.settings.tree.FastGUI.class,
            com.openexchange.groupware.settings.tree.GUI.class,
            com.openexchange.groupware.settings.tree.Identifier.class,
            com.openexchange.groupware.settings.tree.Language.class,
            com.openexchange.groupware.settings.tree.MaxUploadIdleTimeout.class,
            com.openexchange.groupware.settings.tree.Modules.class,
            com.openexchange.groupware.settings.tree.modules.Calendar.class,
            com.openexchange.groupware.settings.tree.modules.calendar.Module.class,
            com.openexchange.groupware.settings.tree.modules.calendar.CalendarConflict.class,
            com.openexchange.groupware.settings.tree.modules.calendar.CalendarFreeBusy.class,
            com.openexchange.groupware.settings.tree.modules.calendar.CalendarTeamView.class,
            com.openexchange.groupware.settings.tree.modules.Contacts.class,
            com.openexchange.groupware.settings.tree.modules.contacts.Module.class,
            com.openexchange.groupware.settings.tree.modules.Folder.class,
            com.openexchange.groupware.settings.tree.modules.folder.PublicFolders.class,
            com.openexchange.groupware.settings.tree.modules.folder.SharedFolders.class,
            com.openexchange.groupware.settings.tree.modules.Infostore.class,
            com.openexchange.groupware.settings.tree.modules.infostore.Module.class,
            com.openexchange.groupware.settings.tree.modules.Interfaces.class,
            com.openexchange.groupware.settings.tree.modules.interfaces.ICal.class,
            com.openexchange.groupware.settings.tree.modules.interfaces.SyncML.class,
            com.openexchange.groupware.settings.tree.modules.interfaces.VCard.class,
            com.openexchange.groupware.settings.tree.modules.Mail.class,
            com.openexchange.groupware.settings.tree.modules.mail.Module.class,
            com.openexchange.groupware.settings.tree.modules.Portal.class,
            com.openexchange.groupware.settings.tree.modules.portal.Module.class,
            com.openexchange.groupware.settings.tree.modules.Tasks.class,
            com.openexchange.groupware.settings.tree.modules.tasks.Module.class,
            com.openexchange.groupware.settings.tree.modules.tasks.DelegateTasks.class,
            com.openexchange.groupware.settings.tree.Participants.class,
            com.openexchange.groupware.settings.tree.participants.ShowWithoutEmail.class,
            com.openexchange.groupware.settings.tree.TaskNotification.class
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
        tree = new Setting("", true);
        tree.setId(-1);

        final Setting timezone = new Setting(TIMEZONE, true);
        timezone.setId(-1);
        tree.addElement(timezone);

        final Setting calNotification = new Setting("calendarnotification",
            true);
        calNotification.setId(-1);
        tree.addElement(calNotification);

        final Setting reloadTimes = new Setting("reloadtimes", true);
        reloadTimes.setId(-1);
        tree.addElement(reloadTimes);

        final Setting serverVersion = new Setting("serverVersion", true);
        serverVersion.setId(-1);
        tree.addElement(serverVersion);

        final Setting currentTime = new Setting("currentTime", true);
        currentTime.setId(-1);
        tree.addElement(currentTime);

        final Setting folder = new Setting("folder", true);
        folder.setId(-1);
        tree.addElement(folder);

        final Setting tasks = new Setting("tasks", true);
        tasks.setId(-1);
        folder.addElement(tasks);

        final Setting calendar = new Setting("calendar", true);
        calendar.setId(-1);
        folder.addElement(calendar);

        final Setting contacts = new Setting("contacts", true);
        contacts.setId(-1);
        folder.addElement(contacts);

        final Setting infostore = new Setting("infostore", true);
        infostore.setId(-1);
        folder.addElement(infostore);

        final Setting mail = new Setting("mail", true);
        mail.setId(-1);
        tree.addElement(mail);

        final Setting defaultaddress = new Setting("defaultaddress", true);
        defaultaddress.setId(-1);
        mail.addElement(defaultaddress);

        final Setting addresses = new Setting("addresses", true);
        addresses.setId(-1);
        mail.addElement(addresses);

        final Setting sendaddress = new Setting("sendaddress", true);
        sendaddress.setId(-1);
        mail.addElement(sendaddress);

        final Setting mailfolder = new Setting("folder", true);
        mailfolder.setId(-1);
        mail.addElement(mailfolder);

        final Setting inbox = new Setting("inbox", true);
        inbox.setId(-1);
        mailfolder.addElement(inbox);

        final Setting drafts = new Setting("drafts", true);
        drafts.setId(-1);
        mailfolder.addElement(drafts);

        final Setting sent = new Setting("sent", true);
        sent.setId(-1);
        mailfolder.addElement(sent);

        final Setting spam = new Setting("spam", true);
        spam.setId(-1);
        mailfolder.addElement(spam);

        final Setting trash = new Setting("trash", true);
        trash.setId(-1);
        mailfolder.addElement(trash);

        final Setting colorquoted = new Setting("colorquoted", true);
        colorquoted.setId(-1);
        mail.addElement(colorquoted);

        final Setting emoticons = new Setting("emoticons", true);
        emoticons.setId(-1);
        mail.addElement(emoticons);

        final Setting deletemail = new Setting("deletemail", true);
        deletemail.setId(-1);
        mail.addElement(deletemail);

        final Setting inlineattachments = new Setting("inlineattachments",
            true);
        inlineattachments.setId(-1);
        mail.addElement(inlineattachments);

        final Setting appendmailtext = new Setting("appendmailtext", true);
        appendmailtext.setId(-1);
        mail.addElement(appendmailtext);

        final Setting forwardmessage = new Setting("forwardmessage", true);
        forwardmessage.setId(-1);
        mail.addElement(forwardmessage);

        final Setting linewrap = new Setting("linewrap", true);
        linewrap.setId(-1);
        mail.addElement(linewrap);

        final Setting vcard = new Setting("vcard", true);
        vcard.setId(-1);
        mail.addElement(vcard);

        final Setting spamButton = new Setting("spambutton", true);
        spamButton.setId(-1);
        mail.addElement(spamButton);

        final Map<String, SharedValue> tmp = new HashMap<String, SharedValue>();
        tmp.put(timezone.getPath(), new AbstractUserFuncs() {
            public void getValue(final Session session,
                final Setting setting) {
                setting.setSingleValue(UserStorage.getStorageUser(session.getUserId(), session.getContext()).getTimeZone());
            }
            public boolean isAvailable(final Session session) {
                return true;
            }
            public boolean isWritable() {
                return true;
            }
            protected void setValue(final UserImpl user, final String value) {
                user.setTimeZone(value);
            }
        });
        tmp.put(calNotification.getPath(), new AbstractMailFuncs() {
            public boolean isAvailable(final Session session) {
                final UserConfiguration config = UserConfigurationStorage.getInstance().getUserConfigurationSafe(
						session.getUserId(), session.getContext());
				return config.hasWebMail() && config.hasCalendar();
            }
            @Override
            protected Object isSet(final UserSettingMail settings) {
                return settings.isNotifyAppointments();
            }
            @Override
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setNotifyAppointments(Boolean.parseBoolean(value));
            }
        });
        tmp.put(reloadTimes.getPath(), new ReadOnlyValue() {
            private static final int MINUTE = 60 * 1000;
            public boolean isAvailable(final Session session) {
                return true;
            }
            public void getValue(final Session session,
                final Setting setting) {
                setting.addMultiValue(0); // Never
                setting.addMultiValue(5 * MINUTE); // 5 Minutes
                setting.addMultiValue(10 * MINUTE); // 10 Minutes
                setting.addMultiValue(15 * MINUTE); // 15 Minutes
                setting.addMultiValue(30 * MINUTE); // 30 Minutes
            }
        });
        tmp.put(serverVersion.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
                return true;
            }
            public void getValue(final Session session,
                final Setting setting) {
                setting.setSingleValue(Version.VERSION_STRING);
            }
        });
        tmp.put(currentTime.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
                return true;
            }
            public void getValue(final Session session,
                final Setting setting) {
                long time = System.currentTimeMillis();
                final TimeZone zone = TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(),
						session.getContext()).getTimeZone());
				time  += zone.getOffset(time);
                setting.setSingleValue(time);
            }
        });
        tmp.put(tasks.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasTask();
			}
            public void getValue(final Session session,
                final Setting setting) throws SettingException {
                final OXFolderAccess acc = new OXFolderAccess(session
                    .getContext());
                try {
                    setting.setSingleValue(acc.getDefaultFolder(session
                        .getUserId(), FolderObject.TASK)
                        .getObjectID());
                } catch (OXException e) {
                    throw new SettingException(e);
                }
            }
        });
        tmp.put(calendar.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasCalendar();
			}
            public void getValue(final Session session,
                final Setting setting) throws SettingException {
                final OXFolderAccess acc = new OXFolderAccess(session
                    .getContext());
                try {
                    setting.setSingleValue(acc.getDefaultFolder(session
                        .getUserId(), FolderObject.CALENDAR)
                        .getObjectID());
                } catch (OXException e) {
                    throw new SettingException(e);
                }
            }
        });
        tmp.put(contacts.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasContact();
			}
            public void getValue(final Session session,
                final Setting setting) throws SettingException {
                final OXFolderAccess acc = new OXFolderAccess(session
                    .getContext());
                try {
                    setting.setSingleValue(acc.getDefaultFolder(session
                        .getUserId(), FolderObject.CONTACT)
                        .getObjectID());
                } catch (OXException e) {
                    throw new SettingException(e);
                }
            }
        });
        tmp.put(infostore.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasInfostore();
			}
            public void getValue(final Session session,
                final Setting setting) throws SettingException {
                final OXFolderAccess acc = new OXFolderAccess(session
                    .getContext());
                try {
                    setting.setSingleValue(acc.getDefaultFolder(session
                        .getUserId(), FolderObject.INFOSTORE)
                        .getObjectID());
                } catch (OXException e) {
                    throw new SettingException(e);
                }
            }
        });
        tmp.put(defaultaddress.getPath(), new AbstractUserFuncs() {
            public void getValue(final Session session,
                final Setting setting) {
                setting.setSingleValue(UserStorage.getStorageUser(session.getUserId(), session.getContext()).getMail());
            }
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            public boolean isWritable() {
                return false;
            }
            protected void setValue(final UserImpl user, final String value)
                throws SettingException {
                throw new SettingException(Code.NO_WRITE,
                    defaultaddress.getPath());
            }
        });
        tmp.put(addresses.getPath(), new AbstractUserFuncs() {
            public void getValue(final Session session,
                final Setting setting) {
                final String[] aliases = UserStorage.getStorageUser(session.getUserId(), session.getContext()).getAliases();
                if (null != aliases) {
                    for (String alias : UserStorage.getStorageUser(session.getUserId(), session.getContext()).getAliases()) {
                        setting.addMultiValue(alias);
                    }
                }
            }
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            public boolean isWritable() {
                return false;
            }
            protected void setValue(final UserImpl user, final String value)
                throws SettingException {
                throw new SettingException(Code.NO_WRITE, addresses.getPath());
            }
        });
        tmp.put(sendaddress.getPath(), new SharedValue() {
			public void getValue(final Session session, final Setting setting) throws SettingException {
				final UserSettingMail settings = UserSettingMailStorage.getInstance().getUserSettingMail(
						session.getUserId(), session.getContext());
				setting.setSingleValue(settings.getSendAddr());
			}

			public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}

			public boolean isWritable() {
				return true;
			}

			public void writeValue(final Session session, final Setting setting) throws SettingException {
				final UserSettingMail settings = UserSettingMailStorage.getInstance().getUserSettingMail(
						session.getUserId(), session.getContext());
				settings.setSendAddr((String) setting.getSingleValue());
				try {
					UserSettingMailStorage.getInstance().saveUserSettingMail(settings, session.getUserId(),
							session.getContext());
				} catch (OXException e) {
					throw new SettingException(e);
				}
			}
		});
        tmp.put(inbox.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            public void getValue(final Session session,
                final Setting setting) throws SettingException {
                MailInterface mail = null;
                try {
                    mail = MailInterface.getInstance(session);
                    setting.setSingleValue(mail.getInboxFolder());
                } catch (MailException e) {
                    throw new SettingException(e);
                } finally {
                    if (mail != null) {
                        try {
                            mail.close(true);
                        } catch (MailException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });
        tmp.put(drafts.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            public void getValue(final Session session,
                final Setting setting) throws SettingException {
                MailInterface mail = null;
                try {
                    mail = MailInterface.getInstance(session);
                    setting.setSingleValue(mail.getDraftsFolder());
                } catch (MailException e) {
                    throw new SettingException(e);
                } finally {
                    if (mail != null) {
                        try {
                            mail.close(true);
                        } catch (MailException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });
        tmp.put(sent.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            public void getValue(final Session session,
                final Setting setting) throws SettingException {
                MailInterface mail = null;
                try {
                    mail = MailInterface.getInstance(session);
                    setting.setSingleValue(mail.getSentFolder());
                } catch (MailException e) {
                    throw new SettingException(e);
                } finally {
                    if (mail != null) {
                        try {
                            mail.close(true);
                        } catch (MailException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });
        tmp.put(spam.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            public void getValue(final Session session,
                final Setting setting) throws SettingException {
                MailInterface mail = null;
                try {
                    mail = MailInterface.getInstance(session);
                    setting.setSingleValue(mail.getSpamFolder());
                } catch (MailException e) {
                    throw new SettingException(e);
                } finally {
                    if (mail != null) {
                        try {
                            mail.close(true);
                        } catch (MailException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });
        tmp.put(trash.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            public void getValue(final Session session,
                final Setting setting) throws SettingException {
                MailInterface mail = null;
                try {
                    mail = MailInterface.getInstance(session);
                    setting.setSingleValue(mail.getTrashFolder());
                } catch (MailException e) {
                    throw new SettingException(e);
                } finally {
                    if (mail != null) {
                        try {
                            mail.close(true);
                        } catch (MailException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });
        tmp.put(colorquoted.getPath(), new AbstractMailFuncs() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            protected Object isSet(final UserSettingMail settings) {
                return settings.isUseColorQuote();
            }
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setUseColorQuote(Boolean.parseBoolean(value));
            }
        });
        tmp.put(emoticons.getPath(), new AbstractMailFuncs() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            protected Object isSet(final UserSettingMail settings) {
                return settings.isShowGraphicEmoticons();
            }
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setShowGraphicEmoticons(Boolean.parseBoolean(value));
            }
        });
        tmp.put(deletemail.getPath(), new AbstractMailFuncs() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            protected Object isSet(final UserSettingMail settings) {
                return settings.isHardDeleteMsgs();
            }
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setHardDeleteMsgs(Boolean.parseBoolean(value));
            }
        });
        tmp.put(inlineattachments.getPath(), new AbstractMailFuncs() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            protected Object isSet(final UserSettingMail settings) {
                return settings.isDisplayHtmlInlineContent();
            }
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setDisplayHtmlInlineContent(
                    Boolean.parseBoolean(value));
            }
        });
        tmp.put(appendmailtext.getPath(), new AbstractMailFuncs() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            protected Object isSet(final UserSettingMail settings) {
                return !settings.isIgnoreOriginalMailTextOnReply();
            }
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setIgnoreOriginalMailTextOnReply(
                    !Boolean.parseBoolean(value));
            }
        });
        tmp.put(forwardmessage.getPath(), new AbstractMailFuncs() {
            private static final String INLINE = "Inline";
            private static final String ATTACHMENT = "Attachment";
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            protected Object isSet(final UserSettingMail settings) {
                final String retval;
                if (settings.isForwardAsAttachment()) {
                    retval = ATTACHMENT;
                } else {
                    retval = INLINE;
                }
                return retval;
            }
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setForwardAsAttachment(ATTACHMENT.equals(value));
            }
        });
        tmp.put(linewrap.getPath(), new SharedValue() {
			public void getValue(final Session session, final Setting setting) throws SettingException {
				final UserSettingMail settings = UserSettingMailStorage.getInstance().getUserSettingMail(
						session.getUserId(), session.getContext());
				setting.setSingleValue(settings.getAutoLinebreak());
			}

			public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}

			public boolean isWritable() {
				return true;
			}

			public void writeValue(final Session session, final Setting setting) throws SettingException {
				final UserSettingMail settings = UserSettingMailStorage.getInstance().getUserSettingMail(
						session.getUserId(), session.getContext());
				try {
					settings.setAutoLinebreak(Integer.parseInt((String) setting.getSingleValue()));
					UserSettingMailStorage.getInstance().saveUserSettingMail(settings, session.getUserId(),
							session.getContext());
				} catch (NumberFormatException e) {
					throw new SettingException(Code.JSON_READ_ERROR, e);
				} catch (OXException e) {
					throw new SettingException(e);
				}
			}
		});
        tmp.put(vcard.getPath(), new AbstractMailFuncs() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            protected Object isSet(final UserSettingMail settings) {
                return settings.isAppendVCard();
            }
            protected void setValue(final UserSettingMail settings,
                final String value) {
                settings.setAppendVCard(Boolean.parseBoolean(value));
            }
        });
        tmp.put(spamButton.getPath(), new ReadOnlyValue() {
            public boolean isAvailable(final Session session) {
				return UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasWebMail();
			}
            public void getValue(final Session session,
                final Setting setting) throws SettingException {
				final UserSettingMail settings = UserSettingMailStorage.getInstance().getUserSettingMail(
						session.getUserId(), session.getContext());
				try {
					setting.setSingleValue(settings.isSpamEnabled());
				} catch (final MailConfigException e) {
					throw new SettingException(e);
				}
			}
        });
        
        try {
            final Class< ? extends SettingSetup>[] clazzes = getClasses();
            final SettingSetup[] setups = new SettingSetup[clazzes.length];
            for (int i = 0; i < clazzes.length; i++) {
                setups[i] = clazzes[i].newInstance();
            }
            Arrays.sort(setups, new Comparator<SettingSetup>() {
                public int compare(final SettingSetup o1,
                    final SettingSetup o2) {
                    return o1.getPath().compareTo(o2.getPath());
                }
            });
            for (SettingSetup setup : setups) {
                // Define parent.
                final Setting setting = getSettingByPath(tree, setup.getPath());
                final Setting subSetting = setup.getSetting();
                // Connect tree.
                setting.addElement(subSetting);
                // Map shared value.
                final SharedValue shared = setup.getSharedValue();
                if (null != shared) {
                    tmp.put(subSetting.getPath(), shared);
                }
            }
        } catch (InstantiationException e) {
            throw new SettingException(Code.INIT, e);
        } catch (IllegalAccessException e) {
            throw new SettingException(Code.INIT, e);
        } catch (SettingException e) {
            throw new SettingException(Code.INIT, e);
        }
        
        readers = Collections.unmodifiableMap(tmp);
    }

    static void stop() {
        if (null == tree) {
            LOG.error("Duplicate shutdown of configuration tree.");
            return;
        }
        readers = null;
        tree = null;
    }
}
