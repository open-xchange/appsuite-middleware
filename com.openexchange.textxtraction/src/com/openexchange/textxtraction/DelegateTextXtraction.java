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

package com.openexchange.textxtraction;

import java.io.InputStream;

/**
 * {@link DelegateTextXtraction} - A delegate text extract service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface DelegateTextXtraction extends TextXtractService {

    /**
     * @return
     * <p>Must be <code>true</code> if this delegate makes {@link InputStream}s unusable for further processing.
     * This is the case if the delegate does not reset the {@link InputStream} properly or even closes it.
     * In this case the {@link InputStream} must be written into a temporary file before it can be processed.
     * This is needed to enable text extraction by the fallback service if a destructive delegate fails.</p>
     *
     * <p><b>Attention:</b><br>
     * A destructive delegate makes text extraction much more expensive. Avoid destructive behavior if possible!</p>
     */
    boolean isDestructive();
}
