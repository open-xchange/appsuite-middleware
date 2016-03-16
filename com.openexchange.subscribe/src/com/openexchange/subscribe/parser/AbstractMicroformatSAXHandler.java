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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.LinkedList;
import org.xml.sax.helpers.DefaultHandler;
import com.openexchange.java.Streams;
import com.openexchange.subscribe.Subscription;

/**
 *
 * {@link AbstractMicroformatSAXHandler}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 * @param <T> Type this handler handles, an ox data object like Task or ContactObject or CalendarDataObject
 */
public abstract class AbstractMicroformatSAXHandler<T> extends DefaultHandler {

    protected Collection<T> objects;
    protected T currentObject;
    public String currentlyReading;

    /**
     * Create an instance of the object this data is parsed into.
     * @return
     */
    public abstract T instantiate();

    public AbstractMicroformatSAXHandler() {
        super();
        objects = new LinkedList<T>();
    }

    /**
     * Takes a character array, as used by SAX, and returns a substring from beginning to end
     * @param arr
     * @param start
     * @param end
     * @return
     */
    protected String stringArray(char[] arr, int start, int end) {
        StringBuilder bob = new StringBuilder( new String(arr));
        return bob.substring(start, end);
    }

    /**
     * Read the site of a subscription and return its content as a string
     * @param subscription
     * @return
     * @throws IOException
     */
    protected String readSubscription(Subscription subscription) throws IOException{ //TODO: refactor to composite pattern
        URL url = new URL(""); //new URL(subscription.getUrl());
        BufferedReader buffy = null;
        StringBuilder bob = new StringBuilder(2048);

        try {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
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

    /**
     * This should be called each time an object is done reading
     */
    protected void newObjectEncountered() {
        if( null != currentObject ){
            objects.add( currentObject ); //clone?
        }
        currentObject = instantiate();
    }





    @Override
    public void endElement(String uri, String name, String qName) {
        currentlyReading = null;
    }

    public Collection<T> getObjects() {
        if(currentObject != null){
            objects.add(currentObject);
        }
        return objects;
    }

}
