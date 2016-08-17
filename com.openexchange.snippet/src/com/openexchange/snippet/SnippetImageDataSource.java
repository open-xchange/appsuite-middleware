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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetManagement;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.internal.Services;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link SnippetImageDataSource}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SnippetImageDataSource implements ImageDataSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SnippetImageDataSource.class);

    private static final SnippetImageDataSource INSTANCE = new SnippetImageDataSource();

    private static final String REGISTRATION_NAME = "com.openexchange.snippet.image";

    private static final String ALIAS = "/snippet/image";

    private static final long EXPIRES = ImageDataSource.YEAR_IN_MILLIS * 50;

    private static final String[] ARGS = { "com.openexchange.snippet.id", "com.openexchange.snippet.cid" };

    /**
     * Returns the instance
     *
     * @return the instance
     */
    public static SnippetImageDataSource getInstance() {
        return INSTANCE;
    }

    private volatile ServiceListing<SnippetService> snippetServices;

    /**
     * Initializes a new {@link SnippetImageDataSource}.
     */
    private SnippetImageDataSource() {
        super();

    }

    /**
     * Applies the service listing
     *
     * @param snippetServices The service listing
     */
    public void setServiceListing(ServiceListing<SnippetService> snippetServices) {
        this.snippetServices = snippetServices;
    }

    /**
     * Gets the snippet service.
     *
     * @param serverSession The server session
     * @return The snippet service
     * @throws OXException If appropriate Snippet service cannot be returned
     */
    private SnippetService getSnippetService(Session serverSession) throws OXException {
        ServiceListing<SnippetService> snippetServices = this.snippetServices;
        if (null == snippetServices) {
            return null;
        }
        CapabilityService capabilityService = Services.optService(CapabilityService.class);
        for (SnippetService snippetService : snippetServices.getServiceList()) {
            List<String> neededCapabilities = snippetService.neededCapabilities();
            if (null == capabilityService || (null == neededCapabilities || neededCapabilities.isEmpty())) {
                // Either no capabilities signaled or service is absent (thus not able to check)
                return snippetService;
            }
            CapabilitySet capabilities = capabilityService.getCapabilities(serverSession);
            boolean contained = true;
            for (int i = neededCapabilities.size(); contained && i-- > 0;) {
                contained = capabilities.contains(neededCapabilities.get(i));
            }
            if (contained) {
                return snippetService;
            }
        }
        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SnippetService.class.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D> Data<D> getData(Class<? extends D> type, DataArguments dataArguments, Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }

        String id = dataArguments.get(ARGS[0]);
        if (id == null) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[0]);
        }

        String cid = dataArguments.get(ARGS[1]);
        if (cid == null) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[1]);
        }

        final DataProperties properties = new DataProperties(4);

        SnippetService snippetService = getSnippetService(session);
        if (null == snippetService) {
            throw ServiceExceptionCode.absentService(SnippetService.class);
        }

        SnippetManagement ssManagement = snippetService.getManagement(session);
        Snippet snippet = ssManagement.getSnippet(id);
        List<Attachment> attachments = snippet.getAttachments();
        if (!attachments.isEmpty()) {

            for (Attachment attachment : attachments) {
                String contentId = attachment.getContentId();
                if (MimeMessageUtility.equalsCID(cid, contentId) || MimeMessageUtility.equalsCID(cid, attachment.getId())) {
                    ThresholdFileHolder sink = null;
                    boolean closeSink = true;
                    try {
                        sink = new ThresholdFileHolder();
                        sink.write(attachment.getInputStream());

                        properties.put(DataProperties.PROPERTY_ID, id);
                        properties.put(DataProperties.PROPERTY_CONTENT_TYPE, attachment.getContentType());
                        properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(sink.getLength()));
                        properties.put(DataProperties.PROPERTY_NAME, determineFileName(attachment));

                        InputStream in = sink.getClosingStream();
                        closeSink = false;
                        return new SimpleData<D>((D) (in), properties);
                    } catch (IOException e) {
                        throw DataExceptionCodes.IO_ERROR.create(e, e.getMessage());
                    } finally {
                        if (closeSink) {
                            Streams.close(sink);
                        }
                    }
                }
            }
        }

        LOG.warn("Requested a non-existing image in snippet: snippet-id={} context={} session-user={}\nReturning an empty image as fallback.", id, session.getContextId(), session.getUserId());
        properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
        properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));

        return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
    }

    private String determineFileName(Attachment attachment) {
        String str = attachment.getContentDisposition();
        if (null != str) {
            try {
                ContentDisposition cd = new ContentDisposition(str);
                String fileName = cd.getFilenameParameter();
                if (null != fileName) {
                    return fileName;
                }
            } catch (OXException e) {
                // Invalid Content-Disposition
            }
        }

        str = attachment.getContentType();
        if (null != str) {
            try {
                ContentType ct = new ContentType(str);
                return ct.getNameParameter();
            } catch (OXException e) {
                // Invalid Content-Disposition
            }

            int pos = str.indexOf(';');
            String ext = MimeType2ExtMap.getFileExtension((pos > 0 ? str.substring(0, pos) : str).trim());
            return  "file." + ext;
        }

        return "file.dat";
    }

    @Override
    public String[] getRequiredArguments() {
        final String[] args = new String[ARGS.length];
        System.arraycopy(ARGS, 0, args, 0, ARGS.length);
        return args;
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

    @Override
    public String getRegistrationName() {
        return REGISTRATION_NAME;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public ImageLocation parseUrl(String url) {
        return ImageUtility.parseImageLocationFrom(url);
    }

    @Override
    public DataArguments generateDataArgumentsFrom(ImageLocation imageLocation) {
        final DataArguments dataArgs = new DataArguments(2);
        dataArgs.put(ARGS[0], imageLocation.getId());
        dataArgs.put(ARGS[1], imageLocation.getImageId());
        return dataArgs;
    }

    @Override
    public String generateUrl(ImageLocation imageLocation, Session session) throws OXException {
        final StringBuilder sb = new StringBuilder(64);
        ImageUtility.startImageUrl(imageLocation, session, this, true, sb);
        return sb.toString();
    }

    @Override
    public long getExpires() {
        return EXPIRES;
    }

    @Override
    public String getETag(ImageLocation imageLocation, Session session) throws OXException {
        final char delim = '#';
        final StringBuilder builder = new StringBuilder(128);
        builder.append(delim).append(imageLocation.getId());
        builder.append(delim).append(imageLocation.getImageId());
        builder.append(delim);
        return ImageUtility.getMD5(builder.toString(), "hex");
    }

    @Override
    public ImageLocation parseRequest(AJAXRequestData requestData) {
        return ImageUtility.parseImageLocationFrom(requestData);
    }

}
