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

package com.openexchange.tools.images;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.slf4j.Logger;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.TransformationContext;
import com.openexchange.imagetransformation.TransformedImage;
import com.openexchange.imagetransformation.TransformedImageCreator;
import com.openexchange.java.Streams;
import com.openexchange.tools.images.transformations.TransformedImageImpl;

/**
 * {@link DefaultTransformedImageCreator} - Default {@link TransformedImageCreator} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultTransformedImageCreator implements TransformedImageCreator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultTransformedImageCreator.class);

    /**
     * Initializes a new {@link DefaultTransformedImageCreator}.
     */
    public DefaultTransformedImageCreator() {
        super();
    }

    @Override
    public TransformedImage writeTransformedImage(BufferedImage image, String formatName, TransformationContext transformationContext, boolean needsCompression) throws IOException {
        if (null == image) {
            return null;
        }
        DigestOutputStream digestOutputStream = null;
        ThresholdFileHolder sink = new ThresholdFileHolder();
        try {
            digestOutputStream = new DigestOutputStream(sink.asOutputStream(), MessageDigest.getInstance("MD5"));
            if (needsCompression) {
                writeCompressed(image, formatName, digestOutputStream, transformationContext);
            } else {
                write(image, formatName, digestOutputStream);
            }

            byte[] md5 = digestOutputStream.getMessageDigest().digest();
            long size = sink.getLength();
            TransformedImageImpl retval = new TransformedImageImpl(image.getWidth(), image.getHeight(), size, formatName, sink, md5, transformationContext.getExpenses());
            sink = null; // Avoid preliminary closing
            return retval;
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        } finally {
            Streams.close(digestOutputStream);
            Streams.close(sink);
        }
    }

    private static void write(BufferedImage image, String formatName, OutputStream output) throws IOException {
        if (false == ImageIO.write(image, formatName, output)) {
            throw new IOException("no appropriate writer found for " + formatName);
        }
    }

    private static void writeCompressed(BufferedImage image, String formatName, OutputStream output, TransformationContext transformationContext) throws IOException {
        ImageWriter writer = null;
        ImageOutputStream imageOutputStream = null;
        try {
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(formatName);
            if (null == iter || false == iter.hasNext()) {
                iter = ImageIO.getImageWritersByMIMEType(formatName);
                if (null == iter || false == iter.hasNext()) {
                    throw new IOException("No image writer for format " + formatName);
                }
            }
            writer = iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            adjustCompressionParams(iwp);
            imageOutputStream = ImageIO.createImageOutputStream(output);
            writer.setOutput(imageOutputStream);
            IIOImage iioImage = new IIOImage(image, null, null);
            writer.write(null, iioImage, iwp);
            transformationContext.addExpense(ImageTransformations.LOW_EXPENSE);
        } finally {
            if (null != writer) {
                writer.dispose();
            }
            if (null != imageOutputStream) {
                imageOutputStream.close();
            }
        }
    }

    /**
     * Tries to adjust the default settings on the supplied image write parameters to apply compression, ignoring any
     * {@link UnsupportedOperationException}s that may occur.
     *
     * @param parameters The parameters to adjust for compression
     */
    private static void adjustCompressionParams(ImageWriteParam parameters) {
        try {
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        } catch (UnsupportedOperationException e) {
            LOG.debug("", e);
        }
        try {
            parameters.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
        } catch (UnsupportedOperationException e) {
            LOG.debug("", e);
        }
        try {
            parameters.setCompressionQuality(0.8f);
        } catch (UnsupportedOperationException e) {
            LOG.debug("", e);
        }
    }

}
