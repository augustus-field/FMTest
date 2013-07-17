package concurrentRequest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import concurrentRequest.util.diff_match_patch;
import concurrentRequest.util.diff_match_patch.Diff;
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

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static concurrentRequest.ConRequest.*;

public class RunnableMarketsRequest implements Runnable {
	private ArrayList<HashMap<String, Object>> incResultList = new ArrayList<HashMap<String,Object>>();
	private ArrayList<HashMap<String, Object>> fullResultList = new ArrayList<HashMap<String,Object>>();
	private Logger log = Logger.getLogger(RunnableMarketsRequest.class.getName());
	private static final int FULL_COUNT = 5;

	private static final String KEYWORD_EVENT = "event_id";
	private static final String KEYWORD_MKT = "mkt_id";
	private static final String KEYWORD_SELECTION = "sel_id";
	private static final String KEYWORD_ODDS = "odds";
	/**
	 *  values of these keywords will be used to construct hash maps
	 */
	private static final List<String> keywordList = new ArrayList<String>();
	/**
	 * key of status as in json objects
	 */
	private static final List<String> statusList = new ArrayList<String>();
	/**
	 * keys for json objects which will be replace as is
	 */
	private static final Set<String> replaceKeySet = new HashSet<String>();
	
	private static final String  dataPathPrefix="/marketLog/marketInfo";
	private MisMatchType misMatchType = null;
	enum MisMatchType {
		ELEMENTS_SIZE,
		PROPERTY_VALUE, 
		ATTRIBUTE_NAMES, 
		ELEMENTS_ATTRIBUTES;
	}
	
	static {
		keywordList.add(KEYWORD_EVENT);
		keywordList.add(KEYWORD_MKT);
		keywordList.add(KEYWORD_SELECTION);
		keywordList.add(KEYWORD_ODDS);

		statusList.add("event_sta");
		statusList.add("mkt_sta");
		
		replaceKeySet.add("lay");
		replaceKeySet.add("back");
		
		String path = WORK_DIR+"/marketLog";

		try {
			// TODO Backup json logs before start a new test session 
			File file = new File(path);
			if(file.isDirectory()){
				FileUtils.deleteDirectory(new File(path));
			}
		} catch (IOException consumed) {		}
		new File(path).mkdirs();
	}


	private DefaultHttpClient httpclient = new DefaultHttpClient();
	private CookieStore cookieStore = new BasicCookieStore();
	private HttpContext httpContext = new BasicHttpContext();

	private String outputPath;
	
	
	
	private static AtomicInteger threadCount = new AtomicInteger(0);
	private AtomicInteger mismatchCount = new AtomicInteger(0);
	private ObjectMapper mapper = new ObjectMapper();

	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private JsonParser jp = new JsonParser();
	
	// request count
	private int reqCount = 0;
	private long fullJsonLogTime = 0;
	
	@Override
	public void run() {
		// log.info("比较全量与增量信息线程开始!");
		
		// Setup log path
		outputPath = "MarketInfoCompare" + threadCount.incrementAndGet() + ".log";

		try {
			sendRequest(STR_LOGIN);
		} catch (Exception e) {
			log.error(e);
			return;
		}

		BigDecimal lastVer = new BigDecimal(0);
		for (int i = 0;; i++) {
			String strGetMarketInc = null; // request url of incremental data
			// Get full data on first request
			if(i!=0) 
				strGetMarketInc = String.format("marketInfo.sv?param={'ver_num':%s}", lastVer.toPlainString());
			else
				strGetMarketInc = "marketInfo.sv";
			
			String incData = sendRequest(strGetMarketInc);

			try {
				updateIncResultList(incData);
			} catch (Exception e) {
				log.error("updateIncResultList exception:\n",e);
			}
			
			// update Last version number
			lastVer = updateVerNum(incData);

			// Fetch and compare full data 
			// after every FULL_COUNT requests
			if (i % FULL_COUNT == (FULL_COUNT - 1)) {
				reqCount++;
				String fullData = sendRequest(STR_GET_MARKET_BASE);
				long preVer = lastVer.longValue();
				lastVer = updateVerNum(fullData);
				// Compare if full/incremental data have the same ver_num
				if (lastVer.longValue() == preVer) {
					log.debug("比较第"+reqCount+"次请求,版本:"+preVer);
					try {
						updateFullResultList(fullData);
					} catch (Exception e) {
						log.error("udpateFullResultList exception: \n",e);
					}
					
					boolean mismatch = false;
					
					if(!compareList(0,fullResultList, incResultList)){
						log.error("!!!!!MISMATCH TYPE:"+misMatchType+" COUNT: "+mismatchCount.incrementAndGet());
						try {
							log.error(mapper.writeValueAsString(fullResultList));
							log.error(" >>>>全量信息 不匹配 增量信息 <<<< ");
							log.error(mapper.writeValueAsString(incResultList));
							
							mismatch=true; // flag for writing json logs
						} catch (JsonProcessingException e) {
							log.error("json compare exception: ", e);
						}
					}
					
					long currTime = System.currentTimeMillis();
					if(mismatch || currTime-fullJsonLogTime>60*1000){
						try {
							// Convert List of hashmaps back to string and pretty printed to log files.
							String fullResultStr = mapper.writeValueAsString(fullResultList);
							String incResultStr = mapper.writeValueAsString(incResultList);
							writeJsonToLog(incResultStr, 2);
							writeJsonToLog(fullResultStr, 3);
							
							// Use modified google diff util to compare a by string 
							// - optional 
							if(mismatch)
								diffStr(fullResultStr, incResultStr);
							
							fullJsonLogTime=currTime;
						} catch (JsonProcessingException e) {
							log.error("json compare exception: ", e);
						}
					}
				}
			}
			reqCount++;
		}

		//log.info("\n\nTotal number of requests sent: " + reqCount);
	}


	/**
	 * Compare two strings and write diff to a log file
	 * @param fullResultStr
	 * @param incResultStr
	 */
	private void diffStr(String fullResultStr, String incResultStr) {
		diff_match_patch diff = new diff_match_patch();
		List<Diff> diffList = diff.diff_main(fullResultStr, incResultStr, true);
		String path =  WORK_DIR+dataPathPrefix + "-" +reqCount + "-" +"diff.log";
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



	/**
	 * Recursively compare two ArrayList of HashMaps. fullList and incList MUST not have
	 * HashMap entries with duplicate keyword values, e.g.,
	 * <pre> [{event_id:223, event_sta:7, ...},{event_id:223, event_sta:10, ...},...]</pre>
	 * The above ArrayList should be merged first by calling {@link #mergeUpdSameEvent}. 
	 * @param keywordIndex
	 * @param fullList
	 * @param incList
	 * @return
	 */
	private boolean compareList(int keywordIndex, ArrayList<HashMap<String, Object>> fullList, ArrayList<HashMap<String, Object>> incList) {
		String keyword = keywordList.get(keywordIndex);
		
		if(incList.size()!=fullList.size()){
			writeLog("全量信息中的数组大小: "+fullList.size(), outputPath);
			writeLog(StringUtils.join(fullList, ","), outputPath);
			writeLog("与增量信息数组数量大小: "+incList.size()+"不匹配: ", outputPath);
			writeLog(StringUtils.join(incList, ","), outputPath);
			misMatchType= MisMatchType.ELEMENTS_SIZE;
			return false;
		}
		
		// Create an id-map from updateList
		// e.g., 1522 (event_id) to HashMap (a sub json node) of this event id.
		HashMap<Object,HashMap<String, Object>> idUpdMap = new HashMap<Object, HashMap<String,Object>>();
		for (HashMap<String, Object> updMap : incList){
			idUpdMap.put(updMap.get(keyword), updMap);
		}
		
		// Create an id-map for mainList
		HashMap<Object,HashMap<String, Object>> idMainMap = new HashMap<Object, HashMap<String,Object>>();
		for (HashMap<String, Object> mainMap : fullList){
			idMainMap.put(mainMap.get(keyword), mainMap);
		}
		
		Set<Object> updKeys = idUpdMap.keySet();
		Set<Object> mainKeys = idMainMap.keySet();
		
		if(!updKeys.equals(mainKeys)){
			// TODO possible type mismatch 
			writeLog(keyword+"节点中的全量信息属性名: ", outputPath);
			writeLog(StringUtils.join(mainKeys, ","), outputPath);
			writeLog("与相应节点的增量信息属性名不匹配: ",outputPath);
			writeLog(StringUtils.join(updKeys, ",") ,outputPath);
			misMatchType= MisMatchType.ATTRIBUTE_NAMES;
			return false;
		}
		
		for (Object key : updKeys) {
			// Compare two maps
			HashMap<String, Object> mainSubMap = idMainMap.get(key);
			HashMap<String, Object> updSubMap = idUpdMap.get(key);
			
			// Keyset of one hashMap
			Set<String> updSubKeys = updSubMap.keySet();
			Set<String> mainSubKeys = mainSubMap.keySet();
			if(!mainSubKeys.equals(updSubKeys)){
				// TODO possible type mismatch 
				writeLog(keyword+"节点中的 "+key+" Json对象不匹配, 全量信息: ", outputPath);
				writeLog(StringUtils.join(mainSubKeys, ","), outputPath);
				writeLog("增量信息: ",outputPath);
				writeLog(StringUtils.join(updSubKeys, ","), outputPath);
				misMatchType= MisMatchType.ELEMENTS_ATTRIBUTES;
				return false;
			}
			
			for (String updSubKey : updSubKeys) {
				Object updVal = updSubMap.get(updSubKey);
				Object mainVal = mainSubMap.get(updSubKey);
				if (updVal instanceof ArrayList<?>) {
					// Return false immediately if mismatch found 
					if(compareList(keywordIndex + 1, (ArrayList<HashMap<String, Object>>) mainVal, (ArrayList<HashMap<String, Object>>) updVal)
							==false)
						return false;
				} else {
					// Compare attribute directly
					if(!updVal.equals(mainVal)){
						// Ignore minutes difference per configuration 
						if(IGNORE_MINUTE_DIFF.equals("1") && updSubKey.equals("minutes")){
							log.info("忽略minutes属性值差异");
						}
						else{
							writeLog(keyword + "/"+updSubMap.get(keyword) +"节点中的"+updSubKey+"属性值不匹配", outputPath);
							writeLog("全量信息值: "+ mainVal, outputPath);
							writeLog("增量信息值: "+ updVal, outputPath);
							misMatchType= MisMatchType.PROPERTY_VALUE;
							return false;
						}
					}
				}
			}
		}
		
		
//		walkList(incList);
//		walkList(fullList);
		return true;
	}

	/**
	 * Update fullResultList by converting from raw fullData
	 * @param fullData
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private void updateFullResultList(String fullData) throws JsonParseException, JsonMappingException, IOException {
		fullData = getEventData(fullData);
		writeJsonToLog(fullData, 1);
		
		fullResultList = mapper.readValue(fullData, new TypeReference<ArrayList<HashMap<String, Object>>>() {});
		Collections.sort(fullResultList, new TopComparator());
	}


	/**
	 * Update incResultList from incremental input String
	 * TODO Remove entries of certain status
	 * @param incData
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	private void updateIncResultList(String incData) throws JsonProcessingException, IOException {
		incData = getEventData(incData);
		
		writeJsonToLog(incData, 0);
		
//		log.info(incData);
		// Update the result list unless it's the first time of data received
		if (incResultList == null || incResultList.isEmpty()) {
			incResultList = mapper.readValue(incData, new TypeReference<ArrayList<HashMap<String, Object>>>() {});
			// walkList(incResultList);
		} else {
			ArrayList<HashMap<String, Object>> updateList = mapper.readValue(incData, new TypeReference<ArrayList<HashMap<String, Object>>>() {});
			selfCleanUpdateList(updateList);
			mergeResultList(0,incResultList, updateList);
		}

		Collections.sort(incResultList, new TopComparator());
		
		// walkList(list);

	}
	
	/**
	 * updateList may include hashMaps with identical event_ids. This method properly merges these entries.
	 * 
	 * Note: Merging events with duplicate event_ids in updateList is achieved by merging all duplicates to the first event
	 * of this event_id.   
	 * @param updateList
	 */
	private void selfCleanUpdateList(ArrayList<HashMap<String, Object>> updateList) {
		// eventMaps: event_id -> List of events
		Map<String, ArrayList<HashMap<String,Object>>> eventMaps = new HashMap<String, ArrayList<HashMap<String,Object>>>(); 

		Set<String> dupEventIdList = new HashSet<String>();// duplicated event ids
		// Put hash maps of identical event_ids in a list
		for(int i=0;i<updateList.size();i++){
			HashMap<String, Object> eventMap = updateList.get(i);
			String eventIdStr = eventMap.get(keywordList.get(0)).toString();
			ArrayList<HashMap<String,Object>> sameIdEventList = eventMaps.get(eventIdStr);
			
			if(sameIdEventList==null)
				sameIdEventList= new ArrayList<HashMap<String,Object>>();
			sameIdEventList.add(eventMap);
			eventMaps.put(eventIdStr, sameIdEventList);
			
			// Save duplicated ids
			if(sameIdEventList.size()>1){
				dupEventIdList.add(eventIdStr);
				// and remove duplicated entry from list
				int sizeDiff = updateList.size() - updateList.size();
				updateList.remove(i-sizeDiff);
			}
		}		

		// Exit if no duplication exists
		if(dupEventIdList.size()==0)
			return;
		
		// deal with duplications
		for(String eventId: dupEventIdList){
			ArrayList<HashMap<String, Object>>  sameEventIdList = eventMaps.get(eventId);
			mergeUpdSameEvent(sameEventIdList);
		}
	}


	/**
	 * Merge a list of events (from incremental data) according to the order of appearance: events with larger indices overrides 
	 * events with smaller indices.
	 * @param sameEventIdList
	 * @return The first entry of the HashMap which has been merged with all other entries.
	 */
	private HashMap<String, Object> mergeUpdSameEvent(ArrayList<HashMap<String, Object>> sameEventIdList) {
		ArrayList<HashMap<String, Object>> resultList = new ArrayList<HashMap<String,Object>>();
		resultList.add(sameEventIdList.get(0));
		for(int i=1;i<sameEventIdList.size();i++){
			ArrayList<HashMap<String, Object>> updateList = new ArrayList<HashMap<String,Object>>();
			updateList.add(sameEventIdList.get(i));
			mergeResultList(0, resultList, updateList);
		}
		
		return resultList.get(0);
	}


	/**
	 * Write input json string to log file, pretty printed
	 * @param uglyStr
	 * @param type 0: incremental, 1: full, 
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
			postFix="-inc-acl"+mismatchCount.get();
			break;
		case 3:
			postFix="-full-acl"+mismatchCount.get();
			break;
		}
		
		PrintWriter writer = null;
		try {
			String path = WORK_DIR+ dataPathPrefix+"-"+reqCount+postFix+".log";
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
	
	/**
	 * Write input json list to log file, pretty printed
	 * @param list
	 * @param type 0: incremental, 1: full, 
	 * 	2:   accumulated incremental data for comparison, 3: full data for comparison 
	 * @throws JsonProcessingException 
	 */
	private void writeJsonListToLog(ArrayList<HashMap<String, Object>> list, int type) throws JsonProcessingException {
		String uglyJson = mapper.writeValueAsString(list);
		writeJsonToLog(uglyJson, type);
	}

	/**
	 * TODO Cannot deal with multiple arrays in one json object, e.g.,
	 * {home:la, mks:[...], sels:[...]} 
	 * @param keywordIndex
	 * @param mainList
	 * @param updateList
	 */
	private void mergeResultList(int keywordIndex, ArrayList<HashMap<String, Object>> mainList, ArrayList<HashMap<String, Object>> updateList) {
		// Status in updateList could be invalid, this is why you
		// can not just add all updateList even when mainList is empty,
//		if(mainList.isEmpty()){
//			mainList.addAll(updateList);
//			return;
//		}
		
		String keyword = keywordList.get(keywordIndex);
		
		// Empty list indicating removal of data unless in top level
		if(keywordIndex!=0 && updateList.size()<1){
			// Update clears previous values, e.g, 
			// [{"mkt_id":4152,"selections":[{"sel_id":6804,"back":[]}]}]
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
		
		Set<Object> updKeys = idUpdMap.keySet(); // Set of event_ids from updateList
		Set<Object> mainKeys = idMainMap.keySet(); // Set of event_ids from mainList
		
		boolean isEntryRemoved = false; // Whether entry need to be removed from mainList
		
		// Iterate event_ids
		for (Object key : updKeys) {
			HashMap<String,Object> eventMap = idUpdMap.get(key);
			
			// Remove entries with invalid status
			// event_sta:  0删除、1新建、2准备、3普通盘交易、4滚球盘交易、5交易已停生、6结束 
			// Remove events if it is top level (event_id) and event_sta is 0, 5, 6
			if(keywordIndex==0){
				Integer eventStatus = (Integer) eventMap.get(statusList.get(keywordIndex));
				if(eventStatus!=null && (eventStatus<1 || eventStatus>4)){
					log.info("Mark for removal: "+keyword+ ", status: "+eventStatus);
					// Remove information for this eventId
					if(idMainMap.get(key)!=null) {
						idMainMap.get(key).clear();
						isEntryRemoved = true;
					}
					continue;
				}
			}else if(keywordIndex==1){
				// mkt_sta: 1待开市、2开市待审核、3集合竞价中、4结束竞价待审核、5待开盘、6开盘待审核、7开盘中、8暂停中、9收盘待审核、10已收盘、
				// 11停盘待审核、12已停盘、13赛果待审核、14待结算、15待发送、16已发送、17已结束、18交易已停止
				Integer eventStatus = (Integer) eventMap.get(statusList.get(keywordIndex));
				if(eventStatus!=null && eventStatus>7){
					log.info("Mark for removal "+keyword+ " by status "+eventStatus);
					// Remove information for this eventId
					if(idMainMap.get(key)!=null) {
						idMainMap.get(key).clear();
						isEntryRemoved = true;
					}
					continue;
				}
			}
			
			log.debug("update " + keyword + ":"+key);
			// Ignore this event if mainList does not have this event
			if(!mainKeys.contains(key)){
				log.debug("Adding new "+keyword+" "+key);
				mainList.add(eventMap);
				continue;
			}else{
				// Merge two maps
				HashMap<String, Object> mainSubMap = idMainMap.get(key);
				HashMap<String, Object> updSubMap = idUpdMap.get(key);
				Set<String> updSubKeys = updSubMap.keySet();
				for (String  updSubKey: updSubKeys) {
					Object updVal = updSubMap.get(updSubKey);
					Object mainVal = mainSubMap.get(updSubKey);
					
					if(mainVal!=null && updVal instanceof ArrayList<?> && !replaceKeySet.contains(updSubKey)){
						// List will be merged unless it is the value for keys in repalceKeySets(lay, back) 
						mergeResultList(keywordIndex+1, (ArrayList<HashMap<String, Object>>) mainVal, (ArrayList<HashMap<String, Object>>) updVal);
					}else{
						// Value to keyword does not change, no need to update
						if(!updSubKey.equals(keyword)){
							log.debug("Update attribute in : "+keyword + " attribute: " +updSubKey + " to "+updVal);
							// introduce manual error for testing 
//							Random rand = new Random();
//							if(3==rand.nextInt(4))
//								updVal="error"+updVal.toString();
							mainSubMap.put(updSubKey, updVal);
						}
					}
				}
			}
		}
		
		// Remove empty hashMaps from mainList
		for (int i=mainList.size()-1; (isEntryRemoved == true) && i>=0;i-- ){
			HashMap<String, Object> mainMap = mainList.get(i);
			if(mainMap.size()<1){
				// Remove keyword marked previously 
				mainList.remove(i);
			}
		}
		
	}
	

	/**
	 * Print all key-val pairs of all sub nodes
	 * 
	 * @param list
	 */
	private void walkList(ArrayList<HashMap<String, Object>> list) {
		for (HashMap<String, Object> topMap : list) {
			Set<String> keys = topMap.keySet();
			for (String key : keys) {
				Object node = topMap.get(key);
				
				if (node instanceof ArrayList<?>) {
					writeLog(key + " includes >>>> " , outputPath);
					walkList((ArrayList<HashMap<String, Object>>) node);
				}else{
					writeLog(key+":"+node.toString(), outputPath);
				}
			}
		}
	}


	private String getEventData(String str) {
		return StringUtils.substringBetween(str, "\"events\":", ",\"return_code");
	}

	private BigDecimal updateVerNum(String incData) {
		String versionStr = StringUtils.substringBetween(incData, "ver_num\":", "}");
//		log.info(versionStr);
		return new BigDecimal(versionStr);
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

			log.info("REQUEST "+ reqCount + StringUtils.repeat("$", 0) + " \n" + url);

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
				log.info("RESPONSE "+reqCount + StringUtils.repeat("#", 0) + " \n" + writer.toString());
				result = writer.toString();
			} else
				log.info("ERROR" + StringUtils.repeat("X", 12) + " \n" + response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return result;
	}

	public void writeLog(String str, String path){
		log.info(str);
	}
	
	
	/**
	 * Compare two hashmap using their respective values for top level key
	 *
	 */
	class   TopComparator implements Comparator<HashMap>{
		@Override
		public int compare(HashMap o1, HashMap o2) {
			if(o1.isEmpty() || o2.isEmpty())
				return o1.size()-o2.size();
			String keyword = keywordList.get(0);
			return o1.get(keyword).toString().compareTo(o2.get(keyword).toString());
		}
	}
//	public void writeLog(String str, String path) {
////		System.out.println(System.currentTimeMillis() + "/" + (new Date()) + StringUtils.repeat(">", 5) + str);
//		System.out.println(str);
//		try {
//			if (pw == null) {
//				pw = new PrintWriter(new FileOutputStream(path, true));
//				pw.append("\n\n" + StringUtils.repeat("*", 32) + "\n");
//			}
////			pw.append(System.currentTimeMillis() + "/" + (new Date()) + StringUtils.repeat(">", 1) +" "+ str);
//			pw.append(str);
//			pw.flush();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//			pw.append("\n");
//		}
//	}
}
