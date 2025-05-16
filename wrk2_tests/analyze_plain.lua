-- analyze_plain.lua
wrk.method = "POST"
wrk.headers["Content-Type"] = "multipart/form-data; boundary=BOUNDARY"
wrk.headers["User-Agent"] = "insomnia/10.3.1"

-- Тестовое содержимое Java-файла (можно заменить на большее при необходимости)
local file_content = [[
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
    }
}
]]

local file_name = "HelloWorld.java"
local component_id = "1"

wrk.body = "--BOUNDARY\r\n" ..
           "Content-Disposition: form-data; name=\"file\"; filename=\"" .. file_name .. "\"\r\n" ..
           "Content-Type: text/x-java-source\r\n\r\n" ..
           file_content .. "\r\n" ..
           "--BOUNDARY\r\n" ..
           "Content-Disposition: form-data; name=\"component_id\"\r\n\r\n" ..
           component_id .. "\r\n" ..
           "--BOUNDARY--\r\n"

