package ru.kernelpunik.teradactyle.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterCpp;
import org.treesitter.TreeSitterJava;
import org.treesitter.TreeSitterPython;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Language {
    PYTHON(0, "Python", new TreeSitterPython()),
    JAVA(1, "Java", new TreeSitterJava()),
    CPP(2, "C++", new TreeSitterCpp());
    public final int id;
    public final String name;
    @JsonIgnore
    public final TSLanguage tsLanguage;
    Language(int id, String name, TSLanguage tsLanguage) {
        this.id = id;
        this.name = name;
        this.tsLanguage = tsLanguage;
    }
}
