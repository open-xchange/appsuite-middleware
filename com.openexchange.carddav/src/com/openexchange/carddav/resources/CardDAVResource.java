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

package com.openexchange.carddav.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.carddav.CarddavProtocol;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.java.Streams;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link CardDAVResource} - Abstract base class for CardDAV resources.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class CardDAVResource extends AbstractResource {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CardDAVResource.class);

    /**
     * The prefix to be applied to all OX CardDAV entities
     */
	protected static final String ETAG_PREFIX = "http://www.open-xchange.com/carddav/";

    protected GroupwareCarddavFactory factory;
    protected WebdavPath url;
	private String cachedVCard = null;

    public CardDAVResource(GroupwareCarddavFactory factory, WebdavPath url) {
        super();
        this.factory = factory;
        this.url = url;
        LOG.debug(getUrl() + ": initialized.");
    }
    
    protected WebdavProtocolException protocolException(Throwable t) {
    	return protocolException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    protected WebdavProtocolException protocolException(int statusCode) {
    	return protocolException(null, statusCode);
    }

    protected WebdavProtocolException protocolException(Throwable t, int statusCode) {
    	if (null == t) {
    		t = new Throwable();
    	}
        LOG.error(t.getMessage(), t);
        return WebdavProtocolException.Code.GENERAL_ERROR.create(this.getUrl(), statusCode, t);
    }

	protected abstract void applyVersitObject(VersitObject versitObject) throws WebdavProtocolException;
	protected abstract String generateVCard() throws WebdavProtocolException;
	protected abstract String getUID();	

	private String getVCard() throws WebdavProtocolException {
		if (null == this.cachedVCard) {
			this.cachedVCard = this.generateVCard();
		}
		return this.cachedVCard;
	}
	
    private VersitObject readBody(InputStream body) throws WebdavProtocolException {
        ByteArrayOutputStream outputStream = null;
    	try {
            VersitDefinition def = Versit.getDefinition("text/vcard");
            VersitDefinition.Reader versitReader = null;
            if (LOG.isTraceEnabled()) {
                int length = 2048;
                byte[] buffer = new byte[length];
                outputStream = new UnsynchronizedByteArrayOutputStream(8192);
                for (int read = body.read(buffer, 0, length); read > 0; read = body.read(buffer, 0, length)) {
                    outputStream.write(buffer, 0, read);
                }
                byte[] bytes = outputStream.toByteArray();
                LOG.trace(new String(bytes, "UTF-8"));
                versitReader = def.getReader(new UnsynchronizedByteArrayInputStream(bytes), "UTF-8");
            } else {
                versitReader = def.getReader(body, "UTF-8");
            }
            return def.parse(versitReader);
        } catch (IOException e) {
        	throw this.protocolException(e);
        } finally {
            Streams.close(outputStream);
            Streams.close(body);
        }
    }	

	@Override
	protected WebdavFactory getFactory() {
		return this.factory;
	}

	@Override
	public WebdavPath getUrl() {
		return this.url;
	}

	@Override
	public String getLanguage() throws WebdavProtocolException {
		return null;
	}

	@Override
	public void setLanguage(String language) throws WebdavProtocolException {
	}

	@Override
	public Long getLength() throws WebdavProtocolException {
		if (this.exists()) {
			String vCard = this.getVCard();
			if (null != vCard && 0 < vCard.length()) {
				return new Long(vCard.getBytes().length);
			}			
		}		
		return 0L;
	}

	@Override
	public void setLength(Long length) throws WebdavProtocolException {
	}

	@Override
	public void setContentType(String type) throws WebdavProtocolException {
	}

	@Override
	public String getContentType() throws WebdavProtocolException {
		return "text/vcard; charset=utf-8";
	}

	@Override
	public String getETag() throws WebdavProtocolException {
    	if (this.exists()) {
    		return String.format("%s%s_%d", ETAG_PREFIX, this.getUID(), this.getLastModified().getTime());
    	} else {
    		return "";
    	}
	}

	@Override
	public String getSource() throws WebdavProtocolException {
		return null;
	}

	@Override
	public void setSource(String source) throws WebdavProtocolException {
	}

	@Override
	public InputStream getBody() throws WebdavProtocolException {
	    String body = this.getVCard();
	    if (LOG.isTraceEnabled()) {
	        LOG.trace(body);
	    }
        return null != body ? new ByteArrayInputStream(body.getBytes()) : null;
	}

	@Override
	public void lock(WebdavLock lock) throws WebdavProtocolException {
	}

	@Override
	public List<WebdavLock> getLocks() throws WebdavProtocolException {
        return Collections.emptyList();
	}

	@Override
	public WebdavLock getLock(String token) throws WebdavProtocolException {
		return null;
	}

	@Override
	public void unlock(String token) throws WebdavProtocolException {
	}

	@Override
	public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
        return Collections.emptyList();
	}

	@Override
	public WebdavLock getOwnLock(String token) throws WebdavProtocolException {
		return null;
	}

	@Override
	public void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException {
    	this.applyVersitObject(this.readBody(body));
	}

	@Override
	public boolean hasBody() throws WebdavProtocolException {
		return true;
	}

	@Override
	public void setCreationDate(Date date) throws WebdavProtocolException {
	}

	@Override
	protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
        return Collections.emptyList();
	}

	@Override
	protected void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException {
	}

	@Override
	protected void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException {
	}

	@Override
	protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
        if (CarddavProtocol.CARD_NS.getURI().equals(namespace) && "address-data".equals(name)) {
            WebdavProperty property = new WebdavProperty(namespace, name);
            property.setValue(this.getVCard());
            return property;
        } else {
        	return null;
        }
	}

	@Override
	protected boolean isset(Property p) {
		return true;
	}

}
