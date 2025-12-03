# 🚀 Quick Reference Guide

## Getting Started (60 seconds)

### 1. Install
```bash
cd edit_config
npm install
```

### 2. Start
```bash
npm start
```

### 3. Open
```
http://localhost:4200
```

---

## 🧭 Navigation Map

```
HOME (Dashboard)
├── Campaign Configuration (/campaign)
│   ├── View Campaigns
│   ├── Edit Configuration
│   └── Delete Campaigns
│
└── Template Configuration (/template)
    ├── Load Template
    ├── View/Edit HTML
    ├── Preview Template
    ├── Upload Template
    └── Delete Template
```

---

## 📱 UI Locations

| Feature | URL | Access |
|---------|-----|--------|
| Dashboard | http://localhost:4200 | Click logo or home |
| Campaigns | http://localhost:4200/campaign | Card click on dashboard |
| Templates | http://localhost:4200/template | Card click on dashboard |

---

## 🎨 Key Colors

| Color | Hex | Use |
|-------|-----|-----|
| Primary Blue | #2563eb | Buttons, links |
| Success Green | #10b981 | Success messages |
| Error Red | #ef4444 | Error messages |
| Accent Purple | #7c3aed | Secondary elements |
| Gray | #6b7280 | Text, borders |

---

## 📂 Important Files

| File | Location | Purpose |
|------|----------|---------|
| Routes | `src/app/app.routes.ts` | Navigation paths |
| Global Styles | `src/styles.css` | Design system |
| Config Service | `src/app/services/config.service.ts` | Campaign API |
| Template Service | `src/app/services/template.service.ts` | Template API |

---

## 🔧 Configuration

### Update API URL
Edit these files:
- `src/app/services/config.service.ts` (line ~9)
- `src/app/services/template.service.ts` (line ~9)

Change:
```typescript
private apiUrl = 'http://localhost:8080/campaign';
```

To your backend URL.

---

## 💻 Commands

```bash
# Start development
npm start

# Build for production
npm run build

# Run tests
npm test

# Watch changes
npm run watch
```

---

## 🐛 Troubleshooting Quick Fixes

| Issue | Solution |
|-------|----------|
| Port 4200 in use | `npm start -- --port 4201` |
| API not connecting | Check backend running on :8080 |
| Styles not loading | Hard refresh: Ctrl+Shift+R |
| Blank page | Check browser console (F12) |
| CORS error | Enable CORS on backend |
| File upload fails | Check file is .html only |

---

## 🎯 Component Quick Reference

### Dashboard Component
**Location**: `src/app/components/dashboard/`
- Shows feature cards
- Navigates to other sections
- Main landing page

### Campaign Component
**Location**: `src/app/components/campaign/`
- List campaigns (left panel)
- Show details (right panel)
- Edit JSON config
- Delete campaigns

### Template Component
**Location**: `src/app/components/template/`
- Search by campaign ID
- View/edit HTML
- Live preview
- Upload/download files

---

## 🎨 Customization Quick Reference

### Change Primary Color
**File**: `src/styles.css`
**Find**: `--primary-color: #2563eb;`
**Change**: Replace hex code

### Change Button Style
**File**: `src/styles.css`
**Find**: `.btn-primary {`
**Modify**: CSS properties

### Change Spacing
**File**: `src/styles.css`
**Find**: `--spacing-md: 1rem;`
**Change**: Adjust value

---

## 📚 Documentation Files

- `README_UI.md` - Full UI documentation
- `SETUP.md` - Setup instructions
- `UI_DESIGN.md` - Design system details
- `FEATURES.md` - Feature checklist
- `IMPLEMENTATION_SUMMARY.md` - What was built

---

## 🔌 API Quick Reference

### Campaign Endpoints
```
GET  /campaign/configs
GET  /campaign/{campId}/config
POST /campaign/{campId}/config/update
DELETE /campaign/{campId}/config
```

### Template Endpoints
```
GET  /campaign/{campId}/template
GET  /campaign/{campId}/template/download
POST /campaign/{campId}/template/upload
DELETE /campaign/{campId}/template
```

---

## 🎓 File Structure Tree

```
edit_config/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── dashboard/  (3 files)
│   │   │   ├── campaign/   (3 files)
│   │   │   └── template/   (3 files)
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

## ✨ Feature Summary

| Feature | Component | Status |
|---------|-----------|--------|
| View Campaigns | Campaign | ✅ Active |
| Edit Config | Campaign | ✅ Active |
| Delete Campaign | Campaign | ✅ Active |
| Load Template | Template | ✅ Active |
| Edit HTML | Template | ✅ Active |
| Preview Template | Template | ✅ Active |
| Upload Template | Template | ✅ Active |
| Download Template | Template | ✅ Active |
| Delete Template | Template | ✅ Active |

---

## 🎯 Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Hard Refresh | Ctrl+Shift+R |
| Dev Tools | F12 |
| Load Template | Enter (in input) |
| Inspect Element | Ctrl+Shift+C |

---

## 📊 Button Guide

| Button | Color | Use |
|--------|-------|-----|
| Primary | Blue | Main actions |
| Secondary | Gray | Secondary actions |
| Outline | Blue border | Alternative actions |
| Delete | Red | Dangerous actions |

---

## 🔐 Security Checklist

- ✅ Only .html files accepted
- ✅ No hardcoded credentials
- ✅ HTTPS ready
- ✅ CORS-protected
- ✅ Input validated

---

## 📞 Quick Help

**Can't see the app?**
- Check http://localhost:4200 is open
- Check npm start is running
- Check console for errors (F12)

**API not working?**
- Verify backend on http://localhost:8080
- Check network tab in DevTools
- Verify API URLs in services

**Styles broken?**
- Hard refresh (Ctrl+Shift+R)
- Clear cache
- Check styles.css is loaded

---

## 🎉 You're All Set!

The application is ready to use. Start with:
1. `npm install`
2. `npm start`
3. Visit http://localhost:4200

Enjoy managing your campaigns and templates! 🚀
