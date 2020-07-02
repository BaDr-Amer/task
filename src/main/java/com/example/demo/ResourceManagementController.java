package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.aerospike.client.Record;

@RestController
public class ResourceManagementController {
	public ResourceManagementController() {
		// TODO Auto-generated constructor stub
	}
	
	@Autowired
	private ServersPoolService service;
	
	@GetMapping("/find/all")
	public List<Record> findAll() {
		return service.findAll();
	}
	
	@GetMapping("/")
	public String helloWorld() {
		return "Hello, World";
	}
	
	@DeleteMapping("/delete/all")
	public boolean deleteAll() {
		service.deleteAll();
		return true;
	}
	
	@GetMapping("/allocate/{memory}")
	public boolean allocateNewServer(@PathVariable long memory) {
		if(memory < 1 || memory > 100) {
			throw new RuntimeException("Server size should be between 1-100 Giga of memory");
		}
		service.allocateMemory(memory);
		return true;
	}
}
