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
        } catch (final FileNotFoundException e) {
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
