package com.scms.backend.config;

import com.scms.backend.entity.*;
import com.scms.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationQRRepository locationQRRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintHistoryRepository historyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedDepartments();
        seedLocations();
        seedUsers();
        seedComplaints();
    }

    private void seedRoles() {
        List<String> roles = Arrays.asList("REPORTER", "DEPARTMENT_COORDINATOR", "STAFF", "ADMIN");
        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }

    private void seedDepartments() {
        List<String> departments = Arrays.asList("IT", "Computer Engineering", "ENTC", "Mechanical", "Civil", "AI & DS");
        for (String deptName : departments) {
            if (departmentRepository.findByName(deptName).isEmpty()) {
                Department dept = new Department();
                dept.setName(deptName);
                departmentRepository.save(dept);
            }
        }
    }

    private void seedLocations() {
        if (locationQRRepository.count() == 0) {
            createLocation("QR-IT-404", "Main Academic Block", "Room 404", "Smart Projector & 24-Port Cisco Switch");
            createLocation("QR-IT-LAB2", "Computer Science Wing", "Lab 2", "Dell OptiPlex Workstations");
            createLocation("QR-CIV-101", "Civil Engineering Block", "Room 101", "Hydraulics Testing Bench");
            createLocation("QR-MECH-WS", "Central Workshop", "Bay 3", "CNC Lathe & Milling Machine");
            createLocation("QR-AUD-MAIN", "Central Auditorium", "Stage Left", "Audio PA System & Acoustic Wall");
        }
    }

    private void createLocation(String uuid, String building, String roomNumber, String equipment) {
        LocationQR loc = new LocationQR();
        loc.setQrCodeUuid(uuid);
        loc.setBuilding(building);
        loc.setRoomNumber(roomNumber);
        loc.setEquipment(equipment);
        locationQRRepository.save(loc);
    }

    private void seedUsers() {
        Role reporterRole = roleRepository.findByName("REPORTER").orElse(null);
        Role coordinatorRole = roleRepository.findByName("DEPARTMENT_COORDINATOR").orElse(null);
        Role staffRole = roleRepository.findByName("STAFF").orElse(null);
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);

        Department itDept = departmentRepository.findByName("IT").orElse(null);
        Department compDept = departmentRepository.findByName("Computer Engineering").orElse(null);
        Department civilDept = departmentRepository.findByName("Civil").orElse(null);
        Department mechDept = departmentRepository.findByName("Mechanical").orElse(null);

        // 1. Admin User
        createUserIfMissing("admin", "admin@scms.local", "9999999990", adminRole, itDept);

        // 2. Department Coordinators
        createUserIfMissing("coordinator", "coordinator@scms.local", "9999999991", coordinatorRole, itDept);
        createUserIfMissing("coord_civil", "coord_civil@scms.local", "9999999992", coordinatorRole, civilDept);
        createUserIfMissing("coord_mech", "coord_mech@scms.local", "9999999993", coordinatorRole, mechDept);

        // 3. Maintenance & IT Staff
        createUserIfMissing("staff", "staff@scms.local", "9999999994", staffRole, itDept);
        createUserIfMissing("staff_net", "staff_net@scms.local", "9999999995", staffRole, itDept);
        createUserIfMissing("staff_civil", "staff_civil@scms.local", "9999999996", staffRole, civilDept);
        createUserIfMissing("staff_mech", "staff_mech@scms.local", "9999999997", staffRole, mechDept);

        // 4. Reporters (Students / Faculty)
        createUserIfMissing("reporter", "reporter@scms.local", "9999999998", reporterRole, itDept);
        createUserIfMissing("reporter_student", "student@scms.local", "9999999999", reporterRole, compDept);
        createUserIfMissing("reporter_faculty", "faculty@scms.local", "9999999988", reporterRole, civilDept);
    }

    private void createUserIfMissing(String username, String email, String phone, Role role, Department department) {
        if (userRepository.findByUsername(username).isEmpty() && role != null) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("password123"));
            user.setPhone(phone);
            user.setRole(role);
            user.setDepartment(department);
            userRepository.save(user);
        }
    }

    private void seedComplaints() {
        if (complaintRepository.count() > 0) {
            return;
        }

        User reporter = userRepository.findByUsername("reporter").orElse(null);
        User reporterStudent = userRepository.findByUsername("reporter_student").orElse(null);
        User reporterFaculty = userRepository.findByUsername("reporter_faculty").orElse(null);

        User coordinator = userRepository.findByUsername("coordinator").orElse(null);
        User coordCivil = userRepository.findByUsername("coord_civil").orElse(null);

        User staff = userRepository.findByUsername("staff").orElse(null);
        User staffNet = userRepository.findByUsername("staff_net").orElse(null);
        User staffCivil = userRepository.findByUsername("staff_civil").orElse(null);

        Department itDept = departmentRepository.findByName("IT").orElse(null);
        Department civilDept = departmentRepository.findByName("Civil").orElse(null);
        Department mechDept = departmentRepository.findByName("Mechanical").orElse(null);

        LocationQR loc404 = locationQRRepository.findByQrCodeUuid("QR-IT-404").orElse(null);
        LocationQR locLab2 = locationQRRepository.findByQrCodeUuid("QR-IT-LAB2").orElse(null);
        LocationQR locCiv = locationQRRepository.findByQrCodeUuid("QR-CIV-101").orElse(null);
        LocationQR locMech = locationQRRepository.findByQrCodeUuid("QR-MECH-WS").orElse(null);

        // Complaint 1: IT - In Progress (Assigned to staff)
        createComplaintWithHistory(
                "Cisco 24-Port Switch Failure in Lab 404",
                "Intermittent packet loss and port 12-16 orange blinking LED. Student stations losing network connection during practicals.",
                "Network", "Critical", "In Progress", reporter, itDept, staff, "Room 404", loc404,
                Arrays.asList(
                        new HistoryEntry("Submitted", "Complaint submitted via student portal", reporter),
                        new HistoryEntry("Assigned", "Assigned to staff: staff for hardware diagnostic", coordinator),
                        new HistoryEntry("In Progress", "Diagnosed faulty SFP module. Replacement ordered from equipment store.", staff)
                )
        );

        // Complaint 2: IT - Resolved (Assigned to staff)
        createComplaintWithHistory(
                "Epson Projector Lamp Blown in Smart Classroom 402",
                "Projector fails to turn on; red temperature/lamp LED blinks rapidly. Requires replacement bulb.",
                "Hardware", "High", "Resolved", reporter, itDept, staff, "Room 402", loc404,
                Arrays.asList(
                        new HistoryEntry("Submitted", "Complaint submitted", reporter),
                        new HistoryEntry("Assigned", "Assigned to staff: staff", coordinator),
                        new HistoryEntry("In Progress", "Sourced replacement lamp unit from inventory", staff),
                        new HistoryEntry("Resolved", "Lamp replaced, lens cleaned, focus calibrated. Display verified via HDMI.", staff)
                )
        );

        // Complaint 3: IT - Assigned (Assigned to staff_net)
        createComplaintWithHistory(
                "Wi-Fi Access Point Offline in 3rd Floor Corridor",
                "Aruba AP-505 has no power LED. Students cannot access campus network or study materials.",
                "Infrastructure", "Medium", "Assigned", reporterStudent, itDept, staffNet, "3rd Floor Corridor", null,
                Arrays.asList(
                        new HistoryEntry("Submitted", "Complaint submitted", reporterStudent),
                        new HistoryEntry("Assigned", "Assigned to staff: staff_net", coordinator)
                )
        );

        // Complaint 4: IT - Submitted (Unassigned - waiting for coordinator triage)
        createComplaintWithHistory(
                "Workstation #14 Blue Screen Loop in Lab 2",
                "System boots into CRITICAL_PROCESS_DIED BSOD every 10 minutes. SSD SMART diagnostics check needed.",
                "Hardware", "High", "Submitted", reporter, itDept, null, "Lab 2", locLab2,
                Arrays.asList(
                        new HistoryEntry("Submitted", "Complaint submitted by lab student", reporter)
                )
        );

        // Complaint 5: Civil - In Progress (Assigned to staff_civil)
        createComplaintWithHistory(
                "Water Seepage Near Electrical Panel in Fluid Mechanics Lab",
                "Moisture detected on wall adjacent to main 3-phase distribution board. Immediate inspection needed.",
                "Plumbing", "Critical", "In Progress", reporterFaculty, civilDept, staffCivil, "Room 101", locCiv,
                Arrays.asList(
                        new HistoryEntry("Submitted", "Complaint submitted by faculty", reporterFaculty),
                        new HistoryEntry("Assigned", "Assigned to staff: staff_civil with urgent flag", coordCivil),
                        new HistoryEntry("In Progress", "Isolated overhead supply line. Sealing leaking pipe flange.", staffCivil)
                )
        );

        // Complaint 6: Civil - Closed
        createComplaintWithHistory(
                "Damaged Acoustic Door Hinge in Seminar Hall 1",
                "Heavy soundproof door sagging and scratching tiles when closing.",
                "Infrastructure", "Low", "Closed", reporterFaculty, civilDept, staffCivil, "Seminar Hall 1", null,
                Arrays.asList(
                        new HistoryEntry("Submitted", "Complaint submitted", reporterFaculty),
                        new HistoryEntry("Assigned", "Assigned to staff: staff_civil", coordCivil),
                        new HistoryEntry("In Progress", "Replaced heavy-duty ball-bearing hinges", staffCivil),
                        new HistoryEntry("Resolved", "Door alignment restored, swinging smoothly", staffCivil),
                        new HistoryEntry("Closed", "Inspection verified by department coordinator", coordCivil)
                )
        );

        // Complaint 7: Mechanical - Submitted (Critical Unassigned)
        createComplaintWithHistory(
                "Emergency Stop Button Jammed on CNC Lathe #4",
                "Safety emergency stop switch mechanically stuck down and fails to latch. Machine unusable.",
                "Electrical", "Critical", "Submitted", reporterStudent, mechDept, null, "Bay 3", locMech,
                Arrays.asList(
                        new HistoryEntry("Submitted", "Safety complaint submitted", reporterStudent)
                )
        );

        // Complaint 8: IT - Closed
        createComplaintWithHistory(
                "Department Laser Printer Paper Feed Roller Jam",
                "HP LaserJet pulls multiple sheets at once or jams continuously on tray 2.",
                "Hardware", "Medium", "Closed", reporter, itDept, staff, "Faculty Room 3", null,
                Arrays.asList(
                        new HistoryEntry("Submitted", "Complaint submitted", reporter),
                        new HistoryEntry("Assigned", "Assigned to staff: staff", coordinator),
                        new HistoryEntry("Resolved", "Installed new pickup and separation roller kit", staff),
                        new HistoryEntry("Closed", "Verified 100 pages continuous print without jam", coordinator)
                )
        );
    }

    private void createComplaintWithHistory(
            String title, String description, String category, String priority, String status,
            User user, Department department, User assignedStaff, String roomNumber, LocationQR location,
            List<HistoryEntry> historyEntries) {

        if (user == null || department == null) return;

        Complaint complaint = new Complaint();
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setCategory(category);
        complaint.setPriority(priority);
        complaint.setStatus(status);
        complaint.setUser(user);
        complaint.setDepartment(department);
        complaint.setAssignedStaff(assignedStaff);
        complaint.setRoomNumber(roomNumber);
        complaint.setLocation(location);
        complaint.setDateSubmitted(new Date());

        Complaint savedComplaint = complaintRepository.save(complaint);

        if (historyEntries != null) {
            for (HistoryEntry entry : historyEntries) {
                if (entry.user != null) {
                    ComplaintHistory history = new ComplaintHistory();
                    history.setComplaint(savedComplaint);
                    history.setStatus(entry.status);
                    history.setRemarks(entry.remarks);
                    history.setChangedBy(entry.user);
                    history.setTimestamp(new Date());
                    historyRepository.save(history);
                }
            }
        }
    }

    private static class HistoryEntry {
        String status;
        String remarks;
        User user;

        HistoryEntry(String status, String remarks, User user) {
            this.status = status;
            this.remarks = remarks;
            this.user = user;
        }
    }
}
