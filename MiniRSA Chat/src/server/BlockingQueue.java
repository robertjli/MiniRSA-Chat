package server;

import java.util.*;

/**
 * A Blocking Queue for Objects of type E.
 * 
 * @author Evan Schoenbach (with inspiration from tutorials.jenkov.com/java-concurrency)
 * @version February 6, 2012
 */
public class BlockingQueue<E> {
	
	private List<E> queue = new LinkedList<E>();
	private int threadLimit = 1;

	public BlockingQueue(){}
	
	public BlockingQueue(int limit){
//		System.out.println("Creating new blocking queue");
		threadLimit = limit;
	}

	public synchronized void enqueue(E element) throws InterruptedException  {
		while(isFull())
			wait();
		if(isEmpty())
			notify();  // wake up threads waiting to dequeue
//		System.out.println("Enqueuing " + element.getID());
		queue.add(element);
//		System.out.println("Enqueued " + element);
	}

	public synchronized E dequeue() throws InterruptedException {
		while(isEmpty())
			wait();
		if(isFull())
			notify(); // wake up elements waiting to be enqueued
		E removedElement = queue.remove(0);
		return removedElement;
	}
		
	private boolean isEmpty() {
		return queue.size() == 0;
	}
	
	private boolean isFull() {
		return queue.size() == threadLimit;
	}
	
}
