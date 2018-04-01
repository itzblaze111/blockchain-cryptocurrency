package com.custom.blockchain.node.component;

import static com.custom.blockchain.node.NodeStateManagement.DIFFICULTY_ADJUSTMENT_BLOCK;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.custom.blockchain.block.Block;
import com.custom.blockchain.configuration.properties.BlockchainProperties;
import com.custom.blockchain.data.block.BlockDB;
import com.custom.blockchain.data.block.CurrentBlockDB;

/**
 * 
 * @author marcosrachid
 *
 */
@Component
public class DifficultyAdjustment {

	private static final Logger LOG = LoggerFactory.getLogger(DifficultyAdjustment.class);

	private static final int MIN_DIFFICULTY = 0;

	private static final int MAX_DIFFICULTY = 32;

	private static final BigDecimal MARGIN_OF_ERROR = new BigDecimal(0.2);

	private BlockchainProperties blockchainProperties;

	private BlockDB blockDB;

	private CurrentBlockDB currentBlockDB;

	public DifficultyAdjustment(final BlockchainProperties blockchainProperties, final BlockDB blockDB,
			final CurrentBlockDB currentBlockDB) {
		this.blockchainProperties = blockchainProperties;
		this.blockDB = blockDB;
		this.currentBlockDB = currentBlockDB;
	}

	/**
	 * 
	 * @return
	 */
	public Integer adjust() {
		LOG.info("[Crypto] Difficulty adjustment every " + DIFFICULTY_ADJUSTMENT_BLOCK + " blocks starting...");
		Block currentBlock = currentBlockDB.get();
		Integer difficulty = currentBlock.getRewardTransaction().getDifficulty();
		List<Long> timestamps = new ArrayList<>();
		List<Long> differences = new ArrayList<>();
		mapTimestamps(DIFFICULTY_ADJUSTMENT_BLOCK - 1, currentBlockDB.get(), timestamps);
		for (int i = 0; i < timestamps.size(); i++) {
			if (i % 2 != 0)
				differences.add(timestamps.get(i) - timestamps.get(i - 1));
		}
		BigDecimal average = new BigDecimal(differences.stream().mapToDouble(t -> t).average().getAsDouble(),
				MathContext.DECIMAL64);
		BigDecimal top = blockchainProperties.getMiningTimeRate().multiply(BigDecimal.ONE.add(MARGIN_OF_ERROR));
		BigDecimal bottom = blockchainProperties.getMiningTimeRate().multiply(BigDecimal.ONE.subtract(MARGIN_OF_ERROR));
		if (average.compareTo(top) > 0 && difficulty.compareTo(MIN_DIFFICULTY) > 0)
			return (difficulty - 1);
		if (average.compareTo(bottom) < 0 && difficulty.compareTo(MAX_DIFFICULTY) < 0)
			return (difficulty + 1);
		return difficulty;

	}

	/**
	 * 
	 * @param adjustmentLoop
	 * @param block
	 * @param timestamps
	 */
	private void mapTimestamps(Integer adjustmentLoop, Block block, final List<Long> timestamps) {
		if (adjustmentLoop == 0)
			return;
		timestamps.add(block.getTimeStamp());
		adjustmentLoop--;
		mapTimestamps(adjustmentLoop, blockDB.get(block.getPreviousHash()), timestamps);
	}

}
