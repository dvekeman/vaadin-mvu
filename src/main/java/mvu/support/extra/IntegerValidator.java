package mvu.support.extra;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;

public class IntegerValidator implements Validator<String> {
	@Override
	public ValidationResult apply(String s, ValueContext valueContext) {
		try {
			if (s == null || s.isEmpty()) {
				return ValidationResult.ok();
			}
			Integer.valueOf(s);
			return ValidationResult.ok();
		} catch (NumberFormatException nfe) {
			return ValidationResult.error(String.format("Cannot convert %s into an integer", s));
		} catch (Exception e) {
			return ValidationResult.error("Unknown exception converting your input");
		}
	}
}
