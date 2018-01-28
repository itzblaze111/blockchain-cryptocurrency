package com.custom.blockchain.service;

import static com.custom.blockchain.constants.BlockchainConstants.BLOCKCHAIN;
import static com.custom.blockchain.constants.BlockchainConstants.DIFFICULTY;

import org.springframework.stereotype.Service;

import com.custom.blockchain.block.Block;
import com.custom.blockchain.util.StringUtil;
import com.custom.blockchain.util.TransactionUtil;

@Service
public class BlockService {

	public void mineBlock(Block block) {
		block.setMerkleRoot(TransactionUtil.getMerkleRoot(block.getTransactions()));
		String target = StringUtil.getDificultyString(DIFFICULTY);
		while (!block.getHash().substring(0, DIFFICULTY).equals(target)) {
			block.setNonce(block.getNonce() + 1);
			block.calculateHash();
		}
		BLOCKCHAIN.add(block);
		System.out.println("Block Mined!!! : " + block.getHash());
	}

}
