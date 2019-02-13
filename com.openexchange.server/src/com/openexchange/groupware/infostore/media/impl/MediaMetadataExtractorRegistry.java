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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware.infostore.media.impl;

import static com.openexchange.java.Autoboxing.I;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.media.EstimationResult;
import com.openexchange.groupware.infostore.media.ExtractorResult;
import com.openexchange.groupware.infostore.media.FileStorageInputStreamProvider;
import com.openexchange.groupware.infostore.media.InputStreamProvider;
import com.openexchange.groupware.infostore.media.MediaMetadataExtractor;
import com.openexchange.groupware.infostore.media.MediaMetadataExtractorService;
import com.openexchange.groupware.infostore.media.MediaMetadataExtractors;
import com.openexchange.groupware.infostore.media.impl.control.ExtractAndApplyMediaMetadataTask;
import com.openexchange.groupware.infostore.media.impl.control.ExtractControl;
import com.openexchange.groupware.infostore.media.impl.control.ExtractControlTask;
import com.openexchange.groupware.infostore.media.impl.processing.StripedProcessor;
import com.openexchange.java.Streams;
import com.openexchange.java.util.Tools;
import com.openexchange.log.LogProperties;
import com.openexchange.session.Session;

/**
 * {@link MediaMetadataExtractorRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MediaMetadataExtractorRegistry implements MediaMetadataExtractorService {

    /** The logger constant */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MediaMetadataExtractorRegistry.class);

    private final CopyOnWriteArrayList<MediaMetadataExtractor> extractors;
    private final StripedProcessor stripedProcessor;
    private final int extractionTimeoutSec;
    private volatile Thread extractControlRunner;

    /**
     * Initializes a new {@link MediaMetadataExtractorRegistry}.
     *
     * @param extractionTimeoutSec The extraction timeout in seconds
     */
    public MediaMetadataExtractorRegistry(int extractionTimeoutSec) {
        super();
        extractors = new CopyOnWriteArrayList<MediaMetadataExtractor>();
        stripedProcessor = new StripedProcessor("Media-Extractor", 20);
        this.extractionTimeoutSec = extractionTimeoutSec <= 0 ? 0 : extractionTimeoutSec;
    }

    /**
     * Starts this extractor/registry
     */
    public void start() {
        // Start-up stuff (if any)
    }

    /**
     * Stops this extractor/registry
     */
    public void stop() {
        stripedProcessor.stop();
        if (extractionTimeoutSec > 0) {
            ExtractControl.getInstance().add(ExtractAndApplyMediaMetadataTask.POISON);
        }
        Thread extractControlRunner = this.extractControlRunner;
        if (null != extractControlRunner) {
            this.extractControlRunner = null;
            extractControlRunner.interrupt();
        }
    }

    /**
     * Adds given extractor
     *
     * @param extractor The extractor to add
     */
    public void addExtractor(MediaMetadataExtractor extractor) {
        extractors.add(extractor);
    }

    /**
     * Removes given extractor
     *
     * @param extractor The extractor to remove
     */
    public void removeExtractor(MediaMetadataExtractor extractor) {
        extractors.remove(extractor);
    }

    @Override
    public EstimationResult estimateEffort(InputStreamProvider provider, DocumentMetadata document) throws OXException {
        if (null == provider) {
            throw OXException.general("Stream must not be null.");
        }

        LOGGER.debug("Going to estimate effort of extracting media metadata from document {} ({}) with version {}", I(document.getId()), document.getFileName(), I(document.getVersion()));

        boolean close = true;
        InputStream in = null;
        BufferedInputStream bufferedStream = null;
        try {
            Map<String, Object> arguments = null;
            MediaMetadataExtractor lowEffortExtractor = null;
            boolean any = false;
            for (MediaMetadataExtractor extractor : extractors) {
                if (extractor.isApplicable(document)) {
                    if (null == bufferedStream) {
                        // First iteration...
                        int bufferSize = 8192; // 8KB
                        in = provider.getInputStream();
                        bufferedStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, bufferSize);
                        bufferedStream.mark(bufferSize);
                    } else {
                        // Reset buffered stream for re-use
                        try {
                            bufferedStream.reset();
                        } catch (Exception e) {
                            // Reset failed
                            LOGGER.debug("Failed to reset stream for estimating effort of extracting media metadata from document {} with version {}", I(document.getId()), I(document.getVersion()), e);
                            Streams.close(bufferedStream, in);
                            int bufferSize = 8192; // 8KB
                            in = provider.getInputStream();
                            bufferedStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, bufferSize);
                            bufferedStream.mark(bufferSize);
                        }
                    }

                    if (null == arguments) {
                        arguments = new HashMap<String, Object>();
                    }

                    switch (extractor.estimateEffort(bufferedStream, document, arguments)) {
                        case LOW_EFFORT:
                            any = true;
                            lowEffortExtractor = extractor;
                            break;
                        case HIGH_EFFORT:
                            any = true;
                            break;
                        default:
                            // Nothing
                            break;
                    }
                }
            }

            if (null == bufferedStream || !any) {
                // No extractors or not applicable
                LOGGER.debug("No suitable extractor or not applicable to extract media metadata from document {} ({}) with version {}", I(document.getId()), document.getFileName(), I(document.getVersion()));
                return EstimationResult.notApplicable();
            }

            if (null == lowEffortExtractor) {
                // No extractor offers low effort processing
                LOGGER.debug("High effort to extract media metadata from document {} ({}) with version {}", I(document.getId()), document.getFileName(), I(document.getVersion()));
                return EstimationResult.highEffortFor(arguments);
            }

            // Reset buffered stream for re-use
            try {
                bufferedStream.reset();
            } catch (Exception e) {
                // Reset failed
                LOGGER.debug("Failed to reset stream to re-use for extracting media metadata from document {} with version {}", I(document.getId()), I(document.getVersion()), e);
                Streams.close(bufferedStream, in);
                bufferedStream = null;
            }
            EstimationResult result = EstimationResult.lowEffortFor(bufferedStream, lowEffortExtractor, arguments);
            LOGGER.debug("Low effort to extract metadata from document {} ({}) with version {} using {}", I(document.getId()), document.getFileName(), I(document.getVersion()), lowEffortExtractor.getClass().getName());
            close = false;
            return result;
        } catch (RuntimeException e) {
            throw InfostoreExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            // Close stream(s)
            if (close) {
                Streams.close(bufferedStream, in);
            }
        }
    }

    @Override
    public ExtractorResult extractAndApplyMediaMetadataUsing(MediaMetadataExtractor extractor, InputStream optStream, InputStreamProvider inProvider, DocumentMetadata document, Map<String, Object> optArguments) throws OXException {
        if (null == extractor) {
            throw OXException.general("Extractor must not be null.");
        }
        if (optStream == null && inProvider == null) {
            throw OXException.general("Stream must not be null.");
        }
        if (null == document) {
            throw OXException.general("Document must not be null.");
        }

        int timeout = this.extractionTimeoutSec;
        startControlThreadIfStopped(timeout);

        if (timeout <= 0) {
            return extractor.extractAndApplyMediaMetadata(optStream, inProvider, document, optArguments);
        }

        int contextId = Tools.getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID));
        return new ExtractAndApplyMediaMetadataTask(timeout, extractor, optStream, inProvider, document, optArguments, contextId).call();
    }

    @Override
    public ExtractorResult extractAndApplyMediaMetadata(InputStreamProvider inProvider, DocumentMetadata document, Map<String, Object> optArguments) throws OXException {
        if (null == inProvider) {
            throw OXException.general("Stream must not be null.");
        }
        if (null == document) {
            throw OXException.general("Document must not be null.");
        }

        LOGGER.debug("Going to extract media metadata from document {} ({}) with version {}", I(document.getId()), document.getFileName(), I(document.getVersion()));

        int timeout = this.extractionTimeoutSec;
        startControlThreadIfStopped(timeout);

        for (MediaMetadataExtractor extractor : extractors) {
            if (extractor.isApplicable(document)) {
                ExtractorResult extractorResult;
                if (timeout <= 0) {
                    extractorResult = extractor.extractAndApplyMediaMetadata(null, inProvider, document, optArguments);
                } else {
                    // Run as a monitored task
                    int contextId = Tools.getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID));
                    extractorResult = new ExtractAndApplyMediaMetadataTask(timeout, extractor, null, inProvider, document, optArguments, contextId).call();
                }

                if (ExtractorResult.NONE != extractorResult) {
                    if (ExtractorResult.SUCCESSFUL == extractorResult) {
                        LOGGER.debug("Successfully extracted media metadata from document {} ({}) with version {} using {}", I(document.getId()), document.getFileName(), I(document.getVersion()), extractor.getClass().getName());
                    } else if (ExtractorResult.INTERRUPTED == extractorResult) {
                        LOGGER.debug("Interrupted while extracting media metadata from document {} ({}) with version {} using {}", I(document.getId()), document.getFileName(), I(document.getVersion()), extractor.getClass().getName());
                    } else {
                        LOGGER.debug("Accepted, but failed to extract media metadata from document {} ({}) with version {} using {}", I(document.getId()), document.getFileName(), I(document.getVersion()), extractor.getClass().getName());
                    }
                    return extractorResult;
                }
            }
        }

        LOGGER.debug("No suitable extractor to extract media metadata from document {} ({}) with version {}", I(document.getId()), document.getFileName(), I(document.getVersion()));
        return ExtractorResult.NONE;
    }

    private void startControlThreadIfStopped(int timeout) {
        if (timeout > 0) {
            // Ensure control thread is running
            Thread extractControlRunner = this.extractControlRunner;
            if (null == extractControlRunner) {
                synchronized (this) {
                    extractControlRunner = this.extractControlRunner;
                    if (null == extractControlRunner) {
                        extractControlRunner = new Thread(new ExtractControlTask(), "ExtractControl");
                        extractControlRunner.start();
                        this.extractControlRunner = extractControlRunner;
                    }
                }
            }
        }
    }

    // ------------------------------------------- More methods for job scheduling etc. ----------------------------------------------------

    private static String generateTaskKey(int version, int documentId, Session session) {
        return new StringBuilder(16).append(documentId).append('-').append(version).append('@').append(session.getContextId()).toString();
    }

    @Override
    public String scheduleMediaMetadataExtraction(final DocumentMetadata document, final FileStorage fileStorage, final Map<String, Object> optArguments, final Session session) throws OXException {
        if (null == document) {
            throw OXException.general("Document must not be null.");
        }
        if (null == session) {
            throw OXException.general("Session must not be null.");
        }

        LOGGER.debug("Going to schedule media metadata from document {} ({}) with version {}", I(document.getId()), document.getFileName(), I(document.getVersion()));

        String taskKey = generateTaskKey(document.getVersion(), document.getId(), session);
        Runnable task = new MediaMetadataExtractionTask(document, fileStorage, optArguments, session, this);
        stripedProcessor.execute(taskKey, task, session);
        return taskKey;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class MediaMetadataExtractionTask implements Runnable {

        private final Session session;
        private final DocumentMetadata document;
        private final FileStorage fileStorage;
        private final MediaMetadataExtractorRegistry registry;
        private final Map<String, Object> optArguments;

        MediaMetadataExtractionTask(DocumentMetadata document, FileStorage fileStorage, Map<String, Object> optArguments, Session session, MediaMetadataExtractorRegistry registry) {
            super();
            this.optArguments = optArguments;
            this.session = session;
            this.document = document;
            this.fileStorage = fileStorage;
            this.registry = registry;
        }

        @Override
        public void run() {
            try {
                DocumentMetadataImpl documentToPass = new DocumentMetadataImpl(document);

                try {
                    ExtractorResult extractorResult = registry.extractAndApplyMediaMetadata(new FileStorageInputStreamProvider(document.getFilestoreLocation(), fileStorage), documentToPass, optArguments);
                    switch (extractorResult) {
                        case SUCCESSFUL:
                            documentToPass.setMediaStatus(MediaStatus.success());
                            break;
                        case INTERRUPTED:
                            Thread.interrupted();
                            //$FALL-THROUGH$
                        case ACCEPTED_BUT_FAILED:
                            {
                                documentToPass.setMediaStatus(MediaStatus.failure());
                                documentToPass.setCaptureDate(null);
                                documentToPass.setGeoLocation(null);
                                documentToPass.setWidth(-1);
                                documentToPass.setHeight(-1);
                                documentToPass.setCameraIsoSpeed(-1);
                                documentToPass.setCameraAperture(-1);
                                documentToPass.setCameraExposureTime(-1);
                                documentToPass.setCameraFocalLength(-1);
                                documentToPass.setCameraModel(null);
                                documentToPass.setMediaMeta(null);
                            }
                            break;
                        case NONE: // fall-through
                        default:
                            {
                                documentToPass.setMediaStatus(MediaStatus.none());
                                documentToPass.setCaptureDate(null);
                                documentToPass.setGeoLocation(null);
                                documentToPass.setWidth(-1);
                                documentToPass.setHeight(-1);
                                documentToPass.setCameraIsoSpeed(-1);
                                documentToPass.setCameraAperture(-1);
                                documentToPass.setCameraExposureTime(-1);
                                documentToPass.setCameraFocalLength(-1);
                                documentToPass.setCameraModel(null);
                                documentToPass.setMediaMeta(null);
                            }
                            break;

                    }
                } catch (OXException | RuntimeException e) {
                    LOGGER.error("Failed to extract media metadata from document {} ({}) with version {} in context {}", I(document.getId()), document.getFileName(), I(document.getVersion()), I(session.getContextId()), e);
                    documentToPass.setMediaStatus(MediaStatus.error());
                    documentToPass.setCaptureDate(null);
                    documentToPass.setGeoLocation(null);
                    documentToPass.setWidth(-1);
                    documentToPass.setHeight(-1);
                    documentToPass.setCameraIsoSpeed(-1);
                    documentToPass.setCameraAperture(-1);
                    documentToPass.setCameraExposureTime(-1);
                    documentToPass.setCameraFocalLength(-1);
                    documentToPass.setCameraModel(null);
                    documentToPass.setMediaMeta(null);
                }

                documentToPass.setSequenceNumber(document.getSequenceNumber());
                MediaMetadataExtractors.saveMediaMetaDataFromDocument(documentToPass, session);
            } catch (Exception e) {
                LOGGER.error("Failed to apply media metadata to document {} ({}) with version {} in context {}", I(document.getId()), document.getFileName(), I(document.getVersion()), I(session.getContextId()), e);
            }
        }
    }

}
