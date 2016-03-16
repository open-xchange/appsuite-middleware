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

package com.openexchange.subscribe.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import com.gargoylesoftware.htmlunit.WebClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.crawler.internal.AbstractStep;


/**
 * {@link RemoveDuplicateContactsStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class RemoveDuplicateContactsStep extends AbstractStep<Contact[], Contact[]> {

    private final HashMap<String, Contact> map = new HashMap<String, Contact>();

    public RemoveDuplicateContactsStep(){

    }

    /* (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.AbstractStep#execute(com.gargoylesoftware.htmlunit.WebClient)
     */
    @Override
    public void execute(WebClient webClient) throws OXException {
        ArrayList<Contact> outputList = new ArrayList<Contact>();
        for (Contact contact : input){
            String hash = getUniqueHash(contact);
            if (!map.containsKey(hash)){
                outputList.add(contact);
                map.put(hash, contact);
            }
        }

        output = outputList.toArray(new Contact[outputList.size()]);

        executedSuccessfully = true;
    }

    private String getUniqueHash(Contact contact){
        String string = "";
        if (contact.getGivenName() != null){
            string += contact.getGivenName();
        }
        if (contact.getSurName() != null){
            string += contact.getSurName();
        }
        if (contact.getDisplayName() != null){
            string += contact.getDisplayName();
        }
        if (contact.getBirthday() != null){
            string += contact.getBirthday().toString();
        }
        if (contact.getEmail1() != null){
            string += contact.getEmail1();
        }
        if (contact.getCellularTelephone1() != null){
            string += contact.getCellularTelephone1();
        }
        return string;
    }
}
