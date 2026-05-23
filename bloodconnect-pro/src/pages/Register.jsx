import { useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../services/api";

export default function Register() {
  const [role, setRole] = useState("");
  const [step, setStep] = useState(1);
  const navigate = useNavigate();

  const [emailOtpSent, setEmailOtpSent] = useState(false);
  const [otpVerified, setOtpVerified] = useState(false);

  const [formData, setFormData] = useState({
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
    emailOtp: "",

    fullName: "",
    bloodGroup: "",
    location: "",
    age: "",
    pincode: "",
    governmentId: null,
    profilePhoto: null,
    bloodCertificate: null,

    hospitalName: "",
    hospitalAddress: "",
    hospitalLicense: "",
    hospitalGst: "",
    hospitalContactPerson: "",
    hospitalHasStorage: "",
    hospitalRegistrationDoc: null,
    hospitalIdProof: null,

    bankName: "",
    bankAddress: "",
    bankLicense: "",
    bankGst:"",
    bankContactPerson: "",
    bankRegistrationDoc: null,
    bankIdProof: null,
  });

  const handleChange = (e) => {
    const { name, value, type, files } = e.target;
    setFormData({
      ...formData,
      [name]: type === "file" ? files[0] : value,
    });
  };

  // ✅ SEND OTP
  const sendOtp = async () => {
    try {
      await API.post("/auth/send-otp", null, {
        params: { email: formData.email },
      });
      alert("OTP sent to email");
      setEmailOtpSent(true);
    } catch {
      alert("Failed to send OTP");
    }
  };

  // ✅ VERIFY OTP
  const verifyOtp = async () => {
    try {
      const res = await API.post("/auth/verify-otp", null, {
        params: {
          email: formData.email,
          otp: formData.emailOtp,
        },
      });

      if (res.data === "OTP verified") {
        alert("OTP Verified");
        setOtpVerified(true);
      } else {
        alert(res.data);
        setOtpVerified(false);
      }
    } catch {
      alert("OTP verification failed");
      setOtpVerified(false);
    }
  };

  // ✅ REGISTER WITH FULL VALIDATION
  const handleSubmit = async (e) => {
    e.preventDefault();

    // PASSWORD CHECK
    if (formData.password !== formData.confirmPassword) {
      alert("Passwords do not match");
      return;
    }

    // COMMON REQUIRED
    if (!formData.email || !formData.phone || !formData.password) {
      alert("Please fill all required fields");
      return;
    }

    // OTP CHECK
    if (!otpVerified) {
      alert("Please verify email OTP");
      return;
    }

    // ROLE VALIDATION
    if (role === "donor") {
      if (
        !formData.fullName ||
        !formData.bloodGroup ||
        !formData.location ||
        !formData.age ||
        !formData.pincode ||
        !formData.profilePhoto ||
        !formData.governmentId ||
        !formData.bloodCertificate
      ) {
        alert("Please fill all donor details and upload all documents");
        return;
      }
    }

    if (role === "hospital") {
      if (
        !formData.hospitalName ||
        !formData.hospitalAddress ||
        !formData.hospitalLicense ||
        !formData.hospitalGst ||
        !formData.hospitalContactPerson ||
        !formData.hospitalHasStorage ||
        !formData.hospitalRegistrationDoc ||
        !formData.hospitalIdProof
      ) {
        alert("Please fill all hospital details and upload documents");
        return;
      }
    }

    if (role === "bloodbank") {
      if (
        !formData.bankName ||
        !formData.bankAddress ||
        !formData.bankLicense ||
        !formData.bankGst ||
        !formData.bankContactPerson ||
        !formData.bankRegistrationDoc ||
        !formData.bankIdProof
      ) {
        alert("Please fill all blood bank details and upload documents");
        return;
      }
    }

    const data = new FormData();

    data.append("email", formData.email);
    data.append("phone", formData.phone);
    data.append("password", formData.password);

    try {
      let url = "";

      if (role === "donor") {
        url = "/auth/register-donor";

        data.append("fullName", formData.fullName);
        data.append("bloodGroup", formData.bloodGroup);
        data.append("location", formData.location);
        data.append("age", formData.age);
        data.append("pincode", formData.pincode);

        data.append("profilePhoto", formData.profilePhoto);
        data.append("governmentIdProof", formData.governmentId);
        data.append("bloodTestCertificate", formData.bloodCertificate);
      }

      if (role === "hospital") {
        url = "/auth/register-hospital";

        data.append("hospitalName", formData.hospitalName);
        data.append("address", formData.hospitalAddress);
        data.append("licenseNumber", formData.hospitalLicense);
        data.append("gstNumber", formData.hospitalGst);
        data.append("authorizedPersonIdNumber", formData.hospitalContactPerson);
        data.append("contactNumber", formData.phone);
        data.append(
          "hasStorageFacility",
          formData.hospitalHasStorage === "yes"
        );

        data.append("registrationCertificate", formData.hospitalRegistrationDoc);
        data.append("authorizedPersonIdProof", formData.hospitalIdProof);
      }

      if (role === "bloodbank") {
        url = "/auth/register-bloodbank";

        data.append("bankName", formData.bankName);
        data.append("address", formData.bankAddress);
        data.append("licenseNumber", formData.bankLicense);
        data.append("gstNumber", formData.bankGst);
        data.append("authorizedPersonIdNumber", formData.bankContactPerson);
        data.append("contactNumber", formData.phone);

        data.append("registrationCertificate", formData.bankRegistrationDoc);
        data.append("authorizedPersonIdProof", formData.bankIdProof);
      }

      const response = await API.post(url, data);

      if (response.data.includes("submitted")) {
        alert(response.data);
        navigate("/login");
      } else {
        alert(response.data);
      }

    } catch (err) {
      console.error(err);
      alert("Registration failed");
    }
  };

  const FileUpload = ({ label, name }) => (
    <div>
      <p className="text-sm font-semibold text-gray-700 mb-1">{label}</p>
      <label className="flex items-center justify-center w-full h-28 border-2 border-dashed border-gray-300 hover:border-red-500 rounded-xl cursor-pointer bg-gray-50 hover:bg-red-50 transition">
        <span className="text-sm text-gray-500">
          {formData[name] ? formData[name].name : "Click to upload document"}
        </span>
        <input
          type="file"
          name={name}
          onChange={handleChange}
          required
          className="hidden"
        />
      </label>
    </div>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-red-50 via-white to-red-100 pt-16 px-6 pb-10">
      <div className="max-w-5xl mx-auto bg-white rounded-3xl shadow-2xl p-10">

        <h2 className="text-3xl font-bold text-center text-red-600 mb-8">
          BloodConnect Pro – Secure Registration
        </h2>

        {!role && (
          <div className="grid md:grid-cols-3 gap-6">
            {["donor", "hospital", "bloodbank"].map((type) => (
              <div
                key={type}
                onClick={() => setRole(type)}
                className="cursor-pointer border p-6 rounded-2xl hover:shadow-lg hover:border-red-500 transition text-center"
              >
                <h3 className="text-xl font-semibold capitalize mb-2">
                  {type === "bloodbank" ? "Blood Bank" : type}
                </h3>
              </div>
            ))}
          </div>
        )}

        {role && (
          <form onSubmit={handleSubmit} className="space-y-6">

            <div className="flex justify-between text-sm font-medium text-gray-400 mb-6">
              <span className={step >= 1 ? "text-red-600" : ""}>
                1. Details & Documents
              </span>
              <span className={step >= 2 ? "text-red-600" : ""}>
                2. Security & OTP Verification
              </span>
            </div>

            {step === 1 && (
              <>
                {role === "donor" && (
                  <>
                    <input type="text" name="fullName" placeholder="Full Name" required onChange={handleChange} className="input" />
                    <select name="bloodGroup" required onChange={handleChange} className="input">
                      <option value="">Select Blood Group</option>
                      <option>A+</option><option>A-</option>
                      <option>B+</option><option>B-</option>
                      <option>O+</option><option>O-</option>
                      <option>AB+</option><option>AB-</option>
                    </select>
                    <input type="text" name="location" placeholder="Location" required onChange={handleChange} className="input" />
                    <input type="text" name="age" placeholder="Age" required onChange={handleChange} className="input" />
                    <input type="text" name="pincode" placeholder="Pincode" required onChange={handleChange} className="input" />

                    <FileUpload label="Government ID" name="governmentId" />
                    <FileUpload label="Profile Photo" name="profilePhoto" />
                    <FileUpload label="Blood Test Certificate" name="bloodCertificate" />
                  </>
                )}

                {role === "hospital" && (
                  <>
                    <input type="text" name="hospitalName" placeholder="Hospital Name" required onChange={handleChange} className="input" />
                    <input type="text" name="hospitalAddress" placeholder="Hospital Address" required onChange={handleChange} className="input" />
                    <input type="text" name="hospitalLicense" placeholder="Medical License Number" required onChange={handleChange} className="input" />
                    <input type="text" name="hospitalGst" placeholder="GST Number" required onChange={handleChange} className="input" />
                    <input type="text" name="hospitalContactPerson" placeholder="Authorized Contact Person ID Number" required onChange={handleChange} className="input" />

                    <select name="hospitalHasStorage" required onChange={handleChange} className="input">
                      <option value="">Does Hospital Have Blood Storage Facility?</option>
                      <option value="yes">Yes</option>
                      <option value="no">No</option>
                    </select>

                    <FileUpload label="Hospital Registration Document" name="hospitalRegistrationDoc" />
                    <FileUpload label="Authorized Person ID Proof" name="hospitalIdProof" />
                  </>
                )}

                {role === "bloodbank" && (
                  <>
                    <input type="text" name="bankName" placeholder="Blood Bank Name" required onChange={handleChange} className="input" />
                    <input type="text" name="bankAddress" placeholder="Blood Bank Address" required onChange={handleChange} className="input" />
                    <input type="text" name="bankLicense" placeholder="License Number" required onChange={handleChange} className="input" />
                    <input
                        type="text"
                        name="bankGst"
                        placeholder="GST Number"
                        required
                        onChange={handleChange}
                        className="input"
                      />
                    <input type="text" name="bankContactPerson" placeholder="Authorized Contact Person ID Number" required onChange={handleChange} className="input" />

                    <FileUpload label="Blood Bank Registration Document" name="bankRegistrationDoc" />
                    <FileUpload label="Authorized Person ID Proof" name="bankIdProof" />
                  </>
                )}

                <button type="button" onClick={() => setStep(2)} className="bg-red-600 text-white px-6 py-2 rounded-xl mt-4">
                  Continue to Security
                </button>
              </>
            )}

            {step === 2 && (
              <>
                <input type="email" name="email" placeholder="Official Email" required onChange={handleChange} className="input" />

                <button type="button" onClick={sendOtp} className="otp-btn">
                  Send Email OTP
                </button>

                {emailOtpSent && (
                  <>
                    <input type="text" name="emailOtp" placeholder="Enter Email OTP" required onChange={handleChange} className="input" />
                    <button type="button" onClick={verifyOtp} className="otp-btn">
                      Verify OTP
                    </button>
                  </>
                )}

                <input type="tel" name="phone" placeholder="Phone Number" required onChange={handleChange} className="input" />

                <input type="password" name="password" placeholder="Create Password" required onChange={handleChange} className="input" />
                <input type="password" name="confirmPassword" placeholder="Confirm Password" required onChange={handleChange} className="input" />

                <div className="flex justify-between mt-4">
                  <button type="button" onClick={() => setStep(1)} className="bg-gray-300 px-6 py-2 rounded-xl">
                    Back
                  </button>
                  <button type="submit" className="bg-green-600 text-white px-6 py-2 rounded-xl">
                    Submit Application
                  </button>
                </div>
              </>
            )}
          </form>
        )}
      </div>

      <style>
        {`
        .input {
          width: 100%;
          border: 1px solid #e5e7eb;
          padding: 12px;
          border-radius: 12px;
          outline: none;
          margin-bottom: 12px;
        }
        .input:focus {
          border-color: #dc2626;
        }
        .otp-btn {
          background: #fee2e2;
          color: #b91c1c;
          padding: 8px 14px;
          border-radius: 8px;
          margin-bottom: 10px;
          font-size: 14px;
        }
        `}
      </style>
    </div>
  );
}