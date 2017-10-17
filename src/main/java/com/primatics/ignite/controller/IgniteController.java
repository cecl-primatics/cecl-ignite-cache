package com.primatics.ignite.controller;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
	
	public static Integer key;

    @GetMapping(value="/cache")
    public ResponseEntity<Stopwatch> loadDataIntoCache() {
    	loanIgnite.start();
    	loanIgnite.initializeLoans();
    	Stopwatch watch = Stopwatch.createStarted();
    	
    	Double[] survivals = new Double[16];
		Arrays.fill(survivals, 1.0);

		Double[] lossRates = new Double[16];
		Arrays.fill(lossRates, 1.0);
    	
    	key = loanIgnite.loadDataIntoCache(survivals, lossRates, 999).getKey();
    	watch = watch.stop();
        return new ResponseEntity<>(watch, HttpStatus.OK);
    }
    
    @PostMapping(value="/recalculate")
   	public ResponseEntity<AnalyzedLoan> recalculate(@RequestBody String requestObject) {

    	JSONObject jsonObj = new JSONObject(requestObject);
    	JSONArray surs = jsonObj.getJSONArray("survival");
    	JSONArray loss = jsonObj.getJSONArray("lossRate");
    	
    	Double[] surList = new Double[surs.length()];
    	for (int i = 0; i < surs.length(); i++) {
    		surList[i] = surs.getDouble(i);
    	}
    	
    	Double[] lossList = new Double[loss.length()];
    	for (int i = 0; i < loss.length(); i++) {
    		lossList[i] = loss.getDouble(i);
    	}
    	if (key == null) {
    		key = 999;
    	}

       	AnalyzedLoan l = loanIgnite.loadDataIntoCache(surList, lossList, key);
       	
   		return new ResponseEntity<AnalyzedLoan>(l, HttpStatus.OK);
   	}
}
