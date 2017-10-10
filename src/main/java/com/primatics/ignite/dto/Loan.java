package com.primatics.ignite.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by Rama Arun on 02/10/2017.
 */
@Document(collection = "loans")
public class Loan implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4175385848044131093L;
	@Id
	private Integer key;
	private String loanId;
	private Double balance;
	private Double[] survival;
	private Double[] lossRate;
	private Double[] lossAmount;

	public Loan() {
	}
	
	public Integer getKey() {
		return key;
	}

	public void setKey(Integer key) {
		this.key = key;
	}

	public String getLoanId() {
		return loanId;
	}

	public void setLoanId(String loanId) {
		this.loanId = loanId;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public Double[] getSurvival() {
		return survival;
	}

	public void setSurvival(Double[] survival) {
		this.survival = survival;
	}

	public Double[] getLossRate() {
		return lossRate;
	}

	public void setLossRate(Double[] lossRate) {
		this.lossRate = lossRate;
	}
	
	public Double[] getLossAmount() {
		return lossAmount;
	}

	public void setLossAmount(Double[] lossAmount) {
		this.lossAmount = lossAmount;
	}

	@Override
	public String toString() {
		return "Loan [key=" + key + ", loanId=" + loanId + ", balance=" + balance + ", survival=" + survival + ", lossRate=" + lossRate + ", lossAmount=" + lossAmount
				+ "]";
	}

	public Loan(Integer key, String loanId, Double balance, Double[] survival, Double[] lossRate, Double[] lossAmount) {
		super();
		this.key = key;
		this.loanId = loanId;
		this.balance = balance;
		this.survival = survival;
		this.lossRate = lossRate;
		this.lossAmount = lossAmount;
	}
}