# Template API Usage Guide - Most Reliable Approach

## Overview
This Template API provides the most reliable approach for HTML template management with comprehensive validation, backup, and error handling features.

## API Endpoints

The API provides both **JSON-based** and **File-based** operations:

### 1. 📄 File Download - Download HTML Template as File
**Endpoint:** `GET /campaign/{campId}/template/download`

**Description:** Downloads the actual HTML file for a campaign. The browser will download the `.html` file directly.

**File Path:** `{templ.storage.path}/{campaignId}/{campaignId}.html`

**Example Request:**
```bash
# Downloads campaignId.html file
curl -X GET "http://localhost:8080/campaign/welcome2024/template/download" \
  -H "Accept: application/octet-stream" \
  -O  # This saves the file locally
```

**Response:** Binary file download with headers:
- `Content-Disposition: attachment; filename="welcome2024.html"`
- `Content-Type: text/html`
- `Content-Length: {file_size}`

### 2. 📤 File Upload - Upload HTML Template File
**Endpoint:** `POST /campaign/{campId}/template/upload`

**Description:** Uploads an HTML file for a campaign. Accepts multipart form data.

**Example Request:**
```bash
# Upload an HTML file
curl -X POST "http://localhost:8080/campaign/welcome2024/template/upload" \
  -F "file=@template.html" \
  -F "createBackup=true"
```

**Form Data:**
- `file` (required): HTML file to upload
- `createBackup` (optional): Whether to create backup (default: true)

**Response:** JSON with template information (same as GET template info)

### 3. 📋 Get Template Info - JSON Response with Template Data
**Endpoint:** `GET /campaign/{campId}/template`

**Description:** Returns template information and content as JSON (not a file download).

**Example Request:**
```bash
curl -X GET "http://localhost:8080/campaign/welcome2024/template" \
  -H "Accept: application/json"
```

**Example Response:**
```json
{
  "campId": "welcome2024",
  "htmlContent": "<!DOCTYPE html><html><head><title>Welcome</title></head><body><h1>Welcome {{NAME}}</h1></body></html>",
  "filePath": "/var/broadside/.../welcome2024/welcome2024.html",
  "exists": true,
  "fileSize": 145,
  "lastModified": "2025-12-02T10:06:47.123Z",
  "contentHash": "a1b2c3d4e5f6...",
  "validHtml": true,
  "validationMessage": "Valid HTML structure"
}
```

**Example Response (when template doesn't exist):**
```json
{
  "campId": "12345",
  "htmlContent": "",
  "filePath": "/var/broadside/var/data/broadside/preprocessor/msgtmpl/12345/12345.html",
  "exists": false,
  "fileSize": 0,
  "lastModified": ""
}
```

### 2. PUT Template - Upload/Update HTML Template
**Endpoint:** `PUT /campaign/{campId}/template`

**Description:** Uploads or updates the HTML template for a specific campaign. This will create the directory structure if it doesn't exist and override the existing template file.

**Example Request:**
```bash
curl -X PUT http://localhost:8080/campaign/12345/template \
  -H "Content-Type: application/json" \
  -d '{
    "htmlContent": "<!DOCTYPE html><html><head><title>Updated Email Template</title></head><body><h1>Hello {{CUSTOMER_NAME}}</h1><p>Thank you for your business!</p><footer>Best regards, Team</footer></body></html>"
  }'
```

**Example Response:**
```json
{
  "campId": "12345",
  "htmlContent": "<!DOCTYPE html><html><head><title>Updated Email Template</title></head><body><h1>Hello {{CUSTOMER_NAME}}</h1><p>Thank you for your business!</p><footer>Best regards, Team</footer></body></html>",
  "filePath": "/var/broadside/var/data/broadside/preprocessor/msgtmpl/12345/12345.html",
  "exists": true,
  "fileSize": 185,
  "lastModified": "2025-12-02T10:15:32.456Z"
}
```

## Features

1. **Automatic Directory Creation:** If the campaign directory doesn't exist, it will be created automatically
2. **File Override:** When uploading a template, it will override any existing template file
3. **Job Tracking:** All operations are tracked in the jobq table for monitoring
4. **Error Handling:** Proper error responses with meaningful messages
5. **File Metadata:** Returns file size and last modified timestamp

## File Structure

The template files are stored following this pattern:
```
{templ.storage.path}/
├── campaignId1/
│   └── campaignId1.html
├── campaignId2/
│   └── campaignId2.html
└── ...
```

Where `templ.storage.path` is configured in `application.properties` as:
```properties
templ.storage.path=/var/broadside/var/data/broadside/preprocessor/msgtmpl
```
