package com.primatics.ignite.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.cache.processor.EntryProcessorResult;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
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

	@Autowired
	private MongoTemplate mongoTemplate;
	
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
		CloseableIterator<Loan> loansIt = mongoTemplate.stream(new Query(), Loan.class);
		System.out.println(" ** Loans from mongoDB into map " + watch.stop());
		
		Stopwatch watch3 = Stopwatch.createStarted();
		List<Loan> list = StreamUtils.asStream(loansIt).collect(Collectors.toList());;
		System.out.println(list.size()+" ** Iterables to List " + watch3.stop());
		
		Stopwatch watch2 = Stopwatch.createStarted();
		Map<Integer, Loan> mapLoans = new HashMap<Integer, Loan>();
		mapLoans = list.stream().collect(Collectors.toMap(Loan::getKey, item -> item));
		System.out.println(mapLoans.size()+" ** List into map " + watch2.stop());
		
		Stopwatch watch1 = Stopwatch.createStarted();
		cache.putAll(mapLoans);
		watch1 = watch1.stop();
		System.out.println(mapLoans.size()+" ** Loans Loaded in Cache " + watch1);
		return watch1;
	}
}
