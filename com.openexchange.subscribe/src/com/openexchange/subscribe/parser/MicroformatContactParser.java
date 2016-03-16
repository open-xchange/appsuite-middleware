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

package com.openexchange.subscribe.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionHandler;
import com.openexchange.subscribe.TargetFolderSession;


public class MicroformatContactParser extends ContactHandler implements SubscriptionHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MicroformatContactParser.class);

    protected Collection<Contact> contacts;
    protected SubscribeService service;

    public MicroformatContactParser(){
        super();
    }

    public MicroformatContactParser(final SubscribeService service){
        this.service = service;
    }

    /**
     * Read the site of a subscription and return its content as a string
     * @param subscription
     * @return
     * @throws IOException
     */
    protected String readSubscription(final Subscription subscription) throws IOException{
        BufferedReader buffy = null;
        final StringBuilder bob = new StringBuilder();

        try {
            final URL url = new URL(""); //new URL(subscription.getUrl());
            final URLConnection connection = url.openConnection();
            buffy = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
            String line = buffy.readLine();
            while (line != null){
                bob.append (line);
                bob.append ('\n');
                line = buffy.readLine();
            }
        } finally {
            Streams.close(buffy);
        }
        return bob.toString();
    }

    @Override
    public void handleSubscription(final Subscription subscription) throws OXException{
        try {
            final String website = readSubscription(subscription);

            parse( website );

            storeContacts(new TargetFolderSession(subscription), subscription.getFolderIdAsInt(), this.getContacts());

        } catch (final IOException e) {
            LOG.error("", e);
        } catch (final OXException e) {
            LOG.error("", e);
        }

    }

    public void parse(final String text) {
        XMLReader xmlReader = null;
        try {
            final AbstractMicroformatSAXHandler handler = new MicroformatContactSAXHandler();
            xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler( handler );
            xmlReader.setErrorHandler( handler );
            xmlReader.parse( new InputSource( new ByteArrayInputStream(text.getBytes()) ) );
            contacts = handler.getObjects();
        } catch (final SAXException e) {
            LOG.error("", e);
        } catch (final IOException e) {
            LOG.error("", e);
        }
    }

    public Collection<Contact> getContacts() {
        return contacts;
    }

}
