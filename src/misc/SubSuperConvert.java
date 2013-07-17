package misc;

public class SubSuperConvert {
	public static void main(String[] args) {
		Child c = new Child();
		c.setId(1);
		c.setName("first c");
		
		System.out.println((Parent) c);
	}
}

class Parent{
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	@Override
	public String toString() {
			return "parent method";
	}
}

class Child extends Parent{
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "child method";
	}
}
