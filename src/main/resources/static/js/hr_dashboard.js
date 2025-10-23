let currentUser = null;

document.addEventListener('DOMContentLoaded', function() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (user.role !== 'HR') {
        window.location.href = '/login.html';
        return;
    }
    currentUser = user;
    document.getElementById('hrName').textContent = user.email;
    loadOverviewData();
});

function logout() {
    localStorage.removeItem('user');
    window.location.href = '/login.html';
}

function showSection(sectionName) {
    document.querySelectorAll('.section').forEach(function(s) { s.classList.remove('active'); });
    document.getElementById(sectionName + '-section').classList.add('active');
    
    document.querySelectorAll('.sidebar-menu a').forEach(function(a) { a.classList.remove('active'); });
    event.target.classList.add('active');

    if (sectionName === 'employees') {
        loadEmployees();
    } else if (sectionName === 'analytics') {
        loadAnalytics();
    }
}

async function loadOverviewData() {
    try {
        const empRes = await fetch('/api/hr/employees');
        const employees = await empRes.json();
        
        const analyticsRes = await fetch('/api/hr/stress-analytics');
        const analytics = await analyticsRes.json();

        document.getElementById('totalEmployees').textContent = employees.length;
        document.getElementById('criticalStress').textContent = analytics.criticalStress || 0;
        document.getElementById('highStress').textContent = analytics.highStress || 0;
        document.getElementById('needCounseling').textContent = analytics.requiresCounseling || 0;
    } catch (error) {
        console.error('Error loading overview data:', error);
    }
}

async function loadEmployees() {
    try {
        const response = await fetch('/api/hr/employees');
        const employees = await response.json();
        
        const container = document.getElementById('employeesList');
        
        if (employees.length === 0) {
            container.innerHTML = '<p style="padding: 2rem; text-align: center;">No employees found.</p>';
            return;
        }
        
        let html = '<table><thead><tr>';
        html += '<th>Employee Code</th><th>Name</th><th>Department</th>';
        html += '<th>Designation</th><th>Base Salary</th><th>Action</th>';
        html += '</tr></thead><tbody>';
        
        for (let i = 0; i < employees.length; i++) {
            const emp = employees[i];
            html += '<tr>';
            html += '<td>' + emp.employeeCode + '</td>';
            html += '<td>' + emp.fullName + '</td>';
            html += '<td>' + emp.department + '</td>';
            html += '<td>' + emp.designation + '</td>';
            html += '<td>Rs ' + emp.baseSalary + '</td>';
            html += '<td><button class="btn btn-primary btn-sm" onclick="viewEmployeeDetail(\'' + emp.id + '\')">View Details</button></td>';
            html += '</tr>';
        }
        
        html += '</tbody></table>';
        container.innerHTML = html;
    } catch (error) {
        console.error('Error loading employees:', error);
    }
}

async function viewEmployeeDetail(employeeId) {
    try {
        const response = await fetch('/api/hr/employee/' + employeeId);
        const employee = await response.json();
        
        document.getElementById('detailEmployeeName').textContent = employee.fullName;
        
        const infoDiv = document.getElementById('detail-info');
        let html = '<h4>Employee Information</h4>';
        html += '<p><strong>Employee Code:</strong> ' + employee.employeeCode + '</p>';
        html += '<p><strong>Email:</strong> ' + employee.userId + '</p>';
        html += '<p><strong>Department:</strong> ' + employee.department + '</p>';
        html += '<p><strong>Designation:</strong> ' + employee.designation + '</p>';
        html += '<p><strong>Base Salary:</strong> Rs ' + employee.baseSalary + '</p>';
        html += '<p><strong>Paid Leaves:</strong> ' + employee.paidLeaves + '</p>';
        html += '<p><strong>Join Date:</strong> ' + employee.joinDate + '</p>';
        html += '<p><strong>Status:</strong> ' + (employee.isActive ? 'Active' : 'Inactive') + '</p>';
        infoDiv.innerHTML = html;
        
        loadEmployeePayslips(employeeId);
        loadEmployeeStress(employeeId);
        
        document.querySelectorAll('.section').forEach(function(s) { s.classList.remove('active'); });
        document.getElementById('employee-detail-section').classList.add('active');
    } catch (error) {
        console.error('Error loading employee detail:', error);
    }
}

function showDetailTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(function(b) { b.classList.remove('active'); });
    document.querySelectorAll('.tab-content').forEach(function(c) { c.classList.remove('active'); });
    
    event.target.classList.add('active');
    document.getElementById('detail-' + tabName).classList.add('active');
}

async function loadEmployeePayslips(employeeId) {
    try {
        const response = await fetch('/api/hr/employee/' + employeeId + '/payslips');
        const payslips = await response.json();
        
        const container = document.getElementById('detail-payslips');
        
        if (payslips.length === 0) {
            container.innerHTML = '<p>No payslips found.</p>';
            return;
        }
        
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        
        let html = '<h4>Payslip History</h4>';
        html += '<table><thead><tr>';
        html += '<th>Month/Year</th><th>Base Salary</th><th>Overtime</th>';
        html += '<th>Deductions</th><th>Net Salary</th><th>Generated</th>';
        html += '</tr></thead><tbody>';
        
        for (let i = 0; i < payslips.length; i++) {
            const p = payslips[i];
            html += '<tr>';
            html += '<td>' + months[p.month - 1] + ' ' + p.year + '</td>';
            html += '<td>Rs ' + p.baseSalary + '</td>';
            html += '<td>Rs ' + p.overtimePay + '</td>';
            html += '<td>Rs ' + p.deductions + '</td>';
            html += '<td><strong>Rs ' + p.netSalary + '</strong></td>';
            html += '<td>' + new Date(p.generatedAt).toLocaleDateString() + '</td>';
            html += '</tr>';
        }
        
        html += '</tbody></table>';
        container.innerHTML = html;
    } catch (error) {
        console.error('Error loading payslips:', error);
    }
}

async function loadEmployeeStress(employeeId) {
    try {
        const response = await fetch('/api/hr/employee/' + employeeId + '/stress-entries');
        const entries = await response.json();
        
        const container = document.getElementById('detail-stress');
        
        if (entries.length === 0) {
            container.innerHTML = '<p>No stress data available.</p>';
            return;
        }
        
        let html = '<h4>Stress History</h4>';
        html += '<table><thead><tr>';
        html += '<th>Month/Year</th><th>Stress Level</th><th>Category</th>';
        html += '<th>Overtime Hours</th><th>Counseling</th><th>Factors</th>';
        html += '</tr></thead><tbody>';
        
        for (let i = 0; i < entries.length; i++) {
            const e = entries[i];
            const badge = getStressBadge(e.stressCategory);
            html += '<tr>';
            html += '<td>' + e.month + '/' + e.year + '</td>';
            html += '<td>' + e.stressLevel + '</td>';
            html += '<td>' + badge + '</td>';
            html += '<td>' + e.overtimeHours + ' hrs</td>';
            html += '<td>' + (e.requiresCounseling ? '<span class="badge badge-critical">Required</span>' : 'No') + '</td>';
            html += '<td>' + e.stressFactors + '</td>';
            html += '</tr>';
        }
        
        html += '</tbody></table>';
        container.innerHTML = html;
    } catch (error) {
        console.error('Error loading stress data:', error);
    }
}

async function loadAnalytics() {
    try {
        const response = await fetch('/api/hr/stress-analytics');
        const analytics = await response.json();
        
        const ctx = document.getElementById('analyticsChart').getContext('2d');
        
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Critical Stress', 'High Stress', 'Need Counseling', 'Total Entries'],
                datasets: [{
                    label: 'Count',
                    data: [
                        analytics.criticalStress || 0,
                        analytics.highStress || 0,
                        analytics.requiresCounseling || 0,
                        analytics.totalEntries || 0
                    ],
                    backgroundColor: [
                        '#dc3545',
                        '#ffc107',
                        '#28a745',
                        '#0066cc'
                    ]
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: 'Organization-wide Stress Analytics'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading analytics:', error);
    }
}

function getStressBadge(category) {
    const badges = {
        'LOW': '<span class="badge badge-low">LOW</span>',
        'MODERATE': '<span class="badge badge-moderate">MODERATE</span>',
        'HIGH': '<span class="badge badge-high">HIGH</span>',
        'CRITICAL': '<span class="badge badge-critical">CRITICAL</span>'
    };
    return badges[category] || '';
}
