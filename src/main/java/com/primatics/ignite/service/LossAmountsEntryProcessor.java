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

	@Override
	public Loan process(final MutableEntry<Integer, Loan> entry, final Object... arguments) throws EntryProcessorException {
		final Loan loan = entry.getValue();
		final Double balanceAmount = loan.getBalance();
		final Double[] survivalRates = loan.getSurvival();
		final Double[] lossRates = loan.getLossRate();
		final Double[] lossAmounts = loan.getLossAmount() == null ? new Double[16] : loan.getLossAmount();

		for (int i = 1; i <= 16; i++) {
			final Double survivalRate = survivalRates[i - 1];
			final Double lossRate = lossRates[i - 1];
			final Double resultAmount = balanceAmount * survivalRate * lossRate;
			lossAmounts[i - 1] = resultAmount;
		}
		
		loan.setLossAmount(lossAmounts);
		
		return loan;
	}

}
