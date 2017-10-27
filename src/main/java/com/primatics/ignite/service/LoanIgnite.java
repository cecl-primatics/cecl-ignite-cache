package com.primatics.ignite.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
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
	@Autowired
	static Ignite ignite = null;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	Double totalBalance = 0.0;

	public void start() {
		Stopwatch watch2 = Stopwatch.createStarted();
		Ignition.state();
		System.out.println("STEP 10 - Ignite State Check took ::: "+watch2.stop()+" - "+Ignition.state());
		if (!Ignition.state().equals(IgniteState.STARTED)) {
			
			Stopwatch watch3 = Stopwatch.createStarted();
			ignite = Ignition.start("classpath:loss-amount-config-client.xml");
			long heapSize = Runtime.getRuntime().totalMemory();
			System.out.println("STEP 11 - Ignite Start took ::: "+watch3.stop()+" - "+Ignition.state()+" - "+heapSize);
		}
	}
	
	public void startAnalysis() {
		Stopwatch watch2 = Stopwatch.createStarted();
		Ignition.state();
		System.out.println("STEP 10 - Ignite State Check took ::: "+watch2.stop()+" - "+Ignition.state());
		if (!Ignition.state().equals(IgniteState.STARTED)) {
			
			Stopwatch watch3 = Stopwatch.createStarted();
			ignite = Ignition.start("classpath:analysis-config-client.xml");
			long heapSize = Runtime.getRuntime().totalMemory();
			System.out.println("STEP 11 - Ignite Start took ::: "+watch3.stop()+" - "+Ignition.state()+" - "+heapSize);
		}
	}
	
	/** Step 2: From file to Cache */
	public AnalyzedLoan initializeLoans(final Double[] survivalScalingFactor, final Double[] lossRateScalingFactor,
			Integer analysisKey, String run_name) {
		
		start();
		IgniteCache<Integer, Loan> cache = ignite.cache("loanCache");
		cache.clear();

		 Query query = new Query();
		 query.addCriteria(Criteria.where("scenario").is(run_name));
		 query.with(new Sort(Sort.Direction.ASC,"loanId"));
		 CloseableIterator<Loan> loansIt = mongoTemplate.stream(query, Loan.class);
		
		List<Loan> list = StreamUtils.asStream(loansIt).collect(Collectors.toList());
		
		Map<Integer, Loan> mapLoans = new HashMap<Integer, Loan>();
		mapLoans = list.stream().collect(Collectors.toMap(Loan::getKey, item -> item));
		
		cache.putAll(mapLoans);
		
		Loan l = new Loan();
		
		final Map<Integer, EntryProcessorResult<Loan>> results = cache.<Loan>invokeAll(mapLoans.keySet(), (entry, args) -> {
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
			l.setKey(loan.getKey());
			l.setLossAmount(lossAmounts);
			l.setScenario(loan.getScenario());
			l.setLoanId(loan.getLoanId());
			l.setBalance(balanceAmount);
			l.setSurvival(survivalRates);
			l.setLossRate(lossRates);
			
			cache.replace(entry.getKey(), l);

			return loan;

		});
		
		DecimalFormat df2 = new DecimalFormat(".##");
		AnalyzedLoan loan = getDataForCalc(survivalScalingFactor, lossRateScalingFactor, analysisKey, mapLoans.keySet(), run_name, Double.valueOf(df2.format(totalBalance)));
		return loan;
	}
	
	/** Step 2 - File to Cache */
 	 public AnalyzedLoan getDataForCalc(final Double[] survivalScalingFactor, final Double[] lossRateScalingFactor,
			Integer key, Set<Integer> set, String run_name, Double totalBalance) {
 		start();
		IgniteCache<Integer, Loan> cache = ignite.cache("loanCache");
		BigDecimal[] sumArrayLossAmounts = new BigDecimal[16];
		AnalyzedLoan al = new AnalyzedLoan();
		al.setKey(key);
		al.setScenario(run_name);
		al.setTotalBalance(new BigDecimal(totalBalance));
		al.setLossRate(lossRateScalingFactor);
		al.setSurvival(survivalScalingFactor);

		Stopwatch watch5 = Stopwatch.createStarted();
		Arrays.fill(sumArrayLossAmounts, new BigDecimal(0.0));
		for (Loan loanCached : cache.getAll(set).values()) {
			final Double[] loanLossAmounts = loanCached.getLossAmount();
			for (int i = 0; i < 16; i++) {
				sumArrayLossAmounts[i] = sumArrayLossAmounts[i].add(new BigDecimal(loanLossAmounts[i])).setScale(2,
						BigDecimal.ROUND_HALF_UP);
			}
		}
		long heapSize3 = Runtime.getRuntime().totalMemory();
		System.out.println("STEP 17 - Summing lossAmounts tooks :::: "+watch5.stop()+ " - "+heapSize3);
		al.setTotalLossAmounts(sumArrayLossAmounts);

		Stopwatch watch6 = Stopwatch.createStarted();
		BigDecimal totalLoss = new BigDecimal(0.0);
		for (int i = 0; i < 16; i++) {
			totalLoss = totalLoss.add(sumArrayLossAmounts[i]).setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		
		al.setTotalLoss(totalLoss);
		long heapSize = Runtime.getRuntime().totalMemory();
		System.out.println("STEP 18 - Summing all totalLossAmounts tooks :::: "+watch6.stop()+ " - "+heapSize);
		
		final IgniteCache<Integer, AnalyzedLoan> cacheAnalysis = ignite.cache("loanAnalysisCache");
		cacheAnalysis.put(al.getKey(), al);
		return al;
	}
 	 
 	 public AnalyzedLoan getAnalyzedLoanFromCache(Integer key) {
 		 
 		startAnalysis();
		final IgniteCache<Integer, AnalyzedLoan> cacheAnalysis = ignite.cache("loanAnalysisCache");
		AnalyzedLoan l = cacheAnalysis.get(key);
		
		System.out.println("*******put into Cache ==== ANALYZED LOAN************");
		System.out.println(l.toString());
		System.out.println("*******END ANALYZED LOAN************");
		
		return l;
 	 }

 	public AnalyzedLoan recalculate(Integer index, final Double[] survivalScalingFactor,
			final Double[] lossRateScalingFactor, Integer analysisKey, String scenario) {
 		
 		start();
		final IgniteCache<Integer, Loan> cacheCalc = ignite.cache("loanCache");
		BigDecimal[] sumArrayLossAmounts = new BigDecimal[16];
		Criteria criteria = new Criteria();
 		criteria.where("scenario").is(scenario);
 		Query query = new Query();
 		query.addCriteria(criteria);
 		List<Integer> keys = mongoTemplate.getCollection("loans")
 		    .distinct("key",query.getQueryObject());
 		Set<Integer> keyset = new HashSet<Integer>(keys);

		AnalyzedLoan current = getAnalyzedLoanFromCache(analysisKey);
		Double balanceAmount = current.getTotalBalance().doubleValue();
		
		Map<Integer, Double[]> losses = new HashMap<Integer, Double[]>();
		
		Map<Integer, Loan> loansFromCache = cacheCalc.getAll(keyset);
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& "+loansFromCache.size());
		
		AnalyzedLoan newAnalysis = new AnalyzedLoan();
		final Map<Integer, EntryProcessorResult<Loan>> results = cacheCalc.<Loan>invokeAll(keyset, (entry, args) -> {
			Loan loan = entry.getValue();
			Double[] survivalRates = loan.getSurvival();
			Double[] lossRates = loan.getLossRate();

			Double survivalRate = survivalRates[index];
			Double lossRate = lossRates[index];
			Double resultAmount = balanceAmount * survivalRate * lossRate * survivalScalingFactor[index] * lossRateScalingFactor[index];

			List<Double> lossArray = Arrays.asList(loan.getLossAmount());
			lossArray.set(index, resultAmount);

			Double[] updatedLossAmounts = new Double[lossArray.size()];
			updatedLossAmounts = lossArray.toArray(updatedLossAmounts);

			loan.setLossAmount(updatedLossAmounts);
			losses.put(loan.getKey(), updatedLossAmounts);
			
			cacheCalc.replace(entry.getKey(), loan);

			return loan;

		});
		// Integer key = 999;
		newAnalysis.setKey(analysisKey);
		newAnalysis.setScenario(scenario);
		newAnalysis.setTotalBalance(new BigDecimal(totalBalance));
		newAnalysis.setLossRate(lossRateScalingFactor);
		newAnalysis.setSurvival(survivalScalingFactor);
		
		Arrays.fill(sumArrayLossAmounts, new BigDecimal(0.0));
		for (Integer k : losses.keySet()) {
			final Double[] loanLossAmounts = losses.get(k);
			for (int i = 0; i < 16; i++) {
				sumArrayLossAmounts[i] = sumArrayLossAmounts[i].add(new BigDecimal(loanLossAmounts[i])).setScale(2,
						BigDecimal.ROUND_HALF_UP);
			}
		}

		newAnalysis.setTotalLossAmounts(sumArrayLossAmounts);
		
		BigDecimal sum = new BigDecimal(0.0);
		for (BigDecimal i : sumArrayLossAmounts) {
			sum = sum.add(i);
		}
		newAnalysis.setTotalLoss(sum);

		final IgniteCache<Integer, AnalyzedLoan> cacheAnalysis = ignite.getOrCreateCache("loanAnalysisCache");
		cacheAnalysis.replace(newAnalysis.getKey(), newAnalysis);
		return newAnalysis;
	}
}
