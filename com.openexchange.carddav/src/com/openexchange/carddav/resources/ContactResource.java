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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.carddav.resources;

import java.io.InputStream;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.carddav.Tools;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.MappedIncorrectString;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.java.Streams;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link ContactResource} - Abstract base class for CardDAV resources.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactResource extends CardDAVResource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactResource.class);
    private static final int MAX_RETRIES = 3;

    private boolean exists = false;
    private Contact contact = null;
    private String parentFolderID = null;

    private ThresholdFileHolder bodyFileHolder = null;

    /**
     * Creates a new {@link ContactResource} representing an existing contact.
     *
     * @param contact The contact
     * @param factory The CardDAV factory
     * @param url The WebDAV URL
     */
    public ContactResource(Contact contact, GroupwareCarddavFactory factory, WebdavPath url) {
        super(factory, url);
        this.contact = contact;
        this.exists = null != contact;
    }

    /**
     * Creates a new placeholder {@link ContactResource} at the specified URL.
     *
     * @param factory The CardDAV factory
     * @param url The WebDAV URL
     * @param parentFolderID The ID of the parent folder
     * @throws WebdavProtocolException
     */
    public ContactResource(GroupwareCarddavFactory factory, WebdavPath url, String parentFolderID) throws WebdavProtocolException {
        this(null, factory, url);
        this.parentFolderID = parentFolderID;
    }

    @Override
    public void create() throws WebdavProtocolException {
        String vCardID = null;
        boolean created = false;
        try {
            if (exists()) {
                throw protocolException(HttpServletResponse.SC_CONFLICT);
            } else if (null == contact) {
                throw protocolException(HttpServletResponse.SC_NOT_FOUND);
            }
            if (contact.getMarkAsDistribtuionlist()) {
                /*
                 * insert & delete not supported contact group (next sync cleans up the client)
                 */
                try {
                    LOG.warn("{}: contact groups not supported, performing immediate deletion of this resource.", this.getUrl());
                    contact.removeDistributionLists();
                    contact.removeNumberOfDistributionLists();
                    factory.getContactService().createContact(factory.getSession(), Integer.toString(contact.getParentFolderID()), contact);
                    factory.getContactService().deleteContact(factory.getSession(), Integer.toString(contact.getParentFolderID()),
                        Integer.toString(contact.getObjectID()), contact.getLastModified());
                } catch (OXException e) {
                    throw protocolException(e);
                }
            } else {
                /*
                 * store original vCard if possible
                 */
                ContactService contactService = factory.getContactService();
                String folderID = Integer.toString(contact.getParentFolderID());
                if (null != bodyFileHolder && contactService.supports(factory.getSession(), folderID, ContactField.VCARD_ID)) {
                    VCardStorageService vCardStorage = factory.getVCardStorageService();
                    if (null != vCardStorage) {
                        vCardID = vCardStorage.saveVCard(bodyFileHolder.getStream(), factory.getSession().getContextId());
                        LOG.debug("{}: saved original vCard in '{}'.", getUrl(), vCardID);
                        contact.setVCardId(vCardID);
                    }
                }
                /*
                 * save contact, trying again in case of recoverable errors
                 */
                for (int i = 0; i <= MAX_RETRIES && false == created; i++) {
                    try {
                        factory.getContactService().createContact(factory.getSession(), Integer.toString(contact.getParentFolderID()), contact);
                        LOG.debug("{}: created.", getUrl());
                        created = true;
                    } catch (OXException e) {
                        if (false == handle(e)) {
                            break;
                        }
                    }
                }
            }
        } catch (OXException e) {
            throw protocolException(e);
        } finally {
            closeBodyFileHolder();
            if (null != vCardID && false == created) {
                deleteVCard(vCardID);
            }
        }
    }

    @Override
    public boolean exists() throws WebdavProtocolException {
        return this.exists;
    }

    @Override
    public void delete() throws WebdavProtocolException {
        boolean deleted = false;
        String vCardID = null != contact ? contact.getVCardId() : null;
        try {
            if (false == exists()) {
                throw protocolException(HttpServletResponse.SC_NOT_FOUND);
            }
            /*
             * delete contact, trying again in case of recoverable errors
             */
            for (int i = 0; i < MAX_RETRIES && false == deleted; i++) {
                try {
                    factory.getContactService().deleteContact(factory.getSession(), Integer.toString(contact.getParentFolderID()),
                        Integer.toString(contact.getObjectID()), contact.getLastModified());
                    LOG.debug("{}: deleted.", getUrl());
                    deleted = true;
                    contact = null;
                    exists = false;
                } catch (OXException e) {
                    if (false == handle(e)) {
                        break;
                    }
                }
            }
        } finally {
            if (null != vCardID && deleted) {
                deleteVCard(vCardID);
            }
        }
    }

    @Override
    public void save() throws WebdavProtocolException {
        String vCardID = null;
        String previousVCardID = null;
        boolean saved = false;
        try {
            if (false == exists()) {
                throw protocolException(HttpServletResponse.SC_NOT_FOUND);
            }
            /*
             * store original vCard if possible
             */
            previousVCardID = contact.getVCardId();
            ContactService contactService = factory.getContactService();
            String folderID = Integer.toString(contact.getParentFolderID());
            if (null != bodyFileHolder && contactService.supports(factory.getSession(), folderID, ContactField.VCARD_ID)) {
                VCardStorageService vCardStorage = factory.getVCardStorageService();
                if (null != vCardStorage) {
                    vCardID = vCardStorage.saveVCard(bodyFileHolder.getStream(), factory.getSession().getContextId());
                    LOG.debug("{}: saved original vCard in '{}'.", getUrl(), vCardID);
                    contact.setVCardId(vCardID);
                }
            }
            /*
             * update contact, trying again in case of recoverable errors
             */
            for (int i = 0; i < MAX_RETRIES && false == saved; i++) {
                try {
                    factory.getContactService().updateContact(factory.getSession(), Integer.toString(contact.getParentFolderID()),
                        Integer.toString(contact.getObjectID()), contact, contact.getLastModified());
                    LOG.debug("{}: saved.", getUrl());
                    saved = true;
                } catch (OXException e) {
                    if (false == handle(e)) {
                        break;
                    }
                }
            }
        } catch (OXException e) {
            throw protocolException(e);
        } finally {
            closeBodyFileHolder();
            if (saved) {
                deleteVCard(previousVCardID);
            } else if (null != vCardID) {
                deleteVCard(vCardID);
            }
        }
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return null != contact ? contact.getCreationDate() : new Date(0);
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return null != contact ? contact.getLastModified() : new Date(0);
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return null != contact ? contact.getDisplayName() : null;
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
        if (null != contact) {
            contact.setDisplayName(displayName);
        }
    }

    @Override
    public void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException {
        try {
            /*
             * park body in file holder for accessing the original vCard later before parsing
             */
            closeBodyFileHolder();
            bodyFileHolder = new ThresholdFileHolder().write(body);
            if (0 >= bodyFileHolder.getCount()) {
                throw this.protocolException(HttpServletResponse.SC_BAD_REQUEST);
            }
            if (false == exists()) {
                /*
                 * import vCard as new contact
                 */
                contact = factory.getVCardService().importVCard(bodyFileHolder.getStream(), null, null);
                if (null != url) {
                    String extractedUID = Tools.extractUID(url);
                    if (null != extractedUID && false == extractedUID.equals(contact.getUid())) {
                        /*
                         * Always extract the UID from the URL; the Addressbook client in MacOS 10.6 uses different UIDs in
                         * the WebDAV path and the UID field in the vCard, so we need to store this UID in the contact
                         * resource, too, to recognize later updates on the resource.
                         */
                        LOG.debug("{}: Storing WebDAV resource name in filename.", getUrl());
                        contact.setFilename(extractedUID);
                    }
                }
                contact.setContextId(factory.getSession().getContextId());
                contact.setParentFolderID(Tools.parse(parentFolderID));
            } else {
                /*
                 * import vCard and merge with existing contact, ensuring that some important properties don't change
                 */
                int parentFolderID = contact.getParentFolderID();
                int contextID = contact.getContextId();
                Date lastModified = contact.getLastModified();
                int objectID = contact.getObjectID();
                String vCardID = contact.getVCardId();
                contact = factory.getVCardService().importVCard(bodyFileHolder.getStream(), contact, null);
                contact.removeUid();
                contact.setParentFolderID(parentFolderID);
                contact.setContextId(contextID);
                contact.setLastModified(lastModified);
                contact.setObjectID(objectID);
                contact.setVCardId(vCardID);
            }
        } catch (OXException e) {
            throw protocolException(e, HttpServletResponse.SC_BAD_REQUEST);
        } finally {
            Streams.close(body);
        }
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        /*
         * retrieve an original vCard if available
         */
        InputStream originalVCard = null;
        String vCardID = contact.getVCardId();
        if (null != vCardID) {
            VCardStorageService vCardStorage = factory.getVCardStorageService();
            if (null != vCardStorage) {
                originalVCard = vCardStorage.getVCard(vCardID, factory.getSession().getContextId());
            }
        }
        /*
         * export current contact data & return resulting vCard stream
         */
        try {
            return factory.getVCardService().exportContact(contact, originalVCard, null);
        } catch (OXException e) {
            throw protocolException(e);
        } finally {
            Streams.close(originalVCard);
        }
    }

    @Override
    protected String getUID() {
        return null != contact ? contact.getUid() : Tools.extractUID(getUrl());
    }

    /**
     * Tries to handle an exception.
     *
     * @param e the exception to handle
     * @return <code>true</code>, if the operation should be retried,
     * <code>false</code>, otherwise.
     * @throws WebdavProtocolException
     */
    private boolean handle(OXException e) throws WebdavProtocolException {
        LOG.debug("Trying to handle exception: {}", e.getMessage(), e);
        if (Tools.isImageProblem(e)) {
            /*
             * image problem, handle by create without image
             */
            LOG.warn("{}: {} - removing image and trying again.", getUrl(), e.getMessage());
            contact.removeImage1();
            return true;
        } else if (Tools.isDataTruncation(e)) {
            /*
             * handle by trimming truncated fields
             */
            if (trimTruncatedAttributes(e)) {
                LOG.warn("{}: {} - trimming fields and trying again.", getUrl(), e.getMessage());
                return true;
            }
        } else if (Tools.isIncorrectString(e)) {
            /*
             * handle by removing incorrect characters
             */
            if (replaceIncorrectStrings(e, "")) {
                LOG.warn("{}: {} - removing incorrect characters and trying again.", getUrl(), e.getMessage());
                return true;
            }
        } else if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
            /*
             * handle by overriding sync-token
             */
            LOG.debug("{}: {}", this.getUrl(), e.getMessage());
            LOG.debug("{}: overriding next sync token for client recovery.", this.getUrl());
            this.factory.overrideNextSyncToken();
        } else if (Category.CATEGORY_CONFLICT.equals(e.getCategory())) {
            throw super.protocolException(e, HttpServletResponse.SC_CONFLICT);
        } else {
            throw super.protocolException(e);
        }
        return false;
    }

    private boolean trimTruncatedAttributes(OXException e) {
        try {
            return MappedTruncation.truncate(e.getProblematics(), contact);
        } catch (OXException x) {
            LOG.warn("{}: error trying to handle truncated attributes", getUrl(), x);
            return false;
        }
    }

    private boolean replaceIncorrectStrings(OXException e, String replacement) {
        try {
            return MappedIncorrectString.replace(e.getProblematics(), this.contact, replacement);
        } catch (OXException x) {
            LOG.warn("{}: error trying to handle truncated attributes", getUrl(), x);
            return false;
        }
    }

    /**
     * Silently closes the body file holder if set.
     */
    private void closeBodyFileHolder() {
        if (null != bodyFileHolder) {
            Streams.close(bodyFileHolder);
            bodyFileHolder = null;
        }
    }

    /**
     * Deletes a vCard silently.
     *
     * @param vCardID The identifier of the vCard to delete, or <code>null</code> to do nothing
     */
    private void deleteVCard(String vCardID) {
        if (null != vCardID) {
            VCardStorageService vCardStorage = factory.getVCardStorageService();
            if (null != vCardStorage) {
                vCardStorage.deleteVCard(vCardID, factory.getSession().getContextId());
            }
        }
    }

}
