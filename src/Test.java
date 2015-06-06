import static org.junit.Assert.*;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;


public class Test {

	
private Queue<Point> queueToDraw = new LinkedList<Point>();
	
	public synchronized void addPointToQueue(int x, int y){
		this.queueToDraw.add(new Point(x, y));
	}
	
	public synchronized Point getPointFromQueue(){
		return this.queueToDraw.poll();
	}
	
	public synchronized boolean isQueueEmpty(){
		return this.queueToDraw.isEmpty();
	}
	
	@org.junit.Test
	public void BigTest() 
	{
		
		
		assertEquals(true, isQueueEmpty());
		
		addPointToQueue(1,2);
		
		assertEquals(false, isQueueEmpty());
		
		Point p = getPointFromQueue();
		assertEquals(1, p.x);
		assertEquals(2, p.y);
		
		assertEquals(true, isQueueEmpty());
		
		addPointToQueue(4,6);
		assertEquals(false, isQueueEmpty());
		addPointToQueue(3,8);
		assertEquals(false, isQueueEmpty());
		
		
		
		Point p2 = getPointFromQueue();
		assertEquals(4, p2.x);
		assertEquals(6, p2.y);
		
		assertEquals(false, isQueueEmpty());
		
		Point p3 = getPointFromQueue();
		assertEquals(3, p3.x);
		assertEquals(8, p3.y);
		
		assertEquals(true, isQueueEmpty());

	    
	}

}
