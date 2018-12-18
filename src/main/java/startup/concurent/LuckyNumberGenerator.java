package startup.concurent;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class LuckyNumberGenerator {
 
	public static void main(String[] args) {
		TransferQueue<String> queue = new LinkedTransferQueue<>();
		for (int i = 0; i < 10; i++) {
			producer(queue);
		}

		for (int i = 0; i < 10; i++) {
			consumer(queue);
//			try {
//				// 消费者进程休眠一秒钟，以便以便生产者获得CPU，从而生产产品
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}

//		System.exit(0);

	}

	private static void consumer(TransferQueue<String> queue) {
		Thread consumer = new Thread(new Consumer(queue));
//		consumer.setDaemon(true);
		consumer.start();
	}

	private static void producer(TransferQueue<String> queue) {
		Thread producer = new Thread(new Producer(queue));
//		producer.setDaemon(true); //设置为守护进程使得线程执行结束后程序自动结束运行
		producer.start();
	}
}