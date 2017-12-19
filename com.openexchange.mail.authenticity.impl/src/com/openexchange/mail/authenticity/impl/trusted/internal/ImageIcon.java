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

package com.openexchange.mail.authenticity.impl.trusted.internal;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.server.UID;
import javax.imageio.ImageIO;
import com.openexchange.mail.authenticity.impl.trusted.Icon;

/**
 * {@link ImageIcon}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ImageIcon implements Icon {

    private final byte[] byteArray;
    private static final String MIME_TYPE = "image/png";
    private static final String FORMAT = "png";
    private final String UID;
    private static final String PREFIX = "trustedMail_";

    /**
     * Initializes a new {@link ImageIcon}.
     *
     * @param url A url to a valid image
     * @throws IOException
     * @throws MalformedURLException
     */
    public ImageIcon(URL url) throws MalformedURLException, IOException {
        super();
        BufferedImage image = ImageIO.read(url);
        if(image==null) {
            throw new IOException("No image found");
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, FORMAT, stream);
        byteArray = stream.toByteArray();
        UID = PREFIX+new UID().toString();

    }

    /**
     * Initializes a new {@link ImageIcon}.
     *
     * @param file An image file
     * @throws IOException
     * @throws MalformedURLException
     */
    public ImageIcon(File file) throws MalformedURLException, IOException {
        super();
        BufferedImage image = ImageIO.read(file);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, FORMAT, stream);
        byteArray = stream.toByteArray();
        UID = PREFIX+new UID().toString();
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public byte[] getData() {
        return byteArray;
    }

    @Override
    public String getUID() {
        return UID;
    }

}
