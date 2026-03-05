package src.models;

import java.io.Serializable;

public class MedicalRecord implements Serializable {
    private String id;
    private String patientId;
    private String doctorId;
    private String nurseId;
    private String division;
    private String data;

    public MedicalRecord(String id, String patientId, String doctorId, String nurseId, String division, String data) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.nurseId = nurseId;
        this.division = division;
        this.data = data;
    }

    public String getId() { return id; }
    
    public String getPatientId() {
        return patientId;
    }
    public String getDoctorId() {
        return doctorId;
    }
    public String getNurseId() {
        return nurseId; 
    }
    public String getDivision() {
        return division;
    }
    public String getData() {
        return data;   
    }

    @Override
    public String toString() {
        return id + ";" + patientId + ";" + doctorId + ";" + nurseId + ";" + division + ";" + data;
    }

    public static MedicalRecord fromString(String line) {
        String[] parts = line.split(";");
        if (parts.length < 6) return null;
        return new MedicalRecord(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }
}
