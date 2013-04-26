package org.quickcached;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickcached.cache.CacheException;
import org.quickcached.cache.CacheInterface;
import org.quickserver.net.server.ClientHandler;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Akshathkumar Shetty
 */
public class TextCommandProcessor {
	private static final Logger logger = Logger.getLogger(TextCommandProcessor.class.getName());
	
	private static String versionOutput = null;
	
	static {
		versionOutput = "VERSION " + QuickCached.version + "\r\n";
	}
	
	private CacheInterface cache;

	public void setCache(CacheInterface cache) {
		this.cache = cache;
	}

	public void handleTextCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException, CacheException {
		if (QuickCached.DEBUG) {
			logger.log(Level.FINE, "command: {0}", command);
		} 

		if (command.startsWith("get ") || command.startsWith("gets ")) {
			handleGetCommands(handler, command);			
		} else if (command.equals("version")) {
			sendResponse(handler, versionOutput);
		} else if (command.startsWith("set ") || command.startsWith("add ")
				|| command.startsWith("replace ") || command.startsWith("append ")
				|| command.startsWith("prepend ") || command.startsWith("cas ")) {
			
			handleStorageCommands(handler, command);
			Data data = (Data) handler.getClientData();
			if (data.isAllDataIn()) {
				processStorageCommands(handler);
				return;
			} else {
				return;
			}			
		} else if (command.startsWith("delete ")) {
			handleDeleteCommands(handler, command);
		} else if (command.startsWith("flush_all")) {
			handleFlushAll(handler, command);			
		} else if (command.equals("stats")) {
			Map stats = CommandHandler.getStats(handler.getServer());
			Set keySet = stats.keySet();
			Iterator iterator = keySet.iterator();
			String key = null;
			String value = null;
			while (iterator.hasNext()) {
				key = (String) iterator.next();
				value = (String) stats.get(key);
				sendResponse(handler, "STAT " + key + " " + value + "\r\n");
			}
			sendResponse(handler, "END\r\n");
		} else if (command.startsWith("stats ")) {
			//TODO
			sendResponse(handler, "ERROR\r\n");
		} else if (command.equals("quit")) {
			handler.closeConnection();
		} else if (command.startsWith("incr ") || command.startsWith("decr ")) {
			handleIncrDecrCommands(handler, command);
		} else if (command.startsWith("touch ")) {
			handleTouchCommands(handler, command);
		} else {
			logger.log(Level.WARNING, "unknown command! {0}", command);
			sendResponse(handler, "ERROR\r\n");
		}
	}

	private void handleFlushAll(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException, CacheException {
		/*
		flush_all [exptime] [noreply]\r\n
		 */
		String cmdData[] = command.split(" ");
		String cmd = cmdData[0];
		String exptime = null;
		
		if(QuickCached.DEBUG==false) {
			logger.log(Level.FINE, "cmd: {0}", new Object[]{cmd});
		}
		
		boolean noreplay = false;

		if(cmdData[cmdData.length-1].equals("noreply")) {
			noreplay = true;
		}

		if (noreplay==false && cmdData.length >= 2) {
			exptime = cmdData[1];
		} else if (noreplay==true && cmdData.length >= 3) {
			exptime = cmdData[1];
		}

		if (exptime == null) {			
			cache.flush();			
		} else {
			final int sleeptime = Integer.parseInt(exptime);
			Thread t = new Thread() {
				public void run() {
					try {
						sleep(1000 * sleeptime);
					} catch (InterruptedException ex) {
						logger.log(Level.WARNING, "Error: "+ex, ex);
					}
					try {
						cache.flush();
					} catch (CacheException ex) {
						logger.log(Level.SEVERE, "Error: "+ex, ex);
					}
				}
			};
			t.start();
		}		
		
		if (noreplay) {
			return;
		}
		
		sendResponse(handler, "OK\r\n");
	}

	private void handleDeleteCommands(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException, CacheException {
		/*
		delete <key> [noreply]\r\n
		 */
		String cmdData[] = command.split(" ");

		String cmd = cmdData[0];
		String key = cmdData[1];
		
		if(QuickCached.DEBUG==false) {
			logger.log(Level.FINE, "cmd: {0}, key: {1}", new Object[]{cmd, key});
		}

		boolean noreplay = false;
		if (cmdData.length == 3) {
			if ("noreply".equals(cmdData[2])) {
				noreplay = true;
			}
		}
		boolean flag = cache.delete(key);		
		
		if (noreplay) {
			return;
		}

		if (flag == true) {
			sendResponse(handler, "DELETED\r\n");
		} else {
			sendResponse(handler, "NOT_FOUND\r\n");
		}
	}
	
	private void handleTouchCommands(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException, CacheException {
		/*
		touch <key> <exptime> [noreply]\r\n
		*/
		String cmdData[] = command.split(" ");
		
		String cmd = cmdData[0];
		String key = cmdData[1];
		int exptime = Integer.parseInt(cmdData[2]);
		
		boolean noreplay = false;		
		if (cmdData.length >= 4) {
			if ("noreply".equals(cmdData[3])) {
				noreplay = true;
			}
		}
		
		if(QuickCached.DEBUG==false) {
			logger.log(Level.FINE, "cmd: {0}, key: {1}", new Object[]{cmd, key});
		}
		
		boolean flag = cache.touch(key, exptime);
		
		if(noreplay) return;
		
		if(flag==false) {
			sendResponse(handler, "NOT_FOUND\r\n");				
		} else {
			sendResponse(handler, "TOUCHED\r\n");
		}
	}

	private void handleGetCommands(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException, CacheException {
		/*
		get <key>*\r\n
		gets <key>*\r\n
		 */
		String cmdData[] = command.split(" ");

		String cmd = cmdData[0];
		String key = null;

		for (int i = 1; i < cmdData.length; i++) {
			key = cmdData[i];
			if(QuickCached.DEBUG==false) {
				logger.log(Level.FINE, "cmd: {0}, key: {1}", new Object[]{cmd, key});
			}
			DataCarrier dc = (DataCarrier) cache.get(key);
			if (dc != null) {
				StringBuilder sb = new StringBuilder();
				sb.append("VALUE ");
				sb.append(key);
				sb.append(" ");
				sb.append(dc.getFlags());
				sb.append(" ");
				sb.append(dc.getData().length);
				sb.append(" ");
				sb.append(dc.getCas());
				sb.append("\r\n");
				sendResponse(handler, sb.toString());
				sendResponse(handler, dc.getData());
				sendResponse(handler, "\r\n");
			}
		}
		sendResponse(handler, "END\r\n");

		/*
		VALUE <key> <flags> <bytes> [<cas unique>]\r\n
		<data block>\r\n
		 */
	}

	private void handleIncrDecrCommands(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException, CacheException {
		/*
		incr <key> <value> [noreply]\r\n
		decr <key> <value> [noreply]\r\n
		 */
		String cmdData[] = command.split(" ");

		if (cmdData.length < 3) {
			sendResponse(handler, "CLIENT_ERROR Bad number of args passed\r\n");
			if (cmdData[0].equals("incr")) {
				CommandHandler.incrMisses++;
			} else if (cmdData[0].equals("decr")) {
				CommandHandler.decrMisses++;
			}
			return;
		}

		String cmd = cmdData[0];
		String key = cmdData[1];
		String _value = cmdData[2];
		long value = 0;
		try {
			value = Long.parseLong(_value);
		} catch (Exception e) {
			sendResponse(handler, "CLIENT_ERROR parse of client value failed\r\n");
			if (cmd.equals("incr")) {
				CommandHandler.incrMisses++;
			} else if (cmd.equals("decr")) {
				CommandHandler.decrMisses++;
			}
			return;
		}
		
		if(QuickCached.DEBUG==false) {
			logger.log(Level.FINE, "cmd: {0}, key: {1}", new Object[]{cmd, key});
		}

		boolean noreplay = false;
		if (cmdData.length >= 4) {
			if ("noreply".equals(cmdData[3])) {
				noreplay = true;
			}
		}


		DataCarrier dc = (DataCarrier) cache.get(key);
		if (dc == null) {
			if (noreplay == false) {
				sendResponse(handler, "NOT_FOUND\r\n");
			}
			if (cmd.equals("incr")) {
				CommandHandler.incrMisses++;
			} else if (cmd.equals("decr")) {
				CommandHandler.decrMisses++;
			}
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		
		dc.writeLock.lock();
		try {			
			long oldvalue = Long.parseLong(new String(dc.getData(), 
				HexUtil.getCharset()));
			if (cmd.equals("incr")) {
				value = oldvalue + value;
			} else if (cmd.equals("decr")) {
				value = oldvalue - value;
				if (value < 0) {
					value = 0;
				}
			} else {
				throw new IllegalArgumentException("Unknown command "+cmd);
			}
			
			sb.append(value);
			dc.setData(sb.toString().getBytes(HexUtil.getCharset()));
			cache.update(key, dc, dc.getSize());
		} catch(Exception e) {
			if(noreplay == false) {
				sendResponse(handler, "CLIENT_ERROR parse of server value failed\r\n");
			}
			if (cmd.equals("incr")) {
				CommandHandler.incrMisses++;
			} else if (cmd.equals("decr")) {
				CommandHandler.decrMisses++;
			}
			return;
		} finally {
			dc.writeLock.unlock();
		}		
		
		if (cmd.equals("incr")) {
			CommandHandler.incrHits++;
		} else if (cmd.equals("decr")) {
			CommandHandler.decrHits++;
		}

		if (noreplay) {
			return;
		}
		
		sb.append("\r\n");
		sendResponse(handler, sb.toString());
	}

	private void handleStorageCommands(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		Data data = (Data) handler.getClientData();
		/*
		<command name> <key> <flags> <exptime> <bytes> [noreply]\r\n
		cas <key> <flags> <exptime> <bytes> <cas unique> [noreply]\r\n
		 */
		String cmdData[] = command.split(" ");

		String cmd = cmdData[0];
		String key = cmdData[1];
		String flags = cmdData[2];
		int exptime = Integer.parseInt(cmdData[3]);
		long bytes = Integer.parseInt(cmdData[4]);
		String casunique = null;

		boolean noreplay = false;
		if (cmdData.length >= 6) {
			if ("noreply".equals(cmdData[5])) {
				noreplay = true;
			} else {
				casunique = cmdData[5];
			}

			if (cmdData.length >= 7) {
				if ("noreply".equals(cmdData[6])) {
					noreplay = true;
				}
			}
		}

		data.setCmd(cmd);
		data.setKey(key);
		data.setFlags(flags);
		data.setExptime(exptime);
		data.setDataRequiredLength(bytes);
		data.setCasUnique(casunique);
		data.setNoreplay(noreplay);
	}

	public void processStorageCommands(ClientHandler handler)
			throws SocketTimeoutException, IOException, CacheException {
		Data data = (Data) handler.getClientData();
		
		if(QuickCached.DEBUG==false) {
			logger.log(Level.FINE, "cmd: {0}, key: {1}", new Object[]{data.getCmd(), data.getKey()});
		}

		byte dataToStore[] = data.getDataByte();

		DataCarrier dc = new DataCarrier(dataToStore);
		dc.setFlags(data.getFlags());

		if (data.getCmd().equals("set")) {
			DataCarrier olddata = (DataCarrier) cache.get(data.getKey());
			if(olddata==null) {
				cache.set(data.getKey(), dc, dc.getSize(), data.getExptime());
			} else {
				olddata.writeLock.lock();
				try {
					olddata.setData(dc.getData());
					olddata.setFlags(dc.getFlags());
					
					cache.update(data.getKey(), olddata, olddata.getSize(), data.getExptime());
				} finally {
					olddata.writeLock.unlock();
				}
			}
			if (data.isNoreplay() == false) {
				sendResponse(handler, "STORED\r\n");
			}
		} else if (data.getCmd().equals("add")) {
			Object olddata = cache.get(data.getKey());
			if (olddata == null) {
				cache.set(data.getKey(), dc, dc.getSize(), data.getExptime());
				if (data.isNoreplay() == false) {
					sendResponse(handler, "STORED\r\n");
				}
			} else {
				if (data.isNoreplay() == false) {
					sendResponse(handler, "NOT_STORED\r\n");
				}
			}
		} else if (data.getCmd().equals("replace")) {
			DataCarrier olddata = (DataCarrier) cache.get(data.getKey());
			if (olddata != null) {
				olddata.writeLock.lock();
				try {
					olddata.setData(dc.getData());
					cache.update(data.getKey(), olddata, olddata.getSize());
				} finally {
					olddata.writeLock.unlock();
				}
				
				dc.setData(null);
				dc = null;
				
				if (data.isNoreplay() == false) {
					if (data.isNoreplay() == false) {
						sendResponse(handler, "STORED\r\n");
					}
				}
			} else {
				if (data.isNoreplay() == false) {
					sendResponse(handler, "NOT_STORED\r\n");
				}
			}
		} else if (data.getCmd().equals("append")) {
			DataCarrier olddata = (DataCarrier) cache.get(data.getKey());
			if (olddata != null) {
				olddata.writeLock.lock();
				try {
					olddata.append(dc.getData());
					cache.update(data.getKey(), olddata, olddata.getSize());
				} finally {
					olddata.writeLock.unlock();
				}
				
				dc.setData(null);
				dc = null;

				if (data.isNoreplay() == false) {
					if (data.isNoreplay() == false) {
						sendResponse(handler, "STORED\r\n");
					}
				}
			} else {
				if (data.isNoreplay() == false) {
					sendResponse(handler, "NOT_STORED\r\n");
				}
			}
		} else if (data.getCmd().equals("prepend")) {
			DataCarrier olddata = (DataCarrier) cache.get(data.getKey());
			if (olddata != null) {
				olddata.writeLock.lock();
				try {
					olddata.prepend(dc.getData());
					cache.update(data.getKey(), olddata, olddata.getSize());
				} finally {
					olddata.writeLock.unlock();
				}

				dc.setData(null);
				dc = null;

				if (data.isNoreplay() == false) {
					if (data.isNoreplay() == false) {
						sendResponse(handler, "STORED\r\n");
					}
				}
			} else {
				if (data.isNoreplay() == false) {
					sendResponse(handler, "NOT_STORED\r\n");
				}
			}
		} else if (data.getCmd().equals("cas")) {
			String reply = null;			
			
			DataCarrier olddata = (DataCarrier) cache.get(data.getKey());			
			if(olddata != null) {
				olddata.writeLock.lock();
				try {
					int oldcas = olddata.getCas();
					int passedcas = Integer.parseInt(data.getCasUnique());

					if (oldcas == passedcas) {
						olddata.setData(dc.getData());
						cache.update(data.getKey(), olddata, olddata.getSize());
						
						dc.setData(null);
						dc = null;
						
						CommandHandler.casHits++;
						if (data.isNoreplay() == false) {
							reply = "STORED\r\n";
						}
					} else {
						CommandHandler.casBadval++;
						if (data.isNoreplay() == false) {
							reply = "EXISTS\r\n";
						}
					}
				} finally {
					olddata.writeLock.unlock();
				}
			} else {
				CommandHandler.casMisses++;
				if (data.isNoreplay() == false) {
					reply = "NOT_FOUND\r\n";
				}
			}		
			
			if(reply!=null) {
				sendResponse(handler, reply);
			}
		}
		data.clear();
	}

	public void sendResponse(ClientHandler handler, String data) throws SocketTimeoutException, IOException {
		sendResponse(handler, data.getBytes(HexUtil.getCharset()));
	}

	public void sendResponse(ClientHandler handler, byte data[]) throws SocketTimeoutException, IOException {
		if(handler.getCommunicationLogging() || QuickCached.DEBUG) {
			logger.log(Level.FINE, "S: {0}", new String(data, HexUtil.getCharset()));
		} else {
			logger.log(Level.FINE, "S: {0} bytes", data.length);
		}
		handler.sendClientBinary(data);
	}
}
