package com.custom.blockchain.data.chainstate;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.custom.blockchain.data.AbstractLevelDB;
import com.custom.blockchain.data.exception.DatabaseException;
import com.custom.blockchain.transaction.TransactionOutput;
import com.custom.blockchain.util.StringUtil;
import com.custom.blockchain.util.WalletUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author marcosrachid
 *
 */
@Component
public class UTXOChainstateDB extends AbstractLevelDB<PublicKey, List<TransactionOutput>> {

	private static final Logger LOG = LoggerFactory.getLogger(UTXOChainstateDB.class);

	private static final TypeReference<List<TransactionOutput>> MAPPER = new TypeReference<List<TransactionOutput>>() {
	};

	private static final String KEY_BINDER = "c";

	private static final String[] EXCLUDING_KEY_BINDER = { "B", "P", "S" };

	private DB chainstateDb;

	private ObjectMapper objectMapper;

	public UTXOChainstateDB(final ObjectMapper objectMapper, final @Qualifier("ChainStateDB") DB chainstateDb) {
		this.objectMapper = objectMapper;
		this.chainstateDb = chainstateDb;
	}

	@Override
	public List<TransactionOutput> get(PublicKey key) {
		String k = WalletUtil.getStringFromKey(key);
		LOG.trace("[Crypto] ChainstateDB Get - Key: " + KEY_BINDER + k);
		try {
			return objectMapper.readValue(StringUtil.decompress(chainstateDb.get(StringUtil.compress(k))), MAPPER);
		} catch (DBException | IOException e) {
			LOG.debug("[Crypto] ChainstateDB Error from key [" + KEY_BINDER + k + "]: " + e.getMessage(), e);
			return new ArrayList<TransactionOutput>();
		}
	}

	@Override
	public void put(PublicKey key, List<TransactionOutput> value) {
		String k = WalletUtil.getStringFromKey(key);
		try {
			String v = objectMapper.writeValueAsString(value);
			LOG.trace("[Crypto] ChainstateDB Add Object - Key: " + KEY_BINDER + k + ", Value: " + v);
			chainstateDb.put(StringUtil.compress(k), StringUtil.compress(v));
		} catch (DBException | IOException e) {
			throw new DatabaseException("Could not put data from key [" + KEY_BINDER + k + "] and TransactionOutput ["
					+ value + "]: " + e.getMessage());
		}
	}

	@Override
	public void delete(PublicKey key) {
		String k = WalletUtil.getStringFromKey(key);
		LOG.trace("[Crypto] ChainstateDB Deleted - Key: " + KEY_BINDER + k);
		try {
			chainstateDb.delete(StringUtil.compress(k));
		} catch (DBException | IOException e) {
			throw new DatabaseException("Could not delete data from key [" + KEY_BINDER + k + "]: " + e.getMessage());
		}
	}

	@Override
	public DBIterator iterator() {
		DBIterator iterator = chainstateDb.iterator();
		iterator.seekToFirst();
		return iterator;
	}

	@Override
	public List<TransactionOutput> next(final DBIterator iterator) {
		try {
			Entry<byte[], byte[]> entry = iterator.next();
			String key = StringUtil.decompress(entry.getKey());
			for (String excluded : EXCLUDING_KEY_BINDER) {
				if (key.startsWith(excluded))
					return next(iterator);
			}
			String value = StringUtil.decompress(entry.getValue());
			LOG.trace("[Crypto] ChainstateDB Current Iterator - Key: " + KEY_BINDER + key + ", Value: " + value);
			return objectMapper.readValue(value, MAPPER);
		} catch (Exception e) {
			throw new DatabaseException("Could not get data from iterator: " + e.getMessage());
		}
	}

	/**
	 * 
	 * @param key
	 * @param transactionOutput
	 */
	public void leftOver(PublicKey key, TransactionOutput transactionOutput) {
		List<TransactionOutput> transactionOutputs = new ArrayList<>();
		transactionOutputs.add(transactionOutput);
		put(key, transactionOutputs);
	}

	/**
	 * 
	 * @param key
	 * @param transactionOutput
	 */
	public void add(PublicKey key, TransactionOutput transactionOutput) {
		List<TransactionOutput> transactionOutputs = get(key);
		transactionOutputs.add(transactionOutput);
		put(key, transactionOutputs);
	}

	/**
	 * 
	 * @param key
	 * @param transactionOutput
	 */
	public void remove(PublicKey key, TransactionOutput transactionOutput) {
		List<TransactionOutput> transactionOutputs = get(key);
		transactionOutputs.remove(transactionOutput);
		put(key, transactionOutputs);
	}

	@Override
	public void close() {
		LOG.info("[Crypto] closing ChainstateDb");
		try {
			chainstateDb.close();
			LOG.info("[Crypto] ChainstateDb closed");
		} catch (IOException e) {
			throw new DatabaseException("Could not close connection: " + e.getMessage());
		}
	}

}
