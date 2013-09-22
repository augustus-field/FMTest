package misc;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTest {
	int id;
	double price;
	
	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String str0 = "reason▲intentionType▲intentionAmt▲matchAmt▲stprofit▲untradedamt▲syndate▲stmatchamt▲flag▲ratio▲tradingItemId▲tradingDirection▲intentionStatus▲profit▲stamt▲invalidAmt▲strategyId▲issueDate▲avgratio▲settlementdate▲traderId▲intentionId";
//		String str1 = "▲3▲100.0▲100.00000▲▲0▲▲100.00▲0▲0.98▲14612321▲2▲4▲0▲100.0▲0▲▲2013-06-18 07:27:32▲▲▲2395▲1000000008706"; 
//		String dummy0[] = str0.split("▲");
//		String dummy1[] = str1.split("▲");
//		System.out.println(dummy0.length==dummy1.length);
//		for (int i=0; i<dummy0.length;i++) {
//			System.out.println(dummy0[i]+":"+dummy1[i]);
////			System.out.println(dummy1[i].equals(""));
//		}
		
		StringBuffer sb = new StringBuffer('M');
		
		String result = "\\Soccer\\Friendlies\\Fixtures 31 July     \\Ath Bilbao v Zaragoza";
		String[] matched = result.split("\\\\");
		for(int i =0;i<matched.length;i++)
			System.out.println(i+"/"+matched[i]);
		
		System.out.println(String.format("%d", Long.MAX_VALUE));
		
		
		 String indexNum2=null;
		String indexNum = "0" + ""
                 + (null != indexNum2 ? "&" + indexNum2 : "");
		System.out.println(indexNum);
		int tradeItemId = 134321;
		float ratio = 0.223f;
		System.out.println(String.format("处理新赔率变更:交易项ID: %d, 赔率: %s", tradeItemId, Float.toString(ratio)));
		
		
		// Regex search
		String ratioXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><req dt=\"1\" opt=\"0\" mid=\"15414345\"><rec sbfid=\"1168579\" tid=\"15554296\" trgt=\"lj,sb,hg,pb,bw,bc\" rto=\"0,0.73,0,0,0,0\" maxBet=\"0,0.0,0,0,0,0\" type=\"-1,1,-1,-1,-1,-1\"/><rec sbfid=\"1168576\" tid=\"15554295\" trgt=\"lj,sb,hg,pb,bw,bc\" rto=\"0,1.09,0,0,0,0\" maxBet=\"0,0.0,0,0,0,0\" type=\"-1,1,-1,-1,-1,-1\"/><rec sbfid=\"1258273\" tid=\"15554294\" trgt=\"lj,sb,hg,pb,bw,bc\" rto=\"0,0.96,0,0,0,0\" maxBet=\"0,0.0,0,0,0,0\" type=\"-1,1,-1,-1,-1,-1\"/><rec sbfid=\"1258269\" tid=\"15554293\" trgt=\"lj,sb,hg,pb,bw,bc\" rto=\"0,0.88,0,0,0,0\" maxBet=\"0,0.0,0,0,0,0\" type=\"-1,1,-1,-1,-1,-1\"/><rec sbfid=\"1168533\" tid=\"15554292\" trgt=\"lj,sb,hg,pb,bw,bc\" rto=\"0,1.08,0,0,0,0\" maxBet=\"0,0.0,0,0,0,0\" type=\"-1,1,-1,-1,-1,-1\"/><rec sbfid=\"1168530\" tid=\"15554291\" trgt=\"lj,sb,hg,pb,bw,bc\" rto=\"0,0.74,0,0,0,0\" maxBet=\"0,0.0,0,0,0,0\" type=\"-1,1,-1,-1,-1,-1\"/><rec sbfid=\"1258244\" tid=\"15554289\" trgt=\"lj,sb,hg,pb,bw,bc\" rto=\"0,0.8,0,0,0,0\" maxBet=\"0,0.0,0,0,0,0\" type=\"-1,1,-1,-1,-1,-1\"/><rec sbfid=\"1258252\" tid=\"15554290\" trgt=\"lj,sb,hg,pb,bw,bc\" rto=\"0,1.04,0,0,0,0\" maxBet=\"0,0.0,0,0,0,0\" type=\"-1,1,-1,-1,-1,-1\"/></req>";
		String QUOTE_SYM = "[\'\"]";
		String regex = "tid="+QUOTE_SYM+"(\\d+)"+QUOTE_SYM+".*?trgt="+QUOTE_SYM+"(.*?)"+QUOTE_SYM
				+".*?rto="+QUOTE_SYM+"(.*?)"+QUOTE_SYM;
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(ratioXml);
		
		Map<String,String> rtoMap = new HashMap<String, String>();
		
		while(matcher.find()){
//			String group = matcher.group();
			String tid = matcher.group(1);
			String[] rtoSrcs = matcher.group(2).split(",");
			int i = rtoSrcs.length-1;
			for(; i>=0 ; i--){
				if(rtoSrcs[i].equalsIgnoreCase("bc")){
					break;
				}
			}
			String rto = matcher.group(3).split(",")[i];
			String oldRto = rtoMap.get(tid);
			if(!rto.equals(oldRto)){
				System.out.println(tid+" - bcRto: "+rto);
				rtoMap.put(tid, rto);
			}
		}
		String name =null;
		StringTest test = new StringTest();
		int id = test.id;
		double price = test.price;
		System.out.println(String.format("name %s, int: %d, double: %f", name, id, price));
		
		System.out.println(TimeZone.getTimeZone("GMT-0"));
		System.out.println(TimeZone.getTimeZone("GMT"));
	}

}
