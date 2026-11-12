package com.scms.backend.service;

import com.scms.backend.dto.ComplaintRequest;
import com.scms.backend.dto.ComplaintResponse;
import com.scms.backend.entity.*;
import com.scms.backend.exception.ResourceNotFoundException;
import com.scms.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplaintService Unit Tests")
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private LocationQRRepository locationQRRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ComplaintImageRepository imageRepository;

    @Mock
    private ComplaintHistoryRepository historyRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private ComplaintService complaintService;

    private Department deptIT;
    private Department deptCivil;
    private Role roleReporter;
    private Role roleStaff;
    private Role roleCoordinator;
    private Role roleAdmin;
    private User reporterUser;
    private User otherReporterUser;
    private User staffUser;
    private User otherDeptStaffUser;
    private User coordinatorUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        deptIT = new Department(1L, "IT");
        deptCivil = new Department(2L, "Civil");

        roleReporter = new Role(1L, "REPORTER");
        roleStaff = new Role(2L, "STAFF");
        roleCoordinator = new Role(3L, "DEPARTMENT_COORDINATOR");
        roleAdmin = new Role(4L, "ADMIN");

        reporterUser = new User(10L, "reporter1", "reporter1@test.com", "pass", "1234567890", roleReporter, deptIT, new Date());
        otherReporterUser = new User(11L, "reporter2", "reporter2@test.com", "pass", "1234567891", roleReporter, deptIT, new Date());
        staffUser = new User(20L, "staffIT", "staffIT@test.com", "pass", "1234567892", roleStaff, deptIT, new Date());
        otherDeptStaffUser = new User(21L, "staffCivil", "staffCivil@test.com", "pass", "1234567893", roleStaff, deptCivil, new Date());
        coordinatorUser = new User(30L, "coordIT", "coordIT@test.com", "pass", "1234567894", roleCoordinator, deptIT, new Date());
        adminUser = new User(40L, "adminUser", "admin@test.com", "pass", "1234567895", roleAdmin, null, new Date());
    }

    private Complaint createSampleComplaint(Long id, User reporter, Department dept, User assignedStaff, String status) {
        Complaint complaint = new Complaint();
        complaint.setId(id);
        complaint.setTitle("Broken Projector");
        complaint.setDescription("Room 404 projector has no HDMI signal.");
        complaint.setCategory("Electrical");
        complaint.setPriority("High");
        complaint.setStatus(status != null ? status : "Submitted");
        complaint.setUser(reporter);
        complaint.setDepartment(dept);
        complaint.setAssignedStaff(assignedStaff);
        complaint.setRoomNumber("404");
        complaint.setDateSubmitted(new Date());
        return complaint;
    }

    @Nested
    @DisplayName("Complaint Creation Tests")
    class ComplaintCreationTests {

        @Test
        @DisplayName("Should successfully create complaint with valid input")
        void shouldCreateComplaintWithValidInput() throws Exception {
            // Arrange
            ComplaintRequest request = new ComplaintRequest();
            request.setTitle("Network Cable Damaged");
            request.setDescription("Ethernet port pin broken in Lab 3.");
            request.setCategory("IT Infrastructure");
            request.setPriority("High");
            request.setDepartmentId(1L);
            request.setRoomNumber("Lab 3");

            when(userRepository.findByUsername("reporter1")).thenReturn(Optional.of(reporterUser));
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(deptIT));
            when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
                Complaint c = invocation.getArgument(0);
                c.setId(100L);
                return c;
            });

            // Act
            ComplaintResponse response = complaintService.createComplaint(request, "reporter1");

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getTitle()).isEqualTo("Network Cable Damaged");
            assertThat(response.getStatus()).isEqualTo("Submitted");
            assertThat(response.getDepartmentName()).isEqualTo("IT");
            assertThat(response.getUserEmail()).isEqualTo("reporter1@test.com");

            verify(complaintRepository).save(any(Complaint.class));
            verify(historyRepository).save(any(ComplaintHistory.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when title is missing or blank")
        void shouldFailWhenTitleIsMissing() {
            ComplaintRequest request = new ComplaintRequest();
            request.setTitle("   ");
            request.setDescription("Valid description");
            request.setCategory("IT");
            request.setPriority("High");
            request.setDepartmentId(1L);

            assertThatThrownBy(() -> complaintService.createComplaint(request, "reporter1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Title is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when description is missing")
        void shouldFailWhenDescriptionIsMissing() {
            ComplaintRequest request = new ComplaintRequest();
            request.setTitle("Valid Title");
            request.setDescription(null);
            request.setCategory("IT");
            request.setPriority("High");
            request.setDepartmentId(1L);

            assertThatThrownBy(() -> complaintService.createComplaint(request, "reporter1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Description is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when category is missing")
        void shouldFailWhenCategoryIsMissing() {
            ComplaintRequest request = new ComplaintRequest();
            request.setTitle("Valid Title");
            request.setDescription("Valid description");
            request.setCategory("");
            request.setPriority("High");
            request.setDepartmentId(1L);

            assertThatThrownBy(() -> complaintService.createComplaint(request, "reporter1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when priority is missing")
        void shouldFailWhenPriorityIsMissing() {
            ComplaintRequest request = new ComplaintRequest();
            request.setTitle("Valid Title");
            request.setDescription("Valid description");
            request.setCategory("IT");
            request.setPriority("  ");
            request.setDepartmentId(1L);

            assertThatThrownBy(() -> complaintService.createComplaint(request, "reporter1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Priority is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when departmentId is null")
        void shouldFailWhenDepartmentIdIsNull() {
            ComplaintRequest request = new ComplaintRequest();
            request.setTitle("Valid Title");
            request.setDescription("Valid description");
            request.setCategory("IT");
            request.setPriority("High");
            request.setDepartmentId(null);

            assertThatThrownBy(() -> complaintService.createComplaint(request, "reporter1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Department is required");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when department does not exist")
        void shouldFailWhenDepartmentNotFound() {
            ComplaintRequest request = new ComplaintRequest();
            request.setTitle("Valid Title");
            request.setDescription("Valid description");
            request.setCategory("IT");
            request.setPriority("High");
            request.setDepartmentId(999L);

            when(userRepository.findByUsername("reporter1")).thenReturn(Optional.of(reporterUser));
            when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> complaintService.createComplaint(request, "reporter1"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Department not found");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user is not found")
        void shouldFailWhenUserNotFound() {
            ComplaintRequest request = new ComplaintRequest();
            request.setTitle("Valid Title");
            request.setDescription("Valid description");
            request.setCategory("IT");
            request.setPriority("High");
            request.setDepartmentId(1L);

            when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> complaintService.createComplaint(request, "unknownUser"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("Complaint Retrieval Tests")
    class ComplaintRetrievalTests {

        @Test
        @DisplayName("Reporter retrieves only their own complaints")
        void reporterRetrievesOnlyOwnComplaints() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(userRepository.findByUsername("reporter1")).thenReturn(Optional.of(reporterUser));
            when(complaintRepository.findByUserId(10L)).thenReturn(List.of(c1));

            List<ComplaintResponse> result = complaintService.getAllComplaints("reporter1");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            verify(complaintRepository).findByUserId(10L);
            verify(complaintRepository, never()).findAll();
        }

        @Test
        @DisplayName("Staff retrieves only assigned complaints")
        void staffRetrievesOnlyAssignedComplaints() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, staffUser, "Assigned");
            when(userRepository.findByUsername("staffIT")).thenReturn(Optional.of(staffUser));
            when(complaintRepository.findByAssignedStaffId(20L)).thenReturn(List.of(c1));

            List<ComplaintResponse> result = complaintService.getAllComplaints("staffIT");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            verify(complaintRepository).findByAssignedStaffId(20L);
        }

        @Test
        @DisplayName("Coordinator retrieves complaints within their department")
        void coordinatorRetrievesDepartmentComplaints() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(userRepository.findByUsername("coordIT")).thenReturn(Optional.of(coordinatorUser));
            when(complaintRepository.findByDepartmentId(1L)).thenReturn(List.of(c1));

            List<ComplaintResponse> result = complaintService.getAllComplaints("coordIT");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            verify(complaintRepository).findByDepartmentId(1L);
        }

        @Test
        @DisplayName("Admin retrieves all complaints across departments")
        void adminRetrievesAllComplaints() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            Complaint c2 = createSampleComplaint(2L, otherReporterUser, deptCivil, null, "Submitted");
            when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminUser));
            when(complaintRepository.findAll()).thenReturn(List.of(c1, c2));

            List<ComplaintResponse> result = complaintService.getAllComplaints("adminUser");

            assertThat(result).hasSize(2);
            verify(complaintRepository).findAll();
        }

        @Test
        @DisplayName("Reporter can view their own complaint by ID")
        void reporterCanViewOwnComplaint() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(userRepository.findByUsername("reporter1")).thenReturn(Optional.of(reporterUser));
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));

            ComplaintResponse response = complaintService.getComplaintById(1L, "reporter1");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Reporter is denied when accessing another reporter's complaint")
        void reporterDeniedAccessToOtherReporterComplaint() {
            Complaint c2 = createSampleComplaint(2L, otherReporterUser, deptIT, null, "Submitted");
            when(userRepository.findByUsername("reporter1")).thenReturn(Optional.of(reporterUser));
            when(complaintRepository.findById(2L)).thenReturn(Optional.of(c2));

            assertThatThrownBy(() -> complaintService.getComplaintById(2L, "reporter1"))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("You can only view your own complaints");
        }

        @Test
        @DisplayName("Staff can view assigned complaint by ID")
        void staffCanViewAssignedComplaint() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, staffUser, "Assigned");
            when(userRepository.findByUsername("staffIT")).thenReturn(Optional.of(staffUser));
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));

            ComplaintResponse response = complaintService.getComplaintById(1L, "staffIT");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Staff is denied when accessing unassigned complaint")
        void staffDeniedAccessToUnassignedComplaint() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(userRepository.findByUsername("staffIT")).thenReturn(Optional.of(staffUser));
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));

            assertThatThrownBy(() -> complaintService.getComplaintById(1L, "staffIT"))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Complaint is not assigned to you");
        }

        @Test
        @DisplayName("Coordinator can view complaint within department")
        void coordinatorCanViewDepartmentComplaint() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(userRepository.findByUsername("coordIT")).thenReturn(Optional.of(coordinatorUser));
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));

            ComplaintResponse response = complaintService.getComplaintById(1L, "coordIT");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Coordinator is denied when accessing complaint outside department")
        void coordinatorDeniedAccessOutsideDepartment() {
            Complaint c2 = createSampleComplaint(2L, otherReporterUser, deptCivil, null, "Submitted");
            when(userRepository.findByUsername("coordIT")).thenReturn(Optional.of(coordinatorUser));
            when(complaintRepository.findById(2L)).thenReturn(Optional.of(c2));

            assertThatThrownBy(() -> complaintService.getComplaintById(2L, "coordIT"))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Complaint is outside your department");
        }

        @Test
        @DisplayName("Admin can view any complaint by ID")
        void adminCanViewAnyComplaint() {
            Complaint c2 = createSampleComplaint(2L, otherReporterUser, deptCivil, null, "Submitted");
            when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminUser));
            when(complaintRepository.findById(2L)).thenReturn(Optional.of(c2));

            ComplaintResponse response = complaintService.getComplaintById(2L, "adminUser");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent complaint ID")
        void shouldThrowWhenComplaintNotFound() {
            when(userRepository.findByUsername("reporter1")).thenReturn(Optional.of(reporterUser));
            when(complaintRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> complaintService.getComplaintById(999L, "reporter1"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Complaint not found");
        }
    }

    @Nested
    @DisplayName("Complaint Assignment Tests")
    class ComplaintAssignmentTests {

        @Test
        @DisplayName("Coordinator assigns complaint in their department to staff in same department (valid)")
        void coordinatorAssignsComplaintSuccessfully() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));
            when(userRepository.findByUsername("coordIT")).thenReturn(Optional.of(coordinatorUser));
            when(userRepository.findById(20L)).thenReturn(Optional.of(staffUser));
            when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ComplaintResponse response = complaintService.assignComplaint(1L, 20L, "coordIT");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("Assigned");
            assertThat(response.getAssignedStaffEmail()).isEqualTo("staffIT@test.com");
            verify(complaintRepository).save(c1);
            verify(historyRepository).save(any(ComplaintHistory.class));
        }

        @Test
        @DisplayName("Coordinator cannot assign complaint outside of their department")
        void coordinatorCannotAssignOutsideDepartment() {
            Complaint c2 = createSampleComplaint(2L, otherReporterUser, deptCivil, null, "Submitted");
            when(complaintRepository.findById(2L)).thenReturn(Optional.of(c2));
            when(userRepository.findByUsername("coordIT")).thenReturn(Optional.of(coordinatorUser));

            assertThatThrownBy(() -> complaintService.assignComplaint(2L, 20L, "coordIT"))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Cannot assign complaint outside of your department");

            verify(complaintRepository, never()).save(any());
        }

        @Test
        @DisplayName("Issue 4: Cannot assign complaint to staff belonging to a different department")
        void cannotAssignToStaffInDifferentDepartment() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));
            when(userRepository.findByUsername("coordIT")).thenReturn(Optional.of(coordinatorUser));
            when(userRepository.findById(21L)).thenReturn(Optional.of(otherDeptStaffUser)); // Civil staff

            assertThatThrownBy(() -> complaintService.assignComplaint(1L, 21L, "coordIT"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Staff member does not belong to the department responsible for this complaint");

            verify(complaintRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cannot assign complaint to a user who is not a staff member")
        void cannotAssignToNonStaffUser() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));
            when(userRepository.findByUsername("coordIT")).thenReturn(Optional.of(coordinatorUser));
            when(userRepository.findById(10L)).thenReturn(Optional.of(reporterUser)); // Reporter, not staff

            assertThatThrownBy(() -> complaintService.assignComplaint(1L, 10L, "coordIT"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Assigned user is not a staff member");

            verify(complaintRepository, never()).save(any());
        }

        @Test
        @DisplayName("Reporter is denied when attempting to assign a complaint")
        void reporterCannotAssignComplaint() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));
            when(userRepository.findByUsername("reporter1")).thenReturn(Optional.of(reporterUser));

            assertThatThrownBy(() -> complaintService.assignComplaint(1L, 20L, "reporter1"))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only coordinators and admins can assign complaints");
        }

        @Test
        @DisplayName("Admin can assign complaint to staff in responsible department")
        void adminCanAssignComplaint() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));
            when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminUser));
            when(userRepository.findById(20L)).thenReturn(Optional.of(staffUser));
            when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ComplaintResponse response = complaintService.assignComplaint(1L, 20L, "adminUser");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("Assigned");
            verify(complaintRepository).save(c1);
        }
    }

    @Nested
    @DisplayName("Complaint Status Update & Transitions Tests")
    class ComplaintStatusUpdateTests {

        @Test
        @DisplayName("Assigned staff member can update complaint status to In Progress")
        void assignedStaffCanUpdateStatusToInProgress() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, staffUser, "Assigned");
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));
            when(userRepository.findByUsername("staffIT")).thenReturn(Optional.of(staffUser));
            when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ComplaintResponse response = complaintService.updateComplaintStatus(1L, "In Progress", "Parts ordered", "staffIT");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("In Progress");
            verify(complaintRepository).save(c1);
            verify(historyRepository).save(any(ComplaintHistory.class));
        }

        @Test
        @DisplayName("Assigned staff member can update complaint status to Resolved")
        void assignedStaffCanUpdateStatusToResolved() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, staffUser, "In Progress");
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));
            when(userRepository.findByUsername("staffIT")).thenReturn(Optional.of(staffUser));
            when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ComplaintResponse response = complaintService.updateComplaintStatus(1L, "Resolved", "Replaced cable", "staffIT");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("Resolved");
        }

        @Test
        @DisplayName("Issue 2: Staff cannot update status of complaint not assigned to them")
        void staffCannotUpdateUnassignedComplaint() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted"); // Not assigned
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));
            when(userRepository.findByUsername("staffIT")).thenReturn(Optional.of(staffUser));

            assertThatThrownBy(() -> complaintService.updateComplaintStatus(1L, "In Progress", "Work started", "staffIT"))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("You can only update complaints assigned to you");

            verify(complaintRepository, never()).save(any());
        }

        @Test
        @DisplayName("Coordinator can update status of complaint in their department")
        void coordinatorCanUpdateDepartmentComplaintStatus() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, staffUser, "Resolved");
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));
            when(userRepository.findByUsername("coordIT")).thenReturn(Optional.of(coordinatorUser));
            when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ComplaintResponse response = complaintService.updateComplaintStatus(1L, "Closed", "Verified resolution", "coordIT");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("Closed");
        }

        @Test
        @DisplayName("Issue 3: Coordinator cannot update status of complaint outside their department")
        void coordinatorCannotUpdateComplaintOutsideDepartment() {
            Complaint c2 = createSampleComplaint(2L, otherReporterUser, deptCivil, null, "Submitted");
            when(complaintRepository.findById(2L)).thenReturn(Optional.of(c2));
            when(userRepository.findByUsername("coordIT")).thenReturn(Optional.of(coordinatorUser));

            assertThatThrownBy(() -> complaintService.updateComplaintStatus(2L, "Closed", "Closing", "coordIT"))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Complaint is outside your department");

            verify(complaintRepository, never()).save(any());
        }

        @Test
        @DisplayName("Reporter is denied when attempting to update complaint status")
        void reporterCannotUpdateStatus() {
            Complaint c1 = createSampleComplaint(1L, reporterUser, deptIT, null, "Submitted");
            when(complaintRepository.findById(1L)).thenReturn(Optional.of(c1));
            when(userRepository.findByUsername("reporter1")).thenReturn(Optional.of(reporterUser));

            assertThatThrownBy(() -> complaintService.updateComplaintStatus(1L, "Resolved", "Self resolved", "reporter1"))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Reporters cannot update complaint status");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when status transition has invalid status string")
        void shouldRejectInvalidStatusString() {
            assertThatThrownBy(() -> complaintService.updateComplaintStatus(1L, "InvalidStatus", "remarks", "staffIT"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid status");
        }
    }
}

