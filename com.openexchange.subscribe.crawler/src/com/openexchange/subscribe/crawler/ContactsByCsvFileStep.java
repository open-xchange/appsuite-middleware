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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.Mappings;

/**
 * {@link ContactsByCsvFileStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactsByCsvFileStep extends AbstractStep<Contact[], TextPage> {

    private boolean ignoreFirstLine;

    private Map<Integer, String> fieldMapping;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactsByCsvFileStep.class);

    public ContactsByCsvFileStep() {
        super();
    }

    public ContactsByCsvFileStep(final String description, final boolean ignoreFirstLine, final Map<Integer, String> fieldMapping) {
        this.description = description;
        this.ignoreFirstLine = ignoreFirstLine;
        this.fieldMapping = fieldMapping;
    }

    @Override
    public void execute(final WebClient webClient) {
        final Vector<Contact> contactObjects = new Vector<Contact>();
        if (input != null) {
            String page = input.getWebResponse().getContentAsString();
            int counter = 0;
            while (page.contains("\n")) {
                final int endOfLine = page.indexOf("\n");
                Contact contact = null;
                if (!(ignoreFirstLine && counter == 0)) {
                    final HashMap<String, String> resultMap = new HashMap<String, String>();
                    final String line = page.substring(0, endOfLine);
                    final String[] fields = line.split("\",\"");

                    int fieldCounter = 0;
                    for (String field : fields) {
                        field = field.replaceAll("\"", "");
                        // if there is a mapping for this value in the cvs-file
                        if (fieldMapping.containsKey(fieldCounter) && !field.equals("")) {
                            resultMap.put(fieldMapping.get(fieldCounter), field);
                        }

                        fieldCounter++;
                    }

                    try {
                        contact = Mappings.translateMapToContact(resultMap);
                    } catch (final OXException e) {
                        LOG.error("{} for Context : {}, User : {}, Folder : {}.", e.getMessage(), workflow.getSubscription().getContext().getContextId(), workflow.getSubscription().getUserId(), workflow.getSubscription().getFolderId());

                        exception = e;
                    }

                }

                page = page.substring(endOfLine + 1);
                counter++;
                if (contact != null) {
                    ContactUtil.generateDisplayName(contact);
                    contactObjects.add(contact);
                }

            }
        }
        executedSuccessfully = true;
        output = new Contact[contactObjects.size()];
        for (int i = 0; i < output.length && i < contactObjects.size(); i++) {
            output[i] = contactObjects.get(i);
        }
    }

    public boolean getIgnoreFirstLine() {
        return ignoreFirstLine;
    }

    public void setIgnoreFirstLine(final boolean ignoreFirstLine) {
        this.ignoreFirstLine = ignoreFirstLine;
    }

    public Map<Integer, String> getFields() {
        return fieldMapping;
    }

    public void setFields(final Map<Integer, String> fields) {
        fieldMapping = fields;
    }

}
