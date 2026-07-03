import fs from 'fs';

const API_BASE = 'http://localhost:8080/api';

async function request(endpoint, method = 'GET', body = null, token = null, isFormData = false) {
    const headers = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    if (!isFormData && body) headers['Content-Type'] = 'application/json';

    const options = { method, headers };
    if (body) {
        if (isFormData) {
            // Very simplified form data mock since native fetch FormData in Node requires careful handling 
            // Better to use URLSearchParams for simple URL encoded or just skip images for testing
        } else {
            options.body = JSON.stringify(body);
        }
    }

    const res = await fetch(`${API_BASE}${endpoint}`, options);
    if (!res.ok) {
        const text = await res.text();
        throw new Error(`HTTP ${res.status}: ${text}`);
    }
    return await res.json();
}

async function runTest() {
    console.log("Starting End-to-End API Test...");
    
    try {
        // 1. Fetch Departments
        console.log("1. Fetching departments...");
        const depts = await request('/departments');
        const itDept = depts.find(d => d.name === 'IT') || depts[0];
        console.log(`Selected Department: ${itDept.name} (ID: ${itDept.id})`);

        // 2. Register Users
        const timestamp = Date.now();
        console.log("2. Registering users...");
        
        await request('/auth/register', 'POST', {
            username: `coord_${timestamp}`, email: `coord${timestamp}@test.com`, 
            password: 'password123', role: 'DEPARTMENT_COORDINATOR', departmentId: itDept.id
        });
        
        await request('/auth/register', 'POST', {
            username: `staff_${timestamp}`, email: `staff${timestamp}@test.com`, 
            password: 'password123', role: 'STAFF', departmentId: itDept.id
        });
        
        await request('/auth/register', 'POST', {
            username: `reporter_${timestamp}`, email: `reporter${timestamp}@test.com`, 
            password: 'password123', role: 'REPORTER'
        });
        
        console.log("Users registered successfully.");

        // 3. Logins
        console.log("3. Logging in users...");
        const { token: coordToken } = await request('/auth/login', 'POST', { username: `coord_${timestamp}`, password: 'password123' });
        const { token: staffToken, id: staffId } = await request('/auth/login', 'POST', { username: `staff_${timestamp}`, password: 'password123' });
        const { token: reporterToken } = await request('/auth/login', 'POST', { username: `reporter_${timestamp}`, password: 'password123' });

        // 4. Reporter creates complaint
        console.log("4. Reporter creating a complaint...");
        const formData = new FormData();
        formData.append('title', 'Projector Broken');
        formData.append('description', 'The projector is not turning on.');
        formData.append('category', 'Electrical');
        formData.append('priority', 'High');
        formData.append('departmentId', itDept.id);
        formData.append('roomNumber', 'Room 404');
        
        const createRes = await fetch(`${API_BASE}/complaints`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${reporterToken}` },
            body: formData
        });
        const complaint = await createRes.json();
        console.log(`Complaint created: ID ${complaint.id}`);

        // 5. Coordinator assigns complaint
        console.log("5. Coordinator fetching and assigning complaint...");
        const coordComplaints = await request('/complaints', 'GET', null, coordToken);
        console.log(`Coordinator sees ${coordComplaints.length} complaint(s) for IT department.`);
        
        if (coordComplaints.length === 0) throw new Error("Coordinator didn't see the complaint!");
        
        const assignRes = await fetch(`${API_BASE}/complaints/${complaint.id}/assign?staffId=${staffId}`, {
            method: 'PATCH',
            headers: { 'Authorization': `Bearer ${coordToken}` }
        });
        const assignedComplaint = await assignRes.json();
        console.log(`Complaint assigned successfully. Status is now: ${assignedComplaint.status}`);

        // 6. Staff updates status
        console.log("6. Staff fetching and updating complaint...");
        const staffComplaints = await request('/complaints', 'GET', null, staffToken);
        console.log(`Staff sees ${staffComplaints.length} assigned complaint(s).`);
        
        const updateRes = await fetch(`${API_BASE}/complaints/${complaint.id}/status?status=In Progress&remarks=Fixing%20it%20now`, {
            method: 'PATCH',
            headers: { 'Authorization': `Bearer ${staffToken}` }
        });
        const updatedComplaint = await updateRes.json();
        console.log(`Staff updated complaint status to: ${updatedComplaint.status}`);

        console.log("\n✅ ALL TESTS PASSED SUCCESSFULLY!");
    } catch (err) {
        console.error("❌ TEST FAILED:", err);
    }
}

runTest();
