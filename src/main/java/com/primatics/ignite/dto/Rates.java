package com.primatics.ignite.dto;

import java.io.Serializable;
import java.util.Arrays;

public class Rates implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Rates() {}
	
	public Rates(Double[] survival, Double[] lossRate) {
		super();
		Arrays.fill(survival, 1.0);
		Arrays.fill(lossRate, 1.0);
		this.survival = survival;
		this.lossRate = lossRate;
	}

	private Double[] survival;
	private Double[] lossRate;
	
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
	
	@Override
	public String toString() {
		return "Rates [survival=" + Arrays.toString(survival) + ", lossRate=" + Arrays.toString(lossRate) + "]";
	}

}
