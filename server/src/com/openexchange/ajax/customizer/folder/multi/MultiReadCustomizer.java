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

package com.openexchange.ajax.customizer.folder.multi;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.customizer.CustomizerFactory;
import com.openexchange.ajax.customizer.folder.FolderGetCustomizer;
import com.openexchange.ajax.customizer.folder.FolderReadCustomizer;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MultiReadCustomizer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class MultiReadCustomizer<T extends FolderReadCustomizer> extends MultiCustomizer<T> implements FolderReadCustomizer {

    public void customizeResponse(Response response) {
        for(FolderReadCustomizer customizer : customizers) {
            customizer.customizeResponse(response);
        }
    }

    public void setColumns(int[] columns) {
        for(FolderReadCustomizer customizer : customizers) {
            customizer.setColumns(columns);
        }
    }

    public void setContext(Context ctx) {
        for(FolderReadCustomizer customizer : customizers) {
            customizer.setContext(ctx);
        }
    }

    public void setParameters(ParamContainer params) throws AbstractOXException {
        for(FolderReadCustomizer customizer : customizers) {
            customizer.setParameters(params);
        }
    }
    
    public T copyAsNeeded(ServerSession session) {
        MultiReadCustomizer<T> copy = newInstance();
        for(T customizer : customizers) {
            if(CustomizerFactory.class.isInstance(customizer)) {
                copy.addCustomizer((T) ((CustomizerFactory) customizer).newInstance(session));
            } else {
                copy.addCustomizer(customizer);
            }
        }
        return (T) copy;
    }

    protected abstract MultiReadCustomizer<T> newInstance();

}
