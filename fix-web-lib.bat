@echo off
chcp 65001 >nul
set DEST=D:\house_order\target\classes\artifacts\house_order_war_exploded\WEB-INF\lib
set SRC=D:\house_order\lib
if not exist "%DEST%" mkdir "%DEST%"
xcopy "%SRC%\*.jar" "%DEST%\" /Y /Q >nul
echo 已复制 %SRC% 下的 jar 到 WEB-INF/lib/
