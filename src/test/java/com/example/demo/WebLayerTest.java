package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WebLayerTest {
	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void testAllocationApi() throws Exception {
		ResponseEntity<Server> response = restTemplate
				.getForEntity(new URL("http://localhost:" + port + "/api/v2/allocate/30").toString(), Server.class);
		assertEquals(30, response.getBody().getMemory());
		assertEquals("creating", response.getBody().getStatus());

		ResponseEntity<Server[]> response2 = restTemplate
				.getForEntity(new URL("http://localhost:" + port + "/api/v2/find/all").toString(), Server[].class);
		Server[] freeServers = response2.getBody();

		assertEquals(freeServers.length, 1);
		assertEquals(freeServers[0].getMemory(), 30);
	}
}
