-- upload_plain.lua
wrk.method = "PATCH"
wrk.headers["Content-Type"] = "multipart/form-data; boundary=BOUNDARY"

-- Замените на корректный ID компонента
local component_id = "1"

-- Тело исходного файла для теста — простой Java-код
local file_content = [[
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
    }
}
]]

-- Имя загружаемого файла
local file_name = "HelloWorld.java"

wrk.body = "--BOUNDARY\r\n" ..
           "Content-Disposition: form-data; name=\"file\"; filename=\"" .. file_name .. "\"\r\n" ..
           "Content-Type: text/plain\r\n\r\n" ..
           file_content .. "\r\n" ..
           "--BOUNDARY\r\n" ..
           "Content-Disposition: form-data; name=\"component_id\"\r\n\r\n" ..
           component_id .. "\r\n" ..
           "--BOUNDARY--\r\n"

