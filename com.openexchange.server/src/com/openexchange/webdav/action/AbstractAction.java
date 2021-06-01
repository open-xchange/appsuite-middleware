/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.webdav.action;

import java.util.List;
import com.openexchange.webdav.loader.BulkLoader;
import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.WebdavProtocolException;

public abstract class AbstractAction implements WebdavAction {
	private WebdavAction next;
	private BulkLoader bulkLoader;

	public void yield(final WebdavRequest req, final WebdavResponse res) throws WebdavProtocolException {
		next.perform(req, res);
	}

	public WebdavAction getNext() {
		return next;
	}

	public void setNext(final WebdavAction next) {
		this.next = next;
	}

	public void setBulkLoader(final BulkLoader loader) {
		this.bulkLoader = loader;
	}

	public BulkLoader getBulkLoader(){
		return this.bulkLoader;
	}

	protected final void preLoad(final LoadingHints loading) {
		if (bulkLoader != null) {
			bulkLoader.load(loading);
		}
	}

	protected final void preLoad(final List<LoadingHints> hints) {
		if (bulkLoader != null) {
			bulkLoader.load(hints);
		}
	}

	@Override
	public String toString(){
		if (null != next) {
			return getClass().getSimpleName()+' '+next.toString();
		}
		return getClass().getSimpleName();
	}
}
