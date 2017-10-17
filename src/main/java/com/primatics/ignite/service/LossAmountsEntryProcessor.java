package com.primatics.ignite.service;

import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;

import org.apache.ignite.cache.CacheEntryProcessor;

import com.primatics.ignite.dto.Loan;

public class LossAmountsEntryProcessor implements CacheEntryProcessor<Integer, Loan, Loan> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 233486996462458966L;
	private Double[] survivalScalingFactor;
	private Double[] lossRateScalingFactor;

	public LossAmountsEntryProcessor(final Double[] survivalScalingFactor, final Double[] lossRateScalingFactor) {
		this.survivalScalingFactor = survivalScalingFactor;
		this.lossRateScalingFactor = lossRateScalingFactor;
	}

	@Override
	public Loan process(final MutableEntry<Integer, Loan> entry, final Object... arguments) throws EntryProcessorException {
		final Loan loan = entry.getValue();
		final Double balanceAmount = loan.getBalance();	
		final Double[] survivalRates = loan.getSurvival();
		final Double[] lossRates = loan.getLossRate();
		final Double[] lossAmounts = loan.getLossAmount() == null ? new Double[16] : loan.getLossAmount();

		if (loan == null || balanceAmount == null || survivalRates == null || lossRates == null) {
			throw new EntryProcessorException(loan.getLoanId() + ": Null Values.");
		}
		
		final int survivalRatesSize = survivalRates.length;
		
		if (lossRates.length != survivalRatesSize) {
			throw new EntryProcessorException(loan.getLoanId() + ": Rates not of correct length");
		}
		
		if (survivalScalingFactor.length < survivalRatesSize || lossRateScalingFactor.length < lossRates.length) {
			throw new EntryProcessorException(loan.getLoanId() + ": Scaling factors not of correct length");
		}
		
		for (int i = 0; i < survivalRatesSize; i++) {
			final Double survivalRate = survivalRates[i];
			final Double lossRate = lossRates[i];
			final Double resultAmount = balanceAmount * survivalRate * survivalScalingFactor[i] * lossRate * lossRateScalingFactor[i];
			lossAmounts[i] = resultAmount;
		}
		
		loan.setLossAmount(lossAmounts);
		
		return loan;
	}
}