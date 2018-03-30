package com.custom.blockchain.node.network.component;

import static com.custom.blockchain.node.NodeStateManagement.BLOCKS_QUEUE;
import static com.custom.blockchain.node.NodeStateManagement.DIFFICULTY_ADJUSTMENT_BLOCK;
import static com.custom.blockchain.node.NodeStateManagement.SERVICES;
import static com.custom.blockchain.node.network.peer.PeerStateManagement.PEERS;
import static com.custom.blockchain.node.network.peer.PeerStateManagement.PEERS_STATUS;
import static com.custom.blockchain.node.network.peer.PeerStateManagement.REMOVED_PEERS;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.custom.blockchain.block.Block;
import com.custom.blockchain.block.BlockStateManagement;
import com.custom.blockchain.block.exception.BlockException;
import com.custom.blockchain.configuration.properties.BlockchainProperties;
import com.custom.blockchain.data.block.CurrentBlockDB;
import com.custom.blockchain.node.network.Service;
import com.custom.blockchain.node.network.exception.NetworkException;
import com.custom.blockchain.node.network.peer.Peer;
import com.custom.blockchain.node.network.peer.component.PeerSender;
import com.custom.blockchain.node.network.request.BlockchainRequest;
import com.custom.blockchain.node.network.request.arguments.BlockArguments;
import com.custom.blockchain.node.network.request.arguments.BlockResponseArguments;
import com.custom.blockchain.node.network.request.arguments.PeerResponseArguments;
import com.custom.blockchain.node.network.request.arguments.StateResponseArguments;
import com.custom.blockchain.node.network.request.arguments.TransactionsResponseArguments;
import com.custom.blockchain.transaction.SimpleTransaction;
import com.custom.blockchain.transaction.component.TransactionMempool;
import com.custom.blockchain.transaction.exception.TransactionException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author marcosrachid
 *
 */
@Component
public class ServiceDispatcher {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceDispatcher.class);

	private ObjectMapper objectMapper;

	private BlockchainProperties blockchainProperties;

	private PeerSender peerSender;

	private CurrentBlockDB currentBlockChainstateDB;

	private BlockStateManagement blockStateManagement;

	private TransactionMempool transactionMempool;

	private Socket clientSocket;

	private Peer peer;

	public ServiceDispatcher(final ObjectMapper objectMapper, final BlockchainProperties blockchainProperties,
			final PeerSender peerSender, final CurrentBlockDB currentBlockChainstateDB,
			final BlockStateManagement blockStateManagement, final TransactionMempool transactionMempool) {
		this.objectMapper = objectMapper;
		this.blockchainProperties = blockchainProperties;
		this.peerSender = peerSender;
		this.currentBlockChainstateDB = currentBlockChainstateDB;
		this.blockStateManagement = blockStateManagement;
		this.transactionMempool = transactionMempool;
	}

	/**
	 * 
	 * @param clientSocket
	 * @param peer
	 * @param request
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void launch(Socket clientSocket, Peer peer, BlockchainRequest request) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		LOG.trace("[Crypto] Service: " + request.getService() + ", Arguments: " + request.getArguments());
		this.clientSocket = clientSocket;
		this.peer = peer;
		if (!SERVICES.stream().map(s -> s.getService()).collect(Collectors.toList())
				.contains(request.getService().getService()))
			return;
		if (request.hasArguments()) {
			LOG.trace("[Crypto] Request with arguments: " + request.getService().getService() + " - "
					+ request.getArguments());
			this.getClass().getDeclaredMethod(request.getService().getService(), request.getArguments().getClass())
					.invoke(this, request.getArguments());
		} else {
			LOG.trace("[Crypto] Request without arguments: " + request.getService().getService());
			this.getClass().getDeclaredMethod(request.getService().getService()).invoke(this);
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private void ping() {
		LOG.trace("[Crypto] Found a " + Service.PING.getService() + " event from peer [" + peer + "]");
		simpleSend(BlockchainRequest.createBuilder().withService(Service.PONG).build());
		Optional<Peer> foundPeer = PEERS.stream().filter(p -> p.equals(peer)).findFirst();
		if (foundPeer.isPresent()) {
			Peer p = foundPeer.get();
			p.setLastConnected(LocalDateTime.now());
			PEERS.add(p);
		} else {
			peer.setCreateDatetime(LocalDateTime.now());
			peer.setLastConnected(LocalDateTime.now());
			PEERS.add(peer);
		}
		PEERS_STATUS.put(peer, LocalDateTime.now());
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private void pong() {
		LOG.trace("[Crypto] Found a " + Service.PONG.getService() + " event");
		LOG.trace(String.format("[Crypto] node [%s] successfully answered", clientSocket.toString()));
		simpleSend(BlockchainRequest.createBuilder().withService(Service.PING).build());
		Optional<Peer> foundPeer = PEERS.stream().filter(p -> p.equals(peer)).findFirst();
		if (foundPeer.isPresent()) {
			Peer p = foundPeer.get();
			p.setLastConnected(LocalDateTime.now());
			PEERS.add(p);
		} else {
			peer.setCreateDatetime(LocalDateTime.now());
			peer.setLastConnected(LocalDateTime.now());
			PEERS.add(peer);
		}
		PEERS_STATUS.put(peer, LocalDateTime.now());
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private void getState() {
		LOG.trace("[Crypto] Found a " + Service.GET_STATE.getService() + " event");
		simpleSend(BlockchainRequest.createBuilder().withService(Service.GET_STATE_RESPONSE)
				.withArguments(new StateResponseArguments(currentBlockChainstateDB.get().getHeight())).build());
	}

	/**
	 * 
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void getStateResponse(StateResponseArguments args) {
		LOG.trace("[Crypto] Found a " + Service.GET_STATE_RESPONSE.getService() + " event");
		Long currentBlock = args.getCurrentBlock();
		LOG.debug("[Crypto] peer [" + peer + "] current block [" + currentBlock + "]");
		Long currentMappedHeight = (currentBlockChainstateDB.get().getHeight() + BLOCKS_QUEUE.size());
		if (currentBlock > currentMappedHeight) {
			LOG.info("[Crypto] Found new block from peer [" + peer + "], requesting block...");
			long blockNumber = currentBlock - currentBlockChainstateDB.get().getHeight();
			for (long i = (currentMappedHeight + 1); i <= currentBlock; i++) {
				BLOCKS_QUEUE.add(new BlockArguments(i));
			}
		}
	}

	/**
	 * 
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void getBlock(BlockResponseArguments args) {
		LOG.trace("[Crypto] Found a " + Service.GET_BLOCK.getService() + " event");
		// simpleSend(Service.GET_BLOCK_RESPONSE,
		// objectMapper.writeValueAsString(arg0));
	}

	/**
	 * 
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void getBlockResponse(BlockResponseArguments args) {
		LOG.trace("[Crypto] Found a " + Service.GET_BLOCK_RESPONSE.getService() + " event");
		Block block = args.getBlock();
		try {
			if (BLOCKS_QUEUE.peek().getHeight().equals(block.getHeight())) {
				blockStateManagement.foundBlock(block);
				BLOCKS_QUEUE.poll();
			}
			if (block.getHeight() % DIFFICULTY_ADJUSTMENT_BLOCK == 0) {
				// TODO: adjust difficulty
			}
		} catch (BlockException e) {
			throw new NetworkException("Block[" + block + "] found error: " + e.getMessage());
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private void getPeers() {
		LOG.trace("[Crypto] Found a " + Service.GET_PEERS.getService() + " event");
		simpleSend(BlockchainRequest.createBuilder().withService(Service.GET_PEERS_RESPONSE)
				.withArguments(new PeerResponseArguments(PEERS)).build());
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private void getPeersResponse(PeerResponseArguments peers) {
		LOG.trace("[Crypto] Found a " + Service.GET_PEERS_RESPONSE.getService() + " event");
		JavaType collectionPeerClass = objectMapper.getTypeFactory().constructCollectionType(Set.class, Peer.class);
		Set<Peer> requestPeers = peers.getPeers();
		requestPeers.removeAll(REMOVED_PEERS);
		requestPeers.removeAll(PEERS);
		requestPeers.forEach(p -> p.setCreateDatetime(LocalDateTime.now()));
		PEERS.addAll(requestPeers);
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private void getTransactions() {
		try {
			LOG.trace("[Crypto] Found a " + Service.GET_TRANSACTIONS.getService() + " event");
			simpleSend(BlockchainRequest.createBuilder().withService(Service.GET_TRANSACTIONS_RESPONSE)
					.withArguments(new TransactionsResponseArguments(transactionMempool.getUnminedTransactions()))
					.build());
			transactionMempool.restartMempool();
		} catch (TransactionException e) {
			throw new NetworkException("Could not restart mempool: " + e.getMessage());
		}

	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private void getTransactionsResponse(TransactionsResponseArguments args) {
		LOG.trace("[Crypto] Found a " + Service.GET_TRANSACTIONS_RESPONSE.getService() + " event");
		for (SimpleTransaction t : args.getTransactions()) {
			try {
				transactionMempool.updateMempool(t);
			} catch (TransactionException e) {
				throw new NetworkException("Transaction[" + t + "] found error: " + e.getMessage());
			}
		}
	}

	/**
	 * 
	 * @param request
	 */
	private void simpleSend(BlockchainRequest request) {
		request.setSignature(blockchainProperties.getNetworkSignature());
		peerSender.connect(peer);
		peerSender.send(request);
		peerSender.close();
	}

}
