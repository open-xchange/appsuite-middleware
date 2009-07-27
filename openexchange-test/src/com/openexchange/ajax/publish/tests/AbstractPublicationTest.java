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

package com.openexchange.ajax.publish.tests;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;

/**
 * {@link AbstractPublicationTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractPublicationTest extends 
AbstractPubSubTest {

    public AbstractPublicationTest(String name) {
        super(name);
    }

    protected Publication generatePublication(String type, String folder){
        SimPublicationTargetDiscoveryService discovery = new SimPublicationTargetDiscoveryService();
        return generatePublication(type, folder, discovery);
    }
    
    protected Publication generatePublication(String type, String folder, SimPublicationTargetDiscoveryService discovery){
        //create publication
        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("siteName", "Site Name")).add(FormElement.checkbox("protected", "Protected"));
        
        PublicationTarget target = new PublicationTarget();
        target.setFormDescription(form);
        target.setId("com.openexchange.publish.microformats."+type+".online");
 
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("siteName", "publication");
        config.put("protected", Boolean.valueOf(true) );
        
        discovery.addTarget(target);
        
        Publication pub = new Publication();
        pub.setModule(type);
        pub.setEntityId(folder);
        pub.setTarget(target);
        pub.setConfiguration(config);
        return pub;
    }
}
