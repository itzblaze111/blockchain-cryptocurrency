package com.custom.blockchain.block;

import static com.custom.blockchain.properties.BlockchainImutableProperties.BLOCKCHAIN;

import com.custom.blockchain.block.exception.BlockException;

/**
 * 
 * @author marcosrachid
 *
 */
public class BlockFactory {

	/**
	 * 
	 * @param blockType
	 * @param previousBlock
	 * @return
	 * @throws BlockException
	 */
	public static Block getBlock(BlockType blockType, Block previousBlock) throws BlockException {
		if (blockType == BlockType.GENESIS) {
			if (!BLOCKCHAIN.isEmpty()) {
				throw new BlockException("Blockchain already started to create genesis block");
			}
			return new Genesis();
		} else {
			return new Block(previousBlock.getHash());
		}
	}

	/**
	 * 
	 * @param previousBlock
	 * @return
	 */
	public static Block getBlock(Block previousBlock) {
		return new Block(previousBlock.getHash());
	}

}
