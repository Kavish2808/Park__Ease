const API_URL = 'http://localhost:8080/api';
const api = axios.create({ baseURL: API_URL });
const $ = selector => document.querySelector(selector);
const list = $('#parking-list');
const message = $('#results-message');
const clear = $('#clear-filter');
const bookingModal = $('#booking-modal');
const modalContent = $('#modal-content');
const authModal = $('#auth-modal');
const listingModal = $('#listing-modal');
const OPEN_HOUR = 6;
const CLOSE_HOUR = 23;
const MAX_HOURS = 12;
let spaces = [];
let authMode = 'login';
let pendingSpace = null;
let booking = null;
let editingSpaceId = null;
let dashTab = null;
let dashData = { mine: [], received: [], spots: [] };

api.interceptors.request.use(config => {
  const token = sessionStorage.getItem('parkeaseToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
api.interceptors.response.use(response => response, error => {
  if (error.response?.status === 401 && sessionStorage.getItem('parkeaseToken') && !error.config.url.startsWith('/auth/')) {
    sessionStorage.clear();
    updateAccountUI();
    toast('Your session has expired. Please log in again.');
  }
  return Promise.reject(error);
});

function apiError(error, fallback) {
  return error.response?.data?.message || (error.response ? fallback : 'Could not reach the ParkEase API. Is the backend running?');
}

function esc(value) {
  return String(value ?? '').replace(/[&<>"']/g, ch => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[ch]);
}

const money = amount => `₹${Number(amount).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;
const pad = n => String(n).padStart(2, '0');
const hourLabel = hour => `${pad(hour % 24)}:00`;
const fmtDate = date => date.toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
const fmtTime = date => `${pad(date.getHours())}:${pad(date.getMinutes())}`;
const isoDate = date => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
const hoursBetween = (start, end) => Math.round((new Date(end) - new Date(start)) / 36e5 * 10) / 10;
const currentUser = () => JSON.parse(sessionStorage.getItem('parkeaseUser') || 'null');
const plural = (n, word) => `${n} ${word}${n === 1 ? '' : 's'}`;
const isOwner = user => user?.role === 'OWNER' || user?.role === 'ADMIN';

let toastTimer;
function toast(text) {
  const el = $('#toast');
  el.textContent = text;
  el.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { el.hidden = true; }, 3500);
}

function updateAccountUI() {
  const user = currentUser();
  $('.login-button').textContent = user ? 'Log out' : 'Log in';
  $('#greeting').textContent = user ? `Hi, ${user.name.split(' ')[0]}` : '';
  $('#dashboard-link').hidden = !user;
  loadDashboard();
}

/* ---------- Search ---------- */

function render(items) {
  list.innerHTML = items.length ? items.map(space => `
    <article class="parking-card">
      <div class="card-image" style="--card-color:${space.imageUrl ? '#aecbd1' : '#cde566'}">${space.imageUrl ? `<img class="card-photo" src="${esc(space.imageUrl)}" alt="${esc(space.name)}" loading="lazy" onerror="this.remove()">` : ''}<div class="card-illustration"></div><span class="spot-label">${esc(space.vehicleType)} friendly</span></div>
      <div class="card-content"><div class="card-top"><h3>${esc(space.name)}</h3><span class="rating">★ New</span></div><p class="address">${esc(space.area)}, ${esc(space.city)}<br><small>${esc(space.address)}</small></p><div class="card-bottom"><span class="price">${money(space.pricePerHour)} <small>/ hour</small></span><button class="book-button" data-id="${space.id}" type="button">Book a slot</button></div></div>
    </article>`).join('') : '<p class="empty">No spaces match those details yet. Try another city, area or vehicle type.</p>';
}

async function loadSpaces(scroll = false) {
  const city = $('#city').value.trim();
  const vehicleType = $('#vehicle').value;
  message.textContent = 'Finding available parking…';
  list.innerHTML = '<p class="empty">Loading parking spaces…</p>';
  try {
    const { data } = await api.get('/parking-spots', { params: { city, ...(vehicleType !== 'all' && { vehicleType }), size: 12, sort: $('#sort').value } });
    spaces = data.content || [];
    const total = data.totalElements ?? spaces.length;
    message.textContent = `Found ${total} space${total === 1 ? '' : 's'}${city ? ` matching “${city}”` : ''}`;
    clear.hidden = !city && vehicleType === 'all';
    render(spaces);
    if (scroll) $('#spaces').scrollIntoView({ behavior: 'smooth', block: 'start' });
  } catch (error) {
    message.textContent = 'Could not reach the ParkEase API.';
    list.innerHTML = `<p class="empty">${esc(apiError(error, 'Start the Spring Boot backend at http://localhost:8080 and try again.'))}</p>`;
  }
}

/* ---------- Time-slot booking ---------- */

function slotDate(hour) {
  const [y, m, d] = booking.date.split('-').map(Number);
  return new Date(y, m - 1, d, hour);
}

function slotState(hour) {
  const start = slotDate(hour);
  const end = slotDate(hour + 1);
  if (start <= new Date()) return 'past';
  if (booking.booked.some(b => new Date(b.startTime) < end && new Date(b.endTime) > start)) return 'booked';
  return 'free';
}

function maxHoursFrom(hour) {
  let hours = 0;
  while (hour + hours < CLOSE_HOUR && hours < MAX_HOURS && slotState(hour + hours) === 'free') hours++;
  return hours;
}

function openBooking(space) {
  if (!sessionStorage.getItem('parkeaseToken')) { pendingSpace = space; openAuth('Log in to book this space.'); return; }
  booking = { space, date: $('#date').value || isoDate(new Date()), booked: [], start: null, hours: 1 };
  modalContent.innerHTML = `<div class="booking-content">
    <p class="eyebrow">RESERVE A TIME SLOT</p><h2>${esc(space.name)}</h2>
    <p class="address">${esc(space.address)} · ${esc(space.area)}, ${esc(space.city)} · <strong>${money(space.pricePerHour)}/hour</strong></p>
    <div class="slot-toolbar"><label>Date <input id="slot-date" type="date" min="${isoDate(new Date())}" value="${booking.date}" /></label><label>Duration <select id="slot-duration" disabled></select></label></div>
    <div class="legend"><span><i class="free"></i>Available</span><span><i class="selected"></i>Your slot</span><span><i class="booked"></i>Booked</span><span><i class="past"></i>Past</span></div>
    <div id="slot-grid" class="slot-grid" role="group" aria-label="Start time"></div>
    <div id="slot-summary" class="booking-summary"></div>
    <p id="booking-error" class="form-error" aria-live="polite"></p>
    <button class="primary-button modal-action" id="confirm-booking" type="button" disabled>Confirm booking <span>→</span></button></div>`;
  bookingModal.showModal();
  $('#slot-date').addEventListener('change', event => { booking.date = event.target.value; booking.start = null; loadSlots(); });
  $('#slot-grid').addEventListener('click', event => {
    const button = event.target.closest('[data-hour]');
    if (!button || button.disabled) return;
    booking.start = Number(button.dataset.hour);
    booking.hours = Math.max(1, Math.min(booking.hours, maxHoursFrom(booking.start)));
    renderSlots();
  });
  $('#slot-duration').addEventListener('change', event => { booking.hours = Number(event.target.value); renderSlots(); });
  $('#confirm-booking').addEventListener('click', confirmBooking);
  loadSlots(Number($('#time').value.slice(0, 2)));
}

async function loadSlots(preferredHour) {
  const grid = $('#slot-grid');
  if (!booking.date) { grid.innerHTML = '<p class="empty">Choose a date.</p>'; return; }
  grid.innerHTML = '<p class="empty">Checking availability…</p>';
  try {
    const { data } = await api.get(`/parking-spots/${booking.space.id}/booked-slots`, { params: { from: slotDate(0).toISOString(), to: slotDate(24).toISOString() } });
    booking.booked = data;
    if (preferredHour >= OPEN_HOUR && preferredHour < CLOSE_HOUR && slotState(preferredHour) === 'free') booking.start = preferredHour;
    renderSlots();
  } catch (error) {
    grid.innerHTML = `<p class="empty">${esc(apiError(error, 'Could not load time slots.'))}</p>`;
  }
}

function renderSlots() {
  const { start, hours } = booking;
  const hoursList = Array.from({ length: CLOSE_HOUR - OPEN_HOUR }, (_, i) => OPEN_HOUR + i);
  $('#slot-grid').innerHTML = hoursList.map(hour => {
    const state = slotState(hour);
    const selected = start !== null && hour >= start && hour < start + hours;
    return `<button type="button" class="slot ${selected ? 'selected' : state}" data-hour="${hour}" ${state === 'free' ? '' : 'disabled'} title="${hourLabel(hour)}–${hourLabel(hour + 1)} ${state === 'free' ? 'available' : state}">${hourLabel(hour)}</button>`;
  }).join('');
  const duration = $('#slot-duration');
  const max = start === null ? 0 : maxHoursFrom(start);
  duration.disabled = !max;
  duration.innerHTML = max ? Array.from({ length: max }, (_, i) => `<option value="${i + 1}" ${i + 1 === hours ? 'selected' : ''}>${i + 1} hour${i ? 's' : ''}</option>`).join('') : '<option>Pick a start time</option>';
  const free = hoursList.filter(hour => slotState(hour) === 'free').length;
  $('#slot-summary').innerHTML = start === null
    ? `${free ? `<strong>${free}</strong> of ${hoursList.length} hourly slots are free on ${fmtDate(slotDate(0))}. Pick a start time.` : `No free slots on ${fmtDate(slotDate(0))}. Try another date.`}`
    : `<strong>${fmtDate(slotDate(start))}</strong><br>${hourLabel(start)} – ${hourLabel(start + hours)} · ${hours} hour${hours > 1 ? 's' : ''}<br><strong>Total ${money(booking.space.pricePerHour * hours)}</strong>`;
  $('#confirm-booking').disabled = start === null;
  $('#booking-error').textContent = '';
}

async function confirmBooking() {
  const button = $('#confirm-booking');
  const errorText = $('#booking-error');
  const { space, start, hours } = booking;
  button.disabled = true;
  errorText.textContent = '';
  try {
    const { data } = await api.post('/bookings', { parkingSpotId: space.id, startTime: slotDate(start).toISOString(), endTime: slotDate(start + hours).toISOString() });
    modalContent.innerHTML = `<div class="booking-content"><p class="eyebrow">BOOKING CONFIRMED</p><h2>You’re all set.</h2><p class="confirmation">Your slot has been reserved.</p><div class="booking-summary"><strong>${esc(space.name)}</strong><br>${fmtDate(slotDate(start))}<br>${hourLabel(start)} – ${hourLabel(start + hours)}<br>Booking #${data.id} · ${money(data.totalAmount)}</div><button class="primary-button modal-action" id="view-dashboard" type="button">View in my dashboard</button></div>`;
    $('#view-dashboard').addEventListener('click', () => { bookingModal.close(); showDashboard('upcoming'); });
    loadDashboard();
  } catch (error) {
    errorText.textContent = apiError(error, 'Booking could not be completed.');
    booking.start = null;
    await loadSlots();
    errorText.textContent = apiError(error, 'Booking could not be completed.');
  }
}

/* ---------- Dashboard ---------- */

function bookingPhase(b) {
  if (b.status === 'CANCELLED') return 'cancelled';
  const now = new Date();
  if (new Date(b.endTime) <= now || b.status === 'COMPLETED') return 'past';
  return new Date(b.startTime) <= now ? 'active' : 'upcoming';
}

const statusLabel = { upcoming: 'Upcoming', active: 'In progress', past: 'Completed', cancelled: 'Cancelled' };

function bookingRow(b, { showDriver = false, canCancel = false } = {}) {
  const start = new Date(b.startTime);
  const end = new Date(b.endTime);
  const phase = bookingPhase(b);
  return `<article class="booking-row">
    <div class="booking-date"><strong>${start.getDate()}</strong><span>${start.toLocaleDateString('en-IN', { month: 'short' })}</span></div>
    <div class="booking-info"><h3>${esc(b.parkingSpotName)}</h3><p>${esc(b.area)}, ${esc(b.city)}</p>
      <p class="slot-time">${fmtDate(start)} · ${fmtTime(start)} – ${fmtTime(end)} (${hoursBetween(start, end)} hr)</p>
      ${showDriver ? `<p class="muted">Booked by ${esc(b.userName)} · #${b.id}</p>` : `<p class="muted">Booking #${b.id}</p>`}</div>
    <div class="booking-meta"><span class="status status-${phase}">${statusLabel[phase]}</span><strong>${money(b.totalAmount)}</strong>
      ${canCancel && phase === 'upcoming' ? `<button class="small-button danger" data-cancel="${b.id}" type="button">Cancel</button>` : ''}</div>
  </article>`;
}

function spaceRow(space) {
  const upcoming = dashData.received.filter(b => b.parkingSpotId === space.id && ['upcoming', 'active'].includes(bookingPhase(b))).length;
  return `<article class="booking-row">
    <div class="booking-date vehicle"><strong>${space.vehicleType === 'BIKE' ? '🏍' : '🚗'}</strong><span>${esc(space.vehicleType)}</span></div>
    <div class="booking-info"><h3>${esc(space.name)}</h3><p>${esc(space.area)}, ${esc(space.city)}</p><p class="muted">${esc(space.address)}</p><p class="slot-time">${upcoming} upcoming booking${upcoming === 1 ? '' : 's'}</p></div>
    <div class="booking-meta"><span class="status ${space.available ? 'status-upcoming' : 'status-cancelled'}">${space.available ? 'Active' : 'Paused'}</span><strong>${money(space.pricePerHour)}/hr</strong>
      <div class="row-actions"><button class="small-button" data-toggle="${space.id}" type="button">${space.available ? 'Pause' : 'Activate'}</button><button class="small-button" data-edit="${space.id}" type="button">Edit</button><button class="small-button danger" data-delete="${space.id}" type="button">Delete</button></div></div>
  </article>`;
}

function dashTabs(user) {
  return isOwner(user)
    ? [['received', 'Bookings received'], ['spaces', 'My spaces'], ['upcoming', 'My bookings']]
    : [['upcoming', 'Upcoming'], ['past', 'Past'], ['cancelled', 'Cancelled']];
}

async function loadDashboard() {
  const user = currentUser();
  $('#dashboard').hidden = !user;
  if (!user) return;
  const owner = isOwner(user);
  $('#dash-eyebrow').textContent = owner ? 'OWNER DASHBOARD' : 'YOUR DASHBOARD';
  $('#dash-title').textContent = owner ? 'Your spaces & bookings.' : 'Your parking, at a glance.';
  try {
    const [mine, received, spots] = await Promise.all([
      api.get('/bookings/mine'),
      owner ? api.get('/bookings/owner') : Promise.resolve({ data: [] }),
      owner ? api.get('/parking-spots/mine', { params: { size: 50 } }) : Promise.resolve({ data: { content: [] } }),
    ]);
    dashData = { mine: mine.data, received: received.data, spots: spots.data.content || [] };
  } catch (error) {
    $('#dash-panel').innerHTML = `<p class="empty">${esc(apiError(error, 'Could not load your dashboard.'))}</p>`;
    return;
  }
  if (!dashTabs(user).some(([key]) => key === dashTab)) dashTab = dashTabs(user)[0][0];
  renderDashboard();
}

function renderDashboard() {
  const user = currentUser();
  if (!user) return;
  const live = list => list.filter(b => b.status !== 'CANCELLED');
  const stats = isOwner(user)
    ? [['Spaces listed', `${dashData.spots.length}`, `${dashData.spots.filter(s => s.available).length} active`],
       ['Upcoming bookings', `${dashData.received.filter(b => ['upcoming', 'active'].includes(bookingPhase(b))).length}`, 'on your spaces'],
       ['Total earnings', money(live(dashData.received).reduce((sum, b) => sum + Number(b.totalAmount), 0)), plural(live(dashData.received).length, 'booking')]]
    : [['Upcoming bookings', `${dashData.mine.filter(b => ['upcoming', 'active'].includes(bookingPhase(b))).length}`, 'reserved slots'],
       ['Hours booked', `${live(dashData.mine).reduce((sum, b) => sum + hoursBetween(b.startTime, b.endTime), 0)}`, 'all time'],
       ['Total spent', money(live(dashData.mine).reduce((sum, b) => sum + Number(b.totalAmount), 0)), plural(live(dashData.mine).length, 'booking')]];
  $('#dash-stats').innerHTML = stats.map(([label, value, hint]) => `<div class="stat"><span>${label}</span><strong>${value}</strong><small>${hint}</small></div>`).join('');
  $('#dash-tabs').innerHTML = dashTabs(user).map(([key, label]) => `<button class="tab ${key === dashTab ? 'active' : ''}" data-tab="${key}" role="tab" aria-selected="${key === dashTab}" type="button">${label}</button>`).join('');

  const sortByStart = (items, desc) => [...items].sort((a, b) => (new Date(a.startTime) - new Date(b.startTime)) * (desc ? -1 : 1));
  let html = '';
  if (dashTab === 'received') {
    const items = sortByStart(dashData.received.filter(b => b.status !== 'CANCELLED'), true);
    html = items.map(b => bookingRow(b, { showDriver: true })).join('') || '<p class="empty">No bookings on your spaces yet.</p>';
  } else if (dashTab === 'spaces') {
    html = `<div class="panel-actions"><button id="dash-add-space" class="primary-button" type="button">+ List a new space</button></div>` +
      (dashData.spots.map(spaceRow).join('') || '<p class="empty">You have not listed any spaces yet.</p>');
  } else if (isOwner(user) && dashTab === 'upcoming') {
    html = sortByStart(dashData.mine, true).map(b => bookingRow(b, { canCancel: true })).join('') || '<p class="empty">You have not booked any parking yet.</p>';
  } else {
    const items = dashData.mine.filter(b => dashTab === 'upcoming' ? ['upcoming', 'active'].includes(bookingPhase(b)) : bookingPhase(b) === dashTab);
    const empty = { upcoming: 'No upcoming bookings. Find a space below and book a slot.', past: 'No completed bookings yet.', cancelled: 'No cancelled bookings.' }[dashTab];
    html = sortByStart(items, dashTab !== 'upcoming').map(b => bookingRow(b, { canCancel: true })).join('') || `<p class="empty">${empty}</p>`;
  }
  $('#dash-panel').innerHTML = html;
}

function showDashboard(tab) {
  if (tab) dashTab = tab;
  renderDashboard();
  $('#dashboard').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// Destructive buttons need a second click to confirm, so no browser dialogs are needed.
function armed(button, label) {
  if (button.dataset.armed) return true;
  button.dataset.armed = '1';
  const original = button.textContent;
  button.textContent = label;
  setTimeout(() => { if (button.isConnected) { delete button.dataset.armed; button.textContent = original; } }, 4000);
  return false;
}

async function handleDashboardClick(event) {
  const button = event.target.closest('button');
  if (!button) return;
  if (button.dataset.tab) { dashTab = button.dataset.tab; renderDashboard(); return; }
  if (button.id === 'dash-add-space') { openListing(); return; }
  const spaceOf = id => dashData.spots.find(s => s.id === Number(id));
  try {
    if (button.dataset.cancel) {
      if (!armed(button, 'Confirm cancel?')) return;
      button.disabled = true;
      await api.patch(`/bookings/${button.dataset.cancel}/cancel`);
      toast('Booking cancelled.');
    } else if (button.dataset.toggle) {
      const space = spaceOf(button.dataset.toggle);
      button.disabled = true;
      await api.put(`/parking-spots/${space.id}`, { ...space, available: !space.available });
      toast(space.available ? 'Space paused. It no longer appears in search.' : 'Space is open for bookings again.');
      loadSpaces();
    } else if (button.dataset.edit) {
      openListing(spaceOf(button.dataset.edit));
      return;
    } else if (button.dataset.delete) {
      if (!armed(button, 'Confirm delete?')) return;
      button.disabled = true;
      await api.delete(`/parking-spots/${button.dataset.delete}`);
      toast('Space deleted.');
      loadSpaces();
    } else return;
  } catch (error) {
    toast(apiError(error, 'That action could not be completed.'));
  }
  loadDashboard();
}

/* ---------- Auth & listing ---------- */

function setAuthMode(mode) {
  authMode = mode;
  $('#auth-modal h2').textContent = mode === 'login' ? 'Welcome back.' : 'Create your account.';
  $('#name-field').hidden = mode === 'login';
  $('#auth-name').required = mode === 'register';
  $('#role-field').hidden = mode === 'login';
  $('#auth-form button').innerHTML = `${mode === 'login' ? 'Log in' : 'Create account'} <span>→</span>`;
  $('#auth-toggle').textContent = mode === 'login' ? 'New to ParkEase? Create an account' : 'Already have an account? Log in';
  $('#auth-error').textContent = '';
}

function openAuth(hint = '') {
  setAuthMode('login');
  $('#auth-error').textContent = hint;
  authModal.showModal();
}

function openListing(space = null) {
  if (!sessionStorage.getItem('parkeaseToken')) { openAuth('Log in with a parking owner account to list a space.'); return; }
  if (!isOwner(currentUser())) { toast('Please log in with a Parking Owner account to list a space.'); return; }
  editingSpaceId = space?.id ?? null;
  const form = $('#listing-form');
  form.reset();
  if (space) {
    $('#space-name').value = space.name; $('#space-address').value = space.address; $('#space-city').value = space.city; $('#space-area').value = space.area;
    $('#space-price').value = space.pricePerHour; $('#space-vehicle').value = space.vehicleType; $('#space-image').value = space.imageUrl || ''; $('#space-available').checked = space.available;
  }
  $('#listing-title').textContent = space ? 'Edit your parking space.' : 'List your parking space.';
  $('#listing-submit').innerHTML = `${space ? 'Save changes' : 'Publish space'} <span>→</span>`;
  $('#listing-error').textContent = '';
  listingModal.showModal();
}

/* ---------- Wiring ---------- */

const nextBookingTime = new Date(Date.now() + 60 * 60 * 1000);
nextBookingTime.setMinutes(0, 0, 0);
$('#date').value = isoDate(nextBookingTime);
$('#date').min = isoDate(new Date());
$('#time').value = fmtTime(nextBookingTime);
$('#search-form').addEventListener('submit', event => { event.preventDefault(); loadSpaces(true); });
$('#sort').addEventListener('change', () => loadSpaces());
clear.addEventListener('click', () => { $('#city').value = ''; $('#vehicle').value = 'all'; loadSpaces(); });
list.addEventListener('click', event => { const button = event.target.closest('[data-id]'); if (button) openBooking(spaces.find(space => space.id === Number(button.dataset.id))); });
$('#close-modal').addEventListener('click', () => bookingModal.close());
$('#close-auth-modal').addEventListener('click', () => { pendingSpace = null; authModal.close(); });
$('#close-listing-modal').addEventListener('click', () => listingModal.close());
$('.login-button').addEventListener('click', () => {
  if (!sessionStorage.getItem('parkeaseToken')) { openAuth(); return; }
  sessionStorage.clear();
  dashData = { mine: [], received: [], spots: [] };
  updateAccountUI();
  toast('You have been logged out.');
});
$('#dashboard-link').addEventListener('click', event => { event.preventDefault(); showDashboard(); });
$('#dash-refresh').addEventListener('click', loadDashboard);
$('#dashboard').addEventListener('click', handleDashboardClick);
$('#list-space-button').addEventListener('click', () => openListing());
$('#auth-toggle').addEventListener('click', () => setAuthMode(authMode === 'login' ? 'register' : 'login'));

$('#auth-form').addEventListener('submit', async event => {
  event.preventDefault();
  const errorText = $('#auth-error');
  const submit = event.currentTarget.querySelector('button');
  const body = { email: $('#auth-email').value, password: $('#auth-password').value };
  if (authMode === 'register') Object.assign(body, { name: $('#auth-name').value, role: $('#auth-role').value });
  submit.disabled = true; errorText.textContent = '';
  try {
    const { data } = await api.post(`/auth/${authMode === 'login' ? 'login' : 'register'}`, body);
    sessionStorage.setItem('parkeaseToken', data.token);
    sessionStorage.setItem('parkeaseUser', JSON.stringify(data));
    authModal.close();
    $('#auth-form').reset();
    dashTab = null;
    updateAccountUI();
    toast(`Welcome, ${data.name.split(' ')[0]}!`);
    if (pendingSpace) { const space = pendingSpace; pendingSpace = null; openBooking(space); }
  } catch (error) { errorText.textContent = apiError(error, 'Could not authenticate.'); }
  finally { submit.disabled = false; }
});

$('#listing-form').addEventListener('submit', async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const errorText = $('#listing-error');
  const submit = $('#listing-submit');
  const body = { name: $('#space-name').value, address: $('#space-address').value, city: $('#space-city').value, area: $('#space-area').value, pricePerHour: Number($('#space-price').value), vehicleType: $('#space-vehicle').value, available: $('#space-available').checked, imageUrl: $('#space-image').value || null };
  submit.disabled = true; errorText.textContent = '';
  try {
    if (editingSpaceId) await api.put(`/parking-spots/${editingSpaceId}`, body);
    else await api.post('/parking-spots', body);
    listingModal.close();
    form.reset();
    toast(editingSpaceId ? 'Space updated.' : 'Your space is live!');
    $('#city').value = body.city;
    await Promise.all([loadSpaces(!editingSpaceId), loadDashboard()]);
  } catch (error) { errorText.textContent = apiError(error, 'Could not publish your parking space.'); }
  finally { submit.disabled = false; }
});

updateAccountUI();
loadSpaces();
