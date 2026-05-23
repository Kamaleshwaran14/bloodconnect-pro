import API from "./api";

// 🩸 EMERGENCY REQUESTS
export const getEmergencyRequests = () =>
  API.get("/donor-dashboard/emergency");

// 📅 UPCOMING
export const getUpcomingDonations = () =>
  API.get("/donor-dashboard/upcoming");

// 📜 HISTORY
export const getDonationHistory = () =>
  API.get("/donor-dashboard/history");