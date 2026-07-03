import puppeteer from 'puppeteer';

const delay = ms => new Promise(res => setTimeout(res, ms));

(async () => {
  console.log("Launching visible browser for UI testing...");
  const browser = await puppeteer.launch({ 
    headless: false, 
    defaultViewport: null,
    args: ['--start-maximized'] 
  });
  
  const page = await browser.newPage();
  const timestamp = Date.now();
  
  try {
    // 1. Register a Coordinator
    console.log("Registering Department Coordinator...");
    await page.goto('http://localhost:5173/register');
    await page.type('input[name="username"]', `coord_${timestamp}`);
    await page.type('input[name="email"]', `coord${timestamp}@test.com`);
    await page.type('input[name="password"]', `password123`);
    await page.type('input[name="phone"]', `1234567890`);
    await page.select('select[name="role"]', 'DEPARTMENT_COORDINATOR');
    await delay(1000); // Visual pause
    
    // Wait for departments to load and select IT
    await page.waitForSelector('select[name="departmentId"] option[value="1"]');
    await page.select('select[name="departmentId"]', '1');
    await delay(1000);
    
    await page.click('button[type="submit"]');
    await page.waitForNavigation();
    console.log("Coordinator registered!");

    // 2. Register a Reporter
    console.log("Registering Reporter...");
    await page.goto('http://localhost:5173/register');
    await page.type('input[name="username"]', `reporter_${timestamp}`);
    await page.type('input[name="email"]', `reporter${timestamp}@test.com`);
    await page.type('input[name="password"]', `password123`);
    await page.select('select[name="role"]', 'REPORTER');
    await delay(1000);
    await page.click('button[type="submit"]');
    await page.waitForNavigation();
    console.log("Reporter registered!");

    // 3. Reporter Log In
    console.log("Logging in as Reporter...");
    await page.type('input[name="username"]', `reporter_${timestamp}`);
    await page.type('input[name="password"]', `password123`);
    await delay(1000);
    await page.click('button[type="submit"]');
    await page.waitForNavigation();

    // 4. Reporter Creates Complaint
    console.log("Reporter navigating to New Complaint...");
    await page.goto('http://localhost:5173/complaints/new');
    await page.type('input[name="title"]', `Visual Test Complaint ${timestamp}`);
    await page.select('select[name="category"]', 'Electrical');
    await page.select('select[name="priority"]', 'High');
    await page.type('textarea[name="description"]', 'This was submitted automatically by the UI tester.');
    await page.type('input[name="roomNumber"]', 'Room 404');
    
    await page.waitForSelector('select[name="departmentId"] option[value="1"]');
    await page.select('select[name="departmentId"]', '1');
    await delay(1500);

    await page.click('button[type="submit"]');
    await page.waitForNavigation();
    console.log("Complaint created successfully!");
    
    await delay(2000); // Let user see the dashboard

    // 5. Coordinator Log In
    console.log("Logging in as Coordinator to verify routing...");
    await page.evaluate(() => localStorage.clear()); // Clear token to logout
    await page.goto('http://localhost:5173/login');
    await page.type('input[name="username"]', `coord_${timestamp}`);
    await page.type('input[name="password"]', `password123`);
    await delay(1000);
    await page.click('button[type="submit"]');
    await page.waitForNavigation();
    
    console.log("Coordinator viewing dashboard...");
    await delay(3000); // Wait on dashboard for user to see it
    
    console.log("Navigating to All Complaints...");
    await page.goto('http://localhost:5173/complaints');
    await delay(3000); // Let user see the complaint list

    console.log("✅ Visual UI testing completed successfully!");
    console.log("Closing browser in 5 seconds...");
    await delay(5000);

  } catch (err) {
    console.error("❌ Error during UI Test:", err);
  } finally {
    await browser.close();
  }
})();
