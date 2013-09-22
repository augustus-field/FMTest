package misc;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, TimeoutException {
		new ProcessBuilder("gnome-terminal").start();
		Future<Integer> ft=null;
		ft.get(5, TimeUnit.SECONDS);
	}

}
