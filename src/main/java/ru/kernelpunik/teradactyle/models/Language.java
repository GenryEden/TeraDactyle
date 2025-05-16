package ru.kernelpunik.teradactyle.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterCpp;
import org.treesitter.TreeSitterJava;
import org.treesitter.TreeSitterPython;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Language {
    PYTHON(0, "Python", new TreeSitterPython(), "py"),
    JAVA(1, "Java", new TreeSitterJava(), "java"),
    CPP(2, "C++", new TreeSitterCpp(), "cpp");
    public final int id;
    public final String name;
    public final Set<String> extensions;
    @JsonIgnore
    public final TSLanguage tsLanguage;
    Language(int id, String name, TSLanguage tsLanguage, Set<String> extensions) {
        this.id = id;
        this.name = name;
        this.tsLanguage = tsLanguage;
        this.extensions = extensions;
    }

    Language(int id, String name, TSLanguage tsLanguage, String... extensions) {
        this(id, name, tsLanguage, new HashSet<>());
        this.extensions.addAll(Arrays.asList(extensions));
    }

    public static Language getByTsLanguage(TSLanguage tsLanguage) {
        for (Language language : values()) {
            if (language.tsLanguage == tsLanguage) {
                return language;
            }
        }
        return null;
    }

    public static Language getByFile(File file) {
        for (Language language : values()) {
            if (language.checkFile(file)) {
                return language;
            }
        }
        return null;
    }

    public static boolean containsLanguageId(int languageId) {
        for (Language language : values()) {
            if (language.id == languageId) {
                return true;
            }
        }
        return false;
    }

    public boolean checkFile(File file) {
        if (!file.isFile()) {
            return false;
        }
        String[] nameParts = file.getName().split("\\.");
        if (nameParts.length == 0) {
            return false;
        }
        String extension = nameParts[nameParts.length - 1];
        return extensions.contains(extension);
    }

}
