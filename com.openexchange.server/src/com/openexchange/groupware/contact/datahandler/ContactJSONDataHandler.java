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

package com.openexchange.groupware.contact.datahandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.openexchange.tools.versit.filetokenizer.VCardFileToken;
import com.openexchange.tools.versit.filetokenizer.VCardTokenizer;

/**
 * {@link ContactJSONDataHandler} - A data handler for storing VCards into a contact folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContactJSONDataHandler implements DataHandler {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ContactJSONDataHandler.class));

    private static final String[] ARGS = {};

    private static final Class<?>[] TYPES = { InputStream.class };

    /**
     * Initializes a new {@link ContactJSONDataHandler}
     */
    public ContactJSONDataHandler() {
        super();
    }

    @Override
    public String[] getRequiredArguments() {
        return ARGS;
    }

    @Override
    public Class<?>[] getTypes() {
        return TYPES;
    }

    private static final ContactField[] CONTACT_FIELDS_FOLDER_ID = new ContactField[] { ContactField.FOLDER_ID };

    @Override
    public Object processData(final Data<?> data, final DataArguments dataArguments, final Session session) throws OXException {
        final Context ctx = ContextStorage.getStorageContext(session);
        /*
         * Parse input stream
         */
        final OXContainerConverter converter = new OXContainerConverter(session, ctx);
        final InputStream inputStream = (InputStream) data.getData();
        try {
            final DataProperties dataProperties = data.getDataProperties();
            final VCardTokenizer tokenizer = new VCardTokenizer(inputStream);
            final List<VCardFileToken> chunks = tokenizer.split();
            if (chunks.isEmpty()) {
                LOG.error("VCard tokenizer returned zero results");
                return new JSONArray();
            }
            final JSONArray jsonArray = new JSONArray();
			final ContactWriter writer = new ContactWriter(TimeZone
					.getTimeZone(UserStorage.getStorageUser(
							session.getUserId(), ctx).getTimeZone()));
            for (final VCardFileToken chunk : chunks) {
                final VersitDefinition def = chunk.getVersitDefinition();
                if (def == null) {
                    /*
                     * No appropriate definition for current part of the VCard stream
                     */
                    LOG.error("Could not recognize format of the following VCard data:\n" + Arrays.toString(chunk.getContent()));
                } else {
                    final VersitDefinition.Reader versitReader = def.getReader(
                        new UnsynchronizedByteArrayInputStream(chunk.getContent()),
                        dataProperties.get(DataProperties.PROPERTY_CHARSET));
                    /*
                     * Parse VCard from reader
                     */
                    final VersitObject versitObject = def.parse(versitReader);
                    /*
                     * Convert to a contact object
                     */
                    converter.setAddDisplayName4DList(true);
                    final Contact contact = converter.convertContact(versitObject);
                    /*
                     * Check folder/object identifier
                     */
                    {
                        final String folderId = dataProperties.get(DataProperties.PROPERTY_FOLDER_ID);
                        if (null != folderId) {
                            try {
                                contact.setParentFolderID(Integer.parseInt(folderId));
                            } catch (final NumberFormatException e) {
                                // Ignore
                            }
                        }
                        final String contactId = dataProperties.get(DataProperties.PROPERTY_ID);
                        if (null != contactId) {
                            try {
                                contact.setObjectID(Integer.parseInt(contactId));
                            } catch (final NumberFormatException e) {
                                // Ignore
                            }
                        }
                    }
					/*
					 * Store contact object in JSON array
					 */
					final JSONObject jsonContact = new JSONObject(32);
					writer.writeContact(contact, jsonContact, session);
					/*
					 * Check for image
					 */
					{
					    final byte[] image = contact.getImage1();
					    if (null != image) {
					        jsonContact.put(ContactField.IMAGE1.getAjaxName(), Base64.encodeBase64String(image));
					        String mimeType = contact.getImageContentType();
					        if (null == mimeType) {
                                mimeType = "image/jpeg";
                            }
					        jsonContact.put(ContactField.IMAGE1_CONTENT_TYPE.getAjaxName(), mimeType);
                        }
					}
					jsonArray.put(jsonContact);
                }
            }
            /*
             * Return JSON response
             */
            return jsonArray;
        } catch (final ConverterException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            converter.close();
            Streams.close(inputStream);
        }
    }
}
