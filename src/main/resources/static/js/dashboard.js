// Dashboard Page JavaScript

// Initialize dashboard functionality
function initializeDashboard() {
  addCardAnimations();
  addQuickActions();
  addRealTimeUpdates();
  addChartAnimations();
  addResponsiveHandling();
}

// Add card animations
function addCardAnimations() {
  const cards = document.querySelectorAll('.dashboard-card');
  
  cards.forEach((card, index) => {
    card.style.opacity = '0';
    card.style.transform = 'translateY(20px)';
    
    setTimeout(() => {
      card.style.transition = 'all 0.6s ease';
      card.style.opacity = '1';
      card.style.transform = 'translateY(0)';
    }, index * 100);
  });
}

// Add quick actions functionality
function addQuickActions() {
  const quickActions = document.querySelectorAll('.quick-action');
  
  quickActions.forEach(action => {
    action.addEventListener('click', function(e) {
      // Add click animation
      this.style.transform = 'scale(0.95)';
      setTimeout(() => {
        this.style.transform = 'scale(1)';
      }, 150);
    });
  });
}

// Add real-time updates simulation
function addRealTimeUpdates() {
  const statValues = document.querySelectorAll('.stat-value');
  
  // Simulate real-time updates every 30 seconds
  setInterval(() => {
    statValues.forEach(stat => {
      if (stat.textContent.includes('$') || stat.textContent.includes('%')) {
        // Add a subtle pulse animation
        stat.style.animation = 'pulse 0.5s ease';
        setTimeout(() => {
          stat.style.animation = '';
        }, 500);
      }
    });
  }, 30000);
}

// Add chart animations (if charts are present)
function addChartAnimations() {
  const charts = document.querySelectorAll('.chart, canvas');
  
  charts.forEach(chart => {
    chart.style.opacity = '0';
    chart.style.transform = 'scale(0.8)';
    
    setTimeout(() => {
      chart.style.transition = 'all 0.8s ease';
      chart.style.opacity = '1';
      chart.style.transform = 'scale(1)';
    }, 500);
  });
}

// Add responsive handling
function addResponsiveHandling() {
  const dashboardGrid = document.querySelector('.dashboard-grid');
  if (!dashboardGrid) return;
  
  function handleResize() {
    const width = window.innerWidth;
    
    if (width < 768) {
      dashboardGrid.style.gridTemplateColumns = '1fr';
    } else if (width < 1024) {
      dashboardGrid.style.gridTemplateColumns = 'repeat(2, 1fr)';
    } else {
      dashboardGrid.style.gridTemplateColumns = 'repeat(auto-fit, minmax(300px, 1fr))';
    }
  }
  
  window.addEventListener('resize', handleResize);
  handleResize(); // Initial call
}

// Add hover effects to cards
function addCardHoverEffects() {
  const cards = document.querySelectorAll('.dashboard-card');
  
  cards.forEach(card => {
    card.addEventListener('mouseenter', function() {
      this.style.transform = 'translateY(-5px) scale(1.02)';
    });
    
    card.addEventListener('mouseleave', function() {
      this.style.transform = 'translateY(0) scale(1)';
    });
  });
}

// Add loading states
function addLoadingStates() {
  const buttons = document.querySelectorAll('.btn-primary, .btn-secondary');
  
  buttons.forEach(button => {
    button.addEventListener('click', function() {
      const originalText = this.textContent;
      this.textContent = 'Loading...';
      this.disabled = true;
      
      // Simulate loading
      setTimeout(() => {
        this.textContent = originalText;
        this.disabled = false;
      }, 2000);
    });
  });
}

// Add keyboard navigation
function addKeyboardNavigation() {
  document.addEventListener('keydown', function(e) {
    // Alt + 1-9 to focus on cards
    if (e.altKey && e.key >= '1' && e.key <= '9') {
      const cardIndex = parseInt(e.key) - 1;
      const cards = document.querySelectorAll('.dashboard-card');
      if (cards[cardIndex]) {
        cards[cardIndex].focus();
        cards[cardIndex].scrollIntoView({ behavior: 'smooth' });
      }
    }
  });
}

// Add refresh functionality
function addRefreshFunctionality() {
  const refreshButton = document.querySelector('[data-action="refresh"]');
  if (refreshButton) {
    refreshButton.addEventListener('click', function() {
      this.style.animation = 'spin 1s linear infinite';
      
      // Simulate refresh
      setTimeout(() => {
        this.style.animation = '';
        location.reload();
      }, 1000);
    });
  }
}

// Add data export functionality
function addDataExport() {
  const exportButton = document.querySelector('[data-action="export"]');
  if (exportButton) {
    exportButton.addEventListener('click', function() {
      // Simulate data export
      const data = {
        timestamp: new Date().toISOString(),
        dashboard: 'train-management'
      };
      
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'dashboard-data.json';
      a.click();
      URL.revokeObjectURL(url);
    });
  }
}

// Initialize everything when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
  initializeDashboard();
  addCardHoverEffects();
  addLoadingStates();
  addKeyboardNavigation();
  addRefreshFunctionality();
  addDataExport();
});

// Add CSS animations
const style = document.createElement('style');
style.textContent = `
  @keyframes pulse {
    0% { transform: scale(1); }
    50% { transform: scale(1.05); }
    100% { transform: scale(1); }
  }
  
  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
  
  .dashboard-card:focus {
    outline: 2px solid #22c55e;
    outline-offset: 2px;
  }
`;
document.head.appendChild(style);
