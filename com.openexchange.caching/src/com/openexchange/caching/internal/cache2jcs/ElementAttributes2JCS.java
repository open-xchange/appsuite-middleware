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

package com.openexchange.caching.internal.cache2jcs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.caching.internal.jcs2cache.JCSElementEventHandlerDelegator;

/**
 * {@link ElementAttributes2JCS} - Delegates its method invocations to an instance of {@link IElementAttributes}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ElementAttributes2JCS implements ElementAttributes {

    private static final long serialVersionUID = 8536168955346943272L;

    private final IElementAttributes attributes;

    /**
     * Initializes a new {@link ElementAttributes2JCS}
     */
    public ElementAttributes2JCS(final IElementAttributes attributes) {
        super();
        this.attributes = attributes;
    }

    @Override
    public void addElementEventHandler(final ElementEventHandler eventHandler) {
        attributes.addElementEventHandler(new JCSElementEventHandlerDelegator(eventHandler));
    }

    @Override
    public void addElementEventHandlers(final ArrayList<ElementEventHandler> eventHandlers) {
        for (final Object object : eventHandlers) {
            attributes.addElementEventHandler(new JCSElementEventHandlerDelegator((ElementEventHandler) object));
        }
    }

    @Override
    public ElementAttributes copy() {
        return new ElementAttributes2JCS(attributes.copy());
    }

    @Override
    public long getCreateTime() {
        return attributes.getCreateTime();
    }

    @Override
    public Queue<ElementEventHandler> getElementEventHandlers() {
        final Queue<IElementEventHandler> l = attributes.getElementEventHandlers();
        final Queue<ElementEventHandler> retval;
        if (l == null) {
            retval = new LinkedList<ElementEventHandler>();
        } else {
            retval = new LinkedList<ElementEventHandler>();
            for (final Object object : l) {
                retval.add(new ElementEventHandler2JCS((IElementEventHandler) object));
            }
        }
        return retval;
    }

    @Override
    public long getIdleTime() {
        return attributes.getIdleTime();
    }

    @Override
    public boolean getIsEternal() {
        return attributes.getIsEternal();
    }

    @Override
    public boolean getIsLateral() {
        return attributes.getIsLateral();
    }

    @Override
    public boolean getIsRemote() {
        return attributes.getIsRemote();
    }

    @Override
    public boolean getIsSpool() {
        return attributes.getIsSpool();
    }

    @Override
    public long getLastAccessTime() {
        return attributes.getLastAccessTime();
    }

    @Override
    public long getMaxLifeSeconds() {
        return attributes.getMaxLifeSeconds();
    }

    @Override
    public int getSize() {
        return attributes.getSize();
    }

    @Override
    public long getTimeToLiveSeconds() {
        return attributes.getTimeToLiveSeconds();
    }

    @Override
    public void setIdleTime(final long idle) {
        attributes.setIdleTime(idle);
    }

    @Override
    public void setIsEternal(final boolean val) {
        attributes.setIsEternal(val);
    }

    @Override
    public void setIsLateral(final boolean val) {
        attributes.setIsLateral(val);
    }

    @Override
    public void setIsRemote(final boolean val) {
        attributes.setIsRemote(val);
    }

    @Override
    public void setIsSpool(final boolean val) {
        attributes.setIsSpool(val);
    }

    @Override
    public void setLastAccessTimeNow() {
        attributes.setLastAccessTimeNow();
    }

    @Override
    public void setMaxLifeSeconds(final long mls) {
        attributes.setMaxLifeSeconds(mls);
    }

    @Override
    public void setSize(final int size) {
        attributes.setSize(size);
    }

}
