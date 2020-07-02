package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BusinessLogicLayerTest {

	@LocalServerPort
	private int port;

	@BeforeEach
	public void beforeEach() {
		ServersPoolServiceV2.removeAllocatedServersList();
		ServersPoolServiceV2.removeFreeServersList();
	}

	@Test
	public void testEmptyFreeServers() throws Exception {
		assertThat(ServersPoolServiceV2.getReservedServers().size()).isEqualTo(0);
		assertThat(ServersPoolServiceV2.getFreeServers().size()).isEqualTo(0);
	}

	@Test
	public void testReserveSpace() throws Exception {
		ServersPoolServiceV2.reserveSpace(30);
		assertThat(ServersPoolServiceV2.getFreeServers().size()).isEqualTo(1);
		assertThat(ServersPoolServiceV2.getFreeServers().first().getMemory()).isEqualTo(70);
	}

	@Test
	public void testWithMultithreading1() {
		int coreCount = Runtime.getRuntime().availableProcessors();
		ExecutorService taskExecutor = Executors.newFixedThreadPool(coreCount);
		for (int i = 0; i < 11; i++) {
			taskExecutor.execute(() -> {
				ServersPoolServiceV2.reserveSpace(50);
			});
		}
		taskExecutor.shutdown();
		try {
			taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {

		}
		assertThat(ServersPoolServiceV2.getReservedServers().size()).isEqualTo(11);
		assertThat(ServersPoolServiceV2.getFreeServers().size()).isEqualTo(6);
		assertThat(ServersPoolServiceV2.getFreeServers().pollLast().getMemory()).isEqualTo(50);
	}

	@Test
	public void testWithMultithreading2() {
		int coreCount = Runtime.getRuntime().availableProcessors();
		ExecutorService taskExecutor = Executors.newFixedThreadPool(coreCount);
		for (int i = 0; i < 10; i++) {
			taskExecutor.execute(() -> {
				ServersPoolServiceV2.reserveSpace(100);
			});
		}
		taskExecutor.shutdown();
		try {
			taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {

		}
		assertThat(ServersPoolServiceV2.getReservedServers().size()).isEqualTo(10);
		assertThat(ServersPoolServiceV2.getFreeServers().size()).isEqualTo(10);
		assertThat(ServersPoolServiceV2.getFreeServers().pollLast().getMemory()).isEqualTo(0);
	}

	@Test
	public void testWithMultithreading3() {
		int coreCount = Runtime.getRuntime().availableProcessors();
		ExecutorService taskExecutor = Executors.newFixedThreadPool(coreCount);
		for (int i = 0; i < 3; i++) {
			taskExecutor.execute(() -> {
				ServersPoolServiceV2.reserveSpace(30);
			});
		}
		taskExecutor.shutdown();
		try {
			taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {

		}
		ServersPoolServiceV2.reserveSpace(60);
		ServersPoolServiceV2.reserveSpace(60);
		assertThat(ServersPoolServiceV2.getReservedServers().size()).isEqualTo(5);
		assertThat(ServersPoolServiceV2.getFreeServers().size()).isEqualTo(3);
		assertThat(ServersPoolServiceV2.getFreeServers().pollFirst().getMemory()).isEqualTo(10);
		assertThat(ServersPoolServiceV2.getFreeServers().pollFirst().getMemory()).isEqualTo(40);
		assertThat(ServersPoolServiceV2.getFreeServers().pollFirst().getMemory()).isEqualTo(40);
	}

	@Test
	public void testWithException() {
		try {
			ServersPoolServiceV2.reserveSpace(101);
		} catch (Exception ex) {
			assertTrue(ex instanceof RuntimeException);
		}
		try {
			ServersPoolServiceV2.reserveSpace(-1);
		} catch (Exception ex) {
			assertTrue(ex instanceof RuntimeException);
		}
		assertThat(ServersPoolServiceV2.getFreeServers().size()).isEqualTo(0);
		assertThat(ServersPoolServiceV2.getFreeServers().size()).isEqualTo(0);
	}
}