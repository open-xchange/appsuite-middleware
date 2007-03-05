package com.openexchange.groupware.infostore.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreException;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.DBService;
import com.openexchange.server.OCLPermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderTools;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.Component;

@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_UTILS_DELUSERFOLDERDISCOVERER,
		component=Component.INFOSTORE
)
public class DelUserFolderDiscoverer extends DBService {
	
	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(DelUserFolderDiscoverer.class);
	
	public DelUserFolderDiscoverer(){
		super();
	}
	
	public DelUserFolderDiscoverer(DBProvider provider) {
		super(provider);
	}

	public List<FolderObject> discoverFolders(int userId, Context ctx) throws OXException {
		List<FolderObject> discovered = new ArrayList<FolderObject>();
		try {
			User user = UserStorage.getInstance(ctx).getUser(userId);
			UserConfiguration userConfig = UserConfiguration.loadUserConfiguration(userId, ctx);
			
			final SearchIterator iter = OXFolderTools
	        .getAllVisibleFoldersIteratorOfModule(userId,
	            user.getGroups(), userConfig.getAccessibleModules(),
	            FolderObject.INFOSTORE, ctx);
			
folder:		while(iter.hasNext()) {
				FolderObject fo = (FolderObject) iter.next();
				for(OCLPermission perm : fo.getPermissionsAsArray()) {
					if(perm.isGroupPermission() || perm.getEntity() != userId){
						continue folder;
					}
				}
				discovered.add(fo);
			}
			
		} catch (AbstractOXException x) {
			throw new InfostoreException(x);
		} catch (SQLException e) {
			throw EXCEPTIONS.create(0,e);
		}
		
		return discovered;
	}
}
