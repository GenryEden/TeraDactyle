package ru.kernelpunik.teradactyle.services;

import ru.kernelpunik.teradactyle.models.Component;

import java.io.File;
import java.io.IOException;

public interface IFingerprintService {
    long addFingerprints(Component component, File fileTree) throws IOException;
}
