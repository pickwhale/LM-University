@echo off
cd /d "%~dp0apps\portal-web"
call F:\nodejs\npm.cmd run dev -- --host 0.0.0.0 --port 5174 > vite-dev.out.log 2> vite-dev.err.log
