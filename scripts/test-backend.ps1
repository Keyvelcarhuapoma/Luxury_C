Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $root "backend"

Push-Location $backend
try {
  if (Test-Path ".\mvnw.cmd") {
    .\mvnw.cmd test
  } else {
    mvn test
  }
}
finally {
  Pop-Location
}
