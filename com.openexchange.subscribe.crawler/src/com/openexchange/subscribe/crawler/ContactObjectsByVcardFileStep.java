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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.ContactSanitizer;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * {@link ContactObjectsByVcardFileStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactObjectsByVcardFileStep extends AbstractStep<Contact[], Page> {

    private static final ContactSanitizer SANITIZER = new ContactSanitizer();

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactObjectsByVcardFileStep.class));

    private List<String> unwantedLines = new ArrayList<String>();

    public ContactObjectsByVcardFileStep() {

    }

    public ContactObjectsByVcardFileStep(final String description, final List<String> unwantedLines) {
        this.description = description;
        this.unwantedLines = unwantedLines;
    }

    @Override
    public void execute(final WebClient webClient) throws OXException {
        final Vector<Contact> contactObjects = new Vector<Contact>();
        final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);

        String pageString = input.getWebResponse().getContentAsString();
        LOG.debug("The page to scan for vCards is : " + pageString);

        while (pageString.contains("BEGIN:VCARD")) {
            final int beginIndex = pageString.indexOf("BEGIN:VCARD");
            final int endIndex = pageString.indexOf("END:VCARD") + 9;
            String vcardString = pageString.substring(beginIndex, endIndex) + "\n";
            vcardString = deleteUnwantedLines(vcardString, unwantedLines);
            pageString = pageString.substring(endIndex);
            try {
                final byte[] vcard = vcardString.getBytes(com.openexchange.java.Charsets.UTF_8);
                final VersitDefinition def = Versit.getDefinition("text/x-vcard");
                VersitDefinition.Reader versitReader;
                versitReader = def.getReader(new ByteArrayInputStream(vcard), "UTF-8");
                final VersitObject versitObject = def.parse(versitReader);
                final Contact contactObject = oxContainerConverter.convertContact(versitObject);
                SANITIZER.sanitize(contactObject);
                contactObjects.add(contactObject);
            } catch (final VersitException e) {
                LOG.error(e);
            } catch (final ConverterException e) {
                LOG.error(e);
            } catch (final IOException e) {
                LOG.error(e);
            }

        }

        executedSuccessfully = true;

        output = new Contact[contactObjects.size()];
        for (int i = 0; i < output.length && i < contactObjects.size(); i++) {
            output[i] = contactObjects.get(i);
        }

    }

    private String deleteUnwantedLines(String vcardString, final List<String> unwantedLines) {
        for (final String regexToReplace : unwantedLines) {
            vcardString = vcardString.replaceAll(regexToReplace, "");
        }
        return vcardString;
    }

    public List<String> getUnwantedLines() {
        return unwantedLines;
    }

    public void setUnwantedLines(final List<String> unwantedLines) {
        this.unwantedLines = unwantedLines;
    }

}
