@REM ----------------------------------------------------------------------------
@REM  Copyright 2001-2006 The Apache Software Foundation.
@REM
@REM  Licensed under the Apache License, Version 2.0 (the "License");
@REM  you may not use this file except in compliance with the License.
@REM  You may obtain a copy of the License at
@REM
@REM       http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.
@REM ----------------------------------------------------------------------------
@REM
@REM   Copyright (c) 2001-2006 The Apache Software Foundation.  All rights
@REM   reserved.

@echo off

set ERROR_CODE=0

:init
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set CMD_LINE_ARGS=%*
goto WinNTGetScriptDir

@REM The 4NT Shell from jp software
:4NTArgs
set CMD_LINE_ARGS=%$
goto WinNTGetScriptDir

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto Win9xGetScriptDir
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto Win9xApp

:Win9xGetScriptDir
set SAVEDIR=%CD%
%0\
cd %0\..\.. 
set BASEDIR=%CD%
cd %SAVEDIR%
set SAVE_DIR=
goto repoSetup

:WinNTGetScriptDir
set BASEDIR=%~dp0\..

:repoSetup
set REPO=


if "%JAVACMD%"=="" set JAVACMD=java

if "%REPO%"=="" set REPO=%BASEDIR%\repo

set CLASSPATH="%BASEDIR%"\etc;"%REPO%"\com\google\cloud\google-cloud-storage\1.27.0\google-cloud-storage-1.27.0.jar;"%REPO%"\com\google\cloud\google-cloud-core\1.27.0\google-cloud-core-1.27.0.jar;"%REPO%"\com\google\guava\guava\20.0\guava-20.0.jar;"%REPO%"\joda-time\joda-time\2.9.2\joda-time-2.9.2.jar;"%REPO%"\com\google\http-client\google-http-client\1.23.0\google-http-client-1.23.0.jar;"%REPO%"\org\apache\httpcomponents\httpclient\4.0.1\httpclient-4.0.1.jar;"%REPO%"\org\apache\httpcomponents\httpcore\4.0.1\httpcore-4.0.1.jar;"%REPO%"\commons-logging\commons-logging\1.1.1\commons-logging-1.1.1.jar;"%REPO%"\commons-codec\commons-codec\1.3\commons-codec-1.3.jar;"%REPO%"\com\google\code\findbugs\jsr305\3.0.1\jsr305-3.0.1.jar;"%REPO%"\com\google\api\api-common\1.5.0\api-common-1.5.0.jar;"%REPO%"\com\google\api\gax\1.23.0\gax-1.23.0.jar;"%REPO%"\org\threeten\threetenbp\1.3.3\threetenbp-1.3.3.jar;"%REPO%"\com\google\protobuf\protobuf-java-util\3.5.1\protobuf-java-util-3.5.1.jar;"%REPO%"\com\google\api\grpc\proto-google-common-protos\1.9.0\proto-google-common-protos-1.9.0.jar;"%REPO%"\com\google\api\grpc\proto-google-iam-v1\0.10.0\proto-google-iam-v1-0.10.0.jar;"%REPO%"\com\google\cloud\google-cloud-core-http\1.27.0\google-cloud-core-http-1.27.0.jar;"%REPO%"\com\google\auth\google-auth-library-credentials\0.9.0\google-auth-library-credentials-0.9.0.jar;"%REPO%"\com\google\auth\google-auth-library-oauth2-http\0.9.0\google-auth-library-oauth2-http-0.9.0.jar;"%REPO%"\com\google\oauth-client\google-oauth-client\1.23.0\google-oauth-client-1.23.0.jar;"%REPO%"\com\google\api-client\google-api-client\1.23.0\google-api-client-1.23.0.jar;"%REPO%"\com\google\http-client\google-http-client-appengine\1.23.0\google-http-client-appengine-1.23.0.jar;"%REPO%"\com\google\http-client\google-http-client-jackson\1.23.0\google-http-client-jackson-1.23.0.jar;"%REPO%"\org\codehaus\jackson\jackson-core-asl\1.9.11\jackson-core-asl-1.9.11.jar;"%REPO%"\com\google\http-client\google-http-client-jackson2\1.23.0\google-http-client-jackson2-1.23.0.jar;"%REPO%"\com\fasterxml\jackson\core\jackson-core\2.1.3\jackson-core-2.1.3.jar;"%REPO%"\com\google\api\gax-httpjson\0.40.0\gax-httpjson-0.40.0.jar;"%REPO%"\io\opencensus\opencensus-api\0.11.1\opencensus-api-0.11.1.jar;"%REPO%"\io\opencensus\opencensus-contrib-http-util\0.11.1\opencensus-contrib-http-util-0.11.1.jar;"%REPO%"\com\google\apis\google-api-services-storage\v1-rev114-1.23.0\google-api-services-storage-v1-rev114-1.23.0.jar;"%REPO%"\com\google\cloud\google-cloud-datastore\1.27.0\google-cloud-datastore-1.27.0.jar;"%REPO%"\com\google\api\grpc\proto-google-cloud-datastore-v1\0.10.0\proto-google-cloud-datastore-v1-0.10.0.jar;"%REPO%"\com\google\protobuf\protobuf-java\3.5.1\protobuf-java-3.5.1.jar;"%REPO%"\com\google\cloud\datastore\datastore-v1-proto-client\1.6.0\datastore-v1-proto-client-1.6.0.jar;"%REPO%"\com\google\http-client\google-http-client-protobuf\1.20.0\google-http-client-protobuf-1.20.0.jar;"%REPO%"\io\grpc\grpc-core\1.10.1\grpc-core-1.10.1.jar;"%REPO%"\io\grpc\grpc-context\1.10.1\grpc-context-1.10.1.jar;"%REPO%"\com\google\code\gson\gson\2.7\gson-2.7.jar;"%REPO%"\com\google\errorprone\error_prone_annotations\2.1.2\error_prone_annotations-2.1.2.jar;"%REPO%"\io\opencensus\opencensus-contrib-grpc-metrics\0.11.0\opencensus-contrib-grpc-metrics-0.11.0.jar;"%REPO%"\project\id2210\project_id2210\0.0.1-SNAPSHOT\project_id2210-0.0.1-SNAPSHOT.jar

set ENDORSED_DIR=
if NOT "%ENDORSED_DIR%" == "" set CLASSPATH="%BASEDIR%"\%ENDORSED_DIR%\*;%CLASSPATH%

if NOT "%CLASSPATH_PREFIX%" == "" set CLASSPATH=%CLASSPATH_PREFIX%;%CLASSPATH%

@REM Reaching here means variables are defined and arguments have been captured
:endInit

%JAVACMD% %JAVA_OPTS%  -classpath %CLASSPATH% -Dapp.name="MyStorage" -Dapp.repo="%REPO%" -Dapp.home="%BASEDIR%" -Dbasedir="%BASEDIR%" project.id2210.project_id2210.MyStorage %CMD_LINE_ARGS%
if %ERRORLEVEL% NEQ 0 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
set ERROR_CODE=%ERRORLEVEL%

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set CMD_LINE_ARGS=
goto postExec

:endNT
@REM If error code is set to 1 then the endlocal was done already in :error.
if %ERROR_CODE% EQU 0 @endlocal


:postExec

if "%FORCE_EXIT_ON_ERROR%" == "on" (
  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
)

exit /B %ERROR_CODE%
