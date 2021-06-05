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

package com.openexchange.image;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ImageDataSource} - An image data source.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ImageDataSource extends DataSource {

    /**
     * The image servlet's alias
     */
    public static final String ALIAS_APPENDIX = "image";

    public static final long SECOND_IN_MILLIS = 1000;

    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;

    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;

    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

    public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;

    /**
     * This constant is actually the length of 364 days, not of a year!
     */
    public static final long YEAR_IN_MILLIS = WEEK_IN_MILLIS * 52;

    /**
     * Gets this data source's registration name.
     *
     * @return The registration name
     */
    String getRegistrationName();

    /**
     * Gets the alias (starting with <code>'/'</code> character).
     *
     * @return The alias
     */
    String getAlias();

    /**
     * Parses specified URL to its image location.
     *
     * @param url The URL to parse
     * @return The resulting image location
     */
    ImageLocation parseUrl(String url);

    /**
     * Generates appropriate data arguments for specified image location.
     *
     * @param imageLocation The image location
     * @return The appropriate data arguments
     */
    DataArguments generateDataArgumentsFrom(ImageLocation imageLocation);

    /**
     * Generates the URL linking to image data
     *
     * @return The image URL
     * @throws OXException If generating the URL fails
     */
    String generateUrl(ImageLocation imageLocation, Session session) throws OXException;

    /**
     * Gets the expires (time-to-live)
     *
     * @return The expires or <code>-1</code> for no expiry
     */
    long getExpires();

    /**
     * Gets the ETag for this image data source.
     *
     * @param imageLocation The image location
     * @param session The session
     * @return The ETag
     * @throws OXException If ETag cannot be returned
     */
    String getETag(ImageLocation imageLocation, Session session) throws OXException;

    /**
     * Parses specified AJAXRequestData to its image location.
     *
     * @param requestData The AJAXRequestData to parse
     * @return The resulting image location
     * @throws IllegalArgumentException If parse attempt fails
     */
    ImageLocation parseRequest(AJAXRequestData requestData);

}
