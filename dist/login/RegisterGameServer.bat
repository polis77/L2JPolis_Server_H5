@echo off
title L2J - Register Game Server
color 17
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;l2jlogin.jar com.polis.tools.gsregistering.BaseGameServerRegister -c
pause