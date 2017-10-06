package com.primatics.ignite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Stopwatch;
import com.primatics.ignite.service.LoanIgnite;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class IgniteController {
	
	@Autowired
    LoanIgnite loanIgnite;

    @GetMapping(value="/import")
    public ResponseEntity<Stopwatch> importDataIntoCache() {
    	Stopwatch took = loanIgnite.initializeLoans();
        return new ResponseEntity<Stopwatch>(took, HttpStatus.OK);
    }
    
    @GetMapping(value="/start")
    public ResponseEntity<String> start() {
    	loanIgnite.start();
        return new ResponseEntity<>("Done", HttpStatus.OK);
    }
    
    @GetMapping(value="/cache")
    public ResponseEntity<Stopwatch> getDataFromCache() {
    	Stopwatch watch = loanIgnite.getDatFromCache();
        return new ResponseEntity<>(watch, HttpStatus.OK);
    }

}
