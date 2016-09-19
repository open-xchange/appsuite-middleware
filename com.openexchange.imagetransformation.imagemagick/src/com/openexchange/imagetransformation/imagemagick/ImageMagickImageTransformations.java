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

package com.openexchange.imagetransformation.imagemagick;

import static com.openexchange.imagetransformation.Utility.getImageFormat;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Stream2BufferedImage;
import org.im4java.process.InputProvider;
import org.im4java.process.OutputConsumer;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.BasicTransformedImage;
import com.openexchange.imagetransformation.Constants;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.imagetransformation.TransformationContext;
import com.openexchange.imagetransformation.TransformedImage;
import com.openexchange.imagetransformation.TransformedImageCreator;
import com.openexchange.imagetransformation.Utility;
import com.openexchange.java.Streams;
import com.openexchange.processing.Processor;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadPools.ExpectedExceptionFactory;


/**
 * {@link ImageMagickImageTransformations}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ImageMagickImageTransformations implements ImageTransformations {

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

    // ----------------------------------------------------------------------------------------------------------------------

    private final boolean doNothing;

    private final Processor processor;
    final TransformedImageCreator transformedImageCreator;
    private final int timeoutSecs;

    final ConvertCmd cmd;
    final IMOperation op;

    final InputStream sourceImageStream;
    final IFileHolder sourceImageFile;
    private final BufferedImage sourceImage;
    private final Object optSource;

    private boolean compress;

    /**
     * Initializes a new {@link ImageMagickImageTransformations}.
     */
    public ImageMagickImageTransformations(BufferedImage sourceImage, Object optSource, TransformedImageCreator transformedImageCreator, String searchPath, boolean useGraphicsMagick, int timeoutSecs, Processor processor, boolean doNothing) {
        super();
        this.doNothing = doNothing;
        this.processor = processor;
        this.timeoutSecs = timeoutSecs;
        this.transformedImageCreator = transformedImageCreator;
        ConvertCmd cmd = new ConvertCmd(useGraphicsMagick);
        cmd.setSearchPath(searchPath);
        this.cmd = cmd;
        this.op = new IMOperation();
        op.addImage();

        this.sourceImage = sourceImage;
        this.sourceImageFile = null;
        this.sourceImageStream = null;

        this.optSource = optSource;
    }

    /**
     * Initializes a new {@link ImageMagickImageTransformations}.
     */
    public ImageMagickImageTransformations(IFileHolder sourceImageFile, Object optSource, TransformedImageCreator transformedImageCreator, String searchPath, boolean useGraphicsMagick, int timeoutSecs, Processor processor, boolean doNothing) {
        super();
        this.doNothing = doNothing;
        this.processor = processor;
        this.timeoutSecs = timeoutSecs;
        this.transformedImageCreator = transformedImageCreator;
        ConvertCmd cmd = new ConvertCmd(useGraphicsMagick);
        cmd.setSearchPath(searchPath);
        this.cmd = cmd;
        this.op = new IMOperation();
        op.addImage("-"); // read from stdin

        this.sourceImage = null;
        this.sourceImageFile = sourceImageFile;
        this.sourceImageStream = null;

        this.optSource = optSource;
    }

    /**
     * Initializes a new {@link ImageMagickImageTransformations}.
     */
    public ImageMagickImageTransformations(InputStream sourceImageStream, Object optSource, TransformedImageCreator transformedImageCreator, String searchPath, boolean useGraphicsMagick, int timeoutSecs, Processor processor, boolean doNothing) {
        super();
        this.doNothing = doNothing;
        this.processor = processor;
        this.timeoutSecs = timeoutSecs;
        this.transformedImageCreator = transformedImageCreator;
        ConvertCmd cmd = new ConvertCmd(useGraphicsMagick);
        cmd.setSearchPath(searchPath);
        this.cmd = cmd;
        this.op = new IMOperation();
        op.addImage("-"); // read from stdin

        this.sourceImage = null;
        this.sourceImageFile = null;
        this.sourceImageStream = sourceImageStream;

        this.optSource = optSource;
    }

    private void awaitCountDown(CountDownLatch latch) throws IOException {
        if (null != latch) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("I/O operation has been interrupted.", e);
            }
        }
    }

    private <V> void execute(FutureTask<V> task, CountDownLatch optLatch) throws IOException {
        processor.execute(optSource, task);
        awaitCountDown(optLatch);
    }

    @Override
    public ImageTransformations rotate() {
        if (doNothing) {
            return this;
        }

        op.autoOrient();
        return this;
    }

    @Override
    public ImageTransformations scale(int maxWidth, int maxHeight, ScaleType scaleType) {
        return scale(maxHeight, maxHeight, scaleType, false);
    }

    @Override
    public ImageTransformations scale(int maxWidth, int maxHeight, ScaleType scaleType, boolean shrinkOnly) {
        if (maxWidth > Constants.getMaxWidth()) {
            throw new IllegalArgumentException("Width " + maxWidth + " exceeds max. supported width " + Constants.getMaxWidth());
        }
        if (maxHeight > Constants.getMaxHeight()) {
            throw new IllegalArgumentException("Height " + maxHeight + " exceeds max. supported height " + Constants.getMaxHeight());
        }

        if (doNothing) {
            return this;
        }

        switch (scaleType) {
            case CONTAIN:
                op.scale(Integer.valueOf(maxWidth), Integer.valueOf(maxHeight));
                break;
            case CONTAIN_FORCE_DIMENSION:
                // http://www.imagemagick.org/Usage/resize/
                // http://www.imagemagick.org/Usage/thumbnails/#fit_summery
                op.resize(Integer.valueOf(maxWidth), Integer.valueOf(maxHeight), Character.valueOf('^'));
                op.gravity("center");
                op.extent(Integer.valueOf(maxWidth), Integer.valueOf(maxHeight));
                break;
            case COVER:
                op.thumbnail(Integer.valueOf(maxWidth), Integer.valueOf(maxHeight));
                break;
            case AUTO:
                // fall-through
            default:
                op.scale(Integer.valueOf(maxWidth), Integer.valueOf(maxHeight));
                break;

        }

        return this;
    }

    @Override
    public ImageTransformations crop(int x, int y, int width, int height) {
        if (doNothing) {
            return this;
        }

        op.crop(Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(x), Integer.valueOf(y));
        return this;
    }

    @Override
    public ImageTransformations compress() {
        if (doNothing) {
            return this;
        }

        compress = true;
        return this;
    }

    void runCommand() throws IOException {
        try {
            if (null != sourceImage) {
                cmd.run(op, sourceImage);
            } else if (null != sourceImageFile) {
                cmd.setInputProvider(new FileInputProvider(sourceImageFile));
                cmd.run(op);
            } else {
                cmd.setInputProvider(new StreamInputProvider(sourceImageStream));
                cmd.run(op);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("I/O operation has been interrupted.", e);
        } catch (IM4JavaException e) {
            throw new IOException("ImageMagick error.", e);
        }
    }

    BufferedImage getImage(String formatName) throws IOException {
        String frmtName = getImageFormat(formatName);

        if (doNothing) {
            // -alpha off
            if (false == Utility.supportsTransparency(frmtName)) {
                op.alpha("off");
            }
        }
        op.addImage(frmtName + ":-");

        final CountDownLatch latch = new CountDownLatch(1);
        FutureTask<BufferedImage> task = new FutureTask<>(new Callable<BufferedImage>() {

            @Override
            public BufferedImage call() throws Exception {
                latch.countDown();
                Stream2BufferedImage s2b = new Stream2BufferedImage();
                cmd.setOutputConsumer(s2b);

                runCommand();

                return s2b.getImage();
            }
        });
        execute(task, latch);

        return ThreadPools.getFrom(task, timeoutSecs, TimeUnit.SECONDS, EXCEPTION_FACTORY);
    }

    @Override
    public BufferedImage getImage() throws IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        FutureTask<BufferedImage> task = new FutureTask<>(new Callable<BufferedImage>() {

            @Override
            public BufferedImage call() throws Exception {
                latch.countDown();
                return getImage("png");
            }
        });
        execute(task, latch);

        return ThreadPools.getFrom(task, timeoutSecs, TimeUnit.SECONDS, EXCEPTION_FACTORY);
    }

    @Override
    public byte[] getBytes(String formatName) throws IOException {
        String frmtName = getImageFormat(formatName);

        if (doNothing) {
            // -alpha off
            if (false == Utility.supportsTransparency(frmtName)) {
                op.alpha("off");
            }
        }
        op.addImage(frmtName + ":-");

        final CountDownLatch latch = new CountDownLatch(1);
        FutureTask<byte[]> task = new FutureTask<>(new Callable<byte[]>() {

            @Override
            public byte[] call() throws Exception {
                latch.countDown();
                ByteCollectingOutputConsumer bytes = new ByteCollectingOutputConsumer();
                cmd.setOutputConsumer(bytes);

                runCommand();

                return bytes.toByteArray();
            }
        });
        execute(task, latch);

        return ThreadPools.getFrom(task, timeoutSecs, TimeUnit.SECONDS, EXCEPTION_FACTORY);
    }

    @Override
    public InputStream getInputStream(String formatName) throws IOException {
        String frmtName = getImageFormat(formatName);

        if (doNothing) {
            // -alpha off
            if (false == Utility.supportsTransparency(frmtName)) {
                op.alpha("off");
            }
        }
        op.addImage(frmtName + ":-");

        if (null != sourceImage) {
            final CountDownLatch latch = new CountDownLatch(1);
            FutureTask<InputStream> task = new FutureTask<>(new Callable<InputStream>() {

                @Override
                public InputStream call() throws Exception {
                    latch.countDown();
                    ByteCollectingOutputConsumer bytes = new ByteCollectingOutputConsumer();
                    cmd.setOutputConsumer(bytes);

                    runCommand();

                    return bytes.asInputStream();
                }
            });
            execute(task, latch);

            return ThreadPools.getFrom(task, timeoutSecs, TimeUnit.SECONDS, EXCEPTION_FACTORY);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        FutureTask<InputStream> task = new FutureTask<>(new Callable<InputStream>() {

            @Override
            public InputStream call() throws Exception {
                InputStream is = null;
                ThresholdFileHolder sink = null;
                try {
                    is = null != sourceImageStream ? sourceImageStream : sourceImageFile.getStream();
                    sink = new ThresholdFileHolder(false);

                    //PipedOutputStream pos = new PipedOutputStream();
                    //ExceptionAwarePipedInputStream pin = new ExceptionAwarePipedInputStream(pos, 65536);

                    PipeIn pipeIn = new PipeIn(is, latch);
                    PipeOut pipeOut = new PipeOut(sink.asOutputStream());

                    // Set up command
                    cmd.setInputProvider(pipeIn);
                    cmd.setOutputConsumer(pipeOut);

                    cmd.run(op);

                    InputStream retval = sink.getClosingStream();
                    sink = null;
                    return retval;
                } catch (OXException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof IOException) {
                        throw (IOException) cause;
                    }
                    throw null == cause ? new IOException(e.getMessage(), e) : new IOException(cause.getMessage(), cause);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("I/O operation has been interrupted.", e);
                } catch (IM4JavaException e) {
                    throw new IOException("ImageMagick error.", e);
                } finally {
                    latch.countDown(); // For safety reason
                    Streams.close(is, sink);
                }
            }
        });
        execute(task, latch);

        return ThreadPools.getFrom(task, timeoutSecs, TimeUnit.SECONDS, EXCEPTION_FACTORY);
    }

    @Override
    public BasicTransformedImage getTransformedImage(String formatName) throws IOException {
        final String frmtName = getImageFormat(formatName);

        if (doNothing) {
            // -alpha off
            if (false == Utility.supportsTransparency(frmtName)) {
                op.alpha("off");
            }
        }
        op.addImage(frmtName + ":-");

        if (null != sourceImage) {
            final CountDownLatch latch = new CountDownLatch(1);
            FutureTask<BasicTransformedImage> task = new FutureTask<>(new Callable<BasicTransformedImage>() {

                @Override
                public BasicTransformedImage call() throws Exception {
                    latch.countDown();
                    ByteCollectingOutputConsumer bytes = new ByteCollectingOutputConsumer();

                    cmd.setOutputConsumer(bytes);
                    runCommand();

                    return new BasicTransformedImageImpl(frmtName, bytes.getSink());
                }
            });
            execute(task, latch);

            return ThreadPools.getFrom(task, timeoutSecs, TimeUnit.SECONDS, EXCEPTION_FACTORY);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        FutureTask<BasicTransformedImage> task = new FutureTask<>(new Callable<BasicTransformedImage>() {

            @Override
            public BasicTransformedImage call() throws Exception {
                InputStream is = null;
                ThresholdFileHolder sink = null;
                try {
                    is = null != sourceImageStream ? sourceImageStream : sourceImageFile.getStream();
                    sink = new ThresholdFileHolder(false);

                    PipeIn pipeIn = new PipeIn(is, latch);
                    PipeOut pipeOut = new PipeOut(sink.asOutputStream());

                    // Set up command
                    cmd.setInputProvider(pipeIn);
                    cmd.setOutputConsumer(pipeOut);

                    cmd.run(op);

                    BasicTransformedImage retval = new BasicTransformedImageImpl(frmtName, sink);
                    sink = null;
                    return retval;
                } catch (OXException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof IOException) {
                        throw (IOException) cause;
                    }
                    throw null == cause ? new IOException(e.getMessage(), e) : new IOException(cause.getMessage(), cause);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("I/O operation has been interrupted.", e);
                } catch (IM4JavaException e) {
                    throw new IOException("ImageMagick error.", e);
                } finally {
                    latch.countDown(); // For safety reason
                    Streams.close(is, sink);
                }
            }
        });
        execute(task, latch);

        return ThreadPools.getFrom(task, timeoutSecs, TimeUnit.SECONDS, EXCEPTION_FACTORY);
    }

    @Override
    public TransformedImage getFullTransformedImage(String formatName) throws IOException {
        final String frmtName = getImageFormat(formatName);

        final CountDownLatch latch = new CountDownLatch(1);
        FutureTask<TransformedImage> task = new FutureTask<>(new Callable<TransformedImage>() {

            @Override
            public TransformedImage call() throws Exception {
                latch.countDown();
                BufferedImage bufferedImage = getImage(frmtName);
                return transformedImageCreator.writeTransformedImage(bufferedImage, frmtName, new TransformationContext(), needsCompression(frmtName));
            }
        });
        execute(task, latch);

        return ThreadPools.getFrom(task, timeoutSecs, TimeUnit.SECONDS, EXCEPTION_FACTORY);
    }

    boolean needsCompression(String formatName) {
        return this.compress && null != formatName && "jpeg".equalsIgnoreCase(formatName) || "jpg".equalsIgnoreCase(formatName);
    }

    // --------------------------------------------------------------------------------------------------------

    private static class FileInputProvider implements InputProvider {

        private final IFileHolder file;

        FileInputProvider(IFileHolder file) {
            super();
            this.file = file;
        }

        @Override
        public void provideInput(OutputStream pOutputStream) throws IOException {
            InputStream in = getFileStream(file);
            try {
                int buflen = 2048;
                byte[] buf = new byte[buflen];
                for (int read; (read = in.read(buf, 0, buflen)) > 0;) {
                    pOutputStream.write(buf, 0, read);
                }
                pOutputStream.flush();
            } finally {
                Streams.close(in);
            }
        }

        private InputStream getFileStream(IFileHolder file) throws IOException {
            if (null == file) {
                return null;
            }
            try {
                return file.getStream();
            } catch (OXException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw null == cause ? new IOException(e.getMessage(), e) : new IOException(cause.getMessage(), cause);
            }
        }
    }

    private static class StreamInputProvider implements InputProvider {

        private final InputStream in;

        StreamInputProvider(InputStream in) {
            super();
            this.in = in;
        }

        @Override
        public void provideInput(OutputStream pOutputStream) throws IOException {
            try {
                int buflen = 2048;
                byte[] buf = new byte[buflen];
                for (int read; (read = in.read(buf, 0, buflen)) > 0;) {
                    pOutputStream.write(buf, 0, read);
                }
                pOutputStream.flush();
            } finally {
                Streams.close(in);
            }
        }
    }

    private static class ByteCollectingOutputConsumer implements OutputConsumer {

        private final ThresholdFileHolder sink;

        ByteCollectingOutputConsumer() {
            super();
            sink = new ThresholdFileHolder(false);
        }

        /**
         * Gets the sink
         *
         * @return The sink
         */
        ThresholdFileHolder getSink() {
            return sink;
        }

        @Override
        public void consumeOutput(InputStream in) throws IOException {
            try {
                int buflen = 2048;
                byte[] buf = new byte[buflen];
                for (int read; (read = in.read(buf, 0, buflen)) > 0;) {
                    write(buf, 0, read, sink);
                }
            } finally {
                Streams.close(in);
            }
        }

        private void write(byte[] buf, int off, int len, ThresholdFileHolder sink) throws IOException {
            try {
                sink.write(buf, off, len);
            } catch (OXException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw null == cause ? new IOException(e.getMessage(), e) : new IOException(cause.getMessage(), cause);
            }
        }

        byte[] toByteArray() throws IOException {
            try {
                return sink.toByteArray();
            } catch (OXException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw null == cause ? new IOException(e.getMessage(), e) : new IOException(cause.getMessage(), cause);
            }
        }

        InputStream asInputStream() throws IOException {
            try {
                return sink.getClosingStream();
            } catch (OXException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw null == cause ? new IOException(e.getMessage(), e) : new IOException(cause.getMessage(), cause);
            }
        }
    }

    private static class BasicTransformedImageImpl implements BasicTransformedImage {

        private final String frmtName;
        private final ThresholdFileHolder sink;

        BasicTransformedImageImpl(String frmtName, ThresholdFileHolder sink) {
            this.frmtName = frmtName;
            this.sink = sink;
        }

        @Override
        public int getTransformationExpenses() {
            return ImageTransformations.LOW_EXPENSE;
        }

        @Override
        public long getSize() {
            return sink.getLength();
        }

        @Override
        public InputStream getImageStream() throws OXException {
            return sink.getStream();
        }

        @Override
        public IFileHolder getImageFile() {
            return sink;
        }

        @Override
        public byte[] getImageData() throws OXException {
            return sink.toByteArray();
        }

        @Override
        public String getFormatName() {
            return frmtName;
        }

        @Override
        public void close() {
            sink.close();
        }
    }

}
