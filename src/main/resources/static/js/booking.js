let selectedCoach = null;

function selectCoach(coachCode) {
  console.log('Selecting coach:', coachCode);
  selectedCoach = coachCode;

  // Update UI to show seat selection
  document.getElementById('coach-selection').style.display = 'none';
  document.getElementById('seat-selection').style.display = 'block';
  document.getElementById('selected-coach-name').textContent = 'in Coach ' + coachCode;
  document.getElementById('back-to-coaches-btn').style.display = 'block';

  // Filter and show seats for the selected coach
  filterSeatsByCoach(coachCode);

  // Update coach button states
  updateCoachButtonStates(coachCode);
}

// Filter seats by coach
// @param {string} coachCode - The code of the coach to filter seats for
// @description Hides or shows seats based on the selected coach
function filterSeatsByCoach(coachCode) {
  const allSeats = document.querySelectorAll('.seat-grid .seat');
  console.log('Filtering seats for coach:', coachCode);

  allSeats.forEach(seat => {
    const seatCoach = seat.getAttribute('data-coach');
    if (seatCoach === coachCode) {
      seat.style.display = 'block';
    } else {
      seat.style.display = 'none';
    }
  });
}

// Update coach button states
// @param {string} selectedCoach - The currently selected coach code
// @description Applies or removes the 'selected' class to coach buttons
function updateCoachButtonStates(selectedCoach) {
  document.querySelectorAll('.coach-btn').forEach(btn => {
    if (btn.getAttribute('data-coach') === selectedCoach) {
      btn.classList.add('selected');
    } else {
      btn.classList.remove('selected');
    }
  });
}

// Go back to coach selection
// @description Resets the UI to show all coaches and hides seat selection
function backToCoachSelection() {
  document.getElementById('coach-selection').style.display = 'block';
  document.getElementById('seat-selection').style.display = 'block';
  document.getElementById('selected-coach-name').textContent = '';
  document.getElementById('back-to-coaches-btn').style.display = 'none';
  selectedCoach = null;

  // Show all seats again
  document.querySelectorAll('.seat-grid .seat').forEach(seat => {
    seat.style.display = 'block';
  });

  // Reset coach button states
  document.querySelectorAll('.coach-btn').forEach(btn => {
    btn.classList.remove('selected');
  });
}

// Clear all seat selections
// @description Unchecks all selectable seat checkboxes and updates their state
function clearSelection() {
  document.querySelectorAll('.seat-grid .seat input[type="checkbox"]').forEach(checkbox => {
    if (!checkbox.disabled) {
      checkbox.checked = false;
      checkbox.dispatchEvent(new Event('change', { bubbles: true }));
    }
  });
}

// Sync selected class with checkbox state
// @param {HTMLInputElement} checkbox - The checkbox element to sync
// @description Toggles the 'selected' class on the parent seat element based on checkbox state
function syncSelectedClass(checkbox) {
  const seatElement = checkbox.closest('.seat');
  if (!seatElement) return;

  if (checkbox.checked) {
    seatElement.classList.add('selected');
  } else {
    seatElement.classList.remove('selected');
  }
  
}

// Initialize seat selection functionality
// @description Sets up initial checkbox states and event listeners for seat selection
function initializeSeatSelection() {
  // Add event listeners to all seat checkboxes
  document.querySelectorAll('.seat-grid .seat input[type="checkbox"]').forEach(checkbox => {
    // Set initial state
    syncSelectedClass(checkbox);

    // Add change event listener
    checkbox.addEventListener('change', function () {
      syncSelectedClass(this);
    });
  });
}

// Form validation
// @returns {boolean} - True if form is valid, false otherwise
// @description Checks if selected seats are provided
function validateBookingForm() {
  const selectedSeats = document.querySelectorAll('.seat-grid .seat input[type="checkbox"]:checked');

  if (selectedSeats.length === 0) {
    alert('Please select at least one seat');
    return false;
  }

  return true;
}

// Handle booking form submission - redirect to passenger login
// @description Redirects to passenger login page instead of submitting form directly
function handleBookingSubmission() {
  const form = document.getElementById('booking-form') || document.querySelector('form');
  console.log('Form found:', form);
  if (form) {
    form.addEventListener('submit', function (e) {
      console.log('Form submit event triggered');
      e.preventDefault(); // Prevent default form submission

      // Validate that seats are selected
      if (!validateBookingForm()) {
        return;
      }

      // Get selected seats and schedule ID
      const selectedSeats = Array.from(document.querySelectorAll('.seat-grid .seat input[type="checkbox"]:checked'))
        .map(checkbox => checkbox.value);
      const scheduleId = document.querySelector('input[name="scheduleId"]').value;

      console.log('Selected seats:', selectedSeats);
      console.log('Schedule ID:', scheduleId);

      // Validate data before storing
      if (!scheduleId) {
        alert('Schedule ID is missing. Please try again.');
        return;
      }

      if (selectedSeats.length === 0) {
        alert('Please select at least one seat.');
        return;
      }

      // Store booking data in localStorage for after login
      const bookingData = {
        scheduleId: scheduleId,
        seatIds: selectedSeats,
        timestamp: new Date().getTime()
      };

      console.log('Storing booking data:', bookingData);
      localStorage.setItem('pendingBooking', JSON.stringify(bookingData));

      // Redirect to passenger login
      window.location.href = '/login';
    });
  }
}

// Initialize everything when DOM is loaded
// @description Sets up all initial functionality when the page loads
document.addEventListener('DOMContentLoaded', function () {
  console.log('DOM loaded, initializing booking functionality');
  initializeSeatSelection();
  handleBookingSubmission();

  // Add smooth scrolling for better UX
  document.querySelectorAll('.seat').forEach(seat => {
    seat.addEventListener('click', function () {
      const input = this.querySelector('input[type="checkbox"]');
      if (input && !input.disabled) {
        this.scrollIntoView({
          block: 'nearest',
          behavior: 'smooth'
        });
      }
    });
  });
});

// Handle book button click directly
// @description Handles the book button click to submit the booking form
function handleBookClick() {
  console.log('Book button clicked');

  // Validate that seats are selected
  if (!validateBookingForm()) {
    return;
  }

  // Get selected seats and schedule ID
  const selectedSeats = Array.from(document.querySelectorAll('.seat-grid .seat input[type="checkbox"]:checked'))
    .map(checkbox => checkbox.value);
  const scheduleId = document.querySelector('input[name="scheduleId"]').value;

  console.log('Selected seats:', selectedSeats);
  console.log('Schedule ID:', scheduleId);

  // Validate data before submitting
  if (!scheduleId) {
    alert('Schedule ID is missing. Please try again.');
    return;
  }

  if (selectedSeats.length === 0) {
    alert('Please select at least one seat.');
    return;
  }

  // Create hidden inputs for seat IDs
  const form = document.getElementById('booking-form');
  
  // Remove any existing seatId inputs
  const existingSeatInputs = form.querySelectorAll('input[name="seatIds"]');
  existingSeatInputs.forEach(input => input.remove());
  
  // Add seat IDs as hidden inputs
  selectedSeats.forEach(seatId => {
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'seatIds';
    input.value = seatId;
    form.appendChild(input);
  });

  console.log('Submitting booking form with seats:', selectedSeats);
  
  // Submit the form to store booking data in session
  form.submit();
}


// Export functions for global access
// @description Makes key functions available globally for potential reuse
window.clearSelection = clearSelection;
window.syncSelectedClass = syncSelectedClass;
window.handleBookClick = handleBookClick;
