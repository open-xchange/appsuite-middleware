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

import java.util.HashMap;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.customizer.folder.FolderReadCustomizer;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.sim.SimBuilder;
import junit.framework.TestCase;


/**
 * {@link MultiReadCustomizerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class MultiReadCustomizerTest extends TestCase {
    
    public void testSetMultiplex() throws AbstractOXException {
        SimBuilder simBuilder1 = new SimBuilder();
        SimBuilder simBuilder2 = new SimBuilder();
        
        int[] columns = new int[]{1,2,3};
        SimContext ctx = new SimContext(1);
        ParamContainer container = ParamContainer.getInstance(new HashMap<String, String>(), EnumComponent.FOLDER);
        Response resp = new Response();
        
        simBuilder1.expectCall("setColumns", columns);
        simBuilder2.expectCall("setColumns", columns);
        
        simBuilder1.expectCall("setContext", ctx);
        simBuilder2.expectCall("setContext", ctx);
        
        simBuilder1.expectCall("setParameters", container);
        simBuilder2.expectCall("setParameters", container);
        
        simBuilder1.expectCall("customizeResponse", resp);
        simBuilder2.expectCall("customizeResponse", resp);
        
        MultiReadCustomizer customizer = new MultiGetCustomizer();
        customizer.addCustomizer(simBuilder1.getSim(FolderReadCustomizer.class));
        customizer.addCustomizer(simBuilder2.getSim(FolderReadCustomizer.class));
        
        customizer.setColumns(columns);
        customizer.setContext(ctx);
        customizer.setParameters(container);
        customizer.customizeResponse(resp);
        
        simBuilder1.assertAllWereCalled();
        simBuilder2.assertAllWereCalled();
        
    }
    
}
