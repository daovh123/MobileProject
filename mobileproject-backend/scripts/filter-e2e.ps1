param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$results = @()

function Invoke-TestCase {
    param(
        [string]$Name,
        [string]$Url,
        [int]$ExpectedStatus,
        [scriptblock]$ValidateBody
    )

    $statusCode = 0
    $bodyText = ""

    try {
        $response = Invoke-WebRequest -Uri $Url -Method Get -UseBasicParsing
        $statusCode = [int]$response.StatusCode
        $bodyText = $response.Content
    } catch {
        $httpResponse = $_.Exception.Response
        if ($null -eq $httpResponse) {
            throw
        }

        $statusCode = [int]$httpResponse.StatusCode
        $stream = $httpResponse.GetResponseStream()
        if ($null -ne $stream) {
            $reader = New-Object System.IO.StreamReader($stream)
            $bodyText = $reader.ReadToEnd()
            $reader.Close()
        }
    }

    $passed = $statusCode -eq $ExpectedStatus
    $message = ""

    if (-not $passed) {
        $message = "Expected status $ExpectedStatus but got $statusCode"
    } elseif ($null -ne $ValidateBody) {
        try {
            $jsonBody = $null
            if (-not [string]::IsNullOrWhiteSpace($bodyText)) {
                $jsonBody = $bodyText | ConvertFrom-Json
            }
            & $ValidateBody $jsonBody
            $message = "OK"
        } catch {
            $passed = $false
            $message = "Body validation failed: $($_.Exception.Message)"
        }
    } else {
        $message = "OK"
    }

    $script:results += [pscustomobject]@{
        Name = $Name
        Passed = $passed
        Status = $statusCode
        Expected = $ExpectedStatus
        Message = $message
        Url = $Url
    }
}

Invoke-TestCase -Name "default-search" -Url "$BaseUrl/api/places?size=5" -ExpectedStatus 200 -ValidateBody {
    param($json)
    if ($null -eq $json.items) {
        throw "Missing items array"
    }
}

Invoke-TestCase -Name "food-filter" -Url "$BaseUrl/api/places?type=food&size=10" -ExpectedStatus 200 -ValidateBody {
    param($json)
    if ($json.items.Count -gt 0) {
        foreach ($item in $json.items) {
            if (-not $item.food) {
                throw "Found non-food item in food filter"
            }
        }
    }
}

Invoke-TestCase -Name "drink-rating-filter" -Url "$BaseUrl/api/places?type=drink&minRating=4&size=10" -ExpectedStatus 200 -ValidateBody {
    param($json)
    if ($json.items.Count -gt 0) {
        foreach ($item in $json.items) {
            if (-not $item.drink) {
                throw "Found non-drink item in drink filter"
            }
            if ($item.rating -lt 4) {
                throw "Found item below min rating"
            }
        }
    }
}

Invoke-TestCase -Name "province-filter" -Url "$BaseUrl/api/places?province=ha%20noi&size=10" -ExpectedStatus 200 -ValidateBody {
    param($json)
    if ($null -eq $json.items) {
        throw "Missing items array"
    }
}

Invoke-TestCase -Name "combined-filters" -Url "$BaseUrl/api/places?q=pho&province=ha%20noi&type=food&minRating=4&sort=ratingMix&size=20" -ExpectedStatus 200 -ValidateBody {
    param($json)
    if ($null -eq $json.items) {
        throw "Missing items array"
    }

    $baseline = Invoke-RestMethod -Uri "$BaseUrl/api/places?q=pho&type=food&minRating=4&sort=ratingMix&size=20" -Method Get
    if ([int]$json.total -gt [int]$baseline.total) {
        throw "Province filter increased total results unexpectedly"
    }

    foreach ($item in $json.items) {
        if (-not $item.food) {
            throw "Found non-food item in combined filters"
        }
        if ($null -eq $item.rating -or [double]$item.rating -lt 4) {
            throw "Found item below minRating in combined filters"
        }
    }
}

Invoke-TestCase -Name "distance-filter" -Url "$BaseUrl/api/places?sort=distance&nearLat=21.0278&nearLng=105.8342&radiusKm=10&size=10" -ExpectedStatus 200 -ValidateBody {
    param($json)
    if ($json.items.Count -gt 1) {
        $last = -1.0
        foreach ($item in $json.items) {
            if ($item.distanceKm -eq $null) {
                throw "Missing distanceKm with distance sort"
            }
            if ($item.distanceKm -gt 10) {
                throw "Found item outside requested radius"
            }
            if ($last -gt $item.distanceKm) {
                throw "Distance sort is not ascending"
            }
            $last = [double]$item.distanceKm
        }
    }
}

Invoke-TestCase -Name "invalid-distance-without-coordinates" -Url "$BaseUrl/api/places?sort=distance" -ExpectedStatus 400 -ValidateBody $null
Invoke-TestCase -Name "invalid-radius-without-coordinates" -Url "$BaseUrl/api/places?radiusKm=5" -ExpectedStatus 400 -ValidateBody $null

Invoke-TestCase -Name "random-filter" -Url "$BaseUrl/api/places/random?type=food&minRating=4" -ExpectedStatus 200 -ValidateBody {
    param($json)
    if ([string]::IsNullOrWhiteSpace($json.id)) {
        throw "Random endpoint did not return id"
    }
}

Invoke-TestCase -Name "trending-endpoint" -Url "$BaseUrl/api/places/trending?size=5" -ExpectedStatus 200 -ValidateBody {
    param($json)
    if ($null -eq $json.items) {
        throw "Trending response missing items"
    }
}

try {
    $ratingAuditBase = "$BaseUrl/api/places?province=ha%20noi&type=all&sort=ratingMix&page=0&size=30"
    $allResp = Invoke-RestMethod -Uri $ratingAuditBase -Method Get
    $oneResp = Invoke-RestMethod -Uri "$ratingAuditBase&minRating=1" -Method Get
    $twoResp = Invoke-RestMethod -Uri "$ratingAuditBase&minRating=2" -Method Get
    $threeResp = Invoke-RestMethod -Uri "$ratingAuditBase&minRating=3" -Method Get
    $fourResp = Invoke-RestMethod -Uri "$ratingAuditBase&minRating=4" -Method Get
    $fiveResp = Invoke-RestMethod -Uri "$ratingAuditBase&minRating=5" -Method Get

    $totals = @(
        [int]$allResp.total,
        [int]$oneResp.total,
        [int]$twoResp.total,
        [int]$threeResp.total,
        [int]$fourResp.total,
        [int]$fiveResp.total
    )

    $monotonic = $true
    for ($i = 0; $i -lt $totals.Count - 1; $i++) {
        if ($totals[$i] -lt $totals[$i + 1]) {
            $monotonic = $false
            break
        }
    }

    $onePlusCoversAll = ([int]$oneResp.total) -ge ([int]$allResp.total)
    $twoPageFiveStarCount = @($twoResp.items | Where-Object { $_.rating -ge 5.0 }).Count
    $hasFiveInTwoPlusPage = ([int]$fiveResp.total -eq 0) -or ($twoPageFiveStarCount -gt 0)

    $allPageHasBelowFive = $true
    if ([int]$fiveResp.total -lt [int]$allResp.total) {
        $allPageHasBelowFive = @($allResp.items | Where-Object { $_.rating -lt 5.0 -or $_.rating -eq $null }).Count -gt 0
    }

    $auditPassed = $monotonic -and $onePlusCoversAll -and $hasFiveInTwoPlusPage -and $allPageHasBelowFive
    $auditMessage = "totals(all,1+,2+,3+,4+,5+)=" + ($totals -join ",") + "; twoPlusPage5Star=$twoPageFiveStarCount"

    $results += [pscustomobject]@{
        Name = "ha-noi-rating-audit"
        Passed = $auditPassed
        Status = if ($auditPassed) { 200 } else { 500 }
        Expected = 200
        Message = $auditMessage
        Url = $ratingAuditBase
    }
} catch {
    $results += [pscustomobject]@{
        Name = "ha-noi-rating-audit"
        Passed = $false
        Status = 500
        Expected = 200
        Message = "Audit execution failed: $($_.Exception.Message)"
        Url = "$BaseUrl/api/places"
    }
}

$results | Format-Table -AutoSize Name, Passed, Status, Expected, Message

$failed = @($results | Where-Object { -not $_.Passed })
if ($failed.Count -gt 0) {
    Write-Error "E2E filter suite failed with $($failed.Count) case(s)."
    exit 1
}

Write-Host "All E2E filter cases passed." -ForegroundColor Green
exit 0
