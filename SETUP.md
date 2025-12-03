# Broadside Config Manager - Quick Setup Guide

## 🚀 Quick Start

### Step 1: Install Dependencies
```bash
cd edit_config
npm install
```

### Step 2: Update API Configuration (if needed)

If your backend is running on a different URL, update these files:

**File 1:** `src/app/services/config.service.ts`
```typescript
private apiUrl = 'http://your-backend-url:8080/campaign';
```

**File 2:** `src/app/services/template.service.ts`
```typescript
private apiUrl = 'http://your-backend-url:8080/campaign';
```

### Step 3: Start the Development Server
```bash
npm start
```

The application will open automatically at `http://localhost:4200`

---

## 📋 What You'll Find

### Dashboard (`/`)
- Main landing page with navigation
- Campaign Configuration card
- Template Configuration card
- Feature overview

### Campaign Config (`/campaign`)
- View all campaigns in a list
- Click to preview campaign configuration
- Edit button to modify settings (JSON format)
- Delete option to remove campaigns
- Real-time validation and error messages

### Template Config (`/template`)
- Enter a Campaign ID to load its template
- View template file information
- Live preview of HTML content
- Edit HTML directly in the editor
- Upload new templates with automatic backup
- Download templates to your computer
- Delete templates when needed

---

## 🎨 Features Overview

### Smart File Management
✅ Preserves original filenames  
✅ Automatic backups with timestamps  
✅ Search for existing files  
✅ Download with actual filename  

### User Experience
✅ Modern, clean interface  
✅ Responsive design (mobile, tablet, desktop)  
✅ Real-time preview  
✅ Smooth animations and transitions  
✅ Clear error messages  
✅ Success notifications  

### Development
✅ Angular 19+ standalone components  
✅ Type-safe services  
✅ Responsive layout  
✅ Accessible design  

---

## 🔧 Available Commands

```bash
# Start development server
npm start

# Build for production
npm run build

# Run tests
npm test

# Watch for changes during development
npm run watch
```

---

## 📁 File Structure

```
edit_config/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── dashboard/
│   │   │   ├── campaign/
│   │   │   └── template/
│   │   ├── services/
│   │   │   ├── config.service.ts
│   │   │   └── template.service.ts
│   │   ├── app.component.*
│   │   ├── app.routes.ts
│   │   └── app.config.ts
│   ├── styles.css
│   ├── index.html
│   └── main.ts
├── angular.json
├── package.json
└── tsconfig.json
```

---

## 🌐 API Integration

The app communicates with your backend API at:
```
http://localhost:8080/campaign
```

### Campaign Endpoints
- GET `/campaign/configs` - List all campaigns
- GET `/campaign/{campId}/config` - Get specific config
- POST `/campaign/{campId}/config/update` - Update config
- DELETE `/campaign/{campId}/config` - Delete config

### Template Endpoints
- GET `/campaign/{campId}/template` - Get template info
- GET `/campaign/{campId}/template/download` - Download template
- POST `/campaign/{campId}/template/upload` - Upload template
- DELETE `/campaign/{campId}/template` - Delete template

---

## 🎯 Navigation

**Dashboard:**
```
http://localhost:4200/
```

**Campaign Configuration:**
```
http://localhost:4200/campaign
```

**Template Configuration:**
```
http://localhost:4200/template
```

---

## ⚙️ Browser Requirements

- Chrome/Chromium (Latest)
- Firefox (Latest)
- Safari (Latest)
- Edge (Latest)

---

## 🐛 Troubleshooting

### Port Already in Use
If port 4200 is already in use:
```bash
npm start -- --port 4201
```

### API Connection Failed
1. Verify backend is running on `http://localhost:8080`
2. Check CORS configuration in backend
3. Verify service URLs in `src/app/services/`
4. Check browser console for errors (F12)

### Styles Not Loading
1. Clear browser cache (Ctrl+Shift+Delete)
2. Hard refresh (Ctrl+Shift+R)
3. Check `styles.css` is properly loaded

### Dependencies Installation Issues
```bash
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

---

## 💡 Tips

1. **Dark Mode**: You can customize colors by modifying CSS variables in `styles.css`
2. **Responsive Design**: The UI automatically adapts to mobile, tablet, and desktop
3. **Error Handling**: All operations provide clear feedback through alerts
4. **Hot Reload**: Changes in the code automatically reload in the browser

---

## 📞 Support

For issues or questions:
1. Check the browser console (F12) for error messages
2. Review the network tab to see API calls
3. Verify backend is running and accessible
4. Check the README_UI.md for detailed documentation

---

**You're all set! Start the development server and begin managing your configurations.** 🎉
