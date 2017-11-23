package com.primatics.ignite.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.cache.processor.EntryProcessorResult;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
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
			String run_name) {
		
		 Query query = new Query();
		 query.addCriteria(Criteria.where("scenario").is(run_name));
		 query.with(new Sort(Sort.Direction.ASC,"loanId"));
		 CloseableIterator<Loan> loansIt = mongoTemplate.stream(query, Loan.class);
		
		List<Loan> list = StreamUtils.asStream(loansIt).collect(Collectors.toList());
		
		Map<String, Loan> mapLoans = new HashMap<String, Loan>();
		mapLoans = list.stream().collect(Collectors.toMap(Loan::getKey, item -> item));
		
		start();
		IgniteCache<String, Loan> cache = ignite.cache("loanCache");
		cache.removeAll(mapLoans.keySet());
		
		cache.putAll(mapLoans);
		
		Loan l = new Loan();
		
		final Map<String, EntryProcessorResult<Loan>> results = cache.<Loan>invokeAll(mapLoans.keySet(), (entry, args) -> {
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
		AnalyzedLoan loan = getDataForCalc(survivalScalingFactor, lossRateScalingFactor, mapLoans.keySet(), run_name, Double.valueOf(df2.format(totalBalance)));
		return loan;
	}
	
	/** Step 2 - File to Cache */
 	 public AnalyzedLoan getDataForCalc(final Double[] survivalScalingFactor, final Double[] lossRateScalingFactor,
 			Set<String> set, String run_name, Double totalBalance) {
 		start();
		IgniteCache<String, Loan> cache = ignite.cache("loanCache");
		BigDecimal[] sumArrayLossAmounts = new BigDecimal[16];
		AnalyzedLoan al = new AnalyzedLoan();
		Query query = new Query(Criteria.where("scenario").is(run_name));
		if (mongoTemplate.exists(query, AnalyzedLoan.class)) {
			al.setKey(mongoTemplate.findOne(query, AnalyzedLoan.class).getKey());
		} else {
		al.setKey(generateUniqueId());
		}
		al.setScenario(run_name);
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
		al.setTotalLossAmounts(sumArrayLossAmounts);

		BigDecimal totalLoss = new BigDecimal(0.0);
		for (int i = 0; i < 16; i++) {
			totalLoss = totalLoss.add(sumArrayLossAmounts[i]).setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		
		al.setTotalLoss(totalLoss);
		
		final IgniteCache<String, AnalyzedLoan> cacheAnalysis = ignite.getOrCreateCache("loanAnalysisCache");
		cacheAnalysis.put(al.getKey(), al);
		
		mongoTemplate.save(al);
		
		return al;
	}
 	 
 	public AnalyzedLoan getAnalyzedLoanFromCache(String scenario) {
 		 
 		Query query = new Query();
 		query.addCriteria(Criteria.where("scenario").is(scenario));
 		String key = (String) mongoTemplate.findOne(query, AnalyzedLoan.class).getKey();
 		
 		startAnalysis();
		final IgniteCache<String, AnalyzedLoan> cacheAnalysis = ignite.cache("loanAnalysisCache");
		AnalyzedLoan l = cacheAnalysis.get(key);
		
		return l;
 	 }

 	public AnalyzedLoan recalculate(Integer index, final Double[] survivalScalingFactor,
			final Double[] lossRateScalingFactor, String scenario) {
 		
 		start();
		final IgniteCache<String, Loan> cacheCalc = ignite.cache("loanCache");
		BigDecimal[] sumArrayLossAmounts = new BigDecimal[16];
 		
		AnalyzedLoan current = getAnalyzedLoanFromCache(scenario);
		Double balanceAmount = current.getTotalBalance().doubleValue();
		
		Map<String, Double[]> losses = new HashMap<String, Double[]>();
		
		List<String> list = findByField("_id");
 		Set<String> keyset = new HashSet<String>(list);
 		
		AnalyzedLoan newAnalysis = new AnalyzedLoan();
		final Map<String, EntryProcessorResult<Loan>> results = cacheCalc.<Loan>invokeAll(keyset, (entry, args) -> {
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
		newAnalysis.setKey(current.getKey());
		newAnalysis.setScenario(scenario);
		newAnalysis.setTotalBalance(new BigDecimal(totalBalance));
		newAnalysis.setLossRate(lossRateScalingFactor);
		newAnalysis.setSurvival(survivalScalingFactor);
		
		Arrays.fill(sumArrayLossAmounts, new BigDecimal(0.0));
		for (String k : losses.keySet()) {
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

		final IgniteCache<String, AnalyzedLoan> cacheAnalysis = ignite.getOrCreateCache("loanAnalysisCache");
		cacheAnalysis.replace(newAnalysis.getKey(), newAnalysis);
		
		Query query1 = new Query();
 		query1.addCriteria(Criteria.where("scenario").is(scenario));
 		mongoTemplate.remove(query1, AnalyzedLoan.class);
		mongoTemplate.save(newAnalysis);
		
		return newAnalysis;
	}
 	
 	public static String generateUniqueId() {      
 		RandomStringGenerator randomStringGenerator =
 		        new RandomStringGenerator.Builder()
 		                .withinRange('0', 'z')
 		                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
 		                .build();
 		return randomStringGenerator.generate(12);
    }
 	
 	public List<String> findByField(String field) {
 		  return (List<String>) mongoTemplate.getCollection("loans").distinct(field);
 		 }
}
