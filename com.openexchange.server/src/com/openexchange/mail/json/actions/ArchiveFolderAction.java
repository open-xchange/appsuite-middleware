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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.json.actions;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.ConfigurationService;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.search.ComparisonType;
import com.openexchange.mail.search.ReceivedDateTerm;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ArchiveFolderAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "archive_folder", description = "Moves mails to archive folder from given folder using certain criteria", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "days", description = "The days threshold to use."),
    @Parameter(name = "folder", description = "Object ID of the source folder.")
},
responseDescription = "A JSON true response.")
public final class ArchiveFolderAction extends AbstractMailAction {

    /**
     * Initializes a new {@link ArchiveFolderAction}.
     *
     * @param services
     */
    public ArchiveFolderAction(final ServiceLookup services) {
        super(services);
    }

    private static volatile Integer defaultDays;
    private static int defaultDays() {
        Integer tmp = defaultDays;
        if (null == tmp) {
            synchronized (ArchiveFolderAction.class) {
                tmp = defaultDays;
                if (null == tmp) {
                    int defaultInt = 90;
                    ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return defaultInt;
                    }
                    tmp = Integer.valueOf(service.getIntProperty("com.openexchange.mail.archive.defaultDays", defaultInt));
                    defaultDays = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            /*
             * Read in parameters
             */
            String days = req.getRequest().getParameter("days");
            if (Strings.isEmpty(days)) {
                days = Integer.toString(defaultDays());
            }
            String sourceFolder = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            // Check service
            final MailAccountStorageService service = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            if (null == service) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailAccountStorageService.class.getName());
            }
            final ServerSession session = req.getSession();
            final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(sourceFolder);
            final int accountId = fa.getAccountId();
            final MailAccount mailAccount = service.getMailAccount(accountId, session.getUserId(), session.getContextId());
            // Connect mail access
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Check archive full name
            char separator;
            String archiveFullname = mailAccount.getArchiveFullname();
            final String parentFullName;
            String archiveName;
            if (isEmpty(archiveFullname)) {
                archiveName = mailAccount.getArchive();
                boolean updateAccount = false;
                if (isEmpty(archiveName)) {
                    final User user = session.getUser();
                    if (!AJAXRequestDataTools.parseBoolParameter("useDefaultName", req.getRequest(), true)) {
                        final String i18nArchive = StringHelper.valueOf(user.getLocale()).getString(MailStrings.ARCHIVE);
                        throw MailExceptionCode.MISSING_DEFAULT_FOLDER_NAME.create(Category.CATEGORY_USER_INPUT, i18nArchive);
                    }
                    // Select default name for archive folder
                    archiveName = StringHelper.valueOf(user.getLocale()).getString(MailStrings.DEFAULT_ARCHIVE);
                    updateAccount = true;
                }
                final String prefix = mailAccess.getFolderStorage().getDefaultFolderPrefix();
                if (isEmpty(prefix)) {
                    separator = mailAccess.getFolderStorage().getFolder("INBOX").getSeparator();
                    archiveFullname = archiveName;
                    parentFullName = MailFolder.DEFAULT_FOLDER_ID;
                } else {
                    separator = prefix.charAt(prefix.length() - 1);
                    archiveFullname = new StringBuilder(prefix).append(archiveName).toString();
                    parentFullName = prefix.substring(0, prefix.length() - 1);
                }
                // Update mail account
                if (updateAccount) {
                    final MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
                    if (null != mass) {
                        final String af = archiveFullname;
                        ThreadPools.getThreadPool().submit(new AbstractTask<Void>() {

                            @Override
                            public Void call() throws Exception {
                                final MailAccountDescription mad = new MailAccountDescription();
                                mad.setId(accountId);
                                mad.setArchiveFullname(af);
                                mass.updateMailAccount(mad, EnumSet.of(Attribute.ARCHIVE_FULLNAME_LITERAL), session.getUserId(), session.getContextId(), session);
                                return null;
                            }
                        });
                    }
                }
            } else {
                separator = mailAccess.getFolderStorage().getFolder("INBOX").getSeparator();
                final int pos = archiveFullname.lastIndexOf(separator);
                if (pos > 0) {
                    parentFullName = archiveFullname.substring(0, pos);
                    archiveName = archiveFullname.substring(pos + 1);
                } else {
                    parentFullName = MailFolder.DEFAULT_FOLDER_ID;
                    archiveName = archiveFullname;
                }
            }
            if (!mailAccess.getFolderStorage().exists(archiveFullname)) {
                if (!AJAXRequestDataTools.parseBoolParameter("createIfAbsent", req.getRequest(), true)) {
                    throw MailExceptionCode.FOLDER_NOT_FOUND.create(archiveFullname);
                }
                final MailFolderDescription toCreate = new MailFolderDescription();
                toCreate.setAccountId(accountId);
                toCreate.setParentAccountId(accountId);
                toCreate.setParentFullname(parentFullName);
                toCreate.setExists(false);
                toCreate.setFullname(archiveFullname);
                toCreate.setName(archiveName);
                {
                    final DefaultMailPermission mp = new DefaultMailPermission();
                    mp.setEntity(session.getUserId());
                    final int p = MailPermission.ADMIN_PERMISSION;
                    mp.setAllPermission(p, p, p, p);
                    mp.setFolderAdmin(true);
                    mp.setGroupPermission(false);
                    toCreate.addPermission(mp);
                }
                mailAccess.getFolderStorage().createFolder(toCreate);
                CacheFolderStorage.getInstance().removeFromCache(parentFullName, "0", true, session);
            }

            // Move to archive folder
            Calendar cal = Calendar.getInstance(TimeZoneUtils.getTimeZone("UTC"));
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.add(Calendar.DATE, Strings.parsePositiveInt(days) * -1);

            ReceivedDateTerm term = new ReceivedDateTerm(ComparisonType.LESS_THAN, cal.getTime());
            MailMessage[] msgs = mailAccess.getMessageStorage().searchMessages(fa.getFullname(), null, MailSortField.RECEIVED_DATE, OrderDirection.DESC, term, new MailField[] { MailField.ID, MailField.RECEIVED_DATE });
            if (null == msgs || msgs.length <= 0) {
                return new AJAXRequestResult(Boolean.TRUE, "native");
            }

            Map<Integer, List<String>> map = new HashMap<Integer, List<String>>(4);
            for (MailMessage mailMessage : msgs) {
                Date receivedDate = mailMessage.getReceivedDate();
                cal.setTime(receivedDate);
                Integer year = Integer.valueOf(cal.get(Calendar.YEAR));
                List<String> ids = map.get(year);
                if (null == ids) {
                    ids = new LinkedList<String>();
                    map.put(year, ids);
                }
                ids.add(mailMessage.getMailId());
            }

            for (Map.Entry<Integer, List<String>> entry : map.entrySet() ) {
                String sYear = entry.getKey().toString();
                String fn = archiveFullname + separator + sYear;
                if (!mailAccess.getFolderStorage().exists(fn)) {
                    final MailFolderDescription toCreate = new MailFolderDescription();
                    toCreate.setAccountId(accountId);
                    toCreate.setParentAccountId(accountId);
                    toCreate.setParentFullname(archiveFullname);
                    toCreate.setExists(false);
                    toCreate.setFullname(fn);
                    toCreate.setName(sYear);
                    {
                        final DefaultMailPermission mp = new DefaultMailPermission();
                        mp.setEntity(session.getUserId());
                        final int p = MailPermission.ADMIN_PERMISSION;
                        mp.setAllPermission(p, p, p, p);
                        mp.setFolderAdmin(true);
                        mp.setGroupPermission(false);
                        toCreate.addPermission(mp);
                    }
                    mailAccess.getFolderStorage().createFolder(toCreate);
                    CacheFolderStorage.getInstance().removeFromCache(archiveFullname, "0", true, session);
                }

                List<String> ids = entry.getValue();
                mailAccess.getMessageStorage().moveMessages(fa.getFullname(), fn, ids.toArray(new String[ids.size()]), true);
            }

            return new AJAXRequestResult(Boolean.TRUE, "native");
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

}
