import { useEffect, useState } from "react";
import API from "../../services/api";

export default function DonorManagement() {

  const [notification, setNotification] = useState("");

  const [requests, setRequests] = useState([]);
  const [upcomingDonations, setUpcomingDonations] = useState([]);
  const [donations, setDonations] = useState([]);

  useEffect(() => {
    loadAllData();
  }, []);

  const loadAllData = async () => {
    try {

      const localResponses =
        JSON.parse(localStorage.getItem("respondedRequests")) || {};

      const [emergencyRes, upcomingRes, historyRes] = await Promise.all([
        API.get("/schedule/incoming"),
        API.get("/schedule/upcoming"),
        API.get("/schedule/history")
      ]);

      /* ---------------- EMERGENCY ---------------- */

      const mappedRequests = emergencyRes.data.map((r) => {

        let status = "Pending";

        // ✅ Backend status priority
        if (r.status === "CONFIRMED") status = "Accepted by Hospital";
        else if (r.status === "REJECTED") status = "Rejected by Hospital";
        else if (r.status === "PENDING") status = "Response Sent";

        // ✅ Local immediate response (only if backend still OPEN)
        else if (localResponses[r.id]) status = localResponses[r.id];

        else if (r.status === "OPEN") status = "Pending";

        return {
          id: r.id,
          hospital: r.hospitalName,
          bloodGroup: r.bloodGroup,
          location: r.location,
          contact: r.contactNumber,
          units: r.unitsRequired,
          urgency: r.urgencyLevel,
          patient: `${r.patientName} (Age: ${r.patientAge})`,
          status,
        };
      });

      // ✅ Remove fully processed (confirmed/rejected by hospital)
      const filteredRequests = mappedRequests.filter(
        (r) =>
          r.status !== "Accepted by Hospital" &&
          r.status !== "Rejected by Hospital"
      );

      setRequests(filteredRequests);

      /* ---------------- UPCOMING ---------------- */

      const mappedUpcoming = upcomingRes.data.map((d) => ({
        id: d.id,
        hospital: d.hospitalName,
        location: d.hospitalName,
        bloodGroup: d.bloodGroup,
        scheduledDate: d.scheduledDate?.split("T")[0],
        scheduledTime: d.scheduledDate?.split("T")[1],
        contactPerson: d.patientName,
        contactPhone: "N/A",
      }));

      setUpcomingDonations(mappedUpcoming);

      /* ---------------- HISTORY ---------------- */

      const mappedHistory = historyRes.data.map((h) => ({
        id: h.id,
        date: h.scheduledDate?.split("T")[0],
        hospital: h.hospitalName,
        status: h.status,
      }));

      setDonations(mappedHistory);

    } catch (err) {
      console.error(err);
      alert("Failed to load donation data");
    }
  };

  /* ---------------- HANDLE ACCEPT / DECLINE ---------------- */

  const handleAction = async (id, action) => {
  try {

    await API.post(
      `/donor-response/respond?requestId=${id}&action=${action.toUpperCase()}`
    );

    if (action === "Accepted") {
      setNotification("Response sent successfully.");
    }

    const prev =
      JSON.parse(localStorage.getItem("respondedRequests")) || {};

    const updated = {
      ...prev,
      [id]: action === "Accepted" ? "Accepted" : "Declined"
    };

    localStorage.setItem("respondedRequests", JSON.stringify(updated));

    if (action === "Declined") {
      // ❌ REMOVE immediately
      setRequests((prev) => prev.filter((req) => req.id !== id));
    } else {
      // ✅ KEEP in UI and update status
      setRequests((prev) =>
        prev.map((req) =>
          req.id === id
            ? { ...req, status: "Response Sent" }
            : req
        )
      );
    }

  } catch (err) {
    alert("Action failed");
  }
};

  return (
    <div className="min-h-screen bg-gray-50 pt-10 px-4 pb-10">
      <div className="max-w-7xl mx-auto space-y-8">

        {notification && (
          <div className="bg-green-100 text-green-700 p-3 rounded-lg text-sm">
            {notification}
          </div>
        )}

        {/* Emergency */}
        <div className="bg-white p-6 rounded-2xl shadow-sm">
          <h2 className="text-lg font-semibold mb-4">
            Emergency Requests
          </h2>

          <div className="space-y-4">
            {requests.map((req) => (
              <div key={req.id} className="border rounded-xl p-4 flex flex-col gap-3">

                <p className="font-bold text-lg">{req.hospital}</p>

                <p className="text-sm">Patient: {req.patient}</p>
                <p className="text-sm">Location: {req.location}</p>
                <p className="text-sm">Blood Group: {req.bloodGroup}</p>
                <p className="text-sm">Units Required: {req.units}</p>
                <p className="text-sm">Urgency Level: {req.urgency}</p>
                <p className="text-sm">Contact: {req.contact}</p>

                {req.status === "Pending" ? (
                  <div className="flex gap-3 mt-2">
                    <button
                      onClick={() => handleAction(req.id, "Accepted")}
                      className="px-4 py-2 bg-gray-900 text-white rounded-lg text-sm"
                    >
                      Accept
                    </button>

                    <button
                      onClick={() => handleAction(req.id, "Declined")}
                      className="px-4 py-2 border rounded-lg text-sm"
                    >
                      Decline
                    </button>
                  </div>
                ) : (
                  <span className="text-sm font-medium mt-2">
                    {req.status}
                  </span>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Upcoming */}
        <div className="bg-white p-6 rounded-2xl shadow-sm">
          <h2 className="text-lg font-semibold mb-4">
            Upcoming Donations
          </h2>

          {upcomingDonations.length === 0 ? (
            <p className="text-sm text-gray-500">
              No upcoming donations scheduled.
            </p>
          ) : (
            <div className="space-y-4">
              {upcomingDonations.map((d) => (
                <div key={d.id} className="border rounded-xl p-4">
                  <p className="font-semibold">{d.hospital}</p>
                  <p className="text-sm">Date: {d.scheduledDate}</p>
                  <p className="text-sm">Time: {d.scheduledTime}</p>
                  <span className="text-green-600 text-sm">Scheduled</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* History */}
        <div className="bg-white p-6 rounded-2xl shadow-sm">
          <h2 className="text-lg font-semibold mb-4">
            Donation History
          </h2>

          <table className="w-full text-left">
            <thead>
              <tr className="bg-gray-100 text-sm">
                <th className="p-3">Date</th>
                <th className="p-3">Hospital</th>
                <th className="p-3">Status</th>
              </tr>
            </thead>

            <tbody>
              {donations.map((d) => (
                <tr key={d.id}>
                  <td className="p-3">{d.date}</td>
                  <td className="p-3">{d.hospital}</td>
                  <td className="p-3">{d.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

      </div>
    </div>
  );
}