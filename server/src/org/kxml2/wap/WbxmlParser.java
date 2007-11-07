/* Copyright (c) 2002,2003,2004 Stefan Haustein, Oberhausen, Rhld., Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The  above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE. */

// Contributors: Bjorn Aadland, Chris Bartley, Nicola Fankhauser, 
//               Victor Havin,  Christian Kurzke, Bogdan Onoiu,
//               Jain Sanjay, David Santoro.

package org.kxml2.wap;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


public class WbxmlParser implements XmlPullParser {

    public static final int WAP_EXTENSION = 64;

    static final private String UNEXPECTED_EOF =
        "Unexpected EOF";
    static final private String ILLEGAL_TYPE =
        "Wrong event type";

    private InputStream in;

	private int TAG_TABLE;
	private int ATTR_START_TABLE = 1;
	private int ATTR_VALUE_TABLE = 2;

    private String[] attrStartTable;
    private String[] attrValueTable;
    private String[] tagTable;
    private String stringTable;
    private boolean processNsp;

    private int depth;
    private String[] elementStack = new String[16];
    private String[] nspStack = new String[8];
    private int[] nspCounts = new int[4];

    private int attributeCount;
    private String[] attributes = new String[16];
	private int nextId = -2;

	private List<String[]> tables = new ArrayList<String[]>();

    int version;
    int publicIdentifierId;
    int charSet;

    //    StartTag current;
    //    ParseEvent next;

    private String prefix;
    private String namespace;
    private String name;
    private String text;
    //	private String encoding;
    private Object wapExtensionData;
    private int wapExtensionCode;
    
    private int type;
	private int codePage;

    private boolean degenerated;
    private boolean isWhitespace;

    public boolean getFeature(String feature) {
        if (XmlPullParser
            .FEATURE_PROCESS_NAMESPACES
            .equals(feature)) {
			return processNsp;
		}
		return false;
    }

    public String getInputEncoding() {
        // should return someting depending on charSet here!!!!!
        return null;
    }

    public void defineEntityReplacementText(
    		final String entity,
    		final String value)
        throws XmlPullParserException {

        // just ignore, has no effect
    }

    public Object getProperty(final String property) {
        return null;
    }

    public int getNamespaceCount(final int depth) {
        if (depth > this.depth) {
			throw new IndexOutOfBoundsException();
		}
        return nspCounts[depth];
    }

    public String getNamespacePrefix(final int pos) {
        return nspStack[pos << 1];
    }

    public String getNamespaceUri(final int pos) {
        return nspStack[(pos << 1) + 1];
    }

    public String getNamespace(final String prefix) {

        if ("xml".equals(prefix)) {
			return "http://www.w3.org/XML/1998/namespace";
		}
        if ("xmlns".equals(prefix)) {
			return "http://www.w3.org/2000/xmlns/";
		}

        for (int i = (getNamespaceCount(depth) << 1) - 2;
            i >= 0;
            i -= 2) {
            if (prefix == null) {
                if (nspStack[i] == null) {
					return nspStack[i + 1];
				}
            }
            else if (prefix.equals(nspStack[i])) {
				return nspStack[i + 1];
			}
        }
        return null;
    }

    public int getDepth() {
        return depth;
    }

    public String getPositionDescription() {

    	final StringBuilder buf =
            new StringBuilder(
                type < TYPES.length ? TYPES[type] : "unknown");
        buf.append(' ');

        if (type == START_TAG || type == END_TAG) {
            if (degenerated) {
				buf.append("(empty) ");
			}
            buf.append('<');
            if (type == END_TAG) {
				buf.append('/');
			}

            if (prefix != null) {
				buf.append('{').append(namespace).append('}').append(prefix).append(':');
			}
            buf.append(name);

            final int cnt = attributeCount << 2;
            for (int i = 0; i < cnt; i += 4) {
                buf.append(' ');
                if (attributes[i + 1] != null) {
					buf.append('{').append(attributes[i]).append('}').append(attributes[i + 1]).append(':');
				}
                buf.append(attributes[i + 2]).append("='").append(attributes[i + 3]).append('\'');
            }

            buf.append('>');
        }
        else if (type == IGNORABLE_WHITESPACE) {
			buf.append("");
		} else if (type != TEXT) {
			buf.append(getText());
		} else if (isWhitespace) {
			buf.append("(whitespace)");
		} else {
            String text = getText();
            if (text.length() > 16) {
				text = text.substring(0, 16) + "...";
			}
            buf.append(text);
        }

        return buf.toString();
    }

    public int getLineNumber() {
        return -1;
    }

    public int getColumnNumber() {
        return -1;
    }

    public boolean isWhitespace()
        throws XmlPullParserException {
        if (type != TEXT
            && type != IGNORABLE_WHITESPACE
            && type != CDSECT) {
			exception(ILLEGAL_TYPE);
		}
        return isWhitespace;
    }

    public String getText() {
        return text;
    }

    public char[] getTextCharacters(int[] poslen) {
        if (type >= TEXT) {
            poslen[0] = 0;
            poslen[1] = text.length();
            final char[] buf = new char[text.length()];
            text.getChars(0, text.length(), buf, 0);
            return buf;
        }

        poslen[0] = -1;
        poslen[1] = -1;
        return null;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isEmptyElementTag()
        throws XmlPullParserException {
        if (type != START_TAG) {
			exception(ILLEGAL_TYPE);
		}
        return degenerated;
    }

    public int getAttributeCount() {
        return attributeCount;
    }

    public String getAttributeType(final int index) {
        return "CDATA";
    }

    public boolean isAttributeDefault(final int index) {
        return false;
    }

    public String getAttributeNamespace(final int index) {
        if (index >= attributeCount) {
			throw new IndexOutOfBoundsException();
		}
        return attributes[index << 2];
    }

    public String getAttributeName(final int index) {
        if (index >= attributeCount) {
			throw new IndexOutOfBoundsException();
		}
        return attributes[(index << 2) + 2];
    }

    public String getAttributePrefix(final int index) {
        if (index >= attributeCount) {
			throw new IndexOutOfBoundsException();
		}
        return attributes[(index << 2) + 1];
    }

    public String getAttributeValue(final int index) {
        if (index >= attributeCount) {
			throw new IndexOutOfBoundsException();
		}
        return attributes[(index << 2) + 3];
    }

    public String getAttributeValue(
    		final String namespace,
    		final String name) {

        for (int i = (attributeCount << 2) - 4;
            i >= 0;
            i -= 4) {
            if (attributes[i + 2].equals(name)
                && (namespace == null
                    || attributes[i].equals(namespace))) {
				return attributes[i + 3];
			}
        }

        return null;
    }

    public int getEventType() throws XmlPullParserException {
        return type;
    }

    public int next() throws XmlPullParserException, IOException {

        isWhitespace = true;
        int minType = 9999;

        while (true) {

        	final String save = text;

            nextImpl();

            if (type < minType) {
				minType = type;
			}

			if (minType > CDSECT) {
				continue; // no "real" event so far
			}

			if (minType >= TEXT) {  // text, see if accumulate
				
				if (save != null) {
					text = text == null ? save : save + text;
				}
				
				switch(peekId()) {
					case Wbxml.ENTITY:
					case Wbxml.STR_I:
					case Wbxml.LITERAL:
					case Wbxml.LITERAL_C:
					case Wbxml.LITERAL_A:
					case Wbxml.LITERAL_AC: continue;
					default:
				}
			}	
            break; 
        }

        type = minType;

        if (type > TEXT) {
			type = TEXT;
		}

        return type;
    }


    public int nextToken() throws XmlPullParserException, IOException {

        isWhitespace = true;
        nextImpl();
        return type;
    }



    public int nextTag() throws XmlPullParserException, IOException {

        next();
        if (type == TEXT && isWhitespace) {
			next();
		}

        if (type != END_TAG && type != START_TAG) {
			exception("unexpected type");
		}

        return type;
    }


    public String nextText() throws XmlPullParserException, IOException {
        if (type != START_TAG) {
			exception("precondition: START_TAG");
		}

        next();

        String result;

        if (type == TEXT) {
            result = getText();
            next();
        } else {
			result = "";
		}

        if (type != END_TAG) {
			exception("END_TAG expected");
		}

        return result;
    }


    public void require(final int type, final String namespace, final String name)
        throws XmlPullParserException, IOException {

        if (type != this.type
            || (namespace != null && !namespace.equals(getNamespace()))
            || (name != null && !name.equals(getName()))) {
			exception(
                "expected: " + TYPES[type] + " {" + namespace + '}' + name);
		}
    }


	public void setInput(final Reader reader) throws XmlPullParserException {
		exception("InputStream required");
	}

    public void setInput(final InputStream in, final String enc)
        throws XmlPullParserException {

        this.in = in;

        try {
            version = readByte();
            publicIdentifierId = readInt();

            if (publicIdentifierId == 0) {
				readInt();
			}

            charSet = readInt(); // skip charset

            final int strTabSize = readInt();

            final StringBuilder buf = new StringBuilder(strTabSize);

            for (int i = 0; i < strTabSize; i++) {
				buf.append((char) readByte());
			}
	        stringTable = buf.toString();
	        
	   //     System.out.println("String table: "+stringTable);
	    //    System.out.println("String table length: "+stringTable.length());
	        
	        selectPage(0, true);
			selectPage(0, false);
        }
        catch (IOException e) {
            exception("Illegal input format");
        }
    }

    public void setFeature(final String feature, final boolean value)
        throws XmlPullParserException {
        if (XmlPullParser.FEATURE_PROCESS_NAMESPACES.equals(feature)) {
			processNsp = value;
		} else {
			exception("unsupported feature: " + feature);
		}
    }

    public void setProperty(final String property, final Object value)
        throws XmlPullParserException {
        throw new XmlPullParserException("unsupported property: " + property);
    }

    // ---------------------- private / internal methods

    private final boolean adjustNsp()
        throws XmlPullParserException {

        boolean any = false;

        for (int i = 0; i < attributeCount << 2; i += 4) {
            // * 4 - 4; i >= 0; i -= 4) {

            String attrName = attributes[i + 2];
            final int cut = attrName.indexOf(':');
            String prefix;

            if (cut != -1) {
                prefix = attrName.substring(0, cut);
                attrName = attrName.substring(cut + 1);
            }
            else if (attrName.equals("xmlns")) {
                prefix = attrName;
                attrName = null;
            } else {
				continue;
			}

            if (!prefix.equals("xmlns")) {
                any = true;
            }
            else {
            	final int j = (nspCounts[depth]++) << 1;

                nspStack = ensureCapacity(nspStack, j + 2);
                nspStack[j] = attrName;
                nspStack[j + 1] = attributes[i + 3];

                if (attrName != null
                    && attributes[i + 3].equals("")) {
					exception("illegal empty namespace");
				}

                //  prefixMap = new PrefixMap (prefixMap, attrName, attr.getValue ());

                //System.out.println (prefixMap);

                System.arraycopy(
                    attributes,
                    i + 4,
                    attributes,
                    i,
                    ((--attributeCount) << 2) - i);

                i -= 4;
            }
        }

        if (any) {
            for (int i = (attributeCount << 2) - 4;
                i >= 0;
                i -= 4) {

                String attrName = attributes[i + 2];
                final int cut = attrName.indexOf(':');

                if (cut == 0) {
					throw new RuntimeException(
                        "illegal attribute name: "
                            + attrName
                            + " at "
                            + this);
				} else if (cut != -1) {
					final String attrPrefix =
                        attrName.substring(0, cut);

                    attrName = attrName.substring(cut + 1);

                    final String attrNs = getNamespace(attrPrefix);

                    if (attrNs == null) {
						throw new RuntimeException(
                            "Undefined Prefix: "
                                + attrPrefix
                                + " in "
                                + this);
					}

                    attributes[i] = attrNs;
                    attributes[i + 1] = attrPrefix;
                    attributes[i + 2] = attrName;

                    for (int j = (attributeCount << 2) - 4;
                        j > i;
                        j -= 4) {
						if (attrName.equals(attributes[j + 2])
                            && attrNs.equals(attributes[j])) {
							exception(
                                "Duplicate Attribute: {"
                                    + attrNs
                                    + '}'
                                    + attrName);
						}
					}
                }
            }
        }

        final int cut = name.indexOf(':');

        if (cut == 0) {
			exception("illegal tag name: " + name);
		} else if (cut != -1) {
            prefix = name.substring(0, cut);
            name = name.substring(cut + 1);
        }

        this.namespace = getNamespace(prefix);

        if (this.namespace == null) {
            if (prefix != null) {
				exception("undefined prefix: " + prefix);
			}
            this.namespace = NO_NAMESPACE;
        }

        return any;
    }

	private final void setTable(final int page, final int type, final String[] table) {
		if(stringTable != null){
			throw new RuntimeException("setXxxTable must be called before setInput!");
		}
		while(tables.size() < 3*page +3){
			tables.add(null);
		}
		tables.add(page*3+type, table);
	}
		
		



    private final void exception(final String desc)
        throws XmlPullParserException {
        throw new XmlPullParserException(desc, this, null);
    }


	private void selectPage(final int nr, final boolean tags) throws XmlPullParserException{
		if(tables.size() == 0 && nr == 0) {
			return;
		}
		
		if(nr*3 > tables.size()) {
			exception("Code Page "+nr+" undefined!");
		}
		
		if(tags) {
			tagTable = tables.get(nr * 3 + TAG_TABLE);
		} else {
			attrStartTable = tables.get(nr * 3 + ATTR_START_TABLE);
			attrValueTable = tables.get(nr * 3 + ATTR_VALUE_TABLE);
		}
	}

    private final void nextImpl()
        throws IOException, XmlPullParserException {

        if (type == END_TAG) {
            depth--;
        }

        if (degenerated) {
            type = XmlPullParser.END_TAG;
            degenerated = false;
            return;
        }

        text = null;
        prefix = null;
        name = null;

        int id = peekId ();
        while(id == Wbxml.SWITCH_PAGE){
        	nextId = -2;
			selectPage(readByte(), true);
			id = peekId();        	
        }
        nextId = -2;

        switch (id) {
            case -1 :
                type = XmlPullParser.END_DOCUMENT;
                break;

            case Wbxml.END :
                {
                	final int sp = (depth - 1) << 2;

                    type = END_TAG;
                    namespace = elementStack[sp];
                    prefix = elementStack[sp + 1];
                    name = elementStack[sp + 2];
                }
                break;

            case Wbxml.ENTITY :
                {
                    type = ENTITY_REF;
                    final char c = (char) readInt();
                    text = "" + c;
                    name = "#" + ((int) c);
                }

                break;

            case Wbxml.STR_I :
                type = TEXT;
                text = readStrI();
                break;

            case Wbxml.EXT_I_0 :
            case Wbxml.EXT_I_1 :
            case Wbxml.EXT_I_2 :
            case Wbxml.EXT_T_0 :
            case Wbxml.EXT_T_1 :
            case Wbxml.EXT_T_2 :
            case Wbxml.EXT_0 :
            case Wbxml.EXT_1 :
            case Wbxml.EXT_2 :
            case Wbxml.OPAQUE :
                parseWapExtension(id);
                break;

            case Wbxml.PI :
                throw new RuntimeException("PI curr. not supp.");
                // readPI;
                // break;

            case Wbxml.STR_T :
                {
                    type = TEXT;
                    final int pos = readInt();
                    final int end = stringTable.indexOf('\0', pos);
                    text = stringTable.substring(pos, end);
                }
                break;

            default :
                parseElement(id);
        }
        //        }
        //      while (next == null);

        //        return next;
    }

    

    public void parseWapExtension(final int id)
        throws IOException, XmlPullParserException {

        type = WAP_EXTENSION;
        wapExtensionCode = id;

        switch (id) {
            case Wbxml.EXT_I_0 :
            case Wbxml.EXT_I_1 :
            case Wbxml.EXT_I_2 :
                wapExtensionData = readStrI();
                break;

            case Wbxml.EXT_T_0 :
            case Wbxml.EXT_T_1 :
            case Wbxml.EXT_T_2 :
                wapExtensionData = Integer.valueOf(readInt());
                break;

            case Wbxml.EXT_0 :
            case Wbxml.EXT_1 :
            case Wbxml.EXT_2 :
                break;

            case Wbxml.OPAQUE :
                {
                	final int len = readInt();
                    byte[] buf = new byte[len];

                    for (int i = 0;
                        i < len;
                        i++) {
						buf[i] = (byte) readByte();
					}

                    wapExtensionData = buf;
                } // case OPAQUE
            	break;
            	
            default:
                exception("illegal id: "+id);
        } // SWITCH
    }

    public void readAttr() throws IOException, XmlPullParserException {

        int id = readByte();
        int i = 0;

        while (id != 1) {

        	while(id == Wbxml.SWITCH_PAGE){
                selectPage(readByte(), false);
                id = readByte();
            } 
        	
            String name = resolveId(attrStartTable, id);
            StringBuilder value;

            final int cut = name.indexOf('=');

            if (cut == -1) {
				value = new StringBuilder();
			} else {
                value =
                    new StringBuilder(name.substring(cut + 1));
                name = name.substring(0, cut);
            }

            id = readByte();
            while (id > 128
            	|| id == Wbxml.SWITCH_PAGE
                || id == Wbxml.ENTITY
                || id == Wbxml.STR_I
                || id == Wbxml.STR_T
                || (id >= Wbxml.EXT_I_0 && id <= Wbxml.EXT_I_2)
                || (id >= Wbxml.EXT_T_0 && id <= Wbxml.EXT_T_2)) {

                switch (id) {
					case Wbxml.SWITCH_PAGE :
						selectPage(readByte(), false);
						break;
                	
                    case Wbxml.ENTITY :
                        value.append((char) readInt());
                        break;

                    case Wbxml.STR_I :
                        value.append(readStrI());
                        break;

                    case Wbxml.EXT_I_0 :
                    case Wbxml.EXT_I_1 :
                    case Wbxml.EXT_I_2 :
                    case Wbxml.EXT_T_0 :
                    case Wbxml.EXT_T_1 :
                    case Wbxml.EXT_T_2 :
                    case Wbxml.EXT_0 :
                    case Wbxml.EXT_1 :
                    case Wbxml.EXT_2 :
                    case Wbxml.OPAQUE :

                        throw new RuntimeException("wap extension in attr not supported yet");

                        /*
                                                ParseEvent e = parseWapExtension(id);
                                                if (!(e.getType() != Xml.TEXT
                                                    && e.getType() != Xml.WHITESPACE))
                                                    throw new RuntimeException("parse WapExtension must return Text Event in order to work inside Attributes!");
                        
                                                value.append(e.getText());
                        
                                                //value.append (handleExtension (id)); // skip EXT in ATTR
                                                //break;
                        */

                    case Wbxml.STR_T :
                        value.append(readStrT());
                        break;

                    default :
                        value.append(
                            resolveId(attrValueTable, id));
                }

                id = readByte();
            }

            attributes = ensureCapacity(attributes, i + 4);

            attributes[i++] = "";
            attributes[i++] = null;
            attributes[i++] = name;
            attributes[i++] = value.toString();
            
            attributeCount++;
        }
    }

	private int peekId () throws IOException {
		if (nextId == -2) {
			nextId = in.read ();
		}
		return nextId;
	}
		
		


    String resolveId(final String[] tab, final int id) throws IOException {
    	final int idx = (id & 0x07f) - 5;
        if (idx == -1) {
			return readStrT();
		}
        if (idx < 0
            || tab == null
            || idx >= tab.length
            || tab[idx] == null) {
			throw new IOException("id " + id + " undef.");
		}

        return tab[idx];
    }

    void parseElement(final int id)
        throws IOException, XmlPullParserException {

		type = START_TAG;
        name = resolveId(tagTable, id & 0x03f);

		attributeCount = 0;
        if ((id & 128) != 0) {
            readAttr();
        }

        degenerated = (id & 64) == 0;

        final int sp = depth++ << 2;

        // transfer to element stack

        elementStack = ensureCapacity(elementStack, sp + 4);
        elementStack[sp + 3] = name;

        if (depth >= nspCounts.length) {
        	final int[] bigger = new int[depth + 4];
             System.arraycopy(nspCounts, 0, bigger, 0, nspCounts.length);
             nspCounts = bigger;
        }
        
        nspCounts[depth] = nspCounts[depth - 1]; 

        for (int i = attributeCount - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (getAttributeName(i)
                    .equals(getAttributeName(j))) {
					exception(
                        "Duplicate Attribute: "
                            + getAttributeName(i));
				}
            }
        }

        if (processNsp) {
			adjustNsp();
		} else {
			namespace = "";
		}

        elementStack[sp] = namespace;
        elementStack[sp + 1] = prefix;
        elementStack[sp + 2] = name;

    }

    private final String[] ensureCapacity(
    		final String[] arr,
    		final int required) {
        if (arr.length >= required) {
			return arr;
		}
        final String[] bigger = new String[required + 16];
        System.arraycopy(arr, 0, bigger, 0, arr.length);
        return bigger;
    }

    int readByte() throws IOException {
    	final int i = in.read();
        if (i == -1) {
			throw new IOException("Unexpected EOF");
		}
        return i;
    }

    int readInt() throws IOException {
        int result = 0;
        int i;

        do {
            i = readByte();
            result = (result << 7) | (i & 0x7f);
        }
        while ((i & 0x80) != 0);

        return result;
    }

    String readStrI() throws IOException {
    	final StringBuilder buf = new StringBuilder();
        boolean wsp = true;
        while (true) {
        	final int i = in.read();
            if (i == -1) {
				throw new IOException("Unexpected EOF");
			}
            if (i == 0) {
				break;
			}
            if (i > 32) {
				wsp = false;
			}
            buf.append((char) i);
        }
        isWhitespace = wsp;
        return buf.toString();
    }

    String readStrT() throws IOException {
    	final int pos = readInt();
    	final int end = stringTable.indexOf('\0', pos);

        return stringTable.substring(pos, end);
    }

    /** 
     * Sets the tag table for a given page.
     * The first string in the array defines tag 5, the second tag 6 etc.
     */

    public void setTagTable(final int page, final String[] table) {
    	setTable(page, TAG_TABLE, table);
    	
//        this.tagTable = tagTable;
  //      if (page != 0)
    //        throw new RuntimeException("code pages curr. not supp.");
    }

    /** Sets the attribute start Table for a given page.
     *	The first string in the array defines attribute 
     *  5, the second attribute 6 etc.
     *  Currently, only page 0 is supported. Please use the 
     *  character '=' (without quote!) as delimiter 
     *  between the attribute name and the (start of the) value 
     */

    public void setAttrStartTable(
    		final int page,
    		final String[] table) {

		setTable(page, ATTR_START_TABLE, table);
    }

    /** Sets the attribute value Table for a given page.
     *	The first string in the array defines attribute value 0x85, 
     *  the second attribute value 0x86 etc.
     *  Currently, only page 0 is supported.
     */

    public void setAttrValueTable(
    		final int page,
    		final String[] table) {

		setTable(page, ATTR_VALUE_TABLE, table);
    }

    public int getWapExtensionCode() {
    	return wapExtensionCode;
    }
    
    public Object getWapExtensionData() {
    	return wapExtensionData;
    }

}
