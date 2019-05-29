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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.importexport.impl;

import com.openexchange.exception.OXException;
import com.openexchange.importexport.Exporter;
import com.openexchange.importexport.Format;
import com.openexchange.importexport.ImportExportService;
import com.openexchange.importexport.Importer;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.exporters.CSVContactExporter;
import com.openexchange.importexport.exporters.ICalExporter;
import com.openexchange.importexport.exporters.VCardExporter;
import com.openexchange.importexport.importers.CSVContactImporter;
import com.openexchange.importexport.importers.ICalImporter;
import com.openexchange.importexport.importers.VCardImporter;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ImportExportServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class ImportExportServiceImpl implements ImportExportService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ImportExportServiceImpl}.
     */
    public ImportExportServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public Exporter getExporter(Format format) throws OXException {
        switch (format) {
            case CSV:
                return new CSVContactExporter();
            case ICAL:
                return new ICalExporter();
            case OUTLOOK_CSV:
                return new CSVContactExporter();
            case VCARD:
                return new VCardExporter();
            default:
                break;
        }

        throw ImportExportExceptionCodes.NO_SUCH_EXPORTER.create(format.getConstantName());
    }

    @Override
    public Importer getImporter(Format format) throws OXException {
        switch (format) {
            case CSV:
                return new CSVContactImporter(services);
            case ICAL:
                return new ICalImporter(services);
            case OUTLOOK_CSV:
                return new CSVContactImporter(services);
            case VCARD:
                return new VCardImporter(services);
            default:
                break;
        }

        throw ImportExportExceptionCodes.NO_SUCH_IMPORTER.create(format.getConstantName());
    }

}
