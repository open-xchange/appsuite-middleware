package de.kippdata.cria.solrext;
/*
 *CVS information:
 * $Revision: 1.1 $
 * $Author: sven $
 * $Date: 2012/04/24 15:06:33 $
 * $State: Exp $
 * $Name:  $
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;


/**
 * 
 * This is a simple <code>SearchComponent</code> that modifies the query command
 * to be sent to the <code>IndexSearcher</code>. It adds a <code>Query</code> that
 * is used as a filter.
 * The component has to be put as a first-component into the component list of a
 * request handler.
 * <p>
 * The code is currently in a pre-production state.
 */
public class MultiFieldSearchModifier extends SearchComponent {
  private final String DESCRIPTION = "Experimental SearchComponent that modifies a query";
  private final String REF = "$Revision: 1.1 $";
  private Map<String,String> fieldMappings = new HashMap<String,String>();

  private final static Logger log = Logger.getLogger(MultiFieldSearchModifier.class);


  @SuppressWarnings("rawtypes")
  public void init(NamedList args) {
    for (int i=0; i < args.size(); i++) {
      fieldMappings.put(args.getName(i),args.getVal(i).toString());
      if (log.isDebugEnabled()) log.debug("[init]: Found argument " + args.getName(i) + " with value " + args.getVal(i));
    }
  }

  public String getDescription() {
    return DESCRIPTION;
  }

  public String getSource() {
    return null;
  }

  public String getSourceId() {
    return null;
  }

  public String getVersion() {
    return REF;
  }

  public void prepare(ResponseBuilder rb) throws IOException {
    log.debug("[prepare]: nothing to be done");
  }


  public void process(ResponseBuilder rb) throws IOException {
    log.debug("[process]: Start");
    log.debug("[process]: query before = " + rb.getQuery().toString());

    SolrQueryRequest request = rb.req;
    SolrParams params = request.getParams();
    String defType = params.get(QueryParsing.DEFTYPE);
    defType = defType==null ? QParserPlugin.DEFAULT_QTYPE : defType;

    log.debug("[process]: defType = " + defType);

    StringBuffer queryBuffer = new StringBuffer();

    for (String s : fieldMappings.keySet()) {
      queryBuffer.append(fieldMappings.get(s) + ":\"" + params.get(s) + "\" ");
      log.debug("[process]: Mapped " + s + " to \'" + fieldMappings.get(s) + "\' with value " + params.get(s));
    }

    String queryString = queryBuffer.toString();

    try {
      log.debug("[process]: query = " + queryString);
      QParser parser = QParser.getParser(queryString, defType, request);
      rb.setQuery( parser.getQuery());
      rb.setQparser(parser);
      log.debug("[process]: Processed query = " + parser.getQuery().toString());
    }
    catch (ParseException e) {
      log.warn("[process]: ERROR building query");
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
    } 

    log.debug("[process]: Done.");
  }
}
