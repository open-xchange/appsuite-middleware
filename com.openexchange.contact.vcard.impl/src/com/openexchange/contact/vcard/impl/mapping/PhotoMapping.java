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

package com.openexchange.contact.vcard.impl.mapping;

import static com.openexchange.java.Autoboxing.I;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.impl.internal.VCardExceptionCodes;
import com.openexchange.contact.vcard.impl.internal.VCardServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.imagetransformation.TransformedImage;
import com.openexchange.java.Charsets;
import com.openexchange.java.InetAddresses;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.tools.ImageTypeDetector;
import com.openexchange.tools.encoding.Base64;
import ezvcard.VCard;
import ezvcard.parameter.ImageType;
import ezvcard.property.Photo;

/**
 * {@link PhotoMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PhotoMapping extends AbstractMapping {

    private static final String X_ABCROP_RECTANGLE = "X-ABCROP-RECTANGLE";
    private static final byte[] PHOTO_PLACEHOLDER = "X-OX-IMAGE1".getBytes(Charsets.US_ASCII);

    private static final String LOCAL_HOST_NAME;
    private static final String LOCAL_HOST_ADDRESS;

    static {
        // Host name initialization
        String localHostName;
        String localHostAddress;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            localHostName = localHost.getCanonicalHostName();
            localHostAddress = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            localHostName = "localhost";
            localHostAddress = "127.0.0.1";
        }
        LOCAL_HOST_NAME = localHostName;
        LOCAL_HOST_ADDRESS = localHostAddress;
    }

    /**
     * Initializes a new {@link PhotoMapping}.
     */
    public PhotoMapping() {
        super("PHOTO", ContactField.IMAGE1, ContactField.NUMBER_OF_IMAGES, ContactField.IMAGE1_CONTENT_TYPE);
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        Photo photo = getFirstProperty(vCard.getPhotos());
        if (null != photo) {
            vCard.removeProperty(photo);
        }
        Photo newPhoto = exportPhoto(contact, parameters, warnings);
        if (null != newPhoto) {
            vCard.addPhoto(newPhoto);
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        Photo photo = getFirstProperty(vCard.getPhotos());
        if (null == photo) {
            contact.setImage1(null);
            contact.setImageContentType(null);
            contact.setNumberOfImages(0);
        } else {
            importPhoto(contact, photo, parameters, warnings);
        }
    }

    private static Photo exportPhoto(Contact contact, VCardParameters parameters, List<OXException> warnings) {
        URI photoUri = contact.getProperty("com.openexchange.contact.vcard.photo.uri");
        if (null != photoUri) {
            String contentType = contact.getProperty("com.openexchange.contact.vcard.photo.contentType");
            return new Photo(photoUri.toString(), getImageType(contentType));
        }
        byte[] contactImage = contact.getImage1();
        if (null != contactImage) {
            ImageType imageType = getImageType(contact.getImageContentType());
            TransformedImage transformedImage = null;
            try {
                try {
                    transformedImage = scaleImageIfNeeded(contactImage, getFormatName(imageType), parameters, warnings);
                } catch (OXException e) {
                    addConversionWarning(warnings, e, "PHOTO", e.getMessage());
                } catch (RuntimeException e) {
                    addConversionWarning(warnings, e, "PHOTO", e.getMessage());
                }
                if (null == transformedImage) {
                    return new Photo(contactImage, imageType);
                }
                try {
                    Photo photo = new Photo(transformedImage.getImageData(), imageType);
                    photo.addParameter(X_ABCROP_RECTANGLE, getABCropRectangle(transformedImage));
                    return photo;
                } catch (OXException e) {
                    addConversionWarning(warnings, e, "PHOTO", e.getMessage());
                }
            } finally {
                Streams.close(transformedImage);
            }
        }
        return null;
    }

    private static void importPhoto(Contact contact, Photo photo, VCardParameters parameters, List<OXException> warnings) {
        byte[] imageData = photo.getData();
        if (null != imageData) {
            Rectangle clipRect = extractClipRect(photo.getParameters(X_ABCROP_RECTANGLE), parameters, warnings);
            if (null != clipRect) {
                /*
                 * try to crop the image based on defined rectangular area
                 */
                try {
                    imageData = doABCrop(imageData, clipRect, getFormatName(photo.getContentType()), parameters, warnings);
                } catch (IOException e) {
                    addConversionWarning(warnings, e, "PHOTO", e.getMessage());
                }
            }
        } else if (null != photo.getUrl()) {
            String urlString = photo.getUrl();
            URI photoUri = contact.getProperty("com.openexchange.contact.vcard.photo.uri");
            if (null != photoUri && photoUri.toString().equals(urlString)) {
                return; // no changes
            }
            IFileHolder fileHolder = null;
            InputStream inputStream = null;
            try {
                try {
                    fileHolder = loadImageFromURL(urlString, warnings);
                    if (null != fileHolder) {
                        if (null != parameters && 0 < parameters.getMaxContactImageSize() && parameters.getMaxContactImageSize() < fileHolder.getLength()) {
                            addConversionWarning(warnings, "PHOTO", "Referenced image exceeds maximum contact image size");
                        } else {
                            inputStream = fileHolder.getStream();
                            imageData = Streams.stream2bytes(inputStream);
                        }
                    }
                } finally {
                    Streams.close(fileHolder);
                }
            } catch (IOException e) {
                addConversionWarning(warnings, e, "PHOTO", "image URL \"" + urlString + "\" appears not to be valid, skipping import.");
            } catch (OXException e) {
                addConversionWarning(warnings, e, "PHOTO", "image URL \"" + urlString + "\" appears not to be valid, skipping import.");
            }
        }
        if (null != imageData && null != parameters && 0 < parameters.getMaxContactImageSize() && parameters.getMaxContactImageSize() < imageData.length) {
            addConversionWarning(warnings, "PHOTO", "Image exceeds maximum contact image size");
            imageData = null;
        }
        if (null == imageData) {
            contact.setImage1(null);
            contact.setNumberOfImages(0);
            contact.setImageContentType(null);
        } else {
            contact.setImage1(imageData);
            contact.setNumberOfImages(1);
            contact.setImageContentType(getMimeType(photo.getContentType()));
            if (null != parameters && parameters.isKeepOriginalVCard() && parameters.isRemoveImageFromKeptVCard()) {
                /*
                 * replace photo after successful import to reduce memory footprint
                 */
                if (null != photo.getData()) {
                    photo.setData(PHOTO_PLACEHOLDER, photo.getContentType());
                }
            }
        }
    }

    private static String getMimeType(ImageType imageType) {
        if (null != imageType) {
            String mediaType = imageType.getMediaType();
            if (null != mediaType) {
                return mediaType;
            }
        }
        return ImageType.JPEG.getMediaType();
    }

    private static String getFormatName(ImageType imageType) {
        return null != imageType ? imageType.getExtension() : ImageType.JPEG.getExtension();
    }

    private static ImageType getImageType(String mimeType) {
        if (null == mimeType) {
            return ImageType.JPEG;
        }
        ImageType imageType = ImageType.find(null, mimeType, null);
        return null != imageType ? imageType : ImageType.JPEG;
    }

    /**
     * Scales a contact image to fit into the supplied target dimension.
     *
     * @param source The image data
     * @param formatName The image format name
     * @param parameters The vCard parameters
     * @param source The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     * @return The scaled image data, or <code>null</code> if no scaling needed
     */
    private static TransformedImage scaleImageIfNeeded(byte[] imageBytes, String formatName, VCardParameters parameters, List<OXException> warnings) throws OXException {
        Dimension targetDimension = null != parameters ? parameters.getPhotoScaleDimension() : null;
        if (null == imageBytes || null == targetDimension || 1 > targetDimension.getWidth() || 1 > targetDimension.getHeight()) {
            return null;
        }
        ImageTransformationService imageService = VCardServiceLookup.getOptionalService(ImageTransformationService.class);
        if (null == imageService) {
            addConversionWarning(warnings, "PHOTO", "unable to acquire image transformation service, unable to scale image");
            return null;
        }
        try {
            return imageService.transfom(imageBytes, getSource(parameters))
                .scale((int) targetDimension.getWidth(), (int) targetDimension.getHeight(), ScaleType.CONTAIN, true).getFullTransformedImage(formatName);
        } catch (IOException e) {
            throw VCardExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Generates a "X-ABCROP-RECTANGLE" parameter value for the supplied transformed image.
     *
     * @param transformedImage The transformed image
     * @return The parameter value
     */
    private static String getABCropRectangle(TransformedImage transformedImage) {
        StringBuilder stringBuilder = new StringBuilder(64);
        stringBuilder.append("ABClipRect_1&");
        int width = transformedImage.getWidth();
        int height = transformedImage.getHeight();
        if (width < height) {
            stringBuilder.append('-').append((height - width) / 2).append("&0&").append(height).append('&').append(height);
        } else if (width > height) {
            stringBuilder.append("0&-").append((width - height) / 2).append('&').append(width).append('&').append(width);
        } else {
            stringBuilder.append("0&0&").append(width).append('&').append(height);
        }
        if (null != transformedImage.getMD5()) {
            stringBuilder.append('&').append(Base64.encode(transformedImage.getMD5()));
        }
        return stringBuilder.toString();
    }

    /**
     * Extracts the clipping information from the supplied 'X-ABCROP-RECTANGLE' parameter values if defined. The result's 'width' and
     * 'height' properties represent the dimensions of the target image. The 'x' property is the horizontal offset to draw the source
     * image in the target image from the left border, the 'y' property is the vertical offset from the bottom.
     *
     * @param cropValues The 'X-ABCROP-RECTANGLE' parameter values
     * @param parameters The vCard parameters
     * @return The clipping rectangle, or <code>null</code>, if not defined
     */
    private static Rectangle extractClipRect(List<String> cropValues, @SuppressWarnings("unused") VCardParameters parameters, List<OXException> warnings) {
        if (null != cropValues && 0 < cropValues.size()) {
            Pattern clipRectPattern = Pattern.compile("ABClipRect_1&([-+]?\\d+?)&([-+]?\\d+?)&([-+]?\\d+?)&([-+]?\\d+?)&");
            for (String value : cropValues) {
                Matcher matcher = clipRectPattern.matcher(value);
                if (matcher.find()) {
                    try {
                        int offsetLeft = Integer.parseInt(matcher.group(1));
                        int offsetBottom = Integer.parseInt(matcher.group(2));
                        int targetWidth = Integer.parseInt(matcher.group(3));
                        int targetHeight = Integer.parseInt(matcher.group(4));
                        return new Rectangle(offsetLeft, offsetBottom, targetWidth, targetHeight);
                    } catch (NumberFormatException e) {
                        addConversionWarning(warnings, e, "PHOTO", e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Performs a crop operation on the source image as defined by the
     * supplied clipping rectangle.
     *
     * @param imageBytes The source image
     * @param clipRect The clip rectangle from an 'X-ABCROP-RECTANGLE' property
     * @param formatName The target image format
     * @param parameters Further options to use, or <code>null</code> to use to the default options
     * @return The cropped image
     */
    private static byte[] doABCrop(byte[] imageBytes, Rectangle clipRect, String formatName, VCardParameters parameters, List<OXException> warnings) throws IOException {
        InputStream inputStream = null;
        try {
            /*
             * read source image
             */
            inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage sourceImage = ImageIO.read(inputStream);
            if (null == sourceImage) {
                addConversionWarning(warnings, "PHOTO", "unable to acquire an ImageReader capable of decoding the supplied image, unable to crop image");
                return imageBytes;
            }
            /*
             * crop the image
             */
            ImageTransformationService imageService = VCardServiceLookup.getOptionalService(ImageTransformationService.class);
            if (null == imageService) {
                addConversionWarning(warnings, "PHOTO", "unable to acquire image transformation service, unable to crop image");
                return imageBytes;
            }
            return imageService.transfom(sourceImage, getSource(parameters)).crop(clipRect.x * -1,
                clipRect.height + clipRect.y - sourceImage.getHeight(), clipRect.width, clipRect.height).getBytes(formatName);
        } finally {
            Streams.close(inputStream);
        }
    }

    private static Object getSource(VCardParameters parameters) {
        return null != parameters && null != parameters.getSession() ? parameters.getSession().getSessionID() : null;
    }

    private static final Set<String> ALLOWED_PROTOCOLS = ImmutableSet.of("http", "https", "ftp", "ftps");
    private static final Set<String> DENIED_HOSTS = ImmutableSet.of("localhost", "127.0.0.1", LOCAL_HOST_ADDRESS, LOCAL_HOST_NAME);

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection and put into given {@link Contact contact container}.
     *
     * @param urlString The image URL
     * @param origUrlString The original image URL
     * @param warnings The listing to add possible warnings to
     * @return A file holder containing the loaded image, or <code>null</code> if no valid image could be loaded
     */
    private static ThresholdFileHolder loadImageFromURL(String urlString, List<OXException> warnings) throws IOException, OXException {
        String urlStringToUse = urlString;
        int maxNumAttempts = 5;
        int numAttempts = 0;
        LoadedImage result;
        do {
            if (numAttempts++ >= maxNumAttempts) {
                addConversionWarning(warnings, "PHOTO", "image URL \"" + urlString + "\" appears not to be valid, skipping import.");
                return null;
            }
            result = doLoadImageFromURL(urlStringToUse, urlString, warnings);
        } while ((urlStringToUse = result.redirectUrl) != null);
        return result.fileHolder;
    }

    private static final Set<Integer> REDIRECT_RESPONSE_CODES = ImmutableSet.of(I(HttpURLConnection.HTTP_MOVED_PERM), I(HttpURLConnection.HTTP_MOVED_TEMP), I(HttpURLConnection.HTTP_SEE_OTHER), I(HttpURLConnection.HTTP_USE_PROXY));

    private static LoadedImage doLoadImageFromURL(String urlString, String origUrlString, List<OXException> warnings) throws IOException, OXException {
        if (Strings.isEmpty(urlString)) {
            return LoadedImage.getNullResult();
        }

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            addConversionWarning(warnings, e, "PHOTO", "image URL \"" + origUrlString + "\" appears not to be valid, skipping import.");
            return LoadedImage.getNullResult();
        }
        /*
         * check URL validity
         */
        {
            String protocol = url.getProtocol();
            if (null == protocol || false == ALLOWED_PROTOCOLS.contains(Strings.asciiLowerCase(protocol))) {
                addConversionWarning(warnings, "PHOTO", "image URL \"" + origUrlString + "\" appears not to be valid, skipping import.");
                return LoadedImage.getNullResult();
            }

            String host = Strings.asciiLowerCase(url.getHost());
            if (null == host || DENIED_HOSTS.contains(host)) {
                addConversionWarning(warnings, "PHOTO", "image URL \"" + origUrlString + "\" appears not to be valid, skipping import.");
                return LoadedImage.getNullResult();
            }

            try {
                InetAddress inetAddress = InetAddress.getByName(url.getHost());
                if (InetAddresses.isInternalAddress(inetAddress)) {
                    addConversionWarning(warnings, "PHOTO", "image URL \"" + origUrlString + "\" appears not to be valid, skipping import.");
                    return LoadedImage.getNullResult();
                }
            } catch (UnknownHostException e) {
                addConversionWarning(warnings, e, "PHOTO", "image URL \"" + origUrlString + "\" appears not to be valid, skipping import.");
                return LoadedImage.getNullResult();
            }
        }
        /*
         * download to file holder
         */
        ThresholdFileHolder fileHolder = null;
        String mimeType = null;
        {
            boolean error = true;
            InputStream inputStream = null;
            try {
                URLConnection urlConnnection = url.openConnection();
                urlConnnection.setConnectTimeout(2500);
                urlConnnection.setReadTimeout(2500);
                if (urlConnnection instanceof HttpURLConnection) {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnnection;
                    // Deny to automatically follow redirects for this HttpURLConnection instance
                    httpURLConnection.setInstanceFollowRedirects(false);
                    httpURLConnection.connect();

                    // Check for redirect
                    int responseCode = httpURLConnection.getResponseCode();
                    if (REDIRECT_RESPONSE_CODES.contains(I(responseCode))) {
                        String redirectUrl = urlConnnection.getHeaderField("Location");
                        httpURLConnection.disconnect();
                        return LoadedImage.getRedirectResult(redirectUrl);
                    }
                    if (responseCode >= 400) {
                        addConversionWarning(warnings, "PHOTO", "image URL \"" + origUrlString + "\" appears not to be valid, skipping import.");
                        return LoadedImage.getNullResult();
                    }
                } else {
                    urlConnnection.connect();
                }

                mimeType = urlConnnection.getContentType();
                inputStream = urlConnnection.getInputStream();
                fileHolder = new ThresholdFileHolder();
                fileHolder.write(inputStream);
                error = false;
            } catch (SocketTimeoutException e) {
                addConversionWarning(warnings, e, "PHOTO", e.getMessage());
                return LoadedImage.getNullResult();
            } finally {
                Streams.close(inputStream);
                if (error) {
                    Streams.close(fileHolder);
                }
            }
        }
        /*
         * check image validity
         */
        if (false == isValidImage(fileHolder)) {
            addConversionWarning(warnings, "PHOTO", "image downloaded from \"" + origUrlString + "\" appears not to be valid, skipping import.");
            Streams.close(fileHolder);
            return LoadedImage.getNullResult();
        }
        /*
         * additional fallbacks to determine the mime type
         */
        if (null == mimeType) {
            mimeType = ImageTypeDetector.getMimeType(fileHolder.getStream());
            if ("application/octet-stream".equals(mimeType)) {
                mimeType = MimeType2ExtMap.getContentType(urlString, ImageType.JPEG.getMediaType());
            }
        }
        fileHolder.setContentType(mimeType);
        return LoadedImage.getFileHolderResult(fileHolder);
    }

    private static boolean isValidImage(ThresholdFileHolder fileHolder) throws IOException, OXException {
        if (fileHolder.isInMemory()) {
            InputStream inputStream = null;
            ImageInputStream imageInputStream = null;
            try {
                inputStream = fileHolder.getStream();
                imageInputStream = ImageIO.createImageInputStream(inputStream);
                return isValidImage(imageInputStream);
            } finally {
                Streams.close(imageInputStream, inputStream);
            }
        }

        File file = fileHolder.getTempFile();
        ImageInputStream imageInputStream = null;
        try {
            imageInputStream = ImageIO.createImageInputStream(file);
            return isValidImage(imageInputStream);
        } finally {
            Streams.close(imageInputStream);
        }
    }

    private static boolean isValidImage(ImageInputStream imageInputStream) {
        if (null == imageInputStream) {
            return false;
        }
        Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
        return null != readers && readers.hasNext();
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------

    private static final class LoadedImage {

        private static final LoadedImage NULL_RESULT = new LoadedImage();

        static LoadedImage getNullResult() {
            return NULL_RESULT;
        }

        static LoadedImage getRedirectResult(String redirectUrl) {
            return Strings.isEmpty(redirectUrl) ? NULL_RESULT : new LoadedImage(redirectUrl);
        }

        static LoadedImage getFileHolderResult(ThresholdFileHolder fileHolder) {
            return null == fileHolder ? NULL_RESULT : new LoadedImage(fileHolder);
        }

        // ----------------------------------------------------------------------

        final ThresholdFileHolder fileHolder;
        final String redirectUrl;

        private LoadedImage() {
            super();
            this.fileHolder = null;
            this.redirectUrl = null;
        }

        private LoadedImage(ThresholdFileHolder fileHolder) {
            super();
            this.fileHolder = fileHolder;
            this.redirectUrl = null;
        }

        private LoadedImage(String redirectUrl) {
            super();
            this.fileHolder = null;
            this.redirectUrl = redirectUrl;
        }
    }

}
