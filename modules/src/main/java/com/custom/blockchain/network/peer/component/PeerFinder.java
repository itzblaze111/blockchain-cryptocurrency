package com.custom.blockchain.network.peer.component;

import static com.custom.blockchain.costants.SystemConstants.MAXIMUM_SEEDS;
import static com.custom.blockchain.costants.SystemConstants.PEERS_FILE;
import static com.custom.blockchain.properties.BlockchainImutableProperties.PEERS;
import static com.custom.blockchain.properties.BlockchainImutableProperties.PEERS_STATUS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.custom.blockchain.network.exception.NetworkException;
import com.custom.blockchain.network.peer.Peer;
import com.custom.blockchain.util.OsUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author marcosrachid
 *
 */
@Component
public class PeerFinder {

	@Value("${application.name:'Rachid Coin'}")
	private String coinName;

	public ObjectMapper objectMapper;

	public PeerFinder(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * 
	 * @param peers
	 */
	public void findPeers() {
		findFromFile();
		findFromDNS();
		findFromPeers();
	}

	private void findFromFile() {
		String path = String.format(OsUtil.getRootDirectory(), coinName);
		List<Peer> filePeers;
		try {
			filePeers = objectMapper.readValue(new File(path + PEERS_FILE), new TypeReference<List<Peer>>() {
			});
			for (Peer peer : new ArrayList<Peer>(filePeers)) {
				if (PEERS_STATUS.containsKey(peer) && !PEERS_STATUS.get(peer))
					filePeers.remove(peer);
			}
		} catch (IOException e) {
			throw new NetworkException("Could not read peers data: " + e.getMessage());
		}
		for (Peer peer : filePeers) {
			PEERS.addPeer(peer);
		}

	}

	private void findFromDNS() {
		if (PEERS.size() >= MAXIMUM_SEEDS)
			return;
	}

	private void findFromPeers() {
		if (PEERS.size() >= MAXIMUM_SEEDS)
			return;
	}

}
