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

package com.openexchange.mail.authenticity.impl.trusted.internal;

import java.io.InputStream;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
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
import com.openexchange.java.Streams;
import com.openexchange.mail.authenticity.impl.osgi.Services;
import com.openexchange.mail.authenticity.impl.trusted.Icon;
import com.openexchange.mail.authenticity.impl.trusted.TrustedMailService;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 *
 * {@link TrustedMailDataSource} is a source for trusted mail images
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public final class TrustedMailDataSource implements ImageDataSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrustedMailDataSource.class);

    private static final TrustedMailDataSource INSTANCE = new TrustedMailDataSource();

    private static final String ARG = "com.openexchange.mail.authenticity.trustedMail.uid";
    private static final String ALIAS = "/mail/trustedMail/picture";
    private static final String REGISTRATION_NAME = "com.openexchange.mail.authenticity.trustedMail";

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static TrustedMailDataSource getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link TrustedMailDataSource}.
     */
    private TrustedMailDataSource() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D> Data<D> getData(final Class<? extends D> type, final DataArguments dataArguments, final Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }

        TrustedMailService service = Services.getService(TrustedMailService.class);
        if (service == null) {
            throw ServiceExceptionCode.absentService(TrustedMailService.class);
        }

        final String uid;
        {
            final String val = dataArguments.get(ARG);
            if (val == null) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARG);
            }
            uid = val.toString();
        }

        Icon icon = null;
        try {
            icon = service.getIcon(session, uid);
        } catch (OXException e) {
            LOG.debug(e.getMessage());
        }

        if (icon == null) {
            LOG.debug("Requested a non-existing trusted mail image. Returning an empty image as fallback.");
            DataProperties properties = new DataProperties();
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "image/jpg");
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
            properties.put(DataProperties.PROPERTY_NAME, "image.jpg");
            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
        }

        byte[] imageBytes = icon.getData();
        String mimeType = icon.getMimeType();

        DataProperties properties = new DataProperties();
        properties.put(DataProperties.PROPERTY_CONTENT_TYPE, mimeType);
        properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(imageBytes.length));
        if (null != mimeType) {
            final List<String> extensions = MimeType2ExtMap.getFileExtensions(mimeType);
            properties.put(DataProperties.PROPERTY_NAME, "image." + extensions.get(0));
        }
        return new SimpleData<D>((D) (Streams.newByteArrayInputStream(imageBytes)), properties);
    }


    @Override
    public String[] getRequiredArguments() {
        return new String[] {ARG};
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
    public ImageLocation parseUrl(final String url) {
        return ImageUtility.parseImageLocationFrom(url);
    }

    @Override
    public DataArguments generateDataArgumentsFrom(final ImageLocation imageLocation) {
        final DataArguments dataArguments = new DataArguments(1);
        dataArguments.put(ARG, imageLocation.getImageId());
        return dataArguments;
    }

    @Override
    public String generateUrl(final ImageLocation imageLocation, final Session session) throws OXException {
        StringBuilder sb = new StringBuilder();
        ImageUtility.startImageUrl(imageLocation, session, this, true, sb);
        return sb.toString();

    }

    @Override
    public long getExpires() {
        return -1L;
    }

    @Override
    public String getETag(final ImageLocation imageLocation, final Session session) throws OXException {
        return imageLocation.getImageId();

    }

    @Override
    public ImageLocation parseRequest(final AJAXRequestData requestData) {
        String parameter = requestData.getParameter(AJAXServlet.PARAMETER_UID);
        return new ImageLocation.Builder(parameter).build();
    }
}
