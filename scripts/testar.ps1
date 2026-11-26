$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    & "$PSScriptRoot/compilar.ps1"
    javac -encoding UTF-8 -cp bin -d bin testes/Testes.java
    if ($LASTEXITCODE -ne 0) { throw 'Falha na compilacao dos testes' }
    java -cp bin testes.Testes
    if ($LASTEXITCODE -ne 0) { throw 'Teste reprovado' }
} finally { Pop-Location }
