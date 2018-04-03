package com.custom.blockchain.service;

import static com.custom.blockchain.node.network.peer.PeerStateManagement.PEERS;
import static com.custom.blockchain.node.network.peer.PeerStateManagement.PEERS_STATUS;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.custom.blockchain.exception.BusinessException;
import com.custom.blockchain.node.network.peer.Peer;

/**
 * 
 * @author marcosrachid
 *
 */
@Service
public class PeerService {

	public PeerService() {
	}

	/**
	 * 
	 * @param ip
	 * @param serverPort
	 * @throws BusinessException
	 */
	public void addPeer(String ip, int serverPort) throws BusinessException {
		Peer peer = new Peer(ip, serverPort);
		if (PEERS.contains(peer) && PEERS_STATUS.containsKey(peer) && PEERS_STATUS.get(peer) != null) {
			throw new BusinessException(String.format("Peer %s is already registered on peer's list", peer));
		}
		if (PEERS.contains(peer) && !PEERS_STATUS.containsKey(peer)) {
			throw new BusinessException(String.format("Peer %s is on queue to try a connection", peer));
		}
		peer.setCreateDatetime(LocalDateTime.now());
		PEERS.add(peer);
	}

	/**
	 * 
	 * @param peer
	 * @throws BusinessException
	 */
	public void addPeer(Peer peer) throws BusinessException {
		if (PEERS.contains(peer) && PEERS_STATUS.containsKey(peer) && PEERS_STATUS.get(peer) != null) {
			throw new BusinessException(String.format("Peer %s is already registered on peer's list", peer));
		}
		if (PEERS.contains(peer) && !PEERS_STATUS.containsKey(peer)) {
			throw new BusinessException(String.format("Peer %s is on queue to try a connection", peer));
		}
		peer.setCreateDatetime(LocalDateTime.now());
		PEERS.add(peer);
	}

}
