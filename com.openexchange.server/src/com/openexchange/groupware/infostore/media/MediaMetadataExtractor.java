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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware.infostore.media;

import java.io.InputStream;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;

/**
 * {@link MediaMetadataExtractor} - Extracts possible metadata from a certain media resources (images, videos, etc.).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface MediaMetadataExtractor {

    /**
     * Checks if this extractor accepts specified document for being processed.
     *
     * @param document The document to check
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isApplicable(DocumentMetadata document) throws OXException;

    /**
     * Checks the effort of extracting media metadata from given input stream.
     *
     * @param in The input stream to examine
     * @param document The document associated with given stream
     * @param optArguments Optional (mutable) additional arguments
     * @return The effort
     * @throws OXException If effort estimation fails
     */
    Effort estimateEffort(InputStream in, DocumentMetadata document, Map<String, Object> optArguments) throws OXException;

    /**
     * Extracts media metadata from given stream and applies them to specified document.
     *
     * @param optStream The optional initial stream
     * @param provider The input stream provider to extract from
     * @param document The document to apply to
     * @param optArguments Optional (immutable) additional arguments
     * @return The extractor result
     * @throws OXException If extraction fails
     */
    ExtractorResult extractAndApplyMediaMetadata(InputStream optStream, InputStreamProvider provider, DocumentMetadata document, Map<String, Object> optArguments) throws OXException;
}
