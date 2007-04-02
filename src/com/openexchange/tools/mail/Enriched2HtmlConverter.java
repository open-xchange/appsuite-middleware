package com.openexchange.tools.mail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class Enriched2HtmlConverter {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(Enriched2HtmlConverter.class);
	
	private static enum ParaType {
		
		PT_LEFT(1), PT_RIGHT(2), PT_IN(3), PT_OUT(4);
		
		public final int type;
		
		private ParaType(final int type) {
			this.type = type;
		}
		
	}
	
	private String font;
	
	private String color;
	
	private int size;
	
	private int excerpt;
	
	private int paraType;
	
	private int paramCounter;
	
	private int nofill;
	
	private int newlineCounter;
	
	private boolean colorParam;
	
	private boolean fontParam;
	
	private boolean paraParam;
	
	public Enriched2HtmlConverter() {
		super();
	}
	
	public static final String convertEnriched2Html(final String enrichedText) {
		return new Enriched2HtmlConverter().convert(enrichedText);
	}
	
	private final void reset() {
		font = null;
		color = null;
		size = 0;
		excerpt = 0;
		paraType = 0;
		paramCounter = 0;
		nofill = 0;
		newlineCounter = 0;
		colorParam = false;
		fontParam = false;
		paraParam = false;
	}
	
	private static final String HTML_LOWERTHAN = "&lt;";
	
	private static final String HTML_GREATERTHAN = "&gt;";
	
	private static final String HTML_AMP = "&amp;";
	
	public final String convert(final String enrichedText) {
		try {
			reset();
			final StringBuilder sb = new StringBuilder(enrichedText.length());
			StringReader input = null;
			try {
				input = new StringReader(enrichedText/*.replaceAll("\r?\n", "<br>")*/);
				int c = -1;
				final StringBuilder tokenBuilder = new StringBuilder();
				/*
				 * Convert until '-1' (EOF) is reached
				 */
				c = input.read();
				while (c != -1) {
					boolean readNext = true;
					if (c == '<') {
						if (newlineCounter == 1) {
							sb.append(' ');
						}
			            newlineCounter = 0;
						final int next = input.read();
						if (next == '<') {
							if (paramCounter <= 0) {
								sb.append(HTML_LOWERTHAN);
							}
						} else {
							/*
							 * A starting tag
							 */
							handleEnrichedTag(getTagName(next, input), sb);
						}
					} else {
			            if (paramCounter > 0) {
			            	if (c != -1) {
			            		tokenBuilder.append(Character.toLowerCase((char) c));
			            	}
			            	while ((c = input.read()) != -1 && c != '<') { 
			            		tokenBuilder.append(Character.toLowerCase((char) c));
			                }
			            	if (c == -1) {
								break;
							}
			            	final String token = tokenBuilder.toString();
			            	tokenBuilder.setLength(0);
			            	readNext = false;
							if (colorParam) {
								color = token;
								openFont(size, font, color, sb);
							} else if (fontParam) {
								font = token;
								openFont(size, font, color, sb);
							} else if (paraParam) {
								if ("left".equals(token)) {
									paraType = ParaType.PT_LEFT.type;
									sb.append("<dl><dd>");
								} else if ("right".equals(token)) {
									paraType = ParaType.PT_RIGHT.type;
								} else if ("in".equals(token)) {
									paraType = ParaType.PT_IN.type;
								} else if ("out".equals(token)) {
									paraType = ParaType.PT_OUT.type;
								}
							}
			            } else if (c == '\n' && nofill <= 0) {
			                if (++newlineCounter > 1) {
			                	sb.append("<br>\n");
			         		   if (excerpt > 0) { 
			         			  sb.append(HTML_GREATERTHAN).append(' ');
			         		   }
			         		}
			            } else if (c == '>') {
			            	sb.append(HTML_GREATERTHAN);
			    	    } else if (c == '&') {
			    	    	sb.append(HTML_AMP);
			            } else {
		            	   if (newlineCounter == 1) {
		            		   sb.append(' ');
		            	   }
		                   newlineCounter = 0;
		                   sb.append((char) c);
			    	    }
					}
					if (readNext) {
						c = input.read();
					}
				}
				sb.append('\n');
			} finally {
				if (input != null) {
					input.close();
				}
			}
			return sb.toString();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return enrichedText;
		}
	}
	
	
	private static final String ENRICHED_BOLD = "bold";
	
	private static final String ENRICHED_ITALIC = "italic";
	
	private static final String ENRICHED_FIXED = "fixed";
	
	private static final String ENRICHED_UNDERLINE = "underline";
	
	private static final String ENRICHED_CENTER = "center";
	
	private static final String ENRICHED_FLUSHLEFT = "flushleft";
	
	private static final String ENRICHED_BIGGER = "bigger";
	
	private static final String ENRICHED_SMALLER = "smaller";
	
	private static final String ENRICHED_INDENT = "indent";
	
	private static final String ENRICHED_EXCERPT = "excerpt";
	
	private static final String ENRICHED_COLOR = "color";
	
	private static final String ENRICHED_FONTFAMILY = "fontfamily";
	
	private static final String ENRICHED_PARAINDENT = "paraindent";
	
	private static final String ENRICHED_FLUSHBOTH = "flushboth";
	
	private static final String ENRICHED_INDENTRIGHT = "indentright";
	
	private static final String ENRICHED_PARAM = "param";
	
	private static final String ENRICHED_NOFILL = "nofill";
	
	private static final String ENRICHED_PREFIX = "x-tad-";

	
	private final void handleEnrichedTag(final String tagArg, final StringBuilder sb) {
		String tag;
		final boolean isEndTag = tagArg.charAt(0) == '/';
		if (isEndTag) {
			tag = tagArg.substring(1);
		} else {
			tag = tagArg;
		}
		if (tag.startsWith(ENRICHED_PREFIX)) {
			tag = tag.substring(6);
		}
		/*
		 * Map
		 */
		if (ENRICHED_BOLD.equals(tag)) {
			mapSimpleTag("b", isEndTag, sb);
		} else if (ENRICHED_ITALIC.equals(tag)) {
			mapSimpleTag("i", isEndTag, sb);
		} else if (ENRICHED_FIXED.equals(tag)) {
			mapSimpleTag("tt", isEndTag, sb);
		} else if (ENRICHED_UNDERLINE.equals(tag)) {
			mapSimpleTag("u", isEndTag, sb);
		} else if (ENRICHED_CENTER.equals(tag)) {
			mapSimpleTag("center", isEndTag, sb);
		} else if (ENRICHED_FLUSHLEFT.equals(tag)) {
			sb.append(isEndTag ? "</div>" : "<div align=left>");
		} else if (ENRICHED_BIGGER.equals(tag)) {
			size = isEndTag ? size - 2 : size + 2;
			if (isEndTag) {
				closeFont(sb);
			} else {
				openFont(size, font, color, sb);
			}
		} else if (ENRICHED_SMALLER.equals(tag)) {
			size = isEndTag ? size + 2 : size - 2;
			if (isEndTag) {
				closeFont(sb);
			} else {
				openFont(size, font, color, sb);
			}
		} else if (ENRICHED_INDENT.equals(tag)) {
			sb.append(isEndTag ? "</dl>" : "<dl><dd>");
		} else if (ENRICHED_EXCERPT.equals(tag)) {
			excerpt = isEndTag ? excerpt - 1 : excerpt + 1;
		} else if (ENRICHED_COLOR.equals(tag)) {
			if (isEndTag) {
				colorParam = false;
				color = null;
				closeFont(sb);
			} else {
				colorParam = true;
			}
		} else if (ENRICHED_FONTFAMILY.equals(tag)) {
			if (isEndTag) {
				fontParam = false;
				font = null;
				closeFont(sb);
			} else {
				fontParam = true;
			}
		} else if (ENRICHED_PARAINDENT.equals(tag)) {
			if (isEndTag) {
				paraParam = false;
				if (paraType == ParaType.PT_LEFT.type) {
					sb.append("</dl>");
				}
				paraType = 0;
			} else {
				paraParam = true;
			}
		} else if (ENRICHED_FLUSHBOTH.equals(tag)) {
			mapSimpleTag("div", isEndTag, sb);
		} else if (ENRICHED_INDENTRIGHT.equals(tag)) {
			sb.append(isEndTag ? "</dl>" : "<dl><dd>");
		} else if (ENRICHED_PARAM.equals(tag)) {
			paramCounter = isEndTag ? paramCounter - 1 : paramCounter + 1;
		} else if (ENRICHED_NOFILL.equals(tag)) {
			if (isEndTag) {
				nofill--;
				sb.append("</pre>\n");
			} else {
				nofill++;
				sb.append("<pre>\n");
			}
		} else {
			/*
			 * Unknown tag
			 */
			sb.append('?').append(isEndTag ? "&lt;/" : "&lt;").append(tag).append("&gt;");
		}
	}
	
	private static final void mapSimpleTag(final String rpl, final boolean isEndTag, final StringBuilder sb) {
		sb.append('<');
		if (isEndTag) {
			sb.append('/');
		}
		sb.append(rpl).append('>');
	}
	
	private static final void openFont(final int size, final String font, final String color, final StringBuilder sb)
	{
	  sb.append("<font");
	  if (size > 0) {
		  sb.append(" size=+").append(size);
	  } else if (size < 0) {
		  sb.append(" size=-").append(size);
	  }
	  if (color != null) {
		  sb.append(" color=\"").append(color).append('"');
	  }
	  if (font != null) {
		  sb.append(" face=\"").append(font).append('"');
	  }
	  sb.append('>');
	}
	
	private static final void closeFont(final StringBuilder sb) {
		sb.append("</font>");
	}
	
	private static final String getTagName(final int firstChar, final Reader r) throws IOException {
		/*
		 * Assume last read character was '<'
		 */
		final StringBuilder result = new StringBuilder();
		result.append(Character.toLowerCase((char) firstChar));
		int level = 1;
		while (level > 0) {
			final int c = r.read();
			if (c == -1) {
				break; // EOF
			}
			if (c == '<') {
				level++;
			} else if (c == '>') {
				level--;
			} else {
				result.append(Character.toLowerCase((char) c));
			}
		}
		return result.toString();
	}

}
