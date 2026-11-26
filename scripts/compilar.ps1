$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    New-Item -ItemType Directory -Force bin | Out-Null
    $fontes = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' }
    [IO.File]::WriteAllLines((Join-Path (Get-Location) 'fontes.txt'), $fontes, [Text.UTF8Encoding]::new($false))
    javac -encoding UTF-8 --release 8 -Xlint:-options -d bin '@fontes.txt'
    if ($LASTEXITCODE -ne 0) { throw 'Falha na compilacao' }
} finally { Pop-Location }
