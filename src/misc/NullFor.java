package misc;
import java.util.HashSet;
import java.util.List;


public class NullFor {
	public static void main(String[] args) {
		List<Object> list = null;
		for(int i=0;i<list.size();i++){
			System.out.println(list.get(i));
		}
		
		for (Object obj : list) {
			obj.toString();
		}
		
		
	}
}
