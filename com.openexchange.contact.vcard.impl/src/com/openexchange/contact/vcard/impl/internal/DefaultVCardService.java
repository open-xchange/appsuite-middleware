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

package com.openexchange.contact.vcard.impl.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.contact.vcard.VCardExport;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.VCardParametersFactory;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.VCardVersion;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.chain.ChainingTextWriter;
import ezvcard.util.IOUtils;

/**
 * {@link DefaultVCardService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class DefaultVCardService implements VCardService {

    private final VCardMapper mapper;
    private final VCardParametersFactory vCardParametersFactory;

    /**
     * Initializes a new {@link DefaultVCardService}.
     *
     * @param vCardParametersFactory The vCard parameters factory to use
     */
    public DefaultVCardService(VCardParametersFactory vCardParametersFactory) {
        super();
        this.vCardParametersFactory = vCardParametersFactory;
        this.mapper = new VCardMapper();
    }

    @Override
    public VCardParameters createParameters(Session session) {
        return vCardParametersFactory.createParameters(session);
    }

    @Override
    public VCardParameters createParameters() {
        return vCardParametersFactory.createParameters();
    }

    @Override
    public VCardExport exportContact(Contact contact, InputStream originalVCard, VCardParameters parameters) throws OXException {
        List<OXException> warnings = new ArrayList<OXException>();
        VCardParameters vCardParameters = getParametersOrDefault(parameters);
        /*
         * parse original vCard as template if supplied
         */
        VCard template = null;
        if (null != originalVCard) {
            try {
                if (vCardParameters.isEnforceUtf8()) {
                    template = Ezvcard.parse(IOUtils.utf8Reader(originalVCard)).first();
                } else {
                    template = Ezvcard.parse(originalVCard).first();
                }
            } catch (IOException e) {
                org.slf4j.LoggerFactory.getLogger(DefaultVCardService.class).error(
                    "Error parsing original vCard during export of contact {}: {}", contact, e.getMessage(), e);
            } finally {
                Streams.close(originalVCard);
            }
        }
        /*
         * export contact & return export result
         */
        VCard exportedVCard = mapper.exportContact(contact, template, vCardParameters, warnings);
        ThresholdFileHolder vCardHolder = exportVCards(Collections.singletonList(exportedVCard), vCardParameters);
        return new DefaultVCardExport(vCardHolder, warnings);
    }

    @Override
    public VCardImport importVCard(InputStream inputStream, Contact contact, VCardParameters parameters) throws OXException {
        VCardImportIterator importIterator = null;
        try {
            importIterator = new VCardImportIterator(inputStream, mapper, getParametersOrDefault(parameters));
            return importIterator.first(contact);
        } finally {
            SearchIterators.close(importIterator);
        }
    }

    @Override
    public SearchIterator<VCardImport> importVCards(InputStream vCards, VCardParameters parameters) throws OXException {
        VCardParameters vCardParameters = getParametersOrDefault(parameters);
        return new VCardImportIterator(vCards, mapper, vCardParameters);
    }

    @Override
    public ContactField[] getContactFields(Set<String> propertyNames) {
        return mapper.getContactFields(propertyNames);
    }

    /**
     * Gets the vCard parameters, or the default parameters if passed instance is <code>null</code>.
     *
     * @param parameters The parameters as passed from the client
     * @return The parameters, or the default parameters if passed instance is <code>null</code>
     */
    private VCardParameters getParametersOrDefault(VCardParameters parameters) {
        return null != parameters ? parameters : vCardParametersFactory.createParameters();
    }

    /**
     * Serializes one or more vCards into a threshold file holder.
     *
     * @param vCards The vCards to export
     * @param parameters The parameters to use
     * @return A new fileholder instance containing the serialized vCards
     */
    private ThresholdFileHolder exportVCards(List<VCard> vCards, VCardParameters parameters) throws OXException {
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        ChainingTextWriter writerChain = Ezvcard.write(vCards);
        applyOptions(getParametersOrDefault(parameters), writerChain);
        try {
            if(parameters.isEnforceUtf8()) {
                writerChain.go(IOUtils.utf8Writer(fileHolder.asOutputStream()));
            } else {
                writerChain.go(fileHolder.asOutputStream());
            }
            return fileHolder;
        } catch (IOException e) {
            Streams.close(fileHolder);
            throw VCardExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static ChainingTextWriter applyOptions(VCardParameters parameters, ChainingTextWriter writerChain) {
        writerChain.prodId(false);
        if (null != parameters) {
            writerChain.version(getVCardVersion(parameters.getVersion()));
            writerChain.versionStrict(parameters.isStrict());
        }
        return writerChain;
    }

    private static ezvcard.VCardVersion getVCardVersion(VCardVersion version) {
        return ezvcard.VCardVersion.valueOfByStr(version.getVersion());
    }

}