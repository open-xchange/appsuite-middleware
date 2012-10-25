package com.openexchange.file.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractRootFolder implements FileStorageFolder {

	public Set<String> getCapabilities() {
		return Collections.emptySet();
	}

	
	public String getId() {
		return FileStorageFolder.ROOT_FULLNAME;
	}

	
	public abstract String getName();

	
	public FileStoragePermission getOwnPermission() {
		return DefaultFileStoragePermission.newInstance();
	}

	
	public String getParentId() {
		return null;
	}

	
	public List<FileStoragePermission> getPermissions() {
		return Arrays.asList((FileStoragePermission)DefaultFileStoragePermission.newInstance());
	}

	
	public boolean hasSubfolders() {
		return true;
	}

	
	public boolean hasSubscribedSubfolders() {
		return false;
	}

	
	public boolean isSubscribed() {
		return true;
	}

	
	public Date getCreationDate() {
		return new Date();
	}

	
	public Date getLastModifiedDate() {
		return new Date();
	}

	
	public boolean isHoldsFolders() {
		return true;
	}

	
	public boolean isHoldsFiles() {
		return true;
	}

	
	public boolean isRootFolder() {
		return true;
	}

	
	public boolean isDefaultFolder() {
		return false;
	}

	
	public abstract int getFileCount();

	
	public Map<String, Object> getProperties() {
		return Collections.emptyMap();
	}

	
}