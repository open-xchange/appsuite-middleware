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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.image.internal;

import java.util.regex.Pattern;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.image.ImageDataSource;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link ImageIDGenerator} - The ID generator.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ImageIDGenerator {

    /**
     * The delimiter.
     */
    private static final char DELIM = '#';

    private static final String SECRET = String.valueOf(OXException.getServerId());

    /**
     * Initializes a new {@link ImageIDGenerator}.
     */
    private ImageIDGenerator() {
        super();
    }

    /**
     * Generates an image ID from specified arguments.
     * 
     * @param imageSource The image data source
     * @param imageArguments The data arguments for image data source
     * @param authId The auth id of the session that needs the generated id
     * @return The generated ID
     */
    static String generateId(final ImageDataSource imageSource, final DataArguments imageArguments, final String authId) {
        final StringBuilder sb = new StringBuilder(64);
        final String[] requiredArguments = imageSource.getRequiredArguments();
        sb.append(imageSource.getRegistrationName());
        for (final String arg : requiredArguments) {
            sb.append(DELIM).append(imageArguments.get(arg));
        }
        sb.append(DELIM).append(authId);
        try {
            final CryptoService cryptoService = ServerServiceRegistry.getInstance().getService(CryptoService.class);
            return cryptoService.encrypt(sb.toString(), SECRET);
        } catch (final OXException e) {
            // Cannot occur
            return sb.toString();
        }
    }

    private static final Pattern SPLIT = Pattern.compile(Pattern.quote(String.valueOf(DELIM)));

    /**
     * Parses specified ID to appropriate data source and data arguments.
     * 
     * @param uniqueId The ID
     * @param service The conversion service
     * @return The data source and data arguments wrapped (in this order) in an array or <code>null</code>
     */
    static Object[] parseId(final String uniqueId, final ConversionService service) {
        final String[] args;
        try {
            final CryptoService cryptoService = ServerServiceRegistry.getInstance().getService(CryptoService.class);
            final String toSplit = cryptoService.decrypt(uniqueId, SECRET);
            args = SPLIT.split(toSplit, 0);
        } catch (final OXException e) {
            com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ImageIDGenerator.class)).warn(e.getMessage(), e);
            return null;
        }
        /*
         * Get data source from conversion service
         */
        final ImageDataSource dataSource;
        try {
            dataSource = (ImageDataSource) service.getDataSource(args[0]);
        } catch (final ClassCastException e) {
            com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ImageIDGenerator.class)).warn(e.getMessage(), e);
            return null;
        }
        if (null == dataSource) {
            /*
             * No data source
             */
            com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ImageIDGenerator.class)).warn(
                new StringBuilder(64).append("Image not found: No data source found for identifier: ").append(args[0]).toString());
            return null;
        }
        /*
         * Return data source and appropriate data arguments
         */
        final String[] requiredArguments = dataSource.getRequiredArguments();
        if (requiredArguments.length > (args.length - 1)) {
            /*
             * Argument mismatch
             */
            com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ImageIDGenerator.class)).warn(
                new StringBuilder(64).append("Image not found: Argument mismatch. Expected ").append(requiredArguments.length).append(
                    " argument(s), but was ").append((args.length - 1)).toString());
            return null;
        }
        final DataArguments dataArguments = new DataArguments();
        for (int i = 0; i < requiredArguments.length; i++) {
            dataArguments.put(requiredArguments[i], args[i + 1]);
        }
        return new Object[] { dataSource, dataArguments };
    }

}
