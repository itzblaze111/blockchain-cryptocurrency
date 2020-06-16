package com.custom.blockchain.transaction;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * 
 * @author marcosrachid
 *
 */
@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = RewardTransaction.class, name = "reward"),
		@Type(value = SimpleTransaction.class, name = "transaction") })
public abstract class Transaction implements Serializable, Comparable<Transaction> {

	private static final long serialVersionUID = 1L;

	protected String transactionId;
	protected BigDecimal value;
	protected Long timeStamp;

	public static int sequence = 0;

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * 
	 * @param arg
	 */
	@JsonIgnore
	public abstract void applyFees(BigDecimal arg);

	/**
	 * 
	 * @return
	 */
	@JsonIgnore
	public abstract BigDecimal getInputsValue();

	/**
	 * 
	 * @return
	 */
	@JsonIgnore
	public abstract BigDecimal getOutputsValue();

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(transactionId).append(value).append(timeStamp).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		return new EqualsBuilder().append(transactionId, other.transactionId).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("transactionId", transactionId).append("value", value)
				.append("timeStamp", timeStamp).build();
	}

	@Override
	public int compareTo(Transaction o) {
		return this.getTimeStamp().compareTo(o.getTimeStamp());
	}

}
