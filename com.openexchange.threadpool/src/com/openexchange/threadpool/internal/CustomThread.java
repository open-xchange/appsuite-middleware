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

package com.openexchange.threadpool.internal;

import com.openexchange.marker.OXThreadMarker;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link CustomThread} - Enhances {@link Thread} class by a setter/getter method for a thread's original name.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomThread extends Thread implements ThreadRenamer, OXThreadMarker {

    private volatile String originalName;
    private volatile String appendix;
    private volatile boolean changed;
    private volatile boolean httpProcessing;

    /**
     * Initializes a new {@link CustomThread}.
     *
     * @param target The object whose run method is called
     * @param name The name of the new thread which is also used as original name
     */
    public CustomThread(final Runnable target, final String name) {
        super(target, name);
        applyName(name);
    }

    private void applyName(final String name) {
        originalName = name;
        int pos = originalName.indexOf('-');
        appendix = pos > 0 ? name.substring(pos) : null;
    }

    @Override
    public boolean isHttpRequestProcessing() {
        return httpProcessing;
    }

    @Override
    public void setHttpRequestProcessing(boolean httpProcessing) {
        this.httpProcessing = httpProcessing;
    }

    /**
     * Gets the original name.
     *
     * @return The original name
     */
    public String getOriginalName() {
        return originalName;
    }

    @Override
    public void restoreName() {
        if (!changed) {
            return;
        }
        setName(originalName);
        changed = false;
    }

    @Override
    public void rename(final String newName) {
        setName(newName);
        changed = true;
    }

    @Override
    public void renamePrefix(final String newPrefix) {
        if (null == appendix) {
            setName(newPrefix);
        } else {
            setName(new StringBuilder(16).append(newPrefix).append(appendix).toString());
        }
        changed = true;
    }

}
