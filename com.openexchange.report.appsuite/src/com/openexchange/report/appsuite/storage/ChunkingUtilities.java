package com.openexchange.report.appsuite.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.report.appsuite.ReportExceptionCodes;
import com.openexchange.report.appsuite.internal.ReportProperties;
import com.openexchange.report.appsuite.serialization.Report;

public class ChunkingUtilities {
    
    private final static int MAX_LOCK_FILE_ATTEMPTS = 20;
    
    public static void removeAllReportParts(String uuid) {
        File partsFolder = new File(ReportProperties.getStoragePath());
        LinkedList<File> parts = new LinkedList<>((Arrays.asList(partsFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".part");
            }
        }))));
        
        for (File file : parts) {
            if(file.getName().contains(uuid)) {
                file.delete();
            }
        }
    }
    
    public static void storeCapSContentToFiles(String reportUUID, String folderPath, Map<String, Object> data) throws JSONException, IOException {
        String filename = reportUUID + "_" + data.get(Report.CAPABILITIES).hashCode() + ".part";
        File storedDataFile = new File(folderPath + "/" + filename);
        if (storedDataFile.exists()) {
            mergeNewWithStoredData(storedDataFile, data);
        }
        
        try (FileWriter fw = new FileWriter(storedDataFile)) {
            // overwrite the so far stored data
            JSONObject jsonData = (JSONObject) JSONCoercion.coerceToJSON(data);
            fw.write(jsonData.toString(2));
        }
    }
    
    private static void mergeNewWithStoredData(File storedDataFile, Map<String, Object> data) throws IOException {
        try (RandomAccessFile storedFile = new RandomAccessFile(storedDataFile, "rw"); FileLock fileLock = getFileLock(storedFile)){
            // unable to get file lock for more then 20 seconds
            if (fileLock == null) {
                storedFile.close();
                throw new IOException("Unable to get file lock on file: " + storedDataFile.getAbsolutePath());
            }
            // Load and parse the existing data first into an Own JSONObject
            Scanner sc = new Scanner(storedDataFile);
            String content = sc.useDelimiter("\\Z").next();
            sc.close();
            Map<String, Object> storedData = (HashMap<String, Object>) JSONCoercion.parseAndCoerceToNative(content);
            // Merge the data of the two files into dataToStore
            mergeNewValuesWithStoredValues(storedData, data);
        } catch (FileNotFoundException e) {
            ReportExceptionCodes.STORED_FILE_NOT_FOUND.create(e);
        } catch (InterruptedException e) {
            ReportExceptionCodes.UNABLE_TO_GET_FILELOCK.create(e);
        }
    }
    
    private static FileLock getFileLock(RandomAccessFile storedFile) throws InterruptedException, IOException {
        FileLock fileLock = null;
        int fileLockAttempts = 0;
        while (fileLock == null && fileLockAttempts <= MAX_LOCK_FILE_ATTEMPTS) {
            try {
                fileLock = storedFile.getChannel().tryLock();
            } catch (OverlappingFileLockException e) {
                Thread.sleep(1000);
                fileLockAttempts++;
                continue;
            }
        }
        return fileLock;
    }
    
    private static void mergeNewValuesWithStoredValues(Map<String, Object> storedCounts, Map<String, Object> additionalCounts) {
        if (storedCounts.get(Report.CONTEXTS) != null && additionalCounts.get(Report.CONTEXTS) != null) {
            Long additionalContexts = Long.parseLong(String.valueOf(storedCounts.get(Report.CONTEXTS)));
            additionalCounts.put(Report.CONTEXTS, Long.parseLong(String.valueOf(additionalCounts.get(Report.CONTEXTS))) + additionalContexts);
        }
        for (Map.Entry<String, Object> entry : storedCounts.entrySet()) {
            String key = entry.getKey();
            // loaded file data can be either Long or integer
            if (!additionalCounts.containsKey(key)) {
                additionalCounts.put(key, entry.getValue());
                continue;
            }
            if (entry.getValue() instanceof Integer || entry.getValue() instanceof Long) {
                Long value = additionalCounts.get(key) instanceof Integer ? Long.parseLong(String.valueOf(additionalCounts.get(key))) : (Long) additionalCounts.get(key);
                Long storedValue = Long.parseLong(String.valueOf(entry.getValue()));
                if (!StringUtils.containsIgnoreCase(key, "context") && !key.equals(Report.TOTAL)) {
                    additionalCounts.put(key, value + storedValue);
                } else if (key.equals(Report.TOTAL)) {
                    additionalCounts.put(Report.TOTAL, value + storedValue);
                    additionalCounts.put(Report.CONTEXT_USERS_AVG, (Long) additionalCounts.get(Report.TOTAL) / (Long) additionalCounts.get(Report.CONTEXTS));
                } else if (key.equals(Report.CONTEXT_USERS_MAX) && storedValue > (Long) additionalCounts.get(Report.CONTEXT_USERS_MAX)) {
                    additionalCounts.put(Report.CONTEXT_USERS_MAX, storedValue);
                } else if (key.equals(Report.CONTEXT_USERS_MIN) && storedValue < (Long) additionalCounts.get(Report.CONTEXT_USERS_MIN) || (Long) additionalCounts.get(Report.CONTEXT_USERS_MIN) == 0l) {
                    additionalCounts.put(Report.CONTEXT_USERS_MIN, storedValue);
                }
            } else if (entry.getValue() instanceof HashMap) {
                mergeNewValuesWithStoredValues((HashMap<String, Object>) entry.getValue(), (HashMap<String, Object>) additionalCounts.get(entry.getKey()));
            }
        }
    }
    
}
