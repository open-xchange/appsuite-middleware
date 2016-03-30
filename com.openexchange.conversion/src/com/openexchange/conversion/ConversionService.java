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

package com.openexchange.conversion;

import java.io.InputStream;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link ConversionService} - The conversion service which offers look-up methods for {@link DataSource data sources} and
 * {@link DataHandler data handlers}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface ConversionService {

    /**
     * Gets the data source associated with specified identifier.
     * <p>
     * The identifier should correspond to java's package naming; e.g.<br>
     * <code>&quot;my.path.to.specific.datasource&quot;</code>
     *
     * @param identifier The identifier string
     * @return The data source associated with specified identifier or <code>null</code>.
     */
    public DataSource getDataSource(String identifier);

    /**
     * Gets the data handler associated with specified identifier.
     * <p>
     * The identifier should correspond to java's package naming; e.g.<br>
     * <code>&quot;my.path.to.specific.datahandler&quot;</code>
     *
     * @param identifier The identifier string
     * @return The data handler associated with specified identifier or <code>null</code>.
     */
    public DataHandler getDataHandler(String identifier);

    /**
     * Looks-up and checks appropriate {@link DataSource data source} and {@link DataHandler data handler}. Then the
     * {@link DataHandler#processData(Object, DataArguments, Session)} method is triggered with
     * {@link DataSource#getData(Class, DataArguments, Session)} as input invoked with a matching supported type.
     *
     * @param dataSourceIdentifier The data source identifier
     * @param dataSourceArguments The data source arguments
     * @param dataHandlerIdentifier The data handler identifier
     * @param dataHandlerArguments The data handler arguments
     * @param session The session providing needed user data
     * @return The resulting object from data handler
     * @throws OXException If conversion fails
     */
    public Object convert(String dataSourceIdentifier, DataArguments dataSourceArguments, String dataHandlerIdentifier, DataArguments dataHandlerArguments, Session session) throws OXException;

    /**
     * Looks-up and checks appropriate {@link DataHandler data handler}. Then the
     * {@link DataHandler#processData(Object, DataArguments, Session)} method is triggered with specified input stream as input provided
     * that {@link DataHandler data handler} supports {@link InputStream} class.
     *
     * @param inputStream The input stream
     * @param dataHandlerIdentifier The data handler identifier
     * @param dataHandlerArguments The data handler arguments
     * @param session The session providing needed user data
     * @return The resulting object from data handler
     * @throws OXException If conversion fails
     */
    public Object convert(InputStream inputStream, String dataHandlerIdentifier, DataArguments dataHandlerArguments, Session session) throws OXException;

}
