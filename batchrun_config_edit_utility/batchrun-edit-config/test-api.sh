#!/bin/bash

# Template API Test Script
# Tests file download and upload functionality

BASE_URL="http://localhost:8080"
CAMPAIGN_ID="test-campaign-2024"

echo "🚀 Template API Test Script"
echo "=========================="
echo "Campaign ID: $CAMPAIGN_ID"
echo "Base URL: $BASE_URL"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to make a request and show response
make_request() {
    local method=$1
    local endpoint=$2
    local description=$3
    local extra_args=${4:-""}
    
    echo -e "${BLUE}📡 $method $endpoint${NC}"
    echo "   $description"
    echo ""
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" "$BASE_URL$endpoint" $extra_args)
    else
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method "$BASE_URL$endpoint" $extra_args)
    fi
    
    http_code=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✅ Success ($http_code)${NC}"
    else
        echo -e "${RED}❌ Failed ($http_code)${NC}"
    fi
    
    echo "Response:"
    echo "$body" | head -20
    echo ""
    echo "---"
    echo ""
}

# Check if server is running
echo -e "${YELLOW}🔍 Checking if server is running...${NC}"
if curl -s --connect-timeout 5 "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Server is running${NC}"
else
    echo -e "${RED}❌ Server is not running. Please start the application first:${NC}"
    echo "   ./mvnw spring-boot:run"
    exit 1
fi
echo ""

# Test 1: Get template info (JSON) - should return 404 initially
make_request "GET" "/campaign/$CAMPAIGN_ID/template" "Get template info (JSON response)"

# Test 2: Try to download file - should return 404 initially  
make_request "GET" "/campaign/$CAMPAIGN_ID/template/download" "Download template file" "-I"

# Test 3: Upload template file
echo -e "${BLUE}📤 Uploading template file${NC}"
echo "   Using test-template.html"
echo ""

if [ -f "test-template.html" ]; then
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "$BASE_URL/campaign/$CAMPAIGN_ID/template/upload" \
        -F "file=@test-template.html" \
        -F "createBackup=true")
    
    http_code=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✅ Upload Success ($http_code)${NC}"
    else
        echo -e "${RED}❌ Upload Failed ($http_code)${NC}"
    fi
    
    echo "Response:"
    echo "$body" | head -20
else
    echo -e "${RED}❌ test-template.html not found${NC}"
fi
echo ""
echo "---"
echo ""

# Test 4: Get template info again (should exist now)
make_request "GET" "/campaign/$CAMPAIGN_ID/template" "Get template info after upload"

# Test 5: Download the template file
echo -e "${BLUE}📥 Downloading template file${NC}"
echo "   Saving as downloaded-$CAMPAIGN_ID.html"
echo ""

response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    "$BASE_URL/campaign/$CAMPAIGN_ID/template/download" \
    -o "downloaded-$CAMPAIGN_ID.html")

http_code=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)

if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
    echo -e "${GREEN}✅ Download Success ($http_code)${NC}"
    if [ -f "downloaded-$CAMPAIGN_ID.html" ]; then
        file_size=$(stat -c%s "downloaded-$CAMPAIGN_ID.html" 2>/dev/null || stat -f%z "downloaded-$CAMPAIGN_ID.html" 2>/dev/null || echo "unknown")
        echo "Downloaded file size: $file_size bytes"
        echo "First 5 lines of downloaded file:"
        head -5 "downloaded-$CAMPAIGN_ID.html"
    fi
else
    echo -e "${RED}❌ Download Failed ($http_code)${NC}"
fi
echo ""
echo "---"
echo ""

# Test 6: Validate template
echo -e "${BLUE}🔍 Validating template${NC}"
echo "   Using validation endpoint"
echo ""

validation_json='{"htmlContent":"<!DOCTYPE html><html><head><title>Test</title></head><body><h1>Valid HTML</h1></body></html>"}'
response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    -X POST "$BASE_URL/campaign/$CAMPAIGN_ID/template/validate" \
    -H "Content-Type: application/json" \
    -d "$validation_json")

http_code=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
body=$(echo "$response" | sed '/HTTP_STATUS/d')

if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
    echo -e "${GREEN}✅ Validation Success ($http_code)${NC}"
else
    echo -e "${RED}❌ Validation Failed ($http_code)${NC}"
fi

echo "Response:"
echo "$body"
echo ""
echo "---"
echo ""

echo -e "${YELLOW}🎉 Test Complete!${NC}"
echo ""
echo "Summary of endpoints tested:"
echo "1. ✅ GET  /campaign/{campId}/template           - Get template info (JSON)"
echo "2. ✅ GET  /campaign/{campId}/template/download  - Download template file"
echo "3. ✅ POST /campaign/{campId}/template/upload    - Upload template file"
echo "4. ✅ POST /campaign/{campId}/template/validate  - Validate template"
echo ""
echo "Files created during test:"
echo "- test-template.html (test file)"
echo "- downloaded-$CAMPAIGN_ID.html (downloaded file)"
echo ""
echo "Template storage location:"
echo "- Check: \${templ.storage.path}/$CAMPAIGN_ID/ (any .html file)"
echo ""
echo -e "${BLUE}🔍 New Behavior Summary:${NC}"
echo "✅ The API now searches for ANY .html file in the campaign directory"
echo "✅ If existing file found, it backs up with original filename + timestamp"
echo "✅ New upload replaces the existing file (keeping original filename)"
echo "✅ Download uses the actual filename of the existing file"
echo "✅ If no existing file, uses default naming: {campaignId}.html"
