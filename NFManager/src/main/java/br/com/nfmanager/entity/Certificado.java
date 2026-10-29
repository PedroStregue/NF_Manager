package br.com.nfmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@With
public class Certificado {
    String alias;
    String thumbprint;
    String nomeExibicao;
    String emissor;
    String numeroSerie;
    LocalDateTime validoDe;
    LocalDateTime validoAte;
    String documento;
    String tipoDocumento;
    @JsonIgnore
    X509Certificate certificado;

    @Override
    public String toString(){
        String doc = documento != null ? " [" + tipoDocumento + " " + documento + "]" : ";";
        return nomeExibicao + doc + " (válido até " + validoAte.toLocalDate() + ")";
    }
}
