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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.images.transformations;

import java.awt.image.BufferedImage;
import java.io.IOException;
import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleOp;
import com.openexchange.tools.images.ScaleType;
import com.openexchange.tools.images.impl.AutoDimensionConstrain;
import com.openexchange.tools.images.impl.ContainDimensionConstrain;
import com.openexchange.tools.images.impl.CoverDimensionConstrain;
import com.openexchange.tools.images.impl.ImageInformation;

/**
 * {@link ScaleTransformation}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ScaleTransformation implements ImageTransformation {

    private final int maxWidth, maxHeight;
    private final ScaleType scaleType;

    public ScaleTransformation(int maxWidth, int maxHeight, ScaleType scaleType) {
        super();
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.scaleType = scaleType;
    }

    @Override
    public BufferedImage perform(BufferedImage sourceImage, ImageInformation imageInformation) throws IOException {
        DimensionConstrain constrain;
        switch (scaleType) {
        case COVER:
            constrain = new CoverDimensionConstrain(maxWidth, maxHeight);
            break;
        case CONTAIN:
            if (null != sourceImage && maxWidth >= sourceImage.getWidth() && maxHeight >= sourceImage.getHeight()) {
                return sourceImage; // nothing to do
            }
            constrain = new ContainDimensionConstrain(maxWidth, maxHeight);
            break;
        default:
            constrain = new AutoDimensionConstrain(maxWidth, maxHeight);
            break;
        }
        return new ResampleOp(constrain).filter(sourceImage, null);
    }

    @Override
    public boolean needsImageInformation() {
        return false;
    }

    @Override
    public boolean supports(String formatName) {
        return true;
    }

}