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

package com.openexchange.imagetransformation.java.transformations;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import org.slf4j.Logger;
import com.openexchange.imagetransformation.ImageInformation;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.TransformationContext;

/**
 * {@link CropTransformation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CropTransformation implements ImageTransformation {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CropTransformation.class);
    }

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
                LoggerHolder.LOG.debug("Can't reuse source image's color model, falling back to defaults.", e);
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