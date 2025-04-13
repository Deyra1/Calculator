@echo off
echo 开始向GitHub推送代码...
echo.

REM 提示用户输入GitHub凭据
set /p username=输入GitHub用户名: 
set /p token=输入GitHub个人访问令牌: 

REM 设置带凭据的远程URL
echo 配置远程仓库...
git remote set-url origin https://%username%:%token%@github.com/RiiiN35/Calculator.git

REM 尝试推送
echo 正在推送代码到GitHub...
git push -u origin main

echo.
IF %ERRORLEVEL% EQU 0 (
    echo 推送成功！请访问 https://github.com/RiiiN35/Calculator 查看您的代码。
) ELSE (
    echo 推送失败。错误代码: %ERRORLEVEL%
    echo 请检查您的用户名和令牌是否正确，或者网络连接是否正常。
)

REM 移除带有凭据的远程URL（安全起见）
git remote set-url origin https://github.com/RiiiN35/Calculator.git

echo.
echo 按任意键退出...
pause > nul 