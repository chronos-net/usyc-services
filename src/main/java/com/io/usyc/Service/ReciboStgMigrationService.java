package com.io.usyc.Service;

public interface ReciboStgMigrationService {
    int migrateByNombreToAlumno(String nombre, String alumnoId);
    boolean alreadyMigratedForAlumno(String alumnoId);

}
