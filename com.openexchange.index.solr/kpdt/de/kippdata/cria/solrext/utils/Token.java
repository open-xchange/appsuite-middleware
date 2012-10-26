package de.kippdata.cria.solrext.utils;
/*
 *CVS information:
 * $Revision: 1.1 $
 * $Author: sven $
 * $Date: 2012/06/11 19:32:46 $
 * $State: Exp $
 * $Name:  $
 */

public class Token {
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
