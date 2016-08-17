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

package com.openexchange.snippet;

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.MimeUtility;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.HTMLDetector;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.utils.ImageMatcher;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.session.Session;
import com.openexchange.snippet.DefaultAttachment.InputStreamProvider;
import com.openexchange.snippet.internal.Services;
import com.openexchange.version.Version;

/**
 * {@link SnippetProcessor}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SnippetProcessor {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SnippetProcessor.class);

    private static class ManagedFileInputStreamProvider implements InputStreamProvider {

        private final ManagedFile managedFile;

        /**
         * Initializes a new {@link ManagedFileInputStreamProvider}.
         *
         * @param mf The managed file
         */
        ManagedFileInputStreamProvider(ManagedFile managedFile) {
            super();
            this.managedFile = managedFile;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return managedFile.getInputStream();
            } catch (OXException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw new IOException(null == cause ? e : cause);
            }
        }
    }

    private static class ThresholdFileHolderInputStreamProvider implements InputStreamProvider {

        private final ThresholdFileHolder fileHolder;

        /**
         * Initializes a new {@link ThresholdFileHolderInputStreamProvider}.
         *
         * @param fileHolder The threshold file holder
         */
        ThresholdFileHolderInputStreamProvider(ThresholdFileHolder fileHolder) {
            super();
            this.fileHolder = fileHolder;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return fileHolder.getStream();
            } catch (OXException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw new IOException(null == cause ? e : cause);
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------

    private final Session session;

    /**
     * Initializes a new {@link SnippetProcessor}.
     *
     * @param session
     * @param ctx
     */
    public SnippetProcessor(Session session) {
        super();
        this.session = session;
    }

    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SRC_PATTERN = Pattern.compile("(?:src=\"([^\"]+)\")|(?:src='([^']+)')", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Process the images in the snippet, extracts them and convert them to attachments.
     *
     * @param snippet The snippet to process
     * @throws OXException If processing images fails
     */
    public void processExternalImages(DefaultSnippet snippet) throws OXException {
        String content = snippet.getContent();
        if (isEmpty(content)) {
            return;
        }

        MaxImageProps maxImageProps = getMaxImageProps();
        long maxImageSize = maxImageProps.maxImageSize;
        int maxImageLimit = maxImageProps.maxImageLimit;

        Matcher m = IMG_PATTERN.matcher(content);
        if (false == m.find()) {
            return;
        }

        List<Attachment> attachments = new LinkedList<Attachment>();
        StringBuffer sb = new StringBuffer(content.length());
        int count = 0;
        do {
            String imgTag = m.group();

            Matcher srcMatcher = SRC_PATTERN.matcher(imgTag);
            if (srcMatcher.find()) {
                // Get the 'src' attribute's value
                String src = srcMatcher.group(1);
                if (src == null) {
                    src = srcMatcher.group(2);
                }

                // Check for valid URL
                URL url;
                try {
                    url = new URL(src);
                } catch (Exception e) {
                    // No... it's not
                    throw SnippetExceptionCodes.UNEXPECTED_ERROR.create(e, "Invalid image URL: " + src);
                }

                // Check max. number of images
                count++;
                if (maxImageLimit > 0 && count > maxImageLimit) {
                    throw SnippetExceptionCodes.MAXIMUM_IMAGES_COUNT.create(Integer.valueOf(maxImageLimit));
                }

                // Get content identifier for URL resource
                String contentId = loadImage(url, count, maxImageSize, attachments);

                if (null == contentId) {
                    // No valid image data accessible through URL. Drop <img> tag
                    m.appendReplacement(sb, "");
                } else {
                    // Replace <img> tag
                    int start = srcMatcher.start();
                    int end = srcMatcher.end();
                    String iTag = new StringBuilder(imgTag.length()).append(imgTag.substring(0, start)).append("src=\"cid:").append(contentId).append("\"").append(imgTag.substring(end)).toString();
                    m.appendReplacement(sb, Strings.quoteReplacement(iTag));
                }
            } else {
                m.appendReplacement(sb, Strings.quoteReplacement(imgTag));
            }

        } while (m.find());
        m.appendTail(sb);

        // Set "new" content
        snippet.setContent(sb.toString());

        // Add attachments
        for (Attachment attachment : attachments) {
            snippet.addAttachment(attachment);
        }
    }

    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 3000;

    private String loadImage(URL url, int count, long maxImageSize, List<Attachment> attachments) throws OXException {
        ThresholdFileHolder fileHolder = null;
        InputStream in = null;
        try {
            URLConnection con = url.openConnection();
            if (con instanceof HttpURLConnection) {
                return loadHttpImage((HttpURLConnection) con, count, maxImageSize, attachments);
            }

            // Generic URLConnection handling
            con.connect();
            in = con.getInputStream();

            int contentLength = con.getContentLength();
            if (contentLength > 0 && contentLength > maxImageSize) {
                throw SnippetExceptionCodes.MAXIMUM_IMAGE_SIZE.create(FileUtils.byteCountToDisplaySize(Long.valueOf(maxImageSize)), maxImageSize);
            }

            String contentType = con.getHeaderField("content-type");
            if (!Strings.isEmpty(contentType) && !Strings.asciiLowerCase(contentType).startsWith("image/")) {
                throw SnippetExceptionCodes.INVALID_IMAGE_DATA.create();
            }
            contentType = Strings.isEmpty(contentType) ? "image/jpeg" : contentType;
            String ext = MimeType2ExtMap.getFileExtension(contentType);

            fileHolder = new ThresholdFileHolder();
            fileHolder.write(in);
            fileHolder.setContentType(contentType);
            fileHolder.setName("image" + count + "." + ext);

            if (false == checkImageData(fileHolder)) {
                // No valid image data
                throw SnippetExceptionCodes.INVALID_IMAGE_DATA.create();
            }

            String id = UUIDs.getUnformattedString(UUID.randomUUID());
            String contentId = processLocalImage(fileHolder, id, true, attachments);
            fileHolder = null; // Null'ify to avoid preliminary closing
            return contentId;
        } catch (IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in, fileHolder);
        }
    }

    private String loadHttpImage(HttpURLConnection httpCon, int count, long maxImageSize, List<Attachment> attachments) throws OXException {
        ThresholdFileHolder fileHolder = null;
        InputStream in = null;
        try {
            httpCon.setRequestMethod("GET");
            httpCon.setConnectTimeout(CONNECT_TIMEOUT);
            httpCon.setReadTimeout(READ_TIMEOUT);
            httpCon.connect();

            int responseCode = httpCon.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            in = httpCon.getInputStream();

            int contentLength = httpCon.getContentLength();
            if (contentLength > 0 && contentLength > maxImageSize) {
                throw SnippetExceptionCodes.MAXIMUM_IMAGE_SIZE.create(FileUtils.byteCountToDisplaySize(Long.valueOf(maxImageSize)), maxImageSize);
            }

            String contentType = httpCon.getHeaderField("content-type");
            if (!Strings.isEmpty(contentType) && !Strings.asciiLowerCase(contentType).startsWith("image/")) {
                throw SnippetExceptionCodes.INVALID_IMAGE_DATA.create();
            }
            contentType = Strings.isEmpty(contentType) ? "image/jpeg" : contentType;
            String ext = MimeType2ExtMap.getFileExtension(contentType);

            fileHolder = new ThresholdFileHolder();
            fileHolder.write(in);
            fileHolder.setContentType(contentType);
            fileHolder.setName("image" + count + "." + ext);

            if (false == checkImageData(fileHolder)) {
                // No valid image data
                throw SnippetExceptionCodes.INVALID_IMAGE_DATA.create();
            }

            String id = UUIDs.getUnformattedString(UUID.randomUUID());
            String contentId = processLocalImage(fileHolder, id, true, attachments);
            fileHolder = null; // Null'ify to avoid preliminary closing
            return contentId;
        } catch (IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in, fileHolder);
            httpCon.disconnect();
        }
    }

    private boolean checkImageData(ThresholdFileHolder fileHolder) throws IOException, OXException {
        if (HTMLDetector.containsHTMLTags(fileHolder.getStream(), false)) {
            return false;
        }

        String contentType = com.openexchange.java.ImageTypeDetector.getMimeType(fileHolder.getStream());
        if (!Strings.asciiLowerCase(contentType).startsWith("image/")) {
            return false;
        }

        return isValidImage(fileHolder.getStream());
    }

    private static boolean isValidImage(InputStream data) {
        try {
            java.awt.image.BufferedImage bimg = javax.imageio.ImageIO.read(data);
            return (bimg != null && bimg.getHeight() > 0 && bimg.getWidth() > 0);
        } catch (Exception e) {
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------ //

    private static final Pattern PATTERN_SRC_ATTRIBUTE = Pattern.compile("(?i)src=\"[^\"]*\"");
    private static final Pattern PATTERN_ID_ATTRIBUTE = Pattern.compile("(?i)id=\"[^\"]*@" + Version.NAME + "\"");

    /**
     * Process the images in the snippet, extracts them and convert them to attachments.
     *
     * @param snippet The snippet to process
     * @throws OXException If processing images fails
     */
    public void processImages(DefaultSnippet snippet) throws OXException {
        processImages0(snippet, new LinkedList<Attachment>(), true);
    }

    /**
     * Process the images in the snippet, extracts them and convert them to attachments.
     *
     * @param snippet The snippet to process
     * @param attachments The list to add attachments to
     * @throws OXException If processing images fails
     */
    public void processImages(DefaultSnippet snippet, List<Attachment> attachments) throws OXException {
        processImages0(snippet, attachments, false);
    }

    private void processImages0(DefaultSnippet snippet, List<Attachment> attachments, boolean addAttachments) throws OXException {
        String content = snippet.getContent();
        if (isEmpty(content)) {
            return;
        }

        MaxImageProps maxImageProps = getMaxImageProps();
        long maxImageSize = maxImageProps.maxImageSize;
        int maxImageLimit = maxImageProps.maxImageLimit;

        ImageMatcher m = ImageMatcher.matcher(content);
        StringBuffer sb = new StringBuffer(content.length());
        if (m.find()) {
            ManagedFileManagement mfm = Services.getService(ManagedFileManagement.class);
            Set<String> trackedIds = new HashSet<String>(2);
            int count = 0;
            do {
                String imageTag = m.group();
                if (MimeMessageUtility.isValidImageUri(imageTag)) {
                    String id = m.getManagedFileId();
                    if (null == id || !mfm.contains(id)) {
                        // Leave it
                        continue;
                    }

                    ManagedFile mf;
                    try {
                        mf = mfm.getByID(id);
                    } catch (OXException e) {
                        LOG.warn("Image with id \"{}\" could not be loaded. Referenced image is skipped.", id, e);
                        // Anyway, replace image tag
                        m.appendLiteralReplacement(sb, MimeMessageUtility.blankSrc(imageTag));
                        continue;
                    }

                    if (++count > maxImageLimit) {
                        throw SnippetExceptionCodes.MAXIMUM_IMAGES_COUNT.create(Integer.valueOf(maxImageLimit));
                    }

                    if (mf.getSize() > maxImageSize) {
                        throw SnippetExceptionCodes.MAXIMUM_IMAGE_SIZE.create(FileUtils.byteCountToDisplaySize(Long.valueOf(maxImageSize)), maxImageSize);
                    }

                    // Replace "src" attribute
                    boolean appendBodyPart = trackedIds.add(id);
                    String contentId = processLocalImage(mf, id, appendBodyPart, attachments);

                    String iTag = PATTERN_SRC_ATTRIBUTE.matcher(imageTag).replaceFirst(Strings.quoteReplacement("src=\"cid:" + contentId + "\""));
                    iTag = PATTERN_ID_ATTRIBUTE.matcher(iTag).replaceFirst("");
                    m.appendLiteralReplacement(sb, iTag);
                } else {
                    // Re-append as-is
                    m.appendLiteralReplacement(sb, imageTag);
                }
            } while (m.find());
        }
        m.appendTail(sb);

        // Set "new" content
        snippet.setContent(sb.toString());

        // Add attachments
        if (addAttachments) {
            for (Attachment attachment : attachments) {
                snippet.addAttachment(attachment);
            }
        }
    }

    /**
     * Processes a local image and returns its content identifier
     *
     * @param mf The uploaded file
     * @param id The uploaded file's ID
     * @param appendBodyPart Whether to actually append the part to the snippet
     * @param attachments The attachment list
     * @return The content id
     */
    private final String processLocalImage(ManagedFile mf, String id, boolean appendBodyPart, List<Attachment> attachments) {
        /*
         * Determine filename
         */
        String fileName = mf.getFileName();
        if (null == fileName) {
            /*
             * Generate dummy file name
             */
            List<String> exts = MimeType2ExtMap.getFileExtensions(mf.getContentType().toLowerCase(Locale.ENGLISH));
            StringBuilder sb = new StringBuilder("image.");
            if (exts == null) {
                sb.append("dat");
            } else {
                sb.append(exts.get(0));
            }
            fileName = sb.toString();
        } else {
            /*
             * Encode image's file name for being mail-safe
             */
            try {
                fileName = MimeUtility.encodeText(fileName, MailProperties.getInstance().getDefaultMimeCharset(), "Q");
            } catch (UnsupportedEncodingException e) {
                fileName = mf.getFileName();
            }
        }
        /*
         * ... and cid
         */
        if (appendBodyPart) {
            DefaultAttachment att = new DefaultAttachment();
            {
                ContentDisposition cd = new ContentDisposition();
                cd.setInline();
                att.setContentDisposition(cd.toString());
            }
            att.setContentType(mf.getContentType());
            att.setContentId(new StringBuilder(32).append('<').append(id).append('>').toString());
            att.setId(mf.getID());
            att.setSize(mf.getSize());
            att.setStreamProvider(new ManagedFileInputStreamProvider(mf));
            attachments.add(att);
        }
        return id;
    }

    /**
     * Processes a local image and returns its content identifier
     *
     * @param fileHolder The file holder containing the binary content and file meta-data
     * @param id The uploaded file's ID
     * @param appendBodyPart Whether to actually append the part to the snippet
     * @param attachments The attachment list
     * @return The content id
     */
    private final String processLocalImage(ThresholdFileHolder fileHolder, String id, boolean appendBodyPart, List<Attachment> attachments) {
        /*
         * Determine filename
         */
        String fileName = fileHolder.getName();
        if (null == fileName) {
            /*
             * Generate dummy file name
             */
            List<String> exts = MimeType2ExtMap.getFileExtensions(fileHolder.getContentType());
            StringBuilder sb = new StringBuilder("image.");
            if (exts == null) {
                sb.append("dat");
            } else {
                sb.append(exts.get(0));
            }
            fileName = sb.toString();
        } else {
            /*
             * Encode image's file name for being mail-safe
             */
            try {
                fileName = MimeUtility.encodeText(fileName, MailProperties.getInstance().getDefaultMimeCharset(), "Q");
            } catch (UnsupportedEncodingException e) {
                fileName = fileHolder.getName();
            }
        }
        /*
         * ... and cid
         */
        if (appendBodyPart) {
            DefaultAttachment att = new DefaultAttachment();
            {
                ContentDisposition cd = new ContentDisposition();
                cd.setInline();
                cd.setFilenameParameter(fileName);
                att.setContentDisposition(cd.toString());
            }
            att.setContentType(fileHolder.getContentType());
            att.setContentId(new StringBuilder(32).append('<').append(id).append('>').toString());
            att.setId(id);
            att.setSize(fileHolder.getLength());
            att.setStreamProvider(new ThresholdFileHolderInputStreamProvider(fileHolder));
            att.setFilename(fileName);
            attachments.add(att);
        }
        return id;
    }

    private MaxImageProps getMaxImageProps() throws OXException {
        long maxImageSize;
        int maxImageLimit;
        {
            ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class);
            ConfigView configView = configViewFactory.getView(session.getUserId(), session.getContextId());
            ConfigProperty<Integer> maxImageLimitConf = configView.property("com.openexchange.mail.signature.maxImageLimit", Integer.class);

            if (maxImageLimitConf.isDefined()) {
                maxImageLimit = maxImageLimitConf.get().intValue();
            } else {
                // Defaults to 3 images
                maxImageLimit = 3;
            }

            {
                ConfigProperty<Double> misConf = configView.property("com.openexchange.mail.signature.maxImageSize", Double.class);
                final double mis;
                if (misConf.isDefined()) {
                    mis = misConf.get().doubleValue();
                } else {
                    // Defaults to 1 MB
                    mis = (1d);
                }
                maxImageSize = (long) (Math.pow(1024, 2) * mis);
            }
        }
        return new MaxImageProps(maxImageSize, maxImageLimit);
    }

    // --------------------------------------------------------------------------------------------------------------------------- //

    private static class MaxImageProps {

        final long maxImageSize;
        final int maxImageLimit;

        MaxImageProps(long maxImageSize, int maxImageLimit) {
            super();
            this.maxImageSize = maxImageSize;
            this.maxImageLimit = maxImageLimit;
        }
    }

}
