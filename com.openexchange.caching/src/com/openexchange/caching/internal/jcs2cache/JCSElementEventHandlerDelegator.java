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

package com.openexchange.caching.internal.jcs2cache;

import java.io.Serializable;
import org.apache.jcs.engine.control.event.ElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.caching.internal.cache2jcs.ElementEvent2JCS;

/**
 * {@link JCSElementEventHandlerDelegator} - A JSC element event handler that delegates events to an instance of {@link ElementEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSElementEventHandlerDelegator implements IElementEventHandler, Serializable {

    private static final long serialVersionUID = 4363921266600402439L;

    private final ElementEventHandler handler;

    /**
     * Initializes a new {@link JCSElementEventHandlerDelegator}
     */
    public JCSElementEventHandlerDelegator(final ElementEventHandler handler) {
        super();
        this.handler = handler;
    }

    @Override
    public void handleElementEvent(final IElementEvent elemEvent) {
        switch (elemEvent.getElementEvent()) {
        case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND:
            handler.onExceededIdletimeBackground(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST:
            handler.onExceededIdletimeOnRequest(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND:
            handler.onExceededMaxlifeBackground(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST:
            handler.onExceededMaxlifeOnRequest(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE:
            handler.onSpooledDiskAvailable(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE:
            handler.onSpooledDiskNotAvailable(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_SPOOLED_NOT_ALLOWED:
            handler.onSpooledNotAllowed(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        default:
            handler.handleElementEvent(new ElementEvent2JCS((ElementEvent) elemEvent));
        }
    }

}
