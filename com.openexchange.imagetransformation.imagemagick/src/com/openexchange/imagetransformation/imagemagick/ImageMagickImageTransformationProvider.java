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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import com.openexchange.imagetransformation.ImageTransformationProvider;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.TransformedImageCreator;
import com.openexchange.imagetransformation.imagemagick.osgi.ImageMagickRegisterer;
import com.openexchange.java.Streams;

/**
 * {@link ImageMagickImageTransformationProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ImageMagickImageTransformationProvider implements ImageTransformationProvider {

    private final TransformedImageCreator transformedImageCreator;
    private final AtomicReference<String> searchPathRef;

    /**
     * Initializes a new {@link ImageMagickImageTransformationProvider}.
     */
    public ImageMagickImageTransformationProvider(TransformedImageCreator transformedImageCreator, String searchPath) {
        super();
        this.transformedImageCreator = transformedImageCreator;
        searchPathRef = new AtomicReference<String>(searchPath);
    }

    /**
     * Sets the search path where to look-up the "convert" command.
     *
     * @param searchPath The search path; e.g. <code>"/usr/bin"</code>
     */
    public void setSearchPath(String searchPath) {
        searchPathRef.set(searchPath);
    }

    private void checkResolution(InputStream pInput, long maxResolution, String searchPath) throws IOException, InterruptedException, IM4JavaException, ImageTransformationDeniedIOException {
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

        long resolution = width * height;
        if (resolution > maxResolution) {
            throw new ImageTransformationDeniedIOException(new StringBuilder("Image transformation denied. Resolution is too big. (current=").append(resolution).append(", max=").append(maxResolution).append(')').toString());
        }
    }

    @Override
    public ImageTransformations transfom(BufferedImage sourceImage) {
        return transfom(sourceImage, null);
    }

    @Override
    public ImageTransformations transfom(BufferedImage sourceImage, Object source) {
        return new ImageMagickImageTransformations(sourceImage, source, transformedImageCreator, searchPathRef.get());
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

            // Check resolution
            {
                long maxResolution = ImageMagickRegisterer.maxResolution();
                if (maxResolution > 0) {
                    InputStream pInput = sink.getStream();
                    try {
                        checkResolution(pInput, maxResolution, searchPath);
                    } finally {
                        Streams.close(pInput);
                    }
                }
            }

            ImageMagickImageTransformations transformations = new ImageMagickImageTransformations(sink.getClosingStream(), source, transformedImageCreator, searchPath);
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
        ThresholdFileHolder sink = null;
        try {
            if (!imageFile.repetitive()) {
                sink = new ThresholdFileHolder(sink);
            }

            // Check size
            {
                long maxSize = ImageMagickRegisterer.maxSize();
                long size = null == sink ? imageFile.getLength() : sink.getLength();
                if (maxSize > 0 && size > maxSize) {
                    throw new ImageTransformationDeniedIOException(new StringBuilder("Image transformation denied. Size is too big. (current=").append(size).append(", max=").append(maxSize).append(')').toString());
                }
            }

            // Get the search path
            String searchPath = searchPathRef.get();

            // Check resolution
            {
                long maxResolution = ImageMagickRegisterer.maxResolution();
                if (maxResolution > 0) {
                    InputStream pInput = null == sink ? imageFile.getStream() : sink.getStream();
                    try {
                        checkResolution(pInput, maxResolution, searchPath);
                    } finally {
                        Streams.close(pInput);
                    }
                }
            }

            if (null == sink) {
                return new ImageMagickImageTransformations(imageFile, source, transformedImageCreator, searchPath);
            }

            ImageMagickImageTransformations transformations = new ImageMagickImageTransformations(sink, source, transformedImageCreator, searchPath);
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
            Streams.close(sink);
        }
    }

    @Override
    public ImageTransformations transfom(byte[] imageData) throws IOException {
        return transfom(imageData, null);
    }

    @Override
    public ImageTransformations transfom(byte[] imageData, Object source) throws IOException {
        return new ImageMagickImageTransformations(Streams.newByteArrayInputStream(imageData), source, transformedImageCreator, searchPathRef.get());
    }

}
