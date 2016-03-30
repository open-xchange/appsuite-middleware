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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.ContactSanitizer;
import com.openexchange.subscribe.helpers.HTTPToolkit;

/**
 * This step takes HtmlPages that each contain contact information in vcard-format and converts them to ContactObjects for OX
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactObjectsByHTMLAnchorsToVCardsStep extends AbstractStep<Contact[], List<HtmlAnchor>> {

    private static final ContactSanitizer SANITIZER = new ContactSanitizer();

    private String vcardUrl, pictureUrl;

    public ContactObjectsByHTMLAnchorsToVCardsStep(final String description, final String vcardUrl, final String pictureUrl) {
        this.description = description;
        this.vcardUrl = vcardUrl;
        this.pictureUrl = pictureUrl;
    }

    public ContactObjectsByHTMLAnchorsToVCardsStep() {

    }

    @Override
    public void execute(final WebClient webClient) {
        final Vector<Contact> contactObjects = new Vector<Contact>();
        // int counter=0;
        for (final HtmlAnchor anchor : input) {
            try {
                final HtmlPage page = anchor.click();
                Contact contact = new Contact();
                TextPage vcardPage = null;
                String imageUrl = "";

                for (final HtmlAnchor link : page.getAnchors()) {
                    // if there is a vcard linked
                    if (link.getHrefAttribute().startsWith(vcardUrl)) {
                        vcardPage = link.click();
                    }
                }

                // if there is a contact picture in an <img>-tag get its Url
                if (page.getWebResponse().getContentAsString().contains(pictureUrl)) {
                    final int startIndex = page.getWebResponse().getContentAsString().indexOf(pictureUrl);
                    final String substring = page.getWebResponse().getContentAsString().substring(startIndex);
                    imageUrl = substring.substring(0, substring.indexOf("\""));
                }

                if (vcardPage != null) {
                    VCardService vCardService = workflow.getActivator().getVCardService();
                    VCardImport vCardImport = null;
                    InputStream contentStream = null;
                    try {
                        contentStream = vcardPage.getWebResponse().getContentAsStream();
                        vCardImport = vCardService.importVCard(contentStream, null, vCardService.createParameters());
                        contact = vCardImport.getContact();
                    } finally {
                        Streams.close(contentStream, vCardImport);
                    }
                }

                // add the image from a url to the contact
                if (!imageUrl.equals("")) {
                    HTTPToolkit.loadImageFromURL(contact, imageUrl);
                }

                SANITIZER.sanitize(contact);
                contactObjects.add(contact);

            } catch (OXException e) {
                exception = e;
            } catch (final IOException e) {
                exception = e;
            }
            executedSuccessfully = true;
        }

        output = new Contact[contactObjects.size()];
        for (int i = 0; i < output.length && i < contactObjects.size(); i++) {
            output[i] = contactObjects.get(i);
        }

    }

    public String getVcardUrl() {
        return vcardUrl;
    }

    public void setVcardUrl(final String vcardUrl) {
        this.vcardUrl = vcardUrl;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(final String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public static ContactSanitizer getSANITIZER() {
        return SANITIZER;
    }

}
