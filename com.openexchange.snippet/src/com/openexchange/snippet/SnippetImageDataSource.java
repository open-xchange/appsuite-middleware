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

package com.openexchange.snippet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
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

    private static final String[] ARGS = { "com.openexchange.snippet.id" };

    /**
     * Returns the instance
     *
     * @return the instance
     */
    public static SnippetImageDataSource getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link SnippetImageDataSource}.
     */
    private SnippetImageDataSource() {
        super();

    }

    @SuppressWarnings("unchecked")
    @Override
    public <D> Data<D> getData(Class<? extends D> type, DataArguments dataArguments, Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }

        final int signId;
        final String arg = dataArguments.get(ARGS[0]);
        if (arg == null) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[0]);
        }
        try {
            signId = Integer.parseInt(arg);
        } catch (final NumberFormatException e) {
            throw DataExceptionCodes.INVALID_ARGUMENT.create(e, ARGS[0], arg);
        }

        final DataProperties properties = new DataProperties(4);

        final SnippetService snippetService = Services.getService(SnippetService.class);
        final SnippetManagement ssManagement = snippetService.getManagement(session);
        final Snippet snippet = ssManagement.getSnippet(arg);
        final List<Attachment> attachments = snippet.getAttachments();
        final byte[] imageBytes;

        if (attachments.size() > 0) {
            final Attachment attachment = attachments.get(0); // There should only be one embedded image in the attachment
            try {
                imageBytes = IOUtils.toByteArray(attachment.getInputStream());
            } catch (IOException e) {
                throw DataExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }

            properties.put(DataProperties.PROPERTY_ID, arg);
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, attachment.getContentType());
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(imageBytes.length));
            properties.put(DataProperties.PROPERTY_NAME, attachment.getId());

            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(imageBytes)), properties);

        } else {
            LOG.warn(
                "Requested a non-existing image in snippet: snippet-id={} context={} session-user={}\nReturning an empty image as fallback.",
                signId,
                session.getContextId(),
                session.getUserId());
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));

            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
        }
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
