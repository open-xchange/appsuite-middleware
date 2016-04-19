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

package com.openexchange.mail.json.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONValue;
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.json.cache.JsonCacheService;
import com.openexchange.json.cache.JsonCaches;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.ThreadedStructure;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.MailRequestSha1Calculator;
import com.openexchange.mail.json.converters.MailConverter;
import com.openexchange.mail.json.utils.ColumnCollection;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.collections.PropertizedList;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SimpleThreadStructureAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SimpleThreadStructureAction extends AbstractMailAction implements MailRequestSha1Calculator {

    protected static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SimpleThreadStructureAction.class);

    /**
     * Initializes a new {@link SimpleThreadStructureAction}.
     *
     * @param services The service look-up
     */
    public SimpleThreadStructureAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        /*
         * Try JSON cache
         */
        //req.getRequest().putParameter("cache", "true");
        //req.getRequest().putParameter("max", "10000");
        final boolean cache = req.optBool("cache", false);
        if (cache && CACHABLE_FORMATS.contains(req.getRequest().getFormat())) {
            final JsonCacheService jsonCache = JsonCaches.getCache();
            if (jsonCache != null) {
                final String sha1Sum = getSha1For(req);
                final String id = "com.openexchange.mail." + sha1Sum;
                final ServerSession session = req.getSession();
                final JSONValue jsonValue = jsonCache.opt(id, session.getUserId(), session.getContextId());
                final AJAXRequestResult result;
                if (jsonValue == null || jsonValue.length() == 0) {
                    /*
                     * Check mailbox size and 'max' parameter
                     */
                    final long max = req.getMax();
                    final MailServletInterface mailInterface = getMailInterface(req);
                    final String folderId = req.checkParameter(Mail.PARAMETER_MAILFOLDER);
                    {
                        final int messageCount = mailInterface.getMessageCount(folderId);
                        final int fetchLimit;
                        if ((messageCount <= 0) || (messageCount <= (fetchLimit = getFetchLimit(mailInterface))) || ((max > 0) && (max <= fetchLimit))) {
                            /*
                             * Mailbox considered small enough for direct hand-off
                             */
                            return perform0(req, mailInterface, false);
                        }
                    }
                    /*
                     * Return empty array immediately
                     */
                    result = new AJAXRequestResult(new JSONArray(0), "json");
                    result.setResponseProperty("cached", Boolean.TRUE);
                } else {
                    result = new AJAXRequestResult(jsonValue, "json");
                    result.setResponseProperty("cached", Boolean.TRUE);
                }
                /*-
                 * Update cache with separate thread
                 */
                final AJAXRequestData requestData = req.getRequest().copyOf();
                requestData.setProperty("mail.sha1", sha1Sum);
                requestData.setProperty("mail.sha1calc", this);
                requestData.setProperty(id, jsonValue);
                final MailRequest mailRequest = new MailRequest(requestData, session);
                final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        final ServerSession session = mailRequest.getSession();
                        MailServletInterface mailInterface = null;
                        boolean locked = false;
                        try {
                            if (!jsonCache.lock(id, session.getUserId(), session.getContextId())) {
                                // Couldn't acquire lock
                                return;
                            }
                            locked = true;
                            mailInterface = MailServletInterface.getInstance(session);
                            final AJAXRequestResult requestResult = perform0(mailRequest, mailInterface, true);
                            MailConverter.getInstance().convert(mailRequest.getRequest(), requestResult, session, null);
                        } catch (final Exception e) {
                            // Something went wrong
                            try {
                                jsonCache.delete(id, session.getUserId(), session.getContextId());
                            } catch (final Exception ignore) {
                                // Ignore
                            }
                        } finally {
                            if (null != mailInterface) {
                                try {
                                    mailInterface.close(true);
                                } catch (final Exception e) {
                                    // Ignore
                                }
                            }
                            if (locked) {
                                try {
                                    jsonCache.unlock(id, session.getUserId(), session.getContextId());
                                } catch (final Exception e) {
                                    // Ignore
                                }
                            }
                        }
                    }
                };
                ThreadPools.getThreadPool().submit(ThreadPools.trackableTask(r));
                /*
                 * Return cached JSON result
                 */
                return result;
            }
        }
        /*
         * Perform
         */
        return perform0(req, getMailInterface(req), cache);
    }

    private int getFetchLimit(final MailServletInterface mailInterface) throws OXException {
        if (null == mailInterface) {
            return MailProperties.getInstance().getMailFetchLimit();
        }
        try {
            return mailInterface.getMailConfig().getMailProperties().getMailFetchLimit();
        } catch (final RuntimeException e) {
            return MailProperties.getInstance().getMailFetchLimit();
        }
    }

    /**
     * Performs the request w/o cache look-up.
     */
    protected AJAXRequestResult perform0(final MailRequest req, final MailServletInterface mailInterface, final boolean cache) throws OXException {
        try {
            // Read parameters
            final String folderId = req.checkParameter(Mail.PARAMETER_MAILFOLDER);
            {
                final FullnameArgument arg = MailFolderUtility.prepareMailFolderParam(folderId);
                LogProperties.put(LogProperties.Name.MAIL_FULL_NAME, arg.getFullname());
                LogProperties.put(LogProperties.Name.MAIL_ACCOUNT_ID, Integer.toString(arg.getAccountId()));
            }
            ColumnCollection columnCollection = req.checkColumnsAndHeaders();
            int[] columns = columnCollection.getFields();
            String[] headers = columnCollection.getHeaders();
            String sort = req.getParameter(AJAXServlet.PARAMETER_SORT);
            String order = req.getParameter(AJAXServlet.PARAMETER_ORDER);
            if (sort != null && order == null) {
                throw MailExceptionCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_ORDER);
            }

            int[] fromToIndices;
            {
                String s = req.getParameter("limit");
                if (null == s) {
                    final int leftHandLimit = req.optInt(AJAXServlet.LEFT_HAND_LIMIT);
                    final int rightHandLimit = req.optInt(AJAXServlet.RIGHT_HAND_LIMIT);
                    if (leftHandLimit == MailRequest.NOT_FOUND || rightHandLimit == MailRequest.NOT_FOUND) {
                        fromToIndices = null;
                    } else {
                        fromToIndices = new int[] { leftHandLimit < 0 ? 0 : leftHandLimit, rightHandLimit < 0 ? 0 : rightHandLimit };
                        if (fromToIndices[0] >= fromToIndices[1]) {
                            return new AJAXRequestResult(ThreadedStructure.valueOf(Collections.<List<MailMessage>> emptyList()), "mail");
                        }
                    }
                } else {
                    int start;
                    int end;
                    try {
                        final int pos = s.indexOf(',');
                        if (pos < 0) {
                            start = 0;
                            final int i = Integer.parseInt(s.trim());
                            end = i < 0 ? 0 : i;
                        } else {
                            int i = Integer.parseInt(s.substring(0, pos).trim());
                            start = i < 0 ? 0 : i;
                            i = Integer.parseInt(s.substring(pos + 1).trim());
                            end = i < 0 ? 0 : i;
                        }
                    } catch (final NumberFormatException e) {
                        throw MailExceptionCode.INVALID_INT_VALUE.create(e, s);
                    }
                    if (start >= end) {
                        return new AJAXRequestResult(ThreadedStructure.valueOf(Collections.<List<MailMessage>> emptyList()), "mail");
                    }
                    fromToIndices = new int[] { start, end };
                }
            }

            long lookAhead = req.getMax();
            boolean includeSent = req.optBool("includeSent", false);
            boolean ignoreSeen = req.optBool("unseen", false);
            boolean ignoreDeleted = !req.optBool("deleted", true);
            boolean filterApplied = (ignoreSeen || ignoreDeleted);
            if (filterApplied) {
                // Ensure flags is contained in provided columns
                int fieldFlags = MailListField.FLAGS.getField();
                boolean found = false;
                for (int i = 0; !found && i < columns.length; i++) {
                    found = fieldFlags == columns[i];
                }
                if (!found) {
                    int[] tmp = columns;
                    columns = new int[columns.length + 1];
                    System.arraycopy(tmp, 0, columns, 0, tmp.length);
                    columns[tmp.length] = fieldFlags;
                }
            }
            if (null != headers && headers.length > 0) {
                // Ensure ID is contained in provided columns
                int fieldFlags = MailListField.ID.getField();
                boolean found = false;
                for (int i = 0; !found && i < columns.length; i++) {
                    found = fieldFlags == columns[i];
                }
                if (!found) {
                    int[] tmp = columns;
                    columns = new int[columns.length + 1];
                    System.arraycopy(tmp, 0, columns, 0, tmp.length);
                    columns[tmp.length] = fieldFlags;
                }
            }
            columns = prepareColumns(columns);
            /*
             * Get mail interface
             */
            int orderDir = OrderDirection.ASC.getOrder();
            if (order != null) {
                if (order.equalsIgnoreCase("asc")) {
                    orderDir = OrderDirection.ASC.getOrder();
                } else if (order.equalsIgnoreCase("desc")) {
                    orderDir = OrderDirection.DESC.getOrder();
                } else {
                    throw MailExceptionCode.INVALID_INT_VALUE.create(AJAXServlet.PARAMETER_ORDER);
                }
            }
            /*
             * Start response
             */
            long start = System.currentTimeMillis();
            int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
            if (!filterApplied) {
                List<List<MailMessage>> mails = mailInterface.getAllSimpleThreadStructuredMessages(folderId, includeSent, cache, sortCol, orderDir, columns, fromToIndices, lookAhead);

                if (null != headers && headers.length > 0) {
                    enrichWithHeaders(mails, headers, mailInterface.getMailAccess().getMessageStorage());
                }

                return new AJAXRequestResult(ThreadedStructure.valueOf(mails), "mail");
            }

            List<List<MailMessage>> mails = mailInterface.getAllSimpleThreadStructuredMessages(folderId, includeSent, false, sortCol, orderDir, columns, null, lookAhead);
            boolean cached = false;
            int more = -1;
            if (mails instanceof PropertizedList) {
                PropertizedList<List<MailMessage>> propertizedList = (PropertizedList<List<MailMessage>>) mails;
                Boolean b = (Boolean) propertizedList.getProperty("cached");
                cached = null != b && b.booleanValue();

                Integer i = (Integer) propertizedList.getProperty("more");
                more = null == i ? -1 : i.intValue();
            }

            {
                List<MailMessage> list;
                boolean foundUnseen;
                for (Iterator<List<MailMessage>> iterator = mails.iterator(); iterator.hasNext();) {
                    list = iterator.next();
                    foundUnseen = false;
                    for (final Iterator<MailMessage> tmp = list.iterator(); tmp.hasNext();) {
                        final MailMessage message = tmp.next();
                        if (discardMail(message, false, ignoreDeleted)) {
                            // Ignore mail
                            tmp.remove();
                        } else {
                            // Check if unseen
                            foundUnseen |= !message.isSeen();
                        }
                    }
                    if ((ignoreSeen && !foundUnseen) || list.isEmpty()) {
                        iterator.remove();
                    }
                }
            }

            if (null != fromToIndices) {
                int fromIndex = fromToIndices[0];
                int toIndex = fromToIndices[1];
                final int sz = mails.size();
                if ((fromIndex) > sz) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    mails = Collections.emptyList();
                } else {
                    /*
                     * Reset end index if out of range
                     */
                    if (toIndex >= sz) {
                        toIndex = sz;
                    }
                    mails = mails.subList(fromIndex, toIndex);
                }
            }

            if (null != headers && headers.length > 0) {
                enrichWithHeaders(mails, headers, mailInterface.getMailAccess().getMessageStorage());
            }

            AJAXRequestResult result = new AJAXRequestResult(ThreadedStructure.valueOf(mails), "mail");
            result.setResponseProperty("cached", Boolean.valueOf(cached));
            if (more > 0) {
                result.setResponseProperty("more", Integer.valueOf(more));
            }
            return result.setDurationByStart(start);
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void enrichWithHeaders(List<List<MailMessage>> conversations, String[] headerNames, IMailMessageStorage messageStorage) throws OXException {
        Map<String, List<MailMessage>> folders = new HashMap<String, List<MailMessage>>(4);

        for (List<MailMessage> conversation : conversations) {
            for (MailMessage mail : conversation) {
                if (null != mail) {
                    String fullName = mail.getFolder();
                    List<MailMessage> msgs = folders.get(fullName);
                    if (null == msgs) {
                        msgs = new LinkedList<MailMessage>();
                        folders.put(fullName, msgs);
                    }
                    msgs.add(mail);
                }
            }
        }

        for (Map.Entry<String, List<MailMessage>> entry : folders.entrySet()) {
            enrichWithHeaders(entry.getKey(), entry.getValue(), headerNames, messageStorage);
        }
    }

    private void enrichWithHeaders(String fullName, List<MailMessage> mails, String[] headerNames, IMailMessageStorage messageStorage) throws OXException {
        Map<String, MailMessage> headers;
        {
            List<String> ids = new LinkedList<String>();
            for (MailMessage mail : mails) {
                if (null != mail) {
                    ids.add(mail.getMailId());
                }
            }

            MailMessage[] ms = messageStorage.getMessages(fullName, ids.toArray(new String[ids.size()]), MailFields.toArray(MailField.ID, MailField.HEADERS));
            headers = new HashMap<String, MailMessage>(ms.length);
            for (MailMessage header : ms) {
                headers.put(header.getMailId(), header);
            }
        }

        for (MailMessage mail : mails) {
            if (null != mail) {
                MailMessage header = headers.get(mail.getMailId());
                if (null != header) {
                    for (String headerName : headerNames) {
                        String[] values = header.getHeader(headerName);
                        if (null != values) {
                            for (String value : values) {
                                mail.addHeader(headerName, value);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getSha1For(final MailRequest req) throws OXException {
        final String id = req.getRequest().getProperty("mail.sha1");
        if (null != id) {
            return id;
        }
        final String sha1Sum =
            JsonCaches.getSHA1Sum(
                "threadedAll",
                req.checkParameter(Mail.PARAMETER_MAILFOLDER),
                req.checkParameter(AJAXServlet.PARAMETER_COLUMNS),
                req.getParameter(AJAXServlet.PARAMETER_SORT),
                req.getParameter(AJAXServlet.PARAMETER_ORDER),
                req.getParameter("limit"),
                req.getParameter("max"),
                req.getParameter(AJAXServlet.LEFT_HAND_LIMIT),
                req.getParameter(AJAXServlet.RIGHT_HAND_LIMIT),
                req.getParameter("includeSent"),
                req.getParameter("unseen"),
                req.getParameter("deleted"));
        return sha1Sum;
    }

}
