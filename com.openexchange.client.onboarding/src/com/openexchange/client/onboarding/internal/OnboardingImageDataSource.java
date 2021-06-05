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
                    @SuppressWarnings("unchecked") D d = (D) (in);
                    SimpleData<D> data = new SimpleData<D>(d, properties);

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
                    @SuppressWarnings("unchecked") D d = (D) (in);
                    SimpleData<D> data = new SimpleData<D>(d, properties);

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
                    @SuppressWarnings("unchecked") D d = (D) (in);
                    SimpleData<D> data = new SimpleData<D>(d, properties);

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
