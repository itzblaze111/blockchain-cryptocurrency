package com.custom.blockchain.resource.exception.handler;

import static com.custom.blockchain.costants.LogMessages.ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.custom.blockchain.block.exception.BlockException;
import com.custom.blockchain.resource.dto.response.ResponseDTO;
import com.custom.blockchain.resource.dto.response.ResponseDTO.ResponseDTOBuilder;
import com.custom.blockchain.resource.dto.response.ResponseErrorsDTO;
import com.custom.blockchain.resource.dto.response.ResponseFieldErrorsDTO;
import com.custom.blockchain.transaction.exception.TransactionException;
import com.custom.blockchain.wallet.exception.WalletException;

@ControllerAdvice(basePackages = "com.custom.blockchain.resource")
public class ResourceExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceExceptionHandler.class);

	@ExceptionHandler(BlockException.class)
	public ResponseEntity<ResponseDTO> handleBlockException(BlockException e) {
		LOG.error(ERROR, e.getMessage(), ExceptionUtils.getStackTrace(e));
		return ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_JSON).body(ResponseDTO.createBuilder()
				.withError(new ResponseErrorsDTO(BAD_REQUEST.value(), e.getMessage())).build());
	}

	@ExceptionHandler(TransactionException.class)
	public ResponseEntity<ResponseDTO> handleTransactionException(TransactionException e) {
		LOG.error(ERROR, e.getMessage(), ExceptionUtils.getStackTrace(e));
		return ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_JSON).body(ResponseDTO.createBuilder()
				.withError(new ResponseErrorsDTO(BAD_REQUEST.value(), e.getMessage())).build());
	}

	@ExceptionHandler(WalletException.class)
	public ResponseEntity<ResponseDTO> handleWalletException(WalletException e) {
		LOG.error(ERROR, e.getMessage(), ExceptionUtils.getStackTrace(e));
		return ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_JSON).body(ResponseDTO.createBuilder()
				.withError(new ResponseErrorsDTO(BAD_REQUEST.value(), e.getMessage())).build());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseDTO> handleFieldsException(MethodArgumentNotValidException e) {
		LOG.error(ERROR, e.getMessage(), ExceptionUtils.getStackTrace(e));
		ResponseDTOBuilder builder = ResponseDTO.createBuilder();
		e.getBindingResult().getFieldErrors().forEach(ex -> {
			builder.withError(new ResponseFieldErrorsDTO(BAD_REQUEST.value(), ex.getDefaultMessage(), ex.getField()));
		});
		return ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_JSON).body(builder.build());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseDTO> handleException(Exception e) {
		LOG.error(ERROR, e.getMessage(), ExceptionUtils.getStackTrace(e));
		return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(APPLICATION_JSON)
				.body(ResponseDTO.createBuilder()
						.withError(new ResponseErrorsDTO(INTERNAL_SERVER_ERROR.value(), e.getMessage())).build());
	}

}
