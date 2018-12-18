package startup.concurent;

import java.util.Random;
import java.util.concurrent.TransferQueue;

public class Producer implements Runnable {
	private final TransferQueue<String> queue;
	private static int count = 0;
 
	public Producer(TransferQueue<String> queue) {
		this.queue = queue;
	}
 
	private String produce() {
		return " your lucky number " + (new Random().nextInt(100));
	}
 
	@Override
	public void run() {
		try {
			synchronized (Producer.class){
				if (count >100) return;
				if (queue.hasWaitingConsumer()) {
//					queue.transfer(produce());
					queue.transfer("your lucky number "+(count++));
				}
			}
//				TimeUnit.SECONDS.sleep(1);//生产者睡眠一秒钟,这样可以看出程序的执行过程
		} catch (InterruptedException e) {
		}
	}
}