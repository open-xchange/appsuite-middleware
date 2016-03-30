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

package com.openexchange.subscribe.microformats.objectparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import org.htmlparser.util.ParserException;
import org.microformats.hCard.HCard;
import org.microformats.hCard.HCardParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactMerger;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.microformats.parser.ObjectParser;
import com.openexchange.subscribe.microformats.transformers.MapToContactObjectTransformer;


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class OXHCardParser implements ObjectParser<Contact>{

    @Override
    public List<Contact> parse(Reader html) throws OXException {
        BufferedReader bufferedReader = new BufferedReader(html);
        String line;
        StringBuffer buffy = new StringBuffer();
        try {
            while((line = bufferedReader.readLine()) != null){
                buffy.append(line);
            }
        } catch (IOException e) {
            //throw new OXException(SubscriptionErrorMessage.ParsingError, e);
        }
        return parse(buffy.toString());
    }


    public List<Contact> parse(String html){
        try {
            List<HCard> parseMany = org.microformats.hCard.HCardParser.parseMany(html);
            List<Contact> contactsFromHCard = new HCardToContactTransformer().transform(parseMany);
            List<Contact> contactsFromOXMF = new MapToContactObjectTransformer().transform(HCardParser.getOXMFData());
            return new ContactMerger(false).merge(contactsFromHCard, contactsFromOXMF);
        } catch (ParserException e) {
            e.printStackTrace();
            return null; //FIXME
        }
    }

    public List<Map<String, String>> getOXMFData() {
        return org.microformats.hCard.HCardParser.getOXMFData();
    }
}
