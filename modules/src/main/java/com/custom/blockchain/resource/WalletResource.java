package com.custom.blockchain.resource;

import static com.custom.blockchain.costants.LogMessagesConstants.REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.custom.blockchain.handler.WalletHandler;
import com.custom.blockchain.resource.dto.request.RequestBalanceDTO;
import com.custom.blockchain.resource.dto.request.RequestImportDTO;
import com.custom.blockchain.resource.dto.response.ResponseDTO;

/**
 * 
 * @author marcosrachid
 *
 */
@Profile("!miner")
@RestController
@RequestMapping(value = "/wallet")
public class WalletResource {

	private static final Logger LOG = LoggerFactory.getLogger(WalletResource.class);

	private WalletHandler walletHandler;

	public WalletResource(final WalletHandler walletHandler) {
		this.walletHandler = walletHandler;
	}

	/**
	 * 
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/balance", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<ResponseDTO> getBalance(@Valid @RequestBody RequestBalanceDTO publicKey) throws Exception {
		LOG.debug(REQUEST, publicKey);
		return ResponseEntity.status(HttpStatus.OK).contentType(APPLICATION_JSON_UTF8)
				.body(new ResponseDTO(walletHandler.getBalance(publicKey.getPublicKey())));
	}

	/**
	 * 
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/import", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<ResponseDTO> importWallet(@Valid @RequestBody RequestImportDTO privateKey) throws Exception {
		LOG.debug(REQUEST, privateKey);
		return ResponseEntity.status(HttpStatus.ACCEPTED).contentType(APPLICATION_JSON_UTF8)
				.body(new ResponseDTO(walletHandler.importWallet(privateKey.getPrivateKey())));
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<ResponseDTO> create() throws Exception {
		return ResponseEntity.status(HttpStatus.CREATED).contentType(APPLICATION_JSON_UTF8)
				.body(new ResponseDTO(walletHandler.createWallet()));
	}

	/**
	 * 
	 * @param publicKeys
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/current", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<ResponseDTO> getCurrentWallet() throws Exception {
		return ResponseEntity.status(HttpStatus.OK).contentType(APPLICATION_JSON_UTF8)
				.body(new ResponseDTO(walletHandler.getCurrentWallet()));
	}

	/**
	 * 
	 * @param publicKeys
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/current/balance", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<ResponseDTO> getCurrentBalance() throws Exception {
		return ResponseEntity.status(HttpStatus.OK).contentType(APPLICATION_JSON_UTF8)
				.body(new ResponseDTO(walletHandler.getCurrentWalletBalance()));
	}

}
