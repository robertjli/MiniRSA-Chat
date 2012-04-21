package server;

import java.util.ArrayList;
import java.net.Socket;

/**
 * A thread pool to manage worker threads for sockets.
 * 
 * @author Evan Schoenbach
 * @version February 6, 2012
 */
public class ThreadPool<E> {
	
	private int threadCount;
	private ArrayList<WorkerThread> threads;
//	private ArrayList<Thread> genericThreads;
		
	public ThreadPool(BlockingQueue<Socket> queue, int threadCount) {
		this.threadCount = threadCount;
		threads = new ArrayList<WorkerThread>();
		for (int i = 0; i < threadCount; i++) {
//			System.out.println("Creating thread " + i);
			threads.add(new WorkerThread(queue));
		}
	}
	
	public ThreadPool(BlockingQueue<Socket> queue) {
		this(queue, 25);
	}
	
	public void start() {
//		System.out.println("Starting pool");
		WorkerThread currentWorker;
		Thread currentThread;
		for (int i = 0; i < threadCount; i++) {
			currentWorker = threads.get(i);
//			System.out.println("Thread: " + i);
			currentWorker.setName("Worker Thread " + (i + 1));
			currentThread = new Thread(currentWorker, "Worker Thread " + (i + 1));
//			currentThread.setName("Thread " + i);
//			System.out.println(currentThread);
			currentThread.start();
//			genericThreads.add(currentThread);
		}
	}
	
	public synchronized void terminate() {
		for (int i = 0; i < threadCount; i++) {
			synchronized(threads.get(i)) {
				threads.get(i).terminate();
				threads.get(i).notify();
			}
//			try {
//				genericThreads.get(i).interrupt();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}
	
	public ArrayList<WorkerThread> getThreads() {
		return threads;
	}
	
	public ArrayList<WorkerThread> getNonTerminatedThreads() {
		ArrayList<WorkerThread> nonTerminatedThreads = new ArrayList<WorkerThread>();
		for (int i = 0; i < threadCount; i++) {
			if (!threads.get(i).isTerminated()) {
				nonTerminatedThreads.add(threads.get(i));
			}
		}
		return nonTerminatedThreads;
	}
}
