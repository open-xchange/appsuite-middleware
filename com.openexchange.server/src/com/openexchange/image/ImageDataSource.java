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
