package com.primatics.ignite;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * Spring MongoDB configuration file
 *
 */
@Configuration
public class SpringMongoConfig extends AbstractMongoConfiguration{

	@Override
	protected String getDatabaseName() {
		return "loans";
	}
	
	@Bean
	public MongoDbFactory mongoDbFactory() throws Exception {

	    // Set credentials      
	    MongoCredential credential = MongoCredential.createCredential("cecl", getDatabaseName(), "cecl".toCharArray());
	    ServerAddress serverAddress = new ServerAddress("18.221.202.202", 27017);

	    // Mongo Client
	    MongoClient mongoClient = new MongoClient(serverAddress,Arrays.asList(credential)); 

	    // Mongo DB Factory
	    SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(
	            mongoClient, getDatabaseName());

	    return simpleMongoDbFactory;
	}

	
    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
    	return new MongoTemplate(mongoDbFactory());
    }

	@Override
	@Bean
	public Mongo mongo() throws Exception {
		//return new MongoClient("172.31.5.101");
		return new MongoClient("18.221.202.202");
	}

}

