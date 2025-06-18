package ru.kernelpunik.teradactyle.services;

import ru.kernelpunik.teradactyle.models.Language;

import java.util.concurrent.ExecutionException;

public interface IComparerService {
    double compareSolutions(String input, String reference, Language language) throws ExecutionException, InterruptedException;
}
