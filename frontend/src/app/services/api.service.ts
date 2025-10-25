import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { Repository, Build, RepositoryWithLatestBuild, SyncResponse } from '../models/repository.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  // Repository endpoints
  getAllRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(`${this.apiUrl}/repositories`);
  }

  getRepositoryById(id: number): Observable<Repository> {
    return this.http.get<Repository>(`${this.apiUrl}/repositories/${id}`);
  }

  createRepository(repository: Partial<Repository>): Observable<Repository> {
    return this.http.post<Repository>(`${this.apiUrl}/repositories`, repository);
  }

  // Build endpoints
  getAllBuilds(): Observable<Build[]> {
    return this.http.get<Build[]>(`${this.apiUrl}/builds`);
  }

  getBuildById(id: number): Observable<Build> {
    return this.http.get<Build>(`${this.apiUrl}/builds/${id}`);
  }

  getBuildsByRepository(repositoryId: number): Observable<Build[]> {
    return this.http.get<Build[]>(`${this.apiUrl}/builds/repository/${repositoryId}`);
  }

  getBuildsByStatus(status: string): Observable<Build[]> {
    return this.http.get<Build[]>(`${this.apiUrl}/builds/status/${status}`);
  }

  syncBuilds(owner: string, repo: string, repositoryId: number): Observable<SyncResponse> {
    return this.http.post<SyncResponse>(`${this.apiUrl}/builds/sync`, {
      owner,
      repo,
      repositoryId
    });
  }

  triggerScheduledSync(): Observable<any> {
    return this.http.post(`${this.apiUrl}/scheduler/trigger-sync`, {});
  }

  // Combined endpoint to get repositories with their latest builds
  getRepositoriesWithLatestBuilds(): Observable<RepositoryWithLatestBuild[]> {
    return this.getAllRepositories().pipe(
      map(repositories => {
        if (repositories.length === 0) {
          return [];
        }

        // Create observables for each repository's builds
        const buildObservables = repositories.map(repo =>
          this.getBuildsByRepository(repo.id).pipe(
            map(builds => {
              // Sort builds by startedAt descending and get the latest
              const latestBuild = builds.sort((a, b) =>
                new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime()
              )[0];

              return {
                repository: repo,
                latestBuild: latestBuild
              } as RepositoryWithLatestBuild;
            }),
            catchError(() => of({
              repository: repo,
              latestBuild: undefined
            } as RepositoryWithLatestBuild))
          )
        );

        // Wait for all build requests to complete
        return forkJoin(buildObservables);
      }),
      map(observable => observable || [])
    ).pipe(
      map(results => results || [])
    );
  }

  // GitHub Actions endpoints
  getGitHubStatus(owner: string, repo: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/github/status/${owner}/${repo}`);
  }

  getGitHubWorkflowRuns(owner: string, repo: string, perPage: number = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/github/runs/${owner}/${repo}?perPage=${perPage}`);
  }
}
