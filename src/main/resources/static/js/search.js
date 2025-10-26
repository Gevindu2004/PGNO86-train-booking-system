// Search Page JavaScript

// Initialize search form functionality
function initializeSearchForm() {
  const searchForm = document.getElementById('train-search-form');
  if (!searchForm) return;

  // Add form validation
  searchForm.addEventListener('submit', function(e) {
    if (!validateSearchForm()) {
      e.preventDefault();
    }
  });

  // Add input enhancements
  const inputs = searchForm.querySelectorAll('input[type="text"], input[type="date"], input[type="time"]');
  inputs.forEach(input => {
    input.classList.add('search-input');
    
    // Add focus effects
    input.addEventListener('focus', function() {
      this.parentElement.classList.add('focused');
    });
    
    input.addEventListener('blur', function() {
      this.parentElement.classList.remove('focused');
    });
  });

  // Add search button styling
  const searchBtn = searchForm.querySelector('button[type="submit"]');
  if (searchBtn) {
    searchBtn.classList.add('search-btn');
  }
}

// Validate search form
function validateSearchForm() {
  const fromStation = document.querySelector('input[name="fromStation"]');
  const toStation = document.querySelector('input[name="toStation"]');
  
  if (!fromStation.value.trim()) {
    alert('Please enter the origin station');
    fromStation.focus();
    return false;
  }
  
  if (!toStation.value.trim()) {
    alert('Please enter the destination station');
    toStation.focus();
    return false;
  }
  
  if (fromStation.value.trim().toLowerCase() === toStation.value.trim().toLowerCase()) {
    alert('Origin and destination stations cannot be the same');
    fromStation.focus();
    return false;
  }
  
  return true;
}

// Enhance results table
function enhanceResultsTable() {
  const table = document.querySelector('table');
  if (!table) return;

  table.classList.add('results-table');

  // Add data labels for mobile responsiveness
  const headers = table.querySelectorAll('thead th');
  const rows = table.querySelectorAll('tbody tr');
  
  rows.forEach(row => {
    const cells = row.querySelectorAll('td');
    cells.forEach((cell, index) => {
      if (headers[index]) {
        cell.setAttribute('data-label', headers[index].textContent);
      }
    });
  });

  // Add hover effects to action buttons
  const actionButtons = table.querySelectorAll('a.btn');
  actionButtons.forEach(btn => {
    btn.classList.add('action-btn');
  });
}

// Add loading state to search button
function addLoadingState() {
  const searchBtn = document.querySelector('button[type="submit"]');
  if (!searchBtn) return;

  searchBtn.addEventListener('click', function() {
    this.innerHTML = '<span>Searching...</span>';
    this.disabled = true;
    
    // Re-enable after 3 seconds (in case of error)
    setTimeout(() => {
      this.innerHTML = 'Search';
      this.disabled = false;
    }, 3000);
  });
}

// Auto-fill current date if not provided
function autoFillCurrentDate() {
  const dateInput = document.querySelector('input[type="date"]');
  if (dateInput && !dateInput.value) {
    const today = new Date().toISOString().split('T')[0];
    dateInput.value = today;
  }
}

// Add keyboard shortcuts
function addKeyboardShortcuts() {
  document.addEventListener('keydown', function(e) {
    // Ctrl/Cmd + Enter to submit search
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      const searchForm = document.getElementById('train-search-form');
      if (searchForm) {
        searchForm.submit();
      }
    }
  });
}

// Initialize everything when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
  initializeSearchForm();
  enhanceResultsTable();
  addLoadingState();
  autoFillCurrentDate();
  addKeyboardShortcuts();
  
  // Add smooth scrolling for better UX
  const results = document.querySelector('.card:last-child');
  if (results) {
    results.scrollIntoView({ 
      behavior: 'smooth', 
      block: 'start' 
    });
  }
});

// Export functions for global access
window.validateSearchForm = validateSearchForm;
