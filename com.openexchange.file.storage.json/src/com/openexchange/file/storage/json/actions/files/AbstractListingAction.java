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

package com.openexchange.file.storage.json.actions.files;

import static com.openexchange.java.Autoboxing.I;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.converters.preview.AbstractPreviewResultConverter;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.log.LogProperties;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.preview.RemoteInternalPreviewService;
import com.openexchange.startup.ThreadControlService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tx.TransactionAwares;

/**
 * {@link AbstractListingAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AbstractListingAction extends AbstractFileAction {

    /** The logger */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractListingAction.class);

    /**
     * Initializes a new {@link AbstractListingAction}.
     */
    protected AbstractListingAction() {
        super();
    }

    @Override
    protected void before(AJAXInfostoreRequest req) throws OXException {
        super.before(req);
        if (req.isPregeneratePreviews()) {
            LogProperties.putProperty(LogProperties.Name.FILE_STORAGE_PREGENERATE_PREVIEWS, Boolean.TRUE);
        }
    }

    @Override
    protected void after(AJAXInfostoreRequest req) {
        if (req.isPregeneratePreviews()) {
            LogProperties.remove(LogProperties.Name.FILE_STORAGE_PREGENERATE_PREVIEWS);
        }
        super.after(req);
    }

    /**
     * Wraps the supplied timed file result into an appropriate AJAX request result.
     *
     * @param documents The underyling timed result
     * @param request The infostore request
     * @return The AJAX request result
     * @throws OXException If operation fails
     */
    protected AJAXRequestResult result(TimedResult<File> documents, InfostoreRequest request) throws OXException {
        TimedResult<File> timedResult = documents;

        if (request.isPregeneratePreviews()) {
            PreviewService previewService = Services.getPreviewService();
            ThreadPoolService threadPool = Services.getThreadPoolService();
            if (null != previewService && null != threadPool) {
                SearchIterator<File> results = timedResult.results();
                try {
                    List<File> files = new LinkedList<File>();
                    while (results.hasNext()) {
                        // Call preview service for next file
                        File fileMetadata = results.next();
                        files.add(fileMetadata);
                    }

                    SearchIterator<File> sf = new SearchIteratorDelegator<File>(files);
                    timedResult = new TimedResultImpl(sf, timedResult.sequenceNumber());
                    threadPool.submit(new TriggerPreviewServiceTask(files, FileStorageUtility.getNumberOfPregeneratedPreviews(), request, previewService, Services.getThreadControlService()));
                } finally {
                    SearchIterators.close(results);
                }

            }
        }

        return new AJAXRequestResult(timedResult, "infostore");
    }

    /**
     * Creates an AJAX request result wrapping the supplied search iterator.
     *
     * @param searchIterator The search iterator to wrap as AJAX result
     * @param timestamp The time stamp
     * @param request The underlying infostore request
     * @return The AJAX request result
     */
    protected AJAXRequestResult results(final SearchIterator<File> searchIterator, final long timestamp, final InfostoreRequest request) throws OXException {
        SearchIterator<File> results = searchIterator;

        if (request.isPregeneratePreviews()) {
            PreviewService previewService = Services.getPreviewService();
            ThreadPoolService threadPool = Services.getThreadPoolService();
            if (null != previewService && null != threadPool) {
                try {
                    List<File> files = new LinkedList<File>();
                    while (results.hasNext()) {
                        // Call preview service for next file
                        File fileMetadata = results.next();
                        files.add(fileMetadata);
                    }

                    results = new SearchIteratorDelegator<File>(files);
                    threadPool.submit(new TriggerPreviewServiceTask(files, FileStorageUtility.getNumberOfPregeneratedPreviews(), request, previewService, Services.getThreadControlService()));
                } finally {
                    SearchIterators.close(searchIterator);
                }
            }
        }

        return new AJAXRequestResult(results, new Date(timestamp), "infostore");
    }

    /**
     * Creates an AJAX request result wrapping the supplied search iterator.
     *
     * @param searchIterator The search iterator to wrap as AJAX result
     * @param request The underlying infostore request
     * @return The AJAX request result
     */
    protected AJAXRequestResult results(SearchIterator<File> searchIterator, InfostoreRequest request) throws OXException {
        SearchIterator<File> results = searchIterator;
        Long timestamp = null;
        if (request.isPregeneratePreviews()) {
            PreviewService previewService = Services.getPreviewService();
            ThreadPoolService threadPool = Services.getThreadPoolService();
            if (null != previewService && null != threadPool) {
                try {
                    List<File> files = new LinkedList<File>();
                    while (results.hasNext()) {
                        // Call preview service for next file
                        File fileMetadata = results.next();
                        if(timestamp == null || timestamp.longValue() < fileMetadata.getSequenceNumber()) {
                            timestamp = Long.valueOf(fileMetadata.getSequenceNumber());
                        }
                        files.add(fileMetadata);
                    }

                    results = new SearchIteratorDelegator<File>(files);
                    threadPool.submit(new TriggerPreviewServiceTask(files, FileStorageUtility.getNumberOfPregeneratedPreviews(), request, previewService, Services.getThreadControlService()));
                } finally {
                    SearchIterators.close(searchIterator);
                }
            }
        }

        // Calculate eventually missing time stamp
        if(timestamp == null && results.hasNext()) {
            List<File> files = new LinkedList<File>();
            while (results.hasNext()) {
                // Call preview service for next file
                File fileMetadata = results.next();
                if(timestamp == null || timestamp.longValue() < fileMetadata.getSequenceNumber()) {
                    timestamp = Long.valueOf(fileMetadata.getSequenceNumber());
                }
                files.add(fileMetadata);
            }
            return new AJAXRequestResult(files, timestamp == null ? null : new Date(timestamp.longValue()), "infostore");
        }

        return new AJAXRequestResult(results, timestamp == null ? null : new Date(timestamp.longValue()), "infostore");
    }

    /**
     * Creates an AJAX request result wrapping the supplied delta.
     *
     * @param delta The delta to wrap as AJAX result
     * @param request The underlying infostore request
     * @return The AJAX request result
     */
    protected AJAXRequestResult result(Delta<File> delta, InfostoreRequest request) throws OXException {
        return new AJAXRequestResult(delta, new Date(delta.sequenceNumber()), "infostore");
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private static final class TriggerPreviewServiceTask extends AbstractTask<Void> {

        private final ThreadControlService threadControl;
        final ServerSession session;
        private final List<File> files;
        private final AJAXRequestData requestData;
        private final PreviewService previewService;
        private int numberOfPregeneratedPreviews;
        private final boolean loadFileMetadata;

        TriggerPreviewServiceTask(List<File> files, int numberOfPregeneratedPreviews, InfostoreRequest request, PreviewService previewService, ThreadControlService threadControl) throws OXException {
            super();
            this.files = files;
            this.numberOfPregeneratedPreviews = numberOfPregeneratedPreviews;
            this.session = request.getSession();
            this.previewService = previewService;
            this.threadControl = null == threadControl ? ThreadControlService.DUMMY_CONTROL : threadControl;

            AJAXRequestData requestData = request.getRequestData().copyOf();
            requestData.putParameter("width", Integer.toString(FileResponseRenderer.THUMBNAIL_WIDTH));
            requestData.putParameter("height", Integer.toString(FileResponseRenderer.THUMBNAIL_HEIGHT));
            requestData.putParameter("delivery", FileResponseRenderer.THUMBNAIL_DELIVERY);
            requestData.putParameter("scaleType", FileResponseRenderer.THUMBNAIL_SCALE_TYPE);
            this.requestData = requestData;

            List<Field> columns = request.getFieldsToLoad();
            if (null == columns || columns.isEmpty()) {
                loadFileMetadata = true;
            } else {
                boolean load = false;
                if (!columns.contains(Field.FILENAME)) {
                    load = true;
                }
                if (!load && !columns.contains(Field.FILE_MIMETYPE)) {
                    load = true;
                }
                if (!load && !columns.contains(Field.FILE_SIZE)) {
                    load = true;
                }
                loadFileMetadata = load;
            }
        }

        @Override
        public void setThreadName(ThreadRenamer threadRenamer) {
            threadRenamer.renamePrefix("Async-Drive-DC-Trigger");
        }

        @Override
        public Void call() {
            Thread currentThread = Thread.currentThread();
            boolean added = threadControl.addThread(currentThread);
            try {
                if (loadFileMetadata) {
                    // Load meta data
                    IDBasedFileAccess fileAccess = Services.getFileAccessFactory().createAccess(session);
                    try {
                        for (Iterator<File> iter = files.iterator(); !currentThread.isInterrupted() && numberOfPregeneratedPreviews > 0 && iter.hasNext();) {
                            File fileMetadata = iter.next();
                            String id = fileMetadata.getId();
                            try {
                                fileMetadata = fileAccess.getFileMetadata(id, FileStorageFileAccess.CURRENT_VERSION);
                                triggerFor(id, fileMetadata);
                            } catch (Exception e) {
                                LOGGER.warn("Failed to pre-generate preview image from file {} for user {} in context {}", fileMetadata.getId(), I(session.getUserId()), I(session.getContextId()), e);
                            }
                        }
                    } finally {
                        TransactionAwares.finishSafe(fileAccess);
                    }
                } else {
                    // No need to load
                    for (Iterator<File> iter = files.iterator(); !currentThread.isInterrupted() && numberOfPregeneratedPreviews > 0 && iter.hasNext();) {
                        File fileMetadata = iter.next();
                        if (fileMetadata.getFileSize() != 0) {
                            String id = fileMetadata.getId();
                            try {
                                triggerFor(id, fileMetadata);
                            } catch (Exception e) {
                                LOGGER.warn("Failed to pre-generate preview image from file {} for user {} in context {}", fileMetadata.getId(), I(session.getUserId()), I(session.getContextId()), e);
                            }
                        }
                    }
                }
            } finally {
                if (added) {
                    threadControl.removeThread(currentThread);
                }
            }
            return null;
        }

        private void triggerFor(final String id, File fileMetadata) {
            RemoteInternalPreviewService candidate = AbstractPreviewResultConverter.getRemoteInternalPreviewServiceFrom(previewService, fileMetadata.getFileName(), PreviewOutput.IMAGE, session);
            if (null != candidate) {
                // Create appropriate IFileHolder instance
                IFileHolder.InputStreamClosure isClosure = new IFileHolder.InputStreamClosure() {

                    @Override
                    public InputStream newStream() throws OXException, IOException {
                        IDBasedFileAccess fileAccess = Services.getFileAccessFactory().createAccess(session);
                        InputStream inputStream = fileAccess.getDocument(id, FileStorageFileAccess.CURRENT_VERSION);
                        if ((inputStream instanceof BufferedInputStream) || (inputStream instanceof ByteArrayInputStream)) {
                            return inputStream;
                        }
                        return new BufferedInputStream(inputStream, 65536);
                    }
                };
                FileHolder fileHolder = new FileHolder(isClosure, fileMetadata.getFileSize(), fileMetadata.getFileMIMEType(), fileMetadata.getFileName());

                AbstractPreviewResultConverter.triggerPreviewService(session, fileHolder, requestData, candidate, PreviewOutput.IMAGE);
                LOGGER.debug("Triggered to create preview from file {} for user {} in context {}", id, I(session.getUserId()), I(session.getContextId()));
                numberOfPregeneratedPreviews--;
            } else {
                LOGGER.debug("Found no suitable {} service to trigger preview creation from file {} for user {} in context {}", RemoteInternalPreviewService.class.getSimpleName(), id, I(session.getUserId()), I(session.getContextId()));
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private static final class TimedResultImpl implements TimedResult<File> {

        private final SearchIterator<File> newIter;
        private final long sequenceNumber;

        TimedResultImpl(SearchIterator<File> newIter, long sequenceNumber) {
            super();
            this.newIter = newIter;
            this.sequenceNumber = sequenceNumber;
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

        @Override
        public SearchIterator<File> results() throws OXException {
            return newIter;
        }
    }

}
