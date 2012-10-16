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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link Mp3ImageDataSource}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Mp3ImageDataSource implements ImageDataSource {

    /**
     * Initializes a new {@link Mp3ImageDataSource}.
     */
    public Mp3ImageDataSource() {
        super();
    }

    public static void main(String[] args) {
        try {
            File src = new File("Skydiving.mp3");

            MP3File mp3 = new MP3File(src);

            BufferedImage coverImage = null;
            TagField imageField = mp3.getID3v2Tag().getFirstField(FieldKey.COVER_ART);
            if (imageField instanceof AbstractID3v2Frame) {
                FrameBodyAPIC imageFrameBody = (FrameBodyAPIC) ((AbstractID3v2Frame) imageField).getBody();
                if (!imageFrameBody.isImageUrl()) {
                    byte[] imageRawData = (byte[]) imageFrameBody.getObjectValue(DataTypes.OBJ_PICTURE_DATA);
                    String mimeType = (String) imageFrameBody.getObjectValue(DataTypes.OBJ_MIME_TYPE);
                    
                    if (mimeType.endsWith("jpg")) {
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream("image.jpg");
                            fos.write(imageRawData);
                            fos.flush();
                        } finally {
                            if (null != fos) {
                                fos.close();
                            }
                        }
                    }
                }
            }

            System.out.println(coverImage);
            System.out.println("DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <D> Data<D> getData(Class<? extends D> type, DataArguments dataArguments, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.conversion.DataSource#getRequiredArguments()
     */
    @Override
    public String[] getRequiredArguments() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.conversion.DataSource#getTypes()
     */
    @Override
    public Class<?>[] getTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.image.ImageDataSource#getRegistrationName()
     */
    @Override
    public String getRegistrationName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.image.ImageDataSource#getAlias()
     */
    @Override
    public String getAlias() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.image.ImageDataSource#parseUrl(java.lang.String)
     */
    @Override
    public ImageLocation parseUrl(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.image.ImageDataSource#generateDataArgumentsFrom(com.openexchange.image.ImageLocation)
     */
    @Override
    public DataArguments generateDataArgumentsFrom(ImageLocation imageLocation) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.image.ImageDataSource#generateUrl(com.openexchange.image.ImageLocation, com.openexchange.session.Session)
     */
    @Override
    public String generateUrl(ImageLocation imageLocation, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.image.ImageDataSource#getExpires()
     */
    @Override
    public long getExpires() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.image.ImageDataSource#getETag(com.openexchange.image.ImageLocation, com.openexchange.session.Session)
     */
    @Override
    public String getETag(ImageLocation imageLocation, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.image.ImageDataSource#parseRequest(com.openexchange.ajax.requesthandler.AJAXRequestData)
     */
    @Override
    public ImageLocation parseRequest(AJAXRequestData requestData) {
        // TODO Auto-generated method stub
        return null;
    }

}
