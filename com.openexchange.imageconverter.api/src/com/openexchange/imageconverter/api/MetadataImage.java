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

package com.openexchange.imageconverter.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link MetadataImage}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10
 */
public class MetadataImage implements Closeable {

    /**
     * Initializes a new {@link MetadataImage}.
     */
    @SuppressWarnings("unused")
    private MetadataImage() {
        m_imageStm = null;
        m_metadata = null;
    }

    /**
     * Initializes a new {@link MetadataImage}.
     */
    /**
     * Initializes a new {@link MetadataImage}.
     * @param imageStm The image {@link InputStream} or null.
     * @param metadata The image {@link IMetadata} interface or null.
     */
    public MetadataImage(final InputStream imageStm, final IMetadata metadata) {
        super();

        m_imageStm = imageStm;
        m_metadata = metadata;
    }

    // - Closeable -------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        if (m_isCloseableOwner.get() && (null != m_imageStm)) {
            m_imageStm.close();
        }
    }

    // - API  ------------------------------------------------------------------

    /**
     * Gets the image {@link InputStream}. The ownership of the {@link InputStream} is still held by this instance.
     *
     * @return The image {@link InputStream} or null, if no such data is available.
     */
    public InputStream getImageInputStream() {
        return m_imageStm;
    }

    /**
     * Gets the image {@link InputStream}. The ownership of the returned {@link InputStream} is transferred to the caller of this method,
     *  if the given parameter is set to <code>true</code>. In this case the caller is responsible for correctly closing the returned
     *  {@link InputStream}.

     * @param transferCloseableOwnership The ownership of the returned {@link InputStream} is transferred to the caller of this method,
     *  if the given parameter is set to <code>true</code>.
     * @return The image {@link InputStream} or null, if no such data is available.
     */
    public InputStream getImageInputStream(boolean transferCloseableOwnership) {
        m_isCloseableOwner.compareAndSet(m_isCloseableOwner.get() && transferCloseableOwnership, false);
        return m_imageStm;
    }

    /**
     * Gets the {@link IMetadata} interface.
     *
     * @return The image {@link IMetadata} interface or null, if no such data is available.
     */
    public IMetadata getMetadata() {
        return m_metadata;
    }

    // - Members ---------------------------------------------------------------

    private final InputStream m_imageStm;

    private final IMetadata m_metadata;

    private AtomicBoolean m_isCloseableOwner = new AtomicBoolean(true);
}
