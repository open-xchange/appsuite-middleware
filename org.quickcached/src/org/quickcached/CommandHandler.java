package org.quickcached;

import java.net.*;
import java.io.*;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.ClientEventHandler;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.*;


import org.quickcached.binary.BinaryPacket;
import org.quickcached.binary.ResponseHeader;
import org.quickcached.cache.CacheException;
import org.quickcached.cache.CacheInterface;
import org.quickcached.mem.MemoryWarningSystem;
import org.quickserver.net.server.ClientBinaryHandler;
import org.quickserver.net.server.QuickServer;

public class CommandHandler implements ClientBinaryHandler, ClientEventHandler {
	private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());
	private static final SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static CacheInterface cache = null;
	private static TextCommandProcessor textCommandProcessor = null;
	private static BinaryCommandProcessor binaryCommandProcessor = null;

	private volatile static long totalConnections;
	private volatile static long bytesRead;
	private volatile static long bytesWritten;
	protected volatile static long gcCalls;	
	protected volatile static long incrMisses;
	protected volatile static long incrHits;
	protected volatile static long decrMisses;
	protected volatile static long decrHits;
	protected volatile static long casMisses;
	protected volatile static long casHits;
	protected volatile static long casBadval;
	
	private static boolean computeAvgForSetCmd = false;

	public static Map getStats(QuickServer server) {
		return getStats(server, null);
	}
	public static Map getStats(QuickServer server, Map stats) {
		if(stats==null) {
			stats = new LinkedHashMap(30);
		}

		//pid
		String pid = QuickCached.getPID();
		stats.put("pid", pid);

		//uptime
		long uptimeSec = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
		stats.put("uptime", "" + uptimeSec);

		//time - current UNIX time according to the server 
		long timeMili = System.currentTimeMillis();
		stats.put("time", ""+(timeMili/1000));		
		//stats.put("current_time_millis", "" + timeMili);
		
		stats.put("datetime", sdfDateTime.format(new Date(timeMili)));

		//version
		stats.put("version", QuickCached.version);

		//curr_connections
		stats.put("curr_connections", "" + server.getClientCount());

		//total_connections
		stats.put("total_connections", "" + totalConnections);

		//bytes_read    Total number of bytes read by this server from network
		stats.put("bytes_read", "" + bytesRead);

		//bytes_written     Total number of bytes sent by this server to network
		stats.put("bytes_written", "" + bytesWritten);

		//bytes - Current number of bytes used by this server to store items
		long usedMemory = Runtime.getRuntime().totalMemory() - 
				Runtime.getRuntime().freeMemory();			        
		stats.put("bytes", "" + usedMemory);

		//limit_maxbytes    Number of bytes this server is allowed to use for storage.
		long heapMaxSize = Runtime.getRuntime().maxMemory();
		stats.put("limit_maxbytes", "" + heapMaxSize);
		
		long mem_percent_used = (long) (100.0*usedMemory/heapMaxSize);
		stats.put("mem_percent_used", "" + mem_percent_used);
		
		//threads           Number of worker threads requested.
		//stats.put("threads", );

		cache.saveStats(stats);
		
		stats.put("incr_misses", "" + incrMisses);
		stats.put("incr_hits", "" + incrHits);
		stats.put("decr_misses", "" + decrMisses);
		stats.put("decr_hits", "" + decrHits);
		stats.put("cas_misses", "" + casMisses);
		stats.put("cas_hits", "" + casHits);
		stats.put("cas_badval", "" + casBadval);
		
		stats.put("app_version", QuickCached.app_version);
		stats.put("app_impl_used", cache.getName());
		
		stats.put("gc_calls", ""+gcCalls);

		return stats;
	}

	

	public CommandHandler() {
		logger.log(Level.FINE, "PID: {0}", QuickCached.getPID());
		logger.log(Level.FINE, "App Version: {0}", QuickCached.app_version);
		logger.log(Level.FINE, "Memcached Version: {0}", QuickCached.version);
		logger.log(Level.FINE, "Cache: {0}", cache);		
	}


	//--ClientEventHandler
	public void gotConnected(ClientHandler handler)
			throws SocketTimeoutException, IOException {
		totalConnections++;
		if(QuickCached.DEBUG) logger.log(Level.FINE, "Connection opened: {0}", 
				handler.getHostAddress());
	}

	public void lostConnection(ClientHandler handler) 
			throws IOException {
		if(QuickCached.DEBUG) logger.log(Level.FINE, "Connection Lost: {0}", 
				handler.getSocket().getInetAddress());
	}
	public void closingConnection(ClientHandler handler) 
			throws IOException {
		if(QuickCached.DEBUG) logger.log(Level.FINE, "Connection closed: {0}", 
				handler.getSocket().getInetAddress());
	}
	//--ClientEventHandler

	

	public void handleBinary(ClientHandler handler, byte command[])
			throws SocketTimeoutException, IOException {
		if(handler.getCommunicationLogging() || QuickCached.DEBUG) {
			logger.log(Level.FINE, "C: {0}", new String(command, HexUtil.getCharset()));
			logger.log(Level.FINE, "H: {0}", HexUtil.encode(new String(command, HexUtil.getCharset())));
		} else {
			logger.log(Level.FINE, "C: {0} bytes", command.length);
		}
		

		Data data = (Data) handler.getClientData();
		data.addBytes(command);

		bytesRead = bytesRead + handler.getTotalReadBytes();
		handler.resetTotalReadBytes();
		
		try {

			if(data.getDataRequiredLength()!=0) {//only used by text mode
				try {
					if(data.isAllDataIn()) {
						textCommandProcessor.processStorageCommands(handler);
						return;
					} else {
						return;
					}
				} catch(IllegalArgumentException e) {
					logger.log(Level.WARNING, "Error[iae]: "+e, e);
					textCommandProcessor.sendResponse(handler, "CLIENT_ERROR "+e.getMessage()+"\r\n");
				} catch(CacheException e) {
					logger.log(Level.WARNING, "Error[ce]: "+e, e);
					textCommandProcessor.sendResponse(handler, "SERVER_ERROR "+e.getMessage()+"\r\n");
				}
			}

			while(data.isMoreCommandToProcess()) {
				if(data.isBinaryCommand()) {
					if(QuickCached.DEBUG) logger.fine("BinaryCommand");
					BinaryPacket bp = null;
					try {
						bp = data.getBinaryCommandHeader();
					} catch (Exception ex) {
						logger.log(Level.SEVERE, "Error: "+ex, ex);
						throw new IOException(""+ex);
					}

					if(bp!=null) {
						if(QuickCached.DEBUG) logger.fine("BinaryCommand Start");
						try {
							binaryCommandProcessor.handleBinaryCommand(handler, bp);
						} catch(IllegalArgumentException e) {
							logger.log(Level.WARNING, "Error[iae]: "+e, e);
							
							ResponseHeader rh = new ResponseHeader();
							rh.setMagic("81");
							rh.setOpcode(bp.getHeader().getOpcode());
							rh.setStatus(ResponseHeader.INVALID_ARGUMENTS);
							
							BinaryPacket binaryPacket = new BinaryPacket();
							binaryPacket.setHeader(rh);
							
							binaryPacket.setValue(e.getMessage().getBytes("utf-8"));
							
							rh.setTotalBodyLength(rh.getKeyLength()
									+ rh.getExtrasLength() + binaryPacket.getValue().length);
							
							binaryCommandProcessor.sendResponse(handler, binaryPacket);
						} catch(CacheException e) {
							logger.log(Level.WARNING, "Error[ce]: "+e, e);
							
							ResponseHeader rh = new ResponseHeader();
							rh.setMagic("81");
							rh.setOpcode(bp.getHeader().getOpcode());
							rh.setStatus(ResponseHeader.INTERNAL_ERROR);
							
							BinaryPacket binaryPacket = new BinaryPacket();
							binaryPacket.setHeader(rh);
							
							binaryPacket.setValue(e.toString().getBytes("utf-8"));
							
							rh.setTotalBodyLength(rh.getKeyLength()
									+ rh.getExtrasLength() + binaryPacket.getValue().length);
							
							binaryCommandProcessor.sendResponse(handler, binaryPacket);
						}
						if(QuickCached.DEBUG) logger.fine("BinaryCommand End");
					} else {
						break;
					}
				} else {
					String cmd = data.getCommand();
					if(cmd!=null) {
						try {
							textCommandProcessor.handleTextCommand(handler, cmd);
						} catch(IllegalArgumentException e) {
							logger.log(Level.WARNING, "Error in text command [iae]: "+e, e);
							textCommandProcessor.sendResponse(handler, "CLIENT_ERROR "+e.getMessage()+"\r\n");
						} catch(CacheException e) {
							logger.log(Level.WARNING, "Error in text command [ce]: "+e, e);
							textCommandProcessor.sendResponse(handler, "SERVER_ERROR "+e.getMessage()+"\r\n");
						}
					} else {
						break;
					}
				}
			}
		} finally {
			bytesWritten = bytesWritten + handler.getTotalWrittenBytes();
			handler.resetTotalWrittenBytes();
		}
	}

	private static boolean lowMemoryActionInit;
	private static MemoryWarningSystem mws = new MemoryWarningSystem();

	public static void init(Map config) {
		logger.fine("in init");
		String implClass = (String) config.get("CACHE_IMPL_CLASS");
		logger.log(Level.FINE, "implClass: {0}", implClass);
		if(implClass==null) throw new NullPointerException("Cache impl class not specified!");
		try {
			cache = (CacheInterface) Class.forName(implClass).newInstance();
		} catch (Exception ex) {
			Logger.getLogger(CommandHandler.class.getName()).log(Level.SEVERE, null, ex);
			System.exit(-1);
		}
		
		String computeAvgForSetCmdStr = (String) config.get("COMPUTE_AVG_FOR_SET_CMD");
		if("true".equals(computeAvgForSetCmdStr)) {
			computeAvgForSetCmd = true;
		}

		textCommandProcessor = new TextCommandProcessor();
		textCommandProcessor.setCache(cache);

		binaryCommandProcessor = new BinaryCommandProcessor();
		binaryCommandProcessor.setCache(cache);

		String flushPercent = (String) config.get("FLUSH_ON_LOW_MEMORY_PERCENT");
		if(flushPercent!=null && flushPercent.trim().equals("")==false) {
			final double fpercent = Double.parseDouble(flushPercent);
			MemoryWarningSystem.setPercentageUsageThreshold(fpercent);//.95=95%
			logger.log(Level.INFO, "MemoryWarningSystem set to {0}; will flush if reached!", fpercent);

			if(lowMemoryActionInit==false) {
				lowMemoryActionInit = true;
				mws.addListener(new MemoryWarningSystem.Listener() {
					public void memoryUsageHigh(long usedMemory, long maxMemory) {
						logger.log(Level.INFO,
								"Memory usage high!: UsedMemory: {0};maxMemory:{1}",
								new Object[]{usedMemory, maxMemory});
						double percentageUsed = (((double) usedMemory) / maxMemory)*100;
						logger.log(Level.SEVERE,
								"Memory usage high! Percentage of memory used: {0}",
								percentageUsed);
						
						long memLimit = (long) (fpercent * 100);
						logger.warning("Calling GC to clear memory");
						System.gc();
						gcCalls++;
						long memPercentAfterGC = MemoryWarningSystem.getMemUsedPercentage();
						logger.log(Level.WARNING, "After GC mem percent used: {0}", memPercentAfterGC);
						if(memPercentAfterGC<0 || memPercentAfterGC > memLimit) {						
							logger.warning("Flushing cache to save JVM.");
							try {
								cache.flush();
							} catch (CacheException ex) {
								logger.log(Level.SEVERE, "Error: "+ex, ex);
							}
							System.gc();
							gcCalls++;
						}
						memPercentAfterGC = MemoryWarningSystem.getMemUsedPercentage();
						logger.log(Level.FINE, "Done. Mem percent used: {0}", memPercentAfterGC);
					}
				});
			}
		} else {
			mws.removeAllListener();
			lowMemoryActionInit = false;
		}
		
		String saveCacheToDiskBwRestarts = (String) 
			config.get("SAVE_CACHE_TO_DISK_IF_SUPPORTED_BW_RESTARTS");
		if(saveCacheToDiskBwRestarts!=null && saveCacheToDiskBwRestarts.equals("true")) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {				
					cache.saveToDisk();				
				}
			});
			cache.readFromDisk();
		}
	}

	public static boolean isComputeAvgForSetCmd() {
		return computeAvgForSetCmd;
	}

	public static void setComputeAvgForSetCmd(boolean aComputeAvgForSetCmd) {
		computeAvgForSetCmd = aComputeAvgForSetCmd;
	}
}
