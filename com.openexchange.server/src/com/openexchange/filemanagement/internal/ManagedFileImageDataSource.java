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

package com.openexchange.filemanagement.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.session.Session;

/**
 * {@link ManagedFileImageDataSource}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagedFileImageDataSource implements ImageDataSource {

    private static final String REG_NAME = "com.openexchange.image.managedFile";

    private static final String ARG = REG_NAME + ".id";

    private final ManagedFileManagement management;

    /**
     * Initializes a new {@link ManagedFileImageDataSource}.
     * @param management 
     */
    public ManagedFileImageDataSource(ManagedFileManagement management) {
        super();
        this.management = management;
    }

    @Override
    public <D> Data<D> getData(final Class<? extends D> type, final DataArguments dataArguments, final Session session) throws OXException {
        try {
            final ManagedFile managedFile = management.getByID(dataArguments.get(ARG));
            final DataProperties properties = new DataProperties();
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, managedFile.getContentType());
            properties.put(DataProperties.PROPERTY_CHARSET, "UTF-8");
            properties.put(DataProperties.PROPERTY_SIZE, Long.toString(managedFile.getSize()));
            properties.put(DataProperties.PROPERTY_NAME, managedFile.getFileName());
            return new SimpleData<D>((D) new FileInputStream(managedFile.getFile()), properties);
        } catch (FileNotFoundException e) {
            throw DataExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String[] getRequiredArguments() {
        return new String[] { ARG };
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

    @Override
    public String getRegistrationName() {
        return REG_NAME;
    }

    @Override
    public DataArguments generateDataArgumentsFrom(final ImageLocation imageLocation) {
        final DataArguments dataArguments = new DataArguments(1);
        dataArguments.put(ARG, imageLocation.getImageId());
        return dataArguments;
    }

    @Override
    public String generateUrl(final ImageLocation imageLocation, final Session session) throws OXException {
        final StringBuilder sb = new StringBuilder(64);
        /*
         * Nothing special...
         */
        ImageUtility.startImageUrl(imageLocation, session, this, true, sb);
        return sb.toString();
    }

    @Override
    public ImageLocation parseUrl(final String url) {
        return ImageUtility.parseImageLocationFrom(url);
    }

    @Override
    public ImageLocation parseRequest(AJAXRequestData requestData) {
        return ImageUtility.parseImageLocationFrom(requestData);
    }

    @Override
    public long getExpires() {
        return -1;
    }

    @Override
    public String getETag(final ImageLocation imageLocation, final Session session) throws OXException {
        return imageLocation.getImageId();
    }

    private static final String ALIAS = "/mfile/picture";

    @Override
    public String getAlias() {
        return ALIAS;
    }

}
