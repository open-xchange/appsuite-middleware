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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.attach.impl;

import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentException;
import com.openexchange.groupware.attach.AttachmentExceptionFactory;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.Classes;
import com.openexchange.tools.service.ServicePriorityConflictException;
import com.openexchange.tools.service.SpecificServiceChooser;

/**
 * {@link OverridableAttachmentListener}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@OXExceptionSource(
    classId = Classes.COM_OPENEXCHANGE_GROUPWARE_ATTACH_IMPL_OVERRIDABLEATTACHMENTLISTENER,
    component = EnumComponent.ATTACHMENT
)
public class OverridableAttachmentListener implements AttachmentListener {
    private static final AttachmentExceptionFactory EXCEPTIONS = new AttachmentExceptionFactory(OverridableAttachmentListener.class);
    private SpecificServiceChooser<AttachmentListener> chooser;

    public OverridableAttachmentListener(SpecificServiceChooser<AttachmentListener> chooser) {
        this.chooser = chooser;
    }

    public long attached(AttachmentEvent e) throws Exception {
        return getDelegate(e).attached(e);
    }

    public long detached(AttachmentEvent e) throws Exception {
        return getDelegate(e).detached(e);
    }

    @OXThrows(category=Category.SETUP_ERROR, desc="", exceptionId=0, msg="Conflicting services registered for context %i and folder %i")
    private AttachmentListener getDelegate(AttachmentEvent e) throws AttachmentException {
        int contextId = e.getContext().getContextId();
        int folderId = e.getFolderId();
        try {
            return chooser.choose(contextId, folderId);
        } catch (ServicePriorityConflictException e1) {
            throw EXCEPTIONS.create(0, contextId, folderId);
        }
        
    }

}
