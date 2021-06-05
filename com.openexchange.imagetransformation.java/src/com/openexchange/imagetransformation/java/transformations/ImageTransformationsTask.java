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

package com.openexchange.imagetransformation.java.transformations;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.ImageTransformationSignaler;
import com.openexchange.imagetransformation.java.scheduler.Scheduler;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadPools.ExpectedExceptionFactory;

/**
 * {@link ImageTransformationsTask} - A task for an {@link ImageTransformationsImpl} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class ImageTransformationsTask extends ImageTransformationsImpl {

    /** The exception factory constant */
    private static final ExpectedExceptionFactory<IOException> EXCEPTION_FACTORY = new ExpectedExceptionFactory<IOException>() {

        @Override
        public Class<IOException> getType() {
            return IOException.class;
        }

        @Override
        public IOException newUnexpectedError(final Throwable t) {
            if (t instanceof java.util.concurrent.TimeoutException) {
                return new IOException("Image transformation timed out", t);
            }
            String message = t.getMessage();
            return new IOException(null == message ? "Image transformation failed" : message, t);
        }
    };

    // --------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link ImageTransformationsTask}.
     *
     * @param sourceImage The source image
     * @param optSource The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     */
    public ImageTransformationsTask(final BufferedImage sourceImage, final Object optSource) {
        super(sourceImage, optSource);
    }

    /**
     * Initializes a new {@link ImageTransformationsTask}.
     *
     * @param sourceImageStream The image input stream
     * @param optSource The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     */
    public ImageTransformationsTask(final InputStream sourceImageStream, final Object optSource) {
        super(sourceImageStream, optSource);
    }

    /**
     * Initializes a new {@link ImageTransformationsTask}.
     *
     * @param imageFile The image file
     * @param optSource The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     */
    public ImageTransformationsTask(IFileHolder imageFile, Object optSource) {
        super(imageFile, optSource);
    }

    @Override
    protected BufferedImage getImage(String formatName, ImageTransformationSignaler signaler) throws IOException {
        try {
            int waitTimeoutSeconds = waitTimeoutSeconds();
            if (waitTimeoutSeconds <= 0) {
                return getImageWithoutTimeout(formatName, signaler);
            }

            return getImageWithTimeout(formatName, signaler, waitTimeoutSeconds);
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(null == cause ? e : cause);
        }
    }

    private BufferedImage getImageWithoutTimeout(String formatName, ImageTransformationSignaler signaler) throws OXException, IOException {
        // No wait timeout configured
        FutureTask<BufferedImage> ft = new FutureTask<BufferedImage>(new GetImageCallable(formatName, null, signaler));

        // Pass appropriate key object to accumulate tasks for the same caller/session/whatever
        boolean success = Scheduler.getInstance().execute(optSource, ft);
        if (!success) {
            throw new IOException("Image transformation rejected");
        }

        // Get result from scheduled task; waiting as long as it needs
        return ThreadPools.getFrom(ft, EXCEPTION_FACTORY);
    }

    private BufferedImage getImageWithTimeout(String formatName, ImageTransformationSignaler signaler, int waitTimeoutSeconds) throws OXException, IOException, InterruptedIOException {
        // Wait timeout configured - Use a CountDownLatch to let timeout jump in if actual image processing happens
        CountDownLatch latch = new CountDownLatch(1);
        FutureTask<BufferedImage> ft = new FutureTask<BufferedImage>(new GetImageCallable(formatName, latch, signaler));

        // Pass appropriate key object to accumulate tasks for the same caller/session/whatever
        boolean success = Scheduler.getInstance().execute(optSource, ft);
        if (!success) {
            throw new IOException("Image transformation rejected");
        }

        // Await until actual processing happens
        try {
            latch.await();
        } catch (InterruptedException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
            InterruptedIOException ioe = new InterruptedIOException("Awaiting image transformation interrupted");
            ioe.initCause(e);
            throw ioe;
        }

        // Get result from scheduled task; waiting for at most the configured time for the computation to complete
        return ThreadPools.getFrom(ft, waitTimeoutSeconds, TimeUnit.SECONDS, EXCEPTION_FACTORY);
    }

    /**
     * Gets the resulting image after applying all transformations.
     *
     * @param formatName the image format to use, or <code>null</code> if not relevant
     * @param signaler The optional signaler or <code>null</code>
     * @return The transformed image
     * @throws IOException if an I/O error occurs
     */
    protected BufferedImage doGetImage(String formatName, ImageTransformationSignaler signaler) throws IOException {
        return super.getImage(formatName, signaler);
    }

    // --------------------------------------------------------------------------------------------------------- //

    private final class GetImageCallable implements Callable<BufferedImage> {

        private final String formatName;
        private final LatchBackedSignaler signaler;
        private final CountDownLatch optLatch;

        GetImageCallable(String formatName, CountDownLatch optLatch, ImageTransformationSignaler delegate) {
            super();
            this.formatName = formatName;
            this.optLatch = optLatch;
            this.signaler = new LatchBackedSignaler(optLatch, delegate);
        }

        @Override
        public BufferedImage call() throws Exception {
            CountDownLatch latch = this.optLatch;
            if (null == latch) {
                return doGetImage(formatName, signaler);
            }

            // If not null, ensure that latch is counted down
            try {
                return doGetImage(formatName, signaler);
            } finally {
                latch.countDown();
            }
        }
    }

    private static final class LatchBackedSignaler implements ImageTransformationSignaler {

        private final ImageTransformationSignaler delegate;
        private final CountDownLatch optLatch;

        LatchBackedSignaler(CountDownLatch optLatch, ImageTransformationSignaler delegate) {
            super();
            this.optLatch = optLatch;
            this.delegate = delegate;
        }

        @Override
        public void onImageRead() {
            // Delegate
            ImageTransformationSignaler delegate = this.delegate;
            if (null != delegate) {
                try { delegate.onImageRead(); } catch (Exception x) { /* ignore */ }
            }

            // Count down latch
            CountDownLatch latch = this.optLatch;
            if (null != latch) {
                latch.countDown();
            }
        }
    }

}
