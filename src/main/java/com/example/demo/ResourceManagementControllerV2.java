package com.example.demo;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2")
public class ResourceManagementControllerV2 {
	
	@GetMapping("/allocate/{memory}")
	public Server allocateNewServer(@PathVariable long memory) {
		if(memory < 1 || memory > 100) {
			throw new RuntimeException("Server size should be between 1-100 Giga of memory");
		}
		
		return ServersPoolServiceV2.reserveSpace(memory);
	}
	
	@GetMapping("/find/{id}")
	public Optional<Server> findServerById(@PathVariable String id) {
		return ServersPoolServiceV2.getReservedServers().stream().filter(s -> s.id.equals(id)).findFirst();
	}
	
	@GetMapping("/find/all")
	public BlockingQueue<Server> findAll() {
		return ServersPoolServiceV2.getReservedServers();
	}
}
