package br.com.nfmanager.controller;

import br.com.nfmanager.entity.Certificado;
import br.com.nfmanager.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/certificados")
@RequiredArgsConstructor
public class CertificadoController {

    @GetMapping("/get")
    public List<Certificado> getCertificados() {
        try {
            return CertificateService.listarCertificados();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
