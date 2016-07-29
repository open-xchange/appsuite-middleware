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

package com.openexchange.imagetransformation.java.transformations;

import static com.openexchange.imagetransformation.java.transformations.Utils.*;
import static com.openexchange.tools.images.ImageTransformationUtility.*;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.BasicTransformedImage;
import com.openexchange.imagetransformation.Constants;
import com.openexchange.imagetransformation.ImageInformation;
import com.openexchange.imagetransformation.ImageTransformationDeniedIOException;
import com.openexchange.imagetransformation.ImageTransformationReloadable;
import com.openexchange.imagetransformation.ImageTransformationSignaler;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.imagetransformation.TransformationContext;
import com.openexchange.imagetransformation.TransformedImage;
import com.openexchange.imagetransformation.java.osgi.Services;
import com.openexchange.java.Streams;
import com.openexchange.tools.images.DefaultTransformedImageCreator;
import com.openexchange.tools.stream.CountingInputStream;
import com.openexchange.tools.stream.CountingInputStream.IOExceptionCreator;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link ImageTransformationsImpl}
 *
 * Default {@link ImageTransformations} implementation.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ImageTransformationsImpl implements ImageTransformations {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ImageTransformationsImpl.class);

    private static volatile Integer waitTimeoutSeconds;
    static int waitTimeoutSeconds() {
        Integer tmp = waitTimeoutSeconds;
        if (null == tmp) {
            synchronized (ImageTransformationsTask.class) {
                tmp = waitTimeoutSeconds;
                if (null == tmp) {
                    int defaultValue = 10;
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Integer.valueOf(configService.getIntProperty("com.openexchange.tools.images.transformations.waitTimeoutSeconds", defaultValue));
                    waitTimeoutSeconds = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Long maxSize;
    static long maxSize() {
        Long tmp = maxSize;
        if (null == tmp) {
            synchronized (ImageTransformationsTask.class) {
                tmp = maxSize;
                if (null == tmp) {
                    int defaultValue = 5242880; // 5 MB
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(configService.getIntProperty("com.openexchange.tools.images.transformations.maxSize", defaultValue));
                    maxSize = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Long maxResolution;
    static long maxResolution() {
        Long tmp = maxResolution;
        if (null == tmp) {
            synchronized (ImageTransformationsTask.class) {
                tmp = maxResolution;
                if (null == tmp) {
                    int defaultValue = 12087962; // 4064 x 2704 (11.1 megapixels) + 10%
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(configService.getIntProperty("com.openexchange.tools.images.transformations.maxResolution", defaultValue));
                    maxResolution = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Float preferThumbnailThreshold;
    static float preferThumbnailThreshold() {
        Float tmp = preferThumbnailThreshold;
        if (null == tmp) {
            synchronized (ImageTransformationsTask.class) {
                tmp = preferThumbnailThreshold;
                if (null == tmp) {
                    float defaultValue = 0.8f;
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    try {
                        tmp = Float.valueOf(configService.getProperty("com.openexchange.tools.images.transformations.preferThumbnailThreshold", String.valueOf(defaultValue)));
                        preferThumbnailThreshold = tmp;
                    } catch (NumberFormatException e) {
                        LOG.warn("error parsing \"com.openexchange.tools.images.transformations.preferThumbnailThreshold\", sticking to defaults.", e);
                    }
                }
            }
        }
        return tmp.floatValue();
    }

    static {
        ImageTransformationReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                waitTimeoutSeconds = null;
                maxSize = null;
                maxResolution = null;
                preferThumbnailThreshold = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties(
                    "com.openexchange.tools.images.transformations.preferThumbnailThreshold",
                    "com.openexchange.tools.images.transformations.maxResolution",
                    "com.openexchange.tools.images.transformations.maxSize",
                    "com.openexchange.tools.images.transformations.waitTimeoutSeconds"
                    );
            }
        });
    }

    private static final IOExceptionCreator IMAGE_SIZE_EXCEEDED_EXCEPTION_CREATOR = new IOExceptionCreator() {

        @Override
        public IOException createIOException(long total, long max) {
            if (total > 0 && max > 0) {
                return new ImageTransformationDeniedIOException(new StringBuilder("Image transformation denied. Size is too big. (current=").append(total).append(", max=").append(max).append(')').toString());
            }
            return new ImageTransformationDeniedIOException("Image transformation denied. Size is too big.");
        }
    };

    private static IOException createResolutionExceededIOException(long maxResolution, int resolution) {
        return new ImageTransformationDeniedIOException(new StringBuilder("Image transformation denied. Resolution is too high. (current=").append(resolution).append(", max=").append(maxResolution).append(')').toString());
    }

    // ------------------------------------------------------------------------------------------------------------------------------ //

    private final TransformationContext transformationContext;
    private final InputStream sourceImageStream;
    private final IFileHolder sourceImageFile;
    private final List<ImageTransformation> transformations;
    private BufferedImage sourceImage;
    private ImageInformation imageInformation;
    private boolean compress;
    protected final Object optSource;

    private ImageTransformationsImpl(BufferedImage sourceImage, InputStream sourceImageStream, IFileHolder imageFile, Object optSource) {
        super();
        this.optSource = optSource;

        if (null == imageFile) {
            this.sourceImageStream = sourceImageStream;
            this.sourceImageFile = null;
        } else {
            if (imageFile.repetitive()) {
                this.sourceImageStream = null;
                this.sourceImageFile = imageFile;
            } else {
                try {
                    this.sourceImageStream = imageFile.getStream();
                    this.sourceImageFile = null;
                } catch (OXException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        }

        this.sourceImage = sourceImage;
        this.transformations = new ArrayList<ImageTransformation>();
        this.transformationContext = new TransformationContext();
    }

    /**
     * Initializes a new {@link ImageTransformationsImpl}.
     *
     * @param sourceImage The source image
     * @param optSource The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     */
    public ImageTransformationsImpl(final BufferedImage sourceImage, final Object optSource) {
        this(sourceImage, null, null, optSource);
    }

    /**
     * Initializes a new {@link ImageTransformationsImpl}.
     *
     * @param sourceImage The source image
     * @param optSource The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     */
    public ImageTransformationsImpl(final InputStream sourceImageStream, final Object optSource) {
        this(null, sourceImageStream, null, optSource);
    }

    /**
     * Initializes a new {@link ImageTransformationsTask}.
     *
     * @param imageFile The image file
     * @param optSource The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     */
    public ImageTransformationsImpl(IFileHolder imageFile, Object optSource) {
        this(null, null, imageFile, optSource);
    }

    @Override
    public ImageTransformations rotate() {
        transformations.add(RotateTransformation.getInstance());
        return this;
    }

    @Override
    public ImageTransformations scale(int maxWidth, int maxHeight, ScaleType scaleType) {
        return scale(maxWidth, maxHeight, scaleType, false);
    }

    @Override
    public ImageTransformations scale(int maxWidth, int maxHeight, ScaleType scaleType, boolean shrinkOnly) {
        if (maxWidth > Constants.getMaxWidth()) {
            throw new IllegalArgumentException("Width " + maxWidth + " exceeds max. supported width " + Constants.getMaxWidth());
        }
        if (maxHeight > Constants.getMaxHeight()) {
            throw new IllegalArgumentException("Height " + maxHeight + " exceeds max. supported height " + Constants.getMaxHeight());
        }
        transformations.add(new ScaleTransformation(maxWidth, maxHeight, scaleType, shrinkOnly));
        return this;
    }

    @Override
    public ImageTransformations crop(int x, int y, int width, int height) {
        transformations.add(new CropTransformation(x, y, width, height));
        return this;
    }

    @Override
    public ImageTransformations compress() {
        this.compress = true;
        return this;
    }

    @Override
    public BufferedImage getImage() throws IOException {
        if (false == needsTransformation(null) && null != sourceImage) {
            return sourceImage;
        }
        // Get BufferedImage
        return getImage(null, null);
    }

    @Override
    public byte[] getBytes(String formatName) throws IOException {
        String imageFormat = getImageFormat(formatName);
        return innerGetBytes(imageFormat);
    }

    private byte[] innerGetBytes(final String imageFormat) throws IOException {
        return write(getImage(imageFormat, null), imageFormat);
    }

    @Override
    public InputStream getInputStream(String formatName) throws IOException {
        String imageFormat = getImageFormat(formatName);
        if (false == needsTransformation(imageFormat)) {
            // Nothing to do
            InputStream in = getFileStream(sourceImageFile);
            if (null != in) {
                return in;
            }
            in = sourceImageStream;
            if (null != in) {
                return in;
            }
        }
        // Perform transformations
        byte[] bytes = innerGetBytes(imageFormat);
        return null == bytes ? null : Streams.newByteArrayInputStream(bytes);
    }

    @Override
    public BasicTransformedImage getTransformedImage(String formatName) throws IOException {
        String imageFormat = getImageFormat(formatName);
        BufferedImage bufferedImage = getImage(imageFormat, null);
        return writeTransformedImage(bufferedImage, imageFormat);
    }

    @Override
    public TransformedImage getFullTransformedImage(String formatName) throws IOException {
        String imageFormat = getImageFormat(formatName);
        BufferedImage bufferedImage = getImage(imageFormat, null);
        return writeTransformedImage(bufferedImage, imageFormat);
    }

    /**
     * Gets a value indicating whether the denoted format name leads to transformations or not.
     *
     * @param formatName The format name
     * @return <code>true</code>, if there are transformations for the target image format, <code>false</code>, otherwise
     */
    private boolean needsTransformation(String formatName) {
        if (false == canRead(formatName)) {
            return false;
        }
        for (ImageTransformation transformation : transformations) {
            if (transformation.supports(formatName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the resulting image after applying all transformations.
     *
     * @param formatName The image format to use, or <code>null</code> if not relevant
     * @param signaler The optional signaler or <code>null</code>
     * @return The transformed image
     * @throws IOException if an I/O error occurs
     */
    protected BufferedImage getImage(String formatName, ImageTransformationSignaler signaler) throws IOException {
        BufferedImage image = getSourceImage(formatName, signaler);

        if (null != image && image.getHeight() > 3 && image.getWidth() > 3) {
            for (ImageTransformation transformation : transformations) {
                if (transformation.supports(formatName)) {
                    image = transformation.perform(image, transformationContext, imageInformation);
                }
            }
        }
        image = removeTransparencyIfNeeded(image, formatName);
        return image;
    }

    /**
     * Gets the source image, either from the supplied buffered image or the supplied stream, extracting image metadata as needed.
     *
     * @param formatName The format to use, e.g. "jpeg" or "tiff"
     * @param signaler The optional signaler or <code>null</code>
     * @return The source image
     * @throws IOException
     */
    private BufferedImage getSourceImage(String formatName, ImageTransformationSignaler signaler) throws IOException {
        if (null == sourceImage) {
            if (null != sourceImageStream) {
                long maxSize = maxSize();
                long maxResolution = maxResolution();
                sourceImage = needsMetadata(formatName, maxSize, maxResolution) ? readAndExtractMetadataFromStream(sourceImageStream, formatName, maxSize, maxResolution, signaler) : read(sourceImageStream, formatName, signaler);
            } else if (null != sourceImageFile) {
                long maxSize = maxSize();
                long maxResolution = maxResolution();
                sourceImage = needsMetadata(formatName, maxSize, maxResolution) ? readAndExtractMetadataFromFile(sourceImageFile, formatName, maxSize, maxResolution, signaler) : read(getFileStream(sourceImageFile), formatName, signaler);
            }
        }
        return sourceImage;
    }

    /**
     * Gets a value indicating whether additional metadata is required for one of the transformations or not.
     *
     * @param formatName The format to use, e.g. "jpeg" or "tiff"
     * @param maxSize The max. size for an image
     * @param maxResolution The max. resolution for an image
     * @return <code>true</code>, if metadata is needed, <code>false</code>, otherwise
     */
    private boolean needsMetadata(String formatName, long maxSize, long maxResolution) {
        if (maxSize > 0 || maxResolution > 0) {
            // Limitations specified, thus meta-data is needed
            return true;
        }
        if (null == formatName || 0 == formatName.length()) {
            return false;
        }
        for (ImageTransformation transformation : transformations) {
            if (transformation.supports(formatName) && transformation.needsImageInformation()) {
                return true;
            }
        }
        return false;
    }

    private TransformedImage writeTransformedImage(BufferedImage image, String formatName) throws IOException {
        return new DefaultTransformedImageCreator().writeTransformedImage(image, formatName, transformationContext, needsCompression(formatName));
    }


    /**
     * Writes out an image into a byte-array.
     *
     * @param image The image to write
     * @param formatName The format to use, e.g. "jpeg" or "tiff"
     * @return The image data
     * @throws IOException
     */
    private byte[] write(BufferedImage image, String formatName) throws IOException {
        if (null == image) {
            return null;
        }
        UnsynchronizedByteArrayOutputStream outputStream = null;
        try {
            outputStream = new UnsynchronizedByteArrayOutputStream(8192);
            if (needsCompression(formatName)) {
                writeCompressed(image, formatName, outputStream, transformationContext);
            } else {
                write(image, formatName, outputStream);
            }
            return outputStream.toByteArray();
        } finally {
            Streams.close(outputStream);
        }
    }

    private boolean needsCompression(String formatName) {
        return this.compress && null != formatName && "jpeg".equalsIgnoreCase(formatName) || "jpg".equalsIgnoreCase(formatName);
    }

    private static void write(BufferedImage image, String formatName, OutputStream output) throws IOException {
        if (false == ImageIO.write(image, formatName, output)) {
            throw new IOException("no appropriate writer found for " + formatName);
        }
    }

    private static void writeCompressed(BufferedImage image, String formatName, OutputStream output, TransformationContext transformationContext) throws IOException {
        ImageWriter writer = null;
        ImageOutputStream imageOutputStream = null;
        try {
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(formatName);
            if (null == iter || false == iter.hasNext()) {
                iter = ImageIO.getImageWritersByMIMEType(formatName);
                if (null == iter || false == iter.hasNext()) {
                    throw new IOException("No image writer for format " + formatName);
                }
            }
            writer = iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            adjustCompressionParams(iwp);
            imageOutputStream = ImageIO.createImageOutputStream(output);
            writer.setOutput(imageOutputStream);
            IIOImage iioImage = new IIOImage(image, null, null);
            writer.write(null, iioImage, iwp);
            transformationContext.addExpense(ImageTransformations.LOW_EXPENSE);
        } finally {
            if (null != writer) {
                writer.dispose();
            }
            if (null != imageOutputStream) {
                imageOutputStream.close();
            }
        }
    }

    /**
     * Tries to adjust the default settings on the supplied image write parameters to apply compression, ignoring any
     * {@link UnsupportedOperationException}s that may occur.
     *
     * @param parameters The parameters to adjust for compression
     */
    private static void adjustCompressionParams(ImageWriteParam parameters) {
        try {
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        } catch (UnsupportedOperationException e) {
            LOG.debug("", e);
        }
        try {
            parameters.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
        } catch (UnsupportedOperationException e) {
            LOG.debug("", e);
        }
        try {
            parameters.setCompressionQuality(0.8f);
        } catch (UnsupportedOperationException e) {
            LOG.debug("", e);
        }
    }

    /**
     * Reads a buffered image from the supplied stream and closes the stream afterwards.
     *
     * @param inputStream The stream to read the image from
     * @param formatName The format name
     * @param signaler The optional signaler or <code>null</code>
     * @return The buffered image
     * @throws IOException
     */
    private BufferedImage read(InputStream inputStream, String formatName, ImageTransformationSignaler signaler) throws IOException {
        try {
            return imageIoRead(inputStream, signaler);
        } catch (final RuntimeException e) {
            LOG.debug("error reading image from stream for {}", formatName, e);
            return null;
        } finally {
            Streams.close(inputStream);
        }
    }

    /**
     * Reads a buffered image from the supplied stream and closes the stream afterwards, trying to extract meta-data information.
     *
     * @param inputStream The stream to read the image from
     * @param formatName The format name
     * @param maxSize The max. size for an image or less than/equal to 0 (zero) for no size limitation
     * @param maxResolution The max. resolution for an image or less than/equal to 0 (zero) for no resolution limitation
     * @param signaler The optional signaler or <code>null</code>
     * @return The buffered image
     * @throws IOException
     */
    private BufferedImage readAndExtractMetadataFromStream(InputStream inputStream, String formatName, long maxSize, long maxResolution, ImageTransformationSignaler signaler) throws IOException {
        ThresholdFileHolder sink = new ThresholdFileHolder();
        try {
            sink.write(maxSize > 0 ? new CountingInputStream(inputStream, maxSize, IMAGE_SIZE_EXCEEDED_EXCEPTION_CREATOR) : inputStream);
            return readAndExtractMetadataFromFile(sink, formatName, maxSize, maxResolution, signaler);
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException("Error accessing file holder", null == cause ? e : cause);
        } finally {
            Streams.close(sink);
        }
    }

    /**
     * Reads a buffered image from the supplied stream and closes the stream afterwards, trying to extract meta-data information.
     *
     * @param imageFile The image file to read from
     * @param formatName The format name
     * @param maxSize The max. size for an image or less than/equal to 0 (zero) for no size limitation
     * @param maxResolution The max. resolution for an image or less than/equal to 0 (zero) for no resolution limitation
     * @param signaler The optional signaler or <code>null</code>
     * @return The buffered image
     */
    private BufferedImage readAndExtractMetadataFromFile(IFileHolder imageFile, String formatName, long maxSize, long maxResolution, ImageTransformationSignaler signaler) throws IOException {
        ImageInputStream imageInputStream = null;
        ImageReader reader = null;
        InputStream inputStream = null;
        try {
            inputStream = imageFile.getStream();
            /*
             * create reader from image input stream
             */
            imageInputStream = getImageInputStream(inputStream);
            reader = getImageReader(imageInputStream, imageFile.getContentType(), imageFile.getName());
            reader.setInput(imageInputStream);
            /*
             * read original image dimensions & check against required dimensions for transformations
             */
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            Dimension requiredResolution = getRequiredResolution(transformations, width, height);
            int imageIndex = selectImage(reader, requiredResolution, maxResolution);
            int orientation = readExifOrientation(reader, imageIndex);
            /*
             * prefer a suitable thumbnail in stream if possible when downscaling images
             */
            float preferThumbnailThreshold = preferThumbnailThreshold();
            if (0 <= preferThumbnailThreshold && reader.hasThumbnails(imageIndex)) {
                if (null != requiredResolution && (requiredResolution.width < width || requiredResolution.height < height)) {
                    int requiredWidth = (int) (preferThumbnailThreshold * requiredResolution.width);
                    int requiredHeight = (int) (preferThumbnailThreshold * requiredResolution.height);
                    for (int i = 0; i < reader.getNumThumbnails(imageIndex); i++) {
                        int thumbnailWidth = reader.getThumbnailWidth(imageIndex, i);
                        int thumbnailHeight = reader.getThumbnailHeight(imageIndex, i);
                        if (thumbnailWidth >= requiredWidth && thumbnailHeight >= requiredHeight) {
                            LOG.trace("Using thumbnail of {}x{}px (requested: {}x{}px)",
                                thumbnailWidth, thumbnailHeight, requiredResolution.width, requiredResolution.height);
                            /*
                             * use thumbnail & skip any additional scale transformations / compressions
                             */
                            compress = false;
                            for (Iterator<ImageTransformation> iterator = transformations.iterator(); iterator.hasNext();) {
                                ImageTransformation transformation = iterator.next();
                                if (ScaleTransformation.class.isInstance(transformation)) {
                                    iterator.remove();
                                }
                            }
                            imageInformation = new ImageInformation(orientation, thumbnailWidth, thumbnailHeight);
                            onImageRead(signaler);
                            return reader.readThumbnail(imageIndex, i);
                        }
                    }
                }
            }
            /*
             * check image size against limitations prior reading source image
             */
            if (0 < maxSize && maxSize < imageFile.getLength()) {
                throw IMAGE_SIZE_EXCEEDED_EXCEPTION_CREATOR.createIOException(imageFile.getLength(), maxSize);
            }
            if (0 < maxResolution) {
                int resolution = height * width;
                if (resolution > maxResolution) {
                    throw createResolutionExceededIOException(maxResolution, resolution);
                }
            }
            imageInformation = new ImageInformation(orientation, width, height);
            onImageRead(signaler);
            return reader.read(imageIndex);
        } catch (RuntimeException e) {
            LOG.debug("error reading image from stream for {}", formatName, e);
            return null;
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw null == cause ? new IOException(e.getMessage(), e) : new IOException(cause.getMessage(), cause);
        } finally {
            if (null != reader) {
                reader.dispose();
            }
            Streams.close(imageInputStream, inputStream);
        }
    }

    /**
     * Returns a {@link BufferedImage} as the result of decoding a supplied {@code InputStream}.
     *
     * @param in The input stream to read from
     * @param signaler The optional signaler or <code>null</code>
     * @return The resulting {@code BufferedImage} instance
     * @throws IOException If an I/O error occurs
     */
    private BufferedImage imageIoRead(InputStream in, ImageTransformationSignaler signaler) throws IOException {
        onImageRead(signaler);
        return ImageIO.read(in);
    }

    private static void onImageRead(ImageTransformationSignaler signaler) {
        if (null != signaler) {
            try {
                signaler.onImageRead();
            } catch (Exception e) {
                LOG.debug("Signaler could not be called", e);
            }
        }
    }

}
