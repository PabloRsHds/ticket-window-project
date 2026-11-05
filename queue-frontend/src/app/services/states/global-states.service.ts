import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class GlobalStatesService {

  private readonly activeSectionValue = localStorage.getItem('activeSection');

  private _activeSection = signal<string>(this.activeSectionValue || 'inicio');
  public activeSection = this._activeSection.asReadonly();

  private _activeFunction = signal<string>('');
  public activeFunction = this._activeFunction.asReadonly();

  private _openModalTableService = signal<boolean>(false);
  public openModalTableService = this._openModalTableService.asReadonly();

  private _openCardDepartment = signal<boolean>(false);
  public openCardDepartment = this._openCardDepartment.asReadonly();

  private _currentSection = signal<'departamento' | 'servico' | 'config'>('departamento');
  public currentSection = this._currentSection.asReadonly();

  private _openRegisterDepartment = signal<boolean>(false);
  public openRegisterDepartment = this._openRegisterDepartment.asReadonly();

  private _openTableDeparments = signal<boolean>(false);
  public openTableDeparments = this._openTableDeparments.asReadonly();

  private _openRegisterService = signal<boolean>(false);
  public openRegisterService = this._openRegisterService.asReadonly();

  private _openTableServices = signal<boolean>(false);
  public openTableServices = this._openTableServices.asReadonly();


  setActiveSection(value: string): void {
    this._activeSection.set(value);
    localStorage.setItem('activeSection', value);
  }

  setActiveFunction(value: string): void {
    this._activeFunction.set(value);
  }

  setOpenModalTableService(value: boolean): void {
    this._openModalTableService.set(value);
  }

  setOpenCardDepartment(value: boolean): void {
    this._openCardDepartment.set(value);
  }

  setCurrentSection(value: 'departamento' | 'servico' | 'config'): void {
    this._currentSection.set(value);
  }

  setOpenRegisterDepartment(value: boolean): void {
    this._openRegisterDepartment.set(value);
  }

  setOpenTableDeparments(value: boolean): void {
    this._openTableDeparments.set(value);
  }

  setOpenRegisterService(value: boolean): void {
    this._openRegisterService.set(value);
  }

  setOpenTableServices(value: boolean): void {
    this._openTableServices.set(value);
  }


  closeModalRegisterService(): void {
    this._openRegisterService.set(false);
  }

  closeModalRegisterDepartment(): void {
    this._openRegisterDepartment.set(false);
  }

  closeModalTableDeparments(): void {
    this._openTableDeparments.set(false);
  }

  closeModalTableServices(): void {
    this._openTableServices.set(false);
  }
}
