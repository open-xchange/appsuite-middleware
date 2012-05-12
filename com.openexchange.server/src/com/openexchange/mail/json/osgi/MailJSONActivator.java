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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
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
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.contact.ContactSearchMultiplexer;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.image.ImageLocation;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailActionFactory;
import com.openexchange.mail.json.converters.MailConverter;
import com.openexchange.mail.json.converters.MailJSONConverter;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailJSONActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailJSONActivator extends AJAXModuleActivator {

    protected static final Log LOG = com.openexchange.log.Log.loggerFor(MailJSONActivator.class);

    /**
     * Initializes a new {@link MailJSONActivator}.
     */
    public MailJSONActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContactInterfaceDiscoveryService.class, ContactStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerModule(new MailActionFactory(new ExceptionOnAbsenceServiceLookup(this)), "mail");
        final MailConverter converter = MailConverter.getInstance();
        registerService(ResultConverter.class, converter);
        registerService(ResultConverter.class, new MailJSONConverter(converter));

        final int[] columns = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.IMAGE1 };
        registerService(AJAXResultDecorator.class, new AJAXResultDecorator() {

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
                final String action = requestData.getParameter("action");
                if ("get".equals(action) && resultObject instanceof MailMessage) {
                    try {
                        final MailMessage mailMessage = (MailMessage) resultObject;
                        final InternetAddress[] from = mailMessage.getFrom();
                        if (null == from || 0 == from.length) {
                            return;
                        }
                        final ContactSearchObject searchObject = createContactSearchObject(from[0]);
                        final ContactSearchMultiplexer multiplexer =
                            new ContactSearchMultiplexer(getService(ContactInterfaceDiscoveryService.class));
                        SearchIterator<Contact> it = null;
                        final List<Contact> contacts = new LinkedList<Contact>();
                        try {
                            it = multiplexer.extendedSearch(session, searchObject, Contact.DISPLAY_NAME, Order.ASCENDING, "utf-8", columns);
                            while (it.hasNext()) {
                                contacts.add(it.next());
                            }
                        } finally {
                            if (it != null) {
                                it.close();
                            }
                        }
                        converter.convert2JSON(requestData, result, session);
                        final JSONObject jObject = (JSONObject) result.getResultObject();
                        final JSONArray jArray = new JSONArray();
                        for (final Contact contact : contacts) {

                            if (contact.containsImage1()) {
                                final byte[] imageData = contact.getImage1();
                                if (imageData != null) {
                                    try {
                                        final ContactImageDataSource imgSource = ContactImageDataSource.getInstance();
                                        final ImageLocation imageLocation =
                                            new ImageLocation.Builder().folder(Integer.toString(contact.getParentFolderID())).id(
                                                Integer.toString(contact.getObjectID())).build();
                                        final String imageURL = imgSource.generateUrl(imageLocation, session);
                                        jArray.put(imageURL);
                                    } catch (final OXException e) {
                                        com.openexchange.log.LogFactory.getLog(ContactWriter.class).warn(
                                            "Contact image URL could not be generated.",
                                            e);
                                    }
                                }
                            }
                        }
                        jObject.put("from_image_urls", jArray);
                    } catch (final JSONException e) {
                        throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
                    }
                }
            }
        });
    }

    protected static Date getCorrectedTime(final Date date, final TimeZone timeZone) {
        if (date == null) {
            return null;
        }

        final int offset = timeZone.getOffset(date.getTime());
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.MILLISECOND, offset);

        return calendar.getTime();
    }

    protected static ContactSearchObject createContactSearchObject(final InternetAddress from) throws OXException {
        final ContactSearchObject searchObject = new ContactSearchObject();
        // searchObject.addFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID); // Global address book
        searchObject.setOrSearch(true);
        final String address = from.getAddress();
        searchObject.setEmail1(address);
        searchObject.setEmail2(address);
        searchObject.setEmail3(address);
        return searchObject;
    }

}
