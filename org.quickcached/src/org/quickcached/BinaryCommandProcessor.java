package org.quickcached;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickcached.binary.BinaryPacket;
import org.quickcached.binary.Extras;
import org.quickcached.binary.OpCode;
import org.quickcached.binary.ResponseHeader;
import org.quickcached.cache.CacheException;
import org.quickcached.cache.CacheInterface;
import org.quickserver.net.server.ClientHandler;

/**
 *
 * @author akshath
 */
public class BinaryCommandProcessor {
	private static final Logger logger = Logger.getLogger(BinaryCommandProcessor.class.getName());

	private static byte[] version = null;

	static {
		try {
			version = QuickCached.version.getBytes(HexUtil.getCharset());
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(BinaryCommandProcessor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	private CacheInterface cache;

	public void setCache(CacheInterface cache) {
		this.cache = cache;
	}

	public void handleBinaryCommand(ClientHandler handler, BinaryPacket command)
			throws SocketTimeoutException, IOException, CacheException {
		if (QuickCached.DEBUG) {
			logger.log(Level.FINE, "command: {0}", command);
		}

		String opcode = command.getHeader().getOpcode();
		if (QuickCached.DEBUG == false) {
			logger.log(Level.FINE, "opcode: {0}, key: {1}", new Object[]{opcode, command.getKey()});
		}

		opcode = opcode.toUpperCase();

		ResponseHeader rh = new ResponseHeader();
		rh.setMagic("81");
		rh.setOpcode(opcode);
		rh.setOpaque(command.getHeader().getOpaque());

		BinaryPacket binaryPacket = new BinaryPacket();
		binaryPacket.setHeader(rh);

		if (OpCode.GET.equals(opcode) || OpCode.GET_Q.equals(opcode)//Get,GetQ,
				|| OpCode.GET_K.equals(opcode) || OpCode.GET_K_Q.equals(opcode) //GetK, GetKQ
				|| OpCode.GAT.equals(opcode) || OpCode.GAT_Q.equals(opcode)) {//GAT, GATQ 

			if (OpCode.GET_K.equals(opcode) || OpCode.GET_K_Q.equals(opcode)) {//GetK, GetKQ
				binaryPacket.setKey(command.getKey());
				rh.setKeyLength(binaryPacket.getKey().length());
			}

			DataCarrier dc = (DataCarrier) cache.get(command.getKey());
			if (dc == null) {
				if (OpCode.GET_Q.equals(opcode) == false && OpCode.GET_K_Q.equals(opcode) == false //GetQ, GetKQ
						&& OpCode.GAT_Q.equals(opcode) == false) { //GATQ
					rh.setStatus(ResponseHeader.KEY_NOT_FOUND);
					sendResponse(handler, binaryPacket);
				}
			} else {
				rh.setStatus(ResponseHeader.STATUS_NO_ERROR);

				if (OpCode.GAT.equals(opcode) || OpCode.GAT_Q.equals(opcode)) {//Gat, GatQ
					if (command.getExtras() != null) {
						cache.touch(command.getKey(), command.getExtras().getExpirationInSec());
					} else {
						logger.log(Level.WARNING, "Extras not passed!!");
						rh.setStatus(ResponseHeader.INVALID_ARGUMENTS);
						sendResponse(handler, binaryPacket);
						return;
					}
				}

				Extras extras = new Extras();
				extras.setFlags(dc.getFlags());
				binaryPacket.setExtras(extras);
				rh.setExtrasLength(4);

				rh.setCas(dc.getCas());
				binaryPacket.setValue(dc.getData());

				rh.setTotalBodyLength(rh.getKeyLength()
						+ rh.getExtrasLength() + binaryPacket.getValue().length);

				rh.setCas(dc.getCas());

				sendResponse(handler, binaryPacket);
			}
		} else if (OpCode.NOOP.equals(opcode)) {
			rh.setStatus(ResponseHeader.STATUS_NO_ERROR);

			sendResponse(handler, binaryPacket);
		} else if (OpCode.VERSION.equals(opcode)) {
			binaryPacket.setValue(version);

			rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
			rh.setTotalBodyLength(rh.getKeyLength()
					+ rh.getExtrasLength() + binaryPacket.getValue().length);

			sendResponse(handler, binaryPacket);
		} else if (OpCode.SET.equals(opcode) || OpCode.SET_Q.equals(opcode)) {//Set,SetQ
			DataCarrier dc = (DataCarrier) cache.get(command.getKey());

			if (command.getHeader().getCas() != null
					&& "0000000000000000".equals(command.getHeader().getCas()) == false) {
				if (dc == null) {
					CommandHandler.casMisses++;
					if (OpCode.SET.equals(opcode)) { //set
						rh.setStatus(ResponseHeader.KEY_NOT_FOUND);
						sendResponse(handler, binaryPacket);
					}
					return;
				}

				dc.readLock.lock();
				try {
					if (dc.checkCas(command.getHeader().getCas()) == false) {
						CommandHandler.casBadval++;
						if(QuickCached.DEBUG) {
							logger.log(Level.FINE,
								"Cas did not match! OldCas:{0}NewCAS:{1}",
								new Object[]{dc.getCas(), command.getHeader().getCas()});
						}
						if (OpCode.SET.equals(opcode)) { //set							
							rh.setStatus(ResponseHeader.ITEM_NOT_STORED);							
							//return; //no return as we need to reply
						} else {
							return;
						}
					} else {
						CommandHandler.casHits++;
					}
				} finally {
					dc.readLock.unlock();
				}
			
				if(rh.getStatus()!=null) {
					sendResponse(handler, binaryPacket);
					return;
				}
			}

			
			if (dc == null) {
				dc = new DataCarrier(command.getValue());
				dc.setFlags(command.getExtras().getFlags());
				cache.set(command.getKey(), dc, dc.getSize(), command.getExtras().getExpirationInSec());
			} else {
				dc.writeLock.lock();
				try {
					dc.setData(command.getValue());
					dc.setFlags(command.getExtras().getFlags());

					cache.update(command.getKey(), dc, dc.getSize(), command.getExtras().getExpirationInSec());
				} finally {
					dc.writeLock.unlock();
				}
			}

			if (OpCode.SET.equals(opcode)) {
				rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
				rh.setCas(dc.getCas());

				sendResponse(handler, binaryPacket);
			}
		} else if (OpCode.ADD.equals(opcode) || OpCode.ADD_Q.equals(opcode)) {
			DataCarrier olddc = (DataCarrier) cache.get(command.getKey());
			if (olddc != null) {
				if (OpCode.ADD.equals(opcode)) {
					rh.setStatus(ResponseHeader.KEY_EXISTS);
					sendResponse(handler, binaryPacket);
				}
				return;
			}

			DataCarrier dc = new DataCarrier(command.getValue());
			
			dc.writeLock.lock();
			try {
				if (command.getExtras().getFlags() != null) {
					dc.setFlags(command.getExtras().getFlags());
				}
				cache.set(command.getKey(), dc, dc.getSize(), command.getExtras().getExpirationInSec());
			} finally {
				dc.writeLock.unlock();
			}

			if (OpCode.ADD.equals(opcode)) {
				rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
				sendResponse(handler, binaryPacket);
			}
		} else if (OpCode.REPLACE.equals(opcode) || OpCode.REPLACE_Q.equals(opcode)) {
			DataCarrier olddc = (DataCarrier) cache.get(command.getKey());
			if (olddc == null) {
				if (OpCode.REPLACE.equals(opcode)) {
					rh.setStatus(ResponseHeader.KEY_NOT_FOUND);
					sendResponse(handler, binaryPacket);
				}
				return;
			}

			if (command.getHeader().getCas() != null
					&& "0000000000000000".equals(command.getHeader().getCas()) == false) {
				if (olddc != null) {
					olddc.readLock.lock();
					try {
						if (olddc.checkCas(command.getHeader().getCas()) == false) {
							CommandHandler.casBadval++;
							if(QuickCached.DEBUG) {
								logger.log(Level.FINE,
										"Cas did not match! OldCas:{0}NewCAS:{1}",
										new Object[]{olddc.getCas(), command.getHeader().getCas()});
							}
							if(OpCode.REPLACE.equals(opcode)) {								
								rh.setStatus(ResponseHeader.ITEM_NOT_STORED);								
								//no return since we have to reply
							} else {
								return;
							}							
						} else {
							CommandHandler.casHits++;
						}
					} finally {
						olddc.readLock.unlock();
					}

					if(rh.getStatus()!=null) {
						sendResponse(handler, binaryPacket);
						return;
					}
				} else {
					CommandHandler.casMisses++;
				}
			}

			olddc.writeLock.lock();
			try {
				if (command.getExtras().getFlags() != null) {
					olddc.setFlags(command.getExtras().getFlags());
				}
				olddc.setData(command.getValue());
				cache.update(command.getKey(), olddc, olddc.getSize());
			} finally {
				olddc.writeLock.unlock();
			}

			if (OpCode.REPLACE.equals(opcode)) {
				rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
				rh.setCas(olddc.getCas());

				sendResponse(handler, binaryPacket);
			}
		} else if (OpCode.APPEND.equals(opcode) || OpCode.APPEND_Q.equals(opcode)) {
			DataCarrier olddc = (DataCarrier) cache.get(command.getKey());
			if (olddc == null) {
				if (OpCode.APPEND.equals(opcode)) {
					rh.setStatus(ResponseHeader.ITEM_NOT_STORED);
					sendResponse(handler, binaryPacket);
				}
				return;
			}

			if (command.getHeader().getCas() != null
					&& "0000000000000000".equals(command.getHeader().getCas()) == false) {
				if (olddc != null) {
					olddc.readLock.lock();
					try {
						if (olddc.checkCas(command.getHeader().getCas()) == false) {
							CommandHandler.casBadval++;
							if (QuickCached.DEBUG) {
								logger.log(Level.FINE, "Cas did not match! OldCas:{0}NewCAS:{1}",
										new Object[]{olddc.getCas(), command.getHeader().getCas()});
							}
							if (OpCode.APPEND.equals(opcode)) {								
								rh.setStatus(ResponseHeader.ITEM_NOT_STORED);
								//no return since we need to reply
							} else {
								return;
							}
						} else {
							CommandHandler.casHits++;
						}
					} finally {
						olddc.readLock.unlock();
					}

					if(rh.getStatus()!=null) {
						sendResponse(handler, binaryPacket);
						return;
					}
				} else {
					CommandHandler.casMisses++;
				}
			}
			
			olddc.writeLock.lock();
			try {
				olddc.append(command.getValue());	
				cache.update(command.getKey(), olddc, olddc.getSize());
			} finally {
				olddc.writeLock.unlock();
			}

			if (OpCode.APPEND.equals(opcode)) {
				rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
				rh.setCas(olddc.getCas());

				sendResponse(handler, binaryPacket);
			}
		} else if (OpCode.PREPEND.equals(opcode) || OpCode.PREPEND_Q.equals(opcode)) {
			DataCarrier olddc = (DataCarrier) cache.get(command.getKey());
			if (olddc == null) {
				if (OpCode.PREPEND.equals(opcode)) {
					rh.setStatus(ResponseHeader.ITEM_NOT_STORED);
					sendResponse(handler, binaryPacket);
				}
				return;
			}

			if (command.getHeader().getCas() != null
					&& "0000000000000000".equals(command.getHeader().getCas()) == false) {
				if (olddc != null) {
					olddc.readLock.lock();
					try {
						if (olddc.checkCas(command.getHeader().getCas()) == false) {
							CommandHandler.casBadval++;
							if(QuickCached.DEBUG) {
								logger.log(Level.FINE,
									"Cas did not match! OldCas:{0}NewCAS:{1}",
									new Object[]{olddc.getCas(), command.getHeader().getCas()});
							}
							if(OpCode.PREPEND.equals(opcode)) {								
								rh.setStatus(ResponseHeader.ITEM_NOT_STORED);
								//no return since we need to reply
							} else {
								return;
							}
						} else {
							CommandHandler.casHits++;
						}
					} finally {
						olddc.readLock.unlock();
					}
					if(rh.getStatus()!=null) {
						sendResponse(handler, binaryPacket);
						return;
					}
				} else {
					CommandHandler.casMisses++;
				}
			}

			olddc.writeLock.lock();
			try {
				olddc.prepend(command.getValue());	
				cache.update(command.getKey(), olddc, olddc.getSize());
			} finally {
				olddc.writeLock.unlock();
			}

			if (OpCode.PREPEND.equals(opcode)) {
				rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
				rh.setCas(olddc.getCas());

				sendResponse(handler, binaryPacket);
			}
		} else if (OpCode.DELETE.equals(opcode) || OpCode.DELETE_Q.equals(opcode)) {//Delete, DeleteQ
			boolean falg = cache.delete(command.getKey());
			if (falg == false) {
				rh.setStatus(ResponseHeader.KEY_NOT_FOUND);
				sendResponse(handler, binaryPacket);
			} else {
				if (OpCode.DELETE.equals(opcode)) {
					rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
					sendResponse(handler, binaryPacket);
				}
			}
		} else if (OpCode.INCREMENT.equals(opcode) || OpCode.DECREMENT.equals(opcode) ||//Increment, Decrement
				OpCode.INCREMENT_Q.equals(opcode) || OpCode.DECREMENT_Q.equals(opcode)) {//IncrementQ, DecrementQ
			Extras extras = command.getExtras();

			char op = ' ';
			if (opcode.equals(OpCode.DECREMENT) || opcode.equals(OpCode.DECREMENT_Q)) {
				op = 'D';
			} else if (opcode.equals(OpCode.INCREMENT) || opcode.equals(OpCode.INCREMENT_Q)) {
				op = 'I';
			} else {
				throw new IllegalStateException("We should not be here!! " + opcode);
			}

			DataCarrier olddc = (DataCarrier) cache.get(command.getKey());
			if (olddc == null) {
				if (extras.getExpiration().equals("ffffffff") == false) {//as per protocol
					if (op == 'I') {
						CommandHandler.incrMisses++;
					} else {
						CommandHandler.decrMisses++;
					}
					if (OpCode.INCREMENT.equals(opcode) || OpCode.DECREMENT.equals(opcode)) { //Increment, Decrement
						rh.setStatus(ResponseHeader.KEY_NOT_FOUND);
						sendResponse(handler, binaryPacket);
					}
					return;
				} else {
					String value = "" + extras.getInitalValueInDec();
					olddc = new DataCarrier(value.getBytes(HexUtil.getCharset()));

					/*
					if (extras.getFlags() != null) {
					olddc.setFlags(extras.getFlags());
					}
					 */
					olddc.setFlags("0");//as per protocol

					cache.set(command.getKey(), olddc, olddc.getSize(), extras.getExpirationInSec());

					if (op == 'I') {
						CommandHandler.incrHits++;
					} else {
						CommandHandler.decrHits++;
					}

					if (OpCode.INCREMENT.equals(opcode) || OpCode.DECREMENT.equals(opcode)) {
						binaryPacket.setValue(olddc.getData());
						rh.setTotalBodyLength(rh.getKeyLength()
								+ rh.getExtrasLength() + binaryPacket.getValue().length);
						rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
						rh.setCas(olddc.getCas());

						sendResponse(handler, binaryPacket);
					}
					return;
				}
			}

			long value = 0;
			try {
				value = extras.getDeltaInDec();
			} catch (Exception e) {
				if(QuickCached.DEBUG) {
					logger.log(Level.WARNING, "Error: {0}", e);
				}
				rh.setStatus(ResponseHeader.INVALID_ARGUMENTS);
				sendResponse(handler, binaryPacket);
				return;
			}

			try {
				olddc.writeLock.lock();
				try {
					long oldvalue = Long.parseLong(new String(olddc.getData(), HexUtil.getCharset()));
					if (op == 'I') {
						value = oldvalue + value;
					} else {
						value = oldvalue - value;
					}

					if (value < 0) {
						value = 0;
					}
					olddc.setData(("" + value).getBytes(HexUtil.getCharset()));
					cache.update(command.getKey(), olddc, olddc.getSize());
				} finally {
					olddc.writeLock.unlock();
				}

				if (op == 'I') {
					CommandHandler.incrHits++;
				} else {
					CommandHandler.decrHits++;
				}
			} catch (Exception e) {
				if (op == 'I') {
					CommandHandler.incrMisses++;
				} else {
					CommandHandler.decrMisses++;
				}

				if (OpCode.INCREMENT.equals(opcode) || OpCode.DECREMENT.equals(opcode)) {
					rh.setStatus(ResponseHeader.INTERNAL_ERROR);
					sendResponse(handler, binaryPacket);
					return;
				}
				return;
			}

			cache.update(command.getKey(), olddc, olddc.getSize());

			if (OpCode.INCREMENT_Q.equals(opcode) || OpCode.DECREMENT_Q.equals(opcode)) {
				return;
			}

			binaryPacket.setValue(Util.prefixZerros(
					new String(olddc.getData(), HexUtil.getCharset()), 8).getBytes(HexUtil.getCharset()));

			rh.setTotalBodyLength(rh.getKeyLength()
					+ rh.getExtrasLength() + binaryPacket.getValue().length);
			rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
			rh.setCas(olddc.getCas());

			sendResponse(handler, binaryPacket);
		} else if (OpCode.QUIT.equals(opcode) || OpCode.QUIT_Q.equals(opcode)) {//Quit,QuitQ
			if (OpCode.QUIT.equals(opcode)) {
				rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
				sendResponse(handler, binaryPacket);
			}
			handler.closeConnection();
		} else if (OpCode.FLUSH.equals(opcode) || OpCode.FLUSH_Q.equals(opcode)) {//Flush, FlushQ
			cache.flush();
			if (OpCode.FLUSH.equals(opcode)) {
				rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
				sendResponse(handler, binaryPacket);
			}
		} else if (OpCode.STAT.equals(opcode)) {//Stat
			rh.setStatus(ResponseHeader.STATUS_NO_ERROR);

			Map stats = CommandHandler.getStats(handler.getServer());
			Set keySet = stats.keySet();
			Iterator iterator = keySet.iterator();
			String key = null;
			String value = null;
			while (iterator.hasNext()) {
				key = (String) iterator.next();
				value = (String) stats.get(key);

				binaryPacket.setKey(key);
				rh.setKeyLength(binaryPacket.getKey().length());

				binaryPacket.setValue(value.getBytes(HexUtil.getCharset()));

				rh.setTotalBodyLength(rh.getKeyLength()
						+ rh.getExtrasLength() + binaryPacket.getValue().length);

				sendResponse(handler, binaryPacket);
			}

			binaryPacket.setKey(null);
			rh.setKeyLength(0);

			binaryPacket.setValue(null);
			rh.setTotalBodyLength(0);
			sendResponse(handler, binaryPacket);
		} else if (OpCode.TOUCH.equals(opcode)) {
			//Touch - TODO revist after protocol docs are updated.. touch or touchq with key ?
			/*
			if ("0C".equals(opcode) || "0D".equals(opcode)) {//TouchK, TouchKQ
			binaryPacket.setKey(command.getKey());
			rh.setKeyLength(binaryPacket.getKey().length());
			}
			 */
			if (command.getExtras() == null) {
				logger.log(Level.WARNING, "Extras not passed!!");
				rh.setStatus(ResponseHeader.INVALID_ARGUMENTS);
				sendResponse(handler, binaryPacket);
				return;
			}
			boolean flag = cache.touch(command.getKey(), command.getExtras().getExpirationInSec());
			if (flag == false) {
				//if ("09".equals(opcode) == false && "0D".equals(opcode) == false) { //TouchQ, TouchKQ touch with key ?
				rh.setStatus(ResponseHeader.KEY_NOT_FOUND);
				sendResponse(handler, binaryPacket);
				//}
			} else {
				rh.setStatus(ResponseHeader.STATUS_NO_ERROR);
				/*
				Extras extras = new Extras();
				extras.setFlags(dc.getFlags());
				binaryPacket.setExtras(extras);
				rh.setExtrasLength(4);
				
				rh.setCas(dc.getCas());
				binaryPacket.setValue(dc.getData());
				
				rh.setTotalBodyLength(rh.getKeyLength()
				+ rh.getExtrasLength() + binaryPacket.getValue().length);
				
				rh.setCas(dc.getCas());
				 * 
				 */

				sendResponse(handler, binaryPacket);
			}
		} else {
			logger.log(Level.WARNING, "unknown binary command! {0}", opcode);
			rh.setStatus(ResponseHeader.UNKNOWN_COMMAND);
			sendResponse(handler, binaryPacket);
		}

	}

	public void sendResponse(ClientHandler handler, BinaryPacket binaryPacket)
			throws SocketTimeoutException, IOException {
		if (QuickCached.DEBUG) {
			logger.log(Level.FINE, "Res BinaryPacket: {0}", binaryPacket);
		} else {
			ResponseHeader rh = (ResponseHeader) binaryPacket.getHeader();
			logger.log(Level.FINE, "S: Status {0}", rh.getStatus());
		}
		byte data[] = binaryPacket.toBinaryByte();
		if (handler.getCommunicationLogging() || QuickCached.DEBUG) {
			logger.log(Level.FINE, "S: {0}", new String(data, HexUtil.getCharset()));
			logger.log(Level.FINE, "H: {0}", HexUtil.encode(new String(data, HexUtil.getCharset())));
		} else {
			logger.log(Level.FINE, "S: {0} bytes", data.length);
		}

		handler.sendClientBinary(data);
	}
}
