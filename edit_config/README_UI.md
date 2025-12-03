# Broadside Config Manager - UI Application

A modern, clean Angular application for managing campaign configurations and email templates for the Broadside system.

## 🎯 Features

### Dashboard
- Clean, modern interface with card-based navigation
- Quick access to Campaign Configuration and Template Management
- Feature overview with icons and descriptions

### Campaign Configuration
- **View Campaigns**: Browse all available campaigns
- **Edit Configuration**: Update campaign settings in JSON format
- **Delete Campaigns**: Remove campaigns from the system
- **Real-time Validation**: Instant feedback on configuration changes

### Template Configuration
- **Load Templates**: Retrieve templates by campaign ID
- **Preview Templates**: Live preview of HTML templates
- **Edit HTML**: Inline HTML editor with syntax support
- **Upload Templates**: Upload new HTML files with automatic backup
- **Download Templates**: Download templates to local machine
- **Delete Templates**: Remove templates when no longer needed

## 🚀 Getting Started

### Prerequisites
- Node.js (v18 or higher)
- Angular CLI 19.1.0 or higher
- Backend API running on `http://localhost:8080`

### Installation

1. Install dependencies:
```bash
cd edit_config
npm install
```

2. Update the API URL if needed:
   - Open `src/app/services/config.service.ts`
   - Open `src/app/services/template.service.ts`
   - Update `private apiUrl = 'http://localhost:8080/campaign'` to match your backend

3. Start the development server:
```bash
npm start
```

4. Open your browser and navigate to:
```
http://localhost:4200
```

## 📁 Project Structure

```
src/
├── app/
│   ├── components/
│   │   ├── dashboard/          # Main dashboard landing page
│   │   ├── campaign/           # Campaign configuration component
│   │   └── template/           # Template management component
│   ├── services/
│   │   ├── config.service.ts   # Campaign config API calls
│   │   └── template.service.ts # Template API calls
│   ├── app.component.*         # Root component
│   ├── app.routes.ts           # Application routing
│   └── app.config.ts           # Application configuration
├── styles.css                  # Global styles
├── index.html                  # HTML template
└── main.ts                     # Application entry point
```

## 🎨 UI Components

### Dashboard (`dashboard.component`)
- Header with gradient background
- Feature cards with icons for Campaign and Template management
- Info section highlighting key features
- Responsive grid layout

### Campaign Configuration (`campaign.component`)
- Two-panel layout: campaigns list + details view
- Config list with active state highlighting
- Preview and edit modes
- Action buttons for CRUD operations
- JSON syntax support for config editing

### Template Configuration (`template.component`)
- Campaign ID input with search functionality
- Template information display (file size, backup status)
- Three view modes: raw HTML, edit mode, and live preview
- File upload with backup checkbox
- Download functionality with original filename preservation

## 🎯 API Integration

The application communicates with the backend API using Angular's HttpClient.

### Campaign API Endpoints
- `GET /campaign/configs` - Get all campaigns
- `GET /campaign/{campId}/config` - Get specific campaign config
- `POST /campaign/{campId}/config/update` - Update config
- `DELETE /campaign/{campId}/config` - Delete config

### Template API Endpoints
- `GET /campaign/{campId}/template` - Get template info
- `GET /campaign/{campId}/template/download` - Download template
- `POST /campaign/{campId}/template/upload` - Upload template
- `DELETE /campaign/{campId}/template` - Delete template

## 🎨 Design System

### Color Palette
- **Primary**: `#2563eb` (Blue)
- **Secondary**: `#7c3aed` (Purple)
- **Success**: `#10b981` (Green)
- **Danger**: `#ef4444` (Red)
- **Warning**: `#f59e0b` (Amber)

### Typography
- **Heading Font**: System font stack (-apple-system, BlinkMacSystemFont, Segoe UI, etc.)
- **Body Font**: Same system font stack
- **Code Font**: Courier New for code blocks

### Spacing
- xs: 0.25rem
- sm: 0.5rem
- md: 1rem
- lg: 1.5rem
- xl: 2rem
- 2xl: 3rem
- 3xl: 4rem

## 📱 Responsive Design

The application is fully responsive with breakpoints for:
- Desktop (1024px and above)
- Tablet (768px to 1023px)
- Mobile (below 768px)

## 🔧 Building for Production

Build the application for production:
```bash
npm run build
```

The build artifacts will be stored in the `dist/` directory.

## 🧪 Running Tests

Run unit tests:
```bash
npm test
```

## 📝 Environment Configuration

### Development
- API Base URL: `http://localhost:8080`
- Port: `4200`

### Production
- Update the API URL in `src/app/services/` files
- Build with: `npm run build`

## 🌟 Key Features

### Smart State Management
- Real-time updates after operations
- Error handling with user-friendly messages
- Success notifications for completed actions
- Loading states for async operations

### User Experience
- Smooth animations and transitions
- Hover effects on interactive elements
- Clear visual feedback for all actions
- Responsive layout that works on all devices
- Accessible color contrast ratios

### Code Quality
- Standalone components with Angular 19+
- Type-safe Angular services
- Reactive HTTP error handling
- Clean component architecture

## 🐛 Troubleshooting

### API Connection Issues
1. Ensure the backend is running on `http://localhost:8080`
2. Check CORS configuration on the backend
3. Verify the API endpoints match the services

### File Upload Issues
1. Ensure file size is within backend limits
2. Verify file is valid HTML (.html extension)
3. Check backend storage path configuration

### Display Issues
1. Clear browser cache: `Ctrl+Shift+Delete`
2. Hard refresh: `Ctrl+Shift+R` (Cmd+Shift+R on Mac)
3. Check browser console for errors

## 📖 Documentation

For more information about the Template API features, see the backend README:
- Enhanced file naming preservation
- Smart backup strategies
- Intelligent file search logic

## 🤝 Contributing

1. Follow the existing component structure
2. Use the established naming conventions
3. Keep components focused and reusable
4. Add appropriate styling and responsive support
5. Update documentation as needed

## 📄 License

This project is part of the Broadside system.

---

**Happy Configuring! 🚀**
