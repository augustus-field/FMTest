package misc;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class DocumentHelperTest {

	public static void main(String[] args) {
		Document doc = DocumentHelper.createDocument();
		Element reqElement = doc.addElement("req");
		// 交易项ID
		reqElement.addAttribute("tid", 2132+"");
//		reqElement.addAttribute("t", "3");//1.7已取消
		Element recElement = reqElement.addElement("rec");
		//设置意向来源4表示api
		recElement.addAttribute("cnl","4");
		// 意向ID
		recElement.addAttribute("iid","iid");
		// 交易方向 ,1 表示买、2 表示卖.
		recElement.addAttribute("bs", "bs");
		// 赔率
		recElement.addAttribute("rto","ratio");
		// 代理子成员ID
//		recElement.addAttribute("suid", String.valueOf(userId)); //1.7改为uid
		recElement.addAttribute("uid", "uid");
		//意向类型，铺货系统的意向全是香港盘
		recElement.addAttribute("mtp", "1");
		String reqXml = doc.asXML();
		System.out.println(reqXml);
	}

}
