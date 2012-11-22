@rem This script is used to run our Selenium Web-UI tests on a windows machine.
@rem it uses the existing build.xml and run.xml of SGTest with configured run.properties.
@title Executing Selenium tests
@echo on

set VERSION=%1
set MILESTONE=%2
set BUILD_NUMBER=%3
set BUILD_VERSION=%4
set SGTEST_CHECKOUT_FOLDER=%5
set SUITE_NAME=%6
set INCLUDE=%7
set EXCLUDE=%8
set BUILD_LOG_URL=%9

shift
shift
shift
set BRANCH_NAME=%7
set SVN_BRANCH_DIRECTORY=%8
set EC2_REGION=%9

@echo setting up enviroment variables
call set-build-env.bat

set SGTEST_HOME=%LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER%\SGTest

@echo cleaning sgtest...
@if exist %LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER% rmdir %LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER% /s /q

@echo retrieving build from tarzan...
@mkdir %LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER%
xcopy %REMOTE_BUILD_DIR%\cloudify\1.5\gigaspaces-cloudify-%VERSION%-%MILESTONE%-b%BUILD_VERSION%.zip %LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER%
@echo extracting build file to local-builds folder
7z x %LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER%\gigaspaces-cloudify-%VERSION%-%MILESTONE%-b%BUILD_VERSION%.zip -o%LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER%
@del %LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER%\gigaspaces-cloudify-%VERSION%-%MILESTONE%-b%BUILD_VERSION%.zip

@echo exporting SGTest
@if %BRANCH_NAME%==trunk (
	set SVN_SGTEST_REPOSITORY=svn://svn-srv/SVN/cloudify/trunk/quality/frameworks/SGTest
) else ( 
	set SVN_SGTEST_REPOSITORY=svn://svn-srv/SVN/cloudify/branches/%SVN_BRANCH_DIRECTORY%/%BRANCH_NAME%/quality/frameworks/SGTest
)

@mkdir %WEBUI_TMP_DIR%
svn export --force %SVN_SGTEST_REPOSITORY% %SGTEST_HOME% 
 
@call %SGTEST_HOME%\src\main\scripts\deploy\bin\windows\set-deploy-env.bat

@echo updating webuitf...
@call %SGTEST_HOME%\src\main\scripts\deploy\bin\windows\deploy_webuitf.bat

@echo Running Suite %SUITE_NAME%:  
@call %SGTEST_HOME%\src\main\scripts\deploy\bin\windows\start-suite.bat %SUITE_NAME% %INCLUDE% %EXCLUDE% %EC2_REGION%

if %selenium.browser% == Chrome (
	taskkill /im chromedriver.exe /F
	taskkill /im chrome.exe /F
)

if %selenium.browser% == Firefox (
	taskkill /im firefox.exe /F
)

@echo tranferring reports to tgrid
echo %LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER%
xcopy %BUILD_LOCATION% Z:\%BUILD_NUMBER% /s /i /y
xcopy %LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER%\%SUITE_NAME% Z:\%BUILD_NUMBER%\%SUITE_NAME% /s /i /y

@echo cleaning local build folder
rmdir %LOCAL_SGPATH%\deploy\local-builds\%BUILD_NUMBER% /s /q
call %SGTEST_HOME%\src\main\scripts\deploy\bin\windows\delete_build.bat %VERSION% %MILESTONE% %USER_HOME%
