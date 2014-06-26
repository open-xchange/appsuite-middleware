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

package com.openexchange.tools.images;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link ImageTransformationService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface ImageTransformationService {

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image considering calling {@link Thread} as source.
     * <p>
     * This is the same as calling <code>transfom(BufferedImage, Object)</code> with latter parameter set to <code>null</code>, thus calling
     * {@link Thread} is referenced as source.
     *
     * @param sourceImage The source image to use
     * @return A new {@link ImageTransformations} instance for the image
     * @see #transfom(BufferedImage, Object)
     */
    ImageTransformations transfom(BufferedImage sourceImage);

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image.
     *
     * @param sourceImage The source image to use
     * @param source The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     * @return A new {@link ImageTransformations} instance for the image
     */
    ImageTransformations transfom(BufferedImage sourceImage, Object source);

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image stream considering calling {@link Thread} as
     * source.
     * <p>
     * This is the same as calling <code>transfom(InputStream, Object)</code> with latter parameter set to <code>null</code>, thus calling
     * {@link Thread} is referenced as source.
     *
     * @param imageStream The source image stream to use
     * @return A new {@link ImageTransformations} instance for the stream
     * @throws IOException If an I/O error occurs
     * @see #transfom(InputStream, Object)
     */
    ImageTransformations transfom(InputStream imageStream) throws IOException;

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image stream.
     *
     * @param imageStream The source image stream to use
     * @param source The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     * @return A new {@link ImageTransformations} instance for the stream
     * @throws IOException If an I/O error occurs
     */
    ImageTransformations transfom(InputStream imageStream, Object source) throws IOException;

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image data considering calling {@link Thread} as
     * source.
     * <p>
     * This is the same as calling <code>transfom(byte[], Object)</code> with latter parameter set to <code>null</code>, thus calling
     * {@link Thread} is referenced as source.
     *
     * @param sourceImage The source image data to use
     * @return A new {@link ImageTransformations} instance for the image
     * @throws IOException If an I/O error occurs
     * @see #transfom(byte[], Object)
     */
    ImageTransformations transfom(byte[] imageData) throws IOException;

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image data.
     *
     * @param sourceImage The source image data to use
     * @param source The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     * @return A new {@link ImageTransformations} instance for the image
     * @throws IOException If an I/O error occurs
     */
    ImageTransformations transfom(byte[] imageData, Object source) throws IOException;

    // ----------------------------------------------------------------------------------------------------------- //

}
