@echo off
title 리니지2 코리아 로그인 서버 ( by police )

:start
echo 리니지2 코리아 로그인 서버를 시작합니다!
echo.

java -Xms128m -Xmx256m -cp ./../libs/*;l2jlogin.jar com.polis.loginserver.L2LoginServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end

:restart
echo.
echo Admin Restarted Login Server.
echo.
goto start

:error
echo.
echo Login Server terminated abnormally!
echo.

:end
echo.
echo Login Server Terminated.
echo.
pause