package com.primatics.ignite.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.cache.processor.EntryProcessorResult;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheEntry;
import org.apache.ignite.cache.CachePeekMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;
import com.primatics.ignite.dto.AnalyzedLoan;
import com.primatics.ignite.dto.Loan;

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
	private MongoTemplate mongoTemplate;

	public void start() {
		Ignition.state();
		if (!Ignition.state().equals(IgniteState.STARTED)) {
			ignite = Ignition.start("classpath:loss-amount-config-client.xml");
		}
	}

	public void startAnalysis() {
		Ignition.state();
		if (!Ignition.state().equals(IgniteState.STARTED)) {
			ignite = Ignition.start("classpath:analysis-config-client.xml");
		}
	}

	BigDecimal[] sumArrayLossAmounts = new BigDecimal[16];
	Double totalBalance = 0.0;

	public AnalyzedLoan loadDataIntoCache(final Double[] survivalScalingFactor, final Double[] lossRateScalingFactor,
			Integer analysisKey) {

		System.out.println("Loan Ignite");
		final IgniteCache<Integer, Loan> cache = ignite.cache("loanCache");
		cache.clear();
		System.out.println("----cache---------" + cache.size(CachePeekMode.PRIMARY));

		Stopwatch watch1 = Stopwatch.createStarted();
		List<Integer> keys = mongoTemplate.getDb().getCollection("loans").distinct("_id");
		System.out.println(" ** Get KEYS from Mongo " + watch1.stop());

		Stopwatch watch2 = Stopwatch.createStarted();
		Set<Integer> set = new HashSet<Integer>(keys);
		System.out.println(" ** Convert KEYS LIST to SEt " + watch2.stop());

		Loan l = new Loan();
		totalBalance = 0.0;
		final Map<Integer, EntryProcessorResult<Loan>> results = cache.<Loan>invokeAll(set, (entry, args) -> {
			Loan loan = entry.getValue();
			Double balanceAmount = loan.getBalance();
			totalBalance = balanceAmount + totalBalance;
			Double[] survivalRates = loan.getSurvival();
			Double[] lossRates = loan.getLossRate();

			final List<Double> lossAmount = new ArrayList<Double>();
			for (int j = 0; j < survivalRates.length; j++) {
				Double survivalRate = survivalRates[j];
				Double lossRate = lossRates[j];
				Double resultAmount = balanceAmount * survivalRate * lossRate * survivalScalingFactor[j]
						* lossRateScalingFactor[j];
				lossAmount.add(resultAmount);
			}
			Double[] lossAmounts = new Double[survivalRates.length];
			lossAmounts = lossAmount.toArray(lossAmounts);

			l.setLoanId(loan.getLoanId());
			l.setBalance(balanceAmount);
			l.setSurvival(survivalRates);
			l.setLossRate(lossRates);
			l.setLossAmount(lossAmounts);

			cache.replace(entry.getKey(), l);

			return loan;

		});
		// Integer key = 999;
		AnalyzedLoan loan = getDataForCalc(survivalScalingFactor, lossRateScalingFactor, analysisKey, results.keySet());
		return loan;
	}

	public AnalyzedLoan getDataForCalc(final Double[] survivalScalingFactor, final Double[] lossRateScalingFactor,
			Integer key, Set<Integer> set) {

		final IgniteCache<Integer, Loan> cache = ignite.cache("loanCache");

		Stopwatch watch = Stopwatch.createStarted();

		AnalyzedLoan al = new AnalyzedLoan();
		al.setKey(key);
		al.setPortfolio("all");
		al.setTotalBalance(new BigDecimal(totalBalance));
		al.setLossRate(lossRateScalingFactor);
		al.setSurvival(survivalScalingFactor);

		Arrays.fill(sumArrayLossAmounts, new BigDecimal(0.0));
		for (Loan loanCached : cache.getAll(set).values()) {
			final Double[] loanLossAmounts = loanCached.getLossAmount();
			for (int i = 0; i < 16; i++) {
				sumArrayLossAmounts[i] = sumArrayLossAmounts[i].add(new BigDecimal(loanLossAmounts[i])).setScale(2,
						BigDecimal.ROUND_HALF_UP);
			}
		}
		watch = watch.stop();

		System.out.println("computation and update took: " + watch + " ----------- " + cache.size(CachePeekMode.PRIMARY)
				+ " ------ ");

		al.setTotalLossAmounts(sumArrayLossAmounts);

		for (int i = 0; i < al.getTotalLossAmounts().length; i++) {
			System.out
					.println("*******LOSS AMOUNT ELEMENTS***********" + Arrays.asList(al.getTotalLossAmounts()).get(i));
		}

		final IgniteCache<Integer, AnalyzedLoan> cacheAnalysis = ignite.getOrCreateCache("loanCacheAnalysis");
		cacheAnalysis.replace(al.getKey(), al);

		return al;
	}

	public Stopwatch initializeLoans() {

		final IgniteCache<Integer, Loan> cache = ignite.cache("loanCache");
		cache.clear();

		Stopwatch watch = Stopwatch.createStarted();
		CloseableIterator<Loan> loansIt = mongoTemplate.stream(new Query(), Loan.class);
		System.out.println(" ** Loans from mongoDB into map " + watch.stop());

		Stopwatch watch3 = Stopwatch.createStarted();
		List<Loan> list = StreamUtils.asStream(loansIt).collect(Collectors.toList());
		;
		System.out.println(list.size() + " ** Iterables to List " + watch3.stop());

		Stopwatch watch2 = Stopwatch.createStarted();
		Map<Integer, Loan> mapLoans = new HashMap<Integer, Loan>();
		mapLoans = list.stream().collect(Collectors.toMap(Loan::getKey, item -> item));
		System.out.println(mapLoans.size() + " ** List into map " + watch2.stop());

		Stopwatch watch1 = Stopwatch.createStarted();
		cache.putAll(mapLoans);
		watch1 = watch1.stop();
		System.out.println(
				mapLoans.size() + " --CACHE SIZE-- " + cache.size(null) + " ** Loans Loaded in Cache " + watch1);

		return watch1;
	}

	public AnalyzedLoan getAnalyzedLoan(Integer key) {

		startAnalysis();
		final IgniteCache<Integer, AnalyzedLoan> cache = ignite.getOrCreateCache("loanCacheAnalysis");

		if (key == null || !cache.containsKey(key) || cache.size(null) == 0) {
			AnalyzedLoan eal = new AnalyzedLoan();
			eal.setKey(999);
			eal.setPortfolio("");
			eal.setTotalBalance(new BigDecimal(0.0));

			Double[] survivals = new Double[16];
			Arrays.fill(survivals, 1.0);
			eal.setSurvival(survivals);

			Double[] lossRates = new Double[16];
			Arrays.fill(lossRates, 1.0);
			eal.setLossRate(lossRates);

			eal.setTotalLoss(new BigDecimal(0.0));

			BigDecimal[] totalLossAmount = new BigDecimal[16];
			Arrays.fill(totalLossAmount, new BigDecimal(0.0));
			eal.setTotalLossAmounts(totalLossAmount);
			ignite.close();
			return eal;
		} else {
			CacheEntry<Integer, AnalyzedLoan> l = cache.getEntry(999);
			ignite.close();
			System.out.println("ANSWER ----------- " + l.getValue().toString());
			return l.getValue();
		}

	}

	public static BigDecimal sum(BigDecimal... values) {
		BigDecimal result = new BigDecimal(0.0);
		for (BigDecimal value : values)
			result = result.add(value);
		return result;
	}
}
