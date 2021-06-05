/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
