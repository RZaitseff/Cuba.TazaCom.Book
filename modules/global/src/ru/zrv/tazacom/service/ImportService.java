package ru.zrv.tazacom.service;

public interface ImportService {
    String NAME = "tazacom_ImportService";

	String doImport(String url);
	
	String doImportRest(String url);
	
	String doImport();
}