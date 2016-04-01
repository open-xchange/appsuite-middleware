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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import com.openexchange.imagetransformation.ImageInformation;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.TransformationContext;

/**
 * {@link CropTransformation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CropTransformation implements ImageTransformation {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CropTransformation.class);

    private final int x, y, width, height;

    public CropTransformation(int x, int y, int width, int height) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public BufferedImage perform(BufferedImage sourceImage, TransformationContext transformationContext, ImageInformation imageInformation) throws IOException {
        /*
         * prepare target image
         */
        BufferedImage targetImage = null;
        if (0 <= x && sourceImage.getWidth() > x && sourceImage.getWidth() >= x + width &&
                0 <= y && sourceImage.getHeight() > y && sourceImage.getHeight() >= y + height) {
            /*
             * extract sub-image directly
             */
            targetImage = sourceImage.getSubimage(x, y, width, height);
            transformationContext.addExpense(ImageTransformations.LOW_EXPENSE);
        } else {
            /*
             * draw partial region to target image
             */
            try {
                targetImage = new BufferedImage(width, height, sourceImage.getType(), (IndexColorModel)sourceImage.getColorModel());
            } catch (ClassCastException e) {
                LOG.debug("Can't reuse source image's color model, falling back to defaults.", e);
                targetImage = new BufferedImage(width, height, sourceImage.getType());
            }
            Graphics2D graphics = targetImage.createGraphics();
            graphics.setBackground(new Color(255, 255, 255, 0));
            graphics.clearRect(0, 0, width, height);
            graphics.drawImage(sourceImage, x, y, null);
            transformationContext.addExpense(ImageTransformations.HIGH_EXPENSE);
        }
        return targetImage;
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
        return originalResolution;
    }

}