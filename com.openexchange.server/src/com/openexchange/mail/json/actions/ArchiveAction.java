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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.java.Reference;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ArchiveAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "archive", description = "Moves mails to archive folder", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Object ID of the requested mail."),
    @Parameter(name = "folder", description = "Object ID of the source folder.")
}, requestBody = "A JSON object containing the id of the destination folder inside the \"folder_id\" field: e.g.: {\"folder_id\": 1376}.",
responseDescription = "A JSON true response.")
public final class ArchiveAction extends AbstractArchiveMailAction {

    /**
     * Initializes a new {@link ArchiveAction}.
     *
     * @param services
     */
    public ArchiveAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        try {
            // Read in parameters
            final ServerSession session = req.getSession();

            MailAccountStorageService service = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            if (null == service) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailAccountStorageService.class.getName());
            }

            JSONArray jArray = ((JSONArray) req.getRequest().getData());
            if (null == jArray) {
                throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
            }

            String sourceFolder = req.getParameter(AJAXServlet.PARAMETER_FOLDERID);

            if (null == sourceFolder) {
                // Expect array of objects
                TIntObjectMap<Map<String, List<String>>> m = new TIntObjectHashMap<Map<String, List<String>>>(2);

                // Parse JSON body
                int length = jArray.length();
                for (int i = 0; i < length; i++) {
                    JSONObject jObject = jArray.getJSONObject(i);

                    String folder = jObject.getString(AJAXServlet.PARAMETER_FOLDERID);
                    String id = jObject.getString(AJAXServlet.PARAMETER_ID);

                    FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(folder);
                    int accountId = fa.getAccountId();

                    Map<String, List<String>> map = m.get(accountId);
                    if (null == map) {
                        map = new HashMap<String, List<String>>();
                        m.put(accountId, map);
                    }

                    String fullName = fa.getFullname();
                    List<String> list = map.get(fullName);
                    if (null == list) {
                        list = new LinkedList<String>();
                        map.put(fullName, list);
                    }

                    list.add(id);
                }

                // Iterate map
                final Reference<Exception> exceptionRef = new Reference<Exception>();
                final Calendar cal = Calendar.getInstance(TimeZoneUtils.getTimeZone("UTC"));
                m.forEachEntry(new TIntObjectProcedure<Map<String, List<String>>>() {

                    @Override
                    public boolean execute(int accountId, Map<String, List<String>> mapping) {
                        boolean proceed = false;
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            // Connect mail access
                            mailAccess = MailAccess.getInstance(session, accountId);
                            mailAccess.connect();

                            // Check archive full name
                            int[] separatorRef = new int[1];
                            String archiveFullname = checkArchiveFullNameFor(mailAccess, req, separatorRef);
                            char separator = (char) separatorRef[0];

                            // Move to archive folder
                            for (Map.Entry<String, List<String>> mappingEntry : mapping.entrySet()) {
                                String fullName = mappingEntry.getKey();

                                // Check location
                                if (!fullName.equals(archiveFullname) && !fullName.startsWith(archiveFullname + separator)) {
                                    List<String> mailIds = mappingEntry.getValue();

                                    MailMessage[] msgs = mailAccess.getMessageStorage().getMessages(fullName, mailIds.toArray(new String[mailIds.size()]), new MailField[] { MailField.ID, MailField.RECEIVED_DATE});
                                    if (null == msgs || msgs.length <= 0) {
                                        return true;
                                    }

                                    move2Archive(msgs, fullName, archiveFullname, separator, cal, mailAccess);
                                }
                            }

                            proceed = true;
                        } catch (Exception e) {
                            exceptionRef.setValue(e);
                        } finally {
                            if (null != mailAccess) {
                                mailAccess.close(true);
                            }
                        }
                        return proceed;
                    }
                });
            } else {
                // Expect array of identifiers
                FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(sourceFolder);
                int accountId = fa.getAccountId();

                MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                try {
                    // Connect mail access
                    mailAccess = MailAccess.getInstance(session, accountId);
                    mailAccess.connect();

                    // Check archive full name
                    int[] separatorRef = new int[1];
                    String archiveFullname = checkArchiveFullNameFor(mailAccess, req, separatorRef);
                    char separator = (char) separatorRef[0];

                    // Check location
                    {
                        String fullName = fa.getFullname();
                        if (fullName.equals(archiveFullname) || fullName.startsWith(archiveFullname + separator)) {
                            return new AJAXRequestResult(Boolean.TRUE, "native");
                        }
                    }

                    int length = jArray.length();
                    String[] mailIds = new String[length];
                    for (int i = 0; i < length; i++) {
                        mailIds[i] = jArray.getString(i);
                    }

                    String fullName = fa.getFullname();
                    MailMessage[] msgs = mailAccess.getMessageStorage().getMessages(fullName, mailIds, new MailField[] { MailField.ID, MailField.RECEIVED_DATE});
                    if (null == msgs || msgs.length <= 0) {
                        return new AJAXRequestResult(Boolean.TRUE, "native");
                    }

                    move2Archive(msgs, fullName, archiveFullname, separator, mailAccess);
                } finally {
                    if (null != mailAccess) {
                        mailAccess.close(true);
                    }
                }
            }

            return new AJAXRequestResult(Boolean.TRUE, "native");
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    protected void move2Archive(MailMessage[] msgs, String fullName, String archiveFullname, char separator, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        Calendar cal = Calendar.getInstance(TimeZoneUtils.getTimeZone("UTC"));
        move2Archive(msgs, fullName, archiveFullname, separator, cal, mailAccess);
    }

    protected void move2Archive(MailMessage[] msgs, String fullName, String archiveFullname, char separator, Calendar cal, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
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

        int accountId = mailAccess.getAccountId();
        Session session = mailAccess.getSession();
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
                toCreate.setSeparator(separator);
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
            mailAccess.getMessageStorage().moveMessages(fullName, fn, ids.toArray(new String[ids.size()]), true);
        }
    }

}
