import { useState, useEffect } from "react";
import API from "../../services/api";

export default function HospitalEmergency() {

  const [requests, setRequests] = useState([]);
  const [responses, setResponses] = useState([]);
  const [donationHistory, setDonationHistory] = useState([]);

  const [activeSchedule, setActiveSchedule] = useState(null);
  const [scheduleDateTime, setScheduleDateTime] = useState("");

  const [formData, setFormData] = useState({
    patientName: "",
    patientAge: "",
    contactNumber: "",
    bloodGroup: "",
    unitsRequired: "",
    urgencyLevel: "NORMAL",
    location: "",
  });

  useEffect(() => {
    loadAll();
  }, []);

  const loadAll = async () => {
    await Promise.all([
      fetchRequests(),
      fetchResponses(),
      fetchHistory()
    ]);
  };

  const fetchRequests = async () => {
    try {
      const res = await API.get("/emergency/requests");
      setRequests(res.data);
    } catch {
      console.error("Failed to fetch requests");
    }
  };

  const fetchResponses = async () => {
    try {
      const res = await API.get("/emergency/responses");

      // ✅ FIX: keep SELECTED also (only remove REJECTED & CONFIRMED)
      const filtered = res.data.filter(
        r => r.status !== "REJECTED" && r.status !== "CONFIRMED"
      );

      setResponses(filtered);
    } catch {
      console.error("Failed to fetch responses");
    }
  };

  const fetchHistory = async () => {
    try {
      const res = await API.get("/emergency/history");
      setDonationHistory(res.data);
    } catch {
      console.error("Failed history");
    }
  };

  const getId = (res) => res.id || res.responseId;

  const handleSubmit = async () => {
    if (
      !formData.patientName ||
      !formData.patientAge ||
      !formData.contactNumber ||
      !formData.bloodGroup ||
      !formData.unitsRequired ||
      !formData.location
    ) {
      alert("Please fill all required fields");
      return;
    }

    try {
      await API.post("/blood-request/create", {
        ...formData,
        unitsRequired: Number(formData.unitsRequired)
      });

      await fetchRequests();

      setFormData({
        patientName: "",
        patientAge: "",
        contactNumber: "",
        bloodGroup: "",
        unitsRequired: "",
        urgencyLevel: "NORMAL",
        location: "",
      });

    } catch {
      alert("Error creating request");
    }
  };

  // ✅ ACCEPT → SET SELECTED + OPEN SCHEDULER
  const confirmResponse = async (res) => {
    const id = getId(res);

    if (!id) {
      alert("Invalid response");
      return;
    }

    try {
      await API.put(`/donor-response/confirm?responseId=${id}`);

      // ✅ KEEP IN UI + OPEN SCHEDULER
      setActiveSchedule(id);

      // ✅ reload to get updated status (SELECTED)
      fetchResponses();

    } catch {
      alert("Error confirming");
    }
  };

  // ✅ FINAL STEP → REMOVE AFTER SCHEDULE
  const createSchedule = async () => {
    if (!scheduleDateTime) {
      alert("Select date & time");
      return;
    }

    try {
      await API.post(
        `/schedule/create?responseId=${activeSchedule}&dateTime=${scheduleDateTime}`
      );

      alert("Donation Scheduled");

      setActiveSchedule(null);
      setScheduleDateTime("");

      // ✅ IMPORTANT: now it becomes CONFIRMED → removed from UI
      loadAll();

    } catch {
      alert("Scheduling failed");
    }
  };

  const rejectResponse = async (res) => {
    const id = getId(res);

    if (!id) {
      alert("Invalid ID");
      return;
    }

    try {
      await API.put(`/donor-response/reject?responseId=${id}`);

      setResponses(prev =>
        prev.filter(r => getId(r) !== id)
      );

    } catch {
      alert("Reject failed");
    }
  };

  const getStatusColor = (status) => {
    if (status === "PENDING") return "text-yellow-600";
    if (status === "SELECTED") return "text-blue-600"; // ✅ NEW
    if (status === "CONFIRMED") return "text-green-600";
    if (status === "REJECTED") return "text-red-600";
    return "text-gray-500";
  };

  return (
    <div className="min-h-screen bg-gray-100 px-6 py-6">

      <div className="max-w-7xl mx-auto space-y-8">

        <h1 className="text-3xl font-bold text-gray-800">
          Emergency Blood Coordination
        </h1>

        <div className="grid lg:grid-cols-2 gap-8">

          {/* CREATE REQUEST */}
          <div className="bg-white p-6 rounded-2xl shadow-md space-y-4">

            <h2 className="text-lg font-semibold">
              Create Emergency Request
            </h2>

            {["patientName","patientAge","contactNumber","unitsRequired","location"]
              .map((field, i) => (
                <input
                  key={i}
                  placeholder={field.replace(/([A-Z])/g, " $1")}
                  value={formData[field]}
                  onChange={(e)=>setFormData({...formData,[field]:e.target.value})}
                  className="w-full border p-2 rounded-lg"
                />
            ))}

            <select
              value={formData.bloodGroup}
              onChange={(e)=>setFormData({...formData,bloodGroup:e.target.value})}
              className="w-full border p-2 rounded-lg">
              <option value="">Select Blood Group</option>
              <option>O+</option><option>A+</option>
              <option>B+</option><option>AB+</option>
              <option>O-</option><option>A-</option>
              <option>B-</option><option>AB-</option>
            </select>

            <select
              value={formData.urgencyLevel}
              onChange={(e)=>setFormData({...formData,urgencyLevel:e.target.value})}
              className="w-full border p-2 rounded-lg">
              <option value="NORMAL">Normal</option>
              <option value="URGENT">Urgent</option>
              <option value="EMERGENCY">Critical</option>
            </select>

            <button
              onClick={handleSubmit}
              className="w-full bg-red-600 text-white py-2 rounded-lg hover:bg-red-700">
              Submit Request
            </button>

          </div>

          {/* RESPONSES */}
          <div className="space-y-4">

            <h2 className="text-lg font-semibold">
              Donor Responses
            </h2>

            {responses.length === 0 && (
              <p className="text-gray-500">No responses yet</p>
            )}

            {responses.map(res => {

              const id = getId(res);

              return (
                <div key={id} className="bg-white p-4 rounded-xl shadow border">

                  <div className="flex justify-between">
                    <div>

                      <p className="font-semibold">
                        {res.blood && res.blood !== "-"
                          ? `${res.donorName} (${res.blood})`
                          : res.donorName}
                      </p>

                      <p className="text-sm">Phone: {res.phone}</p>
                      <p className="text-sm">Location: {res.location}</p>

                      <p className="text-sm mt-2">
                        Patient: {res.patientName}
                      </p>
                      <p className="text-sm">
                        Hospital: {res.hospitalName}
                      </p>
                      <p className="text-sm">
                        Response: {res.response}
                      </p>
                    </div>

                    <p className={`font-semibold ${getStatusColor(res.status)}`}>
                      {res.status}
                    </p>
                  </div>

                  {/* ✅ SHOW BUTTON ONLY FOR PENDING */}
                  {res.status === "PENDING" && (
                    <div className="mt-3 flex gap-3">
                      <button
                        onClick={()=>confirmResponse(res)}
                        className="bg-green-600 text-white px-3 py-1 rounded">
                        Accept
                      </button>

                      <button
                        onClick={()=>rejectResponse(res)}
                        className="border px-3 py-1 rounded">
                        Reject
                      </button>
                    </div>
                  )}

                  {/* ✅ SHOW SCHEDULER FOR SELECTED */}
                  {(res.status === "SELECTED" || activeSchedule === id) && (
                    <div className="mt-3 bg-gray-50 p-3 rounded-lg">

                      <input
                        type="datetime-local"
                        value={scheduleDateTime}
                        onChange={(e)=>setScheduleDateTime(e.target.value)}
                        className="border p-2 rounded w-full mb-2"
                      />

                      <button
                        onClick={createSchedule}
                        className="bg-blue-600 text-white px-3 py-1 rounded">
                        Confirm Schedule
                      </button>
                    </div>
                  )}

                </div>
              );
            })}

          </div>
        </div>

        {/* ACTIVE REQUESTS */}
        <div className="bg-white p-6 rounded-2xl shadow-md">
          <h2 className="text-lg font-semibold mb-4">
            Active Requests
          </h2>

          {requests.map(req => (
            <div key={req.id} className="border rounded-lg p-3 mb-2">

              <p className="font-semibold">{req.patientName}</p>
              <p className="text-sm">Age: {req.patientAge}</p>
              <p className="text-sm">Blood: {req.bloodGroup}</p>
              <p className="text-sm">Units: {req.unitsRequired}</p>
              <p className="text-sm">Location: {req.location}</p>
              <p className="text-sm">Urgency: {req.urgencyLevel}</p>
              <p className="text-sm">Contact: {req.contactNumber}</p>

            </div>
          ))}
        </div>

        {/* HISTORY */}
        <div className="bg-white p-6 rounded-2xl shadow-md">
          <h2 className="text-lg font-semibold mb-4">
            Donation History
          </h2>

          {donationHistory.length === 0 ? (
            <p className="text-gray-500 text-sm">No completed donations</p>
          ) : (
            <table className="w-full text-left">
              <thead>
                <tr className="bg-gray-100 text-sm">
                  <th className="p-3">Responder</th>
                  <th className="p-3">Patient</th>
                  <th className="p-3">Blood</th>
                  <th className="p-3">Date</th>
                  <th className="p-3">Status</th>
                </tr>
              </thead>

              <tbody>
                {donationHistory.map((d) => (
                  <tr key={d.id} className="border-b">
                    <td className="p-3">{d.donorName}</td>
                    <td className="p-3">{d.patientName}</td>
                    <td className="p-3">{d.bloodGroup}</td>
                    <td className="p-3">
                      {d.scheduledDate?.split("T")[0]}
                    </td>
                    <td className="p-3 text-green-600 font-semibold">
                      Completed
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

      </div>
    </div>
  );
}