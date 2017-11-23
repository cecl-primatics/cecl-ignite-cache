package com.primatics.ignite.controller;

import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Stopwatch;
import com.primatics.ignite.dto.AnalyzedLoan;
import com.primatics.ignite.service.LoanIgnite;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class IgniteController {
	
	@Autowired
    LoanIgnite loanIgnite;
	
	/** STEP-1: From File to Cache */
	
	@ResponseBody
    @PostMapping(value="/cache")
    public ResponseEntity<AnalyzedLoan> cache(@RequestBody String requestObject) {
    	
    	JSONObject jsonObj = new JSONObject(requestObject);
    	String run_name = jsonObj.getString("run_name");
    	
    	Stopwatch watch3 = Stopwatch.createStarted();
    	Double[] survivals = new Double[16];
		Arrays.fill(survivals, 1.0);
		Double[] lossRates = new Double[16];
		Arrays.fill(lossRates, 1.0);
    	
		AnalyzedLoan al = loanIgnite.initializeLoans(survivals, lossRates, run_name);
    	watch3 = watch3.stop();
    	
    	long heapSize1 = Runtime.getRuntime().totalMemory();
    	System.out.println("STEP 12 to 19 - Initialize Loans took ::: "+watch3+" - "+heapSize1);
    	
        return ResponseEntity.ok().body(al);
    }
    
    @GetMapping("/analyzedloan/{scenario}")
    public ResponseEntity<AnalyzedLoan> analyzedloan(@PathVariable("scenario") String scenario) throws Exception {
    	AnalyzedLoan al = loanIgnite.getAnalyzedLoanFromCache(scenario);
    	return ResponseEntity.ok().body(al);
    }
    
    @PostMapping(value="/recalculate")
   	public ResponseEntity<AnalyzedLoan> recalculate(@RequestBody String requestObject) {
    	
    	JSONObject jsonObj = new JSONObject(requestObject);
    	JSONArray surs = jsonObj.getJSONArray("survival");
    	JSONArray loss = jsonObj.getJSONArray("lossRate");
    	String run_name = jsonObj.getString("scenario");
    	Integer index = jsonObj.getInt("index");
    	
    	Double[] surList = new Double[surs.length()];
    	for (int i = 0; i < surs.length(); i++) {
    		surList[i] = surs.getDouble(i);
    	}
    	
    	Double[] lossList = new Double[loss.length()];
    	for (int i = 0; i < loss.length(); i++) {
    		lossList[i] = loss.getDouble(i);
    	}

       	AnalyzedLoan l = loanIgnite.recalculate(index, surList, lossList, run_name);
       	
		System.out.println("*******getAnalyzedLoan from Cache ==== ANALYZED LOAN************");
		System.out.println(l.toString());
		System.out.println("*******END ANALYZED LOAN************");
       	
   		return new ResponseEntity<AnalyzedLoan>(l, HttpStatus.OK);
   	}
}
