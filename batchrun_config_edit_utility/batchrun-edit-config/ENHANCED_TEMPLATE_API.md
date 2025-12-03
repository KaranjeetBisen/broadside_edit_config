# Enhanced Template API - Intelligent File Management

## 🚀 New Enhanced Behavior

The Template API has been enhanced with intelligent file management that:

1. **🔍 Searches for existing HTML files** - Finds any `.html` file in the campaign directory
2. **📁 Preserves original filenames** - Keeps existing file names when updating
3. **🔒 Creates intelligent backups** - Backs up with original filename + timestamp
4. **📥 Smart downloads** - Downloads with actual filename of existing file

## 📋 How It Works

### File Search Logic
```
1. When uploading/downloading for campaign "ABC123":
   └── Look in: {templ.storage.path}/ABC123/
       ├── If custom-template.html exists → Use this file
       ├── If welcome-email.html exists → Use this file  
       ├── If ABC123.html exists → Use this file
       └── If no .html files → Create ABC123.html (default)
```

### Backup Strategy
```
Original file: campaign-welcome.html
Upload new content with backup=true
Result:
├── campaign-welcome.html (NEW content)
└── backups/campaign-welcome_20251202_142530.html (OLD content)
```

## 🔧 API Endpoints

### 1. 📥 Download Template File
**GET** `/campaign/{campId}/template/download`

- Searches for any `.html` file in campaign directory
- Downloads with **actual filename** (not forced campId.html)
- Returns 404 if no HTML file exists

**Example:**
```bash
# If directory contains "welcome-email.html"
curl "http://localhost:8080/campaign/summer2024/template/download"
# Downloads as: welcome-email.html (original filename preserved)
```

### 2. 📤 Upload Template File
**POST** `/campaign/{campId}/template/upload`

- Searches for existing `.html` file in directory
- If found: Creates backup + replaces with same filename
- If not found: Creates new file using uploaded filename or default

**Form Data:**
- `file`: HTML file to upload
- `createBackup`: true/false (default: true)

**Backup Behavior:**
```bash
# Scenario 1: Directory has "newsletter.html"
curl -F "file=@new-design.html" -F "createBackup=true" \
  "http://localhost:8080/campaign/spring2024/template/upload"

Result:
├── newsletter.html (NEW content from new-design.html)
└── backups/newsletter_20251202_143022.html (OLD newsletter.html)

# Scenario 2: Empty directory
Result:
└── spring2024.html (NEW content, default naming)
```

### 3. 📋 Get Template Info
**GET** `/campaign/{campId}/template`

- Returns JSON with template information
- Shows actual filename and backup status

**Response includes:**
```json
{
  "campId": "summer2024",
  "filePath": "/path/to/summer2024/welcome-email.html",
  "exists": true,
  "fileSize": 2048,
  "hasBackup": true,
  "backupPath": "/path/to/summer2024/backups",
  "htmlContent": "<!DOCTYPE html>..."
}
```

## 🧪 Test Scripts

### Basic Test
```bash
./test-api.sh
```

### Extended Backup Test
```bash
./test-backup-api.sh
```

## 📂 File Structure Examples

### Example 1: Custom Named Template
```
/var/broadside/preprocessor/msgtmpl/
└── holiday-campaign/
    ├── festive-newsletter.html          # Main template
    └── backups/
        ├── festive-newsletter_20251201_090000.html
        └── festive-newsletter_20251202_143022.html
```

### Example 2: Default Named Template
```
/var/broadside/preprocessor/msgtmpl/
└── promo-2024/
    ├── promo-2024.html                  # Default naming
    └── backups/
        └── promo-2024_20251202_110015.html
```

### Example 3: Multiple Campaigns
```
/var/broadside/preprocessor/msgtmpl/
├── welcome-series/
│   ├── onboarding-email.html
│   └── backups/onboarding-email_20251202_100000.html
├── monthly-newsletter/
│   ├── newsletter-template.html
│   └── backups/newsletter-template_20251201_160000.html
└── flash-sale/
    └── flash-sale.html                  # No backups yet
```

## ✅ Benefits

### 🎯 **Intelligent File Handling**
- No forced naming conventions
- Preserves existing file structure
- Works with any HTML filename

### 🔒 **Safe Operations** 
- Automatic backup before overwrite
- Timestamped backup files
- Optional backup control

### 🚀 **Developer Friendly**
- Consistent API behavior
- Actual filenames in downloads
- Comprehensive metadata

### 📊 **Production Ready**
- Comprehensive logging
- Error handling
- Validation and security

## 🔧 Configuration

**application.properties:**
```properties
# Template storage path
templ.storage.path=/var/broadside/var/data/broadside/preprocessor/msgtmpl
```

## 🎯 Use Cases

### Use Case 1: Marketing Team with Custom Names
```bash
# Marketing uploads: "black-friday-email.html"
POST /campaign/bf2024/template/upload

# Later downloads get the actual filename
GET /campaign/bf2024/template/download
# → Downloads as "black-friday-email.html"
```

### Use Case 2: System Migration
```bash
# Existing system has: "legacy-template.html" 
# New upload automatically backs it up and replaces
# Filename preserved: "legacy-template.html"
```

### Use Case 3: A/B Testing
```bash
# Upload version A
POST /campaign/test/template/upload (file: version-a.html)

# Upload version B (creates backup of A)  
POST /campaign/test/template/upload (file: version-b.html)
# Result: version-b content in same filename as version-a
```

This enhanced system provides maximum flexibility while maintaining safety and consistency! 🎉
