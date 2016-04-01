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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.Pipe;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.ImageTransformationDeniedIOException;
import com.openexchange.imagetransformation.ImageTransformationIdler;
import com.openexchange.imagetransformation.ImageTransformationProvider;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.TransformedImageCreator;
import com.openexchange.imagetransformation.imagemagick.osgi.ImageMagickRegisterer;
import com.openexchange.imagetransformation.imagemagick.osgi.Services;
import com.openexchange.java.Streams;
import com.openexchange.processing.Processor;
import com.openexchange.processing.ProcessorService;

/**
 * {@link ImageMagickImageTransformationProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ImageMagickImageTransformationProvider implements ImageTransformationProvider, ImageTransformationIdler {

    private volatile Processor processor;
    private final TransformedImageCreator transformedImageCreator;
    private final AtomicReference<String> searchPathRef;
    private final AtomicBoolean useGraphicsMagickRef;
    private final AtomicInteger numThreadsRef;
    private final AtomicInteger timeoutSecsRef;

    /**
     * Initializes a new {@link ImageMagickImageTransformationProvider}.
     */
    public ImageMagickImageTransformationProvider(TransformedImageCreator transformedImageCreator, String searchPath, boolean useGraphicsMagick, int numThreads, int timeoutSecs) {
        super();
        this.transformedImageCreator = transformedImageCreator;
        searchPathRef = new AtomicReference<String>(searchPath);
        useGraphicsMagickRef = new AtomicBoolean(useGraphicsMagick);
        numThreadsRef = new AtomicInteger(numThreads);
        timeoutSecsRef = new AtomicInteger(timeoutSecs);
    }

    private Processor getProcessor() throws IOException {
        Processor tmp = processor;
        if (null == tmp) {
            synchronized (this) {
                tmp = processor;
                if (null == tmp) {
                    try {
                        int numThreads = numThreadsRef.get();
                        ProcessorService processorService = Services.getService(ProcessorService.class);
                        tmp = processorService.newProcessor("ImageMagick", numThreads);
                        processor = tmp;
                    } catch (OXException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof IOException) {
                            throw (IOException) cause;
                        }
                        throw new IOException(null == cause ? e : cause);
                    }
                }
            }
        }
        return tmp;
    }

    private void stopProcessor() {
        Processor tmp = processor;
        if (null != tmp) {
            processor = null;
            tmp.stop();
        }
    }

    @Override
    public void idle() {
        stopProcessor();
    }

    /**
     * Sets the search path where to look-up the "convert" command.
     *
     * @param searchPath The search path; e.g. <code>"/usr/bin"</code>
     */
    public void setSearchPath(String searchPath) {
        searchPathRef.set(searchPath);
    }

    /**
     * Sets whether GraphicsMagick is supposed to be used
     *
     * @param useGraphicsMagick <code>true</code> to use GraphicsMagick; otherwise <code>false</code>
     */
    public void setUseGraphicsMagick(boolean useGraphicsMagick) {
        useGraphicsMagickRef.set(useGraphicsMagick);
    }

    /**
     * Sets the number of threads to use
     *
     * @param numThreads The number of threads to use
     */
    public void setNumThreads(int numThreads) {
        int prev = numThreadsRef.get();
        if (prev != numThreads) {
            numThreadsRef.set(numThreads);
            stopProcessor();
        }
    }

    /**
     * Sets the timeout in seconds
     *
     * @param timeoutSecs The timeout in seconds
     */
    public void setTimeoutSecs(int timeoutSecs) {
        timeoutSecsRef.set(timeoutSecs);
    }

    private Dimension getDimension(InputStream pInput, String searchPath) throws IOException, InterruptedException, IM4JavaException {
        IMOperation op = new IMOperation();
        op.ping();
        op.format("%w\n%h");
        op.addImage("-");
        IdentifyCmd identify = new IdentifyCmd();
        identify.setSearchPath(searchPath);
        ArrayListOutputConsumer output = new ArrayListOutputConsumer();
        identify.setOutputConsumer(output);
        if (pInput != null) {
            Pipe inputPipe = new Pipe(pInput, null);
            identify.setInputProvider(inputPipe);
        }
        identify.run(op);

        // ... and parse result
        ArrayList<String> cmdOutput = output.getOutput();
        Iterator<String> iter = cmdOutput.iterator();
        int width = Integer.parseInt(iter.next()); // Width
        int height = Integer.parseInt(iter.next()); // Height
        return new Dimension(width, height);
    }

    private void checkResolution(Dimension dimension, long maxResolution) throws ImageTransformationDeniedIOException {
        long resolution = dimension.width * dimension.height;
        if (resolution > maxResolution) {
            throw new ImageTransformationDeniedIOException(new StringBuilder("Image transformation denied. Resolution is too big. (current=").append(resolution).append(", max=").append(maxResolution).append(')').toString());
        }
    }

    @Override
    public ImageTransformations transfom(BufferedImage sourceImage) throws IOException {
        return transfom(sourceImage, null);
    }

    @Override
    public ImageTransformations transfom(BufferedImage sourceImage, Object source) throws IOException {
        // Obey existing image transformation contract:
        boolean doNothing = (sourceImage.getHeight() <= 3 || sourceImage.getWidth() <= 3);
        return new ImageMagickImageTransformations(sourceImage, source, transformedImageCreator, searchPathRef.get(), useGraphicsMagickRef.get(), timeoutSecsRef.get(), getProcessor(), doNothing);
    }

    @Override
    public ImageTransformations transfom(InputStream imageStream) throws IOException {
        return transfom(imageStream, null);
    }

    @Override
    public ImageTransformations transfom(InputStream imageStream, Object source) throws IOException {
        ThresholdFileHolder sink = new ThresholdFileHolder();
        try {
            sink.write(imageStream);

            // Check size
            {
                long maxSize = ImageMagickRegisterer.maxSize();
                if (maxSize > 0 && sink.getLength() > maxSize) {
                    throw new ImageTransformationDeniedIOException(new StringBuilder("Image transformation denied. Size is too big. (current=").append(sink.getLength()).append(", max=").append(maxSize).append(')').toString());
                }
            }

            // Get the search path
            String searchPath = searchPathRef.get();

            // Get image width/height dimension
            Dimension dimension;
            {
                InputStream pInput = sink.getStream();
                try {
                    dimension = getDimension(pInput, searchPath);
                } finally {
                    Streams.close(pInput);
                }
            }

            // Check resolution
            {
                long maxResolution = ImageMagickRegisterer.maxResolution();
                if (maxResolution > 0) {
                    checkResolution(dimension, maxResolution);
                }
            }

            // Obey existing image transformation contract:
            boolean doNothing = (dimension.height <= 3 || dimension.width <= 3);

            ImageMagickImageTransformations transformations = new ImageMagickImageTransformations(sink.getClosingStream(), source, transformedImageCreator, searchPath, useGraphicsMagickRef.get(), timeoutSecsRef.get(), getProcessor(), doNothing);
            sink = null;
            return transformations;
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
            Streams.close(imageStream, sink);
        }
    }

    @Override
    public ImageTransformations transfom(IFileHolder imageFile, Object source) throws IOException {
        ThresholdFileHolder backup = null;
        try {
            if (!imageFile.repetitive()) {
                backup = new ThresholdFileHolder(imageFile);
                Streams.close(imageFile);
                imageFile = backup;
            }

            // Check size
            {
                long maxSize = ImageMagickRegisterer.maxSize();
                long size = imageFile.getLength();
                if (maxSize > 0 && size > maxSize) {
                    throw new ImageTransformationDeniedIOException(new StringBuilder("Image transformation denied. Size is too big. (current=").append(size).append(", max=").append(maxSize).append(')').toString());
                }
            }

            // Get the search path
            String searchPath = searchPathRef.get();

            // Get image width/height dimension
            Dimension dimension;
            {
                InputStream pInput = imageFile.getStream();
                try {
                    dimension = getDimension(pInput, searchPath);
                } finally {
                    Streams.close(pInput);
                }
            }

            // Check resolution
            {
                long maxResolution = ImageMagickRegisterer.maxResolution();
                if (maxResolution > 0) {
                    checkResolution(dimension, maxResolution);
                }
            }

            // Obey existing image transformation contract:
            boolean doNothing = (dimension.height <= 3 || dimension.width <= 3);

            boolean useGraphicsMagick = useGraphicsMagickRef.get();
            int timeoutSecs = timeoutSecsRef.get();
            ImageMagickImageTransformations transformations = new ImageMagickImageTransformations(imageFile, source, transformedImageCreator, searchPath, useGraphicsMagick, timeoutSecs, getProcessor(), doNothing);
            backup = null; // Null'ify to avoid preliminary closing
            return transformations;
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
            Streams.close(backup);
        }
    }

    @Override
    public ImageTransformations transfom(byte[] imageData) throws IOException {
        return transfom(imageData, null);
    }

    @Override
    public ImageTransformations transfom(byte[] imageData, Object source) throws IOException {
        try {
            // Check size
            {
                long maxSize = ImageMagickRegisterer.maxSize();
                if (maxSize > 0 && imageData.length > maxSize) {
                    throw new ImageTransformationDeniedIOException(new StringBuilder("Image transformation denied. Size is too big. (current=").append(imageData.length).append(", max=").append(maxSize).append(')').toString());
                }
            }

            // Get the search path
            String searchPath = searchPathRef.get();

            // Get image width/height dimension
            Dimension dimension;
            {
                InputStream pInput = Streams.newByteArrayInputStream(imageData);
                try {
                    dimension = getDimension(pInput, searchPath);
                } finally {
                    Streams.close(pInput);
                }
            }

            // Check resolution
            {
                long maxResolution = ImageMagickRegisterer.maxResolution();
                if (maxResolution > 0) {
                    checkResolution(dimension, maxResolution);
                }
            }

            // Obey existing image transformation contract:
            boolean doNothing = (dimension.height <= 3 || dimension.width <= 3);

            return new ImageMagickImageTransformations(Streams.newByteArrayInputStream(imageData), source, transformedImageCreator, searchPath, useGraphicsMagickRef.get(), timeoutSecsRef.get(), getProcessor(), doNothing);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("I/O operation has been interrupted.", e);
        } catch (IM4JavaException e) {
            throw new IOException("ImageMagick error.", e);
        }
    }

}
