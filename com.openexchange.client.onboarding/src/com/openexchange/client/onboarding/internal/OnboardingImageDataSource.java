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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.client.onboarding.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.Bundle;
import com.openexchange.ajax.container.ThresholdFileHolder;
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
import com.openexchange.java.ImageTypeDetector;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.osgi.BundleResourceLoader;
import com.openexchange.session.Session;

/**
 * {@link OnboardingImageDataSource}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OnboardingImageDataSource implements ImageDataSource {

    private static final AtomicReference<Bundle> BUNDLE_REF = new AtomicReference<Bundle>(null);

    /**
     * Sets the associated bundle
     *
     * @param bundle The bundle to set
     */
    public static void setBundle(Bundle bundle) {
        BUNDLE_REF.set(bundle);
    }

    private static final OnboardingImageDataSource INSTANCE = new OnboardingImageDataSource();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static OnboardingImageDataSource getInstance() {
        return INSTANCE;
    }

    private static final long EXPIRES = ImageDataSource.YEAR_IN_MILLIS * 50;

    // --------------------------------------------------------------------------------------------------------

    private final String[] args;
    private final String alias;
    private final String registrationName;

    /**
     * Initializes a new {@link OnboardingImageDataSource}.
     */
    private OnboardingImageDataSource() {
        super();
        args = new String[] { "com.openexchange.client.onboarding.scheme", "com.openexchange.client.onboarding.name" };
        alias = "/onboarding/image";
        registrationName = "com.openexchange.client.onboarding.image";
    }

    @Override
    public <D> Data<D> getData(Class<? extends D> type, DataArguments dataArguments, Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }

        String scheme = Strings.toLowerCase(dataArguments.get(args[0]));
        if (scheme == null) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(args[0]);
        }

        String name = dataArguments.get(args[1]);
        if (name == null) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(args[1]);
        }
        name = ConfiguredLinkImage.getRealNameFor(name);
        if (name == null) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create(args[1]);
        }

        try {
            if (scheme.equals("resource")) {
                BundleResourceLoader loader = new BundleResourceLoader(BUNDLE_REF.get());

                boolean error = true;
                ThresholdFileHolder sink = null;
                try {
                    sink = new ThresholdFileHolder();
                    sink.write(loader.getResourceAsStream(name));

                    String mimeType = ImageTypeDetector.getMimeType(sink.getStream());

                    DataProperties properties = new DataProperties(4);
                    properties.put(DataProperties.PROPERTY_ID, name);
                    properties.put(DataProperties.PROPERTY_CONTENT_TYPE, mimeType);
                    properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(sink.getLength()));
                    properties.put(DataProperties.PROPERTY_NAME, "image." + mimeType.substring(mimeType.indexOf('/') + 1));

                    InputStream in = sink.getClosingStream();
                    SimpleData<D> data = new SimpleData<D>((D) (in), properties);

                    error = false;
                    return data;
                } finally {
                    if (error) {
                        Streams.close(sink);
                    }
                }
            }

            if (scheme.equals("file")) {
                boolean error = true;
                ThresholdFileHolder sink = null;
                try {
                    File file = new File(name);

                    sink = new ThresholdFileHolder();
                    sink.write(new FileInputStream(file));

                    String mimeType = ImageTypeDetector.getMimeType(sink.getStream());

                    DataProperties properties = new DataProperties(4);
                    properties.put(DataProperties.PROPERTY_ID, name);
                    properties.put(DataProperties.PROPERTY_CONTENT_TYPE, mimeType);
                    properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(sink.getLength()));
                    properties.put(DataProperties.PROPERTY_NAME, file.getName());

                    InputStream in = sink.getClosingStream();
                    SimpleData<D> data = new SimpleData<D>((D) (in), properties);

                    error = false;
                    return data;
                } finally {
                    if (error) {
                        Streams.close(sink);
                    }
                }
            }

            if (scheme.equals("http") || scheme.equals("https")) {
                URL url = new URL(scheme + "://" + name);

                HttpURLConnection connection = null;
                boolean error = true;
                ThresholdFileHolder sink = null;
                try {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    String mimeType = connection.getContentType();
                    String fileName = null;
                    {
                        String cds = connection.getHeaderField("Content-Disposition");
                        ContentDisposition contentDisposition;
                        if (null != cds) {
                            contentDisposition = new ContentDisposition(cds);
                            fileName = contentDisposition.getFilenameParameter();
                        }
                    }

                    sink = new ThresholdFileHolder();
                    sink.write(connection.getInputStream());

                    if (null == mimeType) {
                        mimeType = ImageTypeDetector.getMimeType(sink.getStream());
                    } else {
                        mimeType = Strings.asciiLowerCase(mimeType);
                    }

                    DataProperties properties = new DataProperties(4);
                    properties.put(DataProperties.PROPERTY_ID, name);
                    properties.put(DataProperties.PROPERTY_CONTENT_TYPE, mimeType);
                    properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(sink.getLength()));
                    properties.put(DataProperties.PROPERTY_NAME, null != fileName ? fileName : "image." + mimeType.substring(mimeType.indexOf('/') + 1));

                    InputStream in = sink.getClosingStream();
                    SimpleData<D> data = new SimpleData<D>((D) (in), properties);

                    error = false;
                    return data;
                } finally {
                    if (null != connection) {
                        connection.disconnect();
                    }
                    if (error) {
                        Streams.close(sink);
                    }
                }
            }

            throw DataExceptionCodes.ERROR.create("Unknown scheme: " + scheme);
        } catch (MalformedURLException e) {
            throw DataExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw DataExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String[] getRequiredArguments() {
        String[] args = new String[this.args.length];
        System.arraycopy(this.args, 0, args, 0, this.args.length);
        return args;
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

    @Override
    public String getRegistrationName() {
        return registrationName;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public ImageLocation parseUrl(String url) {
        return ImageUtility.parseImageLocationFrom(url);
    }

    @Override
    public DataArguments generateDataArgumentsFrom(ImageLocation imageLocation) {
        DataArguments dataArgs = new DataArguments(2);
        dataArgs.put(args[0], imageLocation.getId());
        dataArgs.put(args[1], imageLocation.getImageId());
        return dataArgs;
    }

    @Override
    public String generateUrl(ImageLocation imageLocation, Session session) throws OXException {
        StringBuilder sb = new StringBuilder(64);
        ImageUtility.startImageUrl(imageLocation, session, this, true, sb);
        return sb.toString();
    }

    @Override
    public long getExpires() {
        return EXPIRES;
    }

    @Override
    public String getETag(ImageLocation imageLocation, Session session) throws OXException {
        char delim = '#';
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
