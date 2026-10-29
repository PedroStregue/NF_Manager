package br.com.nfmanager.service;

import br.com.nfmanager.entity.Certificado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private static final Pattern DOCUMENTO = Pattern.compile(":(\\\\d{14}|\\\\d{11})\\\\s*$");

    public static List<Certificado> listarCertificados() throws Exception {
        return listarCertificados(false);
    }

    public static List<Certificado> listarCertificados(boolean incluirExpirados) throws Exception {
        KeyStore ks = KeyStore.getInstance("Windows-MY");
        ks.load(null, null); // não pede senha; o PIN do token só é pedido ao assinar

        List<Certificado> resultado = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();

        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            // Somente entradas que possuem chave privada (não acessa a chave em si).
            if (!ks.isKeyEntry(alias)) continue;

            Certificate cert = ks.getCertificate(alias);
            if (!(cert instanceof X509Certificate x509)) continue;

            if (!permiteAssinatura(x509)) continue;

            LocalDateTime de = paraLocal(x509.getNotBefore());
            LocalDateTime ate = paraLocal(x509.getNotAfter());
            boolean expirado = agora.isAfter(ate) || agora.isBefore(de);
            if (expirado && !incluirExpirados) continue;

            String cn = extrairCN(x509.getSubjectX500Principal().getName());
            String[] doc = extrairDocumento(cn);

            resultado.add(new Certificado(
                    alias,
                    thumbprint(x509),
                    cn,
                    extrairCN(x509.getIssuerX500Principal().getName()),
                    x509.getSerialNumber().toString(16).toUpperCase(),
                    de,
                    ate,
                    doc[0],
                    doc[1],
                    x509));
        }

        resultado.sort(Comparator.comparing(Certificado::getNomeExibicao, String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    /** KeyUsage: digitalSignature (bit 0) ou nonRepudiation/contentCommitment (bit 1). */
    private static boolean permiteAssinatura(X509Certificate cert) {
        boolean[] ku = cert.getKeyUsage();
        if (ku == null) return true; // sem restrição de uso declarada
        return ku[0] || ku[1];
    }

    private static LocalDateTime paraLocal(java.util.Date d) {
        return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
    }

    private static String thumbprint(X509Certificate cert) throws CertificateEncodingException {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(cert.getEncoded());
            return HexFormat.of().withUpperCase().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String extrairCN(String dn) {
        try {
            for (Rdn rdn : new LdapName(dn).getRdns()) {
                if ("CN".equalsIgnoreCase(rdn.getType())) return rdn.getValue().toString();
            }
        } catch (Exception ignored) { }
        return dn;
    }

    /** CN no padrão ICP-Brasil: "NOME:12345678000199" -> {documento, tipo}. */
    private static String[] extrairDocumento(String cn) {
        Matcher m = DOCUMENTO.matcher(cn);
        if (!m.find()) return new String[] { null, null };
        String d = m.group(1);
        return new String[] { d, d.length() == 14 ? "CNPJ" : "CPF" };
    }

    /** Teste rápido: imprime os certificados encontrados. */
    public static void main(String[] args) throws Exception {
        List<Certificado> lista = listarCertificados();
        if (lista.isEmpty()) {
            System.out.println("Nenhum certificado utilizável encontrado.");
            return;
        }
        for (Certificado c : lista) {
            System.out.println(c + " | thumbprint=" + c.getThumbprint() + " | alias=" + c.getAlias());
        }
    }
}
