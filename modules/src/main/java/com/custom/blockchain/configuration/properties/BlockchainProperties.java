package com.custom.blockchain.configuration.properties;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.custom.blockchain.util.DigestUtil;

@ConfigurationProperties(prefix = "application.blockchain")
@Validated
@Component
public class BlockchainProperties {

	@NotBlank
	private String coinName;

	@NotBlank
	private String version;

	@NotNull
	@Min(0)
	private BigDecimal minimunTransaction;

	@NotNull
	@Min(0)
	private BigDecimal coinLimit;

	@NotNull
	@Min(0)
	private Long miningTimeRate;

	@NotNull
	@Min(0)
	private BigDecimal reward;

	@NotNull
	@Min(0)
	private Long blockSize;

	@NotBlank
	private String coinbase;

	private BigDecimal premined;

	@NotNull
	@Min(1025)
	@Max(65536)
	private Integer networkPort;

	@NotNull
	@Min(0)
	private Integer networkMaximumSeeds;

	private List<String> networkMockedPeers;

	private String miner;

	public String getCoinName() {
		return coinName;
	}

	public void setCoinName(String coinName) {
		this.coinName = coinName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public BigDecimal getMinimunTransaction() {
		return minimunTransaction;
	}

	public void setMinimunTransaction(BigDecimal minimunTransaction) {
		this.minimunTransaction = minimunTransaction;
	}

	public BigDecimal getCoinLimit() {
		return coinLimit;
	}

	public void setCoinLimit(BigDecimal coinLimit) {
		this.coinLimit = coinLimit;
	}

	public String getCoinbase() {
		return coinbase;
	}

	public void setCoinbase(String coinbase) {
		this.coinbase = coinbase;
	}

	public BigDecimal getPremined() {
		return premined;
	}

	public void setPremined(BigDecimal premined) {
		this.premined = premined;
	}

	public Integer getNetworkPort() {
		return networkPort;
	}

	public void setNetworkPort(Integer networkPort) {
		this.networkPort = networkPort;
	}

	public Integer getNetworkMaximumSeeds() {
		return networkMaximumSeeds;
	}

	public void setNetworkMaximumSeeds(Integer networkMaximumSeeds) {
		this.networkMaximumSeeds = networkMaximumSeeds;
	}

	public List<String> getNetworkMockedPeers() {
		return networkMockedPeers;
	}

	public void setNetworkMockedPeers(List<String> networkMockedPeers) {
		this.networkMockedPeers = networkMockedPeers;
	}

	public String getMiner() {
		return miner;
	}

	public void setMiner(String miner) {
		this.miner = miner;
	}

	public Long getMiningTimeRate() {
		return miningTimeRate;
	}

	public void setMiningTimeRate(Long miningTimeRate) {
		this.miningTimeRate = miningTimeRate;
	}

	public BigDecimal getReward() {
		return reward;
	}

	public void setReward(BigDecimal reward) {
		this.reward = reward;
	}

	public Long getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(Long blockSize) {
		this.blockSize = blockSize;
	}

	public String getNetworkSignature() {
		return DigestUtil.applySha256(DigestUtil.applySha256(coinName + version + minimunTransaction.toPlainString()
				+ coinLimit.toPlainString() + miningTimeRate + reward.toPlainString() + blockSize));
	}

}
