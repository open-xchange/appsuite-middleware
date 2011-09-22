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

package com.openexchange.langdetect.internal;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.olivo.lc4j.LanguageCategorization;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.langdetect.LanguageDetectionService;

/**
 * {@link Lc4jLanguageDetectionService}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Lc4jLanguageDetectionService implements LanguageDetectionService {

    private static final int BUFFER_SIZE = 2048;

    private final LanguageCategorization defaultLanguageCategorization;

    /**
     * Initializes a new {@link Lc4jLanguageDetectionService}.
     */
    public Lc4jLanguageDetectionService() {
        super();
        defaultLanguageCategorization = new LanguageCategorization();
        defaultLanguageCategorization.setMaxLanguages(10);
        defaultLanguageCategorization.setNumCharsToExamine(1000);
        defaultLanguageCategorization.setUseTopmostNgrams(400);
        defaultLanguageCategorization.setUnknownThreshold(1.01f);
        defaultLanguageCategorization.setLanguageModelsDir("./models/");
    }

    @Override
    public List<String> findLanguages(final InputStream inputStream) throws OXException {
        // read from stream
        final ByteArrayList input = new ByteArrayList();
        final DataInputStream dis = new DataInputStream(new FastBufferedInputStream(inputStream, BUFFER_SIZE));
        try {
            while (true) {
                input.add(dis.readByte());
            }
        } catch (final EOFException e) {
            // Reached EOF
        } catch (final IOException e) {
            // TODO:
        } finally {
            Streams.close(dis);
        }
        return defaultLanguageCategorization.findLanguage(input);
    }

}
