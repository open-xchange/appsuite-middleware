package de.kippdata.cria.solrext.utils;
/*
 *CVS information:
 * $Revision: 1.4 $
 * $Author: sven $
 * $Date: 2012/06/11 22:38:00 $
 * $State: Exp $
 * $Name:  $
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;

public class FormalFieldParser {
  private Map<String,List<String>> formalFieldMap;
  private static Log log = com.openexchange.log.Log.loggerFor(FormalFieldParser.class);


  public FormalFieldParser(Map<String,String> mapping) {
    formalFieldMap = this.createMapping(mapping);
  }

  public FormalFieldParser(Map<String,List<String>> mapping, boolean mapReady) {
    formalFieldMap = mapping;
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
