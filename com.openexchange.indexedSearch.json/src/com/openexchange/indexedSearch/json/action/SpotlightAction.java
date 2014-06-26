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

package com.openexchange.indexedSearch.json.action;

import java.util.Set;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.Type;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.SearchHandlers;
import com.openexchange.indexedSearch.json.IndexAJAXRequest;
import com.openexchange.indexedSearch.json.IndexedSearchExceptionCodes;
import com.openexchange.indexedSearch.json.ResultConverters;
import com.openexchange.indexedSearch.json.converter.SearchResult;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SpotlightAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@Action(method = RequestMethod.GET, name = "spotlight", description = "Search for mails in a spotlight-like manner.", parameters = {
    @Parameter(
        name = "term",
        description = "The term to search for. Use * for wildcard searches.",
        optional = false,
        type = Type.STRING
    ),
    @Parameter(
        name = "field",
        description = "The field to search in. Possible fields are 'from', 'to' and 'subject'.",
        optional = false,
        type =
        Type.STRING
    ),
    @Parameter(
        name = "columns",
        description = "A comma-separated list of columns to return for found mails. " +
                "If not specified, all columns are returned. Column identifiers for mails are " +
                "defined in http://oxpedia.org/wiki/index.php?title=HTTP_API#DetailedMailData.",
        optional = true,
        type = Type.ARRAY
    ),
    @Parameter(
        name = "offset",
        description = "The offset for documents to return within the list of all found documents. Use for pagination. Default: 0.",
        optional = true,
        type = Type.NUMBER
    ),
    @Parameter(
        name = "length",
        description = "The max. number of returned documents. Use for pagination. Default: 10.",
        optional = true,
        type = Type.NUMBER
    )
})
public class SpotlightAction extends AbstractIndexAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpotlightAction.class);

    /**
     * Initializes a new {@link SpotlightAction}.
     * @param services
     * @param registry
     */
    public SpotlightAction(ServiceLookup services, ResultConverters registry) {
        super(services, registry);
    }

    @Override
    protected AJAXRequestResult perform(IndexAJAXRequest req) throws OXException {
        long startTime = System.currentTimeMillis();
        ServerSession session = req.getSession();
        String term = req.checkParameter("term");
        String field = req.checkParameter("field");
        int offset = req.optInt("offset") == IndexAJAXRequest.NOT_FOUND ? 0 : req.optInt("offset");
        int length = req.optInt("length") == IndexAJAXRequest.NOT_FOUND ? 10 : req.optInt("length");
        int[] columns = req.optIntArray(AJAXServlet.PARAMETER_COLUMNS);
        Set<MailIndexField> indexFields = null;
        if (columns == null) {
            indexFields= MailIndexField.getFor(MailField.getFields(columns));
        }

        final IndexFacadeService indexFacade = getService(IndexFacadeService.class);
        final SearchHandler searchHandler;
        if ("from".equals(field)) {
            searchHandler = SearchHandlers.namedHandler("spotlight_from");
        } else if ("to".equals(field)) {
            searchHandler = SearchHandlers.namedHandler("spotlight_to");
        } else if ("subject".equals(field)) {
            searchHandler = SearchHandlers.namedHandler("spotlight_subject");
        } else {
            throw IndexedSearchExceptionCodes.UNKNOWN_HANDLER.create(indexFields);
        }

        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, session);
        QueryParameters params = new QueryParameters.Builder()
            .setHandler(searchHandler)
            .setSearchTerm(term)
            .setOffset(offset)
            .setLength(length)
            .build();

        IndexResult<MailMessage> result = indexAccess.query(params, null);
        long diff = System.currentTimeMillis() - startTime;
        LOG.debug("Spotlight search duration for field '{}': {}ms.", field, diff);

        SearchResult<MailMessage> searchResult = new SearchResult<MailMessage>(Types.EMAIL);
        searchResult.setNumFound(result.getNumFound());
        searchResult.setDuration(diff);
        searchResult.setDocuments(result.getResults());
        searchResult.setFields(columns);
        return new AJAXRequestResult(searchResult, "searchResult");
    }

    @Override
    public String getAction() {
        return "spotlight";
    }

}
