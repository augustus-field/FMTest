package com.sgfm.api.test;


import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.utils.CloneUtils;


import com.rits.cloning.Cloner;

public class ClonnerTest {
	private static  ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();

	
	public static void main(String[] args) throws CloneNotSupportedException {
		for(int i=0;i<10;i++){
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("map"+i, new MyEntity(i+""));
			list.add(map);
		}
		
		// Java deep clone library: https://code.google.com/p/cloning/w/list
		// and http://stackoverflow.com/questions/665860/deep-clone-utility-recomendation
		// Note: Cloner registers some classes as immutable, and will NOT deep copy these class instances. See registerKnownJdkImmutableClasses in Cloner source.
		Cloner cloner = new Cloner();
		cloner.setDumpClonedClasses(true);
		ArrayList<HashMap<String,Object>>  clonedList = (ArrayList<HashMap<String, Object>>) cloner.deepClone(list);
		ArrayList<HashMap<String,Object>>  shallowList = (ArrayList<HashMap<String,Object>>) CloneUtils.clone(list);
		boolean clonerIdentical = false;
		boolean shallowCloneIdentical = false;
		for(int i=0;i<10;i++){
			if(clonedList.get(i)==list.get(i))
				clonerIdentical=true;
			else if(clonedList.get(i).get("map"+i)==list.get(i).get("map"+i))
				clonerIdentical=true;
				
			if(shallowList.get(i)==list.get(i))
				shallowCloneIdentical=true;
		}		
		
		System.out.println("cloner produces identical objects: "+clonerIdentical);
		System.out.println("ClonerUtils produces identical objects: "+shallowCloneIdentical);
		MyEntity l = new MyEntity(2);
		MyEntity n = new MyEntity(2);
		System.out.println(l==n);
	}


}

class MyEntity {
	String name;
	public MyEntity(String name) {
		this.name=name;
	}
	public MyEntity(int intName) {
		this.name=name+"";
	}
}
