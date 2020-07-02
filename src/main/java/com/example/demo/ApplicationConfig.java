package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aerospike.client.AerospikeClient;

@Configuration
class ApplicationConfig {
	@Bean
	public AerospikeClient aerospikeClient() {
		AerospikeClient client = new AerospikeClient("localhost", 3000);
		return client;
	}
}