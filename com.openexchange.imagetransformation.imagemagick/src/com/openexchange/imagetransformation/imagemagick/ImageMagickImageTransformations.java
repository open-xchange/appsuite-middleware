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

import static com.openexchange.imagetransformation.Utility.getImageFormat;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Stream2BufferedImage;
import org.im4java.process.InputProvider;
import org.im4java.process.OutputConsumer;
import org.im4java.process.Pipe;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.Constants;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.imagetransformation.TransformationContext;
import com.openexchange.imagetransformation.TransformedImage;
import com.openexchange.imagetransformation.BasicTransformedImage;
import com.openexchange.imagetransformation.TransformedImageCreator;
import com.openexchange.java.ExceptionAwarePipedInputStream;
import com.openexchange.java.Streams;


/**
 * {@link ImageMagickImageTransformations}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ImageMagickImageTransformations implements ImageTransformations {

    private final TransformedImageCreator transformedImageCreator;

    private final ConvertCmd cmd;
    private final IMOperation op;

    private final InputStream sourceImageStream;
    private final IFileHolder sourceImageFile;
    private final BufferedImage sourceImage;
    private final Object optSource;

    private boolean compress;

    /**
     * Initializes a new {@link ImageMagickImageTransformations}.
     */
    public ImageMagickImageTransformations(BufferedImage sourceImage, Object optSource, TransformedImageCreator transformedImageCreator, String searchPath) {
        super();
        this.transformedImageCreator = transformedImageCreator;
        ConvertCmd cmd = new ConvertCmd();
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
    public ImageMagickImageTransformations(IFileHolder sourceImageFile, Object optSource, TransformedImageCreator transformedImageCreator, String searchPath) {
        super();
        this.transformedImageCreator = transformedImageCreator;
        ConvertCmd cmd = new ConvertCmd();
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
    public ImageMagickImageTransformations(InputStream sourceImageStream, Object optSource, TransformedImageCreator transformedImageCreator, String searchPath) {
        super();
        this.transformedImageCreator = transformedImageCreator;
        ConvertCmd cmd = new ConvertCmd();
        cmd.setSearchPath(searchPath);
        this.cmd = cmd;
        this.op = new IMOperation();
        op.addImage("-"); // read from stdin

        this.sourceImage = null;
        this.sourceImageFile = null;
        this.sourceImageStream = sourceImageStream;

        this.optSource = optSource;
    }

    @Override
    public ImageTransformations rotate() {
        op.autoOrient();
        return this;
    }

    @Override
    public ImageTransformations scale(int maxWidth, int maxHeight, ScaleType scaleType) {
        if (maxWidth > Constants.getMaxWidth()) {
            throw new IllegalArgumentException("Width " + maxWidth + " exceeds max. supported width " + Constants.getMaxWidth());
        }
        if (maxHeight > Constants.getMaxHeight()) {
            throw new IllegalArgumentException("Height " + maxHeight + " exceeds max. supported height " + Constants.getMaxHeight());
        }

        switch (scaleType) {
            case CONTAIN:
                op.scale(Integer.valueOf(maxWidth), Integer.valueOf(maxHeight));
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
        op.crop(Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(x), Integer.valueOf(y));
        return this;
    }

    @Override
    public ImageTransformations compress() {
        compress = true;
        return this;
    }

    private void runCommand() throws IOException {
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

    private BufferedImage getImage(String formatName) throws IOException {
        op.addImage(getImageFormat(formatName) + ":-");

        Stream2BufferedImage s2b = new Stream2BufferedImage();
        cmd.setOutputConsumer(s2b);

        runCommand();

        return s2b.getImage();
    }

    @Override
    public BufferedImage getImage() throws IOException {
        return getImage("png");
    }

    @Override
    public byte[] getBytes(String formatName) throws IOException {
        op.addImage(getImageFormat(formatName) + ":-");

        ByteCollectingOutputConsumer bytes = new ByteCollectingOutputConsumer();
        cmd.setOutputConsumer(bytes);

        runCommand();

        return bytes.toByteArray();
    }

    @Override
    public InputStream getInputStream(String formatName) throws IOException {
        op.addImage(getImageFormat(formatName) + ":-");

        if (null != sourceImage) {
            ByteCollectingOutputConsumer bytes = new ByteCollectingOutputConsumer();
            cmd.setOutputConsumer(bytes);

            runCommand();

            return bytes.asInputStream();
        }

        try {
            PipedOutputStream pos = new PipedOutputStream();
            final ExceptionAwarePipedInputStream pin = new ExceptionAwarePipedInputStream(pos, 65536);

            InputStream is = null != sourceImageStream ? sourceImageStream : sourceImageFile.getStream();
            Pipe pipeIn  = new Pipe(is,null);
            Pipe pipeOut = new Pipe(null,pos);

            // Set up command
            cmd.setInputProvider(pipeIn);
            cmd.setOutputConsumer(pipeOut);

            cmd.run(op);

            return pin;
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
        }
    }

    @Override
    public BasicTransformedImage getTransformedImage(String formatName) throws IOException {
        String frmtName = getImageFormat(formatName);
        ThresholdFileHolder sink = new ThresholdFileHolder();
        boolean error = true;
        try {
            sink.write(getInputStream(formatName));
            BasicTransformedImage retval = new BasicTransformedImageImpl(frmtName, sink);
            error = false;
            return retval;
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw null == cause ? new IOException(e.getMessage(), e) : new IOException(cause.getMessage(), cause);
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

    @Override
    public TransformedImage getFullTransformedImage(String formatName) throws IOException {
        String frmtName = getImageFormat(formatName);
        BufferedImage bufferedImage = getImage(frmtName);
        return transformedImageCreator.writeTransformedImage(bufferedImage, frmtName, new TransformationContext(), needsCompression(frmtName));
    }

    private boolean needsCompression(String formatName) {
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
            sink = new ThresholdFileHolder();
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
