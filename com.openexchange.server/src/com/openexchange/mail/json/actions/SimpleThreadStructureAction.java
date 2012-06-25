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
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONValue;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.json.cache.JsonCacheService;
import com.openexchange.json.cache.JsonCaches;
import com.openexchange.log.Log;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.ThreadedStructure;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.MailRequestSha1Calculator;
import com.openexchange.mail.json.converters.MailConverter;
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

    protected static final org.apache.commons.logging.Log LOG = Log.loggerFor(SimpleThreadStructureAction.class);

    protected static final boolean DEBUG = LOG.isDebugEnabled();

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
                    result = new AJAXRequestResult(new JSONArray(), "json");
                    result.setResponseProperty("cached", Boolean.TRUE);
                } else {
                    result = new AJAXRequestResult(jsonValue, "json");
                    result.setResponseProperty("cached", Boolean.TRUE);
                    if (DEBUG) {
                        final long dur = System.currentTimeMillis() - st;
                        LOG.debug("\tSimpleThreadStructureAction.perform(): JSON cache look-up took " + dur + "msec");
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
                                LOG.debug("\tSimpleThreadStructureAction.perform(): JSON cache update took " + dur + "msec");
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
                ThreadPools.getThreadPool().submit(ThreadPools.task(r));
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

    /**
     * Performs the request w/o look-up cache.
     */
    protected AJAXRequestResult perform0(final MailRequest req, final MailServletInterface mailInterface, final boolean cache) throws OXException {
        try {
            /*
             * Read in parameters
             */
            final String folderId = req.checkParameter(Mail.PARAMETER_MAILFOLDER);
            int[] columns = req.checkIntArray(Mail.PARAMETER_COLUMNS);
            final String sort = req.getParameter(Mail.PARAMETER_SORT);
            final String order = req.getParameter(Mail.PARAMETER_ORDER);
            if (sort != null && order == null) {
                throw MailExceptionCode.MISSING_PARAM.create(Mail.PARAMETER_ORDER);
            }
            final int[] fromToIndices;
            {
                final String s = req.getParameter("limit");
                if (null == s) {
                    final int leftHandLimit = req.optInt(Mail.LEFT_HAND_LIMIT);
                    final int rightHandLimit = req.optInt(Mail.RIGHT_HAND_LIMIT);
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
            final long max;
            {
                final String s = req.getParameter("max");
                if (null == s) {
                    max = -1L;
                } else {
                    long tmp;
                    try {
                        tmp = Long.parseLong(s.trim());
                    } catch (final NumberFormatException e) {
                        tmp = -1L;
                    }
                    max = tmp;
                }
            }
            final boolean includeSent = req.optBool("includeSent", false);
            final boolean unseen = req.optBool("unseen", false);
            final boolean ignoreDeleted = !req.optBool("deleted", true);
            if (unseen || ignoreDeleted) {
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
                    throw MailExceptionCode.INVALID_INT_VALUE.create(Mail.PARAMETER_ORDER);
                }
            }
            /*
             * Start response
             */
            final long start = System.currentTimeMillis();
            final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
            if (!unseen && !ignoreDeleted) {
                final List<List<MailMessage>> mails =
                    mailInterface.getAllSimpleThreadStructuredMessages(
                        folderId,
                        includeSent,
                        cache,
                        sortCol,
                        orderDir,
                        columns,
                        fromToIndices, max);
                return new AJAXRequestResult(ThreadedStructure.valueOf(mails), "mail");
            }
            List<List<MailMessage>> mails =
                mailInterface.getAllSimpleThreadStructuredMessages(folderId, includeSent, false, sortCol, orderDir, columns, null, max);
            boolean cached = false;
            if (mails instanceof PropertizedList) {
                final PropertizedList<List<MailMessage>> propertizedList = (PropertizedList<List<MailMessage>>) mails;
                final Boolean b = (Boolean) propertizedList.getProperty("cached");
                cached = null != b && b.booleanValue();
            }
            boolean foundUnseen;
            for (final Iterator<List<MailMessage>> iterator = mails.iterator(); iterator.hasNext();) {
                final List<MailMessage> list = iterator.next();
                foundUnseen = false;
                for (final Iterator<MailMessage> tmp = list.iterator(); tmp.hasNext();) {
                    final MailMessage message = tmp.next();
                    if (ignoreDeleted && message.isDeleted()) {
                        // Ignore mail marked for deletion
                        tmp.remove();
                    } else {
                        // Check if unseen
                        foundUnseen |= !message.isSeen();
                    }
                }
                if (unseen && !foundUnseen) {
                    iterator.remove();
                }
            }
            if (null != fromToIndices) {
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
            final AJAXRequestResult result = new AJAXRequestResult(ThreadedStructure.valueOf(mails), "mail");
            result.setResponseProperty("cached", Boolean.valueOf(cached));
            return result.setDurationByStart(start);
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
                "threadedAll",
                req.checkParameter(Mail.PARAMETER_MAILFOLDER),
                req.checkParameter(Mail.PARAMETER_COLUMNS),
                req.getParameter(Mail.PARAMETER_SORT),
                req.getParameter(Mail.PARAMETER_ORDER),
                req.getParameter("limit"),
                req.getParameter(Mail.LEFT_HAND_LIMIT),
                req.getParameter(Mail.RIGHT_HAND_LIMIT),
                req.getParameter("includeSent"),
                req.getParameter("unseen"),
                req.getParameter("deleted"));
        return sha1Sum;
    }

}
