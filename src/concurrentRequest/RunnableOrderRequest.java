package concurrentRequest;

import static concurrentRequest.ConRequest.BASE_URL;
import static concurrentRequest.ConRequest.STR_LOGIN;
import static concurrentRequest.ConRequest.STR_PUSH_ORDER;
import static concurrentRequest.ConRequest.STR_USER_NAME;
import static concurrentRequest.ConRequest.URI_CHARSET;
import static concurrentRequest.ConRequest.WORK_DIR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.rits.cloning.Cloner;

import concurrentRequest.util.diff_match_patch;
import concurrentRequest.util.diff_match_patch.Diff;

public class RunnableOrderRequest implements Runnable {
	private ArrayList<HashMap<String, Object>> incOrderList = new ArrayList<HashMap<String,Object>>();
	private Map<String, Map<String, Object>> fullOrderMap = new HashMap<String, Map<String,Object>>();
//	private Map<String, Map<String, Object>> fullOrderSortedMap = new TreeMap<String, Map<String,Object>>();
	
	
	private Logger log = Logger.getLogger(RunnableOrderRequest.class.getName());
	
	private ObjectMapper mapper = new ObjectMapper();
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private JsonParser jp = new JsonParser();
	
	
	private DefaultHttpClient httpclient = new DefaultHttpClient();
	private CookieStore cookieStore = new BasicCookieStore();
	private HttpContext httpContext = new BasicHttpContext();
	private AtomicInteger mismatchCount = new AtomicInteger(0);
	private int reqCount = 0;
	private static final String basePath = WORK_DIR+"/orderJson/";
	private static final List<String> keywordList = new ArrayList<String>();
	static {
		keywordList.add("order_id");
		keywordList.add("cot_Id");
		
		try {
			// TODO Backup json logs before start a new test session 
			File file = new File(basePath);
			if(file.isDirectory()){
				FileUtils.deleteDirectory(new File(basePath));
			}
		} catch (IOException consumed) {		}
		new File(basePath).mkdirs();
	}
	private static final int FULL_COUNT = 3;
	
	private long prevTime = -1;
	private long currTime = -1;
	
	@Override
	public void run() {
		sendRequest(STR_LOGIN);
		// TODO STOP if login failed
		
		boolean comparedFlag = false;
		for(int i=0;;i++){
			// TODO prevTime might not be correct
			if(prevTime==-1 || comparedFlag){
				comparedFlag=false;
				// First time sending request
				prevTime = System.currentTimeMillis()/1000*1000; // Trim to seconds
			}
			
			if(i==1){
				// Clear data retrieved from first request
				incOrderList.clear();
				prevTime = System.currentTimeMillis()/1000*1000; // Trim to seconds
			}
			
			reqCount++;
			String pushOrderResponse = sendRequest(STR_PUSH_ORDER);
			currTime=System.currentTimeMillis()/1000*1000; // Trim to seconds
			
			try {
				updateIncOrderList(pushOrderResponse);
			} catch (Exception e) {log.error(e);}
			
			
			if (i % FULL_COUNT == (FULL_COUNT - 1)) {
				if(FULL_COUNT-1==i){
					// Do not compare the initial data
					comparedFlag=true;
					incOrderList.clear();
					continue;
				}
				

				
				// Get orders modified from prevTime to the current time
				try {
					fullOrderMap = getFullOrderListFromDB(prevTime, currTime);
				} catch (SQLException e) {
					log.error("DB query error!\n",e);
				}
				
				// compare
				boolean isMatch = compareIncAndFullOrders();
				// clear cached order list
				comparedFlag=true;
				incOrderList.clear();
				
				if(!isMatch){
					try {
						log.error("!!!MISMATCH COUNT!!!"+mismatchCount.incrementAndGet());
						String fullResultStr = mapper.writeValueAsString(fullOrderMap);
//						String incResultStr = mapper.writeValueAsString(incOrderList);
//						writeJsonToLog(incResultStr, 2);
						writeJsonToLog(fullResultStr, 3);
					} catch (JsonProcessingException e) {
						log.error("Create json string error!",e);
					}
				}
			}
		}
	}

	
	@SuppressWarnings("unchecked")
	private boolean compareIncAndFullOrders() {
		// Clone incOrderList to keep it usable in next update 
		ArrayList<HashMap<String, Object>> localIncOrderList=null;
		Cloner cloner = new Cloner();
		cloner.setDumpClonedClasses(false);
		localIncOrderList = (ArrayList<HashMap<String, Object>>) cloner.deepClone(incOrderList);
	
		
		// WARN! Transform incOrderList to map
		Map<String, Map<String, Object>> incOrderMap = transformListToMap(localIncOrderList);
		
		try {
			String incOrderMapJson = mapper.writeValueAsString(incOrderMap);
			writeJsonToLog(incOrderMapJson, 2);
		} catch (JsonProcessingException e) {
			log.error(e);
		}
		return compareIncMap(fullOrderMap, incOrderMap);
		
	}


	private Map<String, Map<String, Object>> transformListToMap(ArrayList<HashMap<String, Object>> localIncOrderList) {
		Map<String, Map<String, Object>> incOrderMap = new HashMap<String, Map<String, Object>>();

		for (int i = 0; i < localIncOrderList.size(); i++) {
			Map<String, Object> orderMap = localIncOrderList.get(i);
//			log.info(StringUtils.join(orderMap.keySet(), ','));
			String key = orderMap.get("order_id").toString();
			incOrderMap.put(key, orderMap);
			Object contractsObj = orderMap.get("contract");
//			log.info("json字段contract的类型为:"+contractsObj.getClass().getName());
			// ---debug only
//			if(contractsObj instanceof HashMap<?, ?>){
//				HashMap<String, Object> tempmap = (HashMap<String, Object>) contractsObj;
//				Iterator<String> tempkeys = tempmap.keySet().iterator();
//				while(tempkeys.hasNext()){
//					log.info(tempkeys.next());
//				}
//			}
			// ---debug only
			ArrayList<HashMap<String, Object>> contractList = (ArrayList<HashMap<String, Object>>) orderMap.get("contract");
			Map<String, Map<String, Object>> contractsMap = new HashMap<String, Map<String, Object>>();
			for (Map<String, Object> contractMap : contractList) {
				String cotKey = contractMap.get("cot_Id").toString();
				if (contractsMap.get(cotKey) != null) {
					log.error("ERROR! DUPLICATE CONTRACT IDS!!!!");
					throw new RuntimeException("Error occurs, check mergeOrderList for duplicate contract ids!");
				}
				contractsMap.put(cotKey, contractMap);
			}
				orderMap.put("contract", contractsMap); // WARN! this will change "contracts" in incOrderList from List to HashMap
			}
		
		
		return incOrderMap;
	}


	/**
	 * 比较两个hashMap
	 * @param mainOrderMap
	 * @param incOrderMap
	 * @return
	 */
	private boolean compareIncMap(Map<String, Map<String, Object>> mainOrderMap, Map<String, Map<String, Object>> incOrderMap ) {
		int diffSize = incOrderMap.size()-mainOrderMap.size();
		if(diffSize!=0){
			log.error("增量中数量与数据库数据量差值:"+diffSize);
			log.error("增量key:"+StringUtils.join(incOrderMap.keySet(), ", "));
			log.error("数据库key:"+StringUtils.join(mainOrderMap.keySet(), ", "));
			return false;
		}
		
		Set<String> keys = mainOrderMap.keySet();
		for (String key : keys) {
			Object updVal = incOrderMap.get(key);
			Object mainVal = mainOrderMap.get(key);
			
			// filter null values
			if(updVal==null){
				if(mainVal!=null){
					log.error("增量数据缺少:"+updVal);
					return false;
				}
				else
					continue;
			}
			
			
			String strUpdVal = updVal.toString();
			String strMainVal = mainVal.toString();
			
			
			if(strUpdVal.equalsIgnoreCase(strMainVal)){
				// Compare as string
				continue;
			}else if(strUpdVal.isEmpty()&&strMainVal.equalsIgnoreCase("0")){
				// for avg_price
				continue;
			}else if(updVal instanceof HashMap<?, ?> && mainVal instanceof HashMap<?, ?>){
				// Compare as map 
				boolean match = compareIncMap((Map<String, Map<String, Object>>)mainVal, (Map<String, Map<String, Object>>)updVal);
				if(!match) {
					return match;
				}

			}else{
				log.error("TYPE OR ATTRIBUTE MISMATCH!");
				log.error("字符串值比较, 数据库/推送值:"+strMainVal+"/"+strUpdVal);
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 使用增量信息更新注单列表:{@link #updOrderList}
	 * @param response
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private void updateIncOrderList(String response) throws JsonParseException, JsonMappingException, IOException {
		response= extractOrderList(response);
		
		writeJsonToLog(response, 0);
		
		if (incOrderList == null || incOrderList.isEmpty()) {
			incOrderList = mapper.readValue(response, new TypeReference<ArrayList<HashMap<String, Object>>>() {});
			// walkList(incResultList);
		} else {
			ArrayList<HashMap<String, Object>> updateList = mapper.readValue(response, new TypeReference<ArrayList<HashMap<String, Object>>>() {
			});
			mergeOrderList(0,incOrderList, updateList);
		}
	}

	/**
	 * 将updateList合并入mainList中.
	 * @param keywordIndex 当前处理的层级. 用于在当前list中提取关键字
	 * @param mainList
	 * @param updateList
	 */
	private void mergeOrderList(int keywordIndex, ArrayList<HashMap<String, Object>> mainList, ArrayList<HashMap<String, Object>> updateList) {
		if(mainList.isEmpty()){
			mainList.addAll(updateList);
			return;
		}
		
		String keyword = keywordList.get(keywordIndex);
		
		// Empty list indicating removal of data unless in top level
		if(keywordIndex!=0 && updateList.size()<1){
			log.debug(mainList.size()+ " entries removed for keyword: "+keyword);
			mainList.clear();
			return ;
		}
		
		// Create an id-map from updateList
		// e.g., 1522 (event_id) to JsonNode map of this event id.
		// TODO poor efficiency walking through mainList everytime
		HashMap<Object,HashMap<String, Object>> idUpdMap = new HashMap<Object, HashMap<String,Object>>();
		for (HashMap<String, Object> updMap : updateList){
			idUpdMap.put(updMap.get(keyword), updMap);
		}
		
		// Create an id-map for mainList
		HashMap<Object,HashMap<String, Object>> idMainMap = new HashMap<Object, HashMap<String,Object>>();
		for (HashMap<String, Object> mainMap : mainList){
			idMainMap.put(mainMap.get(keyword), mainMap);
		}
		
		Set<Object> updKeys = idUpdMap.keySet();
		Set<Object> mainKeys = idMainMap.keySet();
		for (Object key : updKeys) {
			if(!mainKeys.contains(key)){
				log.debug("Adding new "+keyword+" "+key);
				mainList.add(idUpdMap.get(key));
			}else{
				log.debug("update " + keyword + ":"+key);
				// Merge two maps
				HashMap<String, Object> mainSubMap = idMainMap.get(key);
				HashMap<String, Object> updSubMap = idUpdMap.get(key);
				Set<String> updSubKeys = updSubMap.keySet();
				for (String  updSubKey: updSubKeys) {
					Object updVal = updSubMap.get(updSubKey);
					Object mainVal = mainSubMap.get(updSubKey);
					if(mainVal!=null && updVal instanceof ArrayList<?>){
						mergeOrderList(keywordIndex+1, (ArrayList<HashMap<String, Object>>) mainVal, (ArrayList<HashMap<String, Object>>) updVal);
					}else{
						// Value to keyword does not change, no need to update
						if(!updSubKey.equals(keyword)){
							log.debug("Update attribute in : "+keyword + " attribute: " +updSubKey + " to "+updVal);
							if(updSubKey.equalsIgnoreCase("mah_stake")){
								// mah_stake value is incremental, must not override directly
								int oldStakeVal = 0;
								if(mainSubMap.get(updSubKey)!=null)
									oldStakeVal = Integer.parseInt((mainSubMap.get(updSubKey)+""));
								updVal=oldStakeVal+ (Integer) updVal;
							}
							mainSubMap.put(updSubKey, updVal);
						}
					}
				}
			}
		}
	}

	/**
	 * 从返回的数据串中提取用于比较的json数组
	 * @param pushOrderResponse
	 * @return
	 */
	private String extractOrderList(String pushOrderResponse) {
		return StringUtils.substringBetween(pushOrderResponse, "\"data\":", ",\"return_code");
	}


	/**
	 * 向服务器发送doGet请求，打印请求地址与相应的返回结果
	 * 
	 * @param urlStr
	 *            请求地址
	 * @throws HttpException
	 * @throws IOException
	 */
	private String sendRequest(String urlStr) {
		String result = null;
		try {
			String url = new String(urlStr);
			url = BASE_URL + url;

			log.info("REQUEST" + reqCount + " \n" + url);

			httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			HttpUriRequest request = new HttpGet(URIUtil.encodeQuery(url, URI_CHARSET));

			// Execute HTTP GET
			HttpResponse response = httpclient.execute(request, httpContext);

			// Print out response
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// Buffering unknown size: discouraged. Using InputStream
				// instead.
				// String responseStr = httpget.getResponseBodyAsString();
				InputStream is = response.getEntity().getContent();
				StringWriter writer = new StringWriter();
				IOUtils.copy(is, writer, URI_CHARSET);
				log.info("RESPONSE" + reqCount + " \n" + writer.toString());
				result = writer.toString();
			} else
				log.info("ERROR" + StringUtils.repeat("X", 12) + " \n" + response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return result;
	}
	
	
	
	/**
	 * Write input json string to log file, pretty printed
	 * @param uglyStr
	 * @param type 0: incremental, 1: full from db,  
	 * 	2:   accumulated incremental data for comparison, 3: full data for comparison 
	 */
	private void writeJsonToLog(String uglyStr, int type) {
		String postFix = "";
		switch(type){
		case 0: 
			postFix="";
			break;
		case 1:
			postFix="-full";
			break;
		case 2:
			postFix="-inc-acl";
			break;
		case 3:
			postFix="-full-acl"+mismatchCount.get();
			break;
		}
		
		PrintWriter writer = null;
		try {
			String path = basePath+"orderInfo-"+reqCount+postFix+".log";
			writer = new PrintWriter(new FileOutputStream(path, true));
			// pretty print json disabled
			JsonElement je = jp.parse(uglyStr);
			String prettyJsonString = gson.toJson(je);
			writer.append(prettyJsonString);
		} catch (FileNotFoundException e1) {
			log.error(e1);
		} finally{
			writer.close();
		}
	}
	
	
	
	private Map<String, Map<String, Object>> getFullOrderListFromDB(long startFromTimeMillis, long endTimeMillis) throws SQLException {
		// Get intentions first, then contracts. 
		
		// Get intentions
//		String queryIntention ="select "+
//				"ti.intentionid order_id,"+
//				"ti.intentionstatus order_sta,"+
//				"round(to_char(ti.issuedate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000)  pmt_time,"+
//				"round(to_char(ti.updateddate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000)  upd_time,"+
//				"round(to_char(ti.settlementdate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000)  stlmt_time,"+
//				"ti.intentionstatus order_sta,"+
//				"ti.matchamt mah_stake,"+
//				"ti.tradingdirection direction,"+
//				"ti.tradingitemid sel_id,"+
//				"ti.intentionamt stake,"+
//				"ti.ratio price,"+
//				"ti.profit pol_stake,"+
//				"ti.avgratio avg_price "+
//				"from t_intention ti "+
//				"where ti.traderid in "+
//				"(select userid from t_userinfo where login_name=?) and round(to_char(ti.updateddate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000) >? and round(to_char(ti.updateddate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000) <? " +
//				" order by ti.intentionid";
		String queryIntention = " select ti.intentionid order_id, "+
				" ti.traderid cus_id, "+
				" ti.intentionstatus order_sta, "+
				" ti.tradingdirection direction, "+
				" round(to_char(ti.issuedate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000)  pmt_time, "+
				" round(to_char(ti.updateddate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000)  upd_time, "+
				" ti.intentionamt stake, "+
				" to_char(ti.ratio, 'fm9999999990.00') price, "+
				" to_char(ti.matchamt, 'fm9999999990') mah_stake, "+
				" case "+
				" when ti.avgratio > 0 then to_char(ti.avgratio,'fm9999999990.0000') "+
				" else '0' "+
				" end avg_price, "+
				" decode(ti.profit,0,0, to_char(ti.profit, 'fm9999999990.00')) pol_stake, "+
				" case "+
				" when ti.settlementdate > to_date('1900-01-01', 'yyyy-mm-dd') then "+
				" to_char(ti.settlementdate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000 "+
				" else 0 "+
				" end stlmt_time, "+
				" ti.tradingitemid sel_id "+
				" from ( "+
				" select intentionid, "+
				" traderid, "+
				" intentionstatus, "+
				" tradingdirection, "+
				" issuedate, "+
				" updateddate, "+
				" intentionamt, "+
				" ratio, "+
				" matchamt, "+
				" avgratio, "+
				" profit, "+
				" settlementdate, "+
				" tradingitemid "+
				" from t_intention "+
				" where traderid in  "+
				" (select userid from t_userinfo where accountnumber=?) and round(to_char(updateddate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000) >? and round(to_char(updateddate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000) <?  "+
				" union all "+
				" select intentionid, "+
				" traderid, "+
				" intentionstatus, "+
				" tradingdirection, "+
				" issuedate, "+
				" updateddate, "+
				" intentionamt, "+
				" ratio, "+
				" matchamt, "+
				" avgratio, "+
				" profit, "+
				" settlementdate, "+
				" tradingitemid "+
				" from t_intention_invalid "+
				" where traderid in  "+
				" (select userid from t_userinfo where accountnumber=?) and round(to_char(updateddate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000) >? and round(to_char(updateddate - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000) <?  "+
				" ) ti order by ti.intentionid ";


		
		// intention_id to intentionMap
		Map<String,Map<String, Object>> intentionSuperMap = new HashMap<String,Map<String,Object>>();
		Connection conn = DBUtil.getTTConnection();		
		intentionSuperMap= queryForIntentionMapsWithConn(queryIntention,conn, startFromTimeMillis, endTimeMillis);

		// Get contracts
		Set<String> intentionIdSet = intentionSuperMap.keySet();
		
		long intentionSize = intentionIdSet.size();
		// Return empty hashmap if no orders found
		if(intentionSize<1)
			return intentionSuperMap;
		
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<intentionSize;i++){
			builder.append("?,");
		}
		
		String queryContractsPrefix = "select "+
				"tc.intentionid intetion_id,"+
				"tc.matchamt cot_stake,"+
//				"round(to_char(tc.matchdate  - to_date('1970-01-01', 'yyyy-mm-dd')) * 86400000) cot_date,"+
				"round(to_char(tc.matchdate+0 - to_date('1970-01-01','yyyy-mm-dd')) * 86400000) cot_date,"+
				"tc.reason reason,"+
				"tc.tradingdirection direction,"+
				"tc.status cot_sta, "+
				"tc.contractid cot_Id, "+
				"tc.ratio cot_price "+
				"from  t_contract tc "+
				"where tc.intentionid in (";
		String queryContractsPostfix = ") order by tc.intentionid";
		String queryContracts = queryContractsPrefix + builder.substring(0, builder.length()-1) + queryContractsPostfix;
		
		conn = DBUtil.getTTConnection();		
		updateContractMapsFromConn(queryContracts,conn, intentionSuperMap, startFromTimeMillis, endTimeMillis);
		
		return intentionSuperMap;
	}




	private Map<String, Map<String, Object>> queryForIntentionMapsWithConn(String queryStr, Connection conn, long startFromTimeMillis, long endTimeMillis) throws SQLException {
		/**
		 * From intentionId to intention map
		 */
		Map<String,Map<String, Object>> intentionSuperMap = new HashMap<String,Map<String,Object>>();
		try {
			PreparedStatement ps = conn.prepareStatement(queryStr);
			log.info(STR_USER_NAME);
			ps.setString(1, STR_USER_NAME);
			ps.setLong(2, startFromTimeMillis);
			ps.setLong(3, endTimeMillis);
			ps.setString(4, STR_USER_NAME);
			ps.setLong(5, startFromTimeMillis);
			ps.setLong(6, endTimeMillis);
			
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			long updateDatetime = -1;
			long issueDatetime = -1;
			while(rs.next()){
				boolean ignoreByIssueDate = false;
				Map<String,Object> rsMap  = new HashMap<String, Object>(); // A single order
				String intentionId = "";
				int orderSta=-1;
				for(int i=0;i<rsmd.getColumnCount();i++){
					String colName = rsmd.getColumnName(i+1).toLowerCase();
					Object val = rs.getObject(i+1);
					// 意向状态：1交易中、2处理中(拖单)、3处理中(仅交易撤单时)、4交易结束、5已结算、7失效、8已取消、9作废
					if((issueDatetime!=-1 && updateDatetime==issueDatetime && orderSta!=-1 && (orderSta!=4 || orderSta!=8)) 
							|| orderSta==1){
						ignoreByIssueDate = true;
						log.info("Ignoring intention");
						break;
					}
					// Update issue date and update datetime
					// Do not put update time in map
					if(colName.equalsIgnoreCase("upd_time")){
						updateDatetime=((BigDecimal) val).longValue();
						continue;
					}else if(colName.equalsIgnoreCase("pmt_time")){
						issueDatetime=((BigDecimal) val).longValue();
					}else if(colName.equalsIgnoreCase("order_id")){
						// Get intention_id as key value
						intentionId=val.toString();
					}else if(colName.equalsIgnoreCase("stlmt_time")){
						// clear invalid settlement time, e.g., -2208988800000
						long l = ((BigDecimal) val).longValue();
						if(l<=0)
							val="";
					}else if(colName.equalsIgnoreCase("order_sta")){
						orderSta=((BigDecimal) val).intValue();
					}
					
					// Convert null to empty string
					if(val==null)
						val="";
					rsMap.put(colName, val);
				}
				// Do not add into result map if the order is newly created
				if(ignoreByIssueDate)
					continue;
				intentionSuperMap.put(intentionId, rsMap);
			}
		} catch (Exception e) {
			log.error(e);
			throw new SQLException(e);
		} finally{
			DBUtil.close(conn);
		}
		return intentionSuperMap;
	}


	/**
	 * Get contracts from intentions in intentionSuperMap and put contracts in this map
	 * @param queryStr
	 * @param conn
	 * @param intentionSuperMap
	 * @param startFromTimeMillis
	 * @param endTimeMillis
	 * @throws SQLException
	 */
	private void updateContractMapsFromConn(String queryStr, Connection conn, Map<String,Map<String, Object>> intentionSuperMap, long startFromTimeMillis, long endTimeMillis) throws SQLException {
		Set<String> intentionIdSet = intentionSuperMap.keySet();
		
		try {
			PreparedStatement ps = conn.prepareStatement(queryStr);
			Iterator<String> iidIt = intentionIdSet.iterator();
			for(int i=1;iidIt.hasNext();i++){
				long iid = Long.parseLong(iidIt.next());
				ps.setLong(i, iid);
			}

			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();

			
			while(rs.next()){	
				Map<String,Object> rsMap  = new HashMap<String, Object>(); // A single Contract
				String intentionId = "";
				String contractId = "";
				for(int i=0;i<rsmd.getColumnCount();i++){
					String colName = rsmd.getColumnName(i+1).toLowerCase();
					Object val = rs.getObject(i+1);
					if(colName.equalsIgnoreCase("intetion_id")){
						intentionId=val.toString();
					}else if(colName.equalsIgnoreCase("cot_Id")){
						contractId=val.toString();
						colName = "cot_Id";
					}

					// To prevent pretty printing json from ignoring this entry
					// Change null to empty string
					if(val==null)
						val="";
					log.info(colName+"/"+val);
					rsMap.put(colName, val);
				}
				// Initialize contracts map
				Map<String, Map<String,Object>>  contractsMap = (Map<String, Map<String, Object>>) intentionSuperMap.get(intentionId).get("contract");
				if(contractsMap==null)
					contractsMap = new HashMap<String, Map<String,Object>>();
				
				contractsMap.put(contractId, rsMap);
				intentionSuperMap.get(intentionId).put("contract",contractsMap);
				
			}
		} catch (Exception e) {
			log.error(e);
			throw new SQLException(e);
		} finally{
			DBUtil.close(conn);
		}
		
	}
	
	/**
	 * Compare two strings and write diff to a log file
	 * @param fullResultStr
	 * @param incResultStr
	 */
	private void diffStr(String fullResultStr, String incResultStr) {
		diff_match_patch diff = new diff_match_patch();
		List<Diff> diffList = diff.diff_main(fullResultStr, incResultStr, true);
		String path =  WORK_DIR+basePath + "-" +reqCount + "-" +"diff.log";
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileOutputStream(path, true));
			for (Diff diffObj : diffList) {
				writer.append(diffObj.toString());
			}
		} catch (FileNotFoundException e1) {
			log.error(e1);
		} finally{
			writer.close();
		}
		
		log.error(mismatchCount.get()+"-diff is writen to "+path);
	}
}
