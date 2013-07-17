package dom4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import util.TimeLogger;

public class Dom4jSlow {
	final static Log logger = LogFactory.getLog(Dom4jSlow.class);
	public static void main(String[] args) throws DocumentException, InterruptedException {
		final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><req><req tid=\"1347\"/><req tid=\"1347\"/><req tid=\"1347\"/><req tid=\"4682\"><rec cnl=\"4\" iid=\"1000000002954\" bs=\"1\" rto=\"0.98\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002955\" bs=\"2\" rto=\"1.98\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002677\" bs=\"1\" rto=\"0.98\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002972\" bs=\"2\" rto=\"1.98\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002971\" bs=\"2\" rto=\"1.98\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002973\" bs=\"2\" rto=\"1.98\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002862\" bs=\"1\" rto=\"1.02\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002956\" bs=\"2\" rto=\"1.98\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002957\" bs=\"2\" rto=\"1.98\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002959\" bs=\"2\" rto=\"1.32\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002960\" bs=\"2\" rto=\"1.32\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002961\" bs=\"2\" rto=\"1.32\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002962\" bs=\"2\" rto=\"1.98\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002963\" bs=\"2\" rto=\"1.32\" uid=\"1006\"/></req><req tid=\"4844\"><rec cnl=\"4\" iid=\"1000000002969\" bs=\"2\" rto=\"0.98\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002767\" bs=\"2\" rto=\"1.05\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002770\" bs=\"2\" rto=\"1.05\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002772\" bs=\"2\" rto=\"1.06\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002798\" bs=\"2\" rto=\"1.04\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002805\" bs=\"1\" rto=\"1.03\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002810\" bs=\"1\" rto=\"1.02\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002811\" bs=\"1\" rto=\"1.03\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002815\" bs=\"1\" rto=\"1.03\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002817\" bs=\"1\" rto=\"1.02\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002820\" bs=\"1\" rto=\"1.04\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002821\" bs=\"1\" rto=\"1.02\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002828\" bs=\"1\" rto=\"1.04\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002832\" bs=\"1\" rto=\"1.02\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002833\" bs=\"1\" rto=\"1.02\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002839\" bs=\"1\" rto=\"1.02\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002841\" bs=\"1\" rto=\"1.04\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002843\" bs=\"1\" rto=\"1.03\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002845\" bs=\"1\" rto=\"1.02\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002848\" bs=\"1\" rto=\"1.03\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002850\" bs=\"1\" rto=\"1.04\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002851\" bs=\"1\" rto=\"1.03\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002852\" bs=\"1\" rto=\"1.02\" uid=\"1006\"/></req><req tid=\"4687\"><rec cnl=\"4\" iid=\"1000000002853\" bs=\"1\" rto=\"1.04\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002974\" bs=\"2\" rto=\"1.98\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002975\" bs=\"2\" rto=\"1.32\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002976\" bs=\"2\" rto=\"1.32\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002977\" bs=\"2\" rto=\"1.32\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002978\" bs=\"2\" rto=\"1.32\" uid=\"1006\"/></req><req tid=\"4686\"><rec cnl=\"4\" iid=\"1000000002979\" bs=\"2\" rto=\"1.32\" uid=\"1006\"/></req></req>";
		TimeLogger tl = new TimeLogger();
		ExecutorService service = Executors.newFixedThreadPool(1000);
		for(int i=0;i<Integer.MAX_VALUE;i++){
			final int j = i;
			service.execute(new Runnable() {
				public void run() {
					try {
						TimeLogger tl = new TimeLogger();
						Document doc = DocumentHelper.parseText(xml);
						doc.getRootElement().elements();
						TimeUnit.SECONDS.sleep(100);
						logger.info("msg processed count/time : "+j+"/"+tl.getTimeDiff());
					} catch (Exception e) {
						logger.error(e);
					}
				}
			});
			TimeUnit.MILLISECONDS.sleep(1);
		}
		System.out.println("Create dom time: "+tl.getTimeDiff());
	}
}
