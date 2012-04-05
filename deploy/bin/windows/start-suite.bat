set SUITE_NAME=%1
set INCLUDE=%2
set EXCLUDE=%3

@cd %LOCAL_SGPATH%\bin

set selenium.browser=dummy
if %SUITE_NAME% == webui-Firefox (
	set selenium.browser=Firefox
)
if %SUITE_NAME% == webui-Chrome (
	set selenium.browser=Chrome
)
if %SUITE_NAME% == webui-IE (
	set selenium.browser=IE
)
 
@echo running %selenium.browser% tests...
set SUITE_ID=0
call ant -buildfile %LOCAL_SGPATH%\bin\pre-run.xml
call ant -buildfile %LOCAL_SGPATH%\bin\run.xml testsummary -DBUILD_NUMBER=%BUILD_NUMBER% -DSUITE_NAME=%SUITE_NAME% -DBUILD_DIR=%RUNTIME_BUILD_LOCATION% -DMAJOR_VERSION=%VERSION% -DMINOR_VERSION=%MILESTONE% -DSELENIUM_BROWSER=%selenium.browser% -DSUITE_ID=%SUITE_ID% -DSUITE_NUMBER=1 -DINCLUDE=%INCLUDE% -DEXCLUDE=%EXCLUDE%
call %LOCAL_SGPATH%\deploy\bin\windows\generate-report.bat %BUILD_NUMBER% %SUITE_NAME% %VERSION% %MILESTONE%