@echo off

setlocal EnableDelayedExpansion

runjava ss.udapi.sdk.examples.StreamingProgram log4j.properties endpoint.properties sdk.properties  

endlocal