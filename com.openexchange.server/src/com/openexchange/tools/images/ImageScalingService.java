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

package com.openexchange.tools.images;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import com.openexchange.exception.OXException;

/**
 * {@link ImageScalingService}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface ImageScalingService {

    public InputStream scale(InputStream pictureData, int maxWidth, int maxHeight, ScaleType scaleType) throws IOException;
    public InputStream rotateAccordingExif(InputStream pictureData, String contentType) throws IOException, OXException;
    
    /**
     * Creates a cropped version of the supplied image as defined by a 
     * rectangular region. Coordinates out of the bounds of the original 
     * image are possible, resulting in empty areas in the cropped image.    
     * 
     * @param pictureData an input stream for the source image
     * @param x the X coordinate of the upper-left corner of the specified rectangular region
     * @param y the Y coordinate of the upper-left corner of the specified rectangular region
     * @param width the width of the specified rectangular region
     * @param height the height of the specified rectangular region 
     * @param contentType the content type of the image 
     * @return a stream for the cropped image
     * @throws IOException
     */
    InputStream crop(InputStream pictureData, int x, int y, int width, int height, String contentType) throws IOException;
    
    /**
     * Creates a cropped version of the supplied image as defined by a 
     * rectangular region. Coordinates out of the bounds of the original 
     * image are possible, resulting in empty areas in the cropped image.    
     * 
     * @param sourceImage the source image
     * @param x the X coordinate of the upper-left corner of the specified rectangular region
     * @param y the Y coordinate of the upper-left corner of the specified rectangular region
     * @param width the width of the specified rectangular region
     * @param height the height of the specified rectangular region 
     * @return the cropped image
     * @throws IOException
     */
	BufferedImage crop(BufferedImage sourceImage, int x, int y, int width, int height) throws IOException;

}
