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

package com.openexchange.groupware.contact.datasource;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_FOLDERID;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_ID;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.internal.VCardUtil;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link ContactDataSource} - A data source for contacts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContactDataSource implements DataSource {

    private static final Class<?>[] TYPES = { InputStream.class, byte[].class };
    private static final String[] ARGS = { "com.openexchange.groupware.contact.pairs" };

    /**
     * Initializes a new {@link ContactDataSource}
     */
    public ContactDataSource() {
        super();
    }

    @Override
    public <D> Data<D> getData(final Class<? extends D> type, final DataArguments dataArguments, final Session session) throws OXException {
        if (!InputStream.class.equals(type) && !byte[].class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }
        /*
         * Check arguments
         */
        final int[] folderIds;
        final int[] objectIds;
        final int len;
        {
            final JSONArray pairsArray;
            try {
                pairsArray = new JSONArray(dataArguments.get(ARGS[0]));
                len = pairsArray.length();
                if (len == 0) {
                    throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[0]);
                }
                folderIds = new int[len];
                objectIds = new int[len];
                for (int i = 0; i < len; i++) {
                    final JSONObject pairObject = pairsArray.getJSONObject(i);
                    folderIds[i] = pairObject.getInt(PARAMETER_FOLDERID);
                    objectIds[i] = pairObject.getInt(PARAMETER_ID);
                }
            } catch (final JSONException e) {
                throw DataExceptionCodes.INVALID_ARGUMENT.create(e, ARGS[0], dataArguments.get(ARGS[0]));
            }
        }
        /*
         * Get contact
         */
        final Contact[] contacts = new Contact[len];
        {
        	final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class, true);
            for (int i = 0; i < len; i++) {
            	contacts[i] = contactService.getContact(session, Integer.toString(folderIds[i]), Integer.toString(objectIds[i]));
            }
        }
        /*
         * Create necessary objects
         */
        final ByteArrayOutputStream sink = new UnsynchronizedByteArrayOutputStream(len << 12);
        for (final Contact contact : contacts) {
            VCardUtil.exportContact(contact, session, sink);
        }
        /*
         * Return data
         */
        final DataProperties properties = new DataProperties();
        properties.put(DataProperties.PROPERTY_CHARSET, "UTF-8");
        properties.put(DataProperties.PROPERTY_VERSION, "3.0");
        properties.put(DataProperties.PROPERTY_CONTENT_TYPE, "text/vcard");
        final String displayName = contacts.length == 1 ? contacts[0].getDisplayName() : null;
        properties.put(DataProperties.PROPERTY_NAME, displayName == null ? "vcard.vcf" : new StringBuilder(
            displayName.replaceAll(" +", "_")).append(".vcf").toString());
        final byte[] vcardBytes = sink.toByteArray();
        properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(vcardBytes.length));
        return new SimpleData<D>(
            (D) (InputStream.class.equals(type) ? new UnsynchronizedByteArrayInputStream(vcardBytes) : vcardBytes),
            properties);
    }

    @Override
    public String[] getRequiredArguments() {
        return ARGS;
    }

    @Override
    public Class<?>[] getTypes() {
        return TYPES;
    }

}
