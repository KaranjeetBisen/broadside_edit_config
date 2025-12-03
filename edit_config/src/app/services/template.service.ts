import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TemplateView {
  campId: string;
  filePath: string;
  exists: boolean;
  fileSize: number;
  hasBackup: boolean;
  backupPath?: string;
  htmlContent?: string;
}

export interface TemplateUpdateRequest {
  file: File;
  createBackup?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class TemplateService {
  private apiUrl = 'http://localhost:8080/campaign';

  constructor(private http: HttpClient) {}

  // Get template info
  getTemplateInfo(campId: string): Observable<TemplateView> {
    return this.http.get<TemplateView>(`${this.apiUrl}/${campId}/template`);
  }

  // Download template
  downloadTemplate(campId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${campId}/template/download`, {
      responseType: 'blob'
    });
  }

  // Upload template
  uploadTemplate(campId: string, file: File, createBackup: boolean = true): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('createBackup', createBackup.toString());
    
    return this.http.post(`${this.apiUrl}/${campId}/template/upload`, formData);
  }

  // Delete template
  deleteTemplate(campId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${campId}/template`);
  }
}
