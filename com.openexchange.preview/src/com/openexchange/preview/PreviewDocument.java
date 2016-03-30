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
import java.util.List;
import java.util.Map;

/**
 * {@link PreviewDocument}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface PreviewDocument {

    /**
     * Gets the document's meta data.
     * <p>
     * Typical meta data would be:
     * <ul>
     * <li><code>"title"</code></li>
     * <li><code>"subject"</code></li>
     * <li><code>"resourcename"</code></li>
     * <li><code>"content-type"</code></li>
     * <li><code>"author"</code></li>
     * <li>...</li>
     * </ul>
     *
     * @return The meta data as a {@link Map}
     */
    Map<String, String> getMetaData();

    /**
     * Checks if this preview document provides content via {@link #getContent()} method.
     *
     * @return <code>true</code> if content is provided; otherwise <code>false</code>
     */
    boolean hasContent();

    /**
     * Gets the document's content in its output format.
     *
     * @return The content (or <code>null</code> if output format does not imply a content; e.g. {@link PreviewOutput#METADATA})
     */
    List<String> getContent();

    /**
     * Gets the preview image (thumbnail).
     *
     * @return The input stream for the image or <code>null</code> if the image is not available.
     */
    InputStream getThumbnail();


    /**
     * Determines if the original document contains more content than this preview document provides.
     *
     * @return true, if more content is available, false if not and null if the document does not know anything about more content.
     */
    Boolean isMoreAvailable();

}
