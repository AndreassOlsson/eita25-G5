package tests;

import src.models.MedicalRecord;
import src.models.Role;
import src.models.User;

/**
 * Shared test data matching the PKI setup (setup_pki.sh).
 * All IDs follow the certificate naming convention: role_firstname.
 * All usernames match the certificate CN field.
 */
public final class TestFixtures {

    private TestFixtures() {} // Utility class

    // ── User IDs (derived from certificate CN + Role, see User.normalizeUsername) ──
    public static final String ID_DOCTOR_ALICE   = "doctor_alice";
    public static final String ID_DOCTOR_MALLORY = "doctor_mallory";
    public static final String ID_NURSE_BOB      = "nurse_bob";
    public static final String ID_NURSE_CAROL    = "nurse_carol";
    public static final String ID_PATIENT_CHARLIE = "patient_charlie";
    public static final String ID_PATIENT_EVE    = "patient_eve";
    public static final String ID_GOV_DAVE       = "gov_dave";

    // ── Usernames (certificate CN field) ──
    public static final String CN_ALICE   = "Alice Alison";
    public static final String CN_MALLORY = "Mallory Mallet";
    public static final String CN_BOB     = "Bob Bobson";
    public static final String CN_CAROL   = "Carol Carlson";
    public static final String CN_CHARLIE = "Charlie Charles";
    public static final String CN_EVE     = "Eve Evans";
    public static final String CN_DAVE    = "Dave Davidson";

    // ── Divisions (certificate OU field) ──
    public static final String DIV_CARDIOLOGY = "Cardiology";
    public static final String DIV_ONCOLOGY   = "Oncology";

    // ── Record IDs ──
    public static final String REC_CHARLIE_BP       = "rec_charlie_bp";
    public static final String REC_CHARLIE_FOLLOWUP = "rec_charlie_followup";
    public static final String REC_EVE_ONCOLOGY     = "rec_eve_oncology";
    public static final String REC_CHARLIE_NURSE    = "rec_charlie_nurse";

    // ── Users ──

    public static User doctorAlice() {
        return new User(ID_DOCTOR_ALICE, CN_ALICE, Role.DOCTOR, DIV_CARDIOLOGY);
    }

    public static User doctorMallory() {
        return new User(ID_DOCTOR_MALLORY, CN_MALLORY, Role.DOCTOR, DIV_ONCOLOGY);
    }

    public static User nurseBob() {
        return new User(ID_NURSE_BOB, CN_BOB, Role.NURSE, DIV_CARDIOLOGY);
    }

    public static User nurseCarol() {
        return new User(ID_NURSE_CAROL, CN_CAROL, Role.NURSE, DIV_CARDIOLOGY);
    }

    public static User patientCharlie() {
        return new User(ID_PATIENT_CHARLIE, CN_CHARLIE, Role.PATIENT, null);
    }

    public static User patientEve() {
        return new User(ID_PATIENT_EVE, CN_EVE, Role.PATIENT, null);
    }

    public static User govDave() {
        return new User(ID_GOV_DAVE, CN_DAVE, Role.GOVERNMENT, null);
    }

    // ── Medical Records ──

    /** Charlie's blood pressure record — Alice's patient, Bob assigned as nurse, Cardiology */
    public static MedicalRecord charlieBpRecord() {
        return new MedicalRecord(REC_CHARLIE_BP, ID_PATIENT_CHARLIE, ID_DOCTOR_ALICE, ID_NURSE_BOB, DIV_CARDIOLOGY, "Blood Pressure 120/80");
    }

    /** Charlie's follow-up — Alice's patient, Carol assigned as nurse, Cardiology */
    public static MedicalRecord charlieFollowUpRecord() {
        return new MedicalRecord(REC_CHARLIE_FOLLOWUP, ID_PATIENT_CHARLIE, ID_DOCTOR_ALICE, ID_NURSE_CAROL, DIV_CARDIOLOGY, "Follow-up consultation");
    }

    /** Eve's oncology record — Mallory's patient, Carol assigned as nurse, Oncology */
    public static MedicalRecord eveOncologyRecord() {
        return new MedicalRecord(REC_EVE_ONCOLOGY, ID_PATIENT_EVE, ID_DOCTOR_MALLORY, ID_NURSE_CAROL, DIV_ONCOLOGY, "Oncology intake");
    }

    /** Charlie's nurse-specific record — Alice's patient, Bob assigned as nurse, Cardiology */
    public static MedicalRecord charlieNurseRecord() {
        return new MedicalRecord(REC_CHARLIE_NURSE, ID_PATIENT_CHARLIE, ID_DOCTOR_ALICE, ID_NURSE_BOB, DIV_CARDIOLOGY, "Nurse intake");
    }
}
