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

package com.openexchange.mail.json.actions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.json.cache.JsonCacheService;
import com.openexchange.json.cache.JsonCaches;
import com.openexchange.log.Log;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.MailRequestSha1Calculator;
import com.openexchange.mail.json.converters.MailConverter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AllAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "all", description = "Get all mails.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", description = "Object ID of the folder, whose contents are queried."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for appointments are defined in Detailed mail data. The alias \\\"all\\\" uses the predefined columnset [600, 601]."),
    @Parameter(name = "sort", optional=true, description = "The identifier of a column which determines the sort order of the response or the string \u201cthread\u201d to return thread-sorted messages. If this parameter is specified and holds a column number, then the parameter order must be also specified."),
    @Parameter(name = "order", optional=true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.")
}, responseDescription = "Response (not IMAP: with timestamp): An array with mail data. Each array element describes one mail and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
public final class AllAction extends AbstractMailAction implements MailRequestSha1Calculator {

    protected static final org.apache.commons.logging.Log LOG = Log.loggerFor(AllAction.class);

    protected static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link AllAction}.
     *
     * @param services The service look-up
     */
    public AllAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        final boolean cache = req.optBool("cache", false);
        if (cache && CACHABLE_FORMATS.contains(req.getRequest().getFormat())) {
            final JsonCacheService jsonCache = JsonCaches.getCache();
            if (null != jsonCache) {
                final long st = DEBUG ? System.currentTimeMillis() : 0L;
                final String sha1Sum = getSha1For(req);
                final String id = "com.openexchange.mail." + sha1Sum;
                final ServerSession session = req.getSession();
                final JSONValue jsonValue = jsonCache.opt(id, session.getUserId(), session.getContextId());
                final AJAXRequestResult result;
                if (null == jsonValue) {
                    /*
                     * Check mailbox size
                     */
                    final MailServletInterface mailInterface = getMailInterface(req);
                    final String folderId = req.checkParameter(Mail.PARAMETER_MAILFOLDER);
                    if (mailInterface.getMessageCount(folderId) <= mailInterface.getMailConfig().getMailProperties().getMailFetchLimit()) {
                        /*
                         * Mailbox considered small enough for direct hand-off
                         */
                        return perform0(req, mailInterface, false);
                    }
                    /*
                     * Return empty array immediately
                     */
                    result = new AJAXRequestResult(new JSONArray(0), "json");
                    result.setResponseProperty("cached", Boolean.TRUE);
                } else {
                    result = new AJAXRequestResult(jsonValue, "json");
                    result.setResponseProperty("cached", Boolean.TRUE);
                    if (DEBUG) {
                        final long dur = System.currentTimeMillis() - st;
                        LOG.debug("\tAllAction.perform(): JSON cache look-up took " + dur + "msec");
                    }
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
                            final long st = DEBUG ? System.currentTimeMillis() : 0L;
                            mailInterface = MailServletInterface.getInstance(session);
                            final AJAXRequestResult requestResult = perform0(mailRequest, mailInterface, true);
                            MailConverter.getInstance().convert(mailRequest.getRequest(), requestResult, session, null);
                            if (DEBUG) {
                                final long dur = System.currentTimeMillis() - st;
                                LOG.debug("\tAllAction.perform(): JSON cache update took " + dur + "msec");
                            }
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
        return perform0(req, getMailInterface(req), false);
    }

    protected AJAXRequestResult perform0(final MailRequest req, final MailServletInterface mailInterface, final boolean cache) throws OXException {
        try {
            /*
             * Read in parameters
             */
            final String folderId = req.checkParameter(Mail.PARAMETER_MAILFOLDER);
            int[] columns = req.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
            final String sort = req.getParameter(AJAXServlet.PARAMETER_SORT);
            final String order = req.getParameter(AJAXServlet.PARAMETER_ORDER);
            if (sort != null && order == null) {
                throw MailExceptionCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_ORDER);
            }
            final int[] fromToIndices;
            {
                final String s = req.getParameter("limit");
                if (null == s) {
                    final int leftHandLimit = req.optInt(AJAXServlet.LEFT_HAND_LIMIT);
                    final int rightHandLimit = req.optInt(AJAXServlet.RIGHT_HAND_LIMIT);
                    if (leftHandLimit == MailRequest.NOT_FOUND || rightHandLimit == MailRequest.NOT_FOUND) {
                        fromToIndices = null;
                    } else {
                        fromToIndices = new int[] { leftHandLimit < 0 ? 0 : leftHandLimit, rightHandLimit < 0 ? 0 : rightHandLimit};
                        if (fromToIndices[0] >= fromToIndices[1]) {
                            return new AJAXRequestResult(Collections.<MailMessage>emptyList(), "mail");
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
                            i = Integer.parseInt(s.substring(pos+1).trim());
                            end = i < 0 ? 0 : i;
                        }
                    } catch (final NumberFormatException e) {
                        throw MailExceptionCode.INVALID_INT_VALUE.create(e, s);
                    }
                    if (start >= end) {
                        return new AJAXRequestResult(Collections.<MailMessage>emptyList(), "mail");
                    }
                    fromToIndices = new int[] {start,end};
                }
            }
            final boolean unseen = req.optBool("unseen");
            final boolean ignoreDeleted = !req.optBool("deleted", true);
            final boolean filterApplied = (unseen || ignoreDeleted);
            if (filterApplied) {
                // Ensure flags is contained in provided columns
                final int fieldFlags = MailListField.FLAGS.getField();
                boolean found = false;
                for (int i = 0; !found && i < columns.length; i++) {
                   found = fieldFlags == columns[i];
                }
                if (!found) {
                    final int[] tmp = columns;
                    columns = new int[columns.length + 1];
                    System.arraycopy(tmp, 0, columns, 0, tmp.length);
                    columns[tmp.length] = fieldFlags;
                }
            }
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
            final long start = System.currentTimeMillis();
            List<MailMessage> mails = new LinkedList<MailMessage>();
            SearchIterator<MailMessage> it = null;
            try {
                /*
                 * Check for thread-sort
                 */
                if (("thread".equalsIgnoreCase(sort))) {
                    it =
                        mailInterface.getAllThreadedMessages(
                            folderId,
                            MailSortField.RECEIVED_DATE.getField(),
                            orderDir,
                            columns,
                            filterApplied ? null : fromToIndices);
                    final int size = it.size();
                    for (int i = 0; i < size; i++) {
                        final MailMessage mm = it.next();
                        if (null != mm && (!unseen || !mm.isSeen()) && (!ignoreDeleted || !mm.isDeleted())) {
                            if (!mm.containsAccountId()) {
                                mm.setAccountId(mailInterface.getAccountID());
                            }
                            mails.add(mm);
                        }
                    }
                } else {
                    final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
                    /*
                     * Get iterator
                     */
                    it = mailInterface.getAllMessages(folderId, sortCol, orderDir, columns, filterApplied ? null : fromToIndices);
                    final int size = it.size();
                    for (int i = 0; i < size; i++) {
                        final MailMessage mm = it.next();
                        if (null != mm && (!unseen || !mm.isSeen()) && (!ignoreDeleted || !mm.isDeleted())) {
                            if (!mm.containsAccountId()) {
                                mm.setAccountId(mailInterface.getAccountID());
                            }
                            mails.add(mm);
                        }
                    }
                }
            } finally {
                if (null != it) {
                    it.close();
                    it = null;
                }
            }
            if (filterApplied && (null != fromToIndices)) {
                final int fromIndex = fromToIndices[0];
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
            final AJAXRequestResult result = new AJAXRequestResult(mails, "mail").setDurationByStart(start);
            result.setResponseProperty("cached", Boolean.valueOf(cache));
            return result;
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
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
                "all",
                req.checkParameter(Mail.PARAMETER_MAILFOLDER),
                req.checkParameter(AJAXServlet.PARAMETER_COLUMNS),
                req.getParameter(AJAXServlet.PARAMETER_SORT),
                req.getParameter(AJAXServlet.PARAMETER_ORDER),
                req.getParameter("limit"),
                req.getParameter(AJAXServlet.LEFT_HAND_LIMIT),
                req.getParameter(AJAXServlet.RIGHT_HAND_LIMIT),
                req.getParameter("unseen"),
                req.getParameter("deleted"));
        return sha1Sum;
    }

}
