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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.MailImportResult;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.java.Streams;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.AbstractTrackableTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ImportAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.POST, name = "import", description = "Import mail as MIME data block (RFC822)", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", optional = true, description = "In case the mail should not be sent out, but saved in a specific folder, the \"folder\" parameter can be used. If the mail should be sent out to the recipient, the \"folder\" parameter must not be included and the mail is stored in the folder \"Sent Items\". Example \"folder=default.INBOX/Testfolder\""),
    @Parameter(name = "flags", optional = true, description = "In case the mail should be stored with status \"read\" (e.g. mail has been read already in the client inbox), the parameter \"flags\" has to be included. If no \"folder\" parameter is specified, this parameter must not be included. For infos about mail flags see Detailed mail data spec.") }, requestBody = "The MIME Data Block.", responseDescription = "Object ID of the newly created/moved mail.")
public final class ImportAction extends AbstractMailAction {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ImportAction.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link ImportAction}.
     *
     * @param services
     */
    public ImportAction(final ServiceLookup services) {
        super(services);
    }

    private static final String PLAIN_JSON = "plainJson";

    @Override
    protected AJAXRequestResult perform(final MailRequest mailRequest) throws OXException {
        final AJAXRequestData request = mailRequest.getRequest();
        final List<OXException> warnings = new ArrayList<OXException>();
        UploadEvent uploadEvent = null;
        try {
            final String folder = mailRequest.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            final int flags;
            {
                final String tmp = mailRequest.getParameter("flags");
                if (null == tmp) {
                    flags = 0;
                } else {
                    int f;
                    try {
                        f = Integer.parseInt(tmp.trim());
                    } catch (final NumberFormatException e) {
                        f = 0;
                    }
                    flags = f;
                }
            }
            final boolean force;
            {
                final String tmp = mailRequest.getParameter("force");
                if (null == tmp) {
                    force = false;
                } else {
                    force = AJAXRequestDataTools.parseBoolParameter(tmp.trim());
                }
            }
            /*
             * Iterate upload files
             */
            final ServerSession session = mailRequest.getSession();
            final QuotedInternetAddress defaultSendAddr = new QuotedInternetAddress(getDefaultSendAddress(session), true);
            MailServletInterface mailInterface = MailServletInterface.getInstance(session);
            final BlockingQueue<MimeMessage> queue = new ArrayBlockingQueue<MimeMessage>(100);
            Future<Object> future = null;
            {
                final ThreadPoolService service = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class, true);
                final AppenderTask task = new AppenderTask(mailInterface, folder, force, flags, queue);
                try {
                    final Iterator<UploadFile> iter = request.getFiles().iterator();
                    if (iter.hasNext()) {
                        uploadEvent = request.getUploadEvent();
                        future = service.submit(task);
                    }
                    final javax.mail.Session defaultSession = MimeDefaultSession.getDefaultSession();
                    boolean keepgoing = true;
                    while (keepgoing && iter.hasNext()) {
                        final UploadFile item = iter.next();
                        final InputStream is = item.openStream();
                        final MimeMessage message;
                        try {
                            message = new MimeMessage(defaultSession, is);
                            message.removeHeader("x-original-headers");
                        } finally {
                            Streams.close(is);
                        }
                        final String fromAddr = message.getHeader(MessageHeaders.HDR_FROM, null);
                        if (isEmpty(fromAddr)) {
                            // Add from address
                            message.setFrom(defaultSendAddr);
                        }
                        while (keepgoing && !queue.offer(message, 1, TimeUnit.SECONDS)) {
                            keepgoing = !future.isDone();
                        }
                    }
                } finally {
                    task.stop();
                }
            }

            final MailImportResult[] mirs;
            if (null == future) {
                mirs = new MailImportResult[0];
            } else {
                /*
                 * Ensure release from BlockingQueue.take();
                 */
                try {
                    future.get();
                } catch (final ExecutionException e) {
                    final Throwable t = e.getCause();
                    if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    }
                    if (t instanceof Error) {
                        throw (Error) t;
                    }
                    if (t instanceof OXException) {
                        throw (OXException) t;
                    }
                    throw new IllegalStateException("Not unchecked", t);
                }
                final MailImportResult[] alreadyImportedOnes = mailInterface.getMailImportResults();
                /*
                 * Still some in queue?
                 */
                if (queue.isEmpty()) {
                    mirs = alreadyImportedOnes;
                } else {
                    final List<MimeMessage> messages = new ArrayList<MimeMessage>(16);
                    queue.drainTo(messages);
                    messages.remove(POISON);
                    final List<MailMessage> mails = new ArrayList<MailMessage>(messages.size());
                    for (final MimeMessage message : messages) {
                        message.getHeader("Date", null);
                        final MailMessage mm = MimeMessageConverter.convertMessage(message);
                        mails.add(mm);
                    }
                    messages.clear();
                    mailInterface = getMailInterface(mailRequest);
                    {
                        final String[] ids = mailInterface.importMessages(folder, mails.toArray(new MailMessage[mails.size()]), force);
                        mails.clear();
                        if (flags > 0) {
                            mailInterface.updateMessageFlags(folder, ids, flags, true);
                        }
                    }
                    final MailImportResult[] byCaller = mailInterface.getMailImportResults();
                    warnings.addAll(mailInterface.getWarnings());
                    mirs = new MailImportResult[alreadyImportedOnes.length + byCaller.length];
                    System.arraycopy(alreadyImportedOnes, 0, mirs, 0, alreadyImportedOnes.length);
                    System.arraycopy(byCaller, 0, mirs, alreadyImportedOnes.length, byCaller.length);
                }
            }
            final JSONArray respArray = new JSONArray();
            for (final MailImportResult m : mirs) {
                if (m.hasError()) {
                    final JSONObject responseObj = new JSONObject();
                    responseObj.put(FolderChildFields.FOLDER_ID, folder);
                    responseObj.put(MailImportResult.FILENAME, m.getMail().getFileName());
                    responseObj.put(MailImportResult.ERROR, m.getException().getMessage());
                    respArray.put(responseObj);
                } else {
                    final JSONObject responseObj = new JSONObject();
                    responseObj.put(FolderChildFields.FOLDER_ID, folder);
                    responseObj.put(DataFields.ID, m.getId());
                    respArray.put(responseObj);
                }
            }
            /*
             * Create response object
             */
            final AJAXRequestResult result = new AJAXRequestResult(respArray, "json");
            result.setParameter(PLAIN_JSON, Boolean.TRUE);
            result.addWarnings(warnings);
            return result;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e);
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * The poison element to quit message import immediately.
     */
    protected static final MimeMessage POISON = new MimeMessage(MimeDefaultSession.getDefaultSession());

    private static final class AppenderTask extends AbstractTrackableTask<Object> {

        private final AtomicBoolean keepgoing;
        private final MailServletInterface mailInterface;
        private final String folder;
        private final boolean force;
        private final int flags;
        private final BlockingQueue<MimeMessage> queue;
        private final Props logProperties;

        protected AppenderTask(final MailServletInterface mailInterface, final String folder, final boolean force, final int flags, final BlockingQueue<MimeMessage> queue) {
            super();
            keepgoing = new AtomicBoolean(true);
            this.mailInterface = mailInterface;
            this.folder = folder;
            this.force = force;
            this.flags = flags;
            this.queue = queue;
            final Props props = LogProperties.optLogProperties();
            logProperties = null == props ? null : props;
        }

        protected void stop() throws OXException {
            keepgoing.set(false);
            /*
             * Feed poison element to enforce quit
             */
            try {
                queue.put(POISON);
            } catch (final InterruptedException e) {
                /*
                 * Cannot occur, but keep interrupted state
                 */
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            }
        }

        @Override
        public Props optLogProperties() {
            return logProperties;
        }

        @Override
        public Object call() throws Exception {
            final List<String> idList = new ArrayList<String>();
            try {
                final List<MimeMessage> messages = new ArrayList<MimeMessage>(16);
                final List<MailMessage> mails = new ArrayList<MailMessage>(16);
                while (keepgoing.get() || !queue.isEmpty()) {
                    if (queue.isEmpty()) {
                        // Blocking wait for at least 1 message to arrive.
                        final MimeMessage msg = queue.take();
                        if (POISON == msg) {
                            return null;
                        }
                        messages.add(msg);
                    }
                    queue.drainTo(messages);
                    final boolean quit = messages.remove(POISON);
                    for (final MimeMessage message : messages) {
                        message.getHeader("Date", null);
                        final MailMessage mm = MimeMessageConverter.convertMessage(message);
                        mails.add(mm);
                    }
                    messages.clear();
                    final String[] ids = mailInterface.importMessages(folder, mails.toArray(new MailMessage[mails.size()]), force);
                    mails.clear();
                    idList.addAll(Arrays.asList(ids));
                    if (flags > 0) {
                        mailInterface.updateMessageFlags(folder, ids, flags, true);
                    }
                    if (quit) {
                        return null;
                    }
                }
            } catch (final OXException e) {
                throw e;
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            } catch (final InterruptedException e) {
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } finally {
                mailInterface.close(true);
            }
            return null;
        }

        @Override
        public void setThreadName(final ThreadRenamer threadRenamer) {
            threadRenamer.rename("Mail Import Thread");
        }
    }

}
