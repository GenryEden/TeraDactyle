package ru.kernelpunik.teradactyle.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lingala.zip4j.exception.ZipException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.services.IComponentService;
import ru.kernelpunik.teradactyle.services.IComponentStorageService;
import ru.kernelpunik.teradactyle.services.ILibraryService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для ComponentController.
 * Проверяют корректную обработку HTTP-запросов и соответствующие ответы.
 */
@WebMvcTest(ComponentController.class)
public class ComponentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IComponentService componentService;

    @MockBean
    private IComponentStorageService componentStorageService;

    @MockBean
    private ILibraryService fingerprintService;

    private Component testComponent;
    private File testDirectory;
    private File testFile;

    @BeforeEach
    void setUp() throws IOException, ZipException {
        // Инициализация тестовых данных
        testComponent = new Component(1L, "Test Component", "Test Description");
        
        // Создаем временную директорию для тестов с uploadZip
        Path tempDir = Files.createTempDirectory("component-test-dir");
        testDirectory = tempDir.toFile();
        testDirectory.deleteOnExit(); // Удалить директорию после тестов
        
        // Создаем пустой файл внутри директории для имитации распакованного архива
        File sampleFile = new File(testDirectory, "example.java");
        sampleFile.createNewFile();
        sampleFile.deleteOnExit();
        
        // Создаем временный файл для тестов с uploadPlain
        testFile = File.createTempFile("component-test-file", ".tmp");
        testFile.deleteOnExit(); // Удалить файл после тестов

        // Настройка мок-объектов
        when(componentService.getComponent(1L)).thenReturn(testComponent);
        when(componentService.getComponent(999L)).thenReturn(null); // Несуществующий компонент
        when(componentService.addComponent(any(Component.class))).thenAnswer(invocation -> {
            Component component = invocation.getArgument(0);
            if (component.getComponentId() == 0) {
                component.setComponentId(1L);
            }
            return component;
        });

        // Для ZIP файлов возвращаем директорию, для обычных файлов - файл
        when(componentStorageService.storeZip(any())).thenReturn(testDirectory);
        when(componentStorageService.storePlain(any())).thenReturn(testFile);
        
        when(fingerprintService.addLibrary(eq(testComponent), any(File.class))).thenReturn(42L);
    }

    /**
     * Тест успешного добавления нового компонента.
     */
    @Test
    void testAddComponentSuccess() throws Exception {
        Component newComponent = new Component(0L, "New Component", "New Description");

        mockMvc.perform(MockMvcRequestBuilders.post("/v0/api/component/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newComponent)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentId").value(1))
                .andExpect(jsonPath("$.name").value("New Component"))
                .andExpect(jsonPath("$.description").value("New Description"));
    }

    /**
     * Тест добавления компонента с неверными данными - ненулевой ID.
     */
    @Test
    void testAddComponentWithExistingId() throws Exception {
        Component invalidComponent = new Component(42L, "Invalid Component", "Should fail");

        mockMvc.perform(MockMvcRequestBuilders.post("/v0/api/component/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidComponent)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест добавления компонента с неверными данными - отсутствует имя.
     */
    @Test
    void testAddComponentWithNullName() throws Exception {
        Component invalidComponent = new Component(0L, null, "Should fail");

        mockMvc.perform(MockMvcRequestBuilders.post("/v0/api/component/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidComponent)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест успешной загрузки ZIP-файла для существующего компонента.
     */
    @Test
    void testUploadZipSuccess() throws Exception {
        MockMultipartFile zipFile = new MockMultipartFile(
                "file", 
                "test.zip", 
                "application/zip", 
                "test zip content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/v0/api/component/uploadZip")
                .file(zipFile)
                .param("component_id", "1")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("newFingerprints=42")));
    }

    /**
     * Тест загрузки ZIP-файла для несуществующего компонента.
     */
    @Test
    void testUploadZipComponentNotFound() throws Exception {
        MockMultipartFile zipFile = new MockMultipartFile(
                "file", 
                "test.zip", 
                "application/zip", 
                "test zip content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/v0/api/component/uploadZip")
                .file(zipFile)
                .param("component_id", "999")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Component not found"));
    }

    /**
     * Тест ошибки при обработке ZIP-файла.
     */
    @Test
    void testUploadZipProcessingError() throws Exception {
        // Настройка мока на генерацию исключения
        when(componentStorageService.storeZip(any())).thenThrow(new ZipException("Invalid ZIP format"));

        MockMultipartFile zipFile = new MockMultipartFile(
                "file", 
                "invalid.zip", 
                "application/zip", 
                "invalid content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/v0/api/component/uploadZip")
                .file(zipFile)
                .param("component_id", "1")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Uploaded file cannot be unpacked"));
    }

    /**
     * Тест ошибки при сохранении ZIP-файла.
     */
    @Test
    void testUploadZipStorageError() throws Exception {
        // Настройка мока на генерацию исключения
        when(componentStorageService.storeZip(any())).thenThrow(new IOException("Storage error"));

        MockMultipartFile zipFile = new MockMultipartFile(
                "file", 
                "test.zip", 
                "application/zip", 
                "test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/v0/api/component/uploadZip")
                .file(zipFile)
                .param("component_id", "1")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Unable to upload the file"));
    }

    /**
     * Тест ошибки при создании отпечатков.
     */
    @Test
    void testUploadZipFingerprintError() throws Exception {
        // Настройка мока на генерацию исключения
        when(fingerprintService.addLibrary(any(), any())).thenThrow(new IOException("Fingerprint error"));

        MockMultipartFile zipFile = new MockMultipartFile(
                "file", 
                "test.zip", 
                "application/zip", 
                "test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/v0/api/component/uploadZip")
                .file(zipFile)
                .param("component_id", "1")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Unable to process the file"));
    }

    /**
     * Тест успешной загрузки обычного файла для существующего компонента.
     */
    @Test
    void testUploadPlainSuccess() throws Exception {
        MockMultipartFile plainFile = new MockMultipartFile(
                "file", 
                "test.java", 
                "text/plain", 
                "public class Test { }".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/v0/api/component/uploadPlain")
                .file(plainFile)
                .param("component_id", "1")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("newFingerprints=42")));
    }

    /**
     * Тест загрузки обычного файла для несуществующего компонента.
     */
    @Test
    void testUploadPlainComponentNotFound() throws Exception {
        MockMultipartFile plainFile = new MockMultipartFile(
                "file", 
                "test.java", 
                "text/plain", 
                "public class Test { }".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/v0/api/component/uploadPlain")
                .file(plainFile)
                .param("component_id", "999")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Component not found"));
    }

    /**
     * Тест ошибки при сохранении обычного файла.
     */
    @Test
    void testUploadPlainStorageError() throws Exception {
        // Настройка мока на генерацию исключения
        when(componentStorageService.storePlain(any())).thenThrow(new IOException("Storage error"));

        MockMultipartFile plainFile = new MockMultipartFile(
                "file", 
                "test.java", 
                "text/plain", 
                "public class Test { }".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/v0/api/component/uploadPlain")
                .file(plainFile)
                .param("component_id", "1")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Unable to upload file"));
    }

    /**
     * Тест ошибки при создании отпечатков для обычного файла.
     */
    @Test
    void testUploadPlainFingerprintError() throws Exception {
        // Настройка мока на генерацию исключения
        when(fingerprintService.addLibrary(any(), any())).thenThrow(new IOException("Fingerprint error"));

        MockMultipartFile plainFile = new MockMultipartFile(
                "file", 
                "test.java", 
                "text/plain", 
                "public class Test { }".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/v0/api/component/uploadPlain")
                .file(plainFile)
                .param("component_id", "1")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Unable to process the file"));
    }
} 