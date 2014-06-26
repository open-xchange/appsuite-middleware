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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TimeZone;
import junit.framework.TestCase;
import org.ho.yaml.Yaml;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.crawler.internal.ContactSanitizer;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * {@link MultipleVcardsTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class MultipleVcardsTest extends TestCase {

    private static final ContactSanitizer SANITIZER = new ContactSanitizer();

    public void testMultipleVcards() {
        final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);

        String pageString = "";
        try {
            // Insert valid filename here
            pageString = (String) Yaml.load(new File("local_only/vcard-test.yml"));
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        while (pageString.contains("BEGIN:VCARD")) {
            int beginIndex = pageString.indexOf("BEGIN:VCARD");
            int endIndex = pageString.indexOf("END:VCARD") + 9;
            String vcardString = pageString.substring(beginIndex, endIndex) + "\n";
            // delete lines "REV" and "BDAY" as they have an incompatible format
            vcardString = vcardString.replaceAll("REV[^\\n]*", "");
            vcardString = vcardString.replaceAll("BDAY[^\\n]*", "");
            System.out.println(vcardString);
            pageString = pageString.substring(endIndex);

            try {
                byte[] vcard = vcardString.getBytes(com.openexchange.java.Charsets.UTF_8);
                VersitDefinition def = Versit.getDefinition("text/x-vcard");
                VersitDefinition.Reader versitReader;
                versitReader = def.getReader(new ByteArrayInputStream(vcard), "UTF-8");
                VersitObject versitObject = def.parse(versitReader);
                Contact contactObject = oxContainerConverter.convertContact(versitObject);
                SANITIZER.sanitize(contactObject);
                System.out.println("***** " + contactObject);
            } catch (final VersitException e) {
                e.printStackTrace();
            } catch (ConverterException e) {
                System.out.println(e);
            } catch (IOException e) {
                System.out.println(e);
            }

        }
    }

}
