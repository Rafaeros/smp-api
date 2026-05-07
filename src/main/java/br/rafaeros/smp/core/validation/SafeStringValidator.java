package br.rafaeros.smp.core.validation;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SafeStringValidator implements ConstraintValidator<SafeString, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        // Strip all HTML/script tags; if the result differs, input contains markup
        String cleaned = Jsoup.clean(value, Safelist.none());
        return cleaned.equals(value);
    }
}
