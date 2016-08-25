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
import java.util.Collection;
import java.util.List;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.ValidationWarnings;
import ezvcard.io.chain.ChainingTextWriter;
import ezvcard.io.text.VCardReader;
import ezvcard.util.IOUtils;

/**
 * {@link VCardImportIterator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class VCardImportIterator implements SearchIterator<VCardImport> {

    private final VCardMapper mapper;
    private final VCardParameters parameters;
    private final VCardInputStream vCardStream;
    private final VCardReader reader;
    private final List<OXException> warnings;

    private VCardImport next;

    /**
     * Initializes a new {@link VCardImportIterator}.
     *
     * @param inputStream The source input stream
     * @param mapper The vCard mapper to use
     * @param parameters The vCard parameters
     */
    public VCardImportIterator(InputStream inputStream, VCardMapper mapper, VCardParameters parameters) throws OXException {
        super();
        this.mapper = mapper;
        this.parameters = parameters;
        warnings = new ArrayList<OXException>();
        vCardStream = new VCardInputStream(inputStream, parameters.getMaxVCardSize());
        if(parameters.isEnforceUtf8()) {
            reader = new VCardReader(IOUtils.utf8Reader(vCardStream));
        } else {
            reader = new VCardReader(vCardStream);
        }
    }

    /**
     * Imports the first vCard from the underlying input stream and optionally merges the data into an existing contact.
     *
     * @param contact The contact to merge the vCard into, or <code>null</code> to import as a new contact
     * @return The vCard import, or <code>null</code> if there is no first vCard
     * @throws OXException
     */
    public VCardImport first(Contact contact) throws OXException {
        if (null != next) {
            throw new IllegalStateException("first() can't be invoked after hasNext() or next()");
        }
        return readNext(contact);
    }

    @Override
    public boolean hasNext() throws OXException {
        if (null == next) {
            next = readNext(null);
            return null != next;
        }
        return true;
    }

    @Override
    public VCardImport next() throws OXException {
        VCardImport next = null != this.next ? this.next : readNext(null);
        this.next = null;
        return next;
    }

    @Override
    public void close() {
        Streams.close(reader, vCardStream);
    }

    @Override
    public int size() {
        return -1;
    }

    @Override
    public boolean hasWarnings() {
        return null != warnings && 0 < warnings.size();
    }

    @Override
    public void addWarning(OXException warning) {
        warnings.add(warning);
    }

    public void addWarnings(Collection<? extends OXException> warnings) {
        this.warnings.addAll(warnings);
    }

    @Override
    public OXException[] getWarnings() {
        return warnings.toArray(new OXException[warnings.size()]);
    }

    /**
     * Reads & imports the next vCard from the stream.
     *
     * @param contact The contact to merge the next vCard into, or <code>null</code> to import as a new contact
     * @return The vCard import result, or <code>null</code> if there is none
     */
    private VCardImport readNext(Contact contact) throws OXException {
        List<OXException> warnings = new ArrayList<OXException>();
        VCard vCard = null;
        try {
            vCard = reader.readNext();
        } catch (IOException e) {
            if (null != e.getCause() && OXException.class.isInstance(e.getCause())) {
                throw (OXException) e.getCause();
            }
            throw VCardExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
        warnings.addAll(VCardExceptionUtils.getParserWarnings(reader.getWarnings()));
        if (null == vCard) {
            /*
             * no vCard to import, so collect any warnings in parent iterator
             */
            this.addWarnings(warnings);
            return null;
        }
        /*
         * import vCard
         */
        contact = mapper.importVCard(vCard, contact, parameters, warnings);
        if (false == parameters.isSkipValidation()) {
            ValidationWarnings validationWarnings = vCard.validate(
                null != vCard.getVersion() ? vCard.getVersion() : ezvcard.VCardVersion.valueOfByStr(parameters.getVersion().getVersion()));
            warnings.addAll(VCardExceptionUtils.getValidationWarnings(validationWarnings));
        }
        /*
         * store original vCard in file holder if requested
         */
        ThresholdFileHolder originalVCard = null;
        if (parameters.isKeepOriginalVCard()) {
            originalVCard = new ThresholdFileHolder();
            ChainingTextWriter writerChain = Ezvcard.write(vCard).prodId(false);
            try {
                if(parameters.isEnforceUtf8()) {
                    writerChain.go(IOUtils.utf8Writer(originalVCard.asOutputStream()));
                } else {
                    writerChain.go(originalVCard.asOutputStream());
                }
            } catch (IllegalArgumentException e) {
                Streams.close(originalVCard);
                originalVCard = null;
                warnings.add(VCardExceptionCodes.ORIGINAL_VCARD_NOT_STORED.create(e, e.getMessage()));
            } catch (IOException e) {
                Streams.close(originalVCard);
                throw VCardExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
        /*
         * construct & return vCard import result
         */
        return new DefaultVCardImport(contact, warnings, originalVCard);
    }

}
