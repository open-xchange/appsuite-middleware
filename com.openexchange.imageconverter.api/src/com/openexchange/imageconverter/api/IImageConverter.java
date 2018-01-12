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

package com.openexchange.imageconverter.api;

import java.io.InputStream;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link IImageConverter}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10
 */
@SingletonService
public interface IImageConverter {

    /**
     * Creates a new image group with the given key and context. If a group with the same key
     * already exists, its single target images will be overwritten based on the source image from
     * the given {@link InputStream}
     * @param key
     * @param srcImageStm
     * @param context (optional, default <code>null</code>)
     * @throws ImageConverterException
     */
    public void cacheImage(final String key, final InputStream srcImageStm, final String... context) throws ImageConverterException;

    /**
     * @param key
     * @param requestFormat
     * @param context (optional, default <code>null</code>)
     * @return
     * @throws ImageConverterException
     */
    public InputStream getImage(final String key, final String requestFormat, final String... context) throws ImageConverterException;

    /**
     * @param key
     * @param requestFormat
     * @param context (optional, default <code>null</code>)
     * @return
     * @throws ImageConverterException
     */
    public IMetadata getMetadata(final String key, final String... context) throws ImageConverterException;

    /**
     * @param key
     * @param requestFormat
     * @param context
     * @return
     * @throws ImageConverterException
     */
    public MetadataImage getImageAndMetadata(final String key, final String requestFormat, final String... context) throws ImageConverterException;

    /**
     * Getting the {@link InputStream} of the image from the image group with the given key, whose
     * target format best matches the requested format. If no such image group is contained, a new
     * group with the given key and context will be created based on the given source image
     * {@link InputStream}
     *
     *
     * @param key
     * @param requestFormat
     * @param srcImageStm
     * @param context (optional, default <code>null</code>)
     * @return
     * @throws ImageConverterException
     */
    public InputStream cacheAndGetImage(final String key, final String requestFormat, final InputStream srcImageStm, final String... context) throws ImageConverterException;

    /**
     * @param key
     * @param requestFormat
     * @param srcImageStm
     * @param context
     * @return
     * @throws ImageConverterException
     */
    public MetadataImage cacheAndGetImageAndMetadata(final String key, final String requestFormat, final InputStream srcImageStm, final String... context) throws ImageConverterException;

    /**
     * Clears all contained image groups
     *
     * @param context (optional, default <code>null</code>)
     * @throws ImageConverterException
     * @throws ApiException
     */
    public void clearImages(final String... context) throws ImageConverterException;

    /**
     * Clears the image group with the given key
     *
     *
     * @param key
     * @throws ImageConverterException
     */
    public void clearImagesByKey(final String key) throws ImageConverterException;

    /**
     * Querying the number of cached image keys
     *
     * @param context (optional, default <code>null</code>)
     * @return The number of keys
     */
    public long getKeyCount(final String... context) throws ImageConverterException;

    /**
     * Querying all contained image keys.</br>
     * If the result contains no keys, an empty array is returned.
     *
     * @param context (optional, default <code>null</code>)
     * @return The contained keys as array of {@link String}, which might be of length 0.
     */
    public String[] getKeys(final String... context) throws ImageConverterException;

    /**
     * Querying the size of all images.</br>
     * The size is defined as summed up size of all images stored for each cached key.
     *
     * @param context (optional, default <code>null</code>)
     * @return The summed up size of all images.
     */
    public long getTotalImagesSize(final String... context) throws ImageConverterException;
}


