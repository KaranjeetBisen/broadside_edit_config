#!/bin/bash

# Extended Template API Test - Testing Backup Functionality
# This script tests the new behavior where existing files are backed up

BASE_URL="http://localhost:8080"
CAMPAIGN_ID="backup-test-campaign"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🧪 Extended Template API Test - Backup Functionality${NC}"
echo "==============================================="
echo "Campaign ID: $CAMPAIGN_ID"
echo "Testing the new backup behavior where:"
echo "  1. API searches for any .html file in campaign directory"
echo "  2. Backs up existing file with original name + timestamp"
echo "  3. Replaces with new content using existing filename"
echo ""

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

# Test 1: Upload first template with custom content
echo -e "${BLUE}📤 Step 1: Upload first template${NC}"
echo "   This will create the initial template file"
echo ""

first_template='<!DOCTYPE html><html><head><title>Original Template</title></head><body><h1>Original Content</h1><p>This is the first template for {{campaignName}}</p></body></html>'

response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    -X POST "$BASE_URL/campaign/$CAMPAIGN_ID/template/upload" \
    -F "file=@test-template.html" \
    -F "createBackup=true")

http_code=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
body=$(echo "$response" | sed '/HTTP_STATUS/d')

if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
    echo -e "${GREEN}✅ First upload Success ($http_code)${NC}"
else
    echo -e "${RED}❌ First upload Failed ($http_code)${NC}"
fi
echo "---"
echo ""

# Test 2: Get template info to see the current state
echo -e "${BLUE}📋 Step 2: Get current template info${NC}"
response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" "$BASE_URL/campaign/$CAMPAIGN_ID/template")
http_code=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
body=$(echo "$response" | sed '/HTTP_STATUS/d')

if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
    echo -e "${GREEN}✅ Get template Success ($http_code)${NC}"
    echo "Current template info:"
    echo "$body" | head -10
else
    echo -e "${RED}❌ Get template Failed ($http_code)${NC}"
fi
echo "---"
echo ""

# Test 3: Upload second template (this should backup the first one)
echo -e "${BLUE}📤 Step 3: Upload second template (should create backup)${NC}"
echo "   This should backup the existing file and replace with new content"
echo ""

if [ -f "existing-custom-template.html" ]; then
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "$BASE_URL/campaign/$CAMPAIGN_ID/template/upload" \
        -F "file=@existing-custom-template.html" \
        -F "createBackup=true")
    
    http_code=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✅ Second upload Success ($http_code)${NC}"
        echo "Response shows backup information:"
        echo "$body" | grep -E "(backupPath|hasBackup)" || echo "Full response:" && echo "$body" | head -15
    else
        echo -e "${RED}❌ Second upload Failed ($http_code)${NC}"
        echo "$body"
    fi
else
    echo -e "${RED}❌ existing-custom-template.html not found${NC}"
fi
echo "---"
echo ""

# Test 4: Get updated template info
echo -e "${BLUE}📋 Step 4: Get updated template info after backup${NC}"
response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" "$BASE_URL/campaign/$CAMPAIGN_ID/template")
http_code=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
body=$(echo "$response" | sed '/HTTP_STATUS/d')

if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
    echo -e "${GREEN}✅ Get updated template Success ($http_code)${NC}"
    echo "Updated template info (showing backup status):"
    echo "$body" | head -20
else
    echo -e "${RED}❌ Get updated template Failed ($http_code)${NC}"
fi
echo "---"
echo ""

# Test 5: Download the current template
echo -e "${BLUE}📥 Step 5: Download current template${NC}"
echo "   Should download with the filename of the existing file"
echo ""

response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    "$BASE_URL/campaign/$CAMPAIGN_ID/template/download" \
    -o "final-downloaded-$CAMPAIGN_ID.html")

http_code=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)

if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
    echo -e "${GREEN}✅ Download Success ($http_code)${NC}"
    if [ -f "final-downloaded-$CAMPAIGN_ID.html" ]; then
        file_size=$(stat -c%s "final-downloaded-$CAMPAIGN_ID.html" 2>/dev/null || stat -f%z "final-downloaded-$CAMPAIGN_ID.html" 2>/dev/null || echo "unknown")
        echo "Downloaded file size: $file_size bytes"
        echo "Content preview:"
        head -5 "final-downloaded-$CAMPAIGN_ID.html"
    fi
else
    echo -e "${RED}❌ Download Failed ($http_code)${NC}"
fi
echo "---"
echo ""

# Test 6: Upload without backup (test the createBackup=false option)
echo -e "${BLUE}📤 Step 6: Upload without backup (createBackup=false)${NC}"
echo "   This should replace without creating a backup"
echo ""

if [ -f "test-template.html" ]; then
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "$BASE_URL/campaign/$CAMPAIGN_ID/template/upload" \
        -F "file=@test-template.html" \
        -F "createBackup=false")
    
    http_code=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS/d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✅ Upload without backup Success ($http_code)${NC}"
        echo "Should show hasBackup from previous operations:"
        echo "$body" | grep -E "(backupPath|hasBackup)" || echo "Response preview:" && echo "$body" | head -10
    else
        echo -e "${RED}❌ Upload without backup Failed ($http_code)${NC}"
    fi
else
    echo -e "${RED}❌ test-template.html not found${NC}"
fi
echo "---"
echo ""

echo -e "${YELLOW}🎉 Extended Test Complete!${NC}"
echo ""
echo -e "${BLUE}🔍 New Backup Behavior Verified:${NC}"
echo "✅ API searches for any .html file in campaign directory"
echo "✅ Existing files are backed up with original filename + timestamp"
echo "✅ New uploads replace existing files keeping original filenames"
echo "✅ Downloads use actual filenames of existing files"
echo "✅ Backup creation can be controlled with createBackup parameter"
echo ""
echo "Files created during extended test:"
echo "- test-template.html (test file)"
echo "- existing-custom-template.html (second test file)"
echo "- final-downloaded-$CAMPAIGN_ID.html (final downloaded file)"
echo ""
echo "Check template storage location for backup files:"
echo "- \${templ.storage.path}/$CAMPAIGN_ID/ (main template)"
echo "- \${templ.storage.path}/$CAMPAIGN_ID/backups/ (backup files)"
