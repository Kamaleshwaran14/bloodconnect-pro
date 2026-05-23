import { useEffect, useState } from "react";
import API from "../../services/api";

export default function BloodBankDashboard() {

  const [profile, setProfile] = useState({});
  const [stats, setStats] = useState({});
  const [inventoryData, setInventoryData] = useState([]);

  useEffect(() => {
    fetchProfile();
    fetchStats();
    fetchInventory();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await API.get("/bloodbank-dashboard/profile");
      setProfile(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchStats = async () => {
    try {
      const res = await API.get("/bloodbank-dashboard/stats");
      setStats(res.data);
      console.log("STATS RESPONSE:", res.data); // ✅ DEBUG
    } catch (err) {
      console.error(err);
    }
  };

  const fetchInventory = async () => {
    try {
      const res = await API.get("/bloodbank-dashboard/inventory");
      setInventoryData(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  /* ---------------- CALCULATIONS ---------------- */

  const totalUnits = stats.totalUnits || 0;
  const successfulDonations = stats.successfulDonations || 0;
  const lastDonationDate = stats.lastDonationDate || "N/A";

  // ⚠️ keep same UI logic
  const lowStock = inventoryData.filter(item => item.units < 5);

  const sortedInventory = [...inventoryData];

  return (
    <div className="min-h-screen bg-gray-50 pt-6 px-4 pb-10">
      <div className="max-w-7xl mx-auto space-y-8">

        {/* Blood Bank Info */}
<div className="bg-white p-6 rounded-2xl shadow-sm border">
  <div className="flex flex-col md:flex-row md:justify-between md:items-center">
    <div>
      <h1 className="text-2xl font-bold text-gray-800">
        {profile.name || "Blood Bank"}
      </h1>
      <p className="text-sm text-gray-600 mt-1">
        License: {profile.license || "N/A"}
      </p>
      <p className="text-sm text-gray-600">
        {profile.location || "N/A"} • {profile.contact || "N/A"}
      </p>
    </div>

    <span className="text-sm px-4 py-2 rounded-lg bg-gray-100 mt-4 md:mt-0">
      {profile.verified
        ? "Verified Blood Bank"
        : "Pending Verification"}
    </span>
  </div>
</div>

        {/* ✅ UPDATED STATS ONLY */}
        <div className="grid md:grid-cols-3 gap-6">
          <StatCard title="Total Units Available" value={totalUnits} />
          <StatCard title="Successful Donations" value={successfulDonations} />
          <StatCard title="Last Donation Date" value={lastDonationDate} />
        </div>

        {/* Inventory Snapshot (UNCHANGED) */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border">
          <h2 className="text-lg font-semibold mb-4">Inventory Snapshot</h2>

          {sortedInventory.map((item) => (
            <div
              key={item.id}
              className="border rounded-xl p-4 flex justify-between mb-3"
            >
              <div>
                <p className="font-semibold">{item.group}</p>

                {item.units < 5 && (
                  <p className="text-sm text-red-600">Low Stock</p>
                )}
              </div>

              <div className="text-sm text-gray-600">
                Units: {item.units}
              </div>
            </div>
          ))}
        </div>

        {/* Low Stock Alerts (UNCHANGED) */}
        {lowStock.length > 0 && (
          <div className="bg-white p-6 rounded-2xl shadow-sm border">
            <h2 className="text-lg font-semibold mb-4 text-red-600">
              Low Stock Alerts
            </h2>

            {lowStock.map((item) => (
              <div
                key={item.id}
                className="border rounded-xl p-4 flex justify-between mb-3"
              >
                <p className="font-semibold">{item.group}</p>
                <p className="text-sm text-gray-600">
                  Remaining Units: {item.units}
                </p>
              </div>
            ))}
          </div>
        )}

      </div>
    </div>
  );
}

function StatCard({ title, value }) {
  return (
    <div className="bg-white p-6 rounded-2xl shadow-sm border text-center">
      <p className="text-gray-500 text-sm">{title}</p>
      <h3 className="text-2xl font-bold mt-2 text-gray-800">{value}</h3>
    </div>
  );
}