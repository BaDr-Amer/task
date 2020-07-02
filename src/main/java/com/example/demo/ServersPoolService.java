package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.IndexType;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;
import com.aerospike.client.task.IndexTask;

enum Status {
	CREATING("Creating"), ACTIVE("Active");

	private String status;

	Status(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}

enum Keys {
	ID("id"), NAME("name"), MEMORY("memory"), STATUS("status"), ALLOCATED("allocated");

	private String key;

	Keys(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}

@Service
public class ServersPoolService {

	@Autowired
	private AerospikeClient client;
	private final String NAMESPACE = "test";
	private final String FREE = "free";
	private final String SERVER_POOL = "servers";

	private final int DELAY = 20;

	// must be called once to setup an index for search
	private void createSecondaryIndexOnMemoryBin() {
		try {
			client.dropIndex(null, NAMESPACE, FREE, "idx_" + NAMESPACE + "_" + FREE + "_" + Keys.MEMORY.getKey());

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			IndexTask task = client.createIndex(null, NAMESPACE, FREE,
					"idx_" + NAMESPACE + "_" + FREE + "_" + Keys.MEMORY.getKey(), Keys.MEMORY.getKey(),
					IndexType.NUMERIC);
			task.waitTillComplete();
		}
	}

	private void createSecondaryIndexOnStatusBin() {
		try {
			client.dropIndex(null, NAMESPACE, FREE, "idx_" + NAMESPACE + "_" + FREE + "_" + Keys.STATUS.getKey());
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			IndexTask task = client.createIndex(null, NAMESPACE, FREE,
					"idx_" + NAMESPACE + "_" + FREE + "_" + Keys.STATUS.getKey(), Keys.STATUS.getKey(),
					IndexType.NUMERIC);
			task.waitTillComplete();
		}
	}

	public void init() {
		createSecondaryIndexOnMemoryBin();
		createSecondaryIndexOnStatusBin();
	}

	public Key request100GigaFromCloud(long m) {
		String uuid = UUID.randomUUID().toString();
		Key key = new Key(NAMESPACE, FREE, uuid);
		Bin id = new Bin(Keys.ID.getKey(), uuid);
		Bin memory = new Bin(Keys.MEMORY.getKey(), m);
		Bin status = new Bin(Keys.STATUS.getKey(), Status.CREATING.getStatus());
		client.put(null, key, id, memory, status);
		Executors.newScheduledThreadPool(1).schedule(() -> {
			Bin newStatus = new Bin(Keys.STATUS.getKey(), Status.ACTIVE.getStatus());
			client.put(null, key, newStatus);
		}, DELAY, TimeUnit.SECONDS);
		return key;
	}

	private boolean isActiveAndNotAllocated(Record record) {
		boolean isAllocated = record.getBoolean("allocated");
		String status = record.getString("status");

		return isAllocated == false && Status.ACTIVE.getStatus().equals(status);
	}

	private List<Record> getServersAvailable(long m) {
		Statement stmt = new Statement();
		stmt.setNamespace(NAMESPACE);
		stmt.setSetName(FREE);
		stmt.setFilter(Filter.equal(Keys.MEMORY.getKey(), m));
		RecordSet rs = client.query(null, stmt);
		List<Record> list = new ArrayList<>();
		while (rs != null && rs.next()) {
			Record record = rs.getRecord();
			if (isActiveAndNotAllocated(record)) {
				list.add(record);
			}
		}
		return list;
	}

	public List<Record> findAll() {
		Statement stmt = new Statement();
		stmt.setNamespace(NAMESPACE);
		stmt.setSetName(FREE);
		RecordSet rs = client.query(null, stmt);
		List<Record> list = new ArrayList<>();
		while (rs != null && rs.next()) {
			Record record = rs.getRecord();
			list.add(record);
		}
		return list;
	}

	public void deleteAll() {
		List<Record> all = findAll();
		all.stream().forEach(r -> {
			String uuid = r.getString("id");
			Key key = new Key(NAMESPACE, FREE, uuid);
			client.delete(null, key);
		});
	}

	public synchronized void allocateMemory(long m) {
		List<Record> servers = getServersAvailable(m);
		if (servers.size() > 0) {
			Record r = servers.get(0);
		} else {
			final Key key = request100GigaFromCloud(m);
			Executors.newScheduledThreadPool(1).schedule(() -> {
				Bin newStatus = new Bin(Keys.ALLOCATED.getKey(), true);
				client.put(null, key, newStatus);
			}, DELAY, TimeUnit.SECONDS);
		}
	}
}
