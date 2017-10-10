package com.primatics.ignite.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.cache.processor.EntryProcessorResult;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;
import com.primatics.ignite.dto.Loan;
import com.primatics.ignite.repository.LoanRepository;

/**
 * Created by Rama Arun on 02/10/2017.
 */
@Component
public class LoanIgnite implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5398226097451383976L;
	static Ignite ignite = null;
	
	@Autowired
	private LoanRepository loanRepository;

	public void start() {
		ignite = Ignition.start("classpath:loss-amount-config-client.xml");
	}

	public Stopwatch getDatFromCache() {

		System.out.println("Loan Ignite");
		final IgniteCache<Integer, Loan> cache = ignite.cache("loanCache");
		Stopwatch watch = Stopwatch.createStarted();

		System.out.println("----cache---------" + cache.size(CachePeekMode.PRIMARY));
		Set<Integer> keys = loanRepository.getKeys();
		// fastest
		final Map<Integer, EntryProcessorResult<Loan>> results = cache.<Loan>invokeAll(keys, new LossAmountsEntryProcessor());
		watch = watch.stop();
		System.out.println(
				"computation and update took: " + watch + " ----------- " + cache.size(CachePeekMode.PRIMARY) + " ------ ");
		return watch;
	}

	public Stopwatch initializeLoans() {
		
		final IgniteCache<Integer, Loan> cache = ignite.cache("loanCache");
		cache.clear();
		
		Stopwatch watch = Stopwatch.createStarted();
		final List<Loan> loans = loanRepository.findAll();
		
		final Map<Integer, Loan> mapLoans = new HashMap<Integer, Loan>(loans.size());

		for (final Loan loan : loans) {
			mapLoans.put(loan.getKey(), loan);
		}

		cache.putAll(mapLoans);
		
		watch = watch.stop();

		System.out.println(mapLoans.size()+" ** Loans Loaded in Cache " + watch);
		return watch;
	}
}
