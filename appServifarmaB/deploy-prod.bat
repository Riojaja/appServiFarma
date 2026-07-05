@echo off
echo ============================================
echo DESPLIEGUE PRODUCCION - Botica ServiFarma
echo ============================================
echo.

REM Verificar que Java 21 esté instalado
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Java 21 no encontrado en PATH
    echo Instala Java 21 desde: https://www.oracle.com/java/technologies/downloads/#java21
    exit /b 1
)

REM Verificar versión de Java
java -version 2>&1 | findstr /C:"version" | findstr /C:"21" >nul
if %errorlevel% neq 0 (
    echo ERROR: Se requiere Java 21. Versión actual diferente
    echo Actualiza Java a la versión 21
    exit /b 1
)

echo ✓ Java 21 verificado

REM Verificar que Maven esté disponible o usar wrapper
if exist mvnw.cmd (
    echo Usando Maven Wrapper...
    set MAVEN_CMD=mvnw.cmd
) else (
    where mvn >nul 2>nul
    if %errorlevel% neq 0 (
        echo ERROR: Maven no encontrado
        echo Instala Maven o usa el wrapper (mvnw.cmd)
        exit /b 1
    )
    echo Usando Maven instalado...
    set MAVEN_CMD=mvn
)

echo ✓ Maven verificado

REM Solicitar variables de entorno para producción
echo.
echo ============================================
echo CONFIGURACION DE VARIABLES DE ENTORNO
echo ============================================
set /p DB_USERNAME=Usuario de MySQL (ej: servifarma_user): 
set /p DB_PASSWORD=Contraseña de MySQL: 
set /p JWT_SECRET=Secreto JWT (minimo 32 caracteres): 

REM Verificar longitud de JWT_SECRET
if "%JWT_SECRET%"=="" (
    echo ERROR: El secreto JWT no puede estar vacío
    exit /b 1
)

echo %JWT_SECRET% | findstr /r "^.\{32,\}$" >nul
if %errorlevel% neq 0 (
    echo ERROR: El secreto JWT debe tener al menos 32 caracteres
    exit /b 1
)

REM Limpiar y compilar
echo.
echo ============================================
echo COMPILANDO PROYECTO...
echo ============================================
call %MAVEN_CMD% clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Compilación fallida
    exit /b 1
)

echo ✓ Compilación exitosa

REM Crear directorio de despliegue
if not exist deploy mkdir deploy
if not exist deploy\logs mkdir deploy\logs

REM Copiar archivos necesarios
copy target\appServifarmaB-*.jar deploy\
copy src\main\resources\application-prod.properties deploy\application-prod.properties

REM Crear script de ejecución para producción
echo @echo off > deploy\run-prod.bat
echo set DB_USERNAME=%DB_USERNAME% >> deploy\run-prod.bat
echo set DB_PASSWORD=%DB_PASSWORD% >> deploy\run-prod.bat
echo set JWT_SECRET=%JWT_SECRET% >> deploy\run-prod.bat
echo set JAVA_OPTS=-Xmx1024m -Xms512m -server -XX:+UseG1GC -XX:+UseStringDeduplication >> deploy\run-prod.bat
echo. >> deploy\run-prod.bat
echo echo Iniciando Botica ServiFarma en modo producción... >> deploy\run-prod.bat
echo java %JAVA_OPTS% -jar appServifarmaB-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod >> deploy\run-prod.bat

REM Crear script de respaldo de base de datos
echo @echo off > deploy\backup-db.bat
echo echo Realizando respaldo de base de datos... >> deploy\backup-db.bat
echo set BACKUP_DIR=backups\db >> deploy\backup-db.bat
echo if not exist %%BACKUP_DIR%% mkdir %%BACKUP_DIR%% >> deploy\backup-db.bat
echo mysqldump -u%DB_USERNAME% -p%DB_PASSWORD% servifarmaBD > %%BACKUP_DIR%%\servifarmaBD_%%date:~-4,4%%-%%date:~-7,2%%-%%date:~-10,2%%_%%time:~0,2%%-%%time:~3,2%%.sql >> deploy\backup-db.bat

REM Crear archivo de configuración .env para producción
echo DB_USERNAME=%DB_USERNAME% > deploy\.env.prod
echo DB_PASSWORD=%DB_PASSWORD% >> deploy\.env.prod
echo JWT_SECRET=%JWT_SECRET% >> deploy\.env.prod
echo DB_HOST=localhost >> deploy\.env.prod
echo DB_PORT=3306 >> deploy\.env.prod
echo DB_NAME=servifarmaBD >> deploy\.env.prod

echo.
echo ============================================
echo DESPLIEGUE COMPLETADO EXITOSAMENTE!
echo ============================================
echo.
echo Archivos generados en la carpeta 'deploy':
echo  ✓ appServifarmaB-0.0.1-SNAPSHOT.jar
echo  ✓ application-prod.properties
echo  ✓ run-prod.bat        (ejecutar aplicación)
echo  ✓ backup-db.bat       (respaldo de BD)
echo  ✓ .env.prod           (variables de entorno)
echo.
echo Para ejecutar en producción:
echo   1. cd deploy
echo   2. run-prod.bat
echo.
echo Verificación de instalación:
echo   - Acceder a: http://localhost:8080
echo   - Documentación: http://localhost:8080/swagger-ui.html
echo   - Usuario demo: admin / admin123
echo.
echo ============================================
echo IMPORTANTE PARA PRODUCCION REAL:
echo ============================================
echo 1. Configurar certificado SSL
echo 2. Configurar firewall
echo 3. Configurar backup automático
echo 4. Monitorear logs regularmente
echo 5. Actualizar contraseñas por defecto
echo ============================================

pause