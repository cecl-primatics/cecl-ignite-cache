package com.primatics.ignite.dto;

import java.math.BigDecimal;
import java.util.Arrays;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "analyzedloans")
public class AnalyzedLoan {
	
	@Id
	private String key;
	private String scenario;
	private BigDecimal totalBalance;
	private BigDecimal[] totalLossAmounts;
	private Double[] survival;
	private Double[] lossRate;
	private BigDecimal totalLoss;
	
	public AnalyzedLoan() {}
	
	public AnalyzedLoan(String key, String scenario, BigDecimal totalBalance, BigDecimal[] totalLossAmounts, BigDecimal totalLoss) {
		super();
		this.key = key;
		this.scenario = scenario;
		this.totalBalance = totalBalance;
		this.totalLossAmounts = totalLossAmounts;
		this.totalLoss = totalLoss;
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
	public BigDecimal getTotalBalance() {
		return totalBalance;
	}
	public void setTotalBalance(BigDecimal totalBalance) {
		this.totalBalance = totalBalance;
	}
	public BigDecimal[] getTotalLossAmounts() {
		return totalLossAmounts;
	}
	public void setTotalLossAmounts(BigDecimal[] totalLossAmounts) {
		this.totalLossAmounts = totalLossAmounts;
	}

	@Override
	public String toString() {
		return "AnalyzedLoan [key=" + key + ", scenario=" + scenario + ", totalBalance=" + totalBalance
				+ ", totalLossAmounts=" + Arrays.toString(totalLossAmounts) + ", totalLoss=" + totalLoss +"]";
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

	public BigDecimal getTotalLoss() {
		return totalLoss;
	}

	public void setTotalLoss(BigDecimal totalLoss) {
		this.totalLoss = totalLoss;
	}
}