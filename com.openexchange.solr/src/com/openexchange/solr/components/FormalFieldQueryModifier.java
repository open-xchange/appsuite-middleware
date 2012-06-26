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

package com.openexchange.solr.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;


/**
 * {@link FormalFieldQueryModifier}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FormalFieldQueryModifier extends SearchComponent {
    private final String QUERY = "q";
    private final String DESCRIPTION = "SearchComponent that modifies a query";
    private final String REF = "$Revision: 1.6 $";
    private Map<String,String> rawMap = new HashMap<String,String>();
    private FormalFieldParser fieldParser;
    
    private static final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(FormalFieldQueryModifier.class));


    @SuppressWarnings("rawtypes")
    public void init(NamedList args) {
      for (int i=0; i < args.size(); i++) {
        rawMap.put(args.getName(i),args.getVal(i).toString());
        if (log.isDebugEnabled()) log.debug("[init]: Found argument " + args.getName(i) + " with value " + args.getVal(i));
        fieldParser = new FormalFieldParser(rawMap);
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

    public void process(ResponseBuilder rb) throws IOException { }


    public void prepare(ResponseBuilder rb) throws IOException {
      log.debug("[prepare]: Start");

      SolrParams params = rb.req.getParams();

      String queryString;
      if (params.get(QUERY) != null) {
        log.debug("[prepare]: Transforming formal fields for query " + params.get(QUERY));
        queryString = fieldParser.parse(params.get(QUERY));
        log.debug("[prepare]: Transformed query is \'" + queryString + "\'");
      }
      else {
        queryString = "*:* AND NOT (*:*)";
      }

      rb.setQueryString(queryString);
      log.debug("[prepare]: Done.");
    }
    
    private static final class FormalFieldParser {
        private Map<String,List<String>> formalFieldMap;


        public FormalFieldParser(Map<String,String> mapping) {
          formalFieldMap = this.createMapping(mapping);
        }

      /**
       * The method creates a list of terms from a string of the following form:
       * <br>
       * <code>field1:term1 field1:"term2 AND term3" field2:term4</code>
       * <br>
       * The members of the list are 
       * @param source
       * @return
       */
        public String parse(String source) {
          StringBuffer b = new StringBuffer();
          log.debug("[parse]: Parsing \'" + source + "\'");
          List<Token> termList = this.split(source);

          for (Token t : termList) {
            String s = t.getTerm().split(":")[0];
            if (formalFieldMap.containsKey(s)) {
              b.append(this.parseTerm(t.getTerm(),formalFieldMap.get(s)) + " ");
            }
            else {
              if (!(s.equalsIgnoreCase("AND") || s.equalsIgnoreCase("OR") || s.equalsIgnoreCase("NOT")))
                log.debug("[parse]: No mapping for field \'" + s + "\'");
              b.append(s + " ");
            }
          }
          return b.toString().trim();
        }


      // ============================ private methods below ================================= //

        private List<Token> split(String source) {
          List<Token> termList = new ArrayList<Token>();
          boolean protectQuote = false;
          boolean escaped = false;

          StringBuilder b = new StringBuilder();
          log.trace("[split]: Starting to split \'" + source + "\'");

          for (char ch : source.toCharArray()) {
            log.trace("[split]: \'" + ch + "\'");
            switch (ch) {
              case '\\':
                escaped = true;
                log.trace("[split]: set escaped to TRUE");
                b.append(ch);
                break;

              case '"':
                if (!escaped && !protectQuote) {
                  protectQuote = true;
                  log.trace("[split]: set protectQuote to TRUE");
                }
                else {
                  if (!escaped && protectQuote) {
                    protectQuote = false;
                    log.trace("[split]: set protectQuote to FALSE");
                  }
                }
                if (escaped) {
                  escaped = false;
                  log.trace("[split]: set escape to FALSE");
                }
                b.append(ch);
                break;

              case ' ':
                if (! protectQuote) {
                  Token t = new Token(b.toString().trim(),TokenTypes.GENERIC);
                  termList.add(t);
                  log.debug("[split]: add term \'" + t.getTerm() + "\'");
                  b.delete(0,b.length());
                  protectQuote = false;
                  escaped = false;
                }
                else {
                  b.append(ch);
                }
                break;

              case '(':
                if (! protectQuote) {
                  Token t = new Token(b.toString().trim(),TokenTypes.GENERIC);
                  if (b.toString().trim().length() > 0) { 
                    termList.add(t);
                    log.debug("[split]: add term \'" + t.getTerm() + "\'");
                  }
                  termList.add(new Token("(",TokenTypes.BRACKET));
                  log.debug("[split]: add term \'(\'");

                  b.delete(0,b.length());
                  protectQuote = false;
                  escaped = false;
                }
                else {
                  b.append(ch);
                }
                break;

              case ')':
                if (! protectQuote) {
                  Token t = new Token(b.toString().trim(),TokenTypes.GENERIC);
                  if (b.toString().trim().length() > 0) { 
                    termList.add(t);
                    log.debug("[split]: add term \'" + t.getTerm() + "\'");
                  }
                  termList.add(new Token(")",TokenTypes.BRACKET));
                  log.debug("[split]: add term \')\'");
                  b.delete(0,b.length());
                  protectQuote = false;
                  escaped = false;
                }
                else {
                  b.append(ch);
                }
                break;

              default:
                b.append(ch);
            }
          }

          Token t = new Token(b.toString().trim(),TokenTypes.GENERIC);
          termList.add(t);
          log.trace("[split]: add term \'" + t.getTerm() + "\'");
          return termList;
        }


        private String parseTerm(String term, List<String> replacements) throws RuntimeException {
          log.debug("[parseTerm]: Receiving search string \'" + term + "\'");
          String searchTerm;

          if (term.contains(":")) {
            searchTerm = term.substring(term.indexOf(":") + 1, term.length());
          }
          else throw new RuntimeException();

          StringBuffer b = new StringBuffer();
          b.append("(");
          for (String s : replacements) {
            b.append(s + ":" + searchTerm + " ");
          }
          if (log.isDebugEnabled()) log.debug("[parseTerm]: result is \'" + b.toString().trim() + ")" + "\'");
          return b.toString().trim() + ")";
        }


        private Map<String,List<String>> createMapping(Map<String,String> rawMap) {
          if (log.isTraceEnabled()) {
            for (String s : rawMap.keySet()) log.debug("[createMapping]: Received " + s + ":" + rawMap.get(s));
          }
          Map<String,List<String>> fieldMappings = new HashMap<String,List<String>>();

          for (String formalField : rawMap.keySet()) {
            List<String> schemaFields = new ArrayList<String>();
            String mf = rawMap.get(formalField);
            if (mf.contains("{")) {
              log.trace("[createMapping]: is multi-field");
              String prefix = mf.substring(0, mf.indexOf("{"));
              String[] locales = mf.substring(mf.indexOf("{")+1, mf.length()-1).split(",");
              for (String s : locales) {
                schemaFields.add(prefix + "_" + s.trim());
              }
            }
            else {
              log.trace("[createMapping]: is standard field");
              schemaFields.add(mf);
            }
            fieldMappings.put(formalField, schemaFields);
          }

          if (log.isTraceEnabled()) {
            for (String s : fieldMappings.keySet()) {
              StringBuffer b = new StringBuffer();
              b.append(s + ":");
              for (String t : fieldMappings.get(s)) b.append(t + " ");
              log.trace("[createMapping]: result is " + b.toString());
            }
          }
          return fieldMappings;
        }
    }
    
    private static final class Token {
        private String term;
        private TokenTypes type;

        public Token(String term, TokenTypes type) {
          this.term = term;
          this.type = type;
        }

        public String getTerm() {
          return this.term;
        }

        public boolean isOperator() {
          return (this.type == TokenTypes.OPERATOR);
        }

        public boolean isBracket() {
          return (this.type == TokenTypes.BRACKET);
        }

        public boolean isPayload() {
          return (this.type == TokenTypes.PAYLOAD);
        }

        public boolean isQuote() {
          return (this.type == TokenTypes.QUOTE);
        }
    }
    
    private static enum TokenTypes {
        OPERATOR, PAYLOAD, BRACKET,QUOTE,GENERIC
    }
}
