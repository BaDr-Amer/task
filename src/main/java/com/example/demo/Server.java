package com.example.demo;

import java.time.Instant;

public class Server{

	public String id;
	public long memory;
	public String status;
	public Instant dateCreated;

	public Server(String id, long memory, String status, Instant dateCreated) {
		this.id = id;
		this.memory = memory;
		this.status = status;
		this.dateCreated = dateCreated;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getMemory() {
		return memory;
	}

	public void setMemory(long memory) {
		this.memory = memory;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Instant getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Instant dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "{ " + id + " - " + memory + " - " + status + " - " + dateCreated + " }";
	}
}
