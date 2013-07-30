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

package com.openexchange.mail.json.osgi;

import java.util.concurrent.atomic.AtomicReference;
import javax.mail.internet.InternetAddress;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXResultDecorator;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.contact.datasource.UserImageDataSource;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.image.ImageLocation;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailActionFactory;
import com.openexchange.mail.json.converters.MailConverter;
import com.openexchange.mail.json.converters.MailJSONConverter;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailJSONActivator} - The activator for mail module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailJSONActivator extends AJAXModuleActivator {

    protected static final Log LOG = com.openexchange.log.Log.loggerFor(MailJSONActivator.class);

    /**
     * The {@link ServiceLookup} reference.
     */
    public static final AtomicReference<ServiceLookup> SERVICES = new AtomicReference<ServiceLookup>();

    /**
     * Initializes a new {@link MailJSONActivator}.
     */
    public MailJSONActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContactService.class, ContactStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceLookup serviceLookup = new ExceptionOnAbsenceServiceLookup(this);
        SERVICES.set(serviceLookup);
        registerModule(new MailActionFactory(serviceLookup), "mail");
        final MailConverter converter = MailConverter.getInstance();
        registerService(ResultConverter.class, converter);
        registerService(ResultConverter.class, new MailJSONConverter(converter));

        final ContactField[] fields = new ContactField[] {
            ContactField.OBJECT_ID, ContactField.INTERNAL_USERID, ContactField.FOLDER_ID, ContactField.NUMBER_OF_IMAGES };
        registerService(AJAXResultDecorator.class, new DecoratorImpl(converter, fields));
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        SERVICES.set(null);
    }

    private final class DecoratorImpl implements AJAXResultDecorator {

        private final MailConverter converter;

        private final ContactField[] fields;

        /**
         * Initializes a new {@link DecoratorImpl}.
         */
        protected DecoratorImpl(final MailConverter converter, final ContactField[] fields) {
            this.converter = converter;
            this.fields = fields;
        }

        @Override
        public String getIdentifier() {
            return "mail.senderImageUrl";
        }

        @Override
        public String getFormat() {
            return "mail";
        }

        @Override
        public void decorate(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException {
            final Object resultObject = result.getResultObject();
            if (null == resultObject) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Result object is null.");
                }
                result.setResultObject(JSONObject.NULL, "json");
                return;
            }
            final String action = requestData.getAction();
            if ("get".equals(action) && resultObject instanceof MailMessage) {
                try {
                    final MailMessage mailMessage = (MailMessage) resultObject;
                    final InternetAddress[] from = mailMessage.getFrom();
                    if (null == from || 0 == from.length) {
                        return;
                    }
                    /*
                     * discover image URL for 'from' address
                     */
                    SearchIterator<Contact> searchIterator = null;
                    String imageURL = null;
                    try {
                        searchIterator = getService(ContactService.class).searchContacts(
                            session, createContactSearchObject(from[0]), fields, new SortOptions(ContactField.FOLDER_ID, Order.ASCENDING));
                        if (null != searchIterator) {
                            while (null == imageURL && searchIterator.hasNext()) {
                                Contact contact = searchIterator.next();
                                imageURL = getImageURL(session, contact);
                            }
                        }
                    } finally {
                        if (null != searchIterator) {
                            searchIterator.close();
                        }
                    }
                    /*
                     * convert to JSON, decorate with image URL
                     */
                    converter.convert2JSON(requestData, result, session);
                    JSONArray fromImageURLs = new JSONArray();
                    if (null != imageURL) {
                        fromImageURLs.put(imageURL);
                    }
                    ((JSONObject)result.getResultObject()).put("from_image_urls", fromImageURLs);
                } catch (final JSONException e) {
                    throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
                }
            }
        }

        /**
         * Tries to generate an URL for the image of the supplied contact if available.
         *
         * @param session The server session
         * @param contact The contact to generate the image URL for
         * @return The image URL, or <code>null</code> if not available or something went wrong
         */
        private String getImageURL(ServerSession session, Contact contact) {
            if (0 < contact.getNumberOfImages() || contact.containsImage1() && null != contact.getImage1()) {
                String timestamp = null != contact.getLastModified() ? String.valueOf(contact.getLastModified().getTime()) : null;
                try {
                    if (FolderObject.SYSTEM_LDAP_FOLDER_ID == contact.getParentFolderID() && contact.containsInternalUserId()) {
                        /*
                         * prefer user contact image url
                         */
                        ImageLocation imageLocation = new ImageLocation.Builder().id(
                            String.valueOf(contact.getInternalUserId())).timestamp(timestamp).build();
                        return UserImageDataSource.getInstance().generateUrl(imageLocation, session);
                    } else {
                        /*
                         * use default contact image data source
                         */
                        ImageLocation imageLocation = new ImageLocation.Builder().folder(String.valueOf(contact.getParentFolderID()))
                            .id(String.valueOf(contact.getObjectID())).timestamp(timestamp).build();
                        return ContactImageDataSource.getInstance().generateUrl(imageLocation, session);
                    }
                } catch (OXException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Error generating contact image URL", e);
                    }
                }
            }
            return null;
        }

        private ContactSearchObject createContactSearchObject(final InternetAddress from) {
            final ContactSearchObject searchObject = new ContactSearchObject();
            // searchObject.addFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID); // Global address book
            searchObject.setOrSearch(true);
            final String address = from.getAddress();
            searchObject.setEmail1(address);
            searchObject.setEmail2(address);
            searchObject.setEmail3(address);
            return searchObject;
        }

    } // End of class DecoratorImpl

}
