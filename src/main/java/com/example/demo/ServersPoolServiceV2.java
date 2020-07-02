package com.example.demo;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class ServersPoolServiceV2 {
	static Comparator<Server> memoryComparator = Comparator.comparing(Server::getMemory);
	static ConcurrentSkipListSet<Server> free = new ConcurrentSkipListSet<Server>(memoryComparator);
	static BlockingQueue<Server> serversPool = new LinkedBlockingQueue<>();
	final static int DELAY = 20;
	
	private static void request100GigaFromCloud() {
		Server server = new Server(UUID.randomUUID().toString(), 100, "creating", Instant.now());
		free.add(server);
		Executors.newScheduledThreadPool(1).schedule(() -> {
			server.status = "active";
		}, DELAY, TimeUnit.SECONDS);
	}

	public static synchronized Server reserveSpace(long memory) {
		if(memory > 100 || memory <= 0) {
			throw new RuntimeException();
		}
		Server newServer = new Server(UUID.randomUUID().toString(), memory, "creating", Instant.now());
		Server freeServer = free.ceiling(newServer);
		
		if (freeServer == null) {
			request100GigaFromCloud();
			freeServer = free.ceiling(newServer);
		}
		freeServer.memory -= memory;
		newServer.status = freeServer.status;
		if("creating".equals(freeServer.status)) {
			Duration between = Duration.between(freeServer.dateCreated, newServer.dateCreated);
			int updateStatusAfter = (int) (DELAY - between.getSeconds());
			Executors.newScheduledThreadPool(1).schedule(() -> {
				newServer.status = "active";
			}, updateStatusAfter > 0 ? updateStatusAfter : 0, TimeUnit.SECONDS);
		}
		serversPool.add(newServer);

		printFree();
		printReserved();
		return newServer;
	}
	
	public static ConcurrentSkipListSet<Server> getFreeServers() {
		return free;
	}
	
	public static BlockingQueue<Server> getReservedServers() {
		return serversPool;
	}
	
	public static void removeFreeServersList() {
		free.clear();
	}
	
	public static void removeAllocatedServersList() {
		serversPool.clear();
	}
	
	private static void printFree() {
		System.out.println("Available free servers: ");
		free.stream().forEach(s -> {
			System.out.println(s);
		});
	}
	
	private static void printReserved() {
		System.out.println("Allocated servers: ");
		serversPool.stream().forEach(s -> {
			System.out.println(s);
		});
	}
}
