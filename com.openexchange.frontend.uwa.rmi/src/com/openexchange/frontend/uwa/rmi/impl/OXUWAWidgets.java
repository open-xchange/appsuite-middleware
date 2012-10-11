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

package com.openexchange.frontend.uwa.rmi.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;
import com.openexchange.frontend.uwa.UWAWidget;
import com.openexchange.frontend.uwa.UWAWidgetException;
import com.openexchange.frontend.uwa.UWAWidgetService;
import com.openexchange.frontend.uwa.UWAWidgetServiceFactory;
import com.openexchange.frontend.uwa.rmi.OXUWAWidgetInterface;
import com.openexchange.frontend.uwa.rmi.Widget;

/**
 * {@link OXUWAWidgets}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXUWAWidgets  extends OXCommonImpl implements OXUWAWidgetInterface {

    private UWAWidgetServiceFactory factory;

    private BasicAuthenticator authenticator;

    private static final Log LOG = LogFactory.getLog(OXUWAWidgets.class);

    public OXUWAWidgets(UWAWidgetServiceFactory factory) throws StorageException {
        super();
        this.factory = factory;
        this.authenticator = new BasicAuthenticator();
    }

    public void change(Context ctx, Widget data, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        check(ctx, auth);
        try {
            UWAWidgetService widgets = getService(ctx);
            Conversion conversion = new Conversion(data);

            widgets.update(conversion.getUWAWidget(), conversion.getModifiedFields());
        } catch (UWAWidgetException x) {
            LOG.error(x.getMessage(), x);
            throw new StorageException(x.getMessage());
        }
    }

    private UWAWidgetService getService(Context ctx) throws UWAWidgetException {
        return factory.getService(ctx.getId());
    }

    public Widget create(Context ctx, Widget data, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        check(ctx, auth);
        try {
            UWAWidgetService widgets = getService(ctx);
            Conversion conversion = new Conversion(data);

            UWAWidget widget = conversion.getUWAWidget();
            widgets.create(widget);
            data.setId(widget.getId());
            return data;
        } catch (UWAWidgetException x) {
            LOG.error(x.getMessage(), x);
            throw new StorageException(x.getMessage());
        }
    }

    public void delete(Context ctx, Widget[] widgetsToDelete, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        check(ctx, auth);
        try {
            UWAWidgetService widgets = getService(ctx);
            for (Widget widget : widgetsToDelete) {
                widgets.delete(widget.getId());
            }
        } catch (UWAWidgetException x) {
            LOG.error(x.getMessage(), x);
            throw new StorageException(x.getMessage());
        }
    }

    public void delete(Context ctx, Widget widget, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        check(ctx, auth);
        try {
            UWAWidgetService widgets = getService(ctx);
            widgets.delete(widget.getId());
        } catch (UWAWidgetException x) {
            LOG.error(x.getMessage(), x);
            throw new StorageException(x.getMessage());
        }
    }

    public Widget[] getData(Context ctx, Widget[] toLoad, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        check(ctx, auth);
        try {
            UWAWidgetService widgets = getService(ctx);
            List<Widget> retval = new ArrayList<Widget>(toLoad.length);
            for (Widget widget : toLoad) {
                UWAWidget uwaWidget = widgets.get(widget.getId());
                retval.add(new Conversion(uwaWidget).getWidget());
            }

            return retval.toArray(new Widget[retval.size()]);
        } catch (UWAWidgetException x) {
            LOG.error(x.getMessage(), x);
            throw new StorageException(x.getMessage());
        }
    }

    public Widget getData(Context ctx, Widget widget, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        check(ctx, auth);
        try {
            UWAWidgetService widgets = getService(ctx);
            UWAWidget uwaWidget = widgets.get(widget.getId());
            return new Conversion(uwaWidget).getWidget();
        } catch (UWAWidgetException x) {
            LOG.error(x.getMessage(), x);
            throw new StorageException(x.getMessage());
        }
    }

    public Widget[] listAll(Context ctx, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        check(ctx, auth);
        try {
            UWAWidgetService widgets = getService(ctx);
            List<UWAWidget> all = widgets.all();
            List<Widget> retval = new ArrayList<Widget>(all.size());
            for (UWAWidget uwaWidget : all) {
                retval.add(new Conversion(uwaWidget).getWidget());
            }

            return retval.toArray(new Widget[retval.size()]);
        } catch (UWAWidgetException x) {
            LOG.error(x.getMessage(), x);
            throw new StorageException(x.getMessage());
        }
    }

    private void check(Context ctx, Credentials auth) throws InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        checkContextAndSchema(ctx);
        authenticator.doAuthentication(auth, ctx);
    }

}
