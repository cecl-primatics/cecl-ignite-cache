package com.primatics.ignite.dto;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
	private String key;
	private String scenario;
	private String loanId;
	private Double balance;
	private Double[] survival;
	private Double[] lossRate;
	private Double[] lossAmount;

	public Loan() {
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getScenario() {
		return scenario;
	}

	public void setScenario(String scenario) {
		this.scenario = scenario;
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
		return "Loan [scenario=" + scenario + ", key=" + key + ", loanId=" + loanId + ", balance=" + balance + ", survival=" + survival + ", lossRate=" + lossRate + ", lossAmount=" + lossAmount
				+ "]";
	}

	public Loan(String key, String scenario, String loanId, Double balance, Double[] survival, Double[] lossRate, Double[] lossAmount) {
		super();
		this.scenario = scenario;
		this.key = key;
		this.loanId = loanId;
		this.balance = balance;
		this.survival = survival;
		this.lossRate = lossRate;
		this.lossAmount = lossAmount;
	}
}