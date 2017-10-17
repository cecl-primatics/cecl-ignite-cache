package com.primatics.ignite.dto;

import java.math.BigDecimal;
import java.util.Arrays;

public class AnalyzedLoan {
	
	private Integer key;
	private String portfolio;
	private BigDecimal totalBalance;
	private BigDecimal[] totalLossAmounts;
	private Double[] survival;
	private Double[] lossRate;
	private BigDecimal totalLoss;
	
	public AnalyzedLoan() {}
	
	public AnalyzedLoan(Integer key, String portfolio, BigDecimal totalBalance, BigDecimal[] totalLossAmounts, BigDecimal totalLoss) {
		super();
		this.key = key;
		this.portfolio = portfolio;
		this.totalBalance = totalBalance;
		this.totalLossAmounts = totalLossAmounts;
		this.totalLoss = totalLoss;
	}
	
	public Integer getKey() {
		return key;
	}
	public void setKey(Integer key) {
		this.key = key;
	}
	public String getPortfolio() {
		return portfolio;
	}
	public void setPortfolio(String portfolio) {
		this.portfolio = portfolio;
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
		return "AnalyzedLoan [key=" + key + ", portfolio=" + portfolio + ", totalBalance=" + totalBalance
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