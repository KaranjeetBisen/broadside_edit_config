import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ConfigView {
  campId: string;
  configName: string;
  configSettings: any;
  description?: string;
}

export interface ConfigUpdateRequest {
  campId: string;
  newConfig: any;
}

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private apiUrl = 'http://localhost:8080/campaign';

  constructor(private http: HttpClient) {}

  // Get all campaign configs
  getAllConfigs(): Observable<ConfigView[]> {
    return this.http.get<ConfigView[]>(`${this.apiUrl}/configs`);
  }

  // Get specific campaign config
  getConfig(campId: string): Observable<ConfigView> {
    return this.http.get<ConfigView>(`${this.apiUrl}/${campId}/config`);
  }

  // Update campaign config
  updateConfig(campId: string, newConfig: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/${campId}/config/update`, {
      campId,
      newConfig
    });
  }

  // Delete campaign config
  deleteConfig(campId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${campId}/config`);
  }
}
