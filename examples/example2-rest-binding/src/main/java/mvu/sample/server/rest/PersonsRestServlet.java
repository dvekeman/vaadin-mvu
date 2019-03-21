package mvu.sample.server.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mvu.sample.model.Person;

public class PersonsRestServlet extends HttpServlet {

	private final List<Person> heros;

	public PersonsRestServlet() {
		super();

		Person steve = new Person("Steve", "Wozniak");
		Person evan = new Person("Evan", "Czaplicki");
		Person ollie = new Person("Oliver", "Charles");

		heros = Arrays.asList(steve, evan, ollie);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String data = heros.stream()
				.map(p -> String.format("%s %s", p.getFirstName(), p.getLastName()))
				.collect(Collectors.joining(","));

		response.getOutputStream().write(
				data.getBytes(StandardCharsets.UTF_8)
		);
	}
}
