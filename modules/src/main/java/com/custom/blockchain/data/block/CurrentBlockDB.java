package com.custom.blockchain.data.block;

import java.io.IOException;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.custom.blockchain.block.Block;
import com.custom.blockchain.data.PropertyAbstractLevelDB;
import com.custom.blockchain.data.chainstate.UTXOChainstateDB;
import com.custom.blockchain.data.exception.DatabaseException;
import com.custom.blockchain.util.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author marcosrachid
 *
 */
@Component
public class CurrentBlockDB extends PropertyAbstractLevelDB<Block> {

	private static final Logger LOG = LoggerFactory.getLogger(UTXOChainstateDB.class);

	private static final String KEY_BINDER = "B";

	private DB blockDb;

	private ObjectMapper objectMapper;

	public CurrentBlockDB(final @Qualifier("BlockDB") DB blockDb, final ObjectMapper objectMapper) {
		this.blockDb = blockDb;
		this.objectMapper = objectMapper;
	}

	@Override
	public Block get() {
		LOG.trace("[Crypto] ChainstateDB Get - Key: " + KEY_BINDER);
		try {
			return objectMapper.readValue(StringUtil.decompress(blockDb.get(KEY_BINDER.getBytes())), Block.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void put(Block value) {
		try {
			String v = objectMapper.writeValueAsString(value);
			LOG.trace("[Crypto] ChainstateDB Add Object - Key: " + KEY_BINDER + ", Value: " + v);
			blockDb.put(KEY_BINDER.getBytes(), StringUtil.compress(v));
		} catch (DBException | IOException e) {
			throw new DatabaseException(
					"Could not put data from key [" + KEY_BINDER + "] and Block [" + value + "]: " + e.getMessage());
		}
	}

	@Override
	public void delete() {
		LOG.trace("[Crypto] ChainstateDB Deleted - Key: " + KEY_BINDER);
		blockDb.delete(KEY_BINDER.getBytes());
	}

}
