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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.images.impl.ImageInformation;

/**
 * {@link RotateTransformation}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class RotateTransformation implements ImageTransformation {
    
    private static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(RotateTransformation.class));

    public RotateTransformation() {
        super();
    }
    
    @Override
    public BufferedImage perform(BufferedImage sourceImage, ImageInformation imageInformation) throws IOException {
        if (null == imageInformation) {
            LOG.debug("No image information available, unable to rotate image");
            return sourceImage;
        }
        AffineTransform exifTransformation = getExifTransformation(imageInformation);
        if (null == exifTransformation) {
            LOG.debug("No EXIF transformation available, unable to rotate image");
            return sourceImage;
        }
        int newWidth;
        int newHeight;
        if (imageInformation.orientation <= 4) {
            newWidth = sourceImage.getWidth();
            newHeight = sourceImage.getHeight();
        } else {
            newWidth = sourceImage.getHeight();
            newHeight = sourceImage.getWidth();
        }
        BufferedImage destinationImage = new BufferedImage(newWidth, newHeight, sourceImage.getType());
        Graphics2D g = destinationImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
        g.drawImage(sourceImage, exifTransformation, null);
        return destinationImage;
    }

    private AffineTransform getExifTransformation(ImageInformation info) {
        AffineTransform t = new AffineTransform();

        switch (info.orientation) {
        default:
        case 1:
            return null;
        case 2:
            t.scale(-1.0, 1.0);
            t.translate(-info.width, 0);
            break;
        case 3:
            t.translate(info.width, info.height);
            t.rotate(Math.PI);
            break;
        case 4:
            t.scale(1.0, -1.0);
            t.translate(0, -info.height);
            break;
        case 5:
            t.rotate(-Math.PI / 2);
            t.scale(-1.0, 1.0);
            break;
        case 6:
            t.translate(info.height, 0);
            t.rotate(Math.PI / 2);
            break;
        case 7:
            t.scale(-1.0, 1.0);
            t.translate(-info.height, 0);
            t.translate(0, info.width);
            t.rotate(3 * Math.PI / 2);
            break;
        case 8:
            t.translate(0, info.width);
            t.rotate(3 * Math.PI / 2);
            break;
        }
        return t;
    }
    
    @Override
    public boolean needsImageInformation() {
        return true;
    }

    @Override
    public boolean supports(String formatName) {
        return null != formatName && "jpeg".equalsIgnoreCase(formatName) || "jpg".equalsIgnoreCase(formatName) || 
            "tiff".equalsIgnoreCase(formatName) || "psd".equalsIgnoreCase(formatName);
    }

}