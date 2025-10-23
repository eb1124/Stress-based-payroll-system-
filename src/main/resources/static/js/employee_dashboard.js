let currentUser = null;
let stressChart = null;

document.addEventListener('DOMContentLoaded', function() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (!user.employeeId) {
        window.location.href = '/login.html';
        return;
    }
    currentUser = user;
    document.getElementById('employeeName').textContent = user.fullName || user.email;
    loadDashboardData();
    setupEventListeners();
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

    if (sectionName === 'payslips') {
        loadPayslips();
    } else if (sectionName === 'stress') {
        loadStressData();
    }
}

async function loadDashboardData() {
    try {
        const payslipsRes = await fetch('/api/employee/' + currentUser.employeeId + '/payslips');
        const payslips = await payslipsRes.json();
        
        const stressRes = await fetch('/api/employee/' + currentUser.employeeId + '/stress-entries');
        const stressEntries = await stressRes.json();

        document.getElementById('totalPayslips').textContent = payslips.length;
        
        if (stressEntries.length > 0) {
            const latest = stressEntries[0];
            document.getElementById('currentStress').textContent = latest.stressLevel + ' (' + latest.stressCategory + ')';
        }

        let totalPaper = 0;
        for (let i = 0; i < payslips.length; i++) {
            totalPaper += parseFloat(payslips[i].paperSavedGrams || 0);
        }
        document.getElementById('paperSaved').textContent = totalPaper.toFixed(2) + 'g';
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

function setupEventListeners() {
    const form = document.getElementById('payslipForm');
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const data = {
            month: parseInt(document.getElementById('month').value),
            year: parseInt(document.getElementById('year').value),
            overtimeHours: parseFloat(document.getElementById('overtimeHours').value),
            workingDays: parseInt(document.getElementById('workingDays').value),
            daysPresent: parseInt(document.getElementById('daysPresent').value),
            paidLeavesTaken: parseInt(document.getElementById('paidLeavesTaken').value),
            unpaidLeavesTaken: parseInt(document.getElementById('unpaidLeavesTaken').value)
        };

        try {
            const response = await fetch('/api/employee/' + currentUser.employeeId + '/generate-payslip', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const result = await response.json();
            
            if (response.ok) {
                displayPayslipResult(result);
            } else {
                alert('Error: ' + (result.error || 'Failed to generate payslip'));
            }
        } catch (error) {
            alert('An error occurred: ' + error.message);
        }
    });
}

function displayPayslipResult(result) {
    const resultDiv = document.getElementById('payslipResult');
    const payslip = result.payslip;
    const stressEntry = result.stressEntry;
    const psychologistContact = result.psychologistContact;
    
    const stressBadge = getStressBadge(stressEntry.stressCategory);
    
    let html = '<h3>Payslip Generated Successfully!</h3>';
    html += '<div style="margin-top: 1rem;"><h4>Salary Details</h4>';
    html += '<p><strong>Base Salary:</strong> Rs ' + payslip.baseSalary + '</p>';
    html += '<p><strong>Overtime Pay:</strong> Rs ' + payslip.overtimePay + '</p>';
    html += '<p><strong>Deductions:</strong> Rs ' + payslip.deductions + '</p>';
    html += '<p><strong>Net Salary:</strong> Rs ' + payslip.netSalary + '</p></div>';
    
    html += '<div style="margin-top: 1rem;"><h4>Stress Assessment</h4>';
    html += '<p><strong>Stress Level:</strong> ' + stressEntry.stressLevel + ' ' + stressBadge + '</p>';
    html += '<p><strong>Factors:</strong> ' + stressEntry.stressFactors + '</p>';
    html += '<p><strong>Recommendations:</strong> ' + stressEntry.recommendations + '</p>';
    
    if (stressEntry.requiresCounseling) {
        html += '<div style="background: #dc3545; color: white; padding: 1rem; border-radius: 6px; margin-top: 1rem;">';
        html += '<strong>IMPORTANT:</strong> Your stress level requires immediate attention. ';
        html += 'Please contact our psychologist at ' + psychologistContact + '</div>';
    }
    html += '</div>';
    
    html += '<div style="margin-top: 1rem;"><h4>Environmental Impact</h4>';
    html += '<p>By downloading this payslip digitally, you have saved:</p>';
    html += '<p><strong>' + payslip.paperSavedGrams + 'g</strong> of paper</p>';
    html += '<p><strong>' + payslip.treesSaved + '</strong> trees</p></div>';
    
    resultDiv.innerHTML = html;
    resultDiv.style.display = 'block';
    
    loadDashboardData();
}

async function loadPayslips() {
    try {
        const response = await fetch('/api/employee/' + currentUser.employeeId + '/payslips');
        const payslips = await response.json();
        
        const container = document.getElementById('payslipsList');
        
        if (payslips.length === 0) {
            container.innerHTML = '<p style="padding: 2rem; text-align: center;">No payslips generated yet.</p>';
            return;
        }
        
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        
        let html = '<table><thead><tr>';
        html += '<th>Month/Year</th><th>Base Salary</th><th>Overtime</th>';
        html += '<th>Deductions</th><th>Net Salary</th><th>Action</th></tr></thead><tbody>';
        
        for (let i = 0; i < payslips.length; i++) {
            const p = payslips[i];
            html += '<tr>';
            html += '<td>' + months[p.month - 1] + ' ' + p.year + '</td>';
            html += '<td>Rs ' + p.baseSalary + '</td>';
            html += '<td>Rs ' + p.overtimePay + '</td>';
            html += '<td>Rs ' + p.deductions + '</td>';
            html += '<td><strong>Rs ' + p.netSalary + '</strong></td>';
            html += '<td><button class="btn btn-primary btn-sm" onclick="downloadPayslip(\'' + p.id + '\')">Download</button></td>';
            html += '</tr>';
        }
        
        html += '</tbody></table>';
        container.innerHTML = html;
    } catch (error) {
        console.error('Error loading payslips:', error);
    }
}

async function downloadPayslip(payslipId) {
    try {
        const response = await fetch('/api/employee/payslip/' + payslipId + '/download', {
            method: 'PUT'
        });
        
        const result = await response.json();
        
        if (response.ok) {
            alert(result.message);
            loadDashboardData();
        }
    } catch (error) {
        console.error('Error downloading payslip:', error);
    }
}

async function loadStressData() {
    try {
        const response = await fetch('/api/employee/' + currentUser.employeeId + '/stress-entries');
        const entries = await response.json();
        
        if (entries.length === 0) {
            document.getElementById('stressAnalysis').innerHTML = '<p>No stress data available yet.</p>';
            return;
        }
        
        entries.reverse();
        
        const labels = [];
        const data = [];
        for (let i = 0; i < entries.length; i++) {
            labels.push(entries[i].month + '/' + entries[i].year);
            data.push(parseFloat(entries[i].stressLevel));
        }
        
        const ctx = document.getElementById('stressChart').getContext('2d');
        
        if (stressChart) {
            stressChart.destroy();
        }
        
        stressChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Stress Level',
                    data: data,
                    borderColor: '#0066cc',
                    backgroundColor: 'rgba(0, 102, 204, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: 'Monthly Stress Level Trend'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        title: {
                            display: true,
                            text: 'Stress Level'
                        }
                    }
                }
            }
        });
        
        const latest = entries[entries.length - 1];
        const analysis = document.getElementById('stressAnalysis');
        
        const badge = getStressBadge(latest.stressCategory);
        
        let html = '<h3>Latest Stress Analysis</h3>';
        html += '<p><strong>Month:</strong> ' + latest.month + '/' + latest.year + '</p>';
        html += '<p><strong>Stress Level:</strong> ' + latest.stressLevel + ' ' + badge + '</p>';
        html += '<p><strong>Contributing Factors:</strong></p><p>' + latest.stressFactors + '</p>';
        html += '<p><strong>Recommendations:</strong></p><p>' + latest.recommendations + '</p>';
        
        if (latest.requiresCounseling) {
            html += '<div style="background: #dc3545; color: white; padding: 1rem; border-radius: 6px; margin-top: 1rem;">';
            html += '<strong>URGENT:</strong> Please contact the office psychologist immediately.</div>';
        }
        
        analysis.innerHTML = html;
    } catch (error) {
        console.error('Error loading stress data:', error);
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
