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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.preview.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * {@link TikaImageExtractingParser}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TikaImageExtractingParser implements Parser {

    private final Parser delegate;

    private File imgFolder;

    private final Set<MediaType> types;

    private int count = 0;

    private TikaImageExtractingParser(final Parser delegate) {
        super();
        // Our expected types
        types = new HashSet<MediaType>(6);
        types.add(MediaType.image("bmp"));
        types.add(MediaType.image("gif"));
        types.add(MediaType.image("jpg"));
        types.add(MediaType.image("jpeg"));
        types.add(MediaType.image("png"));
        types.add(MediaType.image("tiff"));
        this.delegate = delegate;
    }

    @Override
    public Set<MediaType> getSupportedTypes(final ParseContext context) {
        return types;
    }

    @Override
    public void parse(final InputStream stream, final ContentHandler handler, final Metadata metadata, final ParseContext context) throws IOException, SAXException, TikaException {
        // Is it a supported image?
        final String filename = metadata.get(Metadata.RESOURCE_NAME_KEY);
        final String type = metadata.get(Metadata.CONTENT_TYPE);
        boolean accept = false;

        if (type != null) {
            for (final MediaType mt : types) {
                if (mt.toString().equals(type)) {
                    accept = true;
                }
            }
        }
        if (filename != null) {
            for (final MediaType mt : types) {
                final String ext = "." + mt.getSubtype();
                if (filename.endsWith(ext)) {
                    accept = true;
                }
            }
        }

        if (!accept) {
            return;
        }

        handleImage(stream, filename, type);
    }

    @Override
    public void parse(final InputStream stream, final ContentHandler handler, final Metadata metadata) throws IOException, SAXException, TikaException {
        parse(stream, handler, metadata, new ParseContext());
    }

    private void handleImage(final InputStream stream, String filename, final String type) {
        count++;

        // Do we already have the folder? If not, create it
        if (imgFolder == null) {
            imgFolder = null;
        }

        // Give it a sensible name if needed
        if (filename == null) {
            filename = "image-" + count + ".";
            filename += type.substring(type.indexOf('/') + 1);
        }

        // Prefix the filename if needed
        filename = "open-xchange-" + filename;

        // Save the image
        //createEmbeddedImage(imgFolder, (count == 1), filename, type, stream);
    }

}
