package com.bloodconnect.backend.controller;

import com.bloodconnect.backend.dto.LoginRequestDTO;
import com.bloodconnect.backend.model.*;
import com.bloodconnect.backend.repository.*;
import com.bloodconnect.backend.security.JwtUtil;
import com.bloodconnect.backend.service.FileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private BloodBankRepository bloodBankRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ================= DONOR REGISTRATION =================
    @PostMapping("/register-donor")
    public String registerDonor(

            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String bloodGroup,
            @RequestParam String location,
            @RequestParam String age,
            @RequestParam String pincode,

            @RequestParam MultipartFile profilePhoto,
            @RequestParam MultipartFile governmentIdProof,
            @RequestParam MultipartFile bloodTestCertificate

    ) throws Exception {

        // 🔥 OTP CHECK
        Optional<EmailOtp> optionalOtp = emailOtpRepository.findByEmail(email);

        if (optionalOtp.isEmpty()) {
            return "Please verify your email using OTP before registration.";
        }

        EmailOtp emailOtp = optionalOtp.get();

        if (!emailOtp.getVerified()) {
            return "Email not verified. Please verify OTP.";
        }

        // 🔥 DUPLICATE CHECK
        if (userRepository.findByEmail(email).isPresent()) {
            return "Email already exists";
        }

        if (userRepository.findByPhone(phone).isPresent()) {
            return "Phone already exists";
        }

        // 🔥 CREATE USER
        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("DONOR");
        user.setStatus("PENDING");

        userRepository.save(user);

        // 🔥 FILE STORAGE
        String photoPath = fileStorageService.saveFile(profilePhoto, "donors", user.getId(), fullName, "profilePhoto");
        String idPath = fileStorageService.saveFile(governmentIdProof, "donors", user.getId(), fullName, "governmentIdProof");
        String certPath = fileStorageService.saveFile(bloodTestCertificate, "donors", user.getId(), fullName, "bloodTestCertificate");

        // 🔥 CREATE DONOR
        Donor donor = new Donor();
        donor.setFullName(fullName);
        donor.setBloodGroup(bloodGroup);
        donor.setLocation(location);
        donor.setPhone(phone);
        donor.setAge(age);
        donor.setPincode(pincode);
        donor.setProfilePhoto(photoPath);
        donor.setGovernmentIdProof(idPath);
        donor.setBloodTestCertificate(certPath);
        donor.setAvailable(true);
        donor.setTotalDonations(0);
        donor.setLastDonationDate(null);
        donor.setNextEligibleDate(null);
        donor.setUser(user);

        donorRepository.save(donor);

        // 🔥 DELETE OTP AFTER SUCCESS
        emailOtpRepository.delete(emailOtp);

        return "Donor registration submitted. Waiting for admin approval.";
    }

    // ================= HOSPITAL REGISTRATION =================
    @PostMapping("/register-hospital")
    public String registerHospital(

            @RequestParam String hospitalName,
            @RequestParam String address,
            @RequestParam String licenseNumber,
            @RequestParam String gstNumber,
            @RequestParam String authorizedPersonIdNumber,
            @RequestParam String contactNumber,
            @RequestParam Boolean hasStorageFacility,

            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,

            @RequestParam MultipartFile registrationCertificate,
            @RequestParam MultipartFile authorizedPersonIdProof

    ) throws Exception {

        Optional<EmailOtp> optionalOtp = emailOtpRepository.findByEmail(email);

        if (optionalOtp.isEmpty()) {
            return "Please verify your email using OTP before registration.";
        }

        EmailOtp emailOtp = optionalOtp.get();

        if (!emailOtp.getVerified()) {
            return "Email not verified. Please verify OTP.";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return "Email already exists";
        }

        if (userRepository.findByPhone(phone).isPresent()) {
            return "Phone already exists";
        }

        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("HOSPITAL");
        user.setStatus("PENDING");

        userRepository.save(user);

        String regDoc = fileStorageService.saveFile(registrationCertificate, "hospitals", user.getId(), hospitalName, "registrationCertificate");
        String idDoc = fileStorageService.saveFile(authorizedPersonIdProof, "hospitals", user.getId(), hospitalName, "authorizedPersonIdProof");

        Hospital hospital = new Hospital();
        hospital.setHospitalName(hospitalName);
        hospital.setAddress(address);
        hospital.setLicenseNumber(licenseNumber);
        hospital.setGstNumber(gstNumber);
        hospital.setAuthorizedPersonIdNumber(authorizedPersonIdNumber);
        hospital.setContactNumber(contactNumber);
        hospital.setHasStorageFacility(hasStorageFacility);
        hospital.setRegistrationCertificatePath(regDoc);
        hospital.setAuthorizedPersonIdProofPath(idDoc);
        hospital.setUser(user);

        hospitalRepository.save(hospital);

        emailOtpRepository.delete(emailOtp);

        return "Hospital registration submitted. Waiting for admin approval.";
    }

    // ================= BLOOD BANK REGISTRATION =================
    @PostMapping("/register-bloodbank")
    public String registerBloodBank(

            @RequestParam String bankName,
            @RequestParam String address,
            @RequestParam String licenseNumber,
            @RequestParam String gstNumber,
            @RequestParam String authorizedPersonIdNumber,
            @RequestParam String contactNumber,

            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,

            @RequestParam MultipartFile registrationCertificate,
            @RequestParam MultipartFile authorizedPersonIdProof

    ) throws Exception {

        Optional<EmailOtp> optionalOtp = emailOtpRepository.findByEmail(email);

        if (optionalOtp.isEmpty()) {
            return "Please verify your email using OTP before registration.";
        }

        EmailOtp emailOtp = optionalOtp.get();

        if (!emailOtp.getVerified()) {
            return "Email not verified. Please verify OTP.";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return "Email already exists";
        }

        if (userRepository.findByPhone(phone).isPresent()) {
            return "Phone already exists";
        }

        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("BLOOD_BANK");
        user.setStatus("PENDING");

        userRepository.save(user);

        String regDoc = fileStorageService.saveFile(registrationCertificate, "bloodbanks", user.getId(), bankName, "registrationCertificate");
        String idDoc = fileStorageService.saveFile(authorizedPersonIdProof, "bloodbanks", user.getId(), bankName, "authorizedPersonIdProof");

        BloodBank bank = new BloodBank();
        bank.setBankName(bankName);
        bank.setAddress(address);
        bank.setLicenseNumber(licenseNumber);
        bank.setGstNumber(gstNumber);
        bank.setAuthorizedPersonIdNumber(authorizedPersonIdNumber);
        bank.setContactNumber(contactNumber);
        bank.setRegistrationCertificatePath(regDoc);
        bank.setAuthorizedPersonIdProofPath(idDoc);
        bank.setUser(user);

        bloodBankRepository.save(bank);

        emailOtpRepository.delete(emailOtp);

        return "Blood bank registration submitted. Waiting for admin approval.";
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public Object login(@RequestBody LoginRequestDTO request) {

        Optional<User> optionalUser = userRepository.findByEmailOrPhone(
                request.getEmailOrPhone(),
                request.getEmailOrPhone()
        );

        if (optionalUser.isEmpty()) {
            return "User not found";
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return "Invalid password";
        }

        if (!user.getStatus().equals("APPROVED")) {
            return "Account not approved by admin";
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // 🔥 DEFAULT VALUE
        Boolean hasStorageFacility = false;

        // 🔥 ONLY FOR HOSPITAL
        if ("HOSPITAL".equals(user.getRole())) {
            Hospital hospital = hospitalRepository.findByUserId(user.getId());

            if (hospital != null && hospital.getHasStorageFacility() != null) {
                hasStorageFacility = hospital.getHasStorageFacility();
            }
        }

        return java.util.Map.of(
                "token", token,
                "role", user.getRole(),
                "userId", user.getId(),
                "hasStorageFacility", hasStorageFacility // ✅ ADDED
        );
    }

    // ================= SEND OTP =================
    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String email) {

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        EmailOtp emailOtp = emailOtpRepository.findByEmail(email)
                .orElse(new EmailOtp());

        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        emailOtp.setVerified(false);

        emailOtpRepository.save(emailOtp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("BloodConnect Pro - OTP Verification");
        message.setText(
                "BloodConnect\n\n" +
                        "Your OTP: " + otp + "\n\n" +
                        "Valid for 5 minutes.\n\n" +
                        "Do not share this OTP."
        );

        mailSender.send(message);

        return "OTP sent successfully";
    }

    // ================= VERIFY OTP =================
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp) {

        Optional<EmailOtp> optionalOtp = emailOtpRepository.findByEmail(email);

        if (optionalOtp.isEmpty()) {
            return "Please request OTP first.";
        }

        EmailOtp emailOtp = optionalOtp.get();

        if (emailOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            return "OTP expired";
        }

        if (!emailOtp.getOtp().equals(otp)) {
            return "Invalid OTP";
        }

        emailOtp.setVerified(true);
        emailOtpRepository.save(emailOtp);

        return "OTP verified";
    }
}