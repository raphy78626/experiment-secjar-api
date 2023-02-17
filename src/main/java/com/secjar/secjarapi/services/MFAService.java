package com.secjar.secjarapi.services;

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

    private final UserService userService;

    public MFAService(UserService userService) {
        this.userService = userService;
    }

    public String generateQRUrl(String userUuid) {
        User user = userService.getUserByUuid(userUuid);

        QrGenerator generator = new ZxingPngQrGenerator();

        QrData data = new QrData.Builder()
                .label(user.getEmail())
                .secret(user.getMFASecret())
                .issuer(mfaIssuer)
                .algorithm(HashingAlgorithm.SHA512)
                .digits(6)
                .period(30)
                .build();

        byte[] imageData;

        try {
            imageData = generator.generate(data);
        } catch (QrGenerationException e) {
            throw new RuntimeException(e);
        }

        return getDataUriForImage(imageData, generator.getImageMimeType());
    }
}
