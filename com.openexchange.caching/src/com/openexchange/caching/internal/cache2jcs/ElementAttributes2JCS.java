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

package com.openexchange.caching.internal.cache2jcs;

import java.util.ArrayList;
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
    public ArrayList<ElementEventHandler> getElementEventHandlers() {
        final ArrayList<?> l = attributes.getElementEventHandlers();
        final ArrayList<ElementEventHandler> retval;
        if (l == null) {
            retval = new ArrayList<ElementEventHandler>(0);
        } else {
            retval = new ArrayList<ElementEventHandler>(l.size());
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
