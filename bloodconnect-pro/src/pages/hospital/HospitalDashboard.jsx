import { useEffect, useState } from "react";
import API from "../../services/api";

export default function HospitalDashboard() {

  const [profile, setProfile] = useState({});
  const [stats, setStats] = useState({});
  const [requesterUpcoming, setRequesterUpcoming] = useState([]);

  const [searchLocation, setSearchLocation] = useState("");
  const [searchBlood, setSearchBlood] = useState("");
  const [results, setResults] = useState(null);

  // ================= LOAD DATA =================
  useEffect(() => {
    fetchProfile();
    fetchStats();
    fetchRequesterUpcoming();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await API.get("/hospital-dashboard/profile");
      setProfile(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchStats = async () => {
    try {
      const res = await API.get("/hospital-dashboard/stats");

      console.log("STATS API RESPONSE =", res.data); // 🔥 DEBUG

      setStats(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchRequesterUpcoming = async () => {
    try {
      const res = await API.get("/schedule/hospital/requester-upcoming");

      const mapped = res.data.map((u) => ({
        id: u.id,
        donorName: u.donorName,
        bloodGroup: u.bloodGroup,
        patientName: u.patientName,
        hospitalName: u.hospitalName,
      }));

      setRequesterUpcoming(mapped);

    } catch (err) {
      console.error(err);
    }
  };

  // ================= COMPLETE =================
  const completeSchedule = async (id) => {
    try {
      await API.put(`/schedule/complete?scheduleId=${id}`);

      alert("Donation marked as completed");

      fetchRequesterUpcoming();
      fetchStats();

    } catch (err) {
      console.error(err);
      alert("Failed to complete");
    }
  };

  // ================= SEARCH =================
  const handleSearch = async () => {
    if (!searchLocation || !searchBlood) {
      alert("Please select blood group and location");
      return;
    }

    try {
      const res = await API.get(
        `/search/global?bloodGroup=${encodeURIComponent(searchBlood)}&location=${encodeURIComponent(searchLocation)}`
      );

      const normalized = res.data.map((r) => ({
        ...r,
        phone: r.phone || r.contactNumber || r.contact || "N/A"
      }));

      const donors = normalized.filter(r => r.type === "DONOR");
      const banks = normalized.filter(r => r.type !== "DONOR");

      setResults({ donors, banks });

    } catch (err) {
      console.error(err);
      alert("Search failed");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 pt-6 px-4 pb-10">
      <div className="max-w-7xl mx-auto space-y-8">

        {/* ================= PROFILE ================= */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border">
          <div className="flex flex-col md:flex-row md:justify-between md:items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-800">
                {profile.hospitalName || "Hospital"}
              </h1>

              <p className="text-sm text-gray-600 mt-1">
                License: {profile.licenseNumber || "N/A"}
              </p>

              <p className="text-sm text-gray-600">
                GST: {profile.gstNumber || "N/A"}
              </p>

              <p className="text-sm text-gray-600">
                {profile.address || "N/A"}
              </p>
            </div>

            <span className="text-sm px-4 py-2 rounded-lg bg-green-100 text-green-700 mt-4 md:mt-0">
              Verified Hospital
            </span>
          </div>
        </div>

        {/* ================= SEARCH ================= */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border">
          <h2 className="text-lg font-semibold mb-4">
            Search Blood Availability
          </h2>

          <div className="grid md:grid-cols-3 gap-4 mb-6">
            <select
              value={searchBlood}
              onChange={(e) => setSearchBlood(e.target.value)}
              className="border p-2 rounded-lg"
            >
              <option value="">Select Blood Group</option>
              <option>O+</option>
              <option>O-</option>
              <option>A+</option>
              <option>A-</option>
              <option>B+</option>
              <option>B-</option>
              <option>AB+</option>
              <option>AB-</option>
            </select>

            <input
              type="text"
              placeholder="Enter Location"
              value={searchLocation}
              onChange={(e) => setSearchLocation(e.target.value)}
              className="border p-2 rounded-lg"
            />

            <button
              onClick={handleSearch}
              className="bg-red-600 text-white rounded-lg font-medium"
            >
              Search
            </button>
          </div>

          {results && (
            <div className="grid md:grid-cols-2 gap-6">

              {/* DONORS */}
              <div>
                <h3 className="font-semibold mb-3">Available Donors</h3>

                {results.donors.length === 0 ? (
                  <p className="text-sm text-gray-500">No donors found.</p>
                ) : (
                  results.donors.map((d) => (
                    <div
                      key={d.id}
                      className="border rounded-xl p-4 shadow-sm mb-3"
                    >
                      <p className="font-semibold">{d.name}</p>

                      <p className="text-sm text-gray-500">
                        Blood: {d.bloodGroup}
                      </p>

                      <p className="text-sm text-gray-500">
                        Location: {d.location}
                      </p>

                      <p className="text-sm text-gray-500">
                        Contact: {d.phone}
                      </p>
                    </div>
                  ))
                )}
              </div>

              {/* CENTERS */}
              <div>
                <h3 className="font-semibold mb-3">
                  Blood Banks / Hospitals
                </h3>

                {results.banks.length === 0 ? (
                  <p className="text-sm text-gray-500">
                    No storage centers found.
                  </p>
                ) : (
                  results.banks.map((b) => (
                    <div
                      key={b.id}
                      className="border rounded-xl p-4 shadow-sm mb-3"
                    >
                      <p className="font-semibold">{b.name}</p>

                      <p className="text-sm text-gray-500">
                        Type: {b.type === "HOSPITAL" ? "Hospital" : "Blood Bank"}
                      </p>

                      <p className="text-sm text-gray-500">
                        Blood: {b.bloodGroup}
                      </p>

                      <p className="text-sm text-gray-500">
                        Location: {b.location}
                      </p>

                      <p className="text-sm text-gray-500">
                        Contact: {b.phone}
                      </p>
                    </div>
                  ))
                )}
              </div>

            </div>
          )}
        </div>

        {/* ================= STATS ================= */}
        <div className="grid md:grid-cols-3 gap-6">
          <StatCard title="Active Blood Requests" value={stats.activeRequests || 0} />
          <StatCard title="Total Donor Responses" value={stats.totalResponses || 0} />

          {/* 🔥 FINAL FIX HERE */}
          <StatCard
            title="Successful Donations"
            value={stats.successful ?? stats.successfulDonations ?? 0}
          />
        </div>

        {/* ================= UPCOMING ================= */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border">
          <h2 className="text-lg font-semibold mb-4">
            Upcoming Request Fulfillment
          </h2>

          {requesterUpcoming.length === 0 ? (
            <p className="text-sm text-gray-500">
              No upcoming schedules.
            </p>
          ) : (
            <div className="space-y-4">
              {requesterUpcoming.map((u) => (
                <div
                  key={u.id}
                  className="border rounded-xl p-4 flex justify-between items-center"
                >
                  <div>
                    <p className="font-semibold">Patient: {u.patientName}</p>
                    <p className="text-sm text-gray-500">
                      Blood: {u.bloodGroup}
                    </p>
                    <p className="text-sm text-gray-500">
                      Donor: {u.donorName}
                    </p>
                    <p className="text-sm text-gray-500">
                      From: {u.hospitalName}
                    </p>
                  </div>

                  <div className="flex flex-col items-end gap-2">
                    <span className="text-sm px-3 py-1 rounded-lg bg-green-100 text-green-700">
                      Scheduled
                    </span>

                    <button
                      onClick={() => completeSchedule(u.id)}
                      className="bg-blue-600 text-white px-3 py-1 rounded text-sm"
                    >
                      Complete
                    </button>
                  </div>

                </div>
              ))}
            </div>
          )}
        </div>

      </div>
    </div>
  );
}

// ================= REUSABLE =================
function StatCard({ title, value }) {
  return (
    <div className="bg-white p-6 rounded-2xl shadow-sm border text-center">
      <p className="text-gray-500 text-sm">{title}</p>
      <h3 className="text-2xl font-bold mt-2 text-gray-800">{value}</h3>
    </div>
  );
}