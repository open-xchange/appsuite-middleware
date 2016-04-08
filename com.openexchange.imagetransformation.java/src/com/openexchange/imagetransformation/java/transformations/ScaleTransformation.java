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
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import com.openexchange.imagetransformation.ImageInformation;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.imagetransformation.TransformationContext;
import com.openexchange.imagetransformation.java.impl.AutoDimensionConstrain;
import com.openexchange.imagetransformation.java.impl.ContainDimensionConstrain;
import com.openexchange.imagetransformation.java.impl.CoverDimensionConstrain;
import com.openexchange.imagetransformation.java.impl.DimensionConstrain;

/**
 * {@link ScaleTransformation}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ScaleTransformation implements ImageTransformation {

    private final int maxWidth, maxHeight;
    private final ScaleType scaleType;
    private final boolean shrinkOnly;

    /**
     * Initializes a new {@link ScaleTransformation}.
     *
     * @param maxWidth The maximum width of the target image
     * @param maxHeight The maximum height of the target image
     * @param scaleType The scale type to use
     * @param shrinkOnly <code>true</code> to only scale images 'greater than' target size, <code>false</code>, otherwise
     */
    public ScaleTransformation(int maxWidth, int maxHeight, ScaleType scaleType, boolean shrinkOnly) {
        super();
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.scaleType = scaleType;
        this.shrinkOnly = shrinkOnly;
    }

    @Override
    public BufferedImage perform(BufferedImage sourceImage, TransformationContext transformationContext, ImageInformation imageInformation) throws IOException {
        DimensionConstrain constrain;
        switch (scaleType) {
        case COVER:
            constrain = new CoverDimensionConstrain(maxWidth, maxHeight);
            break;
        case CONTAIN:
            constrain = new ContainDimensionConstrain(maxWidth, maxHeight);
            break;
        default:
            constrain = new AutoDimensionConstrain(maxWidth, maxHeight);
            break;
        }
        transformationContext.addExpense(ImageTransformations.HIGH_EXPENSE);
        Dimension dimension = constrain.getDimension(new Dimension(sourceImage.getWidth(), sourceImage.getHeight()));
        int targetWidth = (int) dimension.getWidth();
        int targetHeight = (int) dimension.getHeight();
        if (shrinkOnly && null != sourceImage && maxWidth >= sourceImage.getWidth() && maxHeight >= sourceImage.getHeight()) {
            return sourceImage; // nothing to do
        }
        return Scalr.resize(sourceImage, Method.AUTOMATIC, targetWidth, targetHeight);
    }

    @Override
    public boolean needsImageInformation() {
        return false;
    }

    @Override
    public boolean supports(String formatName) {
        return true;
    }

    @Override
    public Dimension getRequiredResolution(Dimension originalResolution) {
        DimensionConstrain constrain;
        switch (scaleType) {
        case COVER:
            constrain = new CoverDimensionConstrain(maxWidth, maxHeight);
            break;
        case CONTAIN:
            if (null != originalResolution && maxWidth >= originalResolution.getWidth() && maxHeight >= originalResolution.getHeight()) {
                return originalResolution; // nothing to do
            }
            constrain = new ContainDimensionConstrain(maxWidth, maxHeight);
            break;
        default:
            constrain = new AutoDimensionConstrain(maxWidth, maxHeight);
            break;
        }
        return constrain.getDimension(originalResolution);
    }

}