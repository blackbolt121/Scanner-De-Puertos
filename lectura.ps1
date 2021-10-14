param(
    [Parameter(Mandatory = $true)]
    [System.Collections.ArrayList]$Array,
    [string]$state="Listen",
    [Parameter(Mandatory = $true)]
    [string]$Ip = 0.0.0.0
)
$x = Get-NetTCPConnection -State $state
$x | ForEach-Object {
    if(($_.LocalAddress -eq "0.0.0.0") -or ($_.LocalAddress -eq $ip)){
        $process = $_.OwningProcess
        $port = $_.LocalPort
        if($Array -contains $port){
            $Process = Get-Process -Id $process
            $ProcessName = $Process.Name
            Write-Host "$ProcessName is using port: $port"
        }
    } 
}