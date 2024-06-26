package ru.kernelpunik.teradactyle.services;

import org.springframework.stereotype.Service;
import ru.kernelpunik.teradactyle.models.Language;

import java.util.Arrays;
import java.util.List;

@Service
public class LanguageService {
    public List<Language> getLanguages() {
        return Arrays.stream(Language.values()).toList();
    }


}
