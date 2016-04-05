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

package com.openexchange.imagetransformation.java.transformations;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import com.openexchange.imagetransformation.ImageInformation;
import com.openexchange.imagetransformation.TransformationContext;

/**
 * {@link ImageTransformation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface ImageTransformation {

    /**
     * Performs the image transformation.
     *
     * @param sourceImage The source image
     * @param transformationContext The transformation context
     * @param imageInformation The additional image information, or <code>null</code> if not needed
     * @return The resulting image
     */
    BufferedImage perform(BufferedImage sourceImage, TransformationContext transformationContext, ImageInformation imageInformation) throws IOException;

    /**
     * Gets a value indicating whether the supplied image format is supported by the transformation or not.
     *
     * @param formatName The image format name, e.g. <code>jpeg</code> or <code>tiff</code>
     * @return <code>true</code>, if the format is supported, <code>false</code>, otherwise
     */
    boolean supports(String formatName);

    /**
     * Gets a value indicating whether the transformation needs additional image information or not.
     *
     * @return <code>true</code>, if additional information is required, <code>false</code>, otherwise
     */
    boolean needsImageInformation();

    /**
     * Gets the resolution that is required for this transformation to operate on, based on the input image's dimensions.
     *
     * @param originalResolution The dimension of the source image
     * @return The required resolution, or <code>null</code> if not relevant
     */
    Dimension getRequiredResolution(Dimension originalResolution);

}