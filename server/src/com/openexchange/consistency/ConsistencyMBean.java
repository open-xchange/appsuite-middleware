package com.openexchange.consistency;

import com.openexchange.groupware.AbstractOXException;

import java.util.List;
import java.util.Map;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface ConsistencyMBean {

    // List

    // Missing
    public List<String> listMissingFilesInContext(int contextId) throws AbstractOXException;

    public Map<Integer, List<String>> listMissingFilesInFilestore(int filestoreId) throws AbstractOXException;

    public Map<Integer, List<String>> listMissingFilesInDatabase(int databaseId) throws AbstractOXException;

    public Map<Integer, List<String>> listAllMissingFiles() throws AbstractOXException;

    // Unassigned

    public List<String> listUnassignedFilesInContext(int contextId) throws AbstractOXException;

    public Map<Integer, List<String>> listUnassignedFilesInFilestore(int filestoreId) throws AbstractOXException;

    public Map<Integer, List<String>> listUnassignedFilesInDatabase(int databaseId) throws AbstractOXException;

    public Map<Integer, List<String>> listAllUnassignedFiles() throws AbstractOXException;
    

    // Repair

    public void repairFilesInContext(int contextId, String resolverPolicy) throws AbstractOXException;

    public void repairFilesInFilestore(int filestoreId, String resolverPolicy) throws AbstractOXException;

    public void repairFilesInDatabase(int databaseId, String resolverPolicy) throws AbstractOXException;

    public void repairAllFiles(String resolverPolicy) throws AbstractOXException;


}
