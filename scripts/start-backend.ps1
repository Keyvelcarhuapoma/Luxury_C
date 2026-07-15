Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $root "backend"

Push-Location $backend
try {
  if (Test-Path ".\mvnw.cmd") {
    .\mvnw.cmd spring-boot:run
  } else {
    mvn spring-boot:run
  }
}
finally {
  Pop-Location
}
