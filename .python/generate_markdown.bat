@echo off
chcp 65001 >nul
pushd "%~dp0"
python generate_markdown.py
popd
pause
