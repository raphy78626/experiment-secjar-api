package com.secjar.secjarapi.services;

import com.secjar.secjarapi.exceptions.InternalException;
import com.secjar.secjarapi.models.EmailOTPToken;
import com.secjar.secjarapi.models.User;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
public class MFAService {

    @Value("${mfa.issuer}")
    private String mfaIssuer;

    @Value("${emails.oneTimeLoginPassword.subject}")
    private String otpEmailSubject;

    private final UserService userService;
    private final EmailSenderService emailSenderService;
    private final EmailOTPTokenService emailOTPTokenService;

    public MFAService(UserService userService, EmailSenderService emailSenderService, EmailOTPTokenService emailOTPTokenService) {
        this.userService = userService;
        this.emailSenderService = emailSenderService;
        this.emailOTPTokenService = emailOTPTokenService;
    }

    public String generateQRUrl(String userUuid) {
        User user = userService.getUserByUuid(userUuid);

        QrGenerator generator = new ZxingPngQrGenerator();

        QrData data = new QrData.Builder().label(user.getEmail()).secret(user.getMFASecret()).issuer(mfaIssuer).algorithm(HashingAlgorithm.SHA512).digits(6).period(30).build();

        byte[] imageData;

        try {
            imageData = generator.generate(data);
        } catch (QrGenerationException e) {
            throw new InternalException("Problem while generating 2FA QR code", e);
        }

        return getDataUriForImage(imageData, generator.getImageMimeType());
    }

    public void sendEmailOTP(User user) {
        EmailOTPToken previousEmailOTPToken = user.getEmailOTPToken();

        if (previousEmailOTPToken != null) {
            emailOTPTokenService.deleteEmailOTPToken(previousEmailOTPToken);
        }

        String emailOTPToken = emailOTPTokenService.generateEmailOTPToken(user);

        emailSenderService.sendSimpleMail(user.getEmail(), otpEmailSubject, "Password: " + emailOTPToken);
    }

    public boolean validateEmailOTP(String verificationCode, User user) {
        if (user.getEmailOTPToken() != null && user.getEmailOTPToken().getToken().equals(verificationCode) && !emailOTPTokenService.isEmailOTPTokenExpired(user.getEmailOTPToken())) {
            emailOTPTokenService.deleteEmailOTPTokenByToken(verificationCode);
            return true;
        }
        return false;
    }
}
