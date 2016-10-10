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

import static com.openexchange.mail.mime.utils.MimeMessageUtility.unfold;
import static com.openexchange.mail.utils.DateUtils.getDateRFC822;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;
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
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.FileBackedMimeMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.usersetting.UserSettingMail;
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

    /**
     * Initializes a new {@link ImportAction}.
     *
     * @param services
     */
    public ImportAction(ServiceLookup services) {
        super(services);
    }

    private static final String PLAIN_JSON = "plainJson";

    @Override
    protected AJAXRequestResult perform(MailRequest mailRequest) throws OXException {
        AJAXRequestData request = mailRequest.getRequest();
        List<OXException> warnings = new ArrayList<OXException>();
        try {
            String folder = mailRequest.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            int flags;
            {
                String tmp = mailRequest.getParameter("flags");
                if (null == tmp) {
                    flags = 0;
                } else {
                    try {
                        flags = Integer.parseInt(tmp.trim());
                    } catch (NumberFormatException e) {
                        flags = 0;
                    }
                }
            }
            boolean force;
            {
                String tmp = mailRequest.getParameter("force");
                force = null == tmp ? false : AJAXRequestDataTools.parseBoolParameter(tmp.trim());
            }
            boolean preserveReceivedDate = mailRequest.optBool("preserveReceivedDate", false);
            boolean strictParsing = mailRequest.optBool("strictParsing", true);
            /*
             * Iterate upload files
             */
            ServerSession session = mailRequest.getSession();
            UserSettingMail usm = session.getUserSettingMail();
            QuotedInternetAddress defaultSendAddr = new QuotedInternetAddress(usm.getSendAddr(), false);
            MailServletInterface mailInterface = MailServletInterface.getInstance(session);
            BlockingQueue<MimeMessage> queue = new ArrayBlockingQueue<MimeMessage>(100);
            Future<Object> future = null;
            {
                ThreadPoolService service = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class, true);
                AppenderTask task = new AppenderTask(mailInterface, folder, force, flags, queue);
                try {
                    // Initialize iterator
                    Iterator<UploadFile> iter;
                    {
                        long maxFileSize = usm.getUploadQuotaPerFile();
                        if (maxFileSize <= 0) {
                            maxFileSize = -1L;
                        }
                        long maxSize = usm.getUploadQuota();
                        if (maxSize <= 0) {
                            maxSize = -1L;
                        }
                        iter = request.getFiles(maxFileSize, maxSize).iterator();
                    }

                    // Iterate uploaded messages
                    boolean keepgoing = true;
                    if (keepgoing && iter.hasNext()) {
                        future = service.submit(task);
                        do {
                            UploadFile uploadFile = iter.next();
                            File tmpFile = uploadFile.getTmpFile();
                            boolean first = true;
                            if (null != tmpFile) {
                                try {
                                    // Validate content
                                    if (strictParsing) {
                                        validateRfc822Message(tmpFile);
                                    }
                                    first = false;

                                    // Parse & add to queue
                                    MimeMessage message = newMimeMessagePreservingReceivedDate(tmpFile, preserveReceivedDate);
                                    message.removeHeader("x-original-headers");
                                    String fromAddr = message.getHeader(MessageHeaders.HDR_FROM, null);
                                    if (isEmpty(fromAddr)) {
                                        // Add from address
                                        message.setFrom(defaultSendAddr);
                                    }
                                    while (keepgoing && !queue.offer(message, 1, TimeUnit.SECONDS)) {
                                        keepgoing = !future.isDone();
                                    }
                                } catch (OXException e) {
                                    if (first && !iter.hasNext()) {
                                        throw e;
                                    }
                                    // Otherwise add to warnings
                                    warnings.add(e);
                                }
                            }
                        } while (keepgoing && iter.hasNext());
                    }
                } finally {
                    task.stop();
                }
            }

            MailImportResult[] mirs;
            if (null == future) {
                mirs = new MailImportResult[0];
            } else {
                /*
                 * Ensure release from BlockingQueue.take();
                 */
                try {
                    future.get();
                } catch (ExecutionException e) {
                    Throwable t = e.getCause();
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
                MailImportResult[] alreadyImportedOnes = mailInterface.getMailImportResults();
                /*
                 * Still some in queue?
                 */
                if (queue.isEmpty()) {
                    mirs = alreadyImportedOnes;
                } else {
                    List<MimeMessage> messages = new ArrayList<MimeMessage>(16);
                    queue.drainTo(messages);
                    messages.remove(POISON);
                    List<MailMessage> mails = new ArrayList<MailMessage>(messages.size());
                    for (MimeMessage message : messages) {
                        message.getHeader("Date", null);
                        MailMessage mm = MimeMessageConverter.convertMessage(message);
                        mails.add(mm);
                    }
                    messages.clear();
                    mailInterface = getMailInterface(mailRequest);
                    {
                        String[] ids = mailInterface.importMessages(folder, mails.toArray(new MailMessage[mails.size()]), force);
                        mails.clear();
                        if (flags > 0) {
                            mailInterface.updateMessageFlags(folder, ids, flags, true);
                        }
                    }
                    MailImportResult[] byCaller = mailInterface.getMailImportResults();
                    warnings.addAll(mailInterface.getWarnings());
                    mirs = new MailImportResult[alreadyImportedOnes.length + byCaller.length];
                    System.arraycopy(alreadyImportedOnes, 0, mirs, 0, alreadyImportedOnes.length);
                    System.arraycopy(byCaller, 0, mirs, alreadyImportedOnes.length, byCaller.length);
                }
            }

            JSONArray respArray = new JSONArray();
            for (MailImportResult m : mirs) {
                if (m.hasError()) {
                    JSONObject responseObj = new JSONObject();
                    responseObj.put(FolderChildFields.FOLDER_ID, folder);
                    responseObj.put(MailImportResult.FILENAME, m.getMail().getFileName());
                    responseObj.put(MailImportResult.ERROR, m.getException().getMessage());
                    respArray.put(responseObj);
                } else {
                    JSONObject responseObj = new JSONObject();
                    responseObj.put(FolderChildFields.FOLDER_ID, folder);
                    responseObj.put(DataFields.ID, m.getId());
                    respArray.put(responseObj);
                }
            }

            // Create response object
            AJAXRequestResult result = new AJAXRequestResult(respArray, "json");
            result.setParameter(PLAIN_JSON, Boolean.TRUE);
            result.addWarnings(warnings);
            return result;
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (MessageRemovedException e) {
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e);
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void validateRfc822Message(File rfc822File) throws IOException, OXException {
        MimeConfig config = new MimeConfig();
        config.setMaxLineLen(-1);
        config.setMaxHeaderLen(-1);
        config.setMaxHeaderCount(250);
        config.setStrictParsing(true);

        MimeStreamParser parser = new MimeStreamParser(config);
        parser.setContentHandler(DO_NOTHING_HANDLER);

        InputStream in = new FileInputStream(rfc822File);
        try {
            parser.parse(in);
        } catch (MimeException e) {
            throw MailExceptionCode.INVALID_MESSAGE.create(e, e.getMessage());
        } finally {
            Streams.close(in);
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

        protected AppenderTask(MailServletInterface mailInterface, String folder, boolean force, int flags, BlockingQueue<MimeMessage> queue) {
            super();
            keepgoing = new AtomicBoolean(true);
            this.mailInterface = mailInterface;
            this.folder = folder;
            this.force = force;
            this.flags = flags;
            this.queue = queue;
        }

        protected void stop() throws OXException {
            keepgoing.set(false);
            /*
             * Feed poison element to enforce quit
             */
            try {
                queue.put(POISON);
            } catch (InterruptedException e) {
                /*
                 * Cannot occur, but keep interrupted state
                 */
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            }
        }

        @Override
        public Object call() throws Exception {
            List<String> idList = new ArrayList<String>();
            try {
                List<MimeMessage> messages = new ArrayList<MimeMessage>(16);
                List<MailMessage> mails = new ArrayList<MailMessage>(16);
                while (keepgoing.get() || !queue.isEmpty()) {
                    messages.clear();
                    mails.clear();
                    if (queue.isEmpty()) {
                        // Blocking wait for at least 1 message to arrive.
                        MimeMessage msg = queue.take();
                        if (POISON == msg) {
                            return null;
                        }
                        messages.add(msg);
                    }
                    queue.drainTo(messages);
                    boolean quit = messages.remove(POISON);
                    for (MimeMessage message : messages) {
                        message.getHeader("Date", null);
                        MailMessage mm = MimeMessageConverter.convertMessage(message);
                        mails.add(mm);
                    }
                    String[] ids = mailInterface.importMessages(folder, mails.toArray(new MailMessage[mails.size()]), force);
                    idList.clear();
                    idList.addAll(Arrays.asList(ids));
                    if (flags > 0) {
                        mailInterface.updateMessageFlags(folder, ids, flags, true);
                    }
                    if (quit) {
                        return null;
                    }
                }
            } catch (OXException e) {
                throw e;
            } catch (MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            } catch (InterruptedException e) {
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } finally {
                mailInterface.close(true);
            }
            return null;
        }

        @Override
        public void setThreadName(ThreadRenamer threadRenamer) {
            threadRenamer.rename("Mail Import Thread");
        }
    }

    private MimeMessage newMimeMessagePreservingReceivedDate(File tempFile, boolean preserveReceivedDate) throws MessagingException, IOException {
        MimeMessage tmp;
        if (preserveReceivedDate) {
            tmp = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), tempFile, null) {

                private boolean notParsed = true;
                private Date receivedDate = null;

                @Override
                public Date getReceivedDate() throws MessagingException {
                    if (notParsed) {
                        notParsed = false;
                        String[] receivedHdrs = getHeader(MessageHeaders.HDR_RECEIVED);
                        if (null != receivedHdrs) {
                            long lastReceived = Long.MIN_VALUE;
                            for (int i = 0; i < receivedHdrs.length; i++) {
                                String hdr = unfold(receivedHdrs[i]);
                                int pos;
                                if (hdr != null && (pos = hdr.lastIndexOf(';')) != -1) {
                                    try {
                                        lastReceived = Math.max(lastReceived, getDateRFC822(hdr.substring(pos + 1).trim()).getTime());
                                    } catch (Exception e) {
                                        continue;
                                    }
                                }
                            }

                            if (lastReceived > 0L) {
                                receivedDate = new Date(lastReceived);
                            }
                        }
                    }

                    return receivedDate;
                }
            };
        } else {
            tmp = new FileBackedMimeMessage(MimeDefaultSession.getDefaultSession(), tempFile, null);
        }
        return tmp;
    }

    private static final ContentHandler DO_NOTHING_HANDLER = new ContentHandler() {

        @Override
        public void startMessage() throws MimeException {
            // Nothing
        }

        @Override
        public void endMessage() throws MimeException {
            // Nothing
        }

        @Override
        public void startBodyPart() throws MimeException {
            // Nothing
        }

        @Override
        public void endBodyPart() throws MimeException {
            // Nothing
        }

        @Override
        public void startHeader() throws MimeException {
            // Nothing
        }

        @Override
        public void field(Field rawField) throws MimeException {
            // Nothing
        }

        @Override
        public void endHeader() throws MimeException {
            // Nothing
        }

        @Override
        public void preamble(InputStream is) throws MimeException, IOException {
            // Nothing
        }

        @Override
        public void epilogue(InputStream is) throws MimeException, IOException {
            // Nothing
        }

        @Override
        public void startMultipart(BodyDescriptor bd) throws MimeException {
            // Nothing
        }

        @Override
        public void endMultipart() throws MimeException {
            // Nothing
        }

        @Override
        public void body(BodyDescriptor bd, InputStream is) throws MimeException, IOException {
            // Nothing
        }

        @Override
        public void raw(InputStream is) throws MimeException, IOException {
            // Nothing
        }
    };

}
