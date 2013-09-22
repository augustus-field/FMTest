package misc;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test whether a thread is blocked.
 * @author Chen Li
 *
 */
public class ThreadStateTest {
	private final AtomicBoolean blocked = new AtomicBoolean(true);
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Thread t = new Thread(){
			public void run(){
				blocked.set(false);
			}
		};
		
		t.start();
		
		System.out.println("Thread blocked? "+blocked.get());
	}

}
