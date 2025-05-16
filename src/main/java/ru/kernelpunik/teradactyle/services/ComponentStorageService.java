package ru.kernelpunik.teradactyle.services;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;


@Service
public class ComponentStorageService implements IComponentStorageService {
    private static final String FILE_EXTENSION = ".zip";
    private final File toUpload = new File("archives");
    private final File toUnpack = new File("components");
    private final File toStorePlain = new File("plain");

    @Override
    public File storeZip(MultipartFile file) throws IOException, ZipException {
        if (!toUpload.exists()) {
            toUpload.mkdirs();
        }
        UUID uuid = upload(file);
        try {
            return unzip(uuid);
        } catch (ZipException e) {
            FileUtils.deleteDirectory(resolveToUnpack(uuid));
            throw e;
        } finally {
            resolveToUpload(uuid).delete();
        }
    }

   @Override
    public File storePlain(MultipartFile file) throws IOException {
        if (!toStorePlain.exists()) {
            toStorePlain.mkdirs();
        }
        return uploadPlain(file);
    }





    private UUID upload(MultipartFile file) throws IOException {
        UUID uuid = UUID.randomUUID();
        File destination = resolveToUpload(uuid);
        file.transferTo(destination.getAbsoluteFile());
        return uuid;
    }

    private File uploadPlain(MultipartFile file) throws IOException {
        UUID uuid = UUID.randomUUID();
        String[] partnames = file.getOriginalFilename().split("\\.");
        File destination = new File(toStorePlain, uuid + "." + partnames[partnames.length - 1]).getAbsoluteFile();
        file.transferTo(destination);
        return destination;
    }

    private File unzip(UUID uuid) throws ZipException {
        File source = resolveToUpload(uuid);
        File destination = resolveToUnpack(uuid);
        destination.mkdirs();
        ZipFile zipFile = new ZipFile(source);
        zipFile.extractAll(destination.getAbsolutePath());
        return destination;
    }

    private File resolveToUpload(UUID uuid) {
        return new File(toUpload, uuid + FILE_EXTENSION);
    }

    private File resolveToUnpack(UUID uuid) {
        return new File(toUnpack, uuid.toString());
    }
}
