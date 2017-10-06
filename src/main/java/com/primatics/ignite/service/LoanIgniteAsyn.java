package com.primatics.ignite.service;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

import com.primatics.ignite.dto.Loan;

import java.util.Arrays;

/**
 * Created by Rama Arun on 02/10/2017.
 */
public class LoanIgniteAsyn {
	public  void syncmain() {

		System.out.println("Hello Ignite Asynchronous!!");

		// create a new instance of TCP Discovery SPI
		TcpDiscoverySpi spi = new TcpDiscoverySpi();

		// create a new instance of tcp discovery multicast ip finder
		TcpDiscoveryMulticastIpFinder tcMp = new TcpDiscoveryMulticastIpFinder();
		tcMp.setAddresses(Arrays.asList("localhost")); // change your IP address here

		// set the multi cast ip finder for spi
		spi.setIpFinder(tcMp);

		// create new ignite configuration
		IgniteConfiguration cfg = new IgniteConfiguration();
		cfg.setClientMode(true);
		cfg.setPeerClassLoadingEnabled(true);

		// set the discovery spi to ignite configuration
		cfg.setDiscoverySpi(spi);

		// Start ignite
		Ignite ignite = Ignition.start(cfg);

		// get or create cache
		IgniteCache<Integer, Loan> cache = ignite.getOrCreateCache("testCache");

		// get an asynchronous cache
		IgniteCache<Integer, Loan> asynCache = cache.withAsync();

	    Loan loan = asynCache.get(1);
	    System.out.println(loan.toString());
		IgniteFuture<String> igniteFuture = asynCache.future();

		igniteFuture.listen(f -> System.out.println("Cache Value:" + f.toString()));
		ignite.close();
	}
}
