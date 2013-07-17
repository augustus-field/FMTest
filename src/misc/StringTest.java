package misc;

import org.apache.commons.lang.StringUtils;

public class StringTest {

	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String str0 = "reason▲intentionType▲intentionAmt▲matchAmt▲stprofit▲untradedamt▲syndate▲stmatchamt▲flag▲ratio▲tradingItemId▲tradingDirection▲intentionStatus▲profit▲stamt▲invalidAmt▲strategyId▲issueDate▲avgratio▲settlementdate▲traderId▲intentionId";
		String str1 = "▲3▲100.0▲100.00000▲▲0▲▲100.00▲0▲0.98▲14612321▲2▲4▲0▲100.0▲0▲▲2013-06-18 07:27:32▲▲▲2395▲1000000008706"; 
		String dummy0[] = str0.split("▲");
		String dummy1[] = str1.split("▲");
		System.out.println(dummy0.length==dummy1.length);
		for (int i=0; i<dummy0.length;i++) {
			System.out.println(dummy0[i]+":"+dummy1[i]);
//			System.out.println(dummy1[i].equals(""));
		}
	}

}
