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

package com.openexchange.preview;

import java.io.InputStream;
import com.openexchange.conversion.Data;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link PreviewService} - The preview service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface PreviewService {

    /**
     * Detects the MIME type of passed input stream's data.
     *
     * @param inputStream The input stream providing the data
     * @return The detected MIME type
     * @throws OXException If an error occurs
     */
    String detectDocumentType(InputStream inputStream) throws OXException;

    /**
     * Gets the preview document for specified argument and output format.
     *
     * @param arg The argument either denotes an URL or a file
     * @param output The output format
     * @param session The session
     * @param pages The number of pages to be generated, if possible. If not set, this argument is ignored. -1 for "all pages"
     * @return The preview document with its content set according to given output format
     * @throws OXException If preview document cannot be generated
     */
    PreviewDocument getPreviewFor(String arg, PreviewOutput output, Session session, int pages) throws OXException;

    /**
     * Gets the preview document for specified s data and output format.
     *
     * @param documentData The data
     * @param output The output format
     * @param session The session
     * @param pages The number of pages to be generated, if possible. If not set, this argument is ignored. -1 for "all pages"
     * @return The preview document with its content set according to given output format
     * @throws OXException If preview document cannot be generated
     */
    PreviewDocument getPreviewFor(Data<InputStream> documentData, PreviewOutput output, Session session, int pages) throws OXException;

}
