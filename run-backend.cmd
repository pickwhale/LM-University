@echo off
set "DB_URL=jdbc:mysql://127.0.0.1:3306/university?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
set "DB_USERNAME=root"
set "DB_PASSWORD=123456"
set "SERVER_PORT=8081"
cd /d "%~dp0"
call .\mvnw.cmd -pl backend spring-boot:run > backend-current-8081.out.log 2> backend-current-8081.err.log
