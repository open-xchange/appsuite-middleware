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

package com.openexchange.importexport.exporters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.contact.ContactService;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.log.LogFactory;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (minor: changes to new interface)
 */
public class VCardExporter implements Exporter {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(VCardExporter.class));
    protected final static int[] _contactFields = {
        DataObject.OBJECT_ID,
        DataObject.CREATED_BY,
        DataObject.CREATION_DATE,
        DataObject.LAST_MODIFIED,
        DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID,
        CommonObject.CATEGORIES,
        Contact.GIVEN_NAME,
        Contact.SUR_NAME,
        Contact.ANNIVERSARY,
        Contact.ASSISTANT_NAME,
        Contact.BIRTHDAY,
        Contact.BRANCHES,
        Contact.BUSINESS_CATEGORY,
        Contact.CELLULAR_TELEPHONE1,
        Contact.CELLULAR_TELEPHONE2,
        Contact.CITY_BUSINESS,
        Contact.CITY_HOME,
        Contact.CITY_OTHER,
        Contact.COLOR_LABEL,
        Contact.COMMERCIAL_REGISTER,
        Contact.COMPANY,
        Contact.COUNTRY_BUSINESS,
        Contact.COUNTRY_HOME,
        Contact.COUNTRY_OTHER,
        Contact.DEPARTMENT,
        Contact.DISPLAY_NAME,
        Contact.DISTRIBUTIONLIST,
        Contact.EMAIL1,
        Contact.EMAIL2,
        Contact.EMAIL3,
        Contact.EMPLOYEE_TYPE,
        Contact.FAX_BUSINESS,
        Contact.FAX_HOME,
        Contact.FAX_OTHER,
        Contact.INFO,
        Contact.INSTANT_MESSENGER1,
        Contact.INSTANT_MESSENGER2,
        Contact.IMAGE1,
        Contact.IMAGE1_CONTENT_TYPE,
        Contact.LINKS,
        Contact.MANAGER_NAME,
        Contact.MARITAL_STATUS,
        Contact.MIDDLE_NAME,
        Contact.NICKNAME,
        Contact.NOTE,
        Contact.NUMBER_OF_CHILDREN,
        Contact.NUMBER_OF_EMPLOYEE,
        Contact.POSITION,
        Contact.POSTAL_CODE_BUSINESS,
        Contact.POSTAL_CODE_HOME,
        Contact.POSTAL_CODE_OTHER,
        Contact.PRIVATE_FLAG,
        Contact.PROFESSION,
        Contact.ROOM_NUMBER,
        Contact.SALES_VOLUME,
        Contact.SPOUSE_NAME,
        Contact.STATE_BUSINESS,
        Contact.STATE_HOME,
        Contact.STATE_OTHER,
        Contact.STREET_BUSINESS,
        Contact.STREET_HOME,
        Contact.STREET_OTHER,
        Contact.SUFFIX,
        Contact.TAX_ID,
        Contact.TELEPHONE_ASSISTANT,
        Contact.TELEPHONE_BUSINESS1,
        Contact.TELEPHONE_BUSINESS2,
        Contact.TELEPHONE_CALLBACK,
        Contact.TELEPHONE_CAR,
        Contact.TELEPHONE_COMPANY,
        Contact.TELEPHONE_HOME1,
        Contact.TELEPHONE_HOME2,
        Contact.TELEPHONE_IP,
        Contact.TELEPHONE_ISDN,
        Contact.TELEPHONE_OTHER,
        Contact.TELEPHONE_PAGER,
        Contact.TELEPHONE_PRIMARY,
        Contact.TELEPHONE_RADIO,
        Contact.TELEPHONE_TELEX,
        Contact.TELEPHONE_TTYTDD,
        Contact.TITLE,
        Contact.URL,
        Contact.USERFIELD01,
        Contact.USERFIELD02,
        Contact.USERFIELD03,
        Contact.USERFIELD04,
        Contact.USERFIELD05,
        Contact.USERFIELD06,
        Contact.USERFIELD07,
        Contact.USERFIELD08,
        Contact.USERFIELD09,
        Contact.USERFIELD10,
        Contact.USERFIELD11,
        Contact.USERFIELD12,
        Contact.USERFIELD13,
        Contact.USERFIELD14,
        Contact.USERFIELD15,
        Contact.USERFIELD16,
        Contact.USERFIELD17,
        Contact.USERFIELD18,
        Contact.USERFIELD19,
        Contact.USERFIELD20,
        Contact.DEFAULT_ADDRESS
    };

    @Override
    public boolean canExport(final ServerSession session, final Format format, final String folder, final Map<String, Object> optionalParams) throws OXException {
        if (!format.equals(Format.VCARD)) {
            return false;
        }

        final int folderId = Integer.parseInt(folder);
        final FolderObject fo;
        try {
            fo = new OXFolderAccess(session.getContext()).getFolderObject(folderId);
        } catch (final OXException e) {
            return false;
        }
        //check format of folder
        if ( fo.getModule() == FolderObject.CONTACT){
            if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()).hasContact()) {
                return false;
            }
        } else {
            return false;
        }
        //check read access to folder
        final EffectivePermission perm;
        try {
            perm = fo.getEffectiveUserPermission(session.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()));
        } catch (final OXException e) {
            throw ImportExportExceptionCodes.NO_DATABASE_CONNECTION.create(e);
        } catch (final RuntimeException e) {
            throw ImportExportExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
        return perm.canReadAllObjects();
    }

    @Override
    public SizedInputStream exportData(final ServerSession session, final Format format, final String folder, int[] fieldsToBeExported, final Map<String, Object> optionalParams) throws OXException {
        try {
            final AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
            if (null != requestData) {
                // Try to stream
                final OutputStream out = requestData.optOutputStream();
                if (null != out) {
                    requestData.setResponseHeader("Content-Type", Format.VCARD.getMimeType() + "; charset=UTF-8");
                    requestData.setResponseHeader("Content-Disposition", "attachment; filename=" + Format.VCARD.getFullName() + "." + Format.VCARD.getExtension());
                    requestData.removeCachingHeader();
                    export2out(session, folder, null, fieldsToBeExported, out);
                    return null;
                }
            }
            // No streaming support possible
            final ByteArrayOutputStream byteArrayOutputStream = new UnsynchronizedByteArrayOutputStream();
            export2out(session, folder, null, fieldsToBeExported, byteArrayOutputStream);
            return new SizedInputStream(
                new UnsynchronizedByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                byteArrayOutputStream.size(),
                Format.VCARD);
        } catch (final NumberFormatException e) {
            throw ImportExportExceptionCodes.NUMBER_FAILED.create(e, folder);
        } catch (final ConverterException e) {
            throw ImportExportExceptionCodes.VCARD_CONVERSION_FAILED.create(e);
        } catch (final IOException e) {
            throw ImportExportExceptionCodes.VCARD_CONVERSION_FAILED.create(e);
        }
    }

    private static final ContactField[] FIELDS_ID = new ContactField[] { ContactField.OBJECT_ID };

    private void export2out(final ServerSession session, final String folderId, final String objectId, final int[] fieldsToBeExported, final OutputStream out) throws IOException, ConverterException, OXException {
        final VersitDefinition contactDef = Versit.getDefinition("text/vcard");
        final VersitDefinition.Writer versitWriter = contactDef.getWriter(out, "UTF-8");
        final OXContainerConverter oxContainerConverter = new OXContainerConverter(session);

        //final TimeZone timeZone = TimeZoneUtils.getTimeZone(sessObj.getUserObject().getTimeZone());
        //final String mail = sessObj.getUserObject().getMail();

        //final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj);
        ContactField[] fields = ContactMapper.getInstance().getFields(null == fieldsToBeExported ? _contactFields : fieldsToBeExported, null, (ContactField[])null);
        
        if (null == objectId) {
            if (EnumSet.copyOf(Arrays.asList(fields)).contains(ContactField.IMAGE1)) {
                // Contact by contact
                final ContactService contactService = ImportExportServices.getContactService();
                final SearchIterator<Contact> searchIterator = contactService.getAllContacts(session, folderId, FIELDS_ID);
                try {
                    while (searchIterator.hasNext()) {
                        final Contact contact = searchIterator.next();
                        try {
                            exportContact(oxContainerConverter, contactDef, versitWriter, contactService.getContact(session, folderId, Integer.toString(contact.getObjectID()), fields));
                        } catch (final OXException e) {
                            if (!ContactExceptionCodes.CONTACT_NOT_FOUND.equals(e)) {
                                throw e;
                            }
                        }
                    }
                    // versitWriter.flush();
                } finally {
                    closeVersitResources(oxContainerConverter, versitWriter);
                    try {
                        searchIterator.close();
                    } catch (final SearchIteratorException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            } else {
                final SearchIterator<Contact> searchIterator = ImportExportServices.getContactService().getAllContacts(session, folderId, fields);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Going to export " + searchIterator.size() + " contacts (user=" + session.getUserId() + ", context=" + session.getContextId()+")");
                }
                try {
                    while (searchIterator.hasNext()) {
                        exportContact(oxContainerConverter, contactDef, versitWriter, searchIterator.next());
                    }
                    // versitWriter.flush();
                } finally {
                    closeVersitResources(oxContainerConverter, versitWriter);
                    try {
                        searchIterator.close();
                    } catch (final SearchIteratorException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        } else {
            final Contact contactObj = ImportExportServices.getContactService().getContact(session, folderId, objectId, fields);
            try {
                exportContact(oxContainerConverter, contactDef, versitWriter, contactObj);
            } finally {
                closeVersitResources(oxContainerConverter, versitWriter);
            }
        }
    }

    @Override
    public SizedInputStream exportData(final ServerSession session, final Format format, final String folder, final int objectId, final int[] fieldsToBeExported, final Map<String, Object> optionalParams) throws OXException {
        try {
            final AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
            if (null != requestData) {
                // Try to stream
                final OutputStream out = requestData.optOutputStream();
                if (null != out) {
                    requestData.setResponseHeader("Content-Type", Format.VCARD.getMimeType() + "; charset=UTF-8");
                    requestData.setResponseHeader("Content-Disposition", "attachment; filename=" + Format.VCARD.getFullName() + "." + Format.VCARD.getExtension());
                    requestData.removeCachingHeader();
                    export2out(session, folder, Integer.toString(objectId), fieldsToBeExported, out);
                    return null;
                }
            }
            // No streaming support possible
            final ByteArrayOutputStream byteArrayOutputStream = new UnsynchronizedByteArrayOutputStream();
            export2out(session, folder, Integer.toString(objectId), fieldsToBeExported, byteArrayOutputStream);
            return new SizedInputStream(
                new UnsynchronizedByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                byteArrayOutputStream.size(),
                Format.VCARD);
        } catch (final NumberFormatException e) {
            throw ImportExportExceptionCodes.NUMBER_FAILED.create(e, folder);
        } catch (final IOException e) {
            throw ImportExportExceptionCodes.VCARD_CONVERSION_FAILED.create(e);
        } catch (final ConverterException e) {
            throw ImportExportExceptionCodes.VCARD_CONVERSION_FAILED.create(e);
        }
    }

    protected void exportContact(final OXContainerConverter oxContainerConverter, final VersitDefinition versitDef, final VersitDefinition.Writer writer, final Contact contactObj) throws ConverterException, IOException {
        final VersitObject versitObject = oxContainerConverter.convertContact(contactObj, "3.0");
        versitDef.write(writer, versitObject);
        writer.flush();
    }

    private static void closeVersitResources(final OXContainerConverter oxContainerConverter, final VersitDefinition.Writer versitWriter) {
        if (oxContainerConverter != null) {
            oxContainerConverter.close();
        }
        if (versitWriter != null) {
            try {
                versitWriter.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
