import { useState, useEffect } from "react";
import API from "../../services/api";

export default function HospitalManagement() {

  const [inventory, setInventory] = useState([]);
  const [history, setHistory] = useState([]);

  const [requests, setRequests] = useState([]);
  const [upcoming, setUpcoming] = useState([]);
  const [donationHistory, setDonationHistory] = useState([]);

  const [notification, setNotification] = useState("");

  const [editId, setEditId] = useState(null);
  const [editUnits, setEditUnits] = useState("");

  const [newGroup, setNewGroup] = useState("");
  const [newUnits, setNewUnits] = useState("");

  useEffect(() => {
    fetchInventory();
    fetchHistory();
    loadScheduleData();
  }, []);

  /* ---------------- INVENTORY ---------------- */

  const fetchInventory = async () => {
    try {
      const res = await API.get("/inventory/my");
      setInventory(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchHistory = async () => {
    try {
      const res = await API.get("/inventory/history");
      setHistory(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  /* ---------------- SCHEDULE (MATCH BLOOD BANK) ---------------- */

  const loadScheduleData = async () => {
    try {

      const localResponses =
        JSON.parse(localStorage.getItem("respondedRequests")) || {};

      const [incomingRes, upcomingRes, historyRes] = await Promise.all([
        API.get("/schedule/incoming"),
        API.get("/schedule/upcoming"),
        API.get("/schedule/history"),
      ]);

      // ✅ EMERGENCY (same as blood bank)
      const mappedRequests = incomingRes.data.map((r) => {

        let status = "Pending";

        if (r.status === "CONFIRMED") status = "Accepted";
        else if (r.status === "REJECTED") status = "Rejected";
        else if (r.status === "PENDING") status = "Response Sent";
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

      const filteredRequests = mappedRequests.filter(
        (r) =>
          r.status !== "Accepted" &&
          r.status !== "Rejected"
      );

      setRequests(filteredRequests);

      // ✅ UPCOMING (same structure)
      const mappedUpcoming = upcomingRes.data.map((d) => ({
        id: d.id,
        hospital: d.hospitalName,
        location: d.hospitalName,
        bloodGroup: d.bloodGroup,
        scheduledDate: d.scheduledDate?.split("T")[0],
        scheduledTime: d.scheduledDate?.split("T")[1],
        contactPerson: d.patientName,
      }));

      setUpcoming(mappedUpcoming);

      // ✅ HISTORY (THIS IS WHAT YOU ASKED — fully included)
      const mappedHistory = historyRes.data.map((h) => ({
        id: h.id,
        date: h.scheduledDate?.split("T")[0],
        hospital: h.hospitalName,
        status: h.status,
      }));

      setDonationHistory(mappedHistory);

    } catch (err) {
      console.error(err);
    }
  };

  /* ---------------- INVENTORY ACTIONS ---------------- */

  const handleEdit = (item) => {
    setEditId(item.id);
    setEditUnits(item.unitsAvailable);
  };

  const saveEdit = async (bloodGroup) => {
    try {
      await API.post(
        `/inventory/add?bloodGroup=${encodeURIComponent(bloodGroup)}&units=${editUnits}`
      );
      setEditId(null);
      fetchInventory();
      fetchHistory();
    } catch (err) {
      alert("Error updating stock");
    }
  };

  const addStock = async () => {
    if (!newGroup || !newUnits) return;

    try {
      await API.post(
        `/inventory/add?bloodGroup=${encodeURIComponent(newGroup)}&units=${newUnits}`
      );
      setNewGroup("");
      setNewUnits("");
      fetchInventory();
      fetchHistory();
    } catch (err) {
      console.error(err);
    }
  };

  /* ---------------- RESPONSE ---------------- */

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

  /* ---------------- UI (UNCHANGED) ---------------- */

  return (
    <div className="min-h-screen bg-gray-50 pt-6 px-4 pb-10">
      <div className="max-w-6xl mx-auto space-y-10">

        {notification && (
          <div className="bg-green-100 text-green-700 p-3 rounded-lg text-sm">
            {notification}
          </div>
        )}

        <h1 className="text-2xl font-bold text-gray-800">
          Inventory Dashboard
        </h1>

        {/* INVENTORY */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border">
          <h2 className="text-lg font-semibold mb-6">
            Blood Inventory Management
          </h2>

          <div className="flex gap-3 mb-6">
            <select value={newGroup} onChange={(e) => setNewGroup(e.target.value)} className="border p-2 rounded-lg">
              <option value="">Blood Group</option>
              <option>O+</option><option>O-</option>
              <option>A+</option><option>A-</option>
              <option>B+</option><option>B-</option>
              <option>AB+</option><option>AB-</option>
            </select>

            <input
              type="number"
              value={newUnits}
              onChange={(e) => setNewUnits(e.target.value)}
              placeholder="Units"
              className="border p-2 rounded-lg"
            />

            <button onClick={addStock} className="border px-4 py-2 rounded-lg text-sm">
              Add
            </button>
          </div>

          <div className="space-y-4">
            {inventory.map((item) => (
              <div key={item.id} className="flex justify-between items-center border rounded-xl p-4">

                <div>
                  <p className="font-semibold text-lg">{item.bloodGroup}</p>
                  {item.unitsAvailable < 3 && (
                    <p className="text-sm text-red-600">Low Stock</p>
                  )}
                </div>

                {editId === item.id ? (
                  <div className="flex items-center space-x-3">
                    <input
                      type="number"
                      value={editUnits}
                      onChange={(e) => setEditUnits(e.target.value)}
                      className="border p-2 rounded-lg w-24"
                    />
                    <button
                      onClick={() => saveEdit(item.bloodGroup)}
                      className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm"
                    >
                      Save
                    </button>
                  </div>
                ) : (
                  <div className="flex items-center space-x-6">
                    <p>Units: {item.unitsAvailable}</p>
                    <button
                      onClick={() => handleEdit(item)}
                      className="border px-3 py-1 rounded-lg text-sm"
                    >
                      Edit
                    </button>
                  </div>
                )}

              </div>
            ))}
          </div>
        </div>

        {/* EMERGENCY */}
        <div className="bg-white p-6 rounded-2xl shadow-sm">
          <h2 className="text-lg font-semibold mb-4">Emergency Requests</h2>

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

        {/* UPCOMING */}
        <div className="bg-white p-6 rounded-2xl shadow-sm">
          <h2 className="text-lg font-semibold mb-4">Upcoming Donations</h2>

          {upcoming.length === 0 ? (
            <p className="text-sm text-gray-500">No upcoming donations scheduled.</p>
          ) : (
            <div className="space-y-4">
              {upcoming.map((d) => (
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

        {/* HISTORY */}
        <div className="bg-white p-6 rounded-2xl shadow-sm">
          <h2 className="text-lg font-semibold mb-4">Donation History</h2>

          <table className="w-full text-left">
            <thead>
              <tr className="bg-gray-100 text-sm">
                <th className="p-3">Date</th>
                <th className="p-3">Hospital</th>
                <th className="p-3">Status</th>
              </tr>
            </thead>

            <tbody>
              {donationHistory.map((d) => (
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