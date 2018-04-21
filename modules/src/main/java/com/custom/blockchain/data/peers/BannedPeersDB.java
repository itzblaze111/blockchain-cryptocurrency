package com.custom.blockchain.data.peers;

import java.io.IOException;
import java.util.Map.Entry;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.custom.blockchain.data.FlagAbstractLevelDB;
import com.custom.blockchain.data.exception.DatabaseException;
import com.custom.blockchain.util.StringUtil;

@Component
public class BannedPeersDB extends FlagAbstractLevelDB<String> {

	private static final Logger LOG = LoggerFactory.getLogger(BannedPeersDB.class);

	private static final String KEY_BINDER = "P";

	private static final String EXCLUDING_KEY_BINDER = "p";

	private DB peersDB;

	public BannedPeersDB(final @Qualifier("PeersDB") DB peersDB) {
		this.peersDB = peersDB;
	}

	@Override
	public Boolean get(String key) {
		try {
			LOG.trace("[Crypto] PeersDB Get - Key: " + KEY_BINDER + key);
			return Boolean.valueOf(StringUtil.decompress(peersDB.get(StringUtil.compress(KEY_BINDER + key))));
		} catch (DBException | IOException e) {
			LOG.debug("[Crypto] PeersDB Error from key [" + KEY_BINDER + key + "]: " + e.getMessage());
			return false;
		}
	}

	@Override
	public void put(String key, Boolean value) {
		try {
			String v = value.toString();
			LOG.trace("[Crypto] PeersDB Add Object - Key: " + KEY_BINDER + key + ", Value: " + v);
			peersDB.put(StringUtil.compress(KEY_BINDER + key), StringUtil.compress(v));
		} catch (DBException | IOException e) {
			throw new DatabaseException("Could not put data from key [" + KEY_BINDER + key + "] and Peer [" + value
					+ "]: " + e.getMessage());
		}
	}

	@Override
	public void delete(String key) {
		LOG.trace("[Crypto] PeersDB Deleted - Key: " + key);
		try {
			peersDB.delete(StringUtil.compress(KEY_BINDER + key));
		} catch (DBException | IOException e) {
			throw new DatabaseException("Could not delete data from key [" + KEY_BINDER + key + "]: " + e.getMessage());
		}
	}

	@Override
	public DBIterator iterator() {
		DBIterator iterator = peersDB.iterator();
		iterator.seekToFirst();
		return iterator;
	}

	@Override
	public Boolean next(DBIterator iterator) {
		try {
			Entry<byte[], byte[]> entry = iterator.next();
			String key = StringUtil.decompress(entry.getKey());
			if (key.startsWith(EXCLUDING_KEY_BINDER))
				return next(iterator);
			String value = StringUtil.decompress(entry.getValue());
			LOG.trace("[Crypto] PeersDB Current Iterator - Key: " + key + ", Value: " + value);
			return Boolean.valueOf(value);
		} catch (Exception e) {
			throw new DatabaseException("Could not get data from iterator: " + e.getMessage());
		}
	}

	@Override
	public void close() {
		LOG.info("[Crypto] closing PeersDB");
		try {
			peersDB.close();
			LOG.info("[Crypto] PeersDB closed");
		} catch (IOException e) {
			throw new DatabaseException("Could not close connection: " + e.getMessage());
		}
	}

}
