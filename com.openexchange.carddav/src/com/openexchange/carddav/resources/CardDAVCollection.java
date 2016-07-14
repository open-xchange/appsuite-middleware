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

package com.openexchange.carddav.resources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.carddav.CarddavProtocol;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.carddav.Tools;
import com.openexchange.carddav.mixins.BulkRequests;
import com.openexchange.carddav.mixins.MaxImageSize;
import com.openexchange.carddav.mixins.MaxResourceSize;
import com.openexchange.carddav.mixins.SupportedAddressData;
import com.openexchange.carddav.mixins.SupportedReportSet;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.similarity.ContactSimilarityService;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.SimilarityException;
import com.openexchange.dav.reports.SyncStatus;
import com.openexchange.dav.resources.CommonFolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link CardDAVCollection} - CardDAV collection for contact folders.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CardDAVCollection extends CommonFolderCollection<Contact> {

    protected final GroupwareCarddavFactory factory;

    private Boolean isStoreOriginalVCard;

    /**
     * Initializes a new {@link CardDAVCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder, or <code>null</code> if it not yet exists
     */
    public CardDAVCollection(GroupwareCarddavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        super(factory, url, folder);
        this.factory = factory;
        includeProperties(new SupportedReportSet(), new MaxResourceSize(factory), new MaxImageSize(factory), new SupportedAddressData(), new BulkRequests(factory));
    }

    /**
     * Parses and imports all vCards from the supplied input stream.
     *
     * @param inputStream The input stream to parse and import from
     * @return The import results
     */
    public List<BulkImportResult> bulkImport(InputStream inputStream, float maxSimilarity) throws OXException {
        List<BulkImportResult> importResults = new ArrayList<BulkImportResult>();
        VCardService vCardService = factory.requireService(VCardService.class);
        VCardParameters parameters = vCardService.createParameters(factory.getSession()).setKeepOriginalVCard(isStoreOriginalVCard())
            .setImportAttachments(true).setRemoveAttachmentsFromKeptVCard(true);
        SearchIterator<VCardImport> searchIterator = null;
        try {
            searchIterator = vCardService.importVCards(inputStream, parameters);
            while (searchIterator.hasNext()) {
                importResults.add(bulkImport(searchIterator.next(), maxSimilarity));
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        return importResults;
    }

    private BulkImportResult bulkImport(VCardImport vCardImport, float maxSimilarity) throws OXException {
        BulkImportResult importResult = new BulkImportResult();
        if (null == vCardImport || null == vCardImport.getContact()) {
            importResult.setError(new PreconditionException(DAVProtocol.CARD_NS.getURI(), "valid-address-data", getUrl(), HttpServletResponse.SC_FORBIDDEN));
        } else {
            Contact contact = vCardImport.getContact();
            importResult.setUid(contact.getUid());
            WebdavPath url = null;
            if (contact.containsFilename() || contact.containsUid()) {
                url = constructPathForChildResource(contact);
                importResult.setHref(url);
            }
            try {
                checkMaxResourceSize(vCardImport);
                checkUidConflict(contact.getUid());
                checkSimilarityConflict(maxSimilarity, contact, importResult);
                ContactResource.fromImport(factory, this, url, vCardImport).create();
                if (importResult.getHref() == null) {
                    url = constructPathForChildResource(contact);
                    importResult.setHref(url);
                }
            } catch (SimilarityException e) {
                importResult.setError(e);
            } catch (PreconditionException e) {
                importResult.setError(e);
            } catch (WebdavProtocolException e) {
                importResult.setError(new PreconditionException(DAVProtocol.CARD_NS.getURI(), "valid-address-data", getUrl(), HttpServletResponse.SC_FORBIDDEN));
            }
        }
        return importResult;
    }

    /**
     * Tests if the given contact is too similar to another contact in the same folder
     * 
     * @param maxSimilarity The maximum accepted similarity
     * @param contact The contact to test
     * @param result The result object
     * @throws OXException if the ContactSimilarityService is not available or if the contact is too similar to another contact
     */
    private void checkSimilarityConflict(float maxSimilarity, Contact contact, BulkImportResult result) throws OXException {
        if (maxSimilarity > 0) {
            // test if contact is too similar to other contacts
            ContactSimilarityService service = this.factory.getService(ContactSimilarityService.class);
            if (service == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ContactSimilarityService.class.getSimpleName());
            }
            Contact duplicate = service.getSimilar(factory.getSession(), contact, maxSimilarity);
            if (duplicate != null) {
                result.setUid(duplicate.getUid());
                throw new SimilarityException(constructPathForChildResource(duplicate).toString(), contact.getUid(), HttpServletResponse.SC_CONFLICT);
            }
        }
    }

    /**
     * Checks the vCard import's size against the maximum allowed vCard size.
     *
     * @param vCardImport The vCard import to check
     * @throws PreconditionException <code>(CARDDAV:max-resource-size)</code> if the maximum size is exceeded
     */
    private void checkMaxResourceSize(VCardImport vCardImport) throws PreconditionException {
        long maxSize = factory.getState().getMaxVCardSize();
        if (0 < maxSize) {
            IFileHolder vCard = vCardImport.getVCard();
            if (null != vCard && maxSize < vCard.getLength()) {
                throw new PreconditionException(DAVProtocol.CARD_NS.getURI(), "max-resource-size", getUrl(), HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }

    /**
     * Checks for an existing resource in this collection conflicting with a specific UID.
     *
     * @param uid The UID to check
     * @throws OXException If the check fails
     * @throws PreconditionException <code>(CARDDAV:no-uid-conflict)</code> if the UID conflicts with an existing resource
     */
    private void checkUidConflict(String uid) throws OXException, PreconditionException {
        /*
         * prepare search term
         */
        List<UserizedFolder> folders = getFolders();
        if (0 == folders.size()) {
            return;
        }
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND);
        if (1 == folders.size()) {
            SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
            term.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
            term.addOperand(new ConstantOperand<String>(folders.get(0).getID()));
            searchTerm.addSearchTerm(term);
        } else {
            CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
            for (UserizedFolder folder : folders) {
                SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                term.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
                term.addOperand(new ConstantOperand<String>(folder.getID()));
                orTerm.addSearchTerm(term);
            }
            searchTerm.addSearchTerm(orTerm);
        }
        SingleSearchTerm uidTerm = new SingleSearchTerm(SingleOperation.EQUALS);
        uidTerm.addOperand(new ContactFieldOperand(ContactField.UID));
        uidTerm.addOperand(new ConstantOperand<String>(uid));
        searchTerm.addSearchTerm(uidTerm);
        /*
         * lookup conflicting contacts
         */
        ContactField[] fields = new ContactField[] { ContactField.FILENAME, ContactField.UID };
        SearchIterator<Contact> iterator = null;
        try {
            iterator = factory.getContactService().searchContacts(factory.getSession(), searchTerm, fields, SortOptions.EMPTY);
            if (iterator.hasNext()) {
                Contact contact = iterator.next();
                throw new PreconditionException(DAVProtocol.CARD_NS.getURI(), "no-uid-conflict", constructPathForChildResource(contact), HttpServletResponse.SC_FORBIDDEN);
            }
        } finally {
            SearchIterators.close(iterator);
        }
    }

    /**
     * Gets a value indicating whether the underlying storage supports storing the original vCard or not.
     *
     * @return <code>true</code> if storing the original vCard is possible, <code>false</code>, otherwise
     */
    public boolean isStoreOriginalVCard() {
        if (null == isStoreOriginalVCard) {
            VCardStorageService vCardStorageService = factory.getVCardStorageService(factory.getSession().getContextId());
            if (null != vCardStorageService) {
                try {
                    isStoreOriginalVCard = Boolean.valueOf(
                        factory.requireService(ContactService.class).supports(factory.getSession(), folder.getID(), ContactField.VCARD_ID));
                } catch (OXException e) {
                    LOG.warn("Error checking if storing the vCard ID is supported, assuming \"false\".", e);
                    isStoreOriginalVCard = Boolean.FALSE;
                }
            } else {
                isStoreOriginalVCard = Boolean.FALSE;
            }
        }
        return isStoreOriginalVCard.booleanValue();
    }

    /**
     * Gets a list of one or more folders represented by the collection.
     *
     * @return The folder identifiers
     */
    protected List<UserizedFolder> getFolders() throws OXException {
        return Collections.singletonList(folder);
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        return super.getResourceType() + CarddavProtocol.ADDRESSBOOK;
    }

    public List<WebdavResource> getFilteredObjects(SearchTerm<?> term) throws WebdavProtocolException {
        List<WebdavResource> resources = new ArrayList<WebdavResource>();
        SearchIterator<Contact> searchIterator = null;
        try {
            searchIterator  = searchContacts(term, getFolders());
            while (searchIterator.hasNext()) {
                Contact contact = searchIterator.next();
                resources.add(createResource(contact, constructPathForChildResource(contact)));
            }
        } catch (OXException e) {
            throw protocolException(e);
        } finally {
            SearchIterators.close(searchIterator);
        }
        return resources;
    }

    protected SearchIterator<Contact> searchContacts(SearchTerm<?> term, List<UserizedFolder> folders) throws OXException {
        if (null != folders && 0 < folders.size()) {
            CompositeSearchTerm compositeTerm = new CompositeSearchTerm(CompositeOperation.AND);
            for (UserizedFolder folder : folders) {
                SingleSearchTerm folderIDTerm = new SingleSearchTerm(SingleOperation.EQUALS);
                folderIDTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
                folderIDTerm.addOperand(new ConstantOperand<String>(folder.getID()));
                compositeTerm.addSearchTerm(folderIDTerm);
            }
            compositeTerm.addSearchTerm(term);
            term = compositeTerm;
        }
        return factory.getContactService().searchContacts(factory.getSession(), term);
    }

    @Override
    protected Collection<Contact> getModifiedObjects(Date since) throws OXException {
        Collection<Contact> contacts = new ArrayList<Contact>();
        for (UserizedFolder folder : getFolders()) {
            Collection<Contact> modifiedContacts = factory.getState().getModifiedContacts(since, folder.getID());
            if (null != modifiedContacts) {
                contacts.addAll(modifiedContacts);
            }
        }
        return contacts;
    }

    @Override
    protected Collection<Contact> getDeletedObjects(Date since) throws OXException {
        Collection<Contact> contacts = new ArrayList<Contact>();
        for (UserizedFolder folder : getFolders()) {
            Collection<Contact> contactList = factory.getState().getDeletedContacts(since, folder.getID());
            if (null != contactList) {
                contacts.addAll(contactList);
            }
        }
        return contacts;
    }

    @Override
    protected Collection<Contact> getObjects() throws OXException {
        Collection<Contact> contacts = new ArrayList<Contact>();
        for (UserizedFolder folder : getFolders()) {
            Collection<Contact> contactList = factory.getState().getContacts(folder.getID());
            if (null != contactList) {
                contacts.addAll(contactList);
            }
        }
        return contacts;
    }

    @Override
    protected Contact getObject(String resourceName) throws OXException {
        return factory.getState().load(resourceName);
    }

    @Override
    protected AbstractResource createResource(Contact object, WebdavPath url) throws OXException {
        return new ContactResource(factory, this, object, url);
    }

    @Override
    protected String getFileExtension() {
        return ContactResource.EXTENSION_VCF;
    }

	@Override
	public Date getLastModified() throws WebdavProtocolException {
       try {
            Date lastModified = new Date(0);
            for (UserizedFolder folder : getFolders()) {
                lastModified = Tools.getLatestModified(lastModified, factory.getState().getLastModified(folder));
            }
            return lastModified;
        } catch (OXException e) {
            throw protocolException(e);
        }
	}

    @Override
    public SyncStatus<WebdavResource> getSyncStatus(String token) throws WebdavProtocolException {
        if (null != token && 0 < token.length()) {
            /*
             * check for overridden sync-token for this client
             */
            String overrrideSyncToken = factory.getOverrideNextSyncToken();
            if (null != overrrideSyncToken && 0 < overrrideSyncToken.length()) {
                factory.setOverrideNextSyncToken(null);
                token = overrrideSyncToken;
                LOG.debug("Overriding sync token to '{}' for user '{}'.", token, this.factory.getUser());
            }
        }
        return super.getSyncStatus(token);
    }

    @Override
    public String getCTag() throws WebdavProtocolException {
        /*
         * check for overridden sync-token for this client
         */
        String overrrideSyncToken = factory.getOverrideNextSyncToken();
        if (null != overrrideSyncToken && 0 < overrrideSyncToken.length()) {
            factory.setOverrideNextSyncToken(null);
            String value = "http://www.open-xchange.com/ctags/" + folder.getID() + "-" + overrrideSyncToken;
            LOG.debug("Overriding CTag property to '{}' for user '{}'.", value, factory.getUser());
            return value;
        }
        return super.getCTag();
    }

}
