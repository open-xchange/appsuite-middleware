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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.contact.vcard.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.VCardParametersFactory;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.VCardVersion;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.io.SizeAwareInputStream;
import ezvcard.Ezvcard;
import ezvcard.Ezvcard.ParserChainTextReader;
import ezvcard.Ezvcard.WriterChainText;
import ezvcard.VCard;
import ezvcard.ValidationWarnings;
import ezvcard.Warning;
import ezvcard.property.VCardProperty;

/**
 * {@link DefaultVCardService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class DefaultVCardService implements VCardService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultVCardService.class);

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
    public InputStream exportContact(Contact contact, InputStream originalVCard, VCardParameters parameters) throws OXException {
        VCardParameters vCardParameters = getParametersOrDefault(parameters);
        VCard original = null != originalVCard ? parseFirstVCard(originalVCard, null) : null;
        VCard exportedVCard = mapper.exportContact(contact, original, vCardParameters);
        return exportVCards(Collections.singletonList(exportedVCard), vCardParameters).getClosingStream();
    }

    @Override
    public Contact importVCard(InputStream vCard, Contact contact, VCardParameters parameters) throws OXException {
        VCardParameters vCardParameters = getParametersOrDefault(parameters);
        VCard parsedVCard = parseFirstVCard(vCard, vCardParameters);
        return mapper.importVCard(parsedVCard, contact, vCardParameters);
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

    private VCard parseFirstVCard(InputStream inputStream, VCardParameters parameters) throws OXException {
        final long maxSize = parameters.getMaxVCardSize();
        if (0 < maxSize) {
            inputStream = new SizeAwareInputStream(inputStream) {

                @Override
                public void size(long size) throws IOException {
                    if (size > maxSize) {
                        throw new IOException(VCardExceptionCodes.MAXIMUM_SIZE_EXCEEDED.create(Long.valueOf(maxSize)));
                    }
                }
            };
        }
        List<OXException> warnings = parameters.getWarnings();
        try {
            List<List<String>> parserWarnings = new ArrayList<List<String>>();
            ParserChainTextReader chain = Ezvcard.parse(inputStream).warnings(parserWarnings);
            VCard vCard = chain.first();
            if (null != parserWarnings && 0 < parserWarnings.size()) {
                List<String> warningsList = parserWarnings.get(0);
                for (String warning : warningsList) {
                    LOG.debug("Parser warning: {}", warning);
                    if (null != warnings) {
                        warnings.add(VCardExceptionCodes.PARSER_ERROR.create(warning));
                    }
                }
            }
            if (null == vCard) {
                throw VCardExceptionCodes.NO_VCARD.create();
            }
            if (false == parameters.isSkipValidation()) {
                ValidationWarnings validationWarnings = vCard.validate(null != vCard.getVersion() ? vCard.getVersion() : getVCardVersion(parameters.getVersion()));
                if (null != validationWarnings && false == validationWarnings.isEmpty()) {
                    LOG.debug("Validation warnings: {}", validationWarnings);
                    if (null != warnings) {
                        for (Entry<VCardProperty, List<Warning>> entry : validationWarnings) {
                            VCardProperty property = entry.getKey();
                            List<Warning> propViolations = entry.getValue();
                            String propertyName = null != property ? property.getClass().getSimpleName() : "";
                            if (null != propViolations && 0 < propViolations.size()) {
                                for (Warning propViolation : propViolations) {
                                    warnings.add(VCardExceptionCodes.VALIDATION_FAILED.create(propertyName, propViolation.getMessage(), propViolation.getCode()));
                                }
                            }
                        }
                    }
                }
            }
            return vCard;
        } catch (IOException e) {
            if (null != e.getCause() && OXException.class.isInstance(e.getCause())) {
                throw (OXException) e.getCause();
            }
            throw VCardExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    private ThresholdFileHolder exportVCards(List<VCard> vCards, VCardParameters parameters) throws OXException {
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        WriterChainText writerChain = Ezvcard.write(vCards);
        applyOptions(getParametersOrDefault(parameters), writerChain);
        try {
            writerChain.go(fileHolder.asOutputStream());
            return fileHolder;
        } catch (IOException e) {
            Streams.close(fileHolder);
            throw VCardExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static WriterChainText applyOptions(VCardParameters parameters, WriterChainText writerChain) {
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