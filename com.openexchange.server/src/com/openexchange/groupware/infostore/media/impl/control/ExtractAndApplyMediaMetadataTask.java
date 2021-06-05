/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.infostore.media.impl.control;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.media.ExtractorResult;
import com.openexchange.groupware.infostore.media.InputStreamProvider;
import com.openexchange.groupware.infostore.media.MediaMetadataExtractor;


/**
 * {@link ExtractAndApplyMediaMetadataTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ExtractAndApplyMediaMetadataTask implements Callable<ExtractorResult>, Delayed {

    /** The special poison task to stop taking from queue */
    public static final ExtractAndApplyMediaMetadataTask POISON = new ExtractAndApplyMediaMetadataTask(0, null, null, null, null, null, -1) {

        @Override
        public int compareTo(Delayed o) {
            return -1;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0L;
        }
    };

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final InputStream optStream;
    private final MediaMetadataExtractor extractor;
    private final InputStreamProvider inProvider;
    private final DocumentMetadata document;
    private final Map<String, Object> optArguments;
    private final int timeoutSec;
    private final int contextId;
    private volatile long stamp;
    private volatile Thread worker;

    /**
     * Initializes a new {@link ExtractAndApplyMediaMetadataTask}.
     */
    public ExtractAndApplyMediaMetadataTask(int timeoutSec, MediaMetadataExtractor extractor, InputStream optStream, InputStreamProvider inProvider, DocumentMetadata document, Map<String, Object> optArguments, int contextId) {
        super();
        this.timeoutSec = timeoutSec;
        this.optStream = optStream;
        this.extractor = extractor;
        this.inProvider = inProvider;
        this.document = document;
        this.optArguments = optArguments;
        this.contextId = contextId;
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the document.
     *
     * @return The document
     */
    public DocumentMetadata getDocument() {
        return document;
    }

    @Override
    public int compareTo(Delayed o) {
        long thisStamp = this.stamp;
        long otherStamp = ((ExtractAndApplyMediaMetadataTask) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long toGo = stamp - System.currentTimeMillis();
        return unit.convert(toGo, TimeUnit.MILLISECONDS);
    }

    @Override
    public ExtractorResult call() throws OXException {
        ExtractControl control = ExtractControl.getInstance();
        stamp = System.currentTimeMillis() + (timeoutSec * 1000L);
        worker = Thread.currentThread();
        control.add(this);
        try {
            return extractor.extractAndApplyMediaMetadata(optStream, inProvider, document, optArguments);
        } finally {
            worker = null;
            control.remove(this);
        }
    }

    /**
     * Interrupts this task (if currently processed).
     */
    public void interrupt() {
        Thread worker = this.worker;
        if (null != worker) {
            worker.interrupt();
        }
    }

}
