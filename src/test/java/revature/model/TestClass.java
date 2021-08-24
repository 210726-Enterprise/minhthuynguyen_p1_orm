package revature.model;

import com.revature.p1.orm.annotations.*;

@Entity(tableName= "test_table")
public class TestClass {
	
	@Id(columnName = "test_id")
	private int id;
	
	@Column(columnName = "test_field_1")
	private String testField1;
	
	@Column(columnName = "test_field_2")
	private int testField2;

	public TestClass() {
		this.id = 0;
	}

	public TestClass(String testField1, int testField2) {
		//this.id = id;
		this.testField1 = testField1;
		this.testField2 = testField2;
	}

	public String getTestField1() {
		return testField1;
	}

	public void setTestField1(String testField1) {
		this.testField1 = testField1;
	}

	public int getTestField2() {
		return testField2;
	}

	public void setTestField2(int testField2) {
		this.testField2 = testField2;
	}

	public int getId() { return id; }

	public void setId(int iID) {
		this.id = iID;
	}
}
