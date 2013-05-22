/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication, visit http://creativecommons.org/licenses/publicdomain/ or
 * send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 *
 * Check HCard.java's top-level comment for more information.
 */
package org.microformats.hCard;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;
import com.openexchange.subscribe.microformats.objectparser.OXMFVisitor;

/**
 * Parses HCards in sources (URLs, files, or HTML directly) into HCard objects.
 *
 * @version 0.2
 * @author Reinier Zwitserloot
 */
public class HCardParser {

    private static List<Map<String, String>> oxmfData;

    public static List<Map<String, String>> getOXMFData(){
        return oxmfData;
    }

    /**
	 * Supply with any number of urls, filenames, or straight html sources on the command line, and you'll get the hCards printed out in
	 * human readable form.
	 */
	public static void main(final String[] args) throws ParserException {
		if ( args.length == 0 ) {
			System.out.println("Supply any number of resources as parameters to parse them for hCards and print the results.");
			System.out.println("A resource can be straight HTML, a file name, or a URL.");
		} else {
            display(args);
        }
	}

	/**
	 * Supply with any source. This method will start parsing the source. Once 1 full hCard has been parsed, it is immediately returned.
	 * If no hCard can be found, it returns <code>null</code>.
	 *
	 * @param source A URL, <i>or</i>a block of HTML, <i>or</i> a filename.
	 * @throws ParserException If the HTML is so mangled it can't be parsed anymore.
	 */
	public static HCard parseOne(final String source) throws ParserException {
		return parseOne(source, null);
	}

	/**
	 * Supply with any source. This method will start parsing the source. Once 1 full hCard has been parsed, it is immediately returned.
	 * If no hCard can be found, it returns <code>null</code>.
	 *
	 * @param source A URL, <i>or</i>a block of HTML, <i>or</i> a filename.
	 * @param defaultBase a URL to use as document base. This is neccessary only when passing direct HTML as a <i>source</i>.
	 * @throws ParserException If the HTML is so mangled it can't be parsed anymore.
	 */
	public static HCard parseOne(final String source, final URI defaultBase) throws ParserException {
		final List<HCard> parsed = parse(source, defaultBase, 1);
		if ( parsed.isEmpty() ) {
            return null;
        } else {
            return parsed.get(0);
        }
	}

	/**
	 * Supply with any source. This method will start parsing the source in full, then returns any and all hCards it found.
	 * If no hCard can be found, it returns an empty list; this method never returns <code>null</code>.
	 *
	 * @param source A URL, <i>or</i>a block of HTML, <i>or</i> a filename.
	 * @throws ParserException If the HTML is so mangled it can't be parsed anymore.
	 */
	public static List<HCard> parseMany(final String source) throws ParserException {
		return parseMany(source, null);
	}

	/**
	 * Supply with any source. This method will start parsing the source in full, then returns any and all hCards it found.
	 * If no hCard can be found, it returns an empty list; this method never returns <code>null</code>.
	 *
	 * @param source A URL, <i>or</i>a block of HTML, <i>or</i> a filename.
	 * @param defaultBase a URL to use as document base. This is neccessary only when passing direct HTML as a <i>source</i>.
	 * @throws ParserException If the HTML is so mangled it can't be parsed anymore.
	 */
	public static List<HCard> parseMany(final String source, final URI defaultBase) throws ParserException {
		return parse(source, defaultBase, 0);
	}

	private static List<HCard> parse(final String source, URI defaultBase, final int limit) throws ParserException {
	    oxmfData = null;
		final Parser p = new Parser(source);
		String anchor = null;
		try {
			final String url = p.getURL();
			if ( url != null ) {
                try {
                	defaultBase = new URI(url);
                	final int idx = url.indexOf('#');
                	if ( idx > -1 ) {
                        anchor = url.substring(idx +1);
                    }
                } catch ( final Exception ignore ) {}
            }
			final OXMFVisitor v = new OXMFVisitor(limit, defaultBase);
			if ( anchor != null ) {
                v.ignoreUntilAnchor(anchor);
            }

			try {
				p.visitAllNodesWith(v);
			} catch ( final HCardFound ignore ) {}

			oxmfData = v.getOXMFElements();
			return v.parsedHCards();
		} finally {
			try {
				p.getConnection().getInputStream().close();
				p.getConnection().getOutputStream().close();
			} catch ( final Throwable ignore ) {}
		}
	}

	/**
	 * Creates a new Visitor that can be used to parse a single hCard from a stream of HTML. You may want to use this to parse hCard-formatted subcontent
	 * in e.g. an hReview block.
	 * <p>
	 * If you just need to parse hCards out of a block of HTML, do not use this method; use {@link #parseOne(String) parseOne} or
	 * {@link #parseMany(String) parseMany} instead.
	 * <p>
	 * The idea behind this method is that you have your own Visitor, which, until this one is 'done', redirects
	 * <code>NodeVisitor.visitTag</code>, <code>NodeVisitor.visitEndTag</code>, and <code>NodeVisitor.visitStringNode</code>
	 * to the visitor returned by this method. Make sure that you call visitTag on this visitor with the tag with the <code>class="vcard"</code> on it,
	 * or this Visitor won't find any hCards.
	 * <p>
	 * When 1 complete hCard has been parsed (the last consumed tag will be the tag that closes the tag with the class="vcard"), the visitEndTag method
	 * will throw an HCardParser.HCardFound exception. This is the signal that the HCardVisitor's <code>parsedHCards</code> method will return a list with
	 * exactly 1 HCard object in it. Thus, catch it, handle the hCard, and from that point onwards do not redirect the visit methods to this visitor anymore.
	 * From there you should continue parsing your hReview, or whichever other content is wrapped around the hCard. Then, when you encounter another tag with
	 * <code>class="vcard"</code>, get a new visitor by calling this method again, and repeat the process.
	 *
	 * @param defaultBase URLs in the hCard should be normalized to an absolute URL. Supply any URI that serves as a base. Passing in <code>null</code>
	 *    is legal but will result in relative URIs in the hCard.
	 */
	public static HCardVisitor getHCardParsingVisitor(final URI defaultBase) {
		return new HCardVisitor(1, defaultBase);
	}

	private static void display(final String... urls) throws ParserException {
		for ( final String url : urls ) {
			System.out.printf("For URL %s: ", url);
			final List<HCard> cards = parseMany(url);
			if ( cards.size() == 0 ) {
                System.out.println("no hCards found.");
            } else if ( cards.size() == 1 ) {
                System.out.printf("\n%s\n", cards.get(0));
            } else {
				System.out.printf("%d hCards found:\n", cards.size());
				boolean first = true;
				for ( final HCard card : cards ) {
					if ( first ) {
                        first = false;
                    } else {
                        System.out.println("--------------------------------");
                    }
					System.out.println(card);
				}
			}
		}
	}

	/**
	 * Thrown by the the Visitors returned by the {@link #getHCardParsingVisitor(URI)} method to indicate it has parsed a complete HCard.
	 */
	public static class HCardFound extends RuntimeException {
		private static final long serialVersionUID = -5391059983274499720L;
	}

	static class Result {
		final HCardProperty property;
		String value;
		URI uri;
		String rel;
		List<Result> subResults;

		Result(final HCardProperty property) {
			this.property = property;
		}
	}

	private static class Marker {
		boolean valueExcerpt = false;
		boolean isBeingExcerpted = false;
		final List<HCardProperty> properties;
		final List<Result> results = new ArrayList<Result>();
		final List<String> presetValues;
		final StringBuilder sb;
		final String tagName;
		int count = 1;

		Marker(final Collection<HCardProperty> properties, final Collection<String> presetValues, final boolean buildText, final String tagName, final String rel) {
			this.properties = Collections.unmodifiableList(new ArrayList<HCardProperty>(properties));
			this.presetValues = Collections.unmodifiableList(new ArrayList<String>(presetValues));
			this.sb = buildText ? new StringBuilder() : null;
			this.tagName = tagName;
			for ( final HCardProperty property : this.properties ) {
				final Result r = new Result(property);
				r.rel = rel;
				results.add(r);
			}
		}

		void toValueExcerptMode(final String value) {
			if ( !isBeingExcerpted ) {
                sb.setLength(0);
            }
			isBeingExcerpted = true;
			sb.append(value);
		}
	}

	private static class Base {
		public final URI uri;
		public final String tagName;
		public final boolean isXML;
		public int depth;

		private Base(final URI uri, final String tagName, final boolean isXML) {
			this.uri = uri;
			this.tagName = tagName;
			this.isXML = isXML;
			this.depth = 1;
		}

		private static URI convert(final String url) {
			try {
				final URI uri = new URI(url);
				if ( !uri.isAbsolute() ) {
                    return null;
                }
				return uri;
			} catch ( final Exception ignore ) {}
			return null;
		}

		public static Base forXML(final String tagName, final String url) {
			return new Base(convert(url), tagName, true);
		}

		public static Base forHTML(final String url) {
			return new Base(convert(url), "BASE", false);
		}
	}

	/**
	 * Returned by the {@link #getHCardParsingVisitor} method.
	 *
	 * Like a normal NodeVisitor, except this one also has a {@link HCardVisitor#parsedHCards()} method that returns the hCards found so far.
	 */
	public static class HCardVisitor extends NodeVisitor {
		private int toParse;
		private final URI defaultBase;
		private final LinkedList<Base> baseStack = new LinkedList<Base>();
		private final List<HCard> hCards = new ArrayList<HCard>();
		private String hCardTagName = null;
		private int hCardTagNameLevel = 0;
		private boolean inPre = false;
		private boolean inDel = false;
		private String ignoreUntilAnchor;

		private final LinkedList<Marker> stack = new LinkedList<Marker>();
		private final List<Result> results = new ArrayList<Result>();
		private final List<Result> openResults = new ArrayList<Result>();

		private static final List<String> CONVERT_OPEN_TAG_TO_SOFT_BREAK = Collections.unmodifiableList(Arrays.asList(
				"DIV", "DL", "DT", "TABLE", "TBODY", "THEAD", "TFOOT", "TR", "CAPTION"
		));

		private static final List<String> CONVERT_CLOSE_TAG_TO_SOFT_BREAK = Collections.unmodifiableList(Arrays.asList(
				"DIV", "DL", "LI", "DD", "TABLE", "TBODY", "THEAD", "TFOOT", "TR", "CAPTION"
		));

		/**
		 * Returns a list of hCards parsed so far. Never returns <code>null</code> but will return an empty list if it hasn't finished
		 * parsing anything yet.
		 */
		public List<HCard> parsedHCards() {
			return hCards;
		}

		protected HCardVisitor(final int toParse, final URI defaultBase) {
			this.defaultBase = defaultBase;
			if ( toParse < 1 ) {
                this.toParse = -1;
            } else {
                this.toParse = toParse;
            }
		}

		protected void ignoreUntilAnchor(final String anchor) {
			this.ignoreUntilAnchor = anchor;
		}

		public @Override void visitEndTag(final Tag tag) {
			final String name = tag.getTagName();

			Base top = baseStack.peek();
			if ( top != null && top.isXML && name.equals(top.tagName) && --top.depth == 0 ) {
                baseStack.poll();
            }

			if ( "BASE".equals(name) ) {
				top = baseStack.peek();
				if ( top != null && !top.isXML ) {
                    baseStack.poll();
                }
			}

			if ( "PRE".equals(name) ) {
                inPre = false;
            }
			if ( "DEL".equals(name) ) {
                inDel = false;
            }

			for ( final Marker m : stack ) {
                if ( m.tagName.equals(name) ) {
                    m.count--;
                }
            }

			if ( hCardTagName != null && hCardTagName.equals(name) ) {
				if ( --hCardTagNameLevel == 0 ) {
					finalizeHCard();
					return;
				}
			}

			//The next step handles hCards with crappy formatting where certain class-carrying tags aren't closed.
			//Anytime a given tag is closed, all tags 'below it' in the hierarchy (more specific) are also closed.
			int count = stack.size();
			for ( final ListIterator<Marker> i = stack.listIterator(stack.size()) ; i.hasPrevious() ; ) {
				final Marker m = i.previous();
				if ( m.count == 0 ) {
                    break;
                } else {
                    count--;
                }
			}

			if ( count > 0 ) {
				while ( count-- > 0 ) {
                    finalizeTag();
                }
				return;
			}

			final Appendable sb = getBuildersOnStack();
			if ( sb != null ) {
				if ( "DT".equals(name) ) {
                    sb.append('\n');
                } else if ( name.matches("^H[123456]$") ) {
                    sb.appendSoftLineBreak().append('\n');
                } else if ( "P".equals(name) ) {
                    sb.appendSoftLineBreak().append('\n');
                } else if ( "Q".equals(name) ) {
                    sb.append('"');
                } else if ( "SUB".equals(name) ) {
                    sb.append(')');
                } else if ( "SUP".equals(name) ) {
                    sb.append(']');
                } else if ( "TD".equals(name) || "TH".equals(name) ) {
                    sb.append(" \t");
                } else if ( CONVERT_CLOSE_TAG_TO_SOFT_BREAK.contains(name) ) {
                    sb.appendSoftLineBreak();
                }
			}
		}

		public @Override void visitStringNode(final Text textNode) {
			if ( inDel || hCardTagName == null ) {
                return;
            }
			final Appendable sb = getBuildersOnStack();

			if ( sb != null ) {
				String text = textNode.getText();
				if ( textNode.getText().equals("\n\t\t") ) {
                    System.err.println("WHUH");
                }

				if ( text.length() == 0 ) {
                    return;
                }

				if ( !inPre ) {
					text = collapseWhitespace(text);
					if ( text.length() > 0 && text.charAt(0) == ' ' ) {
						sb.appendSoftSpace();
						text = text.substring(1);
					}
				}

				sb.append(text);
			}
		}

		public @Override void visitTag(final Tag tag) {
			final String name = tag.getTagName();
			final String hClass = tag.getAttribute("class");

			if ( "BASE".equals(name) && !tag.isEndTag() ) {
                baseStack.addFirst(Base.forHTML(tag.getAttribute("href")));
            } else {
				final String baseUrl = tag.getAttribute("xml:base");
				if ( baseUrl != null ) {
                    baseStack.addFirst(Base.forXML(name, baseUrl));
                } else {
					final Base top = baseStack.peek();
					if ( top != null && top.isXML && !tag.isEndTag() && name.equals(top.tagName) ) {
                        top.depth++;
                    }
				}
			}

			for ( final Marker m : stack ) {
                if ( m.tagName.equals(name) ) {
                    m.count++;
                }
            }

			if ( "PRE".equals(name) && !tag.isEndTag() ) {
                inPre = true;
            }
			if ( "DEL".equals(name) && !tag.isEndTag() ) {
                inDel = true;
            }

			if ( ignoreUntilAnchor != null && "A".equals(name) && ignoreUntilAnchor.equals(tag.getAttribute("name")) ) {
                ignoreUntilAnchor = null;
            }

			if ( inDel || ignoreUntilAnchor != null ) {
                return;
            }

			if ( hCardTagName == null ) {
				if ( "vcard".equalsIgnoreCase(hClass) ) {
					hCardTagName = name;
					hCardTagNameLevel = 1;
				}
				return;
			}

			if ( hCardTagName.equals(name) ) {
                hCardTagNameLevel++;
            }

			final List<HCardProperty> properties = new LinkedList<HCardProperty>();

			boolean isValueClass = false;

			if ( hClass != null ) {
                for ( final String hClassElement : hClass.split("\\s+") ) {
                	if ( "value".equals(hClassElement) ) {
                        isValueClass = true;
                    }
                	final HCardProperty property = HCardProperty.fromClassAttribute(stack.isEmpty() ? null : stack.peek().properties, hClassElement);
                	if ( property != null ) {
                        properties.add(property);
                    }
                }
            }

			int pSize = properties.size();
			boolean treatAsEndTag = false;

			if ( pSize == 0 && isValueClass && !stack.isEmpty() ) {
				properties.addAll(stack.peek().properties);
				pSize = properties.size();
			} else {
                isValueClass = false;
            }

			if ( pSize > 0 ) {
				final List<String> values = new LinkedList<String>();
				if ( "ABBR".equals(name) ) {
					final String abbrTitle = tag.getAttribute("title");
					if ( abbrTitle != null ) {
						for ( int i = 0 ; i < pSize ; i++ ) {
                            values.add(abbrTitle);
                        }
						treatAsEndTag = true;
					}
				} else if ( "BR".equals(name) || "HR".equals(name) ) {
					for ( int i = 0 ; i < pSize ; i++ ) {
                        values.add("");
                    }
					treatAsEndTag = true;
				} else if ( "AREA".equals(name) ) {
					treatAsEndTag = true;
					final String areaHref = tag.getAttribute("href");
					String areaAlt = tag.getAttribute("alt");
					if ( areaAlt == null ) {
                        areaAlt = "";
                    }

					for ( final HCardProperty property : properties ) {
						if ( areaHref != null && property.isUrl() ) {
                            values.add(areaHref);
                        } else {
                            values.add(areaAlt);
                        }
					}
				}
				else if ( "A".equals(name) ) {
					final String aHref = tag.getAttribute("href");
					if ( aHref != null ) {
                        for ( final HCardProperty property : properties ) {
                            values.add(property.isUrl() ? aHref : null);
                        }
                    }
				} else if ( "OBJECT".equals(name) ) {
					final String objectData = tag.getAttribute("data");
					if ( objectData != null ) {
						final String url = objectData;
						for ( final HCardProperty property : properties ) {
                            values.add(property.isUrl() ? url : null);
                        }
					}
				} else if ( "IMG".equals(name) ) {
					treatAsEndTag = true;
					final String imgSrc = tag.getAttribute("src");
					String imgAlt = tag.getAttribute("alt");
					if ( imgAlt == null ) {
                        imgAlt = "";
                    }

					for ( final HCardProperty property : properties ) {
						if ( imgSrc != null && property.isUrl() ) {
                            values.add(imgSrc);
                        } else {
                            values.add(imgAlt);
                        }
					}
				}

				boolean buildText = false;
				if ( values.isEmpty() ) {
                    for ( int i = 0 ; i < pSize ; i++ ) {
                        values.add(null);
                    }
                }
				for ( final String value : values ) {
                    if ( value == null ) {
                    	buildText = true;
                    	break;
                    }
                }

				final Marker marker = new Marker(properties, values, buildText, tag.getTagName(), tag.getAttribute("rel"));
				if ( isValueClass ) {
                    marker.valueExcerpt = true;
                } else {
                    openResults.addAll(marker.results);
                }
				stack.addFirst(marker);

				if ( treatAsEndTag || tag.isEndTag() || tag.getAttributeEx("/") != null ) {
                    finalizeTag();
                }
			} else {
				final Appendable sb = getBuildersOnStack();
				if ( sb != null ) {
					if ( "BR".equals(name) ) {
                        sb.append('\n');
                    } else if ( "LI".equals(name) ) {
                        sb.appendSoftLineBreak().append(" * ");
                    } else if ( "DD".equals(name) ) {
                        sb.appendSoftLineBreak().append("  ");
                    } else if ( name.matches("^H[123456]$") ) {
                        sb.appendSoftLineBreak().append('\n');
                    } else if ( "P".equals(name) ) {
                        sb.appendSoftLineBreak().append('\n');
                    } else if ( "Q".equals(name) ) {
                        sb.append('"');
                    } else if ( "SUB".equals(name) ) {
                        sb.append('(');
                    } else if ( "SUP".equals(name) ) {
                        sb.append('[');
                    } else if ( CONVERT_OPEN_TAG_TO_SOFT_BREAK.contains(name) ) {
                        sb.appendSoftLineBreak();
                    }
				}
			}
		}

		private URI fixUrl(final String relativeUrl) {
			try {
				URI base = defaultBase;
				for ( final Base potentialBase : baseStack ) {
                    if ( potentialBase.uri != null && potentialBase.uri.isAbsolute() ) {
                    	base = potentialBase.uri;
                    	break;
                    }
                }

				if ( base == null || !base.isAbsolute() ) {
                    return new URI(relativeUrl);
                } else {
                    return base.resolve(relativeUrl);
                }
			} catch ( final URISyntaxException e ) {
				return null;
			} catch ( final Exception e ) {
				try {
					return new URI(relativeUrl);
				} catch ( final URISyntaxException e2 ) {
					return null;
				}
			}
		}

		static class Appendable {
			private final List<StringBuilder> builders;

			Appendable(final List<StringBuilder> builders) {
				this.builders = builders;
			}

			Appendable append(final String s) {
				for ( final StringBuilder sb : builders ) {
                    sb.append(s);
                }
				return this;
			}

			Appendable append(final char c) {
				for ( final StringBuilder sb : builders ) {
                    sb.append(c);
                }
				return this;
			}

			Appendable appendSoftSpace() {
				for ( final StringBuilder sb : builders ) {
                    if ( sb.length() > 0 && !isWhitespace(sb.charAt(sb.length() -1)) ) {
                        sb.append(' ');
                    }
                }
				return this;
			}

			Appendable appendSoftLineBreak() {
				for ( final StringBuilder sb : builders ) {
                    if ( sb.length() == 0 || sb.charAt(sb.length() -1) != '\n' ) {
                        sb.append('\n');
                    }
                }
				return this;
			}
		}

		private Appendable getBuildersOnStack() {
			final List<StringBuilder> list = new ArrayList<StringBuilder>();
			for ( final Marker m : stack ) {
                if ( m.sb != null && !m.isBeingExcerpted ) {
                    list.add(m.sb);
                }
            }
			if ( list.size() == 0 ) {
                return null;
            } else {
                return new Appendable(list);
            }
		}

		private void finalizeTag() {
			final Marker marker = stack.poll();
			if ( marker == null ) {
                return;
            }

			if ( marker.valueExcerpt ) {
				stack.peek().toValueExcerptMode(marker.sb.toString());
				return;
			}

			final Iterator<HCardProperty> properties = marker.properties.iterator();
			final Iterator<String> presets = marker.presetValues.iterator();
			final Iterator<Result> tagResults = marker.results.iterator();
			final String text = marker.sb == null ? "" : marker.sb.toString();

			openResults.removeAll(marker.results);

			while ( properties.hasNext() ) {
				final HCardProperty property = properties.next();
				final Result result = tagResults.next();
				String value = presets.next();
				if ( value == null ) {
                    value = text.trim();
                }
				result.value = value.trim();
				result.uri = property.isUrl() ? fixUrl(result.value.trim()) : null;

				if ( property.isTopLevel() ) {
                    results.add(result);
                } else {
					final HCardProperty parent = property.parent();
					for ( int i = openResults.size()-1 ; i >= 0 ; i-- ) {
						final Result pRes = openResults.get(i);
						if ( pRes.property == parent ) {
							if ( pRes.subResults == null ) {
                                pRes.subResults = new ArrayList<Result>();
                            }
							pRes.subResults.add(result);
							break;
						}
					}
				}
			}
		}

		private void finalizeHCard() {
			while ( !stack.isEmpty() ) {
                finalizeTag();
            }

			hCardTagName = null;
			hCards.add(HCardCreator.createHCard(results));
			this.hCardTagNameLevel = 0;
			this.results.clear();
			this.stack.clear();
			if ( hCards.size() == toParse ) {
                throw new HCardFound();
            }
		}
	}

	private static String collapseWhitespace(final String text) {
		return text.replaceAll("\\s+", " ");
	}

	/**
     * High speed test for whitespace!  Faster than the java one (from some testing).
     *
     * @return <code>true</code> if the indicated character is whitespace; otherwise <code>false</code>
     */
    private static boolean isWhitespace(final char c) {
        switch (c) {
            case 9:  //'unicode: 0009
            case 10: //'unicode: 000A'
            case 11: //'unicode: 000B'
            case 12: //'unicode: 000C'
            case 13: //'unicode: 000D'
            case 28: //'unicode: 001C'
            case 29: //'unicode: 001D'
            case 30: //'unicode: 001E'
            case 31: //'unicode: 001F'
            case ' ': // Space
                //case Character.SPACE_SEPARATOR:
                //case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                return true;
            default:
                return false;
        }
    }
}
