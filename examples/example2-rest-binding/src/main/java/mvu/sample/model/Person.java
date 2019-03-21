package mvu.sample.model;

public class Person {

	private final String firstName;

	private final String lastName;

	public Person(String firstName, String lastName){
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public Person(Person other){
		this(other.firstName, other.lastName);
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}
