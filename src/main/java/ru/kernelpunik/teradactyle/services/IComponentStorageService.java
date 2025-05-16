package ru.kernelpunik.teradactyle.services;

import net.lingala.zip4j.exception.ZipException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface IComponentStorageService {
    File storeZip(MultipartFile file) throws IOException, ZipException;
    File storePlain(MultipartFile file) throws IOException;
}
